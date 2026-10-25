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
)
