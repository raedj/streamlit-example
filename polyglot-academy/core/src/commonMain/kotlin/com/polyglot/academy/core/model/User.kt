package com.polyglot.academy.core.model

import kotlinx.serialization.Serializable

/** The coaching focus the learner chose for AI conversations. */
@Serializable
enum class CoachTrack { FLUENCY, BUSINESS, TRAVEL, EXAM }

@Serializable
enum class LearningGoal { TRAVEL, WORK, STUDY, EXAM, CONVERSATION, CULTURE, FAMILY, PERSONAL_GROWTH }

@Serializable
enum class LearningStyle { VISUAL, AUDITORY, READING_WRITING, CONVERSATIONAL, MIXED }

@Serializable
enum class ThemeMode { SYSTEM, LIGHT, DARK }

@Serializable
enum class CorrectionIntensity {
    /** Correct only errors that change meaning or block understanding. */
    GENTLE,
    /** Correct meaning-changing and recurring errors; mention minor ones briefly. */
    BALANCED,
    /** Correct every error found, with explanations. */
    THOROUGH,
}

/** Everything the learner chose during onboarding or later in settings. */
@Serializable
data class LearnerProfile(
    val displayName: String = "",
    val interfaceLanguage: LanguageCode = LanguageCode.ENGLISH,
    val nativeLanguage: LanguageCode = LanguageCode.ARABIC,
    /** Preferred explanation language for grammar notes and corrections. */
    val explanationLanguage: LanguageCode = LanguageCode.ARABIC,
    val activeLearningLanguage: LanguageCode = LanguageCode.ENGLISH,
    val learningLanguages: List<LanguageEnrollment> = emptyList(),
    val goals: Set<LearningGoal> = emptySet(),
    val dailyMinutes: Int = 15,
    val learningStyle: LearningStyle = LearningStyle.MIXED,
    val correctionIntensity: CorrectionIntensity = CorrectionIntensity.BALANCED,
    val preferredTutorId: String? = null,
    val includeRelaxation: Boolean = false,
    val onboardingCompleted: Boolean = false,
    val skippedOnboardingSteps: Set<String> = emptySet(),
    /** Null until the learner picks a coaching track (on screen or in the first coaching session). */
    val coachTrack: CoachTrack? = null,
    /** Daily XP goal (see [com.polyglot.academy.core.gamification.XpRules.goalChoices]). */
    val dailyXpGoal: Int = 50,
    /** Streak freezes per learning language. */
    val streaks: Map<LanguageCode, com.polyglot.academy.core.gamification.StreakState> = emptyMap(),
    /** Interests used for AI-generated stories and conversation topics. */
    val interests: List<String> = emptyList(),
) {
    fun enrollment(code: LanguageCode): LanguageEnrollment? = learningLanguages.firstOrNull { it.language == code }
    val activeEnrollment: LanguageEnrollment? get() = enrollment(activeLearningLanguage)
}

@Serializable
data class LanguageEnrollment(
    val language: LanguageCode,
    val variantId: String,
    /** Level the learner starts at (estimated or self-selected). */
    val startingLevel: CefrLevel,
    /** True when the level came from a diagnostic, false when self-selected. */
    val levelFromAssessment: Boolean,
    val targetLevel: CefrLevel = startingLevel.next(),
    val showTransliteration: Boolean = true,
    /** Mandarin: show traditional characters instead of simplified. */
    val useTraditionalCharacters: Boolean = false,
)

@Serializable
data class AccessibilitySettings(
    val reducedMotion: Boolean = false,
    val largerText: Boolean = false,
    val highContrast: Boolean = false,
    val hapticFeedback: Boolean = true,
)

@Serializable
data class SpeechSettings(
    val speechRate: Float = 1.0f,
    val autoPlayTutorReplies: Boolean = true,
    val pushToTalk: Boolean = true,
    val showTranscriptWhileListening: Boolean = true,
    val keepAudioRecordings: Boolean = false,
)

@Serializable
data class ReminderSettings(
    val enabled: Boolean = false,
    val hour: Int = 19,
    val minute: Int = 0,
    val days: Set<Int> = setOf(1, 2, 3, 4, 5, 6, 7),
    val quietHoursStart: Int = 22,
    val quietHoursEnd: Int = 7,
)

@Serializable
data class AppSettings(
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val accessibility: AccessibilitySettings = AccessibilitySettings(),
    val speech: SpeechSettings = SpeechSettings(),
    val reminders: ReminderSettings = ReminderSettings(),
    val privacy: PrivacySettings = PrivacySettings(),
    val showTranslationsByDefault: Boolean = true,
    val showHintsByDefault: Boolean = true,
    val voiceServices: VoiceServiceSettings = VoiceServiceSettings(),
    val sync: SyncSettings = SyncSettings(),
    /** Provider chosen for a feature (e.g. Claude for conversations, Gemini for reading photos); missing = the preferred provider. */
    val aiFeatureProviders: Map<com.polyglot.academy.core.ai.AiCapability, com.polyglot.academy.core.ai.ProviderId> = emptyMap(),
    val writingPractice: WritingPracticeState = WritingPracticeState(),
)

/** Where character writing practice was left, and the characters to practise again. */
@Serializable
data class WritingPracticeState(
    /** [MISSED] for the missed characters, 0 for the course characters, 1..10 for a word-list level. */
    val set: Int = 0,
    /** Null: follow the course setting for traditional characters. */
    val traditional: Boolean? = null,
    /** Position in each set, keyed by "set/traditional". */
    val positions: Map<String, Int> = emptyMap(),
    /** Characters whose last check was not right (shape or stroke order), most recent first. */
    val missed: List<String> = emptyList(),
) {
    fun position(set: Int, traditional: Boolean): Int = positions[key(set, traditional)] ?: 0
    fun withPosition(set: Int, traditional: Boolean, index: Int) = copy(positions = positions + (key(set, traditional) to index))

    /** Adds [c] to the missed characters when [right] is false, removes it when true. */
    fun checked(c: Char, right: Boolean): WritingPracticeState {
        val rest = missed - c.toString()
        return copy(missed = if (right) rest else (listOf(c.toString()) + rest).take(MAX_MISSED))
    }

    companion object {
        const val MISSED = -1
        const val MAX_MISSED = 30
        private fun key(set: Int, traditional: Boolean) = "$set/${if (traditional) "t" else "s"}"
    }
}

/** Encrypted sync through the learner's own WebDAV server. Password and passphrase are in the secure store. */
@Serializable
data class SyncSettings(
    val serverUrl: String = "",
    val username: String = "",
    val includeConversations: Boolean = true,
    val includeNotes: Boolean = true,
    val lastSyncAtMillis: Long? = null,
    /** Fingerprints of the local data and of the server copy at the last successful sync. */
    val lastLocalFingerprint: String? = null,
    val lastRemoteFingerprint: String? = null,
)

/** Optional cloud voice services. Keys are kept in the secure store, never here. */
@Serializable
data class VoiceServiceSettings(
    /** Azure AI Speech region for pronunciation scoring, e.g. "westeurope". Empty until set up. */
    val azureRegion: String = "",
    /** Realtime speech-to-speech model used with the OpenAI key. */
    val realtimeModel: String = "gpt-realtime",
    val realtimeVoice: String = "alloy",
)
