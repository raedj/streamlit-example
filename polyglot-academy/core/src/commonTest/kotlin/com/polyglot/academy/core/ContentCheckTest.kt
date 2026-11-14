package com.polyglot.academy.core

import com.polyglot.academy.core.content.ContentCheck
import com.polyglot.academy.core.content.Curriculum
import com.polyglot.academy.core.model.Exercise
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.wordlist.WordLists
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ContentCheckTest {
    private val trio = listOf(LanguageCode.ENGLISH, LanguageCode.MANDARIN, LanguageCode.MALAY)

    @Test fun everyHandWrittenItemIsListedForTheCheckWithItsArabic() {
        trio.forEach { lang ->
            val c = Curriculum.unfilteredOf(lang)
            val items = ContentCheck.items(c).associateBy { it.id }
            c.vocabulary.forEach { assertTrue(it.id in items, "${it.id} missing") }
            c.assessment.forEach { assertTrue(it.question.id in items, "${it.question.id} missing") }
            c.lessons.flatMap { it.exercises }.filter { "_x_" in it.id && it !is Exercise.GrammarNote }.forEach { assertTrue(it.id in items, "${it.id} missing") }
            val word = items.getValue(c.vocabulary.first().id).text
            assertTrue("ar: " in word && c.vocabulary.first().term in word, word)
            assertTrue(items.values.none { '\t' in it.text || '\n' in it.text })
        }
    }

    @Test fun aHiddenWordTakesItsExercisesWithItAndTheLessonsStayValid() {
        trio.forEach { lang ->
            val c = Curriculum.unfilteredOf(lang)
            val lesson = c.lessons.first { it.vocabularyIds.size >= 4 }
            val word = c.vocabularyById.getValue(lesson.vocabularyIds.first())
            val choice = c.lessons.flatMap { it.exercises }.first { "_x_" in it.id }
            val question = c.assessment.first().question
            val hidden = ContentCheck.without(c, setOf(word.id, choice.id, question.id))
            assertFalse(word.id in hidden.vocabularyById)
            val exercises = hidden.lessons.flatMap { it.exercises }
            assertTrue(exercises.none { it.id == choice.id })
            assertTrue(exercises.none { it is Exercise.Flashcard && it.vocabularyId == word.id })
            assertTrue(exercises.none { it.id.endsWith("_${word.id}") })
            assertTrue(exercises.filterIsInstance<Exercise.Matching>().none { m -> m.pairs.any { it.first == word.term } })
            assertTrue(hidden.assessment.none { it.question.id == question.id })
            assertEquals(c.assessment.size - 1, hidden.assessment.size)
            assertEquals(emptyList(), hidden.validate(), "$lang")
            assertTrue(hidden.lessonsById.getValue(lesson.id).exercises.size < lesson.exercises.size)
        }
    }

    @Test fun nothingHiddenLeavesTheCurriculumAsItIs() {
        val c = Curriculum.of(LanguageCode.MALAY)
        assertTrue(ContentCheck.without(c, emptySet()) === c)
        assertEquals(Curriculum.unfilteredOf(LanguageCode.MALAY).vocabulary.size, c.vocabulary.size)
    }

    @Test fun aHiddenListWordOrExampleLeavesTheLevelsInPlace() {
        val list = WordLists.of(LanguageCode.MALAY)
        val word = list.entries[10]
        val withExamples = list.entries.first { it.examples.size >= 2 }
        val hidden = WordLists.without(list, setOf(word.id, "${withExamples.id}#ex1"))
        assertEquals(list.levelCount, hidden.levelCount)
        assertFalse(word.id in hidden.byId)
        assertEquals(withExamples.examples.drop(1), hidden.byId.getValue(withExamples.id).examples)
        assertEquals(list.entries.size - 1, hidden.entries.size)
    }
}
