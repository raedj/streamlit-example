package com.polyglot.academy.ui.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.Languages
import com.polyglot.academy.core.speech.SpeechSynthesizerService
import com.polyglot.academy.core.speech.SynthesisEvent
import com.polyglot.academy.ui.i18n.K
import com.polyglot.academy.ui.i18n.Labels
import com.polyglot.academy.ui.i18n.Localizer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/** Plays target-language audio through platform TTS and exposes which text is currently playing. */
@Stable
class Speaker(private val tts: SpeechSynthesizerService, private val scope: CoroutineScope, private val onUnavailable: (LanguageCode) -> Unit, private val baseRate: Float) {
    var playingText by mutableStateOf<String?>(null)
        private set
    private var job: Job? = null

    fun toggle(text: String, language: LanguageCode, rateMultiplier: Float = 1f) {
        if (playingText == text) { stop(); return }
        play(text, language, rateMultiplier)
    }

    fun play(text: String, language: LanguageCode, rateMultiplier: Float = 1f) {
        stop()
        val profile = Languages.of(language)
        job = scope.launch {
            if (!tts.isLanguageAvailable(profile.defaultVariant.localeTag)) { onUnavailable(language); return@launch }
            playingText = text
            try {
                tts.speak(text, profile.defaultVariant.localeTag, baseRate * profile.defaultSpeechRate * rateMultiplier).collect { e ->
                    if (e is SynthesisEvent.Done || e is SynthesisEvent.Error) playingText = null
                }
            } finally { if (playingText == text) playingText = null }
        }
    }

    fun stop() { job?.cancel(); job = null; tts.stop(); playingText = null }
}

@Composable
fun rememberSpeaker(s: Localizer): Speaker {
    val app = LocalApp.current
    val scope = rememberCoroutineScope()
    val toast = rememberToast()
    val speaker = remember(app.settings.speech.speechRate) {
        Speaker(app.services.platform.synthesizer, scope, { lang -> toast(s.f(K.TTS_MISSING, Labels.language(s, lang))) }, app.settings.speech.speechRate)
    }
    // Audio must never keep playing after the screen closes.
    DisposableEffect(speaker) { onDispose { speaker.stop() } }
    return speaker
}
