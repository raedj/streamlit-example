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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/** Raw content handed over by the platform file picker or a paste. Binary types carry no text. */
data class ImportInput(val title: String, val mimeType: String, val fileName: String, val sizeBytes: Long, val text: String?)

sealed interface ImportProgress {
    data class Processing(val fraction: Float) : ImportProgress
    data class Done(val material: ImportedMaterial) : ImportProgress
    data class Rejected(val reason: ImportPolicy.Validation) : ImportProgress
}

/**
 * Turns learner-provided material into study content. Text and caption files are processed on
 * device in chunks. Formats that need platform extractors (PDF text, OCR, audio/video
 * transcription) are reported as such rather than faked.
 */
class MaterialService(
    private val library: LibraryRepository,
    private val vocabulary: VocabularyRepository,
    private val reviewService: ReviewService,
    private val clock: TimeSource,
    private val ids: IdGenerator,
    /** Platform-provided extraction support (e.g. Android PDF text via a library). */
    private val platformSupport: Map<MaterialType, ExtractionSupport> = emptyMap(),
) {
    fun support(type: MaterialType): ExtractionSupport = when (type) {
        MaterialType.TEXT, MaterialType.CAPTIONS -> ExtractionSupport.SUPPORTED
        else -> platformSupport[type] ?: ExtractionSupport.NOT_IMPLEMENTED
    }

    fun import(language: LanguageCode, input: ImportInput, cloudAllowed: Boolean): Flow<ImportProgress> = flow {
        val type = ImportPolicy.typeFor(input.mimeType, input.fileName)
        val validation = ImportPolicy.validate(type, input.sizeBytes)
        if (validation != ImportPolicy.Validation.Ok) { emit(ImportProgress.Rejected(validation)); return@flow }
        type!!
        var material = ImportedMaterial(ids.newId("mat"), language, input.title.ifBlank { input.fileName }, type, input.mimeType, input.sizeBytes,
            clock.nowMillis(), MaterialStatus.EXTRACTING, cloudProcessingAllowed = cloudAllowed)
        library.upsertMaterial(material)
        val raw = input.text
        if (support(type) == ExtractionSupport.NOT_IMPLEMENTED || raw == null) {
            material = material.copy(status = MaterialStatus.UNSUPPORTED, errorMessage = "extraction_not_available")
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
