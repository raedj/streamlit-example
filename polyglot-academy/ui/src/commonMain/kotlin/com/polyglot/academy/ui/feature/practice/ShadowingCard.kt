package com.polyglot.academy.ui.feature.practice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.speaking.Shadowing
import com.polyglot.academy.core.speech.AudioCaptureException
import com.polyglot.academy.core.speech.AudioOutput
import com.polyglot.academy.core.speech.SpeechAvailability
import com.polyglot.academy.core.speech.Wav
import com.polyglot.academy.ui.app.LocalApp
import com.polyglot.academy.ui.app.Speaker
import com.polyglot.academy.ui.designsystem.Banner
import com.polyglot.academy.ui.designsystem.BannerKind
import com.polyglot.academy.ui.designsystem.LabeledProgress
import com.polyglot.academy.ui.designsystem.PolyCard
import com.polyglot.academy.ui.designsystem.PolyglotTheme
import com.polyglot.academy.ui.designsystem.PrimaryButton
import com.polyglot.academy.ui.designsystem.SecondaryButton
import com.polyglot.academy.ui.i18n.K
import com.polyglot.academy.ui.i18n.LocalStrings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Shadowing: hear the model, say the sentence straight after it, then hear the two one after the other.
 * Works offline. The recording is kept in memory only and is gone when the card leaves the screen.
 */
