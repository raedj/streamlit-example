package com.polyglot.academy.core

import com.polyglot.academy.core.content.Curriculum
import com.polyglot.academy.core.model.CefrLevel
import com.polyglot.academy.core.model.CourseUnit
import com.polyglot.academy.core.model.LanguageCode
import kotlin.test.Test
import kotlin.test.assertTrue

class UpperLessonsTest {
    @Test fun englishAndMandarinTeachGrammarAtB1AndB2() {
        listOf(LanguageCode.ENGLISH, LanguageCode.MANDARIN).forEach { l ->
            val c = Curriculum.of(l)
            val general = c.units.filter { it.trackId == CourseUnit.GENERAL_TRACK }.flatMap { it.lessonIds }.toSet()
            listOf(CefrLevel.B1 to 4, CefrLevel.B2 to 3).forEach { (level, min) ->
                val lessons = c.lessons.filter { it.level == level && it.id in general && it.grammarTopicId != null }
                assertTrue(lessons.size >= min, "$l $level: ${lessons.map { it.id }}")
                assertTrue(c.grammar.count { it.level == level } >= min - 1, "$l $level grammar")
            }
            assertTrue(c.validate().isEmpty(), c.validate().joinToString())
        }
    }

    @Test fun newLessonsFollowOnFromTheCourse() {
        listOf("en_l_regrets" to "en_l_reported_speech", "zh_l_not_only" to "zh_l_bei", "zh_l_experiences" to "zh_l_compare").forEach { (lesson, before) ->
            assertTrue(before in Curriculum.lesson(lesson)!!.prerequisites, lesson)
        }
    }
}
