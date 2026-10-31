package com.polyglot.academy.core.service

import com.polyglot.academy.core.model.Gloss
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.repo.ProfileRepository

/** A word waiting for review, as the home-screen widget shows it. */
data class WidgetWord(val term: String, val reading: String?, val meaning: Gloss)

/** What the home-screen widget shows: today's progress, the next step and, if allowed, one word to review. */
data class WidgetSummary(
    val language: LanguageCode,
    val minutesToday: Int,
    val goalMinutes: Int,
    val dueReviews: Int,
    val nextLessonTitle: Gloss?,
    val word: WidgetWord?,
    val isNewLearner: Boolean,
)

/** Builds the widget's content from the same stored activity as the home screen; nothing is invented. */
class WidgetService(
    private val dashboard: DashboardService,
    private val reviews: ReviewService,
    private val settings: ProfileRepository,
) {
    suspend fun summary(): WidgetSummary {
        val d = dashboard.snapshot()
        val word = if (settings.currentSettings().privacy.widgetShowsWord && d.dueReviews > 0) {
            reviews.dueQueue(d.language, limit = 1).firstOrNull()?.let { card ->
                val showReading = d.profile.enrollment(d.language)?.showTransliteration != false
                WidgetWord(card.front, card.pronunciation?.takeIf { showReading }, card.back)
            }
        } else null
        return WidgetSummary(d.language, d.minutesToday, d.dailyGoalMinutes, d.dueReviews, (d.interruptedLesson ?: d.nextLesson)?.title, word, d.isNewLearner)
    }
}