@Composable
internal fun ShadowingCard(sentence: String, language: LanguageCode, speaker: Speaker, modifier: Modifier = Modifier) {
    val app = LocalApp.current
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val scope = rememberCoroutineScope()
    val clock = app.services.clock
    val output = app.services.platform.audioOutput
    var recording by remember(sentence) { mutableStateOf<ByteArray?>(null) }
    var result by remember(sentence) { mutableStateOf<Shadowing.Result?>(null) }
    var modelMillis by remember(sentence) { mutableStateOf<Long?>(null) }
    var recordingNow by remember(sentence) { mutableStateOf(false) }
    var stopRequested by remember(sentence) { mutableStateOf(false) }
    var playing by remember(sentence) { mutableStateOf(false) }
    var level by remember { mutableFloatStateOf(0f) }
    var error by remember(sentence) { mutableStateOf<String?>(null) }
    var job by remember { mutableStateOf<Job?>(null) }
    DisposableEffect(sentence) { onDispose { job?.cancel() } }

    /** Plays the model through text-to-speech and waits for it to end; returns how long it played, or null. */
    suspend fun playModel(): Long? {
        speaker.play(sentence, language)
        withTimeoutOrNull(3_000) { snapshotFlow { speaker.playingText }.first { it == sentence } } ?: return null
        val start = clock.nowMillis()
        snapshotFlow { speaker.playingText }.first { it != sentence }
        return (clock.nowMillis() - start).takeIf { it > 300 }
    }

    fun launchPlayback(withModel: Boolean) {
        val pcm = recording ?: return
        job?.cancel()
        job = scope.launch {
            playing = true
            try {
                if (withModel) { playModel()?.let { modelMillis = it }; delay(250) }
                if (!playPcm(output, pcm, Shadowing.SAMPLE_RATE)) error = s[K.SHADOW_NO_PLAYBACK]
            } finally { playing = false }
        }
    }

    PolyCard(modifier) {
        Text(s[K.SHADOW_TITLE], style = MaterialTheme.typography.titleMedium)
        Text(s[K.SHADOW_BODY], style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SecondaryButton(s[K.SHADOW_LISTEN], {
                job?.cancel()
                job = scope.launch { playing = true; try { playModel()?.let { modelMillis = it } } finally { playing = false } }
            }, Modifier.weight(1f), enabled = !recordingNow, icon = Icons.AutoMirrored.Filled.VolumeUp)
            PrimaryButton(if (recordingNow) s[K.SHADOW_STOP] else s[K.SHADOW_RECORD], {
                if (recordingNow) { stopRequested = true; return@PrimaryButton }
                job?.cancel(); speaker.stop()
                stopRequested = false; error = null
                job = scope.launch {
                    val capture = app.services.platform.audioCapture
                    if (capture.availability() == SpeechAvailability.PERMISSION_REQUIRED && !app.platformUi.requestMicrophonePermission()) {
                        error = s[K.PRON_ERR_PERMISSION]; return@launch
                    }
                    recordingNow = true
                    val chunks = mutableListOf<ByteArray>()
                    val failure = runCatching {
                        withTimeoutOrNull(Shadowing.MAX_SECONDS * 1000L) {
                            capture.record(Shadowing.SAMPLE_RATE).collect { chunk ->
                                chunks += chunk; level = Wav.level(chunk)
                                if (stopRequested) throw StopShadowing
                            }
                        }
                    }.exceptionOrNull()?.takeIf { it !is StopShadowing }
                    recordingNow = false; level = 0f
                    if (failure != null) {
                        error = if (failure is AudioCaptureException) s[K.PRON_ERR_MIC] else s.f(K.PRON_ERR_OTHER, failure.message ?: "")
                        return@launch
                    }
                    val pcm = ByteArray(chunks.sumOf { it.size }).also { out -> var at = 0; chunks.forEach { it.copyInto(out, at); at += it.size } }
                    recording = pcm
                    result = Shadowing.check(pcm, modelMillis)
                }
            }, Modifier.weight(1f), icon = if (recordingNow) Icons.Filled.Stop else Icons.Filled.Mic)
        }
        if (recordingNow) LabeledProgress(s[K.PRON_DETAIL_LISTENING], "", (level * 4f).coerceIn(0f, 1f))
        error?.let { Banner(BannerKind.WARNING, it) }
        val r = result
        if (r != null && recording != null && !recordingNow) {
            // The model time can arrive after the recording (the learner listened again), so the check is redone with it.
            val shown = if (r.ratio == null && modelMillis != null) Shadowing.check(recording!!, modelMillis) else r
            when (shown.pace) {
                Shadowing.Pace.NOTHING_HEARD -> Banner(BannerKind.WARNING, s[K.SHADOW_NOTHING])
                else -> {
                    val mine = seconds(shown.voicedMillis)
                    val model = modelMillis?.let { seconds(it) }
                    val text = when {
                        model == null -> s.f(K.SHADOW_LENGTH, mine)
                        shown.pace == Shadowing.Pace.MUCH_SLOWER -> s.f(K.SHADOW_SLOWER, mine, model)
                        shown.pace == Shadowing.Pace.MUCH_FASTER -> s.f(K.SHADOW_FASTER, mine, model)
                        else -> s.f(K.SHADOW_CLOSE, mine, model)
                    }
                    Text(text, style = MaterialTheme.typography.bodyMedium, color = if (shown.pace == Shadowing.Pace.CLOSE) c.success else c.warning)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SecondaryButton(s[K.SHADOW_PLAY_MINE], { launchPlayback(false) }, Modifier.weight(1f), enabled = !playing, icon = Icons.Filled.PlayArrow)
                SecondaryButton(s[K.SHADOW_PLAY_BOTH], { launchPlayback(true) }, Modifier.weight(1f), enabled = !playing, icon = Icons.Filled.PlayArrow)
            }
            Text(s[K.SHADOW_EAR], style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
        }
        Text(s[K.SHADOW_PRIVACY], style = MaterialTheme.typography.bodySmall, color = c.textMuted)
    }
}

/** Thrown inside the capture flow to end a recording when the learner taps Stop. */
private object StopShadowing : Exception()

/** "2.4" for 2400 ms. */
internal fun seconds(ms: Long): String = "${ms / 1000}.${(ms % 1000) / 100}"

/** Plays 16-bit mono PCM to the end; false when the device has no audio output. Cancelling stops it at once. */
private suspend fun playPcm(output: AudioOutput, pcm: ByteArray, sampleRate: Int): Boolean = withContext(Dispatchers.Default) {
    val sink = output.open(sampleRate) ?: return@withContext false
    val started = kotlinx.coroutines.currentCoroutineContext()
    var completed = false
    try {
        // A tenth of a second at a time, so Stop or leaving the screen ends the sound quickly.
        val step = sampleRate / 10 * 2
        var at = 0
        while (at < pcm.size) {
            started.ensureActive()
            val end = minOf(pcm.size, at + step)
            sink.write(pcm.copyOfRange(at, end))
            at = end
        }
        // The last part is still in the device buffer.
        delay(400)
        completed = true
    } finally {
        if (!completed) sink.clear()
        sink.close()
    }
    true
}
