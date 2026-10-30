package com.polyglot.academy.android

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.os.PowerManager
import android.os.StatFs
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Base64
import androidx.core.content.ContextCompat
import com.polyglot.academy.core.ai.NetworkState
import com.polyglot.academy.core.device.DeviceInfo
import com.polyglot.academy.core.media.ExtractionSupport
import com.polyglot.academy.core.model.MaterialType
import com.polyglot.academy.core.repo.SecretStore
import com.polyglot.academy.core.service.DeviceOcr
import com.polyglot.academy.core.service.OcrPage
import com.polyglot.academy.core.service.PlatformServices
import com.polyglot.academy.core.speech.RecognitionEvent
import com.polyglot.academy.core.speech.SpeechAvailability
import com.polyglot.academy.core.speech.SpeechErrorKind
import com.polyglot.academy.core.speech.SpeechRecognizerService
import com.polyglot.academy.core.speech.SpeechSynthesizerService
import com.polyglot.academy.core.speech.SynthesisEvent
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.KeyStore
import java.util.Locale
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class AndroidPlatformServices(private val context: Context) : PlatformServices {
    override val recognizer: SpeechRecognizerService = AndroidSpeechRecognizer(context)
    override val synthesizer: SpeechSynthesizerService = AndroidTextToSpeech(context)
    override val secrets: SecretStore = KeystoreSecretStore(context)
    override val audioCapture: com.polyglot.academy.core.speech.AudioCapture = AndroidAudioCapture(context)
    override val audioOutput: com.polyglot.academy.core.speech.AudioOutput = AndroidAudioOutput()

    override fun network(): NetworkState {
        val cm = context.getSystemService(ConnectivityManager::class.java) ?: return NetworkState.OFFLINE
        val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return NetworkState.OFFLINE
        return if (caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) NetworkState.ONLINE else NetworkState.OFFLINE
    }

    override fun deviceInfo(): DeviceInfo {
        val am = context.getSystemService(ActivityManager::class.java)
        val mem = ActivityManager.MemoryInfo().also { am?.getMemoryInfo(it) }
        val stat = StatFs(context.filesDir.absolutePath)
        val battery = context.getSystemService(BatteryManager::class.java)?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val pm = context.getSystemService(PowerManager::class.java)
        val thermal = if (Build.VERSION.SDK_INT >= 29) (pm?.currentThermalStatus ?: 0) >= PowerManager.THERMAL_STATUS_SEVERE else false
        return DeviceInfo(
            totalRamMb = mem.totalMem / (1024 * 1024), availableStorageMb = stat.availableBytes / (1024 * 1024), osApiLevel = Build.VERSION.SDK_INT,
            cpuCores = Runtime.getRuntime().availableProcessors(), is64Bit = Build.SUPPORTED_64_BIT_ABIS.isNotEmpty(), isLowRamDevice = am?.isLowRamDevice ?: false,
            batteryPercent = battery, isPowerSaveMode = pm?.isPowerSaveMode == true, thermalThrottled = thermal,
            hasMicrophone = context.packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE), platformName = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
        )
    }

    // PDF text is extracted in shared code; transcription is not bundled. Photos can be read on the
    // device for the languages in [deviceOcr], otherwise with a vision AI provider on request.
    override val extractionSupport: Map<MaterialType, ExtractionSupport> = emptyMap()

    override val deviceOcr: DeviceOcr by lazy { TesseractOcr(context) }

    override suspend fun scheduleReminder(enabled: Boolean, hour: Int, minute: Int, days: Set<Int>): Boolean =
        Reminders.schedule(context, enabled, hour, minute, days)

    override val platformName: String get() = "Android ${Build.VERSION.RELEASE}"

    /** Renders pages with the framework PdfRenderer (needs a seekable file, so a private temp copy is used). */
    override suspend fun renderPdfPages(pdf: ByteArray, maxPages: Int): List<OcrPage>? = withContext(Dispatchers.IO) {
        val file = File.createTempFile("ocr", ".pdf", context.cacheDir)
        try {
            file.writeBytes(pdf)
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { fd ->
                PdfRenderer(fd).use { renderer ->
                    (0 until minOf(renderer.pageCount, maxPages)).map { i ->
                        renderer.openPage(i).use { page ->
                            val width = OCR_PAGE_WIDTH
                            val height = (width.toFloat() * page.height / page.width).toInt().coerceAtLeast(1)
                            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                            bitmap.eraseColor(Color.WHITE)
                            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            val out = ByteArrayOutputStream()
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                            bitmap.recycle()
                            OcrPage("image/jpeg", out.toByteArray())
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Damaged or password-protected files cannot be rendered.
            null
        } finally {
            file.delete()
        }
    }

    private companion object {
        /** Wide enough for small print, small enough to stay well under vision API image limits. */
        const val OCR_PAGE_WIDTH = 1600
    }
}

/** Wraps android.speech.SpeechRecognizer. Must be used from the main thread, hence flowOn(Main). */
class AndroidSpeechRecognizer(private val context: Context) : SpeechRecognizerService {
    private var active: SpeechRecognizer? = null

    override suspend fun availability(localeTag: String): SpeechAvailability {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) return SpeechAvailability.NOT_INSTALLED
        return if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) SpeechAvailability.AVAILABLE
        else SpeechAvailability.PERMISSION_REQUIRED
    }

    override val processesAudioOnDevice: Boolean? get() =
        if (Build.VERSION.SDK_INT >= 31) SpeechRecognizer.isOnDeviceRecognitionAvailable(context) else null

    override fun listen(localeTag: String): Flow<RecognitionEvent> = callbackFlow {
        val recognizer = if (Build.VERSION.SDK_INT >= 31 && SpeechRecognizer.isOnDeviceRecognitionAvailable(context))
            SpeechRecognizer.createOnDeviceSpeechRecognizer(context) else SpeechRecognizer.createSpeechRecognizer(context)
        active = recognizer
        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { trySend(RecognitionEvent.Ready) }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) { trySend(RecognitionEvent.LevelChanged(((rmsdB + 2f) / 12f).coerceIn(0f, 1f))) }
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) { trySend(RecognitionEvent.Error(mapError(error))); close() }
            override fun onResults(results: Bundle?) {
                val list = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).orEmpty()
                val conf = results?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)?.firstOrNull()
                trySend(RecognitionEvent.Final(list.firstOrNull().orEmpty(), conf, list.drop(1))); close()
            }
            override fun onPartialResults(partialResults: Bundle?) {
                partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.let { trySend(RecognitionEvent.Partial(it)) }
            }
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, localeTag)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }
        recognizer.startListening(intent)
        // Cancelling the flow (leaving the screen, pressing cancel) always stops the microphone.
        awaitClose { recognizer.cancel(); recognizer.destroy(); if (active === recognizer) active = null }
    }.flowOn(Dispatchers.Main)

    override fun stopListening() { active?.stopListening() }

    private fun mapError(e: Int) = when (e) {
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> SpeechErrorKind.PERMISSION_DENIED
        SpeechRecognizer.ERROR_AUDIO -> SpeechErrorKind.MICROPHONE_UNAVAILABLE
        SpeechRecognizer.ERROR_NO_MATCH -> SpeechErrorKind.NO_SPEECH_DETECTED
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> SpeechErrorKind.RECOGNITION_TIMEOUT
        SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT, SpeechRecognizer.ERROR_SERVER -> SpeechErrorKind.NETWORK
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> SpeechErrorKind.RECOGNIZER_BUSY
        SpeechRecognizer.ERROR_CLIENT -> SpeechErrorKind.CANCELLED
        else -> if (Build.VERSION.SDK_INT >= 31 && (e == SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED || e == SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE)) SpeechErrorKind.LANGUAGE_UNSUPPORTED else SpeechErrorKind.UNKNOWN
    }
}

