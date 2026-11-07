package com.polyglot.academy.data

import com.polyglot.academy.core.content.Curriculum
import com.polyglot.academy.core.learning.RecommendationKind
import com.polyglot.academy.core.model.Exercise
import com.polyglot.academy.core.model.Gloss
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.LanguageEnrollment
import com.polyglot.academy.core.model.CefrLevel
import com.polyglot.academy.core.model.Skill
import com.polyglot.academy.core.model.needsAudio
import com.polyglot.academy.core.service.AppServices
import com.polyglot.academy.core.service.ExerciseOutcome
import com.polyglot.academy.core.service.StudyService
import com.polyglot.academy.core.util.FixedTimeSource
import com.polyglot.academy.core.util.SequentialIdGenerator
import io.ktor.client.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** K-24 (listening without a voice), K-25 (lesson minutes on the home card), K-26 (resumed lesson scoring). */
class LessonFixesTest {
    private val clock = FixedTimeSource(1_761_300_000_000L)
    private val store = SqlStore(createJvmDriver(), Dispatchers.IO, TimeZone.UTC, clock)
    private val app = AppServices(store, FakePlatform(), HttpClient(), clock, SequentialIdGenerator())
    private val lang = LanguageCode.ENGLISH
    private val lesson = Curriculum.of(lang).lessons.first { it.exercises.size >= 3 }

    private suspend fun answer(index: Int, correct: Boolean) {
        val ex = lesson.exercises[index]
        app.study.recordExercise(lang, lesson.id, ex, ExerciseOutcome(correct, if (correct) 1.0 else 0.0, "x"), "a", 1000, 0, "prompt")
        clock.current += 1000
    }

    @Test fun resumedLessonKeepsResultsFromBeforeTheInterruption() = runBlocking<Unit> {
        answer(0, correct = false)
        answer(1, correct = true)
        app.study.saveResumePoint(lesson, 2)
        val resumed = app.study.resumedFirstTries(lesson, 2)
        assertEquals(mapOf(lesson.exercises[0].id to 0.0, lesson.exercises[1].id to 1.0), resumed)
        // With the earlier mistake included the lesson is no longer perfect.
        assertTrue(resumed.values.average() < 1.0)
    }

    @Test fun attemptsFromAnEarlierCompletedRunAreIgnored() = runBlocking<Unit> {
        answer(0, correct = false)
        app.study.completeLesson(lesson, 0.5)
        clock.current += 1000
        answer(0, correct = true)
        app.study.saveResumePoint(lesson, 1)
        assertEquals(mapOf(lesson.exercises[0].id to 1.0), app.study.resumedFirstTries(lesson, 1))
        assertEquals(emptyMap(), app.study.resumedFirstTries(lesson, 0))
    }

    @Test fun firstTryScoreMatchesThePlayer() {
        assertEquals(1.0, StudyService.firstTryScore(true, 1.0))
        assertEquals(0.99, StudyService.firstTryScore(true, 0.9))
        assertEquals(0.6, StudyService.firstTryScore(false, 0.8))
        assertEquals(0.0, StudyService.firstTryScore(false, 0.0))
    }

    @Test fun homeCardShowsTheLessonsOwnMinutes() = runBlocking<Unit> {
        store.updateProfile {
            it.copy(activeLearningLanguage = lang, onboardingCompleted = true,
                learningLanguages = listOf(LanguageEnrollment(lang, "x", CefrLevel.A1, levelFromAssessment = false)))
        }
        val snap = app.dashboard.snapshot()
        assertEquals(RecommendationKind.NEXT_LESSON, snap.recommendation.kind)
        assertEquals(snap.nextLesson!!.estimatedMinutes, snap.recommendation.estimatedMinutes)
    }

    @Test fun onlyAudioOnlyExercisesNeedAVoice() {
        val g = Gloss("x", "x")
        assertTrue(Exercise.Dictation("d", Skill.LISTENING, g, "hello", listOf("hello")).needsAudio)
        assertTrue(Exercise.MultipleChoice("m", Skill.LISTENING, g, "p", listOf("a", "b"), 0, audioText = "a").needsAudio)
        assertFalse(Exercise.MultipleChoice("m2", Skill.READING, g, "p", listOf("a", "b"), 0).needsAudio)
        assertFalse(Exercise.FillInBlank("f", Skill.WRITING, g, "a ___", listOf("b")).needsAudio)
    }
}
