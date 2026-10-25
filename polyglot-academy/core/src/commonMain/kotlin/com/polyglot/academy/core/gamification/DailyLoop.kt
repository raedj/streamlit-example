package com.polyglot.academy.core.gamification

import com.polyglot.academy.core.model.ConversationTurn
import com.polyglot.academy.core.model.ExerciseAttempt
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.SessionKind
import com.polyglot.academy.core.model.Speaker
import com.polyglot.academy.core.model.StudySession
import com.polyglot.academy.core.util.toLocalDate
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.serialization.Serializable

/** What the learner did on one day, counted from stored events. */
data class DayActivity(
    val date: LocalDate,
    val correctAnswers: Int = 0,
    val wrongAnswers: Int = 0,
    val reviews: Int = 0,
    val lessonsCompleted: Int = 0,
    val learnerTurns: Int = 0,
    val spokenTurns: Int = 0,
    val storiesRead: Int = 0,
    val pronunciationChecks: Int = 0,
    val otherItems: Int = 0,
    val activeMinutes: Int = 0,
) {
    val active: Boolean get() = activeMinutes > 0 || correctAnswers + wrongAnswers + reviews + learnerTurns + storiesRead + pronunciationChecks + otherItems > 0

    /** Base XP: effort counts (a wrong answer still earns a little), correct work counts more. */
    val baseXp: Int get() = correctAnswers * XpRules.CORRECT + wrongAnswers * XpRules.WRONG + reviews * XpRules.REVIEW +
        lessonsCompleted * XpRules.LESSON_BONUS + learnerTurns * XpRules.TURN + spokenTurns * XpRules.SPOKEN_BONUS +
        storiesRead * XpRules.STORY + pronunciationChecks * XpRules.PRONUNCIATION + otherItems * XpRules.OTHER_ITEM
}

object XpRules {
    const val CORRECT = 10
    const val WRONG = 3
    const val REVIEW = 2
    const val LESSON_BONUS = 20
    const val TURN = 5
    const val SPOKEN_BONUS = 2
    const val STORY = 15
    const val PRONUNCIATION = 4
    const val OTHER_ITEM = 3
    const val QUEST_BONUS = 15
    val goalChoices = listOf(20, 50, 100, 150, 250)

    /** Groups stored events into days for one language. */
    fun days(attempts: List<ExerciseAttempt>, sessions: List<StudySession>, learnerTurns: List<ConversationTurn>, zone: TimeZone): Map<LocalDate, DayActivity> {
        val out = mutableMapOf<LocalDate, DayActivity>()
        fun at(d: LocalDate, f: (DayActivity) -> DayActivity) { out[d] = f(out[d] ?: DayActivity(d)) }
        attempts.forEach { a -> at(a.answeredAtMillis.toLocalDate(zone)) { if (a.correct) it.copy(correctAnswers = it.correctAnswers + 1) else it.copy(wrongAnswers = it.wrongAnswers + 1) } }
        learnerTurns.filter { it.speaker == Speaker.LEARNER }.forEach { t ->
            at(t.createdAtMillis.toLocalDate(zone)) { it.copy(learnerTurns = it.learnerTurns + 1, spokenTurns = it.spokenTurns + if (t.wasSpoken) 1 else 0) }
        }
        sessions.forEach { s ->
            at(s.startedAtMillis.toLocalDate(zone)) { d ->
                val m = d.copy(activeMinutes = d.activeMinutes + s.activeSeconds / 60)
                when (s.kind) {
                    SessionKind.REVIEW -> m.copy(reviews = m.reviews + s.itemsCompleted)
                    SessionKind.LESSON -> if (s.completed) m.copy(lessonsCompleted = m.lessonsCompleted + 1) else m
                    SessionKind.READING -> if (s.completed) m.copy(storiesRead = m.storiesRead + 1) else m
                    SessionKind.PRONUNCIATION -> m.copy(pronunciationChecks = m.pronunciationChecks + s.itemsCompleted)
                    // Conversation turns and lesson answers are counted from their own records.
                    SessionKind.CONVERSATION, SessionKind.ASSESSMENT, SessionKind.RELAXATION -> m
                    else -> m.copy(otherItems = m.otherItems + s.itemsCompleted)
                }
            }
        }
        return out
    }
}

enum class QuestKind { EARN_XP, COMPLETE_LESSON, REVIEW_WORDS, CORRECT_ANSWERS, CONVERSATION_TURNS, SPEAK_ALOUD, READ_STORY, PRONUNCIATION }

data class Quest(val kind: QuestKind, val target: Int, val progress: Int) {
    val done: Boolean get() = progress >= target
    val fraction: Float get() = (progress.toFloat() / target).coerceIn(0f, 1f)
}

object Quests {
    /** Three quests a day, the same all day, varying from day to day and between languages. */
    fun forDay(date: LocalDate, language: LanguageCode, day: DayActivity, goalXp: Int): List<Quest> {
        val pool = listOf(QuestKind.COMPLETE_LESSON, QuestKind.REVIEW_WORDS, QuestKind.CORRECT_ANSWERS, QuestKind.CONVERSATION_TURNS,
            QuestKind.SPEAK_ALOUD, QuestKind.READ_STORY, QuestKind.PRONUNCIATION)
        val seed = (date.toEpochDays() * 31 + language.ordinal * 7).toLong()
        val picked = pool.sortedBy { k -> mix(seed + k.ordinal) }.take(2)
        return (listOf(QuestKind.EARN_XP) + picked).map { k -> quest(k, day, goalXp) }
    }

