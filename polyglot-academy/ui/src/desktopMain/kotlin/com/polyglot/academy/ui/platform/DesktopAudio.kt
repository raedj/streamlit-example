package com.polyglot.academy.ui.platform

import com.polyglot.academy.core.speech.AudioCapture
import com.polyglot.academy.core.speech.AudioCaptureException
import com.polyglot.academy.core.speech.AudioOutput
import com.polyglot.academy.core.speech.PcmSink
import com.polyglot.academy.core.speech.SpeechAvailability
import com.polyglot.academy.core.speech.SpeechErrorKind
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.SourceDataLine
import javax.sound.sampled.TargetDataLine

private fun format(rate: Int) = AudioFormat(rate.toFloat(), 16, 1, true, false)

/** Microphone capture with javax.sound (no echo cancellation: use headphones for realtime voice). */
object DesktopAudioCapture : AudioCapture {
    override suspend fun availability(): SpeechAvailability =
        if (runCatching { AudioSystem.isLineSupported(DataLine.Info(TargetDataLine::class.java, format(16_000))) }.getOrDefault(false)) SpeechAvailability.AVAILABLE
        else SpeechAvailability.NOT_INSTALLED

    override fun record(sampleRate: Int): Flow<ByteArray> = flow {
        val line = runCatching { AudioSystem.getTargetDataLine(format(sampleRate)) }.getOrNull() ?: throw AudioCaptureException(SpeechErrorKind.MICROPHONE_UNAVAILABLE)
        try {
            line.open(format(sampleRate)); line.start()
            val buf = ByteArray(sampleRate / 10 * 2)
            while (currentCoroutineContext().isActive) {
                val n = line.read(buf, 0, buf.size)
                if (n > 0) emit(buf.copyOf(n))
            }
        } catch (e: javax.sound.sampled.LineUnavailableException) {
            throw AudioCaptureException(SpeechErrorKind.RECOGNIZER_BUSY)
        } finally {
            line.stop(); line.close()
        }
    }.flowOn(Dispatchers.IO)
}

object DesktopAudioOutput : AudioOutput {
    override fun open(sampleRate: Int): PcmSink? {
        val line: SourceDataLine = runCatching { AudioSystem.getSourceDataLine(format(sampleRate)).apply { open(format(sampleRate)); start() } }.getOrNull() ?: return null
        return object : PcmSink {
            @Volatile private var closed = false
            override fun write(pcm: ByteArray) { if (!closed) line.write(pcm, 0, pcm.size) }
            override fun clear() { if (!closed) line.flush() }
            override fun close() { if (!closed) { closed = true; line.stop(); line.close() } }
        }
    }
}
