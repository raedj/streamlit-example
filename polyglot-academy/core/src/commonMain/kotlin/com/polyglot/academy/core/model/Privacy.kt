package com.polyglot.academy.core.model

import kotlinx.serialization.Serializable

/** Classes of user content that may be sent to an AI provider. */
@Serializable
enum class DataCategory(val sensitive: Boolean) {
    /** Exercise prompts and generic learning text authored by the app. */
    LESSON_CONTENT(false),
    /** What the learner types or says in a practice conversation. */
    CONVERSATION_TEXT(false),
    /** Raw microphone audio. */
    VOICE_AUDIO(true),
    /** Free-writing submissions. */
    WRITING_SUBMISSION(false),
    /** The learner's personal notes. */
    PERSONAL_NOTES(true),
    /** Imported documents, images, audio or video and text extracted from them. */
    IMPORTED_MATERIAL(true),
    /** Aggregated history: mistakes, scores, progress. */
    LEARNING_HISTORY(true),
    /** Content from professional tracks the learner marks as work material. */
    PROFESSIONAL_MATERIAL(true),
}

@Serializable
enum class ProviderLocation {
    /** Runs inside the app process on this device. */
    ON_DEVICE,
    /** Runs on a server the learner controls on the local network (e.g. Ollama on a PC). */
    LOCAL_NETWORK,
    /** Third-party cloud service. */
    CLOUD,
}

/**
 * User-controlled privacy configuration. Defaults are conservative: cloud AI is off until the
 * learner explicitly enables it, and sensitive categories stay local even then unless
 * individually allowed.
 */
@Serializable
data class PrivacySettings(
    val cloudAiEnabled: Boolean = false,
    val allowLocalNetworkAi: Boolean = true,
    val cloudAllowedCategories: Set<DataCategory> = setOf(
        DataCategory.LESSON_CONTENT,
        DataCategory.CONVERSATION_TEXT,
        DataCategory.WRITING_SUBMISSION,
    ),
    /** Whether the router may fall back to another cloud provider if the preferred one fails. */
    val allowCloudFallback: Boolean = false,
    val localAnalyticsOnly: Boolean = true,
    val includeInBackup: Boolean = false,
    val consentVersionAccepted: Int = 0,
)

object PrivacyConsent {
    /** Bump when the disclosure text changes materially; learners must re-confirm. */
    const val CURRENT_VERSION = 1
}
