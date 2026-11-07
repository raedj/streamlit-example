package com.polyglot.academy.ui.feature.tutor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.FrontHand
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.polyglot.academy.core.model.ConversationTurn
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.Speaker
import com.polyglot.academy.core.service.RealtimeProblem
import com.polyglot.academy.core.service.RealtimeStatus
import com.polyglot.academy.core.tutor.TutorRoster
import com.polyglot.academy.ui.app.LocalApp
import com.polyglot.academy.ui.app.ScrollScreen
import com.polyglot.academy.ui.designsystem.AvatarState
import com.polyglot.academy.ui.designsystem.Banner
import com.polyglot.academy.ui.designsystem.BannerKind
import com.polyglot.academy.ui.designsystem.LabeledProgress
import com.polyglot.academy.ui.designsystem.LearningText
import com.polyglot.academy.ui.designsystem.Pill
import com.polyglot.academy.ui.designsystem.PolyCard
import com.polyglot.academy.ui.designsystem.PolyglotTheme
import com.polyglot.academy.ui.designsystem.PrimaryButton
import com.polyglot.academy.ui.designsystem.SecondaryButton
import com.polyglot.academy.ui.designsystem.StateMessage
import com.polyglot.academy.ui.designsystem.TutorAvatar
import com.polyglot.academy.ui.i18n.K
import com.polyglot.academy.ui.i18n.LocalStrings
import com.polyglot.academy.ui.navigation.Route
import com.polyglot.academy.ui.navigation.SettingsSection
import kotlinx.coroutines.launch

