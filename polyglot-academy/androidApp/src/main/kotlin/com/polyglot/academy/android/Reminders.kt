package com.polyglot.academy.android

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Daily reminder using an inexact repeating alarm (battery friendly, no exact-alarm permission).
 * The receiver checks the selected days and quiet hours before posting, and never nags: one
 * notification per day at most, with neutral wording.
 */
object Reminders {
    private const val CHANNEL = "study_reminders"
    private const val REQUEST = 4201
    private const val PREFS = "polyglot_reminders"

    fun schedule(context: Context, enabled: Boolean, hour: Int, minute: Int, days: Set<Int>): Boolean {
        val am = context.getSystemService(AlarmManager::class.java) ?: return false
        val pi = PendingIntent.getBroadcast(context, REQUEST, Intent(context, ReminderReceiver::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        am.cancel(pi)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean("enabled", enabled).putInt("hour", hour).putInt("minute", minute).putString("days", days.joinToString(",")).apply()
        if (!enabled) return true
        if (Build.VERSION.SDK_INT >= 33 && !NotificationManagerCompat.from(context).areNotificationsEnabled()) return false
        val first = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour); set(Calendar.MINUTE, minute); set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, first.timeInMillis, AlarmManager.INTERVAL_DAY, pi)
        return true
    }

    fun reschedule(context: Context) {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (!p.getBoolean("enabled", false)) return
        schedule(context, true, p.getInt("hour", 19), p.getInt("minute", 0), daysOf(p.getString("days", "")))
    }

    private fun daysOf(s: String?) = s.orEmpty().split(',').mapNotNull { it.toIntOrNull() }.toSet()

    /** Posts today's reminder; [dueReviews] makes the text concrete when words are waiting, otherwise the wording rotates by day. */
    fun post(context: Context, quietStart: Int, quietEnd: Int, dueReviews: Int = 0) {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val cal = Calendar.getInstance()
        // Calendar: Sunday=1..Saturday=7 → ISO Monday=1..Sunday=7
        val iso = ((cal.get(Calendar.DAY_OF_WEEK) + 5) % 7) + 1
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val inQuiet = if (quietStart > quietEnd) hour >= quietStart || hour < quietEnd else hour in quietStart until quietEnd
        if (iso !in daysOf(p.getString("days", "1,2,3,4,5,6,7")) || inQuiet) return
        val nm = context.getSystemService(NotificationManager::class.java) ?: return
        nm.createNotificationChannel(NotificationChannel(CHANNEL, context.getString(R.string.reminder_channel), NotificationManager.IMPORTANCE_DEFAULT))
        val open = PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        val n = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(context.getString(R.string.reminder_title))
            .setContentText(body(context, dueReviews, cal.get(Calendar.DAY_OF_YEAR)))
            .setContentIntent(open).setAutoCancel(true).build()
        if (Build.VERSION.SDK_INT < 33 || NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            runCatching { NotificationManagerCompat.from(context).notify(REQUEST, n) }
        }
    }
}

internal fun body(context: Context, dueReviews: Int, dayOfYear: Int): String =
    if (dueReviews > 0) context.resources.getQuantityString(R.plurals.reminder_body_due, dueReviews, dueReviews)
    else context.getString(listOf(R.string.reminder_body, R.string.reminder_body_2, R.string.reminder_body_3)[dayOfYear % 3])

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as PolyglotApplication
        val pending = goAsync()
        app.appScope.launch {
            try {
                val r = app.services.store.currentSettings().reminders
                if (r.enabled) {
                    val due = runCatching { app.services.reviews.dueCount(app.services.store.currentProfile().activeLearningLanguage) }.getOrDefault(0)
                    Reminders.post(context, r.quietHoursStart, r.quietHoursEnd, due)
                }
            } finally { pending.finish() }
        }
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) Reminders.reschedule(context)
    }
}
