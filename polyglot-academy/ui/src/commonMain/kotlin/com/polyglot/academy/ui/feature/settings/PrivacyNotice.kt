package com.polyglot.academy.ui.feature.settings

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.polyglot.academy.ui.app.LocalApp
import com.polyglot.academy.ui.app.ScrollScreen
import com.polyglot.academy.ui.designsystem.Banner
import com.polyglot.academy.ui.designsystem.BannerKind
import com.polyglot.academy.ui.designsystem.PolyCard
import com.polyglot.academy.ui.designsystem.PolyglotTheme
import com.polyglot.academy.ui.designsystem.QuietButton
import com.polyglot.academy.ui.designsystem.SectionHeader
import com.polyglot.academy.ui.i18n.K
import com.polyglot.academy.ui.i18n.LocalStrings
import com.polyglot.academy.ui.navigation.Route
import com.polyglot.academy.ui.navigation.SettingsSection

/**
 * What the app stores, what can leave the device and when, how long things are kept and how to
 * delete them. Every statement here matches docs/PRIVACY_AND_SECURITY.md and is backed by a test or
 * by the code; it is a plain description, not the legal privacy policy (which the owner publishes).
 */
@Composable
fun PrivacyNoticeScreen() {
    val app = LocalApp.current
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    ScrollScreen(s[K.PRIV_TITLE]) {
        Text(s.f(K.PRIV_INTRO, app.platformUi.appVersion), style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
        listOf(
            K.PRIV_STORED_TITLE to listOf(K.PRIV_STORED, K.PRIV_KEYS),
            K.PRIV_LEAVES_TITLE to listOf(K.PRIV_NO_SERVER, K.PRIV_AI, K.PRIV_VOICE, K.PRIV_SYNC, K.PRIV_TRANSCRIBE, K.PRIV_SPEECH),
            K.PRIV_PERMISSIONS_TITLE to listOf(K.PRIV_PERMISSIONS),
            K.PRIV_KEPT_TITLE to listOf(K.PRIV_KEPT_DEVICE, K.PRIV_KEPT_PHOTOS, K.PRIV_KEPT_REPORTS, K.PRIV_KEPT_SYNC, K.PRIV_KEPT_PROVIDERS, K.PRIV_KEPT_BACKUP),
            K.PRIV_DELETE_TITLE to listOf(K.PRIV_DELETE, K.PRIV_UNINSTALL),
        ).forEach { (title, lines) ->
            SectionHeader(s[title])
            PolyCard { lines.forEach { Text("• " + s[it], style = MaterialTheme.typography.bodyMedium) } }
        }
        QuietButton(s[K.PRIV_OPEN_SETTINGS], { app.nav.push(Route.SettingsPage(SettingsSection.PRIVACY)) })
        QuietButton(s[K.PRIV_OPEN_DATA], { app.nav.push(Route.SettingsPage(SettingsSection.DATA)) })
        Banner(BannerKind.INFO, s[K.PRIV_NOT_LEGAL])
    }
}
