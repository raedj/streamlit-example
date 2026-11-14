package com.polyglot.academy.ui.designsystem

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.polyglot.academy.core.mandarin.InkPoint
import kotlin.math.sqrt

/** Milliseconds for one stroke of a stroke-order animation. */
const val STROKE_MILLIS = 600

/**
 * Draws stroke medians (0..1 box coordinates): all strokes faintly in [ghost], then the first
 * [progress] strokes in [ink], the stroke in progress only up to its fraction of length.
 */
fun DrawScope.drawStrokeOrder(strokes: List<List<InkPoint>>, progress: Float, ghost: Color, ink: Color) {
    val width = size.minDimension * 0.075f
    val style = Stroke(width = width, cap = StrokeCap.Round, join = StrokeJoin.Round)
    strokes.forEach { drawPath(pathOf(it, 1f), ghost, style = style) }
    strokes.forEachIndexed { i, s ->
        val f = (progress - i).coerceIn(0f, 1f)
        if (f > 0f) drawPath(pathOf(s, f), ink, style = style)
    }
}

private fun DrawScope.pathOf(stroke: List<InkPoint>, fraction: Float): Path {
    val path = Path()
    if (stroke.isEmpty()) return path
    val lengths = stroke.zipWithNext { a, b -> sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y)) }
    var remaining = lengths.sum() * fraction
    path.moveTo(stroke[0].x * size.width, stroke[0].y * size.height)
    for (i in lengths.indices) {
        val a = stroke[i]; val b = stroke[i + 1]
        if (remaining >= lengths[i]) { path.lineTo(b.x * size.width, b.y * size.height); remaining -= lengths[i] }
        else {
            val t = if (lengths[i] == 0f) 0f else remaining / lengths[i]
            path.lineTo((a.x + (b.x - a.x) * t) * size.width, (a.y + (b.y - a.y) * t) * size.height)
            break
        }
    }
    return path
}

/** Practice-grid guide lines (田字格). */
fun DrawScope.drawPracticeGrid(color: Color) {
    val dash = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
    drawLine(color, Offset(size.width / 2, 0f), Offset(size.width / 2, size.height), 1.dp.toPx(), pathEffect = dash)
    drawLine(color, Offset(0f, size.height / 2), Offset(size.width, size.height / 2), 1.dp.toPx(), pathEffect = dash)
}

/**
 * A character's stroke order, animated once when shown and again on each [replay] change.
 * With reduced motion the finished character is shown at once.
 */
@Composable
fun StrokeOrderPlayer(strokes: List<List<InkPoint>>, description: String, replay: Int, modifier: Modifier = Modifier, boxSize: Dp = 120.dp) {
    val c = PolyglotTheme.colors
    val reduced = PolyglotTheme.motion.reduced
    val progress = remember(strokes) { Animatable(0f) }
    LaunchedEffect(strokes, replay, reduced) {
        if (reduced) progress.snapTo(strokes.size.toFloat())
        else {
            progress.snapTo(0f)
            progress.animateTo(strokes.size.toFloat(), tween(strokes.size * STROKE_MILLIS, easing = LinearEasing))
        }
    }
    Canvas(
        modifier.size(boxSize).background(c.surface, RoundedCornerShape(10.dp)).border(1.dp, c.border, RoundedCornerShape(10.dp))
            .semantics { contentDescription = description },
    ) {
        drawPracticeGrid(c.border)
        drawStrokeOrder(strokes, progress.value, c.border, c.primary)
    }
}
