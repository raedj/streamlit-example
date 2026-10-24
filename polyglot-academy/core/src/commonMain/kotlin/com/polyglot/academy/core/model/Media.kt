package com.polyglot.academy.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class MaterialType { TEXT, IMAGE, PDF, DOCUMENT, AUDIO, VIDEO, CAPTIONS }

@Serializable
enum class MaterialStatus { IMPORTED, EXTRACTING, READY_FOR_REVIEW, PROCESSED, FAILED, UNSUPPORTED }

@Serializable
data class ImportedMaterial(
    val id: String,
    val language: LanguageCode,
    val title: String,
    val type: MaterialType,
    val mimeType: String,
    val sizeBytes: Long,
    val importedAtMillis: Long,
    val status: MaterialStatus,
    val extractedText: String? = null,
    val errorMessage: String? = null,
    /** Vocabulary ids created from this material. */
    val generatedVocabularyIds: List<String> = emptyList(),
    /** Whether the learner allowed cloud processing for this specific item. */
    val cloudProcessingAllowed: Boolean = false,
)
