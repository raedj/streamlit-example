package com.polyglot.academy.core

import com.polyglot.academy.core.learning.AnswerChecker
import com.polyglot.academy.core.learning.AnswerVerdict
import com.polyglot.academy.core.util.TextNormalizer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TextAndAnswersTest {
    @Test fun basicNormalisationRemovesPunctuationAndCase() {
        assertEquals("hello my name is sara", TextNormalizer.basic("Hello, my name is Sara!"))
        assertEquals("donde esta la estacion", TextNormalizer.loose("¿Dónde está la estación?"))
    }

    @Test fun arabicDiacriticsAndAlefVariantsAreUnified() {
        assertEquals(TextNormalizer.basic("أنا طالبٌ"), TextNormalizer.basic("انا طالب"))
        assertEquals(TextNormalizer.basic("مستشفى"), TextNormalizer.basic("مستشفي"))
    }

    @Test fun chinesePunctuationIgnored() {
        assertEquals("我是学生", TextNormalizer.basic("我是学生。"))
    }

    @Test fun exactAnswerIsCorrect() {
        val r = AnswerChecker.check("Is", listOf("is"))
        assertEquals(AnswerVerdict.CORRECT, r.verdict)
        assertTrue(r.isCorrect)
    }

    @Test fun missingAccentIsCorrectWithSlip() {
        val r = AnswerChecker.check("esta", listOf("está"))
        assertEquals(AnswerVerdict.CORRECT_WITH_ACCENT_SLIP, r.verdict)
        assertTrue(r.isCorrect)
    }

    @Test fun typoIsAlmostButNotCorrect() {
        val r = AnswerChecker.check("Could we have the bil please", listOf("Could we have the bill please"))
        assertEquals(AnswerVerdict.ALMOST, r.verdict)
        assertTrue(!r.isCorrect)
    }

    @Test fun wrongAndEmptyAnswers() {
        assertEquals(AnswerVerdict.INCORRECT, AnswerChecker.check("dog", listOf("cat")).verdict)
        assertEquals(AnswerVerdict.EMPTY, AnswerChecker.check("  ", listOf("cat")).verdict)
    }

    @Test fun missingWordsForDictation() {
        assertEquals(listOf("straight"), AnswerChecker.missingWords("Go on and turn left", "Go straight on and turn left."))
    }

    @Test fun levenshteinDistance() {
        assertEquals(3, TextNormalizer.levenshtein("kitten", "sitting"))
        assertEquals(1.0, TextNormalizer.similarity("Café", "cafe"))
    }
}
