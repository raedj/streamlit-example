package com.polyglot.academy.ui.feature.practice

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.polyglot.academy.core.content.Curriculum
import com.polyglot.academy.core.mandarin.ComponentRole
import com.polyglot.academy.core.mandarin.HandwritingResult
import com.polyglot.academy.core.mandarin.HandwritingScorer
import com.polyglot.academy.core.mandarin.HandwritingVerdict
import com.polyglot.academy.core.mandarin.HanziData
import com.polyglot.academy.core.mandarin.InkPoint
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.Languages
import com.polyglot.academy.core.model.SessionKind
import com.polyglot.academy.core.model.StudySession
import com.polyglot.academy.ui.app.LocalApp
import com.polyglot.academy.ui.app.ScrollScreen
import com.polyglot.academy.ui.app.rememberSpeaker
import com.polyglot.academy.ui.designsystem.AudioButton
import com.polyglot.academy.ui.designsystem.Banner
import com.polyglot.academy.ui.designsystem.BannerKind
import com.polyglot.academy.ui.designsystem.LearningText
import com.polyglot.academy.ui.designsystem.Pill
import com.polyglot.academy.ui.designsystem.PolyCard
import com.polyglot.academy.ui.designsystem.PolyglotTheme
import com.polyglot.academy.ui.designsystem.PrimaryButton
import com.polyglot.academy.ui.designsystem.SecondaryButton
import com.polyglot.academy.ui.designsystem.SectionHeader
import com.polyglot.academy.ui.designsystem.SwitchRow
import com.polyglot.academy.ui.designsystem.localeFor
import com.polyglot.academy.ui.i18n.K
import com.polyglot.academy.ui.i18n.LocalStrings
import kotlinx.coroutines.launch

/** The characters of the course that have stroke data, in the order the course introduces them. */
private val practiceCharacters: List<Char> by lazy {
    Curriculum.of(LanguageCode.MANDARIN).vocabulary.flatMap { it.term.toList() }.distinct().filter { HanziData.of(it) != null }
}

private val zhLocale by lazy { localeFor(Languages.of(LanguageCode.MANDARIN).defaultVariant.localeTag) }

/** Draws [ch] centred in the scope; used for the faded guide and for the scoring mask alike. */
private fun DrawScope.drawCharacter(measurer: TextMeasurer, ch: String, color: Color) {
    val style = TextStyle(fontSize = (size.minDimension * 0.8f).toSp(), color = color, localeList = zhLocale)
    val layout = measurer.measure(ch, style)
    drawText(layout, topLeft = Offset((size.width - layout.size.width) / 2f, (size.height - layout.size.height) / 2f))
}

private fun characterMask(measurer: TextMeasurer, ch: String, density: Density): BooleanArray {
    val px = 256
    val bitmap = ImageBitmap(px, px)
    CanvasDrawScope().draw(density, LayoutDirection.Ltr, androidx.compose.ui.graphics.Canvas(bitmap), Size(px.toFloat(), px.toFloat())) {
        drawCharacter(measurer, ch, Color.Black)
    }
    val pixels = IntArray(px * px)
    bitmap.readPixels(pixels, 0, 0, px, px)
    return HandwritingScorer.maskFromArgb(pixels, px)
}