    fun quest(kind: QuestKind, d: DayActivity, goalXp: Int): Quest = when (kind) {
        QuestKind.EARN_XP -> Quest(kind, goalXp, d.baseXp)
        QuestKind.COMPLETE_LESSON -> Quest(kind, 1, d.lessonsCompleted)
        QuestKind.REVIEW_WORDS -> Quest(kind, 15, d.reviews)
        QuestKind.CORRECT_ANSWERS -> Quest(kind, 10, d.correctAnswers)
        QuestKind.CONVERSATION_TURNS -> Quest(kind, 6, d.learnerTurns)
        QuestKind.SPEAK_ALOUD -> Quest(kind, 4, d.spokenTurns)
        QuestKind.READ_STORY -> Quest(kind, 1, d.storiesRead)
        QuestKind.PRONUNCIATION -> Quest(kind, 5, d.pronunciationChecks)
    }

    private fun mix(x: Long): Long { var z = x * -0x61c8864680b583ebL; z = (z xor (z ushr 31)) * 0x5851f42d4c957f2dL; return z xor (z ushr 29) }
}

/** Stored per language: streak freezes and the days they covered. */
@Serializable
data class StreakState(
    val freezes: Int = 0,
    val frozenDays: Set<LocalDate> = emptySet(),
    /** Streak length at which the last freeze was awarded. */
    val lastAwardAt: Int = 0,
)

object Streaks {
    const val MAX_FREEZES = 2
    const val DAYS_PER_FREEZE = 7

    fun current(days: Set<LocalDate>, today: LocalDate): Int {
        var n = 0
        var d = if (today in days) today else today.minus(DatePeriod(days = 1))
        while (d in days) { n++; d = d.minus(DatePeriod(days = 1)) }
        return n
    }

    /**
     * Covers missed days since the last active day with freezes, if there are enough of them
     * (a partial cover would not save the streak, so no freeze is used then). Then awards one
     * freeze for every [DAYS_PER_FREEZE] days of streak, up to [MAX_FREEZES].
     */
    fun update(active: Set<LocalDate>, state: StreakState, today: LocalDate): StreakState {
        var st = state
        val covered = active + st.frozenDays
        val last = covered.filter { it < today }.maxOrNull()
        if (last != null) {
            val missed = generateSequence(last.plus(DatePeriod(days = 1))) { it.plus(DatePeriod(days = 1)) }.takeWhile { it < today }.toList()
            if (missed.isNotEmpty() && missed.size <= st.freezes) st = st.copy(freezes = st.freezes - missed.size, frozenDays = st.frozenDays + missed)
        }
        val streak = current(active + st.frozenDays, today)
        st = when {
            streak < st.lastAwardAt -> st.copy(lastAwardAt = 0)
            else -> st
        }
        if (streak >= st.lastAwardAt + DAYS_PER_FREEZE) {
            st = st.copy(freezes = minOf(MAX_FREEZES, st.freezes + 1), lastAwardAt = streak - streak % DAYS_PER_FREEZE)
        }
        // Keep only recent frozen days.
        return st.copy(frozenDays = st.frozenDays.filter { it >= today.minus(DatePeriod(days = 400)) }.toSet())
    }
}

/** Learner levels from total XP: each level needs a little more than the previous one. */
object XpLevels {
    fun levelFor(totalXp: Int): Int { var lvl = 1; while (totalXp >= threshold(lvl + 1)) lvl++; return lvl }
    /** Total XP needed to reach [level] (level 1 = 0). */
    fun threshold(level: Int): Int = if (level <= 1) 0 else (level - 1) * 100 + (level - 1) * (level - 2) * 25
}

data class DailyLoop(
    val today: DayActivity,
    val xpToday: Int,
    val goalXp: Int,
    val quests: List<Quest>,
    val totalXp: Int,
    val level: Int,
    val xpIntoLevel: Int,
    val xpForLevel: Int,
    val streakDays: Int,
    val freezes: Int,
    /** True when a freeze kept the streak alive since the learner's last visit. */
    val freezeUsedRecently: Boolean,
    val weekXp: List<Pair<LocalDate, Int>>,
    val goalMetDaysThisWeek: Int,
)

object DailyLoopCalculator {
    fun compute(language: LanguageCode, days: Map<LocalDate, DayActivity>, state: StreakState, goalXp: Int, today: LocalDate): DailyLoop {
        fun xp(d: DayActivity) = d.baseXp + Quests.forDay(d.date, language, d, goalXp).count { it.done } * XpRules.QUEST_BONUS
        val todayAct = days[today] ?: DayActivity(today)
        val quests = Quests.forDay(today, language, todayAct, goalXp)
        val total = days.values.sumOf { xp(it) }
        val level = XpLevels.levelFor(total)
        val active = days.values.filter { it.active }.map { it.date }.toSet()
        val week = (6 downTo 0).map { back -> today.minus(DatePeriod(days = back)).let { d -> d to (days[d]?.let(::xp) ?: 0) } }
        return DailyLoop(
            today = todayAct, xpToday = xp(todayAct), goalXp = goalXp, quests = quests, totalXp = total, level = level,
            xpIntoLevel = total - XpLevels.threshold(level), xpForLevel = XpLevels.threshold(level + 1) - XpLevels.threshold(level),
            streakDays = Streaks.current(active + state.frozenDays, today), freezes = state.freezes,
            freezeUsedRecently = state.frozenDays.any { it >= today.minus(DatePeriod(days = 2)) },
            weekXp = week, goalMetDaysThisWeek = week.count { it.second >= goalXp },
        )
    }
}
