package com.polyglot.academy.core.tutor

import com.polyglot.academy.core.model.ConversationTurn
import com.polyglot.academy.core.model.Speaker
import kotlin.math.PI
import kotlin.math.sin

/** Facial expression of the drawn tutor avatar. */
enum class AvatarMood { NEUTRAL, HAPPY, ENCOURAGING, THINKING, SURPRISED }

/** Head pose for one animation frame: vertical offset (fraction of the avatar height) and tilt in degrees. */
data class AvatarPose(val bob: Float, val tiltDegrees: Float)

/** Pure animation rules for the avatar, kept out of the drawing code so they can be tested. */
object AvatarMotion {
    /** Expression after the learner's last message: pleased when it was correct, warm and encouraging after corrections. */
    fun mood(turns: List<ConversationTurn>, thinking: Boolean = false): AvatarMood {
        if (thinking) return AvatarMood.THINKING
        val last = turns.lastOrNull { it.speaker == Speaker.LEARNER } ?: return AvatarMood.NEUTRAL
        val errors = last.corrections.count { !it.isStylistic }
        return when {
            errors == 0 && last.text.split(' ').size >= 3 -> AvatarMood.HAPPY
            errors == 0 -> AvatarMood.NEUTRAL
            else -> AvatarMood.ENCOURAGING
        }
    }

    /**
     * Idle breathing, a nod while listening and livelier movement while speaking. [phase] runs
     * 0..1 over one cycle. Amplitudes are small so the portrait never looks unsteady.
     */
    fun pose(listening: Boolean, speaking: Boolean, phase: Float): AvatarPose {
        val a = 2 * PI.toFloat() * phase
        return when {
            listening -> AvatarPose(bob = 0.012f * sin(2 * a).coerceAtLeast(0f), tiltDegrees = 2.5f * sin(a))
            speaking -> AvatarPose(bob = 0.008f * sin(3 * a), tiltDegrees = 1.8f * sin(a))
            else -> AvatarPose(bob = 0.006f * sin(a), tiltDegrees = 0.8f * sin(a))
        }
    }

    /** Mouth opening (0..1) from the loudness of the tutor's audio, with a small noise gate. */
    fun mouthOpening(level: Float): Float = if (level < 0.01f) 0f else ((level - 0.01f) * 6f).coerceIn(0.15f, 1f)

    /** Moves [previous] toward [target]: fast when opening, slower when closing, like a real mouth. */
    fun smooth(previous: Float, target: Float, deltaMillis: Long): Float {
        val rate = if (target > previous) 0.035f else 0.012f
        val k = (rate * deltaMillis).coerceIn(0f, 1f)
        return previous + (target - previous) * k
    }

    /** Milliseconds until the next blink, varied so blinking does not look mechanical. */
    fun nextBlinkDelay(random: Double): Long = (2_400 + random * 3_600).toLong()

    const val BLINK_MILLIS = 130L
}
