package com.polyglot.academy.core

import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.RecallRating
import com.polyglot.academy.core.model.ReviewPhase
import com.polyglot.academy.core.srs.SpacedRepetitionScheduler
import com.polyglot.academy.core.util.MILLIS_PER_DAY
import com.polyglot.academy.core.util.MILLIS_PER_MINUTE
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SchedulerTest {
    private val s = SpacedRepetitionScheduler()
    private val t0 = 1_700_000_000_000L

    @Test fun newItemGoesThroughLearningStepsThenGraduates() {
        var st = s.newState("w", LanguageCode.ENGLISH, t0)
        st = s.review(st, RecallRating.GOOD, t0).state
        assertEquals(ReviewPhase.LEARNING, st.phase)
        assertEquals(t0 + 10 * MILLIS_PER_MINUTE, st.dueAtMillis)
        st = s.review(st, RecallRating.GOOD, st.dueAtMillis).state
        assertEquals(ReviewPhase.REVIEW, st.phase)
        assertEquals(1.0, st.intervalDays)
    }

    @Test fun easyGraduatesImmediatelyWithFourDays() {
        val st = s.review(s.newState("w", LanguageCode.ENGLISH, t0), RecallRating.EASY, t0).state
        assertEquals(ReviewPhase.REVIEW, st.phase)
        assertEquals(4.0, st.intervalDays)
    }

    @Test fun againResetsLearningToFirstStep() {
        val st = s.review(s.newState("w", LanguageCode.ENGLISH, t0), RecallRating.AGAIN, t0).state
        assertEquals(t0 + MILLIS_PER_MINUTE, st.dueAtMillis)
        assertEquals(0, st.learningStep)
    }

    @Test fun intervalsGrowByEaseOnGood() {
        var st = s.review(s.newState("w", LanguageCode.ENGLISH, t0), RecallRating.EASY, t0).state // 4d
        val due = st.dueAtMillis
        st = s.review(st, RecallRating.GOOD, due).state
        assertEquals(10.0, st.intervalDays, 1e-9) // 4 * 2.5
        st = s.review(st, RecallRating.GOOD, st.dueAtMillis).state
        assertEquals(25.0, st.intervalDays, 1e-9)
    }

    @Test fun lapseReducesEaseHalvesIntervalAndRelearns() {
        var st = s.review(s.newState("w", LanguageCode.ENGLISH, t0), RecallRating.EASY, t0).state
        st = s.review(st, RecallRating.GOOD, st.dueAtMillis).state // 10d
        st = s.review(st, RecallRating.AGAIN, st.dueAtMillis).state
        assertEquals(ReviewPhase.RELEARNING, st.phase)
        assertEquals(1, st.lapses)
        assertEquals(2.3, st.ease, 1e-9)
        assertEquals(5.0, st.intervalDays, 1e-9)
        val back = s.review(st, RecallRating.GOOD, st.dueAtMillis).state
        assertEquals(ReviewPhase.REVIEW, back.phase)
        assertEquals(st.dueAtMillis + 5 * MILLIS_PER_DAY, back.dueAtMillis)
    }

    @Test fun easeIsClampedAndIntervalCapped() {
        var st = s.review(s.newState("w", LanguageCode.ENGLISH, t0), RecallRating.EASY, t0).state
        repeat(30) { st = s.review(st, RecallRating.AGAIN, st.dueAtMillis).state; st = s.review(st, RecallRating.GOOD, st.dueAtMillis).state }
        assertTrue(st.ease >= 1.3)
        repeat(30) { st = s.review(st, RecallRating.EASY, st.dueAtMillis).state }
        assertTrue(st.intervalDays <= 365.0)
        assertTrue(st.ease <= 3.0)
    }

    @Test fun retentionEstimateIsTargetWhenDue() {
        var st = s.review(s.newState("w", LanguageCode.ENGLISH, t0), RecallRating.EASY, t0).state
        val r = s.estimatedRetention(st, st.dueAtMillis)
        assertEquals(0.9, r, 1e-6)
        assertEquals(0.0, s.estimatedRetention(s.newState("x", LanguageCode.ENGLISH, t0), t0))
    }

    @Test fun reviewCountsTracked() {
        var st = s.newState("w", LanguageCode.ENGLISH, t0)
        st = s.review(st, RecallRating.AGAIN, t0).state
        st = s.review(st, RecallRating.GOOD, t0 + 60_000).state
        assertEquals(2, st.totalReviews)
        assertEquals(1, st.correctReviews)
    }

    @Test fun previewGivesIncreasingIntervalsForBetterRatings() {
        var st = s.review(s.newState("w", LanguageCode.ENGLISH, t0), RecallRating.EASY, t0).state
        val p = s.preview(st, st.dueAtMillis)
        assertTrue(p.getValue(RecallRating.AGAIN) < p.getValue(RecallRating.HARD))
        assertTrue(p.getValue(RecallRating.HARD) < p.getValue(RecallRating.GOOD))
        assertTrue(p.getValue(RecallRating.GOOD) < p.getValue(RecallRating.EASY))
    }
}
