package com.polyglot.academy.core.service

import com.polyglot.academy.core.content.Curriculum
import com.polyglot.academy.core.learning.AnswerCheck
import com.polyglot.academy.core.model.Achievement
import com.polyglot.academy.core.model.ErrorSeverity
import com.polyglot.academy.core.model.Exercise
import com.polyglot.academy.core.model.ExerciseAttempt
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.Lesson
import com.polyglot.academy.core.model.LessonProgress
import com.polyglot.academy.core.model.Mistake
import com.polyglot.academy.core.model.SessionKind
import com.polyglot.academy.core.model.StudySession
import com.polyglot.academy.core.repo.ProgressRepository
import com.polyglot.academy.core.repo.ReviewRepository
import com.polyglot.academy.core.srs.SpacedRepetitionScheduler
import com.polyglot.academy.core.util.IdGenerator
import com.polyglot.academy.core.util.TimeSource
import kotlinx.coroutines.flow.first

/** Result of answering one exercise, as shown in the feedback panel. */
data class ExerciseOutcome(val correct: Boolean, val score: Double, val expected: String, val note: AnswerCheck? = null)

/** Records learning events and keeps progress, mistakes, reviews and achievements consistent. */
class StudyService(
    private val progress: ProgressRepository,
    private val reviews: ReviewRepository,
    private val achievements: AchievementService,
    private val scheduler: SpacedRepetitionScheduler,
    private val clock: TimeSource,
    private val ids: IdGenerator,
) {
    suspend fun startSession(language: LanguageCode, kind: SessionKind, referenceId: String?): StudySession {
        val s = StudySession(ids.newId("session"), language, kind, referenceId, clock.nowMillis(), null, 0, 0, 0, false)
        progress.upsertSession(s)
        return s
    }

    /** Updates a running session; active time is capped per item so an idle screen does not inflate study time. */
    suspend fun updateSession(session: StudySession, itemsCompleted: Int, itemsCorrect: Int, completed: Boolean, activeSeconds: Int): StudySession {
        val updated = session.copy(
            itemsCompleted = itemsCompleted, itemsCorrect = itemsCorrect, completed = completed,
            activeSeconds = activeSeconds.coerceAtMost(maxOf(60, itemsCompleted * MAX_SECONDS_PER_ITEM)),
            endedAtMillis = if (completed) clock.nowMillis() else session.endedAtMillis,
        )
        progress.upsertSession(updated)
        return updated
    }

    suspend fun recordExercise(
        language: LanguageCode,
        lessonId: String?,
        exercise: Exercise,
        outcome: ExerciseOutcome,
        userAnswer: String,
        responseMillis: Long,
        hintsUsed: Int,
        promptText: String,
    ) {
        val now = clock.nowMillis()
        progress.recordAttempt(
            ExerciseAttempt(ids.newId("att"), lessonId, exercise.id, language, exercise.skill, exercise.difficulty, outcome.correct, outcome.score,
                responseMillis, hintsUsed, now, userAnswer.take(500)),
        )
        if (!outcome.correct && exercise !is Exercise.Flashcard && exercise !is Exercise.FreeWriting && exercise !is Exercise.SpeakRepeat) {
            progress.recordMistake(
                Mistake(ids.newId("mis"), language, exercise.skill, exercise.id, promptText.take(300), userAnswer.take(300), outcome.expected.take(300),
                    exercise.explanation?.en, outcome.note?.severity ?: ErrorSeverity.MODERATE, exercise::class.simpleName ?: "exercise", now),
            )
        }
    }

    /** New vocabulary from a lesson enters the review queue immediately. Existing states are untouched. */
    suspend fun introduceVocabulary(language: LanguageCode, itemIds: List<String>) {
        val now = clock.nowMillis()
        for (id in itemIds) if (reviews.state(id) == null) reviews.save(scheduler.newState(id, language, now), null)
    }

    suspend fun saveResumePoint(lesson: Lesson, index: Int) {
        val existing = progress.lessonProgress(lesson.language).first()[lesson.id]
        progress.saveLessonProgress(
            (existing ?: LessonProgress(lesson.id, lesson.language, 0.0, 0, null)).copy(resumeIndex = index),
        )
    }

    /** Returns achievements newly unlocked by this completion. */
    suspend fun completeLesson(lesson: Lesson, score: Double): List<String> {
        val now = clock.nowMillis()
        val existing = progress.lessonProgress(lesson.language).first()[lesson.id]
        val updated = LessonProgress(lesson.id, lesson.language, maxOf(score, existing?.bestScore ?: 0.0), (existing?.completions ?: 0) + 1, now, resumeIndex = 0)
        progress.saveLessonProgress(updated)
        introduceVocabulary(lesson.language, lesson.vocabularyIds)
        return achievements.evaluateAfterLesson(lesson.language, score)
    }

    suspend fun interruptedLessonId(language: LanguageCode): String? =
        progress.lessonProgress(language).first().values.filter { it.resumeIndex > 0 }.maxByOrNull { it.lastCompletedAtMillis ?: 0 }?.lessonId
            ?.takeIf { Curriculum.of(language).lessonsById.containsKey(it) }

    companion object { const val MAX_SECONDS_PER_ITEM = 120 }
}

