package com.polyglot.academy.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class Speaker { LEARNER, TUTOR, SYSTEM }

/** A correction attached to a learner turn. */
@Serializable
data class Correction(
    val original: String,
    val corrected: String,
    val explanation: String,
    val severity: ErrorSeverity,
    /** True for optional stylistic alternatives that are not errors. */
    val isStylistic: Boolean = false,
)

@Serializable
data class ConversationTurn(
    val id: String,
    val conversationId: String,
    val speaker: Speaker,
    val text: String,
    val translation: String? = null,
    val corrections: List<Correction> = emptyList(),
    val alternatives: List<String> = emptyList(),
    val vocabularyNotes: List<String> = emptyList(),
    val createdAtMillis: Long,
    /** Which engine produced a tutor turn: provider id or "offline". */
    val producedBy: String? = null,
    val wasSpoken: Boolean = false,
)

@Serializable
data class Conversation(
    val id: String,
    val language: LanguageCode,
    val tutorId: String,
    val scenarioId: String?,
    val startedAtMillis: Long,
    val endedAtMillis: Long? = null,
    val summary: String? = null,
)

@Serializable
data class SavedPhrase(
    val id: String,
    val language: LanguageCode,
    val text: String,
    val meaning: String,
    val note: String?,
    val sourceConversationId: String?,
    val createdAtMillis: Long,
)

@Serializable
data class Note(
    val id: String,
    val language: LanguageCode?,
    val title: String,
    val body: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
)

@Serializable
data class Bookmark(
    val id: String,
    val kind: BookmarkKind,
    val referenceId: String,
    val createdAtMillis: Long,
)

@Serializable
enum class BookmarkKind { LESSON, VOCABULARY, GRAMMAR, READING, SCENARIO, MATERIAL }
