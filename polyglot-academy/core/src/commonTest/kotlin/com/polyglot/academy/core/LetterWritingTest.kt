package com.polyglot.academy.core

import com.polyglot.academy.core.arabic.ArabicLetters
import com.polyglot.academy.core.arabic.LetterTemplate
import com.polyglot.academy.core.arabic.LetterWriting
import com.polyglot.academy.core.mandarin.HandwritingVerdict
import com.polyglot.academy.core.mandarin.InkPoint
import com.polyglot.academy.core.model.LanguageCode
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LetterWritingTest {
    private fun t(c: Char) = ArabicLetters.of(c)!!

    /** A learner's copy: smaller, moved, a little shaky; dots written as taps (one point). */
    private fun hand(strokes: List<List<InkPoint>>, seed: Int, scale: Float = 0.8f, dx: Float = 0.06f, dy: Float = -0.04f): List<List<InkPoint>> {
        val r = Random(seed)
        return strokes.map { s ->
            val moved = s.map { InkPoint(0.5f + (it.x - 0.5f) * scale + dx + (r.nextFloat() - 0.5f) * 0.015f, 0.5f + (it.y - 0.5f) * scale + dy + (r.nextFloat() - 0.5f) * 0.015f) }
            if (s.size <= 2) listOf(moved.first()) else moved
        }
    }
    private fun body(t: LetterTemplate) = hand(t.body, 1)
    private fun full(t: LetterTemplate) = hand(t.body + t.marks, 1)

    @Test fun alphabetsAreComplete() {
        assertEquals(28, ArabicLetters.alphabet(LanguageCode.ARABIC).size)
        assertEquals(38, ArabicLetters.alphabet(LanguageCode.URDU).size)
        assertTrue(ArabicLetters.alphabet(LanguageCode.MANDARIN).isEmpty())
        (ArabicLetters.alphabet(LanguageCode.ARABIC) + ArabicLetters.alphabet(LanguageCode.URDU)).forEach { l ->
            assertEquals(l.dots, if (l.hasSign) 0 else l.marks.size, "${l.letter}: one model mark per dot")
            assertTrue(l.body.all { s -> s.all { it.x in 0f..1f && it.y in 0f..1f } }, "${l.letter} fits the box")
        }
    }

    @Test fun everyLetterWrittenLikeTheModelIsAccepted() {
        val all = (ArabicLetters.alphabet(LanguageCode.ARABIC) + ArabicLetters.alphabet(LanguageCode.URDU)).distinct()
        all.forEach { l ->
            listOf(1, 2, 3).forEach { seed ->
                val r = LetterWriting.check(hand(l.body + l.marks, seed), l)
                assertEquals(HandwritingVerdict.GOOD, r.verdict, "${l.letter} seed $seed: $r")
            }
        }
    }

    @Test fun missingOrWrongDotsAreCaught() {
        val dotted = (ArabicLetters.alphabet(LanguageCode.ARABIC) + ArabicLetters.alphabet(LanguageCode.URDU)).distinct().filter { it.dots > 0 || it.hasSign }
        dotted.forEach { l ->
            val r = LetterWriting.check(body(l), l)
            assertEquals(HandwritingVerdict.TRY_AGAIN, r.verdict, "${l.letter} without its marks")
            assertFalse(r.marksRight)
        }
        // Letters that share a body: writing one where another was asked.
        val groups = listOf("بتثپٹ", "جحخچ", "دذڈ", "رزڑژ", "سش", "صض", "طظ", "عغ", "نں", "يی")
        groups.forEach { g ->
            g.forEach { asked -> g.filter { it != asked }.forEach { drawn ->
                val r = LetterWriting.check(full(t(drawn)), t(asked))
                assertEquals(HandwritingVerdict.TRY_AGAIN, r.verdict, "$drawn written for $asked: $r")
            } }
        }
        // ن with its dot under the bowl.
        val nun = t('ن')
        assertFalse(LetterWriting.check(hand(nun.body, 1) + listOf(listOf(InkPoint(0.5f, 0.95f))), nun).marksRight)
    }

    @Test fun handwrittenDotHabitsAreAccepted() {
        // Two dots as one short dash, three dots as a small caret, the dot of ج under the bowl.
        val ta = t('ت')
        val dash = listOf(InkPoint(0.44f, 0.42f), InkPoint(0.5f, 0.42f), InkPoint(0.56f, 0.42f))
        assertEquals(HandwritingVerdict.GOOD, LetterWriting.check(ta.body + listOf(dash), ta).verdict)
        val tha = t('ث')
        val caret = listOf(InkPoint(0.45f, 0.43f), InkPoint(0.5f, 0.36f), InkPoint(0.55f, 0.43f))
        assertEquals(HandwritingVerdict.GOOD, LetterWriting.check(tha.body + listOf(caret), tha).verdict)
        val jim = t('ج')
        assertEquals(HandwritingVerdict.GOOD, LetterWriting.check(jim.body + listOf(listOf(InkPoint(0.5f, 0.95f))), jim).verdict)
    }

    @Test fun directionAndOrderAreReported() {
        val ba = t('ب')
        val backwards = listOf(ba.body.first().reversed()) + ba.marks
        val r = LetterWriting.check(backwards, ba)
        assertTrue(r.reversed)
        assertEquals(HandwritingVerdict.CLOSE, r.verdict)
        val dotFirst = LetterWriting.check(ba.marks + ba.body, ba)
        assertTrue(dotFirst.marksFirst)
        assertEquals(HandwritingVerdict.GOOD, dotFirst.verdict, "body-first is a habit, not a rule")
    }

    @Test fun differentShapesAreNotAccepted() {
        // Every pair of distinct bodies; written without marks against a template without marks needed.
        val bodies = ArabicLetters.alphabet(LanguageCode.URDU).distinctBy { it.body }
        var accepted = 0; var pairs = 0
        val wrong = mutableListOf<String>()
        bodies.forEach { asked -> bodies.filter { it !== asked }.forEach { drawn ->
            pairs++
            val r = LetterWriting.check(body(drawn), asked.copy(dots = 0, hasSign = false, marks = emptyList(), alsoAt = null))
            if (r.body == HandwritingVerdict.GOOD) { accepted++; wrong += "${drawn.letter}→${asked.letter}" }
        } }
        assertTrue(accepted * 50 <= pairs, "$accepted of $pairs different shapes accepted: $wrong")
        assertEquals(HandwritingVerdict.EMPTY, LetterWriting.check(emptyList(), t('ا')).verdict)
    }
}
