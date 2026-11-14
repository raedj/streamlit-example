package com.polyglot.academy.core

import com.polyglot.academy.core.model.CefrLevel
import com.polyglot.academy.core.model.CorrectionIntensity
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.tutor.TutorContext
import com.polyglot.academy.core.tutor.TutorPromptBuilder
import com.polyglot.academy.core.tutor.TutorRoster
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FreeTalkTest {
    private fun prompt(topic: String? = null, scenario: Boolean = false) = TutorPromptBuilder.systemPrompt(TutorContext(
        TutorRoster.tutors.first(), LanguageCode.FRENCH, LanguageCode.ARABIC, CefrLevel.B1, CorrectionIntensity.BALANCED,
        if (scenario) com.polyglot.academy.core.content.Curriculum.of(LanguageCode.FRENCH).scenarios.first() else null, true, topic = topic))

    @Test fun freeConversationFollowsAnyTopicAndAlwaysCorrects() {
        val free = prompt()
        assertTrue("talk about any topic the learner raises" in free, free)
        assertTrue("first report the learner's errors in the corrections field" in free)
        assertTrue("open the session yourself" in free, "the coach opens and welcomes the learner even without a topic")
        val withTopic = prompt(topic = "la cuisine marocaine")
        assertTrue("\"la cuisine marocaine\"" in withTopic && TutorPromptBuilder.START_SIGNAL in withTopic)
        // Role-plays keep their own scope.
        val rolePlay = prompt(scenario = true)
        assertFalse("talk about any topic" in rolePlay)
        assertTrue("first report the learner's errors" in rolePlay)
    }

    @Test fun typedTopicsAreMadeSafeToQuote() {
        assertEquals("football and the World Cup", TutorPromptBuilder.cleanTopic("  football\n\"and\" the | World   Cup "))
        assertEquals(120, TutorPromptBuilder.cleanTopic("x".repeat(500))!!.length)
        assertNull(TutorPromptBuilder.cleanTopic(" \n "))
    }
}
