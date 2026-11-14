package com.polyglot.academy.ui.designsystem

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ChartPoint(val label: String, val value: Double)

/**
 * Bar chart with a zero-based axis (no truncated axes that exaggerate differences), value labels
 * and a full text description for screen readers. Mirrors in RTL so time flows with reading direction.
 */
@Composable
fun BarChart(
    points: List<ChartPoint>,
    description: String,
    modifier: Modifier = Modifier,
    height: Dp = 160.dp,
    color: Color = PolyglotTheme.colors.primary,
    highlightLast: Boolean = true,
    valueFormatter: (Double) -> String = { if (it >= 10 || it == 0.0) it.toInt().toString() else ((it * 10).toInt() / 10.0).toString() },
    /** Text shown when a bar is tapped; defaults to "label: value". */
    detail: (ChartPoint) -> String = { "${it.label}: ${valueFormatter(it.value)}" },
) {
    val c = PolyglotTheme.colors
    val measurer = rememberTextMeasurer()
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val grow by animateFloatAsState(1f, tween(PolyglotTheme.motion.duration(600)))
    val labelStyle = MaterialTheme.typography.labelSmall.copy(color = c.textSecondary)
    val valueStyle = MaterialTheme.typography.labelSmall.copy(color = c.textPrimary)
    var selected by remember(points) { mutableStateOf<Int?>(null) }
    Canvas(modifier.fillMaxWidth().height(height).semantics { contentDescription = description }
        .pointerInput(points, rtl) {
            detectTapGestures { o ->
                if (points.isEmpty()) return@detectTapGestures
                val visual = (o.x / (size.width.toFloat() / points.size)).toInt().coerceIn(0, points.lastIndex)
                val idx = if (rtl) points.lastIndex - visual else visual
                selected = if (selected == idx) null else idx
            }
        }) {
        if (points.isEmpty()) return@Canvas
        val max = (points.maxOf { it.value }).coerceAtLeast(1.0)
        val labelH = 18.dp.toPx(); val valueH = 16.dp.toPx()
        val chartH = size.height - labelH - valueH
        val slot = size.width / points.size
        val barW = (slot * 0.56f).coerceAtMost(36.dp.toPx())
        drawLine(c.divider, Offset(0f, valueH + chartH), Offset(size.width, valueH + chartH), 1.dp.toPx())
        points.forEachIndexed { i, p ->
            val idx = if (rtl) points.lastIndex - i else i
            val cx = slot * idx + slot / 2
            val bh = (chartH * (p.value / max) * grow).toFloat()
            val emphasised = if (selected != null) i == selected else highlightLast && i == points.lastIndex
            val col = if (emphasised) color else color.copy(alpha = 0.55f)
            if (p.value > 0) drawRoundRect(col, Offset(cx - barW / 2, valueH + chartH - bh), Size(barW, bh), CornerRadius(4.dp.toPx()))
            val v = measurer.measure(valueFormatter(p.value), valueStyle)
            drawText(v, topLeft = Offset(cx - v.size.width / 2, (valueH + chartH - bh - v.size.height).coerceAtLeast(0f)))
            val l = measurer.measure(p.label, labelStyle)
            drawText(l, topLeft = Offset(cx - l.size.width / 2, valueH + chartH + 3.dp.toPx()))
        }
        selected?.let { i ->
            val idx = if (rtl) points.lastIndex - i else i
            drawTooltip(measurer.measure(detail(points[i]), valueStyle), slot * idx + slot / 2, c.surfaceElevated, c.border)
        }
    }
}

/** A small labelled box centred on [x] at the top of the chart, kept inside the bounds. */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTooltip(text: androidx.compose.ui.text.TextLayoutResult, x: Float, fill: Color, border: Color) {
    val pad = 6.dp.toPx()
    val w = text.size.width + pad * 2
    val h = text.size.height + pad * 2
    val left = (x - w / 2).coerceIn(0f, (size.width - w).coerceAtLeast(0f))
    drawRoundRect(fill, Offset(left, 0f), Size(w, h), CornerRadius(6.dp.toPx()))
    drawRoundRect(border, Offset(left, 0f), Size(w, h), CornerRadius(6.dp.toPx()), style = Stroke(1.dp.toPx()))
    drawText(text, topLeft = Offset(left + pad, pad))
}