/** Non-coercive achievements: celebrate real milestones; there are no penalties or guilt messages. */
class AchievementService(private val progress: ProgressRepository, private val reviews: ReviewRepository, private val clock: TimeSource) {
    data class Definition(val id: String, val icon: String)

    suspend fun evaluateAfterLesson(language: LanguageCode, score: Double): List<String> {
        val unlocked = mutableListOf<String>()
        val completed = progress.lessonProgress(language).first().values.count { it.completions > 0 }
        if (completed >= 1 && progress.unlock(Achievement(FIRST_LESSON, clock.nowMillis()))) unlocked += FIRST_LESSON
        if (completed >= 5 && progress.unlock(Achievement(FIVE_LESSONS, clock.nowMillis()))) unlocked += FIVE_LESSONS
        if (score >= 1.0 && progress.unlock(Achievement(PERFECT_LESSON, clock.nowMillis()))) unlocked += PERFECT_LESSON
        return unlocked
    }

    suspend fun evaluateAfterReview(language: LanguageCode): List<String> {
        val unlocked = mutableListOf<String>()
        if (reviews.reviewCount(language) >= 50 && progress.unlock(Achievement(FIFTY_REVIEWS, clock.nowMillis()))) unlocked += FIFTY_REVIEWS
        return unlocked
    }

    suspend fun evaluateConversation(turns: Int): List<String> =
        if (turns >= 5 && progress.unlock(Achievement(FIRST_CONVERSATION, clock.nowMillis()))) listOf(FIRST_CONVERSATION) else emptyList()

    suspend fun evaluateStreak(days: Int): List<String> = buildList {
        if (days >= 3 && progress.unlock(Achievement(STREAK_3, clock.nowMillis()))) add(STREAK_3)
        if (days >= 7 && progress.unlock(Achievement(STREAK_7, clock.nowMillis()))) add(STREAK_7)
    }

    suspend fun evaluateAssessment(): List<String> =
        if (progress.unlock(Achievement(PLACEMENT_DONE, clock.nowMillis()))) listOf(PLACEMENT_DONE) else emptyList()

    companion object {
        const val FIRST_LESSON = "first_lesson"
        const val FIVE_LESSONS = "five_lessons"
        const val PERFECT_LESSON = "perfect_lesson"
        const val FIFTY_REVIEWS = "fifty_reviews"
        const val FIRST_CONVERSATION = "first_conversation"
        const val STREAK_3 = "streak_3"
        const val STREAK_7 = "streak_7"
        const val PLACEMENT_DONE = "placement_done"
        val all = listOf(FIRST_LESSON, FIVE_LESSONS, PERFECT_LESSON, FIFTY_REVIEWS, FIRST_CONVERSATION, STREAK_3, STREAK_7, PLACEMENT_DONE)
    }
}
