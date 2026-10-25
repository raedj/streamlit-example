package com.polyglot.academy.core

import com.polyglot.academy.core.gamification.DailyLoopCalculator
import com.polyglot.academy.core.gamification.DayActivity
import com.polyglot.academy.core.gamification.QuestKind
import com.polyglot.academy.core.gamification.Quests
import com.polyglot.academy.core.gamification.StreakState
import com.polyglot.academy.core.gamification.Streaks
import com.polyglot.academy.core.gamification.XpLevels
import com.polyglot.academy.core.gamification.XpRules
import com.polyglot.academy.core.model.ConversationTurn
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.SessionKind
import com.polyglot.academy.core.model.Speaker
import com.polyglot.academy.core.model.StudySession
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DailyLoopTest {
    private val today = LocalDate(2026, 10, 25)
    private fun ago(n: Int) = today.minus(DatePeriod(days = n))

    @Test fun xpIsCountedFromRealActivity() {
        val day0 = 1_000_000L // 1970-01-01 in UTC
        val turns = listOf(
            ConversationTurn("1", "c", Speaker.LEARNER, "hi", createdAtMillis = day0, wasSpoken = true),
            ConversationTurn("2", "c", Speaker.TUTOR, "hello", createdAtMillis = day0),
        )
        val sessions = listOf(
            StudySession("s1", LanguageCode.ENGLISH, SessionKind.REVIEW, null, day0, null, 120, 12, 10, true),
            StudySession("s2", LanguageCode.ENGLISH, SessionKind.LESSON, "l", day0, null, 300, 5, 4, true),
        )
        val days = XpRules.days(listOf(attempt(1, true, at = day0), attempt(2, false, at = day0)), sessions, turns, TimeZone.UTC)
        val d = days.values.single()
        assertEquals(1, d.learnerTurns); assertEquals(1, d.spokenTurns); assertEquals(12, d.reviews); assertEquals(1, d.lessonsCompleted)
        assertEquals(10 + 3 + 12 * 2 + 20 + 5 + 2, d.baseXp)
        assertEquals(7, d.activeMinutes)
        assertFalse(DayActivity(today).active)
    }

    @Test fun questsAreStableForADayAndVary() {
        val a = Quests.forDay(today, LanguageCode.SPANISH, DayActivity(today), 50)
        assertEquals(a, Quests.forDay(today, LanguageCode.SPANISH, DayActivity(today), 50))
        assertEquals(3, a.size); assertEquals(QuestKind.EARN_XP, a.first().kind)
        assertEquals(3, a.map { it.kind }.toSet().size)
        val kinds = (0 until 14).flatMap { Quests.forDay(ago(it), LanguageCode.SPANISH, DayActivity(ago(it)), 50).drop(1).map { q -> q.kind } }.toSet()
        assertTrue(kinds.size >= 4, "quests rotate: $kinds")
        val done = Quests.quest(QuestKind.REVIEW_WORDS, DayActivity(today, reviews = 20), 50)
        assertTrue(done.done); assertEquals(1f, done.fraction)
    }

    @Test fun freezesCoverMissedDaysOnlyWhenEnough() {
        val active = (2..5).map(::ago).toSet() // missed yesterday
        val used = Streaks.update(active, StreakState(freezes = 1, lastAwardAt = 0), today)
        assertEquals(0, used.freezes); assertTrue(ago(1) in used.frozenDays)
        assertEquals(5, Streaks.current(active + used.frozenDays, today))
        // Two missed days and one freeze: the streak is lost and the freeze is kept.
        val kept = Streaks.update((3..5).map(::ago).toSet(), StreakState(freezes = 1), today)
        assertEquals(1, kept.freezes); assertTrue(kept.frozenDays.isEmpty())
    }

    @Test fun freezesAreEarnedEverySevenDaysUpToTwo() {
        val week = (0..6).map(::ago).toSet()
        val one = Streaks.update(week, StreakState(), today)
        assertEquals(1, one.freezes); assertEquals(7, one.lastAwardAt)
        assertEquals(one, Streaks.update(week, one, today), "no double award on the same day")
        val month = (0..29).map(::ago).toSet()
        assertEquals(Streaks.MAX_FREEZES, Streaks.update(month, StreakState(freezes = 2, lastAwardAt = 21), today).freezes)
    }

    @Test fun levelsAndDailyLoop() {
        assertEquals(1, XpLevels.levelFor(0)); assertEquals(2, XpLevels.levelFor(100)); assertEquals(3, XpLevels.levelFor(250))
        val days = mapOf(today to DayActivity(today, correctAnswers = 6), ago(1) to DayActivity(ago(1), correctAnswers = 2))
        val loop = DailyLoopCalculator.compute(LanguageCode.ENGLISH, days, StreakState(), 50, today)
        assertTrue(loop.xpToday >= 60, "base XP plus the EARN_XP quest bonus")
        assertEquals(2, loop.streakDays)
        assertEquals(1, loop.goalMetDaysThisWeek)
        assertEquals(7, loop.weekXp.size)
    }
}
