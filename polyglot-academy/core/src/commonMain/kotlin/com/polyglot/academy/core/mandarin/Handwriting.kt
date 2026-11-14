package com.polyglot.academy.core.mandarin

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

/** A point in the writing box, normalised to 0..1 on both axes (0,0 = top left). */
data class InkPoint(val x: Float, val y: Float)

enum class HandwritingVerdict { GOOD, CLOSE, TRY_AGAIN, EMPTY }

data class HandwritingResult(
    /** Share of the character's shape that the learner's ink covers. */
    val coverage: Double,
    /** Share of the learner's ink that lies on the character. */
    val precision: Double,
    val score: Double,
    val strokesDrawn: Int,
    val strokesExpected: Int?,
    val verdict: HandwritingVerdict,
)

/**
 * Compares handwriting with the target character's rendered shape. It measures shape overlap
 * with a tolerance band, not stroke order or direction, and says so in the UI.
 */
object HandwritingScorer {
    const val GRID = 64

    /**
     * @param target row-major [GRID]×[GRID] mask of the character as drawn by the font.
     * @param tolerance band width as a fraction of the box (absorbs small offsets and thin fonts).
     */
    fun score(strokes: List<List<InkPoint>>, target: BooleanArray, strokesExpected: Int?, tolerance: Float = 0.06f): HandwritingResult {
        require(target.size == GRID * GRID) { "target must be ${GRID}x$GRID" }
        val drawn = strokes.filter { it.isNotEmpty() }
        if (drawn.isEmpty() || target.none { it }) return HandwritingResult(0.0, 0.0, 0.0, drawn.size, strokesExpected, HandwritingVerdict.EMPTY)
        val radius = max(1, (tolerance * GRID).toInt())
        val ink = rasterize(drawn, radius = max(1, radius / 2))
        val nearInk = dilate(ink, radius)
        val nearTarget = dilate(target, radius)
        val targetCells = target.count { it }
        val inkCells = ink.count { it }
        val coverage = target.indices.count { target[it] && nearInk[it] }.toDouble() / targetCells
        val precision = ink.indices.count { ink[it] && nearTarget[it] }.toDouble() / inkCells
        val f1 = if (coverage + precision == 0.0) 0.0 else 2 * coverage * precision / (coverage + precision)
        // Coverage is the stricter test: a missing stroke must not pass as "good".
        val verdict = when {
            coverage >= 0.85 && precision >= 0.8 -> HandwritingVerdict.GOOD
            coverage >= 0.6 && precision >= 0.6 -> HandwritingVerdict.CLOSE
            else -> HandwritingVerdict.TRY_AGAIN
        }
        return HandwritingResult(coverage, precision, f1, drawn.size, strokesExpected, verdict)
    }

    fun rasterize(strokes: List<List<InkPoint>>, radius: Int): BooleanArray {
        val grid = BooleanArray(GRID * GRID)
        fun stamp(cx: Float, cy: Float) {
            val x0 = (cx * (GRID - 1)).toInt()
            val y0 = (cy * (GRID - 1)).toInt()
            for (dy in -radius..radius) for (dx in -radius..radius) {
                if (dx * dx + dy * dy > radius * radius) continue
                val x = x0 + dx
                val y = y0 + dy
                if (x in 0 until GRID && y in 0 until GRID) grid[y * GRID + x] = true
            }
        }
        for (stroke in strokes) {
            stroke.zipWithNext().forEach { (a, b) ->
                val steps = max(1, (sqrt(((b.x - a.x) * (b.x - a.x) + (b.y - a.y) * (b.y - a.y)).toDouble()) * GRID * 2).toInt())
                for (i in 0..steps) {
                    val t = i.toFloat() / steps
                    stamp(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t)
                }
            }
            if (stroke.size == 1) stamp(stroke[0].x, stroke[0].y)
        }
        return grid
    }

    private fun dilate(src: BooleanArray, radius: Int): BooleanArray {
        // Two separable passes with a square window: cheap and adequate for a tolerance band.
        val tmp = BooleanArray(src.size)
        for (y in 0 until GRID) for (x in 0 until GRID) {
            var hit = false
            for (k in max(0, x - radius)..minOf(GRID - 1, x + radius)) if (src[y * GRID + k]) { hit = true; break }
            tmp[y * GRID + x] = hit
        }
        val out = BooleanArray(src.size)
        for (x in 0 until GRID) for (y in 0 until GRID) {
            var hit = false
            for (k in max(0, y - radius)..minOf(GRID - 1, y + radius)) if (tmp[k * GRID + x]) { hit = true; break }
            out[y * GRID + x] = hit
        }
        return out
    }

    /** Converts an ARGB pixel buffer (any size, square) to the scoring grid using its alpha channel. */
    fun maskFromArgb(pixels: IntArray, size: Int): BooleanArray {
        val out = BooleanArray(GRID * GRID)
        for (gy in 0 until GRID) for (gx in 0 until GRID) {
            val x0 = gx * size / GRID
            val y0 = gy * size / GRID
            val x1 = max(x0 + 1, (gx + 1) * size / GRID)
            val y1 = max(y0 + 1, (gy + 1) * size / GRID)
            var on = false
            loop@ for (y in y0 until minOf(y1, size)) for (x in x0 until minOf(x1, size)) {
                if ((pixels[y * size + x] ushr 24) > 100) { on = true; break@loop }
            }
            out[gy * GRID + gx] = on
        }
        return out
    }

    /** Drawn minus expected strokes, when the difference is large enough to mention (joined strokes are common). */
    fun strokeDifference(result: HandwritingResult): Int? =
        result.strokesExpected?.let { result.strokesDrawn - it }?.takeIf { abs(it) >= 2 }
}
