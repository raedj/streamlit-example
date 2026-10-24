package com.polyglot.academy.ui.platform

import com.polyglot.academy.core.ai.NetworkState
import com.polyglot.academy.core.device.DeviceInfo
import com.polyglot.academy.core.media.ExtractionSupport
import com.polyglot.academy.core.model.MaterialType
import com.polyglot.academy.core.repo.InMemorySecretStore
import com.polyglot.academy.core.repo.SecretStore
import com.polyglot.academy.core.service.PlatformServices
import com.polyglot.academy.core.speech.SpeechRecognizerService
import com.polyglot.academy.core.speech.SpeechSynthesizerService
import com.polyglot.academy.core.speech.UnavailableSpeechRecognizer
import com.polyglot.academy.core.speech.UnavailableSpeechSynthesizer
import com.polyglot.academy.ui.app.PickedFile
import com.polyglot.academy.ui.app.PlatformUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Platform services for the JVM desktop verification harness. It is NOT the product: speech and
 * reminders are reported as unavailable, and secrets are kept in memory only.
 */
class DesktopPlatformServices(
    override val recognizer: SpeechRecognizerService = UnavailableSpeechRecognizer,
    override val synthesizer: SpeechSynthesizerService = UnavailableSpeechSynthesizer,
    override val secrets: SecretStore = InMemorySecretStore(),
    var networkState: NetworkState = NetworkState.ONLINE,
) : PlatformServices {
    override fun network() = networkState
    override fun deviceInfo(): DeviceInfo {
        val rt = Runtime.getRuntime()
        val home = File(System.getProperty("user.home"))
        return DeviceInfo(
            totalRamMb = rt.maxMemory() / (1024 * 1024), availableStorageMb = home.usableSpace / (1024 * 1024), osApiLevel = 0,
            cpuCores = rt.availableProcessors(), is64Bit = System.getProperty("os.arch").contains("64"), isLowRamDevice = false,
            batteryPercent = null, isPowerSaveMode = false, thermalThrottled = false, hasMicrophone = false, platformName = "Desktop JVM (verification harness)",
        )
    }
    override val extractionSupport: Map<MaterialType, ExtractionSupport> = emptyMap()
    override suspend fun scheduleReminder(enabled: Boolean, hour: Int, minute: Int, days: Set<Int>): Boolean = !enabled
    override val platformName = "Desktop JVM (verification harness)"
}

class DesktopPlatformUi(private val exportDir: File = File(System.getProperty("user.home"))) : PlatformUi {
    override val canPickFiles = true
    override suspend fun pickFile(): PickedFile? = withContext(Dispatchers.IO) {
        val dialog = java.awt.FileDialog(null as java.awt.Frame?, "Import", java.awt.FileDialog.LOAD)
        dialog.isVisible = true
        val f = dialog.files.firstOrNull() ?: return@withContext null
        val name = f.name.lowercase()
        val textual = name.endsWith(".txt") || name.endsWith(".md") || name.endsWith(".srt") || name.endsWith(".vtt")
        val mime = when {
            name.endsWith(".srt") -> "application/x-subrip"; name.endsWith(".vtt") -> "text/vtt"; textual -> "text/plain"
            name.endsWith(".pdf") -> "application/pdf"; else -> java.nio.file.Files.probeContentType(f.toPath()) ?: "application/octet-stream"
        }
        PickedFile(f.name, mime, f.length(), if (textual && f.length() < 5 * 1024 * 1024) f.readText() else null)
    }
    override suspend fun requestMicrophonePermission() = false
    override suspend fun requestNotificationPermission() = false
    override suspend fun saveExport(fileName: String, content: String): String = withContext(Dispatchers.IO) {
        val f = File(exportDir, fileName); f.writeText(content); f.absolutePath
    }
    override fun openAppSettings() {}
    override val appVersion = "0.1.0-dev (desktop harness)"
}
