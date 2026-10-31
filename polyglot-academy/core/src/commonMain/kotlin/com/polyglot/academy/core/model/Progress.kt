package com.polyglot.academy.core.model

import kotlinx.serialization.Serializable

/** Learner self-rating after recalling an item, used by the spaced-repetition scheduler. */
@Serializable
enum class RecallRating { AGAIN, HARD, GOOD, EASY }

@Serializable
enum class ReviewPhase { NEW, LEARNING, REVIEW, RELEARNING }

/** Scheduling state for one reviewable item (vocabulary or saved phrase). */
@Serializable
data class ReviewState(
    val itemId: String,
    val language: LanguageCode,
    val phase: ReviewPhase = ReviewPhase.NEW,
    /** SM-2 ease factor, kept within [1.3, 3.0]. */
    val ease: Double = 2.5,
    val intervalDays: Double = 0.0,
    /** Index into learning steps while in LEARNING/RELEARNING. */
    val learningStep: Int = 0,
    val repetitions: Int = 0,
    val lapses: Int = 0,
    val lastReviewedAtMillis: Long? = null,
    val dueAtMillis: Long = 0,
    val totalReviews: Int = 0,
    val correctReviews: Int = 0,
) {
    val isLeech: Boolean get() = lapses >= 6
}

@Serializable
data class ReviewLog(
    val itemId: String,
    val reviewedAtMillis: Long,
    val rating: RecallRating,
    val previousIntervalDays: Double,
    val newIntervalDays: Double,
    val responseMillis: Long?,
)

/** One answered exercise. */
@Serializable
data class ExerciseAttempt(
    val id: String,
    val lessonId: String?,
    val exerciseId: String,
    val language: LanguageCode,
    val skill: Skill,
    val difficulty: Int,
    val correct: Boolean,
    /** 0..1, for partially-correct answers (e.g. dictation similarity). */
    val score: Double,
    val responseMillis: Long,
    val hintsUsed: Int,
    val answeredAtMillis: Long,
    val userAnswer: String,
)

@Serializable
enum class ErrorSeverity {
    /** Spelling/accent slip; meaning still clear. */
    MINOR,
    /** Grammatical error; meaning recoverable. */
    MODERATE,
    /** Changes or obscures meaning. */
    MEANING_CHANGING,
}

@Serializable
data class Mistake(
    val id: String,
    val language: LanguageCode,
    val skill: Skill,
    val sourceId: String,
    val prompt: String,
    val userAnswer: String,
    val correctAnswer: String,
    val explanation: String?,
    val severity: ErrorSeverity,
    val category: String,
    val occurredAtMillis: Long,
    val resolved: Boolean = false,
    val occurrences: Int = 1,
)

@Serializable
enum class SessionKind { LESSON, REVIEW, CONVERSATION, READING, LISTENING, WRITING, PRONUNCIATION, ASSESSMENT, RELAXATION, MEDIA, WORD_TEST }

@Serializable
data class StudySession(
    val id: String,
    val language: LanguageCode,
    val kind: SessionKind,
    val referenceId: String?,
    val startedAtMillis: Long,
    val endedAtMillis: Long?,
    val activeSeconds: Int,
    val itemsCompleted: Int,
    val itemsCorrect: Int,
    val completed: Boolean,
)

@Serializable
data class LessonProgress(
    val lessonId: String,
    val language: LanguageCode,
    val bestScore: Double,
    val completions: Int,
    val lastCompletedAtMillis: Long?,
    /** Index of the next exercise if the lesson was interrupted. */
    val resumeIndex: Int = 0,
) {
    val mastered: Boolean get() = bestScore >= MASTERY_THRESHOLD
    companion object { const val MASTERY_THRESHOLD = 0.8 }
}

@Serializable
data class AssessmentResult(
    val id: String,
    val language: LanguageCode,
    val kind: AssessmentKind,
    val takenAtMillis: Long,
    val estimatedLevel: CefrLevel,
    /** Lower and upper bounds of the estimate. */
    val levelLow: CefrLevel,
    val levelHigh: CefrLevel,
    val skillScores: Map<Skill, Double>,
    val questionsAnswered: Int,
    val correctAnswers: Int,
)

@Serializable
enum class AssessmentKind { DIAGNOSTIC, PROGRESS_CHECK }

@Serializable
data class Achievement(
    val id: String,
    val unlockedAtMillis: Long,
)
