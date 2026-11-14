package com.polyglot.academy.android

import android.content.Context
import android.media.AudioFormat
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import com.polyglot.academy.core.media.DeviceTranscriber
import com.polyglot.academy.core.media.TranscriptionResult
import com.polyglot.academy.core.media.TranscriptionStage
import com.polyglot.academy.core.model.LanguageCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteOrder

/** whisper.cpp through JNI (src/main/cpp). */
internal object WhisperNative {
    init { System.loadLibrary("polyglot_ml") }
    external fun initContext(modelPath: String): Long
    external fun freeContext(context: Long)
    external fun transcribe(context: Long, samples: FloatArray, language: String, threads: Int): ByteArray?
}

/**
 * Transcribes speech on the device with Whisper (base model, 5-bit quantised). The model is downloaded
 * once on the learner's request from the whisper.cpp model repository on Hugging Face (nothing about the
 * learner is sent) and checked against its SHA-256 before it is used. Audio is decoded with the
 * platform's MediaCodec, mixed to mono and resampled to 16 kHz; it never leaves the device.
 */
class WhisperTranscriber(private val context: Context) : DeviceTranscriber {
    companion object {
        private const val MODEL_URL = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-base-q5_1.bin"
        private const val MODEL_SIZE = 59_707_625L
        private const val MODEL_SHA256 = "422f1ae452ade6f30a004d7e5c6a43195e4433bc370bf23fac9cc591f01a8898"
        private const val RATE = 16_000
        private val codes = mapOf(
            LanguageCode.ENGLISH to "en", LanguageCode.MANDARIN to "zh", LanguageCode.SPANISH to "es", LanguageCode.FRENCH to "fr",
            LanguageCode.ARABIC to "ar", LanguageCode.TURKISH to "tr", LanguageCode.URDU to "ur", LanguageCode.MALAY to "ms",
        )
    }

    // Kept out of backups, like the OCR models.
    private val model get() = File(context.noBackupFilesDir, "whisper/ggml-base-q5_1.bin")

    override val languages: Set<LanguageCode> = codes.keys
    override val modelSizeBytes = MODEL_SIZE
    override fun isModelReady() = model.length() == MODEL_SIZE

    override suspend fun downloadModel(onProgress: (Float) -> Unit): Result<Unit> =
        ModelDownload.fetch(MODEL_URL, MODEL_SIZE, MODEL_SHA256, model, onProgress)

    override fun deleteModel() { model.delete() }

    override suspend fun transcribe(source: String, language: LanguageCode, onStage: (TranscriptionStage) -> Unit): TranscriptionResult =
        withContext(Dispatchers.Default) {
            if (!isModelReady()) return@withContext TranscriptionResult.ModelMissing
            onStage(TranscriptionStage.DECODING)
            val decoded = runCatching { decode(source, DeviceTranscriber.MAX_MINUTES * 60) }.getOrNull() ?: return@withContext TranscriptionResult.Unreadable
            val (samples, truncated) = decoded
            if (samples.isEmpty()) return@withContext TranscriptionResult.Unreadable
            onStage(TranscriptionStage.RECOGNISING)
            val ctx = WhisperNative.initContext(model.path)
            if (ctx == 0L) return@withContext TranscriptionResult.Failed("model")
            try {
                val threads = Runtime.getRuntime().availableProcessors().coerceIn(1, 4)
                val bytes = WhisperNative.transcribe(ctx, samples, codes.getValue(language), threads) ?: return@withContext TranscriptionResult.Failed("whisper")
                val text = bytes.decodeToString().lines().map { it.trim() }.filter { it.isNotEmpty() }.joinToString("\n")
                if (text.isBlank()) TranscriptionResult.NoSpeech else TranscriptionResult.Text(text, samples.size / RATE, truncated)
            } finally {
                WhisperNative.freeContext(ctx)
            }
        }

    /** The first audio track as 16 kHz mono floats, at most [maxSeconds]; true when it was cut. */
    private fun decode(path: String, maxSeconds: Int): Pair<FloatArray, Boolean>? {
        val extractor = MediaExtractor()
        try {
            extractor.setDataSource(path)
            val track = (0 until extractor.trackCount).firstOrNull { extractor.getTrackFormat(it).getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true } ?: return null
            extractor.selectTrack(track)
            val format = extractor.getTrackFormat(track)
            val codec = MediaCodec.createDecoderByType(format.getString(MediaFormat.KEY_MIME)!!)
            var rate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
            var channels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
            var floatPcm = false
            var mono = FloatArray(rate * 30)
            var count = 0
            var truncated = false
            val info = MediaCodec.BufferInfo()
            try {
                codec.configure(format, null, null, 0)
                codec.start()
                var inputDone = false
                var outputDone = false
                while (!outputDone) {
                    if (!inputDone) {
                        val i = codec.dequeueInputBuffer(10_000)
                        if (i >= 0) {
                            val size = extractor.readSampleData(codec.getInputBuffer(i)!!, 0)
                            if (size < 0) { codec.queueInputBuffer(i, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM); inputDone = true }
                            else { codec.queueInputBuffer(i, 0, size, extractor.sampleTime, 0); extractor.advance() }
                        }
                    }
                    val o = codec.dequeueOutputBuffer(info, 10_000)
                    when {
                        o == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                            val f = codec.outputFormat
                            rate = f.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                            channels = f.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                            floatPcm = f.containsKey(MediaFormat.KEY_PCM_ENCODING) && f.getInteger(MediaFormat.KEY_PCM_ENCODING) == AudioFormat.ENCODING_PCM_FLOAT
                        }
                        o >= 0 -> {
                            val buf = codec.getOutputBuffer(o)!!.order(ByteOrder.LITTLE_ENDIAN)
                            buf.position(info.offset); buf.limit(info.offset + info.size)
                            val frames = info.size / ((if (floatPcm) 4 else 2) * channels)
                            val limit = maxSeconds * rate
                            for (k in 0 until frames) {
                                if (count >= limit) { truncated = true; break }
                                var sum = 0f
                                repeat(channels) { sum += if (floatPcm) buf.float else buf.short / 32768f }
                                if (count == mono.size) mono = mono.copyOf(mono.size * 2)
                                mono[count++] = sum / channels
                            }
                            codec.releaseOutputBuffer(o, false)
                            if (truncated || info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) outputDone = true
                        }
                    }
                }
            } finally {
                runCatching { codec.stop() }; codec.release()
            }
            return resample(mono, count, rate) to truncated
        } finally {
            extractor.release()
        }
    }

    /** Linear resampling to 16 kHz (enough for speech recognition). */
    private fun resample(src: FloatArray, count: Int, rate: Int): FloatArray {
        if (rate == RATE) return src.copyOf(count)
        val out = FloatArray((count.toLong() * RATE / rate).toInt())
        val step = rate.toDouble() / RATE
        for (i in out.indices) {
            val pos = i * step
            val j = pos.toInt().coerceAtMost(count - 1)
            val next = (j + 1).coerceAtMost(count - 1)
            val f = (pos - j).toFloat()
            out[i] = src[j] * (1 - f) + src[next] * f
        }
        return out
    }
}
