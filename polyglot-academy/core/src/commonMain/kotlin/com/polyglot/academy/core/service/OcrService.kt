package com.polyglot.academy.core.service

import com.polyglot.academy.core.ai.AiCapability
import com.polyglot.academy.core.ai.AiError
import com.polyglot.academy.core.ai.AiOutcome
import com.polyglot.academy.core.ai.ChatMessage
import com.polyglot.academy.core.ai.ChatRequest
import com.polyglot.academy.core.ai.ChatRole
import com.polyglot.academy.core.ai.ImagePart
import com.polyglot.academy.core.model.DataCategory
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.Languages
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/** One page or photo to read: JPEG/PNG/WebP bytes. */
class OcrPage(val mimeType: String, val bytes: ByteArray)

sealed interface OcrResult {
    data class Text(val text: String, val pagesRead: Int, val pagesWithoutText: Int) : OcrResult
    data object NoText : OcrResult
    data class Failed(val error: AiError) : OcrResult
    data class TooLarge(val maxBytes: Int) : OcrResult
}

/** On-device text recognition (Tesseract on Android). Nothing leaves the device. */
interface DeviceOcr {
    /** Languages whose recognition data is bundled with the app. */
    val languages: Set<LanguageCode>
    /** The text of one page, or an empty string when none was found. */
    suspend fun recognize(page: OcrPage, language: LanguageCode): String
}

/**
 * Reads text from images with a vision-capable AI provider. This sends the images to that
 * provider, so it only runs on an explicit request and is subject to the privacy settings
 * (imported material is a sensitive category). Nothing is guessed: pages the model reports as
 * empty are counted, and provider errors are returned as they are.
 */
class OcrService(private val providers: ProviderService, private val device: DeviceOcr? = null) {
    /** Whether [language] can be read on the device, without sending anything to a provider. */
    fun canReadOnDevice(language: LanguageCode): Boolean = device != null && language in device.languages

    /** Reads [pages] on the device. Same result shape as [recognize]; there is no provider error to report. */
    suspend fun recognizeOnDevice(pages: List<OcrPage>, language: LanguageCode, onPage: (Int, Int) -> Unit = { _, _ -> }): OcrResult {
        val ocr = device?.takeIf { language in it.languages } ?: return OcrResult.Failed(AiError.NoProviderAvailable("no on-device recognition for $language"))
        val selected = pages.take(MAX_PAGES)
        val texts = mutableListOf<String>()
        var empty = 0
        selected.forEachIndexed { i, page ->
            onPage(i + 1, selected.size)
            val t = ocr.recognize(page, language).trim()
            if (t.isEmpty()) empty++ else texts += t
        }
        return if (texts.isEmpty()) OcrResult.NoText else OcrResult.Text(texts.joinToString("\n\n"), selected.size, empty)
    }

    @OptIn(ExperimentalEncodingApi::class)
    suspend fun recognize(pages: List<OcrPage>, language: LanguageCode, onPage: (Int, Int) -> Unit = { _, _ -> }): OcrResult {
        val selected = pages.take(MAX_PAGES)
        if (selected.any { it.bytes.size > MAX_IMAGE_BYTES }) return OcrResult.TooLarge(MAX_IMAGE_BYTES)
        val name = Languages.of(language).englishName
        val texts = mutableListOf<String>()
        var empty = 0
        selected.forEachIndexed { i, page ->
            onPage(i + 1, selected.size)
            val request = ChatRequest(
                system = SYSTEM,
                messages = listOf(ChatMessage(ChatRole.USER, "Transcribe the text on this page. It is probably in $name.",
                    images = listOf(ImagePart(page.mimeType, Base64.encode(page.bytes))))),
                dataCategories = setOf(DataCategory.IMPORTED_MATERIAL),
                maxOutputTokens = 2_000, temperature = 0.0, timeoutMillis = 90_000,
            )
            // Called only after the learner confirmed sending this file; that confirmation is the per-item consent.
            when (val outcome = providers.execute(AiCapability.VISION, request, perItemCloudConsent = true)) {
                is AiOutcome.Success -> {
                    val t = outcome.response.text.trim()
                    if (t.isEmpty() || t == NO_TEXT) empty++ else texts += t
                }
                is AiOutcome.Failure -> return OcrResult.Failed(outcome.error)
            }
        }
        return if (texts.isEmpty()) OcrResult.NoText else OcrResult.Text(texts.joinToString("\n\n"), selected.size, empty)
    }

    companion object {
        const val MAX_PAGES = 20
        /** Below the smallest per-image limit of the supported vision APIs. */
        const val MAX_IMAGE_BYTES = 4_500_000
        const val NO_TEXT = "NO_TEXT"
        val SYSTEM = "You transcribe text from images for a language learner. Output only the text that is visible, " +
            "in natural reading order (right to left for Arabic), with a blank line between paragraphs. " +
            "Do not translate, correct, summarise or add anything. If there is no readable text, output exactly $NO_TEXT."
    }
}
