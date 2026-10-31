package com.polyglot.academy.core

import com.polyglot.academy.core.mandarin.HandwritingScorer
import com.polyglot.academy.core.mandarin.HandwritingVerdict
import com.polyglot.academy.core.mandarin.InkPoint
import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Scores simulated handwriting against characters rendered by a real CJK font (as the app does). */
class HandwritingFontTest {
    private val font = Font("Noto Sans CJK SC", Font.PLAIN, 200)

    private fun mask(c: String): BooleanArray {
        assertTrue(font.canDisplay(c[0]), "this test needs a CJK font (Noto Sans CJK SC)")
        val size = 256
        val img = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
        val g = img.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.font = font.deriveFont(size * 0.8f)
        g.color = Color.BLACK
        val fm = g.fontMetrics
        g.drawString(c, (size - fm.stringWidth(c)) / 2, (size - fm.height) / 2 + fm.ascent)
        g.dispose()
        return HandwritingScorer.maskFromArgb(img.getRGB(0, 0, size, size, null, 0, size), size)
    }

    private fun line(x0: Float, y0: Float, x1: Float, y1: Float) = (0..20).map { InkPoint(x0 + (x1 - x0) * it / 20, y0 + (y1 - y0) * it / 20) }
    private val plus = listOf(line(0.15f, 0.47f, 0.85f, 0.47f), line(0.5f, 0.12f, 0.5f, 0.9f))
    private val box = listOf(line(0.25f, 0.3f, 0.25f, 0.75f), line(0.25f, 0.3f, 0.75f, 0.3f) + line(0.75f, 0.3f, 0.75f, 0.75f), line(0.25f, 0.75f, 0.75f, 0.75f))

    @Test fun rightCharacterPassesAndMissingOrWrongStrokesDoNot() {
        assertEquals(HandwritingVerdict.GOOD, HandwritingScorer.score(plus, mask("十"), 2).verdict)
        assertEquals(HandwritingVerdict.GOOD, HandwritingScorer.score(box, mask("口"), 3).verdict)
        // 口 drawn where 日 was asked: the middle stroke is missing.
        assertEquals(HandwritingVerdict.CLOSE, HandwritingScorer.score(box, mask("日"), 4).verdict)
        assertEquals(HandwritingVerdict.TRY_AGAIN, HandwritingScorer.score(plus, mask("好"), 6).verdict)
    }
}
