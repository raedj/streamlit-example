package com.polyglot.academy.ui.feature.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.polyglot.academy.core.service.ConflictChoice
import com.polyglot.academy.core.service.SyncOutcome
import com.polyglot.academy.core.sync.SyncError
import com.polyglot.academy.ui.app.LocalApp
import com.polyglot.academy.ui.designsystem.AppTextField
import com.polyglot.academy.ui.designsystem.Banner
import com.polyglot.academy.ui.designsystem.BannerKind
import com.polyglot.academy.ui.designsystem.PolyCard
import com.polyglot.academy.ui.designsystem.PolyglotTheme
import com.polyglot.academy.ui.designsystem.PrimaryButton
import com.polyglot.academy.ui.designsystem.QuietButton
import com.polyglot.academy.ui.designsystem.SecondaryButton
import com.polyglot.academy.ui.designsystem.SectionHeader
import com.polyglot.academy.ui.designsystem.SwitchRow
import com.polyglot.academy.ui.i18n.K
import kotlinx.datetime.toLocalDateTime
import com.polyglot.academy.ui.i18n.LocalStrings
import com.polyglot.academy.ui.i18n.Localizer
import kotlinx.coroutines.launch

private fun shortDateTime(millis: Long): String {
    val t = kotlinx.datetime.Instant.fromEpochMilliseconds(millis).toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
    fun two(n: Int) = n.toString().padStart(2, '0')
    return "${t.year}-${two(t.monthNumber)}-${two(t.dayOfMonth)} ${two(t.hour)}:${two(t.minute)}"
}

private fun syncMessage(e: Throwable, s: Localizer): String = when (e) {
    SyncError.NotConfigured -> s[K.SYNC_ERR_SETUP]
    SyncError.InsecureUrl -> s[K.SYNC_ERR_INSECURE]
    SyncError.Authentication -> s[K.SYNC_ERR_AUTH]
    SyncError.WrongPassphrase -> s[K.SYNC_ERR_PASSPHRASE]
    SyncError.NotASyncFile -> s[K.SYNC_ERR_NOT_SYNC]
    SyncError.NewerFormat -> s[K.SYNC_ERR_NEWER]
    SyncError.Changed -> s[K.SYNC_ERR_CHANGED]
    is IllegalArgumentException -> s[K.SYNC_ERR_SHORT_PASSPHRASE]
    else -> s.f(K.SYNC_ERR_OTHER, e.message ?: "")
}

/** Sync through the learner's own WebDAV server, end-to-end encrypted with their passphrase. */
@Composable
internal fun SyncSection() {
    val app = LocalApp.current
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val scope = rememberCoroutineScope()
    val sync = app.settings.sync
    var url by remember { mutableStateOf(sync.serverUrl) }
    var user by remember { mutableStateOf(sync.username) }
    var password by remember { mutableStateOf("") }
    var passphrase by remember { mutableStateOf("") }
    var configured by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<Pair<BannerKind, String>?>(null) }
    var conflict by remember { mutableStateOf(false) }
    LaunchedEffect(app.settings.sync) { configured = app.services.sync.isConfigured() }

    fun run(choice: ConflictChoice? = null) {
        busy = true
        scope.launch {
            message = app.services.sync.sync(choice).fold({ o ->
                if (o == SyncOutcome.CONFLICT) { conflict = true; null } else BannerKind.SUCCESS to s[K.valueOf("SYNC_RESULT_" + o.name)]
            }, { e -> BannerKind.WARNING to syncMessage(e, s) })
            busy = false
        }
    }

    SectionHeader(s[K.SYNC_TITLE])
    PolyCard {
        Text(s[K.SYNC_BODY], style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
        AppTextField(url, { url = it.take(300) }, s[K.SYNC_URL], supporting = s[K.SYNC_URL_HINT])
        AppTextField(user, { user = it.take(120) }, s[K.SYNC_USER])
        AppTextField(password, { password = it.take(200) }, if (configured) s[K.SYNC_PASSWORD_SAVED] else s[K.SYNC_PASSWORD], secret = true)
        AppTextField(passphrase, { passphrase = it.take(200) }, if (configured) s[K.SYNC_PASSPHRASE_SAVED] else s[K.SYNC_PASSPHRASE], secret = true,
            supporting = s[K.SYNC_PASSPHRASE_HINT])
        PrimaryButton(s[K.SAVE], {
            scope.launch {
                message = app.services.sync.configure(url, user, password, passphrase).fold(
                    { password = ""; passphrase = ""; BannerKind.SUCCESS to s[K.SYNC_SAVED] }, { e -> BannerKind.WARNING to syncMessage(e, s) })
                configured = app.services.sync.isConfigured()
            }
        }, Modifier.fillMaxWidth(), enabled = url.isNotBlank() && user.isNotBlank())
    }
    PolyCard(contentPadding = 0.dp) {
        SwitchRow(s[K.SET_EXPORT_CONV], sync.includeConversations, { v -> scope.launch { app.services.sync.setIncluded(v, sync.includeNotes) } })
        SwitchRow(s[K.SET_EXPORT_NOTES], sync.includeNotes, { v -> scope.launch { app.services.sync.setIncluded(sync.includeConversations, v) } })
    }
    SecondaryButton(s[K.SYNC_NOW], { run() }, Modifier.fillMaxWidth(), enabled = configured && !busy)
    Text(sync.lastSyncAtMillis?.let { s.f(K.SYNC_LAST, shortDateTime(it)) } ?: s[K.SYNC_NEVER], style = MaterialTheme.typography.bodySmall, color = c.textMuted)
    message?.let { (k, t) -> Banner(k, t) }
    if (configured) QuietButton(s[K.SYNC_DISCONNECT], { scope.launch { app.services.sync.disconnect(); configured = false; message = null } })
    Text(s[K.SYNC_NOTE], style = MaterialTheme.typography.bodySmall, color = c.textMuted)

    if (conflict) AlertDialog(onDismissRequest = { conflict = false }, title = { Text(s[K.SYNC_CONFLICT_TITLE]) }, text = { Text(s[K.SYNC_CONFLICT_BODY]) },
        confirmButton = { QuietButton(s[K.SYNC_KEEP_THIS], { conflict = false; run(ConflictChoice.KEEP_THIS_DEVICE) }) },
        dismissButton = { QuietButton(s[K.SYNC_USE_SERVER], { conflict = false; run(ConflictChoice.USE_SERVER) }) })
}
