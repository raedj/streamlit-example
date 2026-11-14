// SIGNATURE STUB for type-checking only (tools/android-typecheck). Mirrors the public API of the real library; never shipped.
package androidx.core.app
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
class NotificationCompat {
    class Builder(context: Context, channelId: String) {
        fun setSmallIcon(icon: Int): Builder = this
        fun setContentTitle(title: CharSequence?): Builder = this
        fun setContentText(text: CharSequence?): Builder = this
        fun setContentIntent(intent: PendingIntent?): Builder = this
        fun setAutoCancel(autoCancel: Boolean): Builder = this
        fun build(): Notification = TODO()
    }
}
class NotificationManagerCompat private constructor() {
    fun areNotificationsEnabled(): Boolean = TODO()
    fun notify(id: Int, notification: Notification) {}
    companion object { @JvmStatic fun from(context: Context): NotificationManagerCompat = TODO() }
}
