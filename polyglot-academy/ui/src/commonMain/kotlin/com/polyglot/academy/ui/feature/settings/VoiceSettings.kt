package com.polyglot.academy.ui.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
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
import com.polyglot.academy.core.ai.ProviderId
import com.polyglot.academy.core.service.PronunciationReadiness
import com.polyglot.academy.core.service.RealtimeReadiness
import com.polyglot.academy.ui.app.LocalApp
import com.polyglot.academy.ui.designsystem.AppTextField
import com.polyglot.academy.ui.designsystem.Banner
import com.polyglot.academy.ui.designsystem.BannerKind
import com.polyglot.academy.ui.designsystem.ChipGroup
import com.polyglot.academy.ui.designsystem.Pill
import com.polyglot.academy.ui.designsystem.PolyCard
import com.polyglot.academy.ui.designsystem.PolyglotTheme
import com.polyglot.academy.ui.designsystem.PrimaryButton
import com.polyglot.academy.ui.designsystem.QuietButton
import com.polyglot.academy.ui.designsystem.SecondaryButton
import com.polyglot.academy.ui.designsystem.SectionHeader
import com.polyglot.academy.ui.i18n.K
import com.polyglot.academy.ui.i18n.LocalStrings
import com.polyglot.academy.ui.navigation.Route
import com.polyglot.academy.ui.navigation.SettingsSection
import kotlinx.coroutines.launch

/** Setup for the optional cloud voice services: pronunciation scoring and realtime voice. */
@Composable
internal fun VoicePage() {
    val app = LocalApp.current
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val scope = rememberCoroutineScope()
    val voice = app.settings.voiceServices
    var region by remember { mutableStateOf(voice.azureRegion) }
    var key by remember { mutableStateOf("") }
    var hasKey by remember { mutableStateOf(false) }
    var pron by remember { mutableStateOf<PronunciationReadiness?>(null) }
    var realtime by remember { mutableStateOf<RealtimeReadiness?>(null) }
    var message by remember { mutableStateOf<Pair<BannerKind, String>?>(null) }
    var testing by remember { mutableStateOf(false) }
    var model by remember { mutableStateOf(voice.realtimeModel) }
    LaunchedEffect(app.settings) {
        hasKey = app.services.pronunciation.hasKey()
        pron = app.services.pronunciation.readiness()
        realtime = app.services.realtime.readiness()
    }

    Text(s[K.VOICE_INTRO], style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
    val voiceAllowed = pron != PronunciationReadiness.NO_CONSENT && realtime != RealtimeReadiness.NO_CONSENT
    if (pron == PronunciationReadiness.NO_CONSENT || realtime == RealtimeReadiness.NO_CONSENT || !voiceAllowed) {
        Banner(BannerKind.INFO, s[K.VOICE_NEEDS_CONSENT], action = s[K.SET_PRIVACY], onAction = { app.nav.push(Route.SettingsPage(SettingsSection.PRIVACY)) })
    }

    SectionHeader(s[K.VOICE_PRON_TITLE])
    PolyCard {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { pron?.let { Pill(s[K.valueOf("VOICE_STATE_" + it.name)]) } }
        Text(s[K.VOICE_PRON_BODY], style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
        Text(s[K.VOICE_PRON_HOWTO], style = MaterialTheme.typography.bodySmall, color = c.textMuted)
        AppTextField(region, { region = it.take(30) }, s[K.VOICE_REGION], supporting = s[K.VOICE_REGION_HINT])
        AppTextField(key, { key = it.take(200) }, if (hasKey) s[K.VOICE_KEY_SAVED] else s[K.VOICE_KEY], secret = true)
        PrimaryButton(s[K.SAVE], {
            scope.launch {
                message = if (app.services.pronunciation.configure(region, key)) { key = ""; BannerKind.SUCCESS to s[K.VOICE_SAVED] } else BannerKind.WARNING to s[K.VOICE_BAD_REGION]
                hasKey = app.services.pronunciation.hasKey(); pron = app.services.pronunciation.readiness()
            }
        }, enabled = region.isNotBlank())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SecondaryButton(s[K.VOICE_TEST], {
                testing = true
                scope.launch {
                    message = app.services.pronunciation.verify().fold(
                        { ok -> if (ok) BannerKind.SUCCESS to s[K.VOICE_TEST_OK] else BannerKind.WARNING to s[K.VOICE_TEST_BAD_KEY] },
                        { e -> BannerKind.WARNING to s.f(K.VOICE_TEST_FAILED, e.message ?: "") })
                    testing = false
                }
            }, Modifier.weight(1f), enabled = hasKey && !testing)
            if (hasKey) QuietButton(s[K.VOICE_REMOVE_KEY], { scope.launch { app.services.pronunciation.removeKey(); hasKey = false; pron = app.services.pronunciation.readiness() } })
        }
        message?.let { (k, t) -> Banner(k, t) }
    }

    SectionHeader(s[K.VOICE_RT_TITLE])
    PolyCard {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { realtime?.let { Pill(s[K.valueOf("RT_STATE_" + it.name)]) } }
        Text(s[K.VOICE_RT_BODY], style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
        if (realtime == RealtimeReadiness.NO_KEY) QuietButton(s[K.VOICE_RT_ADD_KEY], { app.nav.push(Route.SettingsPage(SettingsSection.PROVIDERS)) })
        AppTextField(model, { model = it.take(60) }, s[K.VOICE_RT_MODEL], supporting = s[K.VOICE_RT_MODEL_HINT])
        Text(s[K.VOICE_RT_VOICE], style = MaterialTheme.typography.labelLarge)
        ChipGroup(com.polyglot.academy.core.realtime.RealtimeVoices.all, setOf(voice.realtimeVoice), { it }, { v ->
            scope.launch { app.services.store.updateSettings { it.copy(voiceServices = it.voiceServices.copy(realtimeVoice = v)) } }
        })
        PrimaryButton(s[K.SAVE], {
            scope.launch { app.services.store.updateSettings { it.copy(voiceServices = it.voiceServices.copy(realtimeModel = model.trim().ifEmpty { "gpt-realtime" })) } }
        })
        Text(s.f(K.VOICE_RT_KEY_NOTE, ProviderId.OPENAI.name), style = MaterialTheme.typography.bodySmall, color = c.textMuted)
    }
}
