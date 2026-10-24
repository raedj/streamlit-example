package com.polyglot.academy.core

import com.polyglot.academy.core.content.Curriculum
import com.polyglot.academy.core.learning.DifficultyAdjuster
import com.polyglot.academy.core.learning.DifficultyReason
import com.polyglot.academy.core.learning.ExplanationDepth
import com.polyglot.academy.core.learning.MasteryTracker
import com.polyglot.academy.core.learning.NextActivityRecommender
import com.polyglot.academy.core.learning.RecommendationInput
import com.polyglot.academy.core.learning.RecommendationKind
import com.polyglot.academy.core.learning.SkillEstimate
import com.polyglot.academy.core.learning.wilsonLowerBound
import com.polyglot.academy.core.model.CefrLevel
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.LessonProgress
import com.polyglot.academy.core.model.SessionKind
import com.polyglot.academy.core.model.Skill
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LearningEngineTest {
    @Test fun masteryRisesWithCorrectAnswersAndFallsWithErrors() {
        val good = MasteryTracker.estimate(Skill.VOCABULARY, (1..10).map { attempt(it, true) })
        val bad = MasteryTracker.estimate(Skill.VOCABULARY, (1..10).map { attempt(it, false) })
        assertTrue(good.mastery > 0.8, "good=${good.mastery}")
        assertTrue(bad.mastery < 0.2, "bad=${bad.mastery}")
        assertTrue(good.hasEnoughData)
    }

    @Test fun noDataMeansNoConfidence() {
        val e = MasteryTracker.estimate(Skill.GRAMMAR, emptyList())
        assertEquals(0.0, e.confidence)
        assertTrue(!e.hasEnoughData)
    }

    @Test fun hintsReduceMasteryGain() {
        val noHints = MasteryTracker.estimate(Skill.VOCABULARY, (1..6).map { attempt(it, true) })
        val hints = MasteryTracker.estimate(Skill.VOCABULARY, (1..6).map { attempt(it, true, hints = 1) })
        assertTrue(noHints.mastery > hints.mastery)
    }

    @Test fun difficultyIncreasesAfterStrongFastPerformance() {
        val d = DifficultyAdjuster.decide(2, (1..8).map { attempt(it, true) })
        assertEquals(3, d.difficulty)
        assertEquals(DifficultyReason.STRONG_RECENT_PERFORMANCE, d.reason)
        assertEquals(ExplanationDepth.BRIEF, d.explanationDepth)
    }

    @Test fun difficultyHeldWhenCorrectButSlow() {
        val d = DifficultyAdjuster.decide(2, (1..8).map { attempt(it, true, responseMillis = 40_000) })
        assertEquals(2, d.difficulty)
    }

    @Test fun difficultyDecreasesAndHintsEnabledWhenStruggling() {
        val d = DifficultyAdjuster.decide(3, (1..8).map { attempt(it, it % 3 == 0) }, hintsPreferred = false)
        assertEquals(2, d.difficulty)
        assertTrue(d.hintsAvailable)
        assertEquals(ExplanationDepth.DETAILED, d.explanationDepth)
    }

    @Test fun difficultyUnchangedWithFewAttemptsAndBounded() {
        assertEquals(DifficultyReason.NOT_ENOUGH_DATA, DifficultyAdjuster.decide(2, listOf(attempt(1, true))).reason)
        assertEquals(5, DifficultyAdjuster.decide(5, (1..8).map { attempt(it, true) }).difficulty)
        assertEquals(1, DifficultyAdjuster.decide(1, (1..8).map { attempt(it, false) }).difficulty)
    }

    @Test fun sequencerStartsAtPlacementLevelAndRespectsPrerequisites() {
        val c = Curriculum.of(LanguageCode.ENGLISH)
        val first = c.sequencer.nextLesson(CefrLevel.A1, emptyMap())
        assertEquals("en_l_greetings", first?.id)
        val atA2 = c.sequencer.nextLesson(CefrLevel.A2, emptyMap())
        assertEquals("en_l_routine", atA2?.id)
        val progress = mapOf("en_l_greetings" to LessonProgress("en_l_greetings", LanguageCode.ENGLISH, 0.9, 1, 1L))
        assertEquals("en_l_introductions", c.sequencer.nextLesson(CefrLevel.A1, progress)?.id)
        val locked = c.lessonsById.getValue("en_l_cafe")
        assertTrue(!c.sequencer.isUnlocked(locked, CefrLevel.A1, emptyMap()))
    }

    @Test fun lowScoreDoesNotCountAsMastered() {
        val c = Curriculum.of(LanguageCode.ENGLISH)
        val progress = mapOf("en_l_greetings" to LessonProgress("en_l_greetings", LanguageCode.ENGLISH, 0.5, 1, 1L))
        assertEquals("en_l_greetings", c.sequencer.nextLesson(CefrLevel.A1, progress)?.id)
    }

    private fun input(
        due: Int = 0, mistakes: Int = 0, interrupted: String? = null, next: String? = "l1",
        estimates: List<SkillEstimate> = emptyList(), last: SessionKind? = null, minutes: Int = 0, placement: Boolean = true,
    ) = RecommendationInput(due, mistakes, interrupted, next, estimates, last, minutes, 15, placement, includeRelaxation = false)

    @Test fun recommenderPriorities() {
        assertEquals(RecommendationKind.ASSESSMENT, NextActivityRecommender.recommend(input(placement = false)).kind)
        assertEquals(RecommendationKind.CONTINUE_LESSON, NextActivityRecommender.recommend(input(interrupted = "x", due = 50)).kind)
        assertEquals(RecommendationKind.REVIEW_DUE, NextActivityRecommender.recommend(input(due = 12)).kind)
        assertEquals(RecommendationKind.MISTAKE_REVIEW, NextActivityRecommender.recommend(input(mistakes = 6)).kind)
        assertEquals(RecommendationKind.NEXT_LESSON, NextActivityRecommender.recommend(input()).kind)
        assertEquals(RecommendationKind.ALL_DONE, NextActivityRecommender.recommend(input(minutes = 20)).kind)
    }

    @Test fun recommenderInterleavesWeakestSkillAfterLesson() {
        val est = listOf(SkillEstimate(Skill.GRAMMAR, 0.4, 10, 0.6), SkillEstimate(Skill.VOCABULARY, 0.9, 10, 0.6))
        val r = NextActivityRecommender.recommend(input(estimates = est, last = SessionKind.LESSON))
        assertEquals(RecommendationKind.SKILL_PRACTICE, r.kind)
        assertEquals(Skill.GRAMMAR, r.skill)
    }

    @Test fun recommenderDoesNotRepeatReviewImmediately() {
        val r = NextActivityRecommender.recommend(input(due = 30, last = SessionKind.REVIEW))
        assertEquals(RecommendationKind.NEXT_LESSON, r.kind)
    }

    @Test fun wilsonBound() {
        assertEquals(0.0, wilsonLowerBound(0, 0))
        assertTrue(wilsonLowerBound(9, 10) < 0.9)
        assertTrue(wilsonLowerBound(90, 100) > wilsonLowerBound(9, 10))
        assertNotNull(wilsonLowerBound(1, 1))
    }
}
