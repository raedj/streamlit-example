package com.polyglot.academy.core.speech

import kotlinx.coroutines.flow.Flow

enum class SpeechAvailability { AVAILABLE, NOT_INSTALLED, LANGUAGE_UNSUPPORTED, PERMISSION_REQUIRED, PERMISSION_DENIED }

enum class SpeechErrorKind(val recoverable: Boolean) {
    PERMISSION_DENIED(false),
    MICROPHONE_UNAVAILABLE(false),
    NO_SPEECH_DETECTED(true),
    RECOGNITION_TIMEOUT(true),
    LANGUAGE_UNSUPPORTED(false),
    NETWORK(true),
    RECOGNIZER_BUSY(true),
    AUDIO_FOCUS_LOST(true),
    TTS_UNAVAILABLE(false),
    TTS_FAILED(true),
    CANCELLED(true),
    UNKNOWN(true),
}

sealed interface RecognitionEvent {
    data object Ready : RecognitionEvent
    data class LevelChanged(val level: Float) : RecognitionEvent
    data class Partial(val text: String) : RecognitionEvent
    data class Final(val text: String, val confidence: Float?, val alternatives: List<String>) : RecognitionEvent
    data class Error(val kind: SpeechErrorKind) : RecognitionEvent
}

sealed interface SynthesisEvent {
    data object Started : SynthesisEvent
    data class Progress(val charStart: Int, val charEnd: Int) : SynthesisEvent
    data object Done : SynthesisEvent
    data class Error(val kind: SpeechErrorKind) : SynthesisEvent
}

/** Platform speech-to-text. The flow ends after a Final or Error event; cancelling it stops recording. */
interface SpeechRecognizerService {
    suspend fun availability(localeTag: String): SpeechAvailability
    fun listen(localeTag: String): Flow<RecognitionEvent>
    /** Stop listening and deliver a final result if one is available. */
    fun stopListening()
    /** Whether audio leaves the device during recognition (cloud recognizer). Shown in the UI. */
    val processesAudioOnDevice: Boolean?
}

/** Platform text-to-speech. */
interface SpeechSynthesizerService {
    suspend fun isLanguageAvailable(localeTag: String): Boolean
    fun speak(text: String, localeTag: String, rate: Float): Flow<SynthesisEvent>
    fun stop()
}

/** Used where the platform has no speech engine (desktop harness, tests); always reports unavailability honestly. */
object UnavailableSpeechRecognizer : SpeechRecognizerService {
    override suspend fun availability(localeTag: String) = SpeechAvailability.NOT_INSTALLED
    override fun listen(localeTag: String): Flow<RecognitionEvent> = kotlinx.coroutines.flow.flowOf(RecognitionEvent.Error(SpeechErrorKind.MICROPHONE_UNAVAILABLE))
    override fun stopListening() {}
    override val processesAudioOnDevice: Boolean? = null
}

object UnavailableSpeechSynthesizer : SpeechSynthesizerService {
    override suspend fun isLanguageAvailable(localeTag: String) = false
    override fun speak(text: String, localeTag: String, rate: Float): Flow<SynthesisEvent> = kotlinx.coroutines.flow.flowOf(SynthesisEvent.Error(SpeechErrorKind.TTS_UNAVAILABLE))
    override fun stop() {}
}

/** Visible state of the voice pipeline. Every state has an exit, so the UI can never spin forever. */
sealed interface VoiceState {
    data object Idle : VoiceState
    data class Listening(val partial: String, val level: Float) : VoiceState
    /** Waiting for the tutor (AI or scripted) reply. */
    data object Processing : VoiceState
    data class Speaking(val turnId: String) : VoiceState
    data object Paused : VoiceState
    data class Failed(val kind: SpeechErrorKind) : VoiceState
    data object Completed : VoiceState
}

sealed interface VoiceEvent {
    data object StartListening : VoiceEvent
    data class Recognition(val event: RecognitionEvent) : VoiceEvent
    data object SubmitTyped : VoiceEvent
    data object ReplyReady : VoiceEvent
    data class SpeakStarted(val turnId: String) : VoiceEvent
    data object SpeakFinished : VoiceEvent
    data class SpeakFailed(val kind: SpeechErrorKind) : VoiceEvent
    data object ReplyFailed : VoiceEvent
    data object Pause : VoiceEvent
    data object Resume : VoiceEvent
    data object Cancel : VoiceEvent
    data object Finish : VoiceEvent
    data object DismissError : VoiceEvent
}

/** Pure transition function for the voice pipeline (unit-tested exhaustively). */
object VoiceStateMachine {
    fun reduce(state: VoiceState, event: VoiceEvent): VoiceState = when (event) {
        VoiceEvent.Finish -> VoiceState.Completed
        VoiceEvent.Cancel -> if (state is VoiceState.Completed) state else VoiceState.Idle
        VoiceEvent.Pause -> if (state is VoiceState.Speaking || state is VoiceState.Listening) VoiceState.Paused else state
        VoiceEvent.Resume -> if (state is VoiceState.Paused) VoiceState.Idle else state
        VoiceEvent.DismissError -> if (state is VoiceState.Failed) VoiceState.Idle else state
        VoiceEvent.StartListening -> when (state) {
            is VoiceState.Idle, is VoiceState.Failed, is VoiceState.Paused, is VoiceState.Speaking -> VoiceState.Listening("", 0f)
            else -> state
        }
        is VoiceEvent.Recognition -> if (state !is VoiceState.Listening) state else when (val e = event.event) {
            RecognitionEvent.Ready -> state
            is RecognitionEvent.LevelChanged -> state.copy(level = e.level)
            is RecognitionEvent.Partial -> state.copy(partial = e.text)
            is RecognitionEvent.Final -> if (e.text.isBlank()) VoiceState.Failed(SpeechErrorKind.NO_SPEECH_DETECTED) else VoiceState.Processing
            is RecognitionEvent.Error -> VoiceState.Failed(e.kind)
        }
        VoiceEvent.SubmitTyped -> when (state) {
            is VoiceState.Processing, is VoiceState.Completed -> state
            else -> VoiceState.Processing
        }
        VoiceEvent.ReplyReady -> if (state is VoiceState.Processing) VoiceState.Idle else state
        VoiceEvent.ReplyFailed -> if (state is VoiceState.Processing) VoiceState.Failed(SpeechErrorKind.NETWORK) else state
        is VoiceEvent.SpeakStarted -> if (state is VoiceState.Idle || state is VoiceState.Processing) VoiceState.Speaking(event.turnId) else state
        VoiceEvent.SpeakFinished -> if (state is VoiceState.Speaking) VoiceState.Idle else state
        is VoiceEvent.SpeakFailed -> if (state is VoiceState.Speaking) VoiceState.Failed(event.kind) else state
    }
}
