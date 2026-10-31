package com.polyglot.academy.core.srs

import com.polyglot.academy.core.model.RecallRating
import com.polyglot.academy.core.model.ReviewLog
import com.polyglot.academy.core.model.ReviewPhase
import com.polyglot.academy.core.model.ReviewState
import com.polyglot.academy.core.util.MILLIS_PER_DAY
import com.polyglot.academy.core.util.MILLIS_PER_MINUTE
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min

/**
 * Spaced-repetition scheduler based on the SM-2 algorithm (Wozniak, 1990) with short learning
 * steps for new and lapsed items, as popularised by Anki. Parameters are documented in
 * docs/ARCHITECTURE_DECISIONS.md (ADR-006). Intervals are deterministic (no random fuzz) so
 * results are reproducible in tests.
 */
class SpacedRepetitionScheduler(private val config: Config = Config()) {

    data class Config(
        val learningStepsMinutes: List<Long> = listOf(1, 10),
        val relearningStepsMinutes: List<Long> = listOf(10),
        val graduatingIntervalDays: Double = 1.0,
        val easyIntervalDays: Double = 4.0,
        val startingEase: Double = 2.5,
        val minEase: Double = 1.3,
        val maxEase: Double = 3.0,
        val hardMultiplier: Double = 1.2,
        val easyBonus: Double = 1.3,
        val lapseIntervalMultiplier: Double = 0.5,
        val maxIntervalDays: Double = 365.0,
        /** Retention the schedule aims for when an item becomes due. Used for estimates only. */
        val targetRetention: Double = 0.9,
    )

    data class Result(val state: ReviewState, val log: ReviewLog)

    fun newState(itemId: String, language: com.polyglot.academy.core.model.LanguageCode, nowMillis: Long) =
        ReviewState(itemId = itemId, language = language, ease = config.startingEase, dueAtMillis = nowMillis)

    fun review(state: ReviewState, rating: RecallRating, nowMillis: Long, responseMillis: Long? = null): Result {
        val correct = rating != RecallRating.AGAIN
        val next = when (state.phase) {
            ReviewPhase.NEW, ReviewPhase.LEARNING -> learning(state, rating, nowMillis, config.learningStepsMinutes)
            ReviewPhase.RELEARNING -> relearning(state, rating, nowMillis)
            ReviewPhase.REVIEW -> reviewPhase(state, rating, nowMillis)
        }.copy(
            lastReviewedAtMillis = nowMillis,
            totalReviews = state.totalReviews + 1,
            correctReviews = state.correctReviews + if (correct) 1 else 0,
        )
        val log = ReviewLog(state.itemId, nowMillis, rating, state.intervalDays, next.intervalDays, responseMillis)
        return Result(next, log)
    }

    private fun learning(s: ReviewState, rating: RecallRating, now: Long, steps: List<Long>): ReviewState = when (rating) {
        RecallRating.AGAIN -> s.copy(phase = ReviewPhase.LEARNING, learningStep = 0, dueAtMillis = now + steps[0] * MILLIS_PER_MINUTE)
        RecallRating.HARD -> {
            val cur = steps[min(s.learningStep, steps.lastIndex)]
            val nxt = steps.getOrElse(s.learningStep + 1) { cur * 2 }
            s.copy(phase = ReviewPhase.LEARNING, dueAtMillis = now + ((cur + nxt) / 2) * MILLIS_PER_MINUTE)
        }
        RecallRating.GOOD -> {
            val nextStep = s.learningStep + 1
            if (nextStep >= steps.size) graduate(s, config.graduatingIntervalDays, now)
            else s.copy(phase = ReviewPhase.LEARNING, learningStep = nextStep, dueAtMillis = now + steps[nextStep] * MILLIS_PER_MINUTE)
        }
        RecallRating.EASY -> graduate(s, config.easyIntervalDays, now)
    }

    private fun graduate(s: ReviewState, days: Double, now: Long) = s.copy(
        phase = ReviewPhase.REVIEW,
        learningStep = 0,
        intervalDays = days,
        repetitions = s.repetitions + 1,
        dueAtMillis = now + (days * MILLIS_PER_DAY).toLong(),
    )

