package com.polyglot.academy.ui.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin

/**
 * What the learner feels and hears when an answer is checked: a short vibration (Android) and a short
 * sound, each switchable in Settings > Accessibility. Provided once by the app root; previews and tests
 * get the silent default.
 */
@Immutable
class AnswerCues(val haptics: Boolean, val sounds: Boolean, private val play: (ShortArray, Int) -> Unit) {
    fun playSound(correct: Boolean) {
        if (sounds) play(CueTones.pcm(correct), CueTones.SAMPLE_RATE)
    }

    companion object {
        val Silent = AnswerCues(haptics = false, sounds = false) { _, _ -> }
    }
}

val LocalAnswerCues = staticCompositionLocalOf { AnswerCues.Silent }

/**
 * The two cue sounds, synthesised so the app ships no audio files: a rising pair of soft notes for a
 * right answer and one low, short note for a wrong one. Quiet on purpose (under a quarter of full scale),
 * with a fade in and out so they never click.
 */
object CueTones {
    const val SAMPLE_RATE = 22050

    fun pcm(correct: Boolean): ShortArray =
        if (correct) notes(listOf(659.25 to 0.09, 880.0 to 0.16)) else notes(listOf(233.08 to 0.07, 196.0 to 0.18))

    private fun notes(parts: List<Pair<Double, Double>>): ShortArray {
        val out = ArrayList<Short>()
        for ((freq, seconds) in parts) {
            val n = (seconds * SAMPLE_RATE).toInt()
            val fade = (0.012 * SAMPLE_RATE).toInt()
            for (i in 0 until n) {
                val env = min(1.0, min(i.toDouble() / fade, (n - i).toDouble() / (fade * 4)))
                // A little of the octave makes it sound like a chime rather than a test tone.
                val v = sin(2 * PI * freq * i / SAMPLE_RATE) * 0.8 + sin(4 * PI * freq * i / SAMPLE_RATE) * 0.2
                out += (v * env * 0.22 * Short.MAX_VALUE).toInt().toShort()
            }
        }
        return out.toShortArray()
    }
}