@Composable
fun CharactersScreen() {
    val app = LocalApp.current
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val scope = rememberCoroutineScope()
    val speaker = rememberSpeaker(s)
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val zh = Curriculum.of(LanguageCode.MANDARIN)
    var index by remember { mutableIntStateOf(0) }
    var showGuide by remember { mutableStateOf(true) }
    val strokes = remember(index) { mutableStateListOf<List<InkPoint>>() }
    var current by remember(index) { mutableStateOf<List<InkPoint>>(emptyList()) }
    var result by remember(index) { mutableStateOf<HandwritingResult?>(null) }
    var session by remember { mutableStateOf<StudySession?>(null) }
    var checked by remember { mutableIntStateOf(0) }
    var passed by remember { mutableIntStateOf(0) }

    ScrollScreen(s[K.CHARS_TITLE]) {
        if (app.profile.activeLearningLanguage != LanguageCode.MANDARIN) Banner(BannerKind.INFO, s[K.CHARS_NOT_MANDARIN])
        Text(s[K.CHARS_BODY], style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
        val ch = practiceCharacters[index % practiceCharacters.size]
        val entry = HanziData.of(ch)!!
        val word = zh.vocabulary.firstOrNull { it.term == ch.toString() } ?: zh.vocabulary.first { ch in it.term }
        PolyCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    LearningText(ch.toString(), LanguageCode.MANDARIN, style = MaterialTheme.typography.displaySmall)
                    LearningText(word.term, LanguageCode.MANDARIN, style = MaterialTheme.typography.titleMedium, reading = word.pronunciation)
                    Text(s.gloss(word.meaning), style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
                }
                AudioButton(s[K.PLAY_AUDIO], { speaker.toggle(word.term, LanguageCode.MANDARIN) }, playing = speaker.playingText == word.term)
            }
            Text(s.f(K.CHARS_STROKES, entry.strokes), style = MaterialTheme.typography.labelLarge)
            if (entry.components.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    entry.components.forEach { part ->
                        val role = when (part.role) { ComponentRole.SOUND -> s[K.CHARS_ROLE_SOUND]; ComponentRole.MEANING -> s[K.CHARS_ROLE_MEANING]; ComponentRole.OTHER -> s[K.CHARS_ROLE_PART] }
                        Pill("${part.form} · ${s.gloss(part.meaning)} · $role")
                    }
                }
            }
            entry.hint?.let { Text(s.gloss(it), style = MaterialTheme.typography.bodySmall, color = c.textSecondary) }
        }

        PolyCard(contentPadding = 0.dp) { SwitchRow(s[K.CHARS_GUIDE], showGuide, { showGuide = it }, subtitle = s[K.CHARS_GUIDE_SUB]) }

        val ink = c.textPrimary
        val guide = c.border
        Canvas(
            Modifier.fillMaxWidth().widthIn(max = 360.dp).aspectRatio(1f).background(c.surface, RoundedCornerShape(12.dp))
                .border(1.dp, c.border, RoundedCornerShape(12.dp))
                .semantics { contentDescription = s.f(K.CHARS_BOX_DESC, ch.toString(), strokes.size) }
                .pointerInput(index) {
                    detectDragGestures(
                        onDragStart = { o -> current = listOf(InkPoint(o.x / size.width, o.y / size.height)); result = null },
                        onDrag = { change, _ ->
                            val p = change.position
                            current = current + InkPoint((p.x / size.width).coerceIn(0f, 1f), (p.y / size.height).coerceIn(0f, 1f))
                        },
                        onDragEnd = { if (current.isNotEmpty()) strokes += current; current = emptyList() },
                        onDragCancel = { current = emptyList() },
                    )
                },
        ) {
            // 田字格: the traditional practice grid with dashed centre lines.
            val dash = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
            drawLine(guide, Offset(size.width / 2, 0f), Offset(size.width / 2, size.height), 1.dp.toPx(), pathEffect = dash)
            drawLine(guide, Offset(0f, size.height / 2), Offset(size.width, size.height / 2), 1.dp.toPx(), pathEffect = dash)
            if (showGuide) drawCharacter(measurer, ch.toString(), guide.copy(alpha = 0.55f))
            (strokes + listOf(current)).filter { it.isNotEmpty() }.forEach { stroke ->
                val path = Path()
                stroke.forEachIndexed { i, p -> if (i == 0) path.moveTo(p.x * size.width, p.y * size.height) else path.lineTo(p.x * size.width, p.y * size.height) }
                drawPath(path, ink, style = Stroke(width = size.minDimension * 0.045f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SecondaryButton(s[K.CHARS_UNDO], { strokes.removeLastOrNull(); result = null }, Modifier.weight(1f), enabled = strokes.isNotEmpty())
            SecondaryButton(s[K.CHARS_CLEAR], { strokes.clear(); result = null }, Modifier.weight(1f), enabled = strokes.isNotEmpty())
        }
        PrimaryButton(s[K.CHARS_CHECK], {
            val r = HandwritingScorer.score(strokes.toList(), characterMask(measurer, ch.toString(), density), entry.strokes)
            result = r
            if (r.verdict != HandwritingVerdict.EMPTY) {
                checked++; if (r.verdict == HandwritingVerdict.GOOD) passed++
                scope.launch {
                    val sess = session ?: app.services.study.startSession(LanguageCode.MANDARIN, SessionKind.WRITING, "characters").also { session = it }
                    session = app.services.study.updateSession(sess, checked, passed, false, 0)
                }
            }
        }, Modifier.fillMaxWidth(), enabled = strokes.isNotEmpty())

        result?.let { r ->
            val (kind, text) = when (r.verdict) {
                HandwritingVerdict.GOOD -> BannerKind.SUCCESS to s[K.CHARS_GOOD]
                HandwritingVerdict.CLOSE -> BannerKind.INFO to s[K.CHARS_CLOSE]
                HandwritingVerdict.TRY_AGAIN -> BannerKind.WARNING to s[K.CHARS_TRY_AGAIN]
                HandwritingVerdict.EMPTY -> BannerKind.INFO to s[K.CHARS_EMPTY]
            }
            Banner(kind, text)
            if (r.verdict != HandwritingVerdict.EMPTY) {
                Text(s.f(K.CHARS_SCORES, (r.coverage * 100).toInt(), (r.precision * 100).toInt()), style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                HandwritingScorer.strokeDifference(r)?.let { d ->
                    Text(if (d > 0) s.f(K.CHARS_STROKES_MORE, r.strokesDrawn, entry.strokes) else s.f(K.CHARS_STROKES_FEWER, r.strokesDrawn, entry.strokes),
                        style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                }
            }
        }
        if (checked > 0) Text(s.f(K.TONES_SCORE, passed, checked), style = MaterialTheme.typography.titleSmall)
        PrimaryButton(s[K.NEXT], { index++ }, Modifier.fillMaxWidth())
        SectionHeader(s[K.CHARS_ABOUT_TITLE])
        Text(s[K.CHARS_ABOUT], style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
    }
}
