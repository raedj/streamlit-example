package com.polyglot.academy.ui.designsystem

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.polyglot.academy.core.tutor.Accessory
import com.polyglot.academy.core.tutor.AvatarSpec
import com.polyglot.academy.core.tutor.HairStyle
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class AvatarState { IDLE, LISTENING, THINKING, SPEAKING }

/**
 * Original vector tutor portrait (implementation level 2: animated 2D avatar). Drawn from an
 * [AvatarSpec] so every tutor has a distinct look without bitmap assets. Animations stop when
 * reduced motion is enabled; the state is also conveyed by text elsewhere in the UI.
 */
@Composable
fun TutorAvatar(spec: AvatarSpec, description: String, modifier: Modifier = Modifier, size: Dp = 64.dp, state: AvatarState = AvatarState.IDLE) {
    val reduced = PolyglotTheme.motion.reduced
    val animate = !reduced && state != AvatarState.IDLE
    val t = if (animate) rememberInfiniteTransition().animateFloat(0f, 1f, infiniteRepeatable(tween(if (state == AvatarState.SPEAKING) 260 else 900), RepeatMode.Reverse)).value else 0f
    Canvas(modifier.size(size).semantics { contentDescription = description }) {
        val w = this.size.width; val h = this.size.height
        val accent = Color(spec.accentColor)
        // Backdrop disc.
        drawCircle(accent.copy(alpha = 0.18f), radius = w / 2)
        if (state == AvatarState.LISTENING) drawCircle(accent.copy(alpha = 0.35f + 0.3f * t), radius = w / 2 - 1.5f, style = Stroke(w * 0.05f))
        clipPath(Path().apply { addOval(androidx.compose.ui.geometry.Rect(0f, 0f, w, h)) }) {
            // Shoulders / clothing.
            drawOval(accent, topLeft = Offset(w * 0.12f, h * 0.70f), size = Size(w * 0.76f, h * 0.55f))
            if (spec.accessory == Accessory.TIE) {
                val tie = Path().apply { moveTo(w * 0.5f, h * 0.74f); lineTo(w * 0.46f, h * 0.9f); lineTo(w * 0.5f, h); lineTo(w * 0.54f, h * 0.9f); close() }
                drawPath(tie, Color(0xFF1B1B3A))
            }
            if (spec.accessory == Accessory.SCARF) drawRoundRect(Color(0xFFE85D3F), Offset(w * 0.3f, h * 0.68f), Size(w * 0.4f, h * 0.08f), CornerRadius(w * 0.04f))
            // Neck.
            val skin = Color(spec.skinTone)
            drawRect(skin, Offset(w * 0.43f, h * 0.58f), Size(w * 0.14f, h * 0.14f))
            // Hair behind the face for long styles.
            val hair = Color(spec.hairColor)
            when (spec.hairStyle) {
                HairStyle.LONG, HairStyle.WAVY -> drawRoundRect(hair, Offset(w * 0.24f, h * 0.2f), Size(w * 0.52f, h * 0.52f), CornerRadius(w * 0.2f))
                HairStyle.BOB -> drawRoundRect(hair, Offset(w * 0.25f, h * 0.18f), Size(w * 0.5f, h * 0.42f), CornerRadius(w * 0.18f))
                else -> {}
            }
            // Face.
            drawOval(skin, topLeft = Offset(w * 0.3f, h * 0.2f), size = Size(w * 0.4f, h * 0.46f))
            // Hair on top.
            when (spec.hairStyle) {
                HairStyle.SHORT -> drawArc(hair, 180f, 180f, true, Offset(w * 0.29f, h * 0.16f), Size(w * 0.42f, h * 0.26f))
                HairStyle.LONG, HairStyle.BOB -> drawArc(hair, 180f, 180f, true, Offset(w * 0.28f, h * 0.15f), Size(w * 0.44f, h * 0.28f))
                HairStyle.WAVY -> for (i in 0..3) drawCircle(hair, w * 0.09f, Offset(w * (0.34f + i * 0.105f), h * 0.23f))
                HairStyle.CURLY -> for (i in 0..4) drawCircle(hair, w * 0.075f, Offset(w * (0.32f + i * 0.09f), h * 0.2f + (i % 2) * h * 0.02f))
                HairStyle.BUN -> { drawCircle(hair, w * 0.09f, Offset(w * 0.5f, h * 0.12f)); drawArc(hair, 180f, 180f, true, Offset(w * 0.29f, h * 0.16f), Size(w * 0.42f, h * 0.24f)) }
                HairStyle.BALD_BEARD -> drawArc(hair, 0f, 180f, true, Offset(w * 0.32f, h * 0.42f), Size(w * 0.36f, h * 0.24f))
            }
            // Eyes (blink-free; a gentle look-up while thinking).
            val eyeY = h * (if (state == AvatarState.THINKING) 0.40f else 0.42f)
            val eye = Color(0xFF1F2328)
            drawCircle(eye, w * 0.022f, Offset(w * 0.43f, eyeY)); drawCircle(eye, w * 0.022f, Offset(w * 0.57f, eyeY))
            if (spec.accessory == Accessory.GLASSES) {
                val g = Stroke(w * 0.012f)
                drawCircle(Color(0xFF2B2B2B), w * 0.05f, Offset(w * 0.43f, eyeY), style = g)
                drawCircle(Color(0xFF2B2B2B), w * 0.05f, Offset(w * 0.57f, eyeY), style = g)
                drawLine(Color(0xFF2B2B2B), Offset(w * 0.48f, eyeY), Offset(w * 0.52f, eyeY), w * 0.012f)
            }
            if (spec.accessory == Accessory.EARRINGS) {
                drawCircle(Color(0xFFE6B422), w * 0.018f, Offset(w * 0.305f, h * 0.5f)); drawCircle(Color(0xFFE6B422), w * 0.018f, Offset(w * 0.695f, h * 0.5f))
            }
            // Mouth: opens rhythmically while speaking.
            val mouthColor = Color(0xFF8A3B3B)
            if (state == AvatarState.SPEAKING) {
                val open = h * (0.015f + 0.035f * t)
                drawOval(mouthColor, Offset(w * 0.455f, h * 0.53f), Size(w * 0.09f, open))
            } else {
                drawArc(mouthColor, 20f, 140f, false, Offset(w * 0.44f, h * 0.49f), Size(w * 0.12f, h * 0.07f), style = Stroke(w * 0.016f, cap = StrokeCap.Round))
            }
            if (spec.accessory == Accessory.HEADPHONES) {
                val s = Stroke(w * 0.035f, cap = StrokeCap.Round)
                drawArc(Color(0xFF263238), 190f, 160f, false, Offset(w * 0.26f, h * 0.14f), Size(w * 0.48f, h * 0.4f), style = s)
                drawRoundRect(Color(0xFF263238), Offset(w * 0.25f, h * 0.36f), Size(w * 0.07f, h * 0.12f), CornerRadius(w * 0.03f))
                drawRoundRect(Color(0xFF263238), Offset(w * 0.68f, h * 0.36f), Size(w * 0.07f, h * 0.12f), CornerRadius(w * 0.03f))
            }
        }
        if (state == AvatarState.THINKING) {
            for (i in 0..2) drawCircle(accent.copy(alpha = 0.4f + 0.6f * ((t + i / 3f) % 1f)), w * 0.035f, Offset(w * (0.72f + i * 0.08f), h * 0.14f))
        }
    }
}

