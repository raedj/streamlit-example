package com.polyglot.academy.core

import com.polyglot.academy.core.content.Curriculum
import com.polyglot.academy.core.mandarin.HanziData
import com.polyglot.academy.core.mandarin.InkPoint
import com.polyglot.academy.core.mandarin.StrokeMatch
import com.polyglot.academy.core.mandarin.StrokeOrder
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.wordlist.WordLists
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StrokeOrderTest {
    private fun shrink(strokes: List<List<InkPoint>>, scale: Float, dx: Float, dy: Float) =
        strokes.map { s -> s.map { InkPoint(it.x * scale + dx, it.y * scale + dy) } }

    @Test fun everyCharacterOfTheAppHasStrokeData() {
        val missing = WordLists.of(LanguageCode.MANDARIN).entries.flatMap { e -> (e.term + e.extra["trad"].orEmpty()).filter { it in '一'..'鿿' }.toList() }
            .toSet().filter { StrokeOrder.of(it) == null }
        assertEquals(emptyList(), missing)
        assertNull(StrokeOrder.of('A'))
    }

    @Test fun strokeCountsAgreeWithTheCourseCharacters() {
        // The hand-written stroke counts of the course characters and the Make Me a Hanzi data must agree.
        // This covers the traditional forms too, which are hand-written entries of their own.
        val missing = HanziData.entries.keys.filter { StrokeOrder.of(it) == null }
        assertEquals(emptyList(), missing)
        val disagree = HanziData.entries.values.mapNotNull { h -> StrokeOrder.of(h.character)!!.size.let { n -> if (n != h.strokes) "${h.character} ${h.strokes}/$n" else null } }
        assertEquals(emptyList(), disagree)
    }

    @Test fun pointsAreInsideTheWritingBox() {
        val chars = WordLists.of(LanguageCode.MANDARIN).entries.flatMap { it.term.toList() }.toSet()
        for (c in chars) {
            val strokes = StrokeOrder.of(c) ?: continue
            strokes.flatten().forEach { p -> assertTrue(p.x in -0.05f..1.05f && p.y in -0.05f..1.05f, "$c: $p") }
        }
    }

    @Test fun theExpectedStrokesThemselvesPass() {
        val strokes = assertNotNull(StrokeOrder.of('国'))
        val r = StrokeOrder.check(strokes, strokes)
        assertTrue(r.allCorrect)
        assertEquals(8, r.expected)
    }

    @Test fun aSmallerOffCentreCopyStillPasses() {
        val strokes = assertNotNull(StrokeOrder.of('好'))
        assertTrue(StrokeOrder.check(shrink(strokes, 0.6f, 0.05f, 0.3f), strokes).allCorrect)
    }

    @Test fun reversedSwappedMissingAndExtraStrokesAreReported() {
        val strokes = assertNotNull(StrokeOrder.of('十'))
        assertEquals(listOf(StrokeMatch.REVERSED, StrokeMatch.OK), StrokeOrder.check(listOf(strokes[0].reversed(), strokes[1]), strokes).strokes)
        assertEquals(listOf(StrokeMatch.OUT_OF_ORDER, StrokeMatch.OUT_OF_ORDER), StrokeOrder.check(listOf(strokes[1], strokes[0]), strokes).strokes)
        val missing = StrokeOrder.check(listOf(strokes[0]), strokes)
        assertEquals(listOf(StrokeMatch.OK, StrokeMatch.MISSING), missing.strokes)
        assertEquals(1, missing.firstProblem)
        assertEquals(StrokeMatch.EXTRA, StrokeOrder.check(strokes + listOf(strokes[0]), strokes).strokes.last())
    }

    @Test fun aScribbleIsDifferent() {
        val strokes = assertNotNull(StrokeOrder.of('人'))
        val scribble = listOf(listOf(InkPoint(0.1f, 0.1f), InkPoint(0.12f, 0.9f)), listOf(InkPoint(0.9f, 0.1f), InkPoint(0.88f, 0.12f)))
        assertTrue(StrokeOrder.check(scribble, strokes).strokes.none { it == StrokeMatch.OK })
    }

    @Test fun aStrayExtraStrokeDoesNotSpoilTheOthers() {
        val strokes = assertNotNull(StrokeOrder.of('你'))
        val withStray = listOf(strokes[0].reversed()) + strokes.drop(1) + listOf(listOf(InkPoint(0.45f, 0.99f), InkPoint(0.46f, 1.0f)))
        val r = StrokeOrder.check(withStray, strokes)
        assertEquals(StrokeMatch.REVERSED, r.strokes[0])
        assertEquals(6, r.correct)
        assertEquals(StrokeMatch.EXTRA, r.strokes.last())
    }
}
