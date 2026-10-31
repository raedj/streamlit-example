package com.polyglot.academy.ui.feature.me

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.polyglot.academy.core.ai.AiError
import com.polyglot.academy.core.model.ImportedMaterial
import com.polyglot.academy.core.model.Languages
import com.polyglot.academy.core.model.MaterialType
import com.polyglot.academy.core.service.OcrPage
import com.polyglot.academy.core.service.OcrResult
import com.polyglot.academy.core.service.OcrService
import com.polyglot.academy.ui.app.LocalApp
import com.polyglot.academy.ui.app.rememberToast
import com.polyglot.academy.ui.designsystem.ConfirmDialog
import com.polyglot.academy.ui.designsystem.PolyCard
import com.polyglot.academy.ui.designsystem.PolyglotTheme
import com.polyglot.academy.ui.designsystem.PrimaryButton
import com.polyglot.academy.ui.designsystem.SecondaryButton
import com.polyglot.academy.ui.i18n.K
import com.polyglot.academy.ui.i18n.Labels
import com.polyglot.academy.ui.i18n.LocalStrings
import kotlinx.coroutines.launch

/**
 * Files picked in this session that could be read with AI. Only kept in memory (never stored),
 * and only the most recent few, so a large scan doesn't stay around.
 */
object PendingOcrFiles {
    private const val KEEP = 3
    private val files = LinkedHashMap<String, Pair<String, ByteArray>>()

    fun put(materialId: String, mimeType: String, bytes: ByteArray) {
        files.remove(materialId)
        files[materialId] = mimeType to bytes
        while (files.size > KEEP) files.remove(files.keys.first())
    }

    fun get(materialId: String): Pair<String, ByteArray>? = files[materialId]
    fun remove(materialId: String) { files.remove(materialId) }
}

@Composable
internal fun OcrCard(material: ImportedMaterial) {
    val app = LocalApp.current
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val scope = rememberCoroutineScope()
    val toast = rememberToast()
    val pending = PendingOcrFiles.get(material.id)
    var confirm by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf<String?>(null) }

    fun read(onDevice: Boolean) {
        val (mime, bytes) = pending ?: return
        scope.launch {
            progress = s[K.MATERIAL_OCR_PREPARING]
            val pages = if (material.type == MaterialType.PDF) app.services.platform.renderPdfPages(bytes, OcrService.MAX_PAGES)
                else listOf(OcrPage(mime, bytes))
            if (pages.isNullOrEmpty()) {
                progress = null
                toast(s[K.MATERIAL_OCR_NO_RENDER])
                return@launch
            }
            val (_, result) = app.services.materials.recognizeText(material, pages, onDevice) { i, n -> progress = s.f(K.MATERIAL_OCR_PROGRESS, i, n) }
            progress = null
            toast(when (result) {
                is OcrResult.Text -> { PendingOcrFiles.remove(material.id); s.f(K.MATERIAL_OCR_DONE, result.pagesRead - result.pagesWithoutText, result.pagesRead) }
                OcrResult.NoText -> s[K.MATERIAL_OCR_NO_TEXT]
                is OcrResult.TooLarge -> s.f(K.MATERIAL_OCR_TOO_LARGE, result.maxBytes / 1_000_000)
                is OcrResult.Failed -> when (result.error) {
                    is AiError.BlockedByPrivacy -> s[K.MATERIAL_OCR_PRIVACY]
                    is AiError.NoProviderAvailable, is AiError.NotConfigured -> s[K.MATERIAL_OCR_NO_PROVIDER]
                    else -> s.f(K.MATERIAL_OCR_FAILED, result.error.message.orEmpty())
                }
            })
        }
    }

    // Reading on the device needs no confirmation: the file does not leave the device.
    if (app.services.materials.canReadOnDevice(material)) PolyCard {
        Text(s[K.MATERIAL_OCR_DEVICE_TITLE], style = MaterialTheme.typography.titleSmall)
        Text(s.f(K.MATERIAL_OCR_DEVICE_BODY, Labels.language(s, material.language)), style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
        when {
            pending == null -> Text(s[K.MATERIAL_OCR_REIMPORT], style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
            progress != null -> Text(progress!!, style = MaterialTheme.typography.bodyMedium)
            else -> PrimaryButton(s[K.MATERIAL_OCR_DEVICE_START], { read(onDevice = true) }, Modifier.fillMaxWidth())
        }
    }
    PolyCard {
        Text(s[K.MATERIAL_OCR_TITLE], style = MaterialTheme.typography.titleSmall)
        Text(s[K.MATERIAL_OCR_BODY], style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
        when {
            pending == null -> Text(s[K.MATERIAL_OCR_REIMPORT], style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
            progress != null -> Text(progress!!, style = MaterialTheme.typography.bodyMedium)
            else -> SecondaryButton(s[K.MATERIAL_OCR_START], { confirm = true }, Modifier.fillMaxWidth())
        }
    }
    if (confirm && pending != null) ConfirmDialog(s[K.MATERIAL_OCR_CONFIRM_TITLE], s[K.MATERIAL_OCR_CONFIRM_BODY], s[K.MATERIAL_OCR_START], s[K.CANCEL],
        onConfirm = { confirm = false; read(onDevice = false) }, onDismiss = { confirm = false })
}