enum class HeroKind { WELCOME, REVIEW_DONE, EMPTY_LIBRARY, SUCCESS, PRIVACY, OFFLINE, ERROR, MICROPHONE, PLAN, ANALYTICS_EMPTY, RELAX }

/** Original line-and-shape illustrations for onboarding and empty/error states. */
@Composable
fun HeroIllustration(kind: HeroKind, description: String, modifier: Modifier = Modifier, size: Dp = 160.dp) {
    val c = PolyglotTheme.colors
    val measurer = rememberTextMeasurer()
    Canvas(modifier.size(size).semantics { contentDescription = description }) {
        val w = this.size.width; val h = this.size.height
        drawCircle(c.primaryContainer.copy(alpha = if (c.isDark) 0.5f else 0.8f), w * 0.42f, Offset(w / 2, h / 2))
        when (kind) {
            HeroKind.WELCOME -> {
                val bubbles = listOf(Triple("Aa", c.primary, Offset(0.3f, 0.36f)), Triple("ع", c.secondary, Offset(0.68f, 0.32f)),
                    Triple("中", c.accent, Offset(0.36f, 0.68f)), Triple("Ñ", c.chartSeries[3], Offset(0.7f, 0.66f)))
                bubbles.forEach { (txt, col, o) -> speechBubble(Offset(w * o.x, h * o.y), w * 0.15f, col) ; drawLabel(measurer, txt, Offset(w * o.x, h * o.y), (w * 0.09f).toSp(), Color.White) }
            }
            HeroKind.REVIEW_DONE, HeroKind.SUCCESS -> {
                drawStar(Offset(w / 2, h * 0.47f), w * 0.22f, c.accent)
                drawArc(c.success, 200f, 140f, false, Offset(w * 0.22f, h * 0.55f), Size(w * 0.56f, h * 0.3f), style = Stroke(w * 0.03f, cap = StrokeCap.Round))
            }
            HeroKind.EMPTY_LIBRARY -> {
                for (i in 0..2) drawRoundRect(listOf(c.primary, c.secondary, c.accent)[i], Offset(w * (0.3f + i * 0.08f), h * (0.28f + i * 0.05f)), Size(w * 0.3f, h * 0.4f), CornerRadius(w * 0.03f), alpha = 0.85f)
                for (i in 0..3) drawLine(Color.White, Offset(w * 0.5f, h * (0.46f + i * 0.06f)), Offset(w * 0.68f, h * (0.46f + i * 0.06f)), w * 0.012f)
            }
            HeroKind.PRIVACY -> {
                val shield = Path().apply {
                    moveTo(w * 0.5f, h * 0.22f); lineTo(w * 0.72f, h * 0.3f); lineTo(w * 0.7f, h * 0.55f)
                    quadraticTo(w * 0.66f, h * 0.72f, w * 0.5f, h * 0.8f); quadraticTo(w * 0.34f, h * 0.72f, w * 0.3f, h * 0.55f); lineTo(w * 0.28f, h * 0.3f); close()
                }
                drawPath(shield, c.primary)
                drawRoundRect(Color.White, Offset(w * 0.42f, h * 0.48f), Size(w * 0.16f, h * 0.14f), CornerRadius(w * 0.02f))
                drawArc(Color.White, 180f, 180f, false, Offset(w * 0.445f, h * 0.4f), Size(w * 0.11f, h * 0.14f), style = Stroke(w * 0.025f))
            }
            HeroKind.OFFLINE -> {
                drawCircle(c.textMuted, w * 0.1f, Offset(w * 0.42f, h * 0.5f)); drawCircle(c.textMuted, w * 0.13f, Offset(w * 0.56f, h * 0.45f))
                drawRoundRect(c.textMuted, Offset(w * 0.32f, h * 0.48f), Size(w * 0.4f, h * 0.12f), CornerRadius(w * 0.06f))
                drawLine(c.error, Offset(w * 0.28f, h * 0.28f), Offset(w * 0.72f, h * 0.72f), w * 0.035f, StrokeCap.Round)
            }
            HeroKind.ERROR -> {
                drawCircle(c.error, w * 0.2f, Offset(w / 2, h / 2))
                drawLine(Color.White, Offset(w * 0.5f, h * 0.38f), Offset(w * 0.5f, h * 0.53f), w * 0.04f, StrokeCap.Round)
                drawCircle(Color.White, w * 0.025f, Offset(w * 0.5f, h * 0.61f))
            }
            HeroKind.MICROPHONE -> {
                drawRoundRect(c.primary, Offset(w * 0.42f, h * 0.24f), Size(w * 0.16f, h * 0.32f), CornerRadius(w * 0.08f))
                drawArc(c.primary, 0f, 180f, false, Offset(w * 0.34f, h * 0.36f), Size(w * 0.32f, h * 0.28f), style = Stroke(w * 0.03f, cap = StrokeCap.Round))
                drawLine(c.primary, Offset(w * 0.5f, h * 0.64f), Offset(w * 0.5f, h * 0.74f), w * 0.03f)
                for (i in 1..2) drawArc(c.secondary.copy(alpha = 0.6f / i), -40f, 80f, false, Offset(w * (0.5f - 0.1f - i * 0.08f), h * (0.4f - i * 0.08f)), Size(w * (0.2f + i * 0.16f), h * (0.2f + i * 0.16f)), style = Stroke(w * 0.02f, cap = StrokeCap.Round))
            }
            HeroKind.PLAN -> {
                drawRoundRect(c.card, Offset(w * 0.28f, h * 0.24f), Size(w * 0.44f, h * 0.52f), CornerRadius(w * 0.04f))
                drawRoundRect(c.primary, Offset(w * 0.28f, h * 0.24f), Size(w * 0.44f, h * 0.1f), CornerRadius(w * 0.04f))
                for (r in 0..2) for (col in 0..2) drawCircle(if (r * 3 + col < 5) c.success else c.border, w * 0.03f, Offset(w * (0.38f + col * 0.12f), h * (0.45f + r * 0.1f)))
            }
            HeroKind.ANALYTICS_EMPTY -> {
                listOf(0.25f, 0.4f, 0.32f, 0.5f).forEachIndexed { i, v ->
                    drawRoundRect(c.chartSeries[i % c.chartSeries.size].copy(alpha = 0.35f), Offset(w * (0.3f + i * 0.11f), h * (0.72f - v)), Size(w * 0.08f, h * v), CornerRadius(w * 0.02f))
                }
                drawLine(c.textMuted, Offset(w * 0.26f, h * 0.73f), Offset(w * 0.76f, h * 0.73f), w * 0.012f)
            }
            HeroKind.RELAX -> {
                for (i in 0..2) drawCircle(c.primary.copy(alpha = 0.25f + i * 0.2f), w * (0.28f - i * 0.07f), Offset(w / 2, h / 2))
            }
        }
    }
}

