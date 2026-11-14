package com.polyglot.academy.core.speaking

/**
 * Shadowing: the learner hears a model sentence, says it straight after, and compares the two by ear.
 * Everything here runs on the device; the recording is never sent or stored.
 *
 * Without a speech service the app cannot judge the sounds themselves, so it only checks what it can
 * measure honestly: whether anything was heard, and how long the learner spoke compared with the model.
 */
object Shadowing {
    const val SAMPLE_RATE = 16_000
    const val MAX_SECONDS = 15

    /** A 20 ms frame quieter than this (RMS, 0..1) counts as silence. */
    const val SILENCE_LEVEL = 0.02
    /** Slower than this many times the model reads as "slower"; see [pace]. */
    const val SLOW_RATIO = 1.35
    /** Shorter than this share of the model often means words were left out. */
    const val FAST_RATIO = 0.7

    enum class Pace { NOTHING_HEARD, MUCH_SLOWER, CLOSE, MUCH_FASTER }

    data class Result(
        /** From the first to the last frame with speech, in milliseconds. */
        val voicedMillis: Long,
        /** The loudest frame, 0..1. */
        val peak: Double,
        val pace: Pace,
        /** Learner time / model time, or null when there was no model time to compare with. */
        val ratio: Double?,
    )

    /** RMS level of each 20 ms frame of 16-bit little-endian mono [pcm]. */
    fun frameLevels(pcm: ByteArray, sampleRate: Int = SAMPLE_RATE): DoubleArray {
        val frame = (sampleRate / 50).coerceAtLeast(1)
        val samples = pcm.size / 2
        val frames = samples / frame
        return DoubleArray(frames) { f ->
            var sum = 0.0
            for (i in f * frame until (f + 1) * frame) {
                val v = ((pcm[2 * i + 1].toInt() shl 8) or (pcm[2 * i].toInt() and 0xFF)).toShort().toInt()
                sum += v.toDouble() * v
            }
            kotlin.math.sqrt(sum / frame) / 32768.0
        }
    }

    /** Milliseconds from the first to the last frame with speech: the pauses before and after are not counted. */
    fun voicedMillis(pcm: ByteArray, sampleRate: Int = SAMPLE_RATE): Long {
        val levels = frameLevels(pcm, sampleRate)
        val first = levels.indexOfFirst { it >= SILENCE_LEVEL }
        if (first < 0) return 0
        val last = levels.indexOfLast { it >= SILENCE_LEVEL }
        return (last - first + 1) * 20L
    }

    /**
     * Compares the learner's recording with the model. [modelMillis] is how long the model sentence played,
     * or null when it was not played to the end (then only "was anything heard" is checked).
     */
    fun check(pcm: ByteArray, modelMillis: Long?, sampleRate: Int = SAMPLE_RATE): Result {
        val levels = frameLevels(pcm, sampleRate)
        val peak = levels.maxOrNull() ?: 0.0
        val voiced = voicedMillis(pcm, sampleRate)
        // Under a quarter of a second of sound is a cough or a click, not a sentence.
        if (voiced < 250) return Result(voiced, peak, Pace.NOTHING_HEARD, null)
        val ratio = modelMillis?.takeIf { it > 0 }?.let { voiced.toDouble() / it }
        val pace = when {
            ratio == null -> Pace.CLOSE
            ratio > SLOW_RATIO -> Pace.MUCH_SLOWER
            ratio < FAST_RATIO -> Pace.MUCH_FASTER
            else -> Pace.CLOSE
        }
        return Result(voiced, peak, pace, ratio)
    }
}
