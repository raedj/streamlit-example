package com.polyglot.academy.core.service

import com.polyglot.academy.core.gamification.DailyLoop
import com.polyglot.academy.core.gamification.DailyLoopCalculator
import com.polyglot.academy.core.gamification.Streaks
import com.polyglot.academy.core.gamification.StreakState
import com.polyglot.academy.core.gamification.XpRules
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.repo.ConversationRepository
import com.polyglot.academy.core.repo.ProfileRepository
import com.polyglot.academy.core.repo.ProgressRepository
import com.polyglot.academy.core.util.TimeSource
import com.polyglot.academy.core.util.toLocalDate
import kotlinx.coroutines.flow.first
import kotlinx.datetime.TimeZone

/** XP, daily goal, quests and streak freezes, all derived from stored activity. */
class GamificationService(
    private val profiles: ProfileRepository,
    private val progress: ProgressRepository,
    private val conversations: ConversationRepository,
    private val clock: TimeSource,
    private val zone: TimeZone = TimeZone.currentSystemDefault(),
) {
    /** Computes today's loop and applies or awards streak freezes (the only stored part). */
    suspend fun today(language: LanguageCode): DailyLoop {
        val today = clock.nowMillis().toLocalDate(zone)
        val days = XpRules.days(progress.attempts(language).first(), progress.sessions(language).first(), conversations.learnerTurns(language), zone)
        val profile = profiles.currentProfile()
        val old = profile.streaks[language] ?: StreakState()
        val updated = Streaks.update(days.values.filter { it.active }.map { it.date }.toSet(), old, today)
        if (updated != old) profiles.updateProfile { it.copy(streaks = it.streaks + (language to updated)) }
        return DailyLoopCalculator.compute(language, days, updated, profile.dailyXpGoal, today)
    }
}
