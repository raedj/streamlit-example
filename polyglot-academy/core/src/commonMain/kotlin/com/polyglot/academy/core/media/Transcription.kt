package com.polyglot.academy.core.media

import com.polyglot.academy.core.model.LanguageCode

/** What the transcriber is doing, for the progress line. */
enum class TranscriptionStage { DECODING, RECOGNISING }

sealed interface TranscriptionResult {
    /** [truncated]: only the first [DeviceTranscriber.MAX_MINUTES] minutes were transcribed. */
    data class Text(val text: String, val seconds: Int, val truncated: Boolean) : TranscriptionResult
    data object NoSpeech : TranscriptionResult
    data object ModelMissing : TranscriptionResult
    /** The file has no audio track the device can decode. */
    data object Unreadable : TranscriptionResult
    data class Failed(val reason: String) : TranscriptionResult
}

/**
 * Speech to text that runs entirely on the device (Whisper via whisper.cpp on Android). The model is
 * not bundled: it is downloaded once, on the learner's request, and checked before use. Audio never
 * leaves the device.
 */
interface DeviceTranscriber {
    val languages: Set<LanguageCode>
    /** Size of the model download, for the consent text. */
    val modelSizeBytes: Long
    fun isModelReady(): Boolean
    /** Downloads and verifies the model; [onProgress] gets 0..1. */
    suspend fun downloadModel(onProgress: (Float) -> Unit): Result<Unit>
    fun deleteModel()
    /** [source] is a platform path to an audio or video file kept for this import. */
    suspend fun transcribe(source: String, language: LanguageCode, onStage: (TranscriptionStage) -> Unit): TranscriptionResult

    companion object {
        /** Longer recordings are cut here (memory: 16 kHz floats) and the learner is told. */
        const val MAX_MINUTES = 10
    }
}