private fun DrawScope.speechBubble(center: Offset, r: Float, color: Color) {
    drawCircle(color, r, center)
    val tail = Path().apply { moveTo(center.x - r * 0.3f, center.y + r * 0.8f); lineTo(center.x - r * 0.7f, center.y + r * 1.25f); lineTo(center.x + r * 0.1f, center.y + r * 0.9f); close() }
    drawPath(tail, color)
}

private fun DrawScope.drawStar(center: Offset, r: Float, color: Color) {
    val p = Path()
    for (i in 0 until 10) {
        val rad = if (i % 2 == 0) r else r * 0.45f
        val a = -PI / 2 + i * PI / 5
        val x = center.x + (rad * cos(a)).toFloat(); val y = center.y + (rad * sin(a)).toFloat()
        if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
    }
    p.close(); drawPath(p, color)
}

private fun DrawScope.drawLabel(measurer: androidx.compose.ui.text.TextMeasurer, text: String, center: Offset, size: androidx.compose.ui.unit.TextUnit, color: Color) {
    val layout = measurer.measure(text, TextStyle(fontSize = size, fontWeight = FontWeight.Bold, color = color))
    drawText(layout, topLeft = Offset(center.x - layout.size.width / 2f, center.y - layout.size.height / 2f))
}

/** Achievement medal with a numeric or symbolic glyph. */
@Composable
fun AchievementBadge(glyph: String, unlocked: Boolean, description: String, modifier: Modifier = Modifier, size: Dp = 56.dp) {
    val c = PolyglotTheme.colors
    val measurer = rememberTextMeasurer()
    Canvas(modifier.size(size).semantics { contentDescription = description }) {
        val w = this.size.width
        val fill = if (unlocked) c.accent else c.disabled
        drawCircle(fill.copy(alpha = 0.25f), w / 2)
        drawCircle(fill, w * 0.38f)
        drawCircle(Color.White.copy(alpha = 0.35f), w * 0.38f, style = Stroke(w * 0.04f))
        drawLabel(measurer, glyph, Offset(w / 2, w / 2), (w * 0.3f).toSp(), if (unlocked) c.onAccent else c.onDisabled)
    }
}