/** Wraps android.speech.tts.TextToSpeech with lazy initialisation. */
class AndroidTextToSpeech(private val context: Context) : SpeechSynthesizerService {
    private var ready: CompletableDeferred<TextToSpeech?>? = null
    @Volatile private var engineRef: TextToSpeech? = null

    private suspend fun engine(): TextToSpeech? {
        val d = ready ?: CompletableDeferred<TextToSpeech?>().also { def ->
            ready = def
            withContext(Dispatchers.Main) {
                var tts: TextToSpeech? = null
                tts = TextToSpeech(context) { status -> val ok = if (status == TextToSpeech.SUCCESS) tts else null; engineRef = ok; def.complete(ok) }
            }
        }
        return d.await()
    }

    override suspend fun isLanguageAvailable(localeTag: String): Boolean {
        val tts = engine() ?: return false
        return tts.isLanguageAvailable(Locale.forLanguageTag(localeTag)) >= TextToSpeech.LANG_AVAILABLE
    }

    override fun speak(text: String, localeTag: String, rate: Float): Flow<SynthesisEvent> = callbackFlow {
        val tts = engine()
        if (tts == null) { trySend(SynthesisEvent.Error(SpeechErrorKind.TTS_UNAVAILABLE)); close(); return@callbackFlow }
        val id = UUID.randomUUID().toString()
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) { if (utteranceId == id) trySend(SynthesisEvent.Started) }
            override fun onDone(utteranceId: String?) { if (utteranceId == id) { trySend(SynthesisEvent.Done); close() } }
            @Deprecated("Deprecated in Java") override fun onError(utteranceId: String?) { if (utteranceId == id) { trySend(SynthesisEvent.Error(SpeechErrorKind.TTS_FAILED)); close() } }
            override fun onStop(utteranceId: String?, interrupted: Boolean) { if (utteranceId == id) { trySend(SynthesisEvent.Done); close() } }
            override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) { if (utteranceId == id) trySend(SynthesisEvent.Progress(start, end)) }
        })
        tts.language = Locale.forLanguageTag(localeTag)
        tts.setSpeechRate(rate.coerceIn(0.3f, 2f))
        if (tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, id) != TextToSpeech.SUCCESS) { trySend(SynthesisEvent.Error(SpeechErrorKind.TTS_FAILED)); close() }
        awaitClose { }
    }

    override fun stop() { engineRef?.stop() }
}

