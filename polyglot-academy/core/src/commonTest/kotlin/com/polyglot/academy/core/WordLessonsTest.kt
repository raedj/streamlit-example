package com.polyglot.academy.core

import com.polyglot.academy.core.content.Curriculum
import com.polyglot.academy.core.content.WordLessons
import com.polyglot.academy.core.model.Exercise
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.wordlist.WordLevels
import com.polyglot.academy.core.wordlist.WordLists
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WordLessonsTest {
    @Test fun idsRoundTripAndRejectInvalidOnes() {
        assertEquals(Triple(LanguageCode.ARABIC, 7, 30), WordLessons.parse(WordLessons.id(LanguageCode.ARABIC, 7, 30)))
        assertNull(WordLessons.parse("wl_xx_1_1"))
        assertNull(WordLessons.parse("wl_en_11_1"))
        assertNull(WordLessons.parse("wl_en_1_31"))
        assertNull(WordLessons.parse("en_l_greetings"))
        assertEquals(301..310, WordLessons.ranks(2, 1))
        assertEquals(2991..3000, WordLessons.ranks(10, 30))
    }

    @Test fun everyGeneratedLessonIsWellFormed() {
        for (lang in LanguageCode.entries) {
            val list = WordLists.of(lang)
            val curriculum = Curriculum.of(lang).vocabularyById
            for (level in 1..WordLevels.COUNT) for (number in 1..WordLessons.PER_LEVEL) {
                val lesson = WordLessons.build(list, level, number)
                val where = lesson.id
                assertEquals(WordLessons.PER_LESSON, lesson.vocabularyIds.size, where)
                assertEquals(lesson.exercises.size, lesson.exercises.map { it.id }.toSet().size, "$where: duplicate exercise id")
                assertTrue(lesson.exercises.size >= 18, "$where: only ${lesson.exercises.size} exercises")
                lesson.exercises.forEach { e ->
                    when (e) {
                        is Exercise.Flashcard -> assertTrue(e.vocabularyId in curriculum || e.vocabularyId in list.byId, "$where: ${e.vocabularyId}")
                        is Exercise.MultipleChoice -> {
                            assertEquals(4, e.options.size, "$where ${e.id}")
                            assertEquals(4, e.options.toSet().size, "$where ${e.id}: repeated option")
                            assertTrue(e.correctIndex in e.options.indices, "$where ${e.id}")
                            e.optionGlosses?.let { g -> assertEquals(4, g.map { it.ar }.toSet().size, "$where ${e.id}: repeated Arabic option") }
                        }
                        is Exercise.Matching -> assertTrue(e.pairs.size >= 3, "$where ${e.id}")
                        is Exercise.SentenceOrder -> assertTrue(e.tokens.size in 4..8, "$where ${e.id}")
                        is Exercise.Dictation -> assertTrue(e.audioText.isNotBlank(), "$where ${e.id}")
                        else -> error("$where: unexpected ${e::class.simpleName}")
                    }
                }
            }
        }
    }

    @Test fun generationIsDeterministic() {
        val list = WordLists.of(LanguageCode.TURKISH)
        assertEquals(WordLessons.build(list, 4, 12), WordLessons.build(list, 4, 12))
        assertNotNull(WordLessons.build(list, 4, 12).exercises.firstOrNull { it is Exercise.Dictation })
    }
}
