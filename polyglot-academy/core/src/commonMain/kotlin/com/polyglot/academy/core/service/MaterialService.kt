package com.polyglot.academy.core.service

import com.polyglot.academy.core.content.Curriculum
import com.polyglot.academy.core.media.CaptionParser
import com.polyglot.academy.core.media.ChunkedTextProcessor
import com.polyglot.academy.core.media.ExtractionSupport
import com.polyglot.academy.core.media.ImportPolicy
import com.polyglot.academy.core.media.VocabularyCandidate
import com.polyglot.academy.core.media.VocabularyExtractor
import com.polyglot.academy.core.model.CefrLevel
import com.polyglot.academy.core.model.Gloss
import com.polyglot.academy.core.model.ImportedMaterial
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.MaterialStatus
import com.polyglot.academy.core.model.MaterialType
import com.polyglot.academy.core.model.PartOfSpeech
import com.polyglot.academy.core.model.VocabularyItem
import com.polyglot.academy.core.model.VocabularySource
import com.polyglot.academy.core.repo.LibraryRepository
import com.polyglot.academy.core.repo.VocabularyRepository
import com.polyglot.academy.core.util.IdGenerator
import com.polyglot.academy.core.util.TextNormalizer
import com.polyglot.academy.core.util.TimeSource
import com.polyglot.academy.core.media.PdfTextExtractor
import com.polyglot.academy.core.media.PdfTextResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

/** Raw content handed over by the platform file picker or a paste. Binary types carry [bytes] instead of text. */
class ImportInput(val title: String, val mimeType: String, val fileName: String, val sizeBytes: Long, val text: String?, val bytes: ByteArray? = null)

sealed interface ImportProgress {
    data class Processing(val fraction: Float) : ImportProgress
    data class Done(val material: ImportedMaterial) : ImportProgress
    data class Rejected(val reason: ImportPolicy.Validation) : ImportProgress
}

/**
 * Turns learner-provided material into study content. Text and caption files are processed on
 * device in chunks; PDFs with real text are extracted on device ([PdfTextExtractor]). Scanned
 * PDFs (OCR), images and audio/video transcription are reported as unsupported rather than faked.
 */
