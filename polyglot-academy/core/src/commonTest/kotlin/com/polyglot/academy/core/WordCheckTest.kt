package com.polyglot.academy.core

import com.polyglot.academy.core.assessment.WordCheck
import com.polyglot.academy.core.model.AssessmentKind
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.wordlist.WordLevels
import com.polyglot.academy.core.wordlist.WordLists
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class WordCheckTest {
    private val languages = listOf(LanguageCode.ENGLISH, LanguageCode.MANDARIN, LanguageCode.MALAY)

    @Test fun samplesEvenlyFromEveryLevel() = languages.forEach { l ->
        val words = WordCheck.sample(WordLists.of(l), 5)
        assertEquals(WordCheck.SIZE, words.size, "$l")
        assertEquals(WordCheck.SIZE, words.map { it.id }.toSet().size, "$l")
        (1..WordLevels.COUNT).forEach { level -> assertEquals(WordCheck.PER_LEVEL, words.count { WordLevels.levelOf(it.rank) == level }, "$l $level") }
    }

    @Test fun anotherDayMeetsOtherWords() {
        val list = WordLists.of(LanguageCode.ENGLISH)
        assertEquals(WordCheck.sample(list, 5), WordCheck.sample(list, 5))
        assertNotEquals(WordCheck.sample(list, 5), WordCheck.sample(list, 6))
    }

    @Test fun scoreSuggestsARoughWordCountAndIsNotAPlacement() {
        assertEquals(0, WordCheck.estimatedWords(0))
        assertEquals(WordLevels.TARGET, WordCheck.estimatedWords(WordCheck.SIZE))
        val words = WordCheck.sample(WordLists.of(LanguageCode.MALAY), 5)
        val r = WordCheck.result("id", LanguageCode.MALAY, 1L, words.mapIndexed { i, w -> w to (i < 3) })
        assertEquals(AssessmentKind.PROGRESS_CHECK, r.kind)
        assertEquals(3, r.correctAnswers); assertEquals(WordCheck.SIZE, r.questionsAnswered)
    }
}
