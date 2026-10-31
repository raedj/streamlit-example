package com.polyglot.academy.core

import com.polyglot.academy.core.mandarin.HanziData
import com.polyglot.academy.core.mandarin.StrokeOrder
import com.polyglot.academy.core.mandarin.WritingCharacters
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.wordlist.WordLevels
import com.polyglot.academy.core.wordlist.WordLists
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WritingCharactersTest {
    private val list = WordLists.of(LanguageCode.MANDARIN)

    @Test fun courseSetIsTheHandWrittenCharacters() {
        val course = WritingCharacters.course(traditional = false)
        assertTrue(course.size >= 70, "${course.size}")
        assertTrue(course.all { HanziData.of(it.character) != null && it.character in it.word })
        assertEquals('你', course.first().character)
    }

    @Test fun traditionalCourseSetUsesTraditionalFormsWithBreakdowns() {
        val trad = WritingCharacters.course(traditional = true).map { it.character }
        assertTrue('謝' in trad && '國' in trad && '谢' !in trad)
        // Every traditional form of a course character has its own hand-written entry.
        assertEquals(emptyList(), trad.filter { HanziData.of(it) == null })
        assertTrue(WritingCharacters.course(traditional = true).first { it.character == '謝' }.word == "謝謝")
    }

    @Test fun levelsCoverEveryWordListCharacterOnce() {
        val sets = (1..WordLevels.COUNT).map { WritingCharacters.level(list, it, traditional = false).map { s -> s.character } }
        val all = sets.flatten()
        assertEquals(all.size, all.toSet().size, "a character is offered in two levels")
        val wanted = list.entries.flatMap { it.term.filter { c -> c in '一'..'鿿' }.toList() }.toSet()
        assertEquals(wanted, all.toSet())
        assertTrue(sets.all { it.isNotEmpty() })
        assertEquals('的', sets[0].first())
    }

    @Test fun levelItemsHaveStrokesAndContext() {
        val items = (1..WordLevels.COUNT).flatMap { WritingCharacters.level(list, it, traditional = true) }
        assertTrue(items.all { it.strokes > 0 && StrokeOrder.of(it.character)!!.size == it.strokes && it.character in it.word })
        // 们 becomes 們 in the traditional set; characters that do not change stay as they are.
        val level1 = WritingCharacters.level(list, 1, traditional = true).map { it.character }
        assertTrue('們' in level1 && '们' !in level1 && '我' in level1)
    }

    @Test fun missedCharactersAreFoundWithTheirWords() {
        val items = WritingCharacters.of(listOf('謝', '的', 'A'), list)
        assertEquals(listOf('謝', '的'), items.map { it.character })
        assertEquals("謝謝", items[0].word)
        assertTrue(WritingCharacters.of(emptyList(), list).isEmpty())
    }

    @Test fun practiceStateKeepsPositionsAndMissedCharacters() {
        var st = com.polyglot.academy.core.model.WritingPracticeState()
        st = st.withPosition(3, traditional = true, index = 12)
        assertEquals(12, st.position(3, true))
        assertEquals(0, st.position(3, false))
        st = st.checked('我', right = false).checked('你', right = false).checked('我', right = false)
        assertEquals(listOf("我", "你"), st.missed)
        st = st.checked('你', right = true)
        assertEquals(listOf("我"), st.missed)
        repeat(40) { st = st.checked('一' + it, right = false) }
        assertEquals(com.polyglot.academy.core.model.WritingPracticeState.MAX_MISSED, st.missed.size)
    }
}
