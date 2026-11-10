package com.polyglot.academy.core

import com.polyglot.academy.core.content.Curriculum
import com.polyglot.academy.core.model.Exercise
import com.polyglot.academy.core.model.LanguageCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZhFoundationsTest {
    private val zh = Curriculum.of(LanguageCode.MANDARIN)

    @Test fun theCourseStartsWithHowChineseWorks() {
        assertEquals("zh_u_foundations", zh.units.first().id)
        assertEquals("zh_l_how_chinese_works", zh.units.first().lessonIds.first())
        assertTrue(Curriculum.lesson("zh_l_how_chinese_works")!!.prerequisites.isEmpty())
        // Pinyin comes before the tones lesson, and tone changes before the greetings.
        assertTrue("zh_l_pinyin_finals" in Curriculum.lesson("zh_l_tones_intro")!!.prerequisites)
        assertTrue("zh_l_tone_changes" in Curriculum.lesson("zh_l_greetings")!!.prerequisites)
    }

    @Test fun charactersAreWrittenInEveryLessonWithWords() {
        val withWords = zh.lessons.filter { it.vocabularyIds.isNotEmpty() }
        val missing = withWords.filter { l -> l.exercises.none { it is Exercise.WriteCharacter } }.map { it.id }
        assertTrue(missing.isEmpty(), "no writing exercise in $missing")
        val writing = zh.lessons.flatMap { it.exercises }.filterIsInstance<Exercise.WriteCharacter>()
        assertTrue(writing.size >= 40, "${writing.size} writing exercises")
        assertTrue(writing.all { it.character.length == 1 && it.character in it.word })
    }

    @Test fun theWritingUnitTeachesStrokesThenRadicalsThenWords() {
        assertEquals(listOf("zh_l_strokes", "zh_l_radicals", "zh_l_first_characters"), zh.units.first { it.id == "zh_u_writing" }.lessonIds)
        assertTrue(zh.validate().isEmpty(), zh.validate().joinToString())
    }
}
