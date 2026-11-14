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
            // English and Mandarin have a first C1 unit (2026-11-10); the other languages stop at B2 at most.
            // If this changes, PR-26 and the in-app text (LEVELS_NOTE) must be updated with it.
            val withC1 = lang in setOf(LanguageCode.ENGLISH, LanguageCode.MANDARIN)
            assertTrue(if (withC1) ceiling == CefrLevel.C1 else ceiling <= CefrLevel.B2, "$lang course ceiling $ceiling")
            assertTrue(ceiling >= CefrLevel.A2, "$lang course ceiling $ceiling")
            val c2 = LevelCoverage.of(lang).single { it.level == CefrLevel.C2 }
            assertTrue(c2.isEmpty, "$lang: nothing is offered at C2")
            val c1 = LevelCoverage.of(lang).single { it.level == CefrLevel.C1 }
            if (withC1) assertTrue(c1.courseLessons >= 4 && c1.texts >= 2, "$lang C1: $c1")
            else assertEquals(0, c1.courseLessons, "$lang has no C1 course")
            assertEquals(!withC1, LevelCoverage.isAboveCourse(lang, CefrLevel.C1))
            assertTrue(LevelCoverage.isAboveCourse(lang, CefrLevel.C2))
            assertFalse(LevelCoverage.isAboveCourse(lang, CefrLevel.A1))
        }
    }

    @Test fun textsGetLongerAndHarderLevelByLevel() {
        // The upper levels used to read like A2: reading sentences must grow with the level,
        // and the C1 texts must be real texts (10+ sentences of 15+ words / 25+ characters on average).
        for (lang in listOf(LanguageCode.ENGLISH, LanguageCode.MANDARIN)) {
            val zh = lang == LanguageCode.MANDARIN
            fun len(s: String) = if (zh) s.count { it in '一'..'鿿' } else s.split(" ").size
            val byLevel = Curriculum.of(lang).readings.groupBy { it.level }.toSortedMap()
            val avg = byLevel.mapValues { (_, rs) -> rs.flatMap { it.sentences }.map(::len).average() }
            avg.values.zipWithNext().forEach { (a, b) -> assertTrue(b > a, "$lang sentence length by level: $avg") }
            val c1 = byLevel.getValue(CefrLevel.C1)
            assertTrue(c1.all { it.sentences.size >= 10 }, "$lang C1 texts are too short")
            assertTrue(avg.getValue(CefrLevel.C1) >= if (zh) 25.0 else 15.0, "$lang C1 sentence length ${avg[CefrLevel.C1]}")
        }
    }
}