class MaterialService(
    private val library: LibraryRepository,
    private val vocabulary: VocabularyRepository,
    private val reviewService: ReviewService,
    private val clock: TimeSource,
    private val ids: IdGenerator,
    /** Platform-provided extraction support (e.g. Android PDF text via a library). */
    private val platformSupport: Map<MaterialType, ExtractionSupport> = emptyMap(),
    private val ocr: OcrService? = null,
) {
    fun support(type: MaterialType): ExtractionSupport = when (type) {
        MaterialType.TEXT, MaterialType.CAPTIONS, MaterialType.PDF -> ExtractionSupport.SUPPORTED
        MaterialType.IMAGE -> if (ocr != null) ExtractionSupport.AI_OCR else ExtractionSupport.NOT_IMPLEMENTED
        else -> platformSupport[type] ?: ExtractionSupport.NOT_IMPLEMENTED
    }

    /** Whether a failed or unsupported material can still be read with AI text recognition. */
    fun canTryOcr(material: ImportedMaterial): Boolean = ocr != null &&
        (material.type == MaterialType.IMAGE || material.errorMessage == "pdf_no_text")

    /** Whether this material's language can be read on the device, without any AI provider. */
    fun canReadOnDevice(material: ImportedMaterial): Boolean = canReadOnDevice(material.language)
    fun canReadOnDevice(language: LanguageCode): Boolean = ocr?.canReadOnDevice(language) == true

    /**
     * Reads the text of [pages] (a photo, or rendered pages of a scanned PDF) and stores it for review:
     * on the device when [onDevice], otherwise with a vision AI provider, which must only happen after
     * the learner confirmed sending the file.
     */
    suspend fun recognizeText(material: ImportedMaterial, pages: List<OcrPage>, onDevice: Boolean = false, onPage: (Int, Int) -> Unit = { _, _ -> }): Pair<ImportedMaterial, OcrResult> {
        val service = ocr ?: return material to OcrResult.Failed(com.polyglot.academy.core.ai.AiError.NoProviderAvailable("no vision service"))
        val result = if (onDevice) service.recognizeOnDevice(pages, material.language, onPage) else service.recognize(pages, material.language, onPage)
        // Reading on the device sends nothing anywhere, so it does not count as cloud processing of this item.
        val cloud = material.cloudProcessingAllowed || !onDevice
        val updated = when (result) {
            is OcrResult.Text -> material.copy(status = MaterialStatus.READY_FOR_REVIEW, extractedText = result.text, errorMessage = null, cloudProcessingAllowed = cloud)
            OcrResult.NoText -> material.copy(status = MaterialStatus.FAILED, errorMessage = "ocr_no_text", cloudProcessingAllowed = cloud)
            is OcrResult.Failed, is OcrResult.TooLarge -> material
        }
        if (updated != material) library.upsertMaterial(updated)
        return updated to result
    }

    fun import(language: LanguageCode, input: ImportInput, cloudAllowed: Boolean): Flow<ImportProgress> = flow {
        val type = ImportPolicy.typeFor(input.mimeType, input.fileName)
        val validation = ImportPolicy.validate(type, input.sizeBytes)
        if (validation != ImportPolicy.Validation.Ok) { emit(ImportProgress.Rejected(validation)); return@flow }
        type!!
        var material = ImportedMaterial(ids.newId("mat"), language, input.title.ifBlank { input.fileName }, type, input.mimeType, input.sizeBytes,
            clock.nowMillis(), MaterialStatus.EXTRACTING, cloudProcessingAllowed = cloudAllowed)
        library.upsertMaterial(material)
        val raw = if (type == MaterialType.PDF && input.bytes != null) {
            when (val r = withContext(Dispatchers.Default) { PdfTextExtractor.extract(input.bytes) }) {
                is PdfTextResult.Text -> r.text
                else -> {
                    val reason = when (r) { PdfTextResult.Encrypted -> "pdf_encrypted"; PdfTextResult.NoText -> "pdf_no_text"; else -> "pdf_invalid" }
                    material = material.copy(status = MaterialStatus.FAILED, errorMessage = reason)
                    library.upsertMaterial(material)
                    emit(ImportProgress.Done(material)); return@flow
                }
            }
        } else input.text
        if (support(type) == ExtractionSupport.NOT_IMPLEMENTED || raw == null) {
            val reason = if (type == MaterialType.IMAGE && ocr != null) "needs_ocr" else "extraction_not_available"
            material = material.copy(status = MaterialStatus.UNSUPPORTED, errorMessage = reason)
            library.upsertMaterial(material)
            emit(ImportProgress.Done(material)); return@flow
        }
        val text = if (type == MaterialType.CAPTIONS) CaptionParser.parse(raw).joinToString("\n") { it.text } else raw
        var result: String? = null
        ChunkedTextProcessor.process(ChunkedTextProcessor.chunks(text)).collect { p ->
            emit(ImportProgress.Processing(p.fraction))
            if (p.done) result = p.partialText
        }
        material = if (result.isNullOrBlank()) material.copy(status = MaterialStatus.FAILED, errorMessage = "no_text_found")
        else material.copy(status = MaterialStatus.READY_FOR_REVIEW, extractedText = result)
        library.upsertMaterial(material)
        emit(ImportProgress.Done(material))
    }

    suspend fun updateExtractedText(material: ImportedMaterial, text: String) =
        library.upsertMaterial(material.copy(extractedText = text, status = MaterialStatus.READY_FOR_REVIEW))

    fun candidates(material: ImportedMaterial, limit: Int = 30): List<VocabularyCandidate> {
        val c = Curriculum.of(material.language)
        val dict = c.vocabulary.associate { (if (material.language == LanguageCode.MANDARIN) it.term else TextNormalizer.basic(it.term)) to it.id }
        return VocabularyExtractor(material.language, dict).candidates(material.extractedText.orEmpty(), limit)
    }

    /** Adds chosen words to the learner's deck. Known words reuse curriculum meanings; new ones keep the learner's meaning. */
    suspend fun addToDeck(material: ImportedMaterial, chosen: List<Pair<VocabularyCandidate, String>>): List<String> {
        val c = Curriculum.of(material.language)
        val added = mutableListOf<String>()
        for ((cand, meaning) in chosen) {
            val id = cand.knownId ?: run {
                val item = VocabularyItem(
                    id = ids.newId("uv"), language = material.language, term = cand.term, meaning = Gloss(meaning, meaning),
                    partOfSpeech = PartOfSpeech.PHRASE, level = CefrLevel.A2, topic = "imported", example = cand.exampleSentence,
                    source = VocabularySource.IMPORTED_MATERIAL, tags = setOf("material:${material.id}"),
                )
                vocabulary.addVocabulary(item)
                item.id
            }
            if (cand.knownId != null) require(c.vocabularyById.containsKey(id))
            reviewService.addToReview(material.language, id)
            added += id
        }
        val updated = material.copy(status = MaterialStatus.PROCESSED, generatedVocabularyIds = (material.generatedVocabularyIds + added).distinct())
        library.upsertMaterial(updated)
        return added
    }

    /** Deletes the material and the vocabulary that was created only from it. */
    suspend fun delete(material: ImportedMaterial) {
        material.generatedVocabularyIds.filter { it.startsWith("uv_") }.forEach { vocabulary.removeVocabulary(it) }
        library.deleteMaterial(material.id)
    }
}
