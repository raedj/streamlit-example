package com.polyglot.academy.core.arabic

import com.polyglot.academy.core.mandarin.HandwritingScorer
import com.polyglot.academy.core.mandarin.HandwritingVerdict
import com.polyglot.academy.core.mandarin.InkPoint
import kotlin.math.acos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.PI

/** The three things checked in a written letter, kept apart so the learner is told which one is off. */
data class LetterResult(
    val verdict: HandwritingVerdict,
    /** The letter's main shape, compared with the template (dots and signs left out). */
    val body: HandwritingVerdict,
    val coverage: Double,
    val precision: Double,
    val dotsExpected: Int,
    /** Dots counted from the learner's small strokes (a short dash counts as two, a small caret as three). */
    val dotsFound: Int,
    /** False when dots or a sign are missing, extra, or in the wrong place. */
    val marksRight: Boolean,
    /** The main stroke was drawn the opposite way (for example left to right). */
    val reversed: Boolean,
    /** A dot or sign was written before the letter's body (the usual habit is body first). */
    val marksFirst: Boolean,
)

/**
 * Checks an isolated Arabic or Urdu letter written with a finger. Unlike the Chinese shape check,
 * dots are counted and placed explicitly, because ب ت ث (and ج ح خ, ر ز ڑ ژ…) differ only in them;
 * see docs/WRITING_ARABIC_URDU_FEASIBILITY_AR.md. It does not judge calligraphy.
 */
object LetterWriting {
    /** A stroke whose extent is under this share of the letter's size is a dot or a small sign. */
    private const val MARK_SHARE = 0.25f
    /** A mark smaller than this share of the letter is one dot; larger ones are a dash or a caret. */
    private const val DOT_SHARE = 0.08f
    /** Shapes are compared after both are scaled to fill this much of the box, so small letters are judged as strictly as large ones. */
    private const val FILL = 0.8f

    private class Box(val left: Float, val top: Float, val right: Float, val bottom: Float) {
        val width get() = right - left
        val height get() = bottom - top
        val side get() = max(width, height)
        val cx get() = (left + right) / 2
        val cy get() = (top + bottom) / 2
    }

    private fun box(points: List<InkPoint>) = Box(points.minOf { it.x }, points.minOf { it.y }, points.maxOf { it.x }, points.maxOf { it.y })

    /** Splits strokes into body strokes and marks (dots, dashes, small signs). */
    private fun split(strokes: List<List<InkPoint>>): Pair<List<Int>, List<Int>> {
        val size = box(strokes.flatten()).side.coerceAtLeast(1e-3f)
        val marks = strokes.indices.filter { i -> box(strokes[i]).side < MARK_SHARE * size }
        // A letter always has a body: if everything is small, the longest stroke is it.
        val fixed = if (marks.size == strokes.size) marks - strokes.indices.maxBy { box(strokes[it]).side } else marks
        return strokes.indices.filter { it !in fixed } to fixed
    }

    /** Where a mark sits relative to the widest body stroke. */
    private fun position(mark: List<InkPoint>, reference: Box): MarkPosition {
        val y = box(mark).cy
        val h = max(reference.height, reference.side * 0.25f)
        return when {
            y < reference.top + 0.35f * h -> MarkPosition.ABOVE
            y > reference.top + 0.8f * h -> MarkPosition.BELOW
            else -> MarkPosition.INSIDE
        }
    }

    /** Dots in one mark: a tap or tiny stroke is one; a short dash two; a dash that turns back (a caret) three. */
    private fun dotsIn(mark: List<InkPoint>, size: Float): Int {
        if (box(mark).side < DOT_SHARE * size || mark.size < 3) return 1
        val a = mark.first(); val m = mark[mark.size / 2]; val b = mark.last()
        val v1x = m.x - a.x; val v1y = m.y - a.y; val v2x = b.x - m.x; val v2y = b.y - m.y
        val n = hypot(v1x.toDouble(), v1y.toDouble()) * hypot(v2x.toDouble(), v2y.toDouble())
        val turn = if (n == 0.0) 0.0 else acos(((v1x * v2x + v1y * v2y) / n).coerceIn(-1.0, 1.0)) * 180 / PI
        return if (turn > 70) 3 else 2
    }

    /** The strokes scaled uniformly so their box fills [FILL] of the writing box, centred. */
    private fun fill(strokes: List<List<InkPoint>>): List<List<InkPoint>> {
        val b = box(strokes.flatten())
        val scale = if (b.side == 0f) 1f else FILL / b.side
        return strokes.map { s -> s.map { InkPoint(0.5f + (it.x - b.cx) * scale, 0.5f + (it.y - b.cy) * scale) } }
    }

    private const val PATH_POINTS = 48
    /** Mean distance (in box units) between corresponding points of the two paths. */
    private const val PATH_CLOSE = 0.09
    private const val PATH_TRY_AGAIN = 0.14

    /** [n] points spread evenly along the strokes, stroke by stroke. */
    private fun resample(strokes: List<List<InkPoint>>, n: Int): List<InkPoint> {
        val lengths = strokes.map { s -> s.zipWithNext().sumOf { (a, b) -> hypot((b.x - a.x).toDouble(), (b.y - a.y).toDouble()) } }
        val total = lengths.sum().takeIf { it > 0 } ?: return List(n) { strokes.first().first() }
        return buildList {
            strokes.forEachIndexed { i, s ->
                val count = if (i == strokes.lastIndex) n - size else max(1, (n * lengths[i] / total).toInt())
                if (s.size == 1 || lengths[i] == 0.0) repeat(count) { add(s.first()) }
                else for (k in 0 until count) {
                    var target = lengths[i] * k / max(1, count - 1)
                    var j = 0
                    while (j < s.size - 2 && hypot((s[j + 1].x - s[j].x).toDouble(), (s[j + 1].y - s[j].y).toDouble()) < target) {
                        target -= hypot((s[j + 1].x - s[j].x).toDouble(), (s[j + 1].y - s[j].y).toDouble()); j++
                    }
                    val seg = hypot((s[j + 1].x - s[j].x).toDouble(), (s[j + 1].y - s[j].y).toDouble())
                    val f = if (seg == 0.0) 0.0 else (target / seg).coerceIn(0.0, 1.0)
                    add(InkPoint((s[j].x + (s[j + 1].x - s[j].x) * f).toFloat(), (s[j].y + (s[j + 1].y - s[j].y) * f).toFloat()))
                }
            }
        }.take(n)
    }

