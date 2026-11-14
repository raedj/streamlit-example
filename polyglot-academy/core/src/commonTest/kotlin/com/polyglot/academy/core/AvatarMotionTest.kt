package com.polyglot.academy.core

import com.polyglot.academy.core.model.ConversationTurn
import com.polyglot.academy.core.model.Correction
import com.polyglot.academy.core.model.ErrorSeverity
import com.polyglot.academy.core.model.Speaker
import com.polyglot.academy.core.tutor.AvatarMood
import com.polyglot.academy.core.tutor.AvatarMotion
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AvatarMotionTest {
    private fun turn(text: String, speaker: Speaker = Speaker.LEARNER, errors: Int = 0, style: Boolean = false) = ConversationTurn("t", "c", speaker, text,
        corrections = List(errors) { Correction("a", "b", "", ErrorSeverity.MINOR, isStylistic = style) }, createdAtMillis = 0)

    @Test fun moodFollowsTheLearnersLastMessage() {
        assertEquals(AvatarMood.NEUTRAL, AvatarMotion.mood(emptyList()))
        assertEquals(AvatarMood.HAPPY, AvatarMotion.mood(listOf(turn("I like green tea"), turn("Great!", Speaker.TUTOR))))
        assertEquals(AvatarMood.ENCOURAGING, AvatarMotion.mood(listOf(turn("I likes tea", errors = 1))))
        assertEquals(AvatarMood.HAPPY, AvatarMotion.mood(listOf(turn("I would like some tea", errors = 1, style = true))), "style suggestions are not errors")
        assertEquals(AvatarMood.THINKING, AvatarMotion.mood(listOf(turn("Hi there friend")), thinking = true))
    }

    @Test fun motionStaysSubtleAndMouthFollowsLoudness() {
        val poses = (0..100).map { AvatarMotion.pose(listening = it % 2 == 0, speaking = it % 3 == 0, phase = it / 100f) }
        assertTrue(poses.all { abs(it.bob) <= 0.012f && abs(it.tiltDegrees) <= 2.5f })
        assertEquals(0f, AvatarMotion.mouthOpening(0.005f), "noise gate keeps the mouth closed in silence")
        assertTrue(AvatarMotion.mouthOpening(0.05f) < AvatarMotion.mouthOpening(0.12f))
        assertEquals(1f, AvatarMotion.mouthOpening(0.9f))
        val opening = AvatarMotion.smooth(0f, 1f, 16); val closing = 1f - AvatarMotion.smooth(1f, 0f, 16)
        assertTrue(opening > closing, "opens faster than it closes")
        assertTrue(AvatarMotion.nextBlinkDelay(0.0) >= 2_400 && AvatarMotion.nextBlinkDelay(1.0) <= 6_000)
    }
}
