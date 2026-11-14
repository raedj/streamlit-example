package com.polyglot.academy.core

import com.polyglot.academy.core.content.Curriculum
import com.polyglot.academy.core.learning.AnswerAmbiguity
import com.polyglot.academy.core.model.Exercise
import com.polyglot.academy.core.model.Gloss
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AnswerAmbiguityTest {
    @Test fun sharedSensesAreDetectedInEitherLanguage() {
        assertTrue(AnswerAmbiguity.overlaps(Gloss("big", "كبير"), Gloss("large, big", "ضخم")))
        assertTrue(AnswerAmbiguity.overlaps(Gloss("to eat", "يأكل"), Gloss("eat (formal)", "يتناول")))
        assertTrue(AnswerAmbiguity.overlaps(Gloss("house", "بَيْت"), Gloss("home", "بيت")))
        assertFalse(AnswerAmbiguity.overlaps(Gloss("big", "كبير"), Gloss("small", "صغير")))
    }

    @Test fun lessonChoicesHaveNoDuplicateOptions() {
        // A single-answer question must not offer the same option twice (one of them would be "wrong").
        val problems = Curriculum.all.values.flatMap { c -> c.lessons.flatMap { l -> l.exercises.filterIsInstance<Exercise.MultipleChoice>().map { l.id to it } } }
            .mapNotNull { (lesson, e) ->
                val normalized = e.options.map { it.trim().lowercase() }
                when {
                    e.correctIndex !in e.options.indices -> "$lesson/${e.id}: answer index out of range"
                    normalized.toSet().size != normalized.size -> "$lesson/${e.id}: duplicate options ${e.options}"
                    else -> null
                }
            }
        assertEquals(emptyList(), problems)
    }
}
