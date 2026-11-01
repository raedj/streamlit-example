package com.polyglot.academy.core

import com.polyglot.academy.core.content.Curriculum
import com.polyglot.academy.core.content.LevelCoverage
import com.polyglot.academy.core.model.CefrLevel
import com.polyglot.academy.core.model.LanguageCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LevelCoverageTest {
    @Test fun countsMatchTheContent() {
        LanguageCode.entries.forEach { lang ->
            val offers = LevelCoverage.of(lang)
            assertEquals(Curriculum.of(lang).lessons.count { it.level in LevelCoverage.levels }, offers.sumOf { it.lessons }, "$lang lessons")
            assertEquals(Curriculum.of(lang).readings.count { it.level in LevelCoverage.levels }, offers.sumOf { it.texts }, "$lang texts")
            assertEquals((1..10).toList(), offers.flatMap { it.wordLevels }.sorted(), "$lang word levels")
        }
    }

    @Test fun theStatedScopeHolds() {
        LanguageCode.entries.forEach { lang ->
            val ceiling = LevelCoverage.courseCeiling(lang)
            // If someone adds a C1 course, PR-26 and the in-app text must be updated with it.
            assertTrue(ceiling <= CefrLevel.B2, "$lang course ceiling $ceiling")
            assertTrue(ceiling >= CefrLevel.A2, "$lang course ceiling $ceiling")
            val c2 = LevelCoverage.of(lang).single { it.level == CefrLevel.C2 }
            assertTrue(c2.isEmpty, "$lang: nothing is offered at C2")
            assertEquals(0, LevelCoverage.of(lang).single { it.level == CefrLevel.C1 }.courseLessons, "$lang has no C1 course")
            assertTrue(LevelCoverage.isAboveCourse(lang, CefrLevel.C1))
            assertFalse(LevelCoverage.isAboveCourse(lang, CefrLevel.A1))
        }
    }
}
