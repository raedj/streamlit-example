package com.polyglot.academy.core.analytics

import com.polyglot.academy.core.model.ExerciseAttempt
import com.polyglot.academy.core.model.ReviewPhase
import com.polyglot.academy.core.model.ReviewState
import com.polyglot.academy.core.model.Skill
import com.polyglot.academy.core.model.StudySession
import com.polyglot.academy.core.srs.SpacedRepetitionScheduler
import com.polyglot.academy.core.util.MILLIS_PER_DAY
import com.polyglot.academy.core.util.toLocalDate
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus

data class DailyValue(val date: LocalDate, val value: Double)

data class RetentionBuckets(val strong: Int, val fading: Int, val weak: Int, val notStarted: Int) {
    val total: Int get() = strong + fading + weak + notStarted
}

/** All derived metrics. "Observed" values are counted from events; "estimated" values come from models. */
data class LearningSummary(
    val observedMinutesByDay: List<DailyValue>,
    val observedAccuracyByWeek: List<DailyValue>,
    val observedLessonsCompleted: Int,
    val observedWordsReviewed: Int,
    val observedSpeakingTurns: Int,
    val currentStreakDays: Int,
    val longestStreakDays: Int,
    val estimatedRetention: RetentionBuckets,
    val reviewForecast: List<DailyValue>,
    val skillAccuracy: Map<Skill, Double>,
    val skillAttempts: Map<Skill, Int>,
)

class AnalyticsCalculator(
    private val scheduler: SpacedRepetitionScheduler = SpacedRepetitionScheduler(),
    private val zone: TimeZone = TimeZone.currentSystemDefault(),
) {
    fun minutesByDay(sessions: List<StudySession>, today: LocalDate, days: Int): List<DailyValue> {
        val grouped = sessions.groupBy { it.startedAtMillis.toLocalDate(zone) }
            .mapValues { (_, s) -> s.sumOf { it.activeSeconds } / 60.0 }
        return (days - 1 downTo 0).map { back ->
            val d = today.minus(DatePeriod(days = back))
            DailyValue(d, grouped[d] ?: 0.0)
        }
    }

    fun accuracyByWeek(attempts: List<ExerciseAttempt>, today: LocalDate, weeks: Int): List<DailyValue> =
        (weeks - 1 downTo 0).mapNotNull { back ->
            val end = today.minus(DatePeriod(days = back * 7))
            val start = end.minus(DatePeriod(days = 6))
            val inWeek = attempts.filter { val d = it.answeredAtMillis.toLocalDate(zone); d >= start && d <= end }
            if (inWeek.isEmpty()) null else DailyValue(end, inWeek.count { it.correct }.toDouble() / inWeek.size)
        }

    fun streaks(activeDays: Set<LocalDate>, today: LocalDate): Pair<Int, Int> {
        var current = 0
        var d = if (today in activeDays) today else today.minus(DatePeriod(days = 1))
        while (d in activeDays) { current++; d = d.minus(DatePeriod(days = 1)) }
        var longest = 0; var run = 0; var prev: LocalDate? = null
        for (day in activeDays.sorted()) {
            run = if (prev != null && prev.plus(DatePeriod(days = 1)) == day) run + 1 else 1
            longest = maxOf(longest, run); prev = day
        }
        return current to longest
    }

    fun retention(states: List<ReviewState>, nowMillis: Long): RetentionBuckets {
        var strong = 0; var fading = 0; var weak = 0; var notStarted = 0
        for (s in states) {
            if (s.phase == ReviewPhase.NEW) { notStarted++; continue }
            val r = scheduler.estimatedRetention(s, nowMillis)
            when {
                r >= 0.85 -> strong++
                r >= 0.6 -> fading++
                else -> weak++
            }
        }
        return RetentionBuckets(strong, fading, weak, notStarted)
    }

    fun reviewForecast(states: List<ReviewState>, nowMillis: Long, days: Int): List<DailyValue> {
        val today = nowMillis.toLocalDate(zone)
        return (0 until days).map { i ->
            val d = today.plus(DatePeriod(days = i))
            val count = states.count { s ->
                s.phase != ReviewPhase.NEW && (if (i == 0) s.dueAtMillis <= nowMillis + MILLIS_PER_DAY else s.dueAtMillis.toLocalDate(zone) == d) &&
                    (i == 0 || s.dueAtMillis > nowMillis)
            }
            DailyValue(d, count.toDouble())
        }
    }

    fun summarize(
        sessions: List<StudySession>,
        attempts: List<ExerciseAttempt>,
        states: List<ReviewState>,
        reviewsDone: Int,
        lessonsCompleted: Int,
        speakingTurns: Int,
        nowMillis: Long,
        days: Int = 7,
    ): LearningSummary {
        val today = nowMillis.toLocalDate(zone)
        val activeDays = sessions.filter { it.activeSeconds > 0 }.map { it.startedAtMillis.toLocalDate(zone) }.toSet()
        val (cur, longest) = streaks(activeDays, today)
        val bySkill = attempts.groupBy { it.skill }
        return LearningSummary(
            observedMinutesByDay = minutesByDay(sessions, today, days),
            observedAccuracyByWeek = accuracyByWeek(attempts, today, 6),
            observedLessonsCompleted = lessonsCompleted,
            observedWordsReviewed = reviewsDone,
            observedSpeakingTurns = speakingTurns,
            currentStreakDays = cur,
            longestStreakDays = longest,
            estimatedRetention = retention(states, nowMillis),
            reviewForecast = reviewForecast(states, nowMillis, 7),
            skillAccuracy = bySkill.mapValues { (_, a) -> a.count { it.correct }.toDouble() / a.size },
            skillAttempts = bySkill.mapValues { it.value.size },
        )
    }
}