    /** The smallest mean distance over stroke orders and directions (a body has at most three strokes). */
    private fun pathDistance(written: List<List<InkPoint>>, model: List<List<InkPoint>>): Double {
        val m = resample(model, PATH_POINTS)
        fun dist(order: List<List<InkPoint>>): Double {
            val w = resample(order, PATH_POINTS)
            return w.indices.sumOf { hypot((w[it].x - m[it].x).toDouble(), (w[it].y - m[it].y).toDouble()) } / w.size
        }
        val strokes = written.take(3)
        val orders = if (strokes.size == 1) listOf(strokes) else permutations(strokes)
        return orders.minOf { order ->
            // Every combination of stroke directions.
            (0 until (1 shl order.size)).minOf { mask -> dist(order.mapIndexed { i, s -> if (mask shr i and 1 == 1) s.reversed() else s }) }
        }
    }

    private fun <T> permutations(items: List<T>): List<List<T>> =
        if (items.size <= 1) listOf(items) else items.indices.flatMap { i -> permutations(items.filterIndexed { j, _ -> j != i }).map { listOf(items[i]) + it } }

    private fun widest(strokes: List<List<InkPoint>>) = strokes.maxBy { box(it).width + 0.3f * box(it).height }

    fun check(written: List<List<InkPoint>>, template: LetterTemplate): LetterResult {
        val strokes = written.filter { it.isNotEmpty() }
        val expectedDots = template.dots
        if (strokes.isEmpty()) return LetterResult(HandwritingVerdict.EMPTY, HandwritingVerdict.EMPTY, 0.0, 0.0, expectedDots, 0, false, false, false)
        val (bodyIdx, markIdx) = split(strokes)
        val body = bodyIdx.map { strokes[it] }
        val marks = markIdx.map { strokes[it] }
        val size = box(strokes.flatten()).side

        // Body: both shapes scaled (keeping proportions) to fill the box, then compared within a band.
        val modelBody = fill(template.body)
        val moved = fill(body)
        val target = HandwritingScorer.rasterize(modelBody, radius = 1)
        val area = HandwritingScorer.score(moved, target, null, tolerance = 0.07f)
        // The pen's path in writing order separates outlines that overlap (ف and ب, ق and ن): ف begins
        // with its head. Each stroke may run either way here; direction is reported on its own below.
        val path = pathDistance(moved, modelBody)
        val shape = when {
            path > PATH_TRY_AGAIN -> area.copy(verdict = HandwritingVerdict.TRY_AGAIN)
            path > PATH_CLOSE && area.verdict == HandwritingVerdict.GOOD -> area.copy(verdict = HandwritingVerdict.CLOSE)
            else -> area
        }

        // Direction of the main stroke, compared with the template's first stroke.
        val t0 = modelBody.first()
        val l0 = moved.first()
        val tdx = t0.last().x - t0.first().x; val tdy = t0.last().y - t0.first().y
        val ldx = l0.last().x - l0.first().x; val ldy = l0.last().y - l0.first().y
        val reversed = hypot(tdx.toDouble(), tdy.toDouble()) > 0.25 && hypot(ldx.toDouble(), ldy.toDouble()) > 0.1 && tdx * ldx + tdy * ldy < 0

        // Marks: count and place them relative to the widest body stroke, as in the template.
        val reference = box(widest(body))
        val places = marks.map { position(it, reference) }
        val tReference = box(widest(template.body))
        val tPlaces = template.marks.map { position(it, tReference) }.toSet() + listOfNotNull(template.alsoAt)
        val found = marks.sumOf { dotsIn(it, size) }
        // A small sign (the ط over ٹ, the mark in ك) has a loop or a turn, or a line beside another mark; plain dots do not.
        val signLike = marks.any { dotsIn(it, size) == 3 } || (marks.size >= 2 && marks.any { box(it).side >= DOT_SHARE * size })
        val marksRight = when {
            template.hasSign -> marks.size in 1..3 && signLike && places.all { it in tPlaces }
            expectedDots == 0 -> marks.isEmpty()
            else -> found == expectedDots && places.all { it in tPlaces }
        }
        val marksFirst = markIdx.isNotEmpty() && bodyIdx.isNotEmpty() && markIdx.first() < bodyIdx.first()

        val verdict = when {
            !marksRight || shape.verdict == HandwritingVerdict.TRY_AGAIN -> HandwritingVerdict.TRY_AGAIN
            shape.verdict == HandwritingVerdict.CLOSE || reversed -> HandwritingVerdict.CLOSE
            else -> HandwritingVerdict.GOOD
        }
        return LetterResult(verdict, shape.verdict, shape.coverage, shape.precision, expectedDots, if (template.hasSign) 0 else found, marksRight, reversed, marksFirst)
    }

    /** The model letter as strokes in writing order: body first, then dots or sign. */
    fun model(template: LetterTemplate): List<List<InkPoint>> = template.body + template.marks
}
