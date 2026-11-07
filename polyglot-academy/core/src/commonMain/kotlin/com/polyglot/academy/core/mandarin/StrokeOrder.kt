package com.polyglot.academy.core.mandarin

import com.polyglot.academy.core.mandarin.generated.GeneratedStrokes
import kotlin.math.max
import kotlin.math.sqrt

/** How one drawn stroke compares with the stroke expected at the same position in the order. */
enum class StrokeMatch { OK, REVERSED, OUT_OF_ORDER, DIFFERENT, MISSING, EXTRA }

data class StrokeOrderResult(val expected: Int, val drawn: Int, val strokes: List<StrokeMatch>) {
    val allCorrect: Boolean get() = strokes.all { it == StrokeMatch.OK }
    /** 0-based index of the first stroke that is not right, or null. */
    val firstProblem: Int? get() = strokes.indexOfFirst { it != StrokeMatch.OK }.takeIf { it >= 0 }
    val correct: Int get() = strokes.count { it == StrokeMatch.OK }
}

/**
 * Stroke order for the app's Chinese characters: the median line of every stroke, in writing
 * order, from Make Me a Hanzi (Arphic Public License, see content/hanzi). Coordinates are
 * normalised to the writing box (0..1, y downwards), the same space as [InkPoint].
 *
 * [check] compares the learner's strokes with the expected ones position by position. It is a
 * tolerance-based comparison of path shape and direction, not handwriting recognition.
 */
object StrokeOrder {
    private const val BOX = 1024f
    private const val SAMPLES = 12
    /** Mean distance (fraction of the box) below which two strokes count as the same stroke. */
    const val TOLERANCE = 0.13f

    private val raw: Map<Char, String> by lazy {
        GeneratedStrokes.rows().associate { row -> row[0] to row.substring(2) }
    }
    private val parsed = HashMap<Char, List<List<InkPoint>>>()

    val characterCount: Int get() = raw.size

    /** The character's strokes in writing order, or null when the character has no stroke data. */
    fun of(c: Char): List<List<InkPoint>>? {
        parsed[c]?.let { return it }
        val strokes = raw[c]?.split(';')?.map { stroke ->
            stroke.split(' ').map { p -> val (x, y) = p.split(','); InkPoint(x.toFloat() / BOX, y.toFloat() / BOX) }
        } ?: return null
        parsed[c] = strokes
        return strokes
    }

    fun check(drawn: List<List<InkPoint>>, expected: List<List<InkPoint>>): StrokeOrderResult {
        val ink = drawn.filter { it.isNotEmpty() }
        // Aligning to the character's extent helps small or off-centre writing, but one stray stroke
        // would distort it, so the drawing as written is compared too and the better result kept.
        val aligned = compare(align(ink, expected), expected)
        val asWritten = compare(ink, expected)
        return if (asWritten.correct > aligned.correct) asWritten else aligned
    }

    private fun compare(ink: List<List<InkPoint>>, expected: List<List<InkPoint>>): StrokeOrderResult {
        val want = expected.map { resample(it) }
        val got = ink.map { resample(it) }
        val matches = (0 until max(want.size, got.size)).map { i ->
            when {
                i >= got.size -> StrokeMatch.MISSING
                i >= want.size -> StrokeMatch.EXTRA
                distance(got[i], want[i]) <= TOLERANCE -> StrokeMatch.OK
                distance(got[i].reversed(), want[i]) <= TOLERANCE -> StrokeMatch.REVERSED
                want.indices.any { j -> j != i && distance(got[i], want[j]) <= TOLERANCE } -> StrokeMatch.OUT_OF_ORDER
                else -> StrokeMatch.DIFFERENT
            }
        }
        return StrokeOrderResult(expected.size, got.size, matches)
    }

    /**
     * Maps the learner's ink onto the character's extent, so a smaller or off-centre drawing can
     * still match. An axis on which either side is nearly flat (for example 一) is only shifted.
     */
    private fun align(drawn: List<List<InkPoint>>, expected: List<List<InkPoint>>): List<List<InkPoint>> {
        val d = drawn.flatten()
        val e = expected.flatten()
        if (d.isEmpty() || e.isEmpty()) return drawn
        fun axis(dv: List<Float>, ev: List<Float>): (Float) -> Float {
            val dMin = dv.min(); val dMax = dv.max(); val eMin = ev.min(); val eMax = ev.max()
            return if (dMax - dMin > 0.15f && eMax - eMin > 0.15f) { v -> eMin + (v - dMin) / (dMax - dMin) * (eMax - eMin) }
            else { v -> v - (dMin + dMax) / 2 + (eMin + eMax) / 2 }
        }
        val fx = axis(d.map { it.x }, e.map { it.x })
        val fy = axis(d.map { it.y }, e.map { it.y })
        return drawn.map { s -> s.map { InkPoint(fx(it.x), fy(it.y)) } }
    }

    /** [SAMPLES] points evenly spaced along the stroke's length. */
    internal fun resample(stroke: List<InkPoint>): List<InkPoint> {
        if (stroke.size == 1) return List(SAMPLES) { stroke[0] }
        val seg = stroke.zipWithNext { a, b -> len(a, b) }
        val total = seg.sum()
        if (total == 0f) return List(SAMPLES) { stroke[0] }
        return List(SAMPLES) { k ->
            var target = total * k / (SAMPLES - 1)
            var i = 0
            while (i < seg.size - 1 && target > seg[i]) { target -= seg[i]; i++ }
            val t = if (seg[i] == 0f) 0f else (target / seg[i]).coerceIn(0f, 1f)
            InkPoint(stroke[i].x + (stroke[i + 1].x - stroke[i].x) * t, stroke[i].y + (stroke[i + 1].y - stroke[i].y) * t)
        }
    }

    private fun distance(a: List<InkPoint>, b: List<InkPoint>): Float = a.zip(b) { p, q -> len(p, q) }.average().toFloat()

    private fun len(a: InkPoint, b: InkPoint) = sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y))
}
