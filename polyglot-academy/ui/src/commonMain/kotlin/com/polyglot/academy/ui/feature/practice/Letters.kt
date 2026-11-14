package com.polyglot.academy.ui.feature.practice

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.polyglot.academy.core.arabic.ArabicLetters
import com.polyglot.academy.core.arabic.LetterResult
import com.polyglot.academy.core.arabic.LetterTemplate
import com.polyglot.academy.core.arabic.LetterWriting
import com.polyglot.academy.core.arabic.MarkPosition
import com.polyglot.academy.core.mandarin.HandwritingVerdict
import com.polyglot.academy.core.mandarin.InkPoint
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.SessionKind
import com.polyglot.academy.ui.app.LocalApp
import com.polyglot.academy.ui.app.ScrollScreen
import com.polyglot.academy.ui.designsystem.Banner
import com.polyglot.academy.ui.designsystem.BannerKind
import com.polyglot.academy.ui.designsystem.ChipGroup
import com.polyglot.academy.ui.designsystem.PolyCard
import com.polyglot.academy.ui.designsystem.PolyglotTheme
import com.polyglot.academy.ui.designsystem.PrimaryButton
import com.polyglot.academy.ui.designsystem.STROKE_MILLIS
import com.polyglot.academy.ui.designsystem.SecondaryButton
import com.polyglot.academy.ui.designsystem.SectionHeader
import com.polyglot.academy.ui.designsystem.SwitchRow
import com.polyglot.academy.ui.designsystem.drawStrokeOrder
import com.polyglot.academy.ui.i18n.K
import com.polyglot.academy.ui.i18n.Labels
import com.polyglot.academy.ui.i18n.LocalStrings
import com.polyglot.academy.ui.i18n.Localizer
import kotlinx.coroutines.launch

/**
 * Writing the isolated Arabic or Urdu letters (Naskh). The checker looks at the shape, the dots and
 * the direction separately; the templates are original and not yet reviewed by a teacher.
 */
