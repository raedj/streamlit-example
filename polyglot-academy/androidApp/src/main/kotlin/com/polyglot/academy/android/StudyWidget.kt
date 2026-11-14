package com.polyglot.academy.android

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.polyglot.academy.ui.app.WidgetLines
import com.polyglot.academy.ui.app.WidgetText
import com.polyglot.academy.ui.i18n.Localizer
import kotlinx.coroutines.launch

/**
 * Home-screen widget (never offered for the lock screen): today's progress, the next step and,
 * unless turned off in privacy settings, one word to review. Tapping it opens the app. It is
 * refreshed by the system about every 2 hours and whenever the app goes to the background.
 */
class StudyWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        val pending = goAsync()
        refresh(context) { pending.finish() }
    }

    companion object {
        fun refresh(context: Context, done: () -> Unit = {}) {
            val app = context.applicationContext as PolyglotApplication
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, StudyWidget::class.java))
            if (ids.isEmpty()) { done(); return }
            app.appScope.launch {
                try {
                    val summary = runCatching { app.services.widget.summary() }.getOrNull()
                    val language = runCatching { app.services.store.currentProfile().interfaceLanguage }.getOrNull()
                    val lines = WidgetText.lines(summary, Localizer(language ?: com.polyglot.academy.core.model.LanguageCode.ARABIC))
                    manager.updateAppWidget(ids, views(context, lines))
                } finally {
                    done()
                }
            }
        }

        private fun views(context: Context, lines: WidgetLines): RemoteViews = RemoteViews(context.packageName, R.layout.widget_study).apply {
            setTextViewText(R.id.widget_title, lines.title)
            setTextViewText(R.id.widget_progress, lines.progress)
            text(R.id.widget_next, lines.next)
            text(R.id.widget_word_label, lines.wordLabel)
            text(R.id.widget_word, lines.word)
            text(R.id.widget_meaning, lines.meaning)
            val open = PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
            setOnClickPendingIntent(R.id.widget_root, open)
        }

        private fun RemoteViews.text(id: Int, value: String?) {
            setTextViewText(id, value.orEmpty())
            setViewVisibility(id, if (value == null) View.GONE else View.VISIBLE)
        }
    }
}
