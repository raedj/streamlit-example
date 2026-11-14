package com.polyglot.academy.core

import com.polyglot.academy.core.content.Curriculum
import com.polyglot.academy.core.learning.AnswerChecker
import com.polyglot.academy.core.model.Exercise
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.WORD_RECALL_MARK
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LessonTemplateTest {
    private val languages = listOf(LanguageCode.ENGLISH, LanguageCode.MANDARIN, LanguageCode.MALAY)
    private fun lessons(l: LanguageCode) = Curriculum.of(l).lessons

    @Test fun lessonsAskForMoreProductionThanRecognition() = languages.forEach { l ->
        lessons(l).filter { it.vocabularyIds.size >= 4 }.forEach { lesson ->
            val choice = lesson.exercises.count { it is Exercise.MultipleChoice && it.audioText == null && "_x_" !in it.id }
            val recall = lesson.exercises.count { it.id.contains(WORD_RECALL_MARK) }
            assertEquals(4, recall, lesson.id)
            assertTrue(recall > choice, "${lesson.id}: $recall recall, $choice choice")
        }
    }

    @Test fun everyRecallAcceptsItsOwnWord() = languages.forEach { l ->
        lessons(l).flatMap { it.exercises }.filterIsInstance<Exercise.Translate>().filter { WORD_RECALL_MARK in it.id }.forEach { e ->
            assertTrue(AnswerChecker.check(e.acceptedAnswers.first(), e.acceptedAnswers).isCorrect, e.id)
        }
    }

    @Test fun theRuleComesRightBeforeTheHandWrittenExercises() = languages.forEach { l ->
        lessons(l).filter { it.grammarTopicId != null }.forEach { lesson ->
            val notes = lesson.exercises.filterIsInstance<Exercise.GrammarNote>()
            assertEquals(1, notes.size, lesson.id)
            assertEquals(lesson.grammarTopicId, notes.single().topicId, lesson.id)
        }
    }

    @Test fun mandarinGetsNoSentenceSplitFromAnExample() {
        lessons(LanguageCode.MANDARIN).flatMap { it.exercises }.forEach { e ->
            assertFalse("_order_" in e.id || "_dict_" in e.id, e.id)
        }
    }

    @Test fun englishAndMalayLessonsListenToAndOrderASentence() = listOf(LanguageCode.ENGLISH, LanguageCode.MALAY).forEach { l ->
        val generated = lessons(l).flatMap { it.exercises }
        assertTrue(generated.any { "_dict_" in it.id }, "$l dictation")
        assertTrue(generated.any { "_order_" in it.id }, "$l order")
    }

    @Test fun exerciseIdsStayUniqueInEachLesson() = languages.forEach { l ->
        lessons(l).forEach { lesson -> assertEquals(lesson.exercises.size, lesson.exercises.map { it.id }.toSet().size, lesson.id) }
    }
}
