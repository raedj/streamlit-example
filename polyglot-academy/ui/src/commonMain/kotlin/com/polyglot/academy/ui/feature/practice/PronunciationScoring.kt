package com.polyglot.academy.ui.feature.practice

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.pronunciation.PronunciationError
import com.polyglot.academy.core.pronunciation.PronunciationReport
import com.polyglot.academy.core.pronunciation.WordScore
import com.polyglot.academy.core.service.PronunciationReadiness
import com.polyglot.academy.core.service.PronunciationService
import com.polyglot.academy.core.speech.AudioCaptureException
import com.polyglot.academy.core.speech.SpeechAvailability
import com.polyglot.academy.core.speech.Wav
import com.polyglot.academy.ui.app.LocalApp
import com.polyglot.academy.ui.designsystem.Banner
import com.polyglot.academy.ui.designsystem.BannerKind
import com.polyglot.academy.ui.designsystem.LabeledProgress
import com.polyglot.academy.ui.designsystem.LearningText
import com.polyglot.academy.ui.designsystem.PolyCard
import com.polyglot.academy.ui.designsystem.PolyglotTheme
import com.polyglot.academy.ui.designsystem.PrimaryButton
import com.polyglot.academy.ui.designsystem.QuietButton
import com.polyglot.academy.ui.designsystem.StatTile
import com.polyglot.academy.ui.i18n.K
import com.polyglot.academy.ui.i18n.LocalStrings
import com.polyglot.academy.ui.navigation.Route
import com.polyglot.academy.ui.navigation.SettingsSection
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

private sealed interface ScoringState {
    data object Idle : ScoringState
    data object Recording : ScoringState
    data object Scoring : ScoringState
    data class Done(val report: PronunciationReport) : ScoringState
    data class Failed(val message: String, val setup: SettingsSection? = null) : ScoringState
}

@Composable
internal fun scoreColor(score: Double?): Color {
    val c = PolyglotTheme.colors
    return when {
        score == null -> c.textMuted
        score >= 80 -> c.success
        score >= PronunciationReport.WEAK_BELOW -> c.warning
        else -> c.error
    }
}

/** Records the learner reading [reference] and shows per-word and per-sound scores from the speech service. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DetailedPronunciationCard(reference: String, language: LanguageCode) {
    val app = LocalApp.current
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val scope = rememberCoroutineScope()
    var readiness by remember { mutableStateOf<PronunciationReadiness?>(null) }
    var state by remember(reference) { mutableStateOf<ScoringState>(ScoringState.Idle) }
    var level by remember { mutableFloatStateOf(0f) }
    var job by remember { mutableStateOf<Job?>(null) }
    var stopRequested by remember { mutableStateOf(false) }
    var selectedWord by remember(reference) { mutableStateOf<WordScore?>(null) }
    LaunchedEffect(app.settings) { readiness = app.services.pronunciation.readiness() }

    PolyCard {
        Text(s[K.PRON_DETAIL_TITLE], style = MaterialTheme.typography.titleMedium)
        when (readiness) {
            null -> return@PolyCard
            PronunciationReadiness.NOT_CONFIGURED -> {
                Text(s[K.PRON_DETAIL_SETUP], style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
                QuietButton(s[K.PRON_DETAIL_SET_UP], { app.nav.push(Route.SettingsPage(SettingsSection.VOICE)) })
                return@PolyCard
            }
            PronunciationReadiness.NO_CONSENT -> {
                Banner(BannerKind.INFO, s[K.VOICE_NEEDS_CONSENT], action = s[K.SET_PRIVACY], onAction = { app.nav.push(Route.SettingsPage(SettingsSection.PRIVACY)) })
                return@PolyCard
            }
            PronunciationReadiness.OFFLINE -> { Text(s[K.OFFLINE_BANNER], color = c.textSecondary); return@PolyCard }
            PronunciationReadiness.READY -> Unit
        }
        Text(s[K.PRON_DETAIL_BODY], style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
        LearningText(reference, language, style = PolyglotTheme.learning.sentence)

        val recording = state == ScoringState.Recording
        PrimaryButton(if (recording) s[K.PRON_DETAIL_STOP] else s[K.PRON_DETAIL_RECORD], {
            if (recording) { stopRequested = true; return@PrimaryButton }
            stopRequested = false; selectedWord = null
            job = scope.launch {
                if (app.services.platform.audioCapture.availability() == SpeechAvailability.PERMISSION_REQUIRED && !app.platformUi.requestMicrophonePermission()) {
                    state = ScoringState.Failed(s[K.PRON_ERR_PERMISSION]); return@launch
                }
                state = ScoringState.Recording
                val chunks = mutableListOf<ByteArray>()
                val failure = runCatching {
                    withTimeoutOrNull(PronunciationService.MAX_SECONDS * 1000L) {
                        app.services.platform.audioCapture.record(PronunciationService.SAMPLE_RATE).collect { chunk ->
                            chunks += chunk; level = Wav.level(chunk)
                            if (stopRequested) throw StopRecording
                        }
                    }
                }.exceptionOrNull()?.takeIf { it !is StopRecording }
                level = 0f
                if (failure != null) {
                    state = ScoringState.Failed(if (failure is AudioCaptureException) s[K.PRON_ERR_MIC] else s.f(K.PRON_ERR_OTHER, failure.message ?: "")); return@launch
                }
                state = ScoringState.Scoring
                val pcm = ByteArray(chunks.sumOf { it.size }).also { out -> var at = 0; chunks.forEach { it.copyInto(out, at); at += it.size } }
                state = app.services.pronunciation.assess(language, reference, pcm).fold({ ScoringState.Done(it) }, { e -> failureState(e, s) })
            }
        }, Modifier.fillMaxWidth(), enabled = state != ScoringState.Scoring, loading = state == ScoringState.Scoring, icon = Icons.Filled.Mic)
        if (recording) {
            LabeledProgress(s[K.PRON_DETAIL_LISTENING], "", (level * 4f).coerceIn(0f, 1f))
        }
        when (val st = state) {
            is ScoringState.Failed -> st.setup?.let { sec -> Banner(BannerKind.WARNING, st.message, action = s[K.PRON_DETAIL_SET_UP], onAction = { app.nav.push(Route.SettingsPage(sec)) }) }
                ?: Banner(BannerKind.WARNING, st.message)
            is ScoringState.Done -> ReportView(st.report, language, selectedWord) { selectedWord = it }
            else -> Unit
        }
        Text(s[K.PRON_DETAIL_PRIVACY], style = MaterialTheme.typography.bodySmall, color = c.textMuted)
    }
}

/** Thrown inside the capture flow to end a recording when the learner taps Stop. */
private object StopRecording : Exception()