/** Line chart for 0..1 metrics (accuracy). Y axis is fixed at 0–100% so trends are not exaggerated. */
@Composable
fun LineChart(
    points: List<ChartPoint>, description: String, modifier: Modifier = Modifier, height: Dp = 150.dp, color: Color = PolyglotTheme.colors.secondary,
    detail: (ChartPoint) -> String = { "${it.label}: ${(it.value * 100).toInt()}%" },
) {
    val c = PolyglotTheme.colors
    val measurer = rememberTextMeasurer()
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val labelStyle = MaterialTheme.typography.labelSmall.copy(color = c.textSecondary)
    val valueStyle = MaterialTheme.typography.labelSmall.copy(color = c.textPrimary)
    var selected by remember(points) { mutableStateOf<Int?>(null) }
    Canvas(modifier.fillMaxWidth().height(height).semantics { contentDescription = description }
        .pointerInput(points, rtl) {
            detectTapGestures { o ->
                if (points.isEmpty()) return@detectTapGestures
                val axisW = 34.dp.toPx()
                val left = if (rtl) 0f else axisW
                val chartW = size.width - axisW
                val step = if (points.size == 1) 0f else chartW / (points.size - 1)
                val idx = if (step == 0f) 0 else {
                    val visual = ((o.x - left) / step).let { kotlin.math.round(it).toInt() }.coerceIn(0, points.lastIndex)
                    if (rtl) points.lastIndex - visual else visual
                }
                selected = if (selected == idx) null else idx
            }
        }) {
        val labelH = 18.dp.toPx(); val axisW = 34.dp.toPx()
        val chartH = size.height - labelH
        val left = if (rtl) 0f else axisW
        val chartW = size.width - axisW
        listOf(0.0, 0.5, 1.0).forEach { g ->
            val y = (chartH - chartH * g).toFloat().coerceIn(6f, chartH)
            drawLine(c.divider, Offset(left, y), Offset(left + chartW, y), 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)))
            val t = measurer.measure("${(g * 100).toInt()}%", labelStyle)
            drawText(t, topLeft = Offset(if (rtl) size.width - t.size.width else 0f, y - t.size.height / 2))
        }
        if (points.isEmpty()) return@Canvas
        val step = if (points.size == 1) 0f else chartW / (points.size - 1)
        fun x(i: Int) = if (rtl) left + chartW - step * i - (if (points.size == 1) chartW / 2 else 0f) else left + step * i + (if (points.size == 1) chartW / 2 else 0f)
        fun y(v: Double) = (chartH - chartH * v.coerceIn(0.0, 1.0)).toFloat().coerceIn(4f, chartH - 2f)
        val path = Path()
        points.forEachIndexed { i, p -> if (i == 0) path.moveTo(x(i), y(p.value)) else path.lineTo(x(i), y(p.value)) }
        drawPath(path, color, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
        points.forEachIndexed { i, p ->
            drawCircle(color, if (i == selected) 7.dp.toPx() else 4.dp.toPx(), Offset(x(i), y(p.value)))
            val l = measurer.measure(p.label, labelStyle)
            drawText(l, topLeft = Offset((x(i) - l.size.width / 2).coerceIn(0f, size.width - l.size.width), chartH + 3.dp.toPx()))
        }
        selected?.let { i ->
            drawLine(c.border, Offset(x(i), 0f), Offset(x(i), chartH), 1.dp.toPx())
            drawTooltip(measurer.measure(detail(points[i]), valueStyle), x(i), c.surfaceElevated, c.border)
        }
    }
}

/** Study-consistency heatmap: one cell per day, intensity by minutes. Legend explains the scale in text. */
@Composable
fun CalendarHeatmap(values: List<ChartPoint>, description: String, modifier: Modifier = Modifier, columns: Int = 7, cell: Dp = 28.dp) {
    val c = PolyglotTheme.colors
    val max = values.maxOfOrNull { it.value }?.coerceAtLeast(1.0) ?: 1.0
    Column(modifier.semantics(mergeDescendants = true) { contentDescription = description }, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        values.chunked(columns).forEach { week ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                week.forEach { d ->
                    val intensity = (d.value / max).toFloat()
                    Box(Modifier.size(cell).clip(RoundedCornerShape(6.dp)).background(if (d.value <= 0) c.progressTrack else c.primary.copy(alpha = 0.25f + 0.75f * intensity)),
                        contentAlignment = Alignment.Center) {
                        Text(d.label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = if (intensity > 0.6f) c.onPrimary else c.textSecondary)
                    }
                }
            }
        }
    }
}

data class Segment(val label: String, val value: Int, val color: Color)

/** Horizontal stacked bar with a text legend including the numbers. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StackedBar(segments: List<Segment>, description: String, modifier: Modifier = Modifier) {
    val c = PolyglotTheme.colors
    val total = segments.sumOf { it.value }.coerceAtLeast(1)
    Column(modifier.fillMaxWidth().semantics(mergeDescendants = true) { contentDescription = description }, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth().height(16.dp).clip(RoundedCornerShape(8.dp)).background(c.progressTrack)) {
            segments.filter { it.value > 0 }.forEach { s -> Box(Modifier.weight(s.value.toFloat() / total).height(16.dp).background(s.color)) }
            val rest = 1f - segments.sumOf { it.value }.toFloat() / total
            if (rest > 0.001f) Spacer(Modifier.weight(rest))
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            segments.forEach { s ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(10.dp).clip(RoundedCornerShape(3.dp)).background(s.color))
                    Spacer(Modifier.width(6.dp))
                    Text("${s.label}: ${s.value}", style = MaterialTheme.typography.labelMedium, color = c.textSecondary)
                }
            }
        }
    }
}

/** Horizontal skill bars with numeric percentages (used instead of a radar chart for readability). */
@Composable
fun SkillBars(items: List<Triple<String, Float?, String>>, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items.forEach { (label, value, valueText) ->
            LabeledProgress(label, valueText, value ?: 0f, color = if (value == null) PolyglotTheme.colors.border else PolyglotTheme.colors.primary)
        }
    }
}

