package com.polyglot.academy.core

import com.polyglot.academy.core.content.Curriculum
import com.polyglot.academy.core.mandarin.HandwritingScorer
import com.polyglot.academy.core.mandarin.HandwritingScorer.GRID
import com.polyglot.academy.core.mandarin.HandwritingVerdict
import com.polyglot.academy.core.mandarin.HanziData
import com.polyglot.academy.core.mandarin.InkPoint
import com.polyglot.academy.core.model.LanguageCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HanziTest {
    @Test fun dataCoversMostCourseCharactersWithPlausibleFacts() {
        val courseChars = Curriculum.of(LanguageCode.MANDARIN).vocabulary.flatMap { it.term.toList() }.filter { it in '一'..'鿿' }.toSet()
        val covered = courseChars.count { HanziData.of(it) != null }
        assertTrue(covered >= 70, "only $covered of ${courseChars.size} course characters have data")
        HanziData.entries.values.forEach { e ->
            assertTrue(e.strokes in 1..30, "${e.character} strokes ${e.strokes}")
            e.components.forEach { assertTrue(it.meaning.en.isNotBlank() && it.meaning.ar.isNotBlank()) }
        }
        // Spot checks against standard counts.
        assertEquals(6, HanziData.of('好')!!.strokes)
        assertEquals(12, HanziData.of('谢')!!.strokes)
        assertEquals(listOf("日", "月"), HanziData.of('明')!!.components.map { it.form })
    }

    /** A plus sign (十) drawn into a mask: horizontal and vertical bars through the centre. */
    private fun plusMask(): BooleanArray = BooleanArray(GRID * GRID) { i ->
        val x = i % GRID; val y = i / GRID
        (y in 30..33 && x in 10..53) || (x in 30..33 && y in 10..53)
    }

    private fun line(x0: Float, y0: Float, x1: Float, y1: Float) = (0..10).map { InkPoint(x0 + (x1 - x0) * it / 10, y0 + (y1 - y0) * it / 10) }

    @Test fun matchingShapeScoresWellRegardlessOfSmallOffsets() {
        val r = HandwritingScorer.score(listOf(line(0.17f, 0.5f, 0.84f, 0.5f), line(0.5f, 0.17f, 0.5f, 0.84f)), plusMask(), 2)
        assertEquals(HandwritingVerdict.GOOD, r.verdict, r.toString())
        val shifted = HandwritingScorer.score(listOf(line(0.2f, 0.53f, 0.86f, 0.53f), line(0.53f, 0.2f, 0.53f, 0.86f)), plusMask(), 2)
        assertTrue(shifted.verdict != HandwritingVerdict.TRY_AGAIN, shifted.toString())
    }

    @Test fun partialOrWrongShapesScoreLower() {
        val half = HandwritingScorer.score(listOf(line(0.17f, 0.5f, 0.84f, 0.5f)), plusMask(), 2)
        assertTrue(half.coverage < 0.7 && half.precision > 0.9, half.toString())
        val wrong = HandwritingScorer.score(listOf(line(0.1f, 0.1f, 0.9f, 0.9f), line(0.9f, 0.1f, 0.1f, 0.9f)), plusMask(), 2)
        assertEquals(HandwritingVerdict.TRY_AGAIN, wrong.verdict, wrong.toString())
        assertEquals(HandwritingVerdict.EMPTY, HandwritingScorer.score(emptyList(), plusMask(), 2).verdict)
    }

    @Test fun strokeCountDifferenceIsReportedOnlyWhenLarge() {
        val ok = HandwritingScorer.score(listOf(line(0.17f, 0.5f, 0.84f, 0.5f)), plusMask(), 2)
        assertEquals(null, HandwritingScorer.strokeDifference(ok))
        val many = HandwritingScorer.score(List(5) { line(0.17f, 0.5f, 0.84f, 0.5f) }, plusMask(), 2)
        assertEquals(3, HandwritingScorer.strokeDifference(many))
    }

    @Test fun argbMaskUsesAlpha() {
        val size = 128
        val pixels = IntArray(size * size) { i -> if ((i % size) in 60..67) 0xFF000000.toInt() else 0 }
        val mask = HandwritingScorer.maskFromArgb(pixels, size)
        assertTrue(mask[10 * GRID + 31] && !mask[10 * GRID + 5])
    }
}