private fun failureState(e: Throwable, s: com.polyglot.academy.ui.i18n.Localizer): ScoringState.Failed = when (e) {
    PronunciationError.NotConfigured -> ScoringState.Failed(s[K.PRON_DETAIL_SETUP], SettingsSection.VOICE)
    PronunciationError.NoConsent -> ScoringState.Failed(s[K.VOICE_NEEDS_CONSENT], SettingsSection.PRIVACY)
    PronunciationError.Authentication -> ScoringState.Failed(s[K.VOICE_TEST_BAD_KEY], SettingsSection.VOICE)
    PronunciationError.Quota -> ScoringState.Failed(s[K.PRON_ERR_QUOTA])
    PronunciationError.NoSpeech -> ScoringState.Failed(s[K.PRON_ERR_NO_SPEECH])
    PronunciationError.TooShort -> ScoringState.Failed(s[K.PRON_ERR_TOO_SHORT])
    PronunciationError.UnsupportedLanguage -> ScoringState.Failed(s[K.PRON_ERR_LANGUAGE])
    PronunciationError.Offline -> ScoringState.Failed(s[K.OFFLINE_BANNER])
    else -> ScoringState.Failed(s.f(K.PRON_ERR_OTHER, e.message ?: ""))
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReportView(r: PronunciationReport, language: LanguageCode, selected: WordScore?, onSelect: (WordScore) -> Unit) {
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    fun fmt(v: Double?) = v?.let { "${it.toInt()}" } ?: "—"
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatTile(fmt(r.overall ?: r.accuracy), s[K.PRON_SCORE_OVERALL], Icons.Filled.TaskAlt, scoreColor(r.overall ?: r.accuracy), Modifier.weight(1f))
        StatTile(fmt(r.accuracy), s[K.PRON_SCORE_ACCURACY], Icons.Filled.GraphicEq, scoreColor(r.accuracy), Modifier.weight(1f))
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatTile(fmt(r.fluency), s[K.PRON_SCORE_FLUENCY], Icons.Filled.Speed, scoreColor(r.fluency), Modifier.weight(1f))
        StatTile(fmt(r.completeness), s[K.PRON_SCORE_COMPLETENESS], Icons.Filled.TaskAlt, scoreColor(r.completeness), Modifier.weight(1f))
    }
    r.prosody?.let { Text(s.f(K.PRON_SCORE_PROSODY, it.toInt()), style = MaterialTheme.typography.bodyMedium) }
    if (r.recognizedText.isNotBlank()) Text(s.f(K.PRON_HEARD, r.recognizedText), style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
    Text(s[K.PRON_WORDS_TAP], style = MaterialTheme.typography.labelLarge)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        r.words.forEach { w ->
            val col = if (w.error == com.polyglot.academy.core.pronunciation.WordError.OMISSION) c.error else scoreColor(w.accuracy)
            val desc = s.f(K.PRON_WORD_DESC, w.word, fmt(w.accuracy), s[K.valueOf("PRON_ERR_TYPE_" + w.error.name)])
            LearningText(w.word, language, style = MaterialTheme.typography.titleMedium, color = col,
                modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(if (selected == w) c.selection else col.copy(alpha = 0.12f))
                    .clickable(role = Role.Button) { onSelect(w) }.padding(horizontal = 8.dp, vertical = 4.dp).semantics { contentDescription = desc })
        }
    }
    selected?.let { w ->
        PolyCard(containerColor = c.surfaceElevated) {
            LearningText(w.word, language, style = MaterialTheme.typography.titleLarge)
            Text(s.f(K.PRON_WORD_SCORE, fmt(w.accuracy), s[K.valueOf("PRON_ERR_TYPE_" + w.error.name)]), style = MaterialTheme.typography.bodyMedium)
            if (w.phonemes.isEmpty()) Text(s[K.PRON_NO_PHONEMES], style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
            w.phonemes.forEach { p -> LabeledProgress("/${p.phoneme}/", "${p.accuracy.toInt()}", (p.accuracy / 100).toFloat(), color = scoreColor(p.accuracy)) }
        }
    }
    val weak = r.weakPhonemes
    if (weak.isNotEmpty()) Text(s.f(K.PRON_WEAK_SOUNDS, weak.joinToString("  ") { "/${it.phoneme}/" }), style = MaterialTheme.typography.bodyMedium, color = c.warning)
    else if ((r.overall ?: r.accuracy ?: 0.0) >= 80) Text(s[K.PRON_GREAT], style = MaterialTheme.typography.bodyMedium, color = c.success)
    Text(s.f(K.PRON_SCORED_BY, r.provider), style = MaterialTheme.typography.bodySmall, color = c.textMuted)
}
