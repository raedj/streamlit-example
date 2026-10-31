package com.polyglot.academy.core

import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.speech.MouthShape
import com.polyglot.academy.core.speech.Visemes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class VisemesTest {
    @Test fun shapesFollowSpelling() {
        assertEquals(listOf(MouthShape.WIDE, MouthShape.ROUND), Visemes.shapes("hello", LanguageCode.ENGLISH))
        assertEquals(listOf(MouthShape.CLOSED, MouthShape.OPEN, MouthShape.CLOSED, MouthShape.OPEN), Visemes.shapes("mama", LanguageCode.ENGLISH))
        assertEquals(listOf(MouthShape.CLOSED, MouthShape.OPEN), Visemes.shapes("ما", LanguageCode.ARABIC))
        assertEquals(4, Visemes.shapes("你好", LanguageCode.MANDARIN).size)
        assertEquals(MouthShape.REST, Visemes.shapeAt("tsk", LanguageCode.ENGLISH, 0.5f))
        assertEquals(MouthShape.OPEN, Visemes.shapeAt("mama", LanguageCode.ENGLISH, 0.99f))
    }

    @Test fun wordAtUsesTtsCharOffsets() {
        val text = "Where is the station?"
        assertEquals("the", Visemes.wordAt(text, 9))
        assertEquals("station?", Visemes.wordAt(text, 14))
        assertNull(Visemes.wordAt(text, 5))
        assertNull(Visemes.wordAt(text, 99))
    }
}