/**
 * API keys encrypted with an AES-256-GCM key that never leaves the Android Keystore. The
 * ciphertext lives in a private SharedPreferences file excluded from backups.
 */
class KeystoreSecretStore(context: Context) : SecretStore {
    private val prefs = context.getSharedPreferences("polyglot_secrets", Context.MODE_PRIVATE)
    private val alias = "polyglot_secret_key"

    private fun key(): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (ks.getEntry(alias, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }
        val gen = KeyGenerator.getInstance(android.security.keystore.KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        gen.init(android.security.keystore.KeyGenParameterSpec.Builder(alias,
            android.security.keystore.KeyProperties.PURPOSE_ENCRYPT or android.security.keystore.KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(android.security.keystore.KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256).build())
        return gen.generateKey()
    }

    override suspend fun get(key: String): String? = withContext(Dispatchers.IO) {
        val stored = prefs.getString(key, null) ?: return@withContext null
        runCatching {
            val bytes = Base64.decode(stored, Base64.NO_WRAP)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, bytes.copyOfRange(0, 12)))
            String(cipher.doFinal(bytes.copyOfRange(12, bytes.size)), Charsets.UTF_8)
        }.getOrNull()
    }

    override suspend fun put(key: String, value: String) = withContext(Dispatchers.IO) {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val out = cipher.iv + cipher.doFinal(value.toByteArray(Charsets.UTF_8))
        prefs.edit().putString(key, Base64.encodeToString(out, Base64.NO_WRAP)).apply()
    }

    override suspend fun remove(key: String) = withContext(Dispatchers.IO) { prefs.edit().remove(key).apply() }
    override suspend fun keys(): Set<String> = prefs.all.keys
    override val protectionDescription = "Android Keystore (AES-256-GCM)"
}
