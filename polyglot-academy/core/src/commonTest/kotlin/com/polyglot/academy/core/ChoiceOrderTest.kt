package com.polyglot.academy.core

import com.polyglot.academy.core.content.Curriculum
import com.polyglot.academy.core.learning.ChoiceOrder
import com.polyglot.academy.core.model.Exercise
import com.polyglot.academy.core.model.LanguageCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChoiceOrderTest {
    @Test fun theOrderIsAPermutationAndStaysTheSameForAQuestion() {
        val order = ChoiceOrder.of("ms_a_a1_1", 3)
        assertEquals(listOf(0, 1, 2), order.sorted())
        assertEquals(order, ChoiceOrder.of("ms_a_a1_1", 3))
    }

    /** Tapping the first option shown must not pass the placement test or the lessons' choice questions. */
    @Test fun alwaysTappingTheFirstOptionIsRightLessThanHalfTheTime() {
        LanguageCode.entries.forEach { code ->
            val c = Curriculum.of(code)
            val choices = c.assessment.map { it.question } + c.lessons.flatMap { it.exercises }.filterIsInstance<Exercise.MultipleChoice>()
            val firstShownIsRight = choices.count { ChoiceOrder.of(it.id, it.options.size).first() == it.correctIndex }
            assertTrue(firstShownIsRight * 2 < choices.size, "$code: the first option shown is right in $firstShownIsRight of ${choices.size}")
            val placement = c.assessment.count { ChoiceOrder.of(it.question.id, it.question.options.size).first() == it.question.correctIndex }
            assertTrue(placement * 2 < c.assessment.size, "$code placement: the first option shown is right in $placement of ${c.assessment.size}")
        }
    }
}