    private fun relearning(s: ReviewState, rating: RecallRating, now: Long): ReviewState {
        val steps = config.relearningStepsMinutes
        return when (rating) {
            RecallRating.AGAIN -> s.copy(learningStep = 0, dueAtMillis = now + steps[0] * MILLIS_PER_MINUTE)
            RecallRating.HARD -> s.copy(dueAtMillis = now + steps[min(s.learningStep, steps.lastIndex)] * MILLIS_PER_MINUTE)
            RecallRating.GOOD, RecallRating.EASY -> {
                val nextStep = s.learningStep + 1
                if (rating == RecallRating.GOOD && nextStep < steps.size) {
                    s.copy(learningStep = nextStep, dueAtMillis = now + steps[nextStep] * MILLIS_PER_MINUTE)
                } else {
                    // intervalDays already holds the post-lapse interval set when the lapse happened.
                    s.copy(phase = ReviewPhase.REVIEW, learningStep = 0, dueAtMillis = now + (s.intervalDays * MILLIS_PER_DAY).toLong())
                }
            }
        }
    }

    private fun reviewPhase(s: ReviewState, rating: RecallRating, now: Long): ReviewState {
        val elapsedDays = s.lastReviewedAtMillis?.let { max(0.0, (now - it).toDouble() / MILLIS_PER_DAY) } ?: s.intervalDays
        val overdueDays = max(0.0, elapsedDays - s.intervalDays)
        return when (rating) {
            RecallRating.AGAIN -> {
                val ease = clampEase(s.ease - 0.2)
                val lapsed = max(1.0, s.intervalDays * config.lapseIntervalMultiplier)
                s.copy(
                    phase = ReviewPhase.RELEARNING, learningStep = 0, ease = ease, lapses = s.lapses + 1,
                    intervalDays = min(lapsed, config.maxIntervalDays),
                    dueAtMillis = now + config.relearningStepsMinutes[0] * MILLIS_PER_MINUTE,
                )
            }
            RecallRating.HARD -> schedule(s, max(s.intervalDays + 1, s.intervalDays * config.hardMultiplier), clampEase(s.ease - 0.15), now)
            RecallRating.GOOD -> {
                val interval = (s.intervalDays + overdueDays / 2) * s.ease
                schedule(s, max(s.intervalDays + 1, interval), s.ease, now)
            }
            RecallRating.EASY -> {
                val interval = (s.intervalDays + overdueDays) * s.ease * config.easyBonus
                schedule(s, max(s.intervalDays + 2, interval), clampEase(s.ease + 0.15), now)
            }
        }
    }

    private fun schedule(s: ReviewState, rawDays: Double, ease: Double, now: Long): ReviewState {
        val days = min(rawDays, config.maxIntervalDays)
        return s.copy(
            intervalDays = days, ease = ease, repetitions = s.repetitions + 1,
            dueAtMillis = now + (days * MILLIS_PER_DAY).toLong(),
        )
    }

    private fun clampEase(e: Double) = e.coerceIn(config.minEase, config.maxEase)

    /**
     * Estimated probability of recall now, assuming an exponential forgetting curve whose
     * stability is calibrated so recall equals [Config.targetRetention] when the item is due.
     * This is a model estimate, not a measurement.
     */
    fun estimatedRetention(s: ReviewState, nowMillis: Long): Double {
        if (s.phase == ReviewPhase.NEW || s.lastReviewedAtMillis == null) return 0.0
        val stabilityDays = max(s.intervalDays, 1.0 / 24) / -ln(config.targetRetention)
        val elapsed = max(0.0, (nowMillis - s.lastReviewedAtMillis).toDouble() / MILLIS_PER_DAY)
        return exp(-elapsed / stabilityDays)
    }

    fun isDue(s: ReviewState, nowMillis: Long) = s.dueAtMillis <= nowMillis

    /** Preview of the next interval for each rating, for showing on rating buttons. */
    fun preview(s: ReviewState, nowMillis: Long): Map<RecallRating, Long> =
        RecallRating.entries.associateWith { review(s, it, nowMillis).state.dueAtMillis - nowMillis }
}