/** A live voice call with a tutor over the realtime API, with a corrected transcript. */
@Composable
fun LiveVoiceScreen(tutorId: String, topic: String?) {
    val app = LocalApp.current
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val scope = rememberCoroutineScope()
    val lang = app.profile.activeLearningLanguage
    val tutor = TutorRoster.byId(tutorId)
    ScrollScreen(s[K.LIVE_TITLE]) {
        if (tutor == null) { StateMessage(s[K.ERROR_TITLE], s[K.LESSON_NOT_FOUND]); return@ScrollScreen }
        val controller = remember(tutorId, topic, lang) { app.services.realtime.session(scope, lang, tutor, topic) }
        DisposableEffect(controller) { onDispose { controller.end() } }
        val st by controller.state.collectAsState()
        val live = st.status == RealtimeStatus.LIVE
        val avatarState = when {
            st.tutorSpeaking -> AvatarState.SPEAKING
            st.learnerSpeaking -> AvatarState.LISTENING
            st.status == RealtimeStatus.CONNECTING || st.checking.isNotEmpty() -> AvatarState.THINKING
            else -> AvatarState.IDLE
        }

        PolyCard {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                TutorAvatar(tutor.avatar, s.f(K.TUTOR_AVATAR_DESC, tutor.name), size = 96.dp, state = avatarState,
                    language = lang, mood = com.polyglot.academy.core.tutor.AvatarMotion.mood(st.turns), mouthLevel = st.tutorLevel)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(tutor.name, style = MaterialTheme.typography.titleLarge)
                    Text(s[K.valueOf("LIVE_STATUS_" + st.status.name)], style = MaterialTheme.typography.bodyMedium, color = c.textSecondary,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                    topic?.let { Pill(s.f(K.CONV_TOPIC, it)) }
                }
            }
            if (live) {
                Text(when {
                    st.muted -> s[K.LIVE_MUTED]
                    st.tutorSpeaking -> s[K.LIVE_TUTOR_SPEAKING]
                    st.learnerSpeaking -> s[K.LIVE_YOU_SPEAKING]
                    else -> s[K.LIVE_YOUR_TURN]
                }, style = MaterialTheme.typography.titleSmall, color = c.primary)
                LabeledProgress(s[K.LIVE_MIC_LEVEL], "", (st.micLevel * 4f).coerceIn(0f, 1f))
            }
            when (st.status) {
                RealtimeStatus.IDLE, RealtimeStatus.ENDED, RealtimeStatus.FAILED ->
                    PrimaryButton(if (st.status == RealtimeStatus.IDLE) s[K.LIVE_START] else s[K.LIVE_AGAIN], {
                        scope.launch {
                            if (app.services.platform.audioCapture.availability() == com.polyglot.academy.core.speech.SpeechAvailability.PERMISSION_REQUIRED) app.platformUi.requestMicrophonePermission()
                            controller.start()
                        }
                    }, Modifier.fillMaxWidth(), icon = Icons.Filled.Phone)
                RealtimeStatus.CONNECTING -> PrimaryButton(s[K.LIVE_CONNECTING], {}, Modifier.fillMaxWidth(), loading = true)
                RealtimeStatus.LIVE -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SecondaryButton(if (st.muted) s[K.LIVE_UNMUTE] else s[K.LIVE_MUTE], { controller.setMuted(!st.muted) }, Modifier.weight(1f),
                            icon = if (st.muted) Icons.Filled.Mic else Icons.Filled.MicOff)
                        SecondaryButton(s[K.LIVE_INTERRUPT], { controller.interrupt() }, Modifier.weight(1f), enabled = st.tutorSpeaking, icon = Icons.Filled.FrontHand)
                    }
                    PrimaryButton(s[K.LIVE_END], { controller.end() }, Modifier.fillMaxWidth(), icon = Icons.Filled.CallEnd)
                }
            }
            if (st.status == RealtimeStatus.IDLE) Text(s[K.LIVE_HEADPHONES], style = MaterialTheme.typography.bodySmall, color = c.textMuted)
        }

        st.problem?.let { p ->
            val (msg, section) = when (p) {
                RealtimeProblem.NO_KEY -> s[K.LIVE_ERR_NO_KEY] to SettingsSection.PROVIDERS
                RealtimeProblem.NO_CONSENT -> s[K.VOICE_NEEDS_CONSENT] to SettingsSection.PRIVACY
                RealtimeProblem.OFFLINE -> s[K.OFFLINE_BANNER] to null
                RealtimeProblem.MICROPHONE -> s[K.PRON_ERR_MIC] to null
                RealtimeProblem.PERMISSION -> s[K.PRON_ERR_PERMISSION] to SettingsSection.PERMISSIONS
                RealtimeProblem.NO_SPEAKER -> s[K.LIVE_ERR_SPEAKER] to null
                RealtimeProblem.CONNECTION -> s.f(K.LIVE_ERR_CONNECTION, st.serverMessage.orEmpty()) to SettingsSection.VOICE
                RealtimeProblem.SERVER -> s.f(K.LIVE_ERR_SERVER, st.serverMessage.orEmpty()) to SettingsSection.VOICE
            }
            if (section != null) Banner(BannerKind.WARNING, msg, action = s[K.PRON_DETAIL_SET_UP], onAction = { app.nav.push(Route.SettingsPage(section)) })
            else Banner(BannerKind.WARNING, msg)
        }
        if (st.problem == null) st.serverMessage?.let { Banner(BannerKind.INFO, it) }

        st.turns.forEach { t -> LiveTurn(t, lang, tutor.name, checking = t.id in st.checking) }
        if (st.tutorDraft.isNotBlank()) LiveTurn(ConversationTurn("draft", "", Speaker.TUTOR, st.tutorDraft, createdAtMillis = 0), lang, tutor.name, false)
        if (st.status == RealtimeStatus.ENDED && st.turns.isNotEmpty()) Banner(BannerKind.SUCCESS, s.f(K.LIVE_SUMMARY, st.turns.count { it.speaker == Speaker.LEARNER },
            st.turns.sumOf { it.corrections.size }))
        Text(s[K.LIVE_PRIVACY], style = MaterialTheme.typography.bodySmall, color = c.textMuted)
    }
}

@Composable
private fun LiveTurn(t: ConversationTurn, lang: LanguageCode, tutorName: String, checking: Boolean) {
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val learner = t.speaker == Speaker.LEARNER
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (learner) Arrangement.End else Arrangement.Start) {
        Column(
            Modifier.widthIn(max = 540.dp).clip(RoundedCornerShape(16.dp)).background(if (learner) c.primaryContainer else c.card).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(if (learner) s[K.CONV_YOU] else tutorName, style = MaterialTheme.typography.labelMedium, color = if (learner) c.onPrimaryContainer else c.primary)
            LearningText(t.text, lang, style = MaterialTheme.typography.bodyLarge, color = if (learner) c.onPrimaryContainer else c.textPrimary)
            if (checking) Text(s[K.LIVE_CHECKING], style = MaterialTheme.typography.bodySmall, color = c.textMuted)
            t.corrections.forEach { CorrectionCard(it, lang) }
        }
    }
}
