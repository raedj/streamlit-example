package com.polyglot.academy.ui.app

import com.polyglot.academy.core.service.WidgetSummary
import com.polyglot.academy.ui.i18n.K
import com.polyglot.academy.ui.i18n.Labels
import com.polyglot.academy.ui.i18n.Localizer

/** The home-screen widget's lines in the interface language; null lines are hidden. */
data class WidgetLines(
    val title: String,
    val progress: String,
    val next: String?,
    val wordLabel: String?,
    val word: String?,
    val meaning: String?,
)

object WidgetText {
    /** [summary] is null when the app has no learner yet (or the data could not be read). */
    fun lines(summary: WidgetSummary?, s: Localizer): WidgetLines {
        if (summary == null || summary.isNewLearner && summary.nextLessonTitle == null) {
            return WidgetLines(s[K.APP_NAME], s[K.WIDGET_START], null, null, null, null)
        }
        val progress = if (summary.minutesToday >= summary.goalMinutes) s[K.WIDGET_GOAL_DONE]
            else s.plural(K.WIDGET_TODAY, summary.goalMinutes, summary.minutesToday)
        val lesson = summary.nextLessonTitle
        val next = when {
            summary.dueReviews > 0 -> s.plural(K.WIDGET_DUE, summary.dueReviews)
            lesson != null -> s.f(K.WIDGET_NEXT, s.gloss(lesson))
            else -> null
        }
        val w = summary.word
        return WidgetLines(
            Labels.language(s, summary.language), progress, next,
            w?.let { s[K.WIDGET_WORD] },
            w?.let { listOfNotNull(it.term, it.reading).joinToString("  ·  ") },
            w?.let { s.gloss(it.meaning) },
        )
    }
}
