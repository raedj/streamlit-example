package com.polyglot.academy.android

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.media.audiofx.AcousticEchoCanceler
import android.media.audiofx.NoiseSuppressor
import androidx.core.content.ContextCompat
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

/** Microphone PCM via AudioRecord, with the platform echo canceller so the tutor's voice is not re-recorded. */
class AndroidAudioCapture(private val context: Context) : AudioCapture {
    override suspend fun availability(): SpeechAvailability = when {
        !context.packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE) -> SpeechAvailability.NOT_INSTALLED
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED -> SpeechAvailability.PERMISSION_REQUIRED
        else -> SpeechAvailability.AVAILABLE
    }

    @SuppressLint("MissingPermission") // checked in availability() and again below
    override fun record(sampleRate: Int): Flow<ByteArray> = flow {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            throw AudioCaptureException(SpeechErrorKind.PERMISSION_DENIED)
        }
        val minBuf = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
        if (minBuf <= 0) throw AudioCaptureException(SpeechErrorKind.MICROPHONE_UNAVAILABLE)
        val chunk = sampleRate / 10 * 2 // 100 ms
        val rec = AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, sampleRate, AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT, maxOf(minBuf, chunk * 4))
        if (rec.state != AudioRecord.STATE_INITIALIZED) { rec.release(); throw AudioCaptureException(SpeechErrorKind.MICROPHONE_UNAVAILABLE) }
        val aec = if (AcousticEchoCanceler.isAvailable()) AcousticEchoCanceler.create(rec.audioSessionId)?.apply { enabled = true } else null
        val ns = if (NoiseSuppressor.isAvailable()) NoiseSuppressor.create(rec.audioSessionId)?.apply { enabled = true } else null
        try {
            rec.startRecording()
            if (rec.recordingState != AudioRecord.RECORDSTATE_RECORDING) throw AudioCaptureException(SpeechErrorKind.RECOGNIZER_BUSY)
            val buf = ByteArray(chunk)
            while (currentCoroutineContext().isActive) {
                val n = rec.read(buf, 0, buf.size)
                if (n < 0) throw AudioCaptureException(SpeechErrorKind.MICROPHONE_UNAVAILABLE)
                if (n > 0) emit(buf.copyOf(n))
            }
        } finally {
            runCatching { rec.stop() }
            aec?.release(); ns?.release()
            rec.release()
        }
    }.flowOn(Dispatchers.IO)
}

/** Streams PCM to an AudioTrack in streaming mode. */
class AndroidAudioOutput : AudioOutput {
    override fun open(sampleRate: Int): PcmSink? {
        val minBuf = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
        if (minBuf <= 0) return null
        val track = AudioTrack.Builder()
            .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build())
            .setAudioFormat(AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(sampleRate).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
            .setBufferSizeInBytes(minBuf * 4)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()
        if (track.state != AudioTrack.STATE_INITIALIZED) { track.release(); return null }
        track.play()
        return object : PcmSink {
            @Volatile private var closed = false
            override fun write(pcm: ByteArray) { if (!closed) track.write(pcm, 0, pcm.size) }
            override fun clear() { if (!closed) { track.pause(); track.flush(); track.play() } }
            override fun close() { if (!closed) { closed = true; runCatching { track.stop() }; track.release() } }
        }
    }
}