@Composable
fun LettersScreen() {
    val app = LocalApp.current
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val scope = rememberCoroutineScope()
    val active = app.profile.activeLearningLanguage
    // Arabic learners practise the Arabic alphabet; anyone else who opens it from a link sees the Urdu set only when learning Urdu.
    val language = if (ArabicLetters.supports(active)) active else LanguageCode.ARABIC
    val letters = remember(language) { ArabicLetters.alphabet(language) }
    var index by remember(language) { mutableIntStateOf((app.settings.letterPositions[language] ?: 0).coerceIn(0, letters.lastIndex)) }
    LaunchedEffect(language, index) {
        app.services.store.updateSettings { it.copy(letterPositions = it.letterPositions + (language to index)) }
    }
    val letter = letters[index]
    var showGuide by remember { mutableStateOf(true) }
    val strokes = remember(index) { mutableStateListOf<List<InkPoint>>() }
    var current by remember(index) { mutableStateOf<List<InkPoint>>(emptyList()) }
    var result by remember(index) { mutableStateOf<LetterResult?>(null) }
    val demo = remember(index) { androidx.compose.animation.core.Animatable(0f) }
    var showDemo by remember(index) { mutableStateOf(false) }
    val reduced = PolyglotTheme.motion.reduced
    val practice = remember(language) { app.services.study.practice(language, SessionKind.WRITING, "letters") }
    var checked by remember { mutableIntStateOf(0) }
    var passed by remember { mutableIntStateOf(0) }
    val model = remember(letter) { LetterWriting.model(letter) }

    ScrollScreen(s[K.LETTERS_TITLE]) {
        if (!ArabicLetters.supports(active)) Banner(BannerKind.INFO, s[K.LETTERS_NOT_ACTIVE])
        Text(s.f(K.LETTERS_BODY, Labels.language(s, language)), style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
        Banner(BannerKind.INFO, s[K.LETTERS_UNREVIEWED])
        PolyCard {
            Text(s.f(K.LETTERS_POSITION, index + 1, letters.size), style = MaterialTheme.typography.labelMedium, color = c.textSecondary)
            // Naskh, as the templates are, even for Urdu (whose reading texts use Nastaliq).
            Text(letter.letter.toString(), style = MaterialTheme.typography.displayMedium.copy(fontFamily = FontFamily.Default))
            Text(s.gloss(letter.name), style = MaterialTheme.typography.titleSmall)
            Text(marksText(s, letter), style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
            SecondaryButton(s[K.LETTERS_SHOW_HOW], {
                showDemo = true
                scope.launch {
                    if (reduced) demo.snapTo(model.size.toFloat())
                    else { demo.snapTo(0f); demo.animateTo(model.size.toFloat(), androidx.compose.animation.core.tween(model.size * STROKE_MILLIS, easing = androidx.compose.animation.core.LinearEasing)) }
                }
            }, Modifier.fillMaxWidth())
        }
        PolyCard {
            Text(s[K.LETTERS_PICK], style = MaterialTheme.typography.titleSmall)
            ChipGroup(letters.indices.toList(), setOf(index), { letters[it].letter.toString() }, { index = it })
        }
        PolyCard(contentPadding = 0.dp) { SwitchRow(s[K.CHARS_GUIDE], showGuide, { showGuide = it }, subtitle = s[K.CHARS_GUIDE_SUB]) }

        val ink = c.textPrimary
        val guide = c.border
        Canvas(
            Modifier.fillMaxWidth().widthIn(max = 360.dp).aspectRatio(1f).background(c.surface, RoundedCornerShape(12.dp))
                .border(1.dp, c.border, RoundedCornerShape(12.dp))
                .semantics { contentDescription = s.f(K.LETTERS_BOX_DESC, letter.letter.toString(), strokes.size) }
                // A tap is a stroke too: dots are often just tapped.
                .pointerInput(index) {
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        result = null; showDemo = false
                        current = listOf(InkPoint((down.position.x / size.width).coerceIn(0f, 1f), (down.position.y / size.height).coerceIn(0f, 1f)))
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (!change.pressed) break
                            if (change.positionChange() != Offset.Zero) {
                                current = current + InkPoint((change.position.x / size.width).coerceIn(0f, 1f), (change.position.y / size.height).coerceIn(0f, 1f))
                                change.consume()
                            }
                        }
                        strokes += current
                        current = emptyList()
                    }
                },
        ) {
            // The line the letter sits on.
            drawLine(guide, Offset(0f, size.height * 0.62f), Offset(size.width, size.height * 0.62f), strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)))
            if (showDemo) drawStrokeOrder(model, demo.value, guide.copy(alpha = 0.7f), c.primary)
            else if (showGuide) drawStrokeOrder(model, 0f, guide.copy(alpha = 0.55f), guide)
            (strokes + listOf(current)).filter { it.isNotEmpty() }.forEach { stroke ->
                if (stroke.size == 1) drawCircle(ink, size.minDimension * 0.03f, Offset(stroke[0].x * size.width, stroke[0].y * size.height))
                else {
                    val path = Path()
                    stroke.forEachIndexed { i, p -> if (i == 0) path.moveTo(p.x * size.width, p.y * size.height) else path.lineTo(p.x * size.width, p.y * size.height) }
                    drawPath(path, ink, style = Stroke(width = size.minDimension * 0.045f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SecondaryButton(s[K.CHARS_UNDO], { strokes.removeLastOrNull(); result = null }, Modifier.weight(1f), enabled = strokes.isNotEmpty())
            SecondaryButton(s[K.CHARS_CLEAR], { strokes.clear(); result = null }, Modifier.weight(1f), enabled = strokes.isNotEmpty())
        }
        PrimaryButton(s[K.CHARS_CHECK], {
            val r = LetterWriting.check(strokes.toList(), letter)
            result = r
            if (r.verdict != HandwritingVerdict.EMPTY) {
                checked++; if (r.verdict == HandwritingVerdict.GOOD) passed++
                scope.launch { practice.answer(r.verdict == HandwritingVerdict.GOOD) }
            }
        }, Modifier.fillMaxWidth(), enabled = strokes.isNotEmpty())

        result?.let { r ->
            val (kind, text) = when (r.verdict) {
                HandwritingVerdict.GOOD -> BannerKind.SUCCESS to s[K.LETTERS_GOOD]
                HandwritingVerdict.CLOSE -> BannerKind.INFO to s[K.LETTERS_CLOSE]
                HandwritingVerdict.TRY_AGAIN -> BannerKind.WARNING to s[K.LETTERS_TRY_AGAIN]
                HandwritingVerdict.EMPTY -> BannerKind.INFO to s[K.CHARS_EMPTY]
            }
            Banner(kind, text)
            if (r.verdict != HandwritingVerdict.EMPTY) feedback(s, letter, r).forEach {
                Text("• $it", style = MaterialTheme.typography.bodyMedium)
            }
        }
        if (checked > 0) Text(s.f(K.TONES_SCORE, passed, checked), style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SecondaryButton(s[K.LETTERS_PREVIOUS], { index-- }, Modifier.weight(1f), enabled = index > 0)
            PrimaryButton(s[K.NEXT], { index = (index + 1) % letters.size }, Modifier.weight(1f))
        }
        SectionHeader(s[K.CHARS_ABOUT_TITLE])
        Text(s[K.LETTERS_ABOUT], style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
    }
}

private fun place(s: Localizer, p: MarkPosition) = s[when (p) { MarkPosition.ABOVE -> K.LETTERS_ABOVE; MarkPosition.BELOW -> K.LETTERS_BELOW; MarkPosition.INSIDE -> K.LETTERS_INSIDE }]

/** Where the model's dots or sign are, for the description and the feedback. */
private fun modelPlace(letter: LetterTemplate): MarkPosition? {
    val y = letter.marks.flatten().map { it.y }.average().takeIf { letter.marks.isNotEmpty() } ?: return null
    val body = letter.body.maxBy { s -> s.maxOf { it.x } - s.minOf { it.x } }
    val top = body.minOf { it.y }; val h = maxOf(body.maxOf { it.y } - top, 0.15f)
    return when { y < top + 0.35 * h -> MarkPosition.ABOVE; y > top + 0.8 * h -> MarkPosition.BELOW; else -> MarkPosition.INSIDE }
}

private fun marksText(s: Localizer, letter: LetterTemplate): String {
    val p = modelPlace(letter)
    return when {
        letter.hasSign && p != null -> s.f(if (letter.letter == 'ك') K.LETTERS_HAS_MARK else K.LETTERS_HAS_SMALL_TA, place(s, p))
        letter.dots > 0 && p != null -> s.f(K.LETTERS_HAS_DOTS, s.plural(K.LETTERS_DOTS_N, letter.dots), place(s, p))
        else -> s[K.LETTERS_NO_DOTS]
    }
}

/** One line per check: shape, dots or sign, direction, and the body-first habit. */
private fun feedback(s: Localizer, letter: LetterTemplate, r: LetterResult): List<String> = buildList {
    add(s[when (r.body) { HandwritingVerdict.GOOD -> K.LETTERS_SHAPE_GOOD; HandwritingVerdict.CLOSE -> K.LETTERS_SHAPE_CLOSE; else -> K.LETTERS_SHAPE_OFF }])
    if (r.marksRight) { if (letter.dots > 0 || letter.hasSign) add(s[K.LETTERS_MARKS_RIGHT]) }
    else add(marksText(s, letter) + " " + if (letter.hasSign) s[K.LETTERS_SIGN_CHECK] else if (r.dotsFound == 0) s[K.LETTERS_FOUND_NONE] else s.f(K.LETTERS_FOUND, s.plural(K.LETTERS_DOTS_N, r.dotsFound)))
    if (r.reversed) add(s[K.LETTERS_REVERSED])
    if (r.marksFirst) add(s[K.LETTERS_MARKS_FIRST])
}
