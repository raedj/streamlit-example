package com.polyglot.academy.core.speech

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/** Raw microphone capture (16-bit little-endian mono PCM), used for pronunciation scoring and realtime voice. */
interface AudioCapture {
    suspend fun availability(): SpeechAvailability
    /** Emits PCM chunks at [sampleRate] until the flow is cancelled. Throws [AudioCaptureException] if the microphone fails. */
    fun record(sampleRate: Int): Flow<ByteArray>
}

class AudioCaptureException(val kind: SpeechErrorKind) : Exception(kind.name)

/** Plays streamed PCM (16-bit little-endian mono). */
interface AudioOutput {
    fun open(sampleRate: Int): PcmSink?
}

interface PcmSink {
    fun write(pcm: ByteArray)
    /** Drops audio not yet played (the learner interrupted). */
    fun clear()
    fun close()
}

object NoAudioCapture : AudioCapture {
    override suspend fun availability() = SpeechAvailability.NOT_INSTALLED
    override fun record(sampleRate: Int): Flow<ByteArray> = flow { throw AudioCaptureException(SpeechErrorKind.MICROPHONE_UNAVAILABLE) }
}

object NoAudioOutput : AudioOutput {
    override fun open(sampleRate: Int): PcmSink? = null
}

object Wav {
    /** Wraps 16-bit mono PCM in a RIFF/WAVE header. */
    fun encode(pcm: ByteArray, sampleRate: Int): ByteArray {
        val out = ByteArray(44 + pcm.size)
        fun str(at: Int, s: String) = s.forEachIndexed { i, c -> out[at + i] = c.code.toByte() }
        fun int(at: Int, v: Int) { for (i in 0 until 4) out[at + i] = (v ushr (8 * i)).toByte() }
        fun short(at: Int, v: Int) { out[at] = v.toByte(); out[at + 1] = (v ushr 8).toByte() }
        str(0, "RIFF"); int(4, 36 + pcm.size); str(8, "WAVE")
        str(12, "fmt "); int(16, 16); short(20, 1); short(22, 1); int(24, sampleRate); int(28, sampleRate * 2); short(32, 2); short(34, 16)
        str(36, "data"); int(40, pcm.size)
        pcm.copyInto(out, 44)
        return out
    }

    /** Root-mean-square level of 16-bit PCM in 0..1, for the recording meter and silence checks. */
    fun level(pcm: ByteArray): Float {
        if (pcm.size < 2) return 0f
        var sum = 0.0
        val n = pcm.size / 2
        for (i in 0 until n) {
            val v = ((pcm[2 * i + 1].toInt() shl 8) or (pcm[2 * i].toInt() and 0xFF)).toShort().toInt()
            sum += v.toDouble() * v
        }
        return (kotlin.math.sqrt(sum / n) / 32768.0).toFloat().coerceIn(0f, 1f)
    }

    fun durationMillis(pcmBytes: Int, sampleRate: Int): Long = pcmBytes / 2 * 1000L / sampleRate
}
