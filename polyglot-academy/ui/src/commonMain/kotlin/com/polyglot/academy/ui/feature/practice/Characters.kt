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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
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
import com.polyglot.academy.core.mandarin.ComponentRole
import com.polyglot.academy.core.mandarin.HandwritingResult
import com.polyglot.academy.core.mandarin.HandwritingScorer
import com.polyglot.academy.core.mandarin.HandwritingVerdict
import com.polyglot.academy.core.mandarin.HanziData
import com.polyglot.academy.core.mandarin.InkPoint
import com.polyglot.academy.core.mandarin.WritingCharacters
import com.polyglot.academy.core.mandarin.WritingItem
import com.polyglot.academy.core.wordlist.WordLevels
import com.polyglot.academy.core.wordlist.WordLists
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.Languages
import com.polyglot.academy.core.model.SessionKind
import com.polyglot.academy.core.model.WritingPracticeState
import com.polyglot.academy.ui.app.LocalApp
import com.polyglot.academy.ui.app.ScrollScreen
import com.polyglot.academy.ui.app.rememberSpeaker
import com.polyglot.academy.ui.designsystem.AudioButton
import com.polyglot.academy.ui.designsystem.Banner
import com.polyglot.academy.ui.designsystem.ChipGroup
import com.polyglot.academy.ui.designsystem.LoadingPlaceholder
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
import com.polyglot.academy.core.mandarin.StrokeMatch
import com.polyglot.academy.core.mandarin.StrokeOrder
import com.polyglot.academy.core.mandarin.StrokeOrderResult
import com.polyglot.academy.ui.designsystem.STROKE_MILLIS
import com.polyglot.academy.ui.designsystem.drawPracticeGrid
import com.polyglot.academy.ui.designsystem.drawStrokeOrder
import com.polyglot.academy.ui.i18n.Localizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val zhLocale by lazy { localeFor(Languages.of(LanguageCode.MANDARIN).defaultVariant.localeTag) }
// Traditional characters are drawn with Taiwan glyph shapes, which match the stroke data.
private val zhTraditionalLocale by lazy { localeFor("zh-TW") }

/** Draws [ch] centred in the scope; used for the faded guide and for the scoring mask alike. */
private fun DrawScope.drawCharacter(measurer: TextMeasurer, ch: String, color: Color, traditional: Boolean) {
    val style = TextStyle(fontSize = (size.minDimension * 0.8f).toSp(), color = color, localeList = if (traditional) zhTraditionalLocale else zhLocale)
    val layout = measurer.measure(ch, style)
    drawText(layout, topLeft = Offset((size.width - layout.size.width) / 2f, (size.height - layout.size.height) / 2f))
}

private fun characterMask(measurer: TextMeasurer, ch: String, density: Density, traditional: Boolean): BooleanArray {
    val px = 256
    val bitmap = ImageBitmap(px, px)
    CanvasDrawScope().draw(density, LayoutDirection.Ltr, androidx.compose.ui.graphics.Canvas(bitmap), Size(px.toFloat(), px.toFloat())) {
        drawCharacter(measurer, ch, Color.Black, traditional)
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
    // Where practice was left last time (set, script, position) and the characters to practise again.
    val saved = remember { app.settings.writingPractice }
    val missed = app.settings.writingPractice.missed
    // MISSED: the missed characters; 0: the course characters; 1..10: the characters a word-list level introduces.
    var setLevel by remember { mutableIntStateOf(if (saved.set == WritingPracticeState.MISSED && saved.missed.isEmpty()) 0 else saved.set) }
    var traditional by remember { mutableStateOf(saved.traditional ?: (app.profile.enrollment(LanguageCode.MANDARIN)?.useTraditionalCharacters == true)) }
    // The missed set is fixed while it is open, so characters written right do not shift the position.
    val missedSet = remember(setLevel) { missed.mapNotNull { it.firstOrNull() } }
    val items by produceState<List<WritingItem>?>(null, setLevel, traditional) {
        value = null
        value = withContext(Dispatchers.Default) {
            when (setLevel) {
                WritingPracticeState.MISSED -> WritingCharacters.of(missedSet, WordLists.load(LanguageCode.MANDARIN))
                0 -> WritingCharacters.course(traditional)
                else -> WritingCharacters.level(WordLists.load(LanguageCode.MANDARIN), setLevel, traditional)
            }
        }
    }
    var index by remember(setLevel, traditional) { mutableIntStateOf(if (setLevel == WritingPracticeState.MISSED) 0 else saved.position(setLevel, traditional)) }
    LaunchedEffect(setLevel, traditional, index) {
        app.services.store.updateSettings { st ->
            st.copy(writingPractice = st.writingPractice.copy(set = setLevel, traditional = traditional).withPosition(setLevel, traditional, index))
        }
    }
    var showGuide by remember { mutableStateOf(true) }
    val practice = remember { app.services.study.practice(LanguageCode.MANDARIN, SessionKind.WRITING, "characters") }
    var checked by remember { mutableIntStateOf(0) }
    var passed by remember { mutableIntStateOf(0) }

    ScrollScreen(s[K.CHARS_TITLE]) {
        if (app.profile.activeLearningLanguage != LanguageCode.MANDARIN) Banner(BannerKind.INFO, s[K.CHARS_NOT_MANDARIN])
        Text(s[K.CHARS_BODY], style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
        PolyCard {
            Text(s[K.CHARS_SET], style = MaterialTheme.typography.titleSmall)
            val sets = (if (missed.isNotEmpty() || setLevel == WritingPracticeState.MISSED) listOf(WritingPracticeState.MISSED) else emptyList()) + listOf(0) + (1..WordLevels.COUNT)
            ChipGroup(sets, setOf(setLevel), {
                when (it) { WritingPracticeState.MISSED -> s.f(K.CHARS_SET_MISSED, missed.size); 0 -> s[K.CHARS_SET_COURSE]; else -> s.f(K.WORDLEVEL_NAME, it) }
            }, { setLevel = it })
            Text(s[K.CHARS_SET_SUB], style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
        }
        // The missed characters are shown as they were written, so the script switch does not apply there.
        if (setLevel != WritingPracticeState.MISSED) PolyCard(contentPadding = 0.dp) { SwitchRow(s[K.TONES_CHARACTERS], traditional, { traditional = it }) }
        val list = items
        if (list == null) { LoadingPlaceholder(s[K.LOADING]); return@ScrollScreen }
        if (list.isEmpty()) { Text(s[K.CHARS_MISSED_DONE], style = MaterialTheme.typography.bodyMedium); return@ScrollScreen }
        val item = list[index % list.size]
        val ch = item.character
        val entry = HanziData.of(ch)
        PolyCard {
            Text(s.f(K.CHARS_POSITION, index % list.size + 1, list.size), style = MaterialTheme.typography.labelMedium, color = c.textSecondary)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    LearningText(ch.toString(), LanguageCode.MANDARIN, style = MaterialTheme.typography.displaySmall)
                    LearningText(item.word, LanguageCode.MANDARIN, style = MaterialTheme.typography.titleMedium, reading = item.reading)
                    Text(s.gloss(item.meaning), style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
                }
                AudioButton(s[K.PLAY_AUDIO], { speaker.toggle(item.word, LanguageCode.MANDARIN) }, playing = speaker.playingText == item.word)
            }
            Text(s.f(K.CHARS_STROKES, item.strokes), style = MaterialTheme.typography.labelLarge)
            if (entry != null && entry.components.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    entry.components.forEach { part ->
                        val role = when (part.role) { ComponentRole.SOUND -> s[K.CHARS_ROLE_SOUND]; ComponentRole.MEANING -> s[K.CHARS_ROLE_MEANING]; ComponentRole.OTHER -> s[K.CHARS_ROLE_PART] }
                        Pill("${part.form} · ${s.gloss(part.meaning)} · $role")
                    }
                }
            }
            entry?.hint?.let { Text(s.gloss(it), style = MaterialTheme.typography.bodySmall, color = c.textSecondary) }
        }

        PolyCard(contentPadding = 0.dp) { SwitchRow(s[K.CHARS_GUIDE], showGuide, { showGuide = it }, subtitle = s[K.CHARS_GUIDE_SUB]) }

        key(index, setLevel, traditional) {
            WritingPad(ch, item.strokes, traditional, showGuide) { check ->
                checked++; if (check.result.verdict == HandwritingVerdict.GOOD) passed++
                scope.launch {
                    practice.answer(check.result.verdict == HandwritingVerdict.GOOD)
                    app.services.store.updateSettings { st -> st.copy(writingPractice = st.writingPractice.checked(ch, check.right)) }
                }
            }
        }
        if (checked > 0) Text(s.f(K.TONES_SCORE, passed, checked), style = MaterialTheme.typography.titleSmall)
        PrimaryButton(s[K.NEXT], { index++ }, Modifier.fillMaxWidth())
        SectionHeader(s[K.CHARS_ABOUT_TITLE])
        Text(s[K.CHARS_ABOUT], style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
    }
}

/** The outcome of checking a drawing. [right] is a good shape, or a close one with every stroke in order and direction. */
internal data class WritingCheck(val result: HandwritingResult, val order: StrokeOrderResult?, val right: Boolean)

/**
 * A 田字格 practice grid to write [ch] on, with the stroke-order demonstration, undo, clear and a check that
 * compares the drawing with the character's shape and its stroke-order data. Used by writing practice and
 * by the write-the-character exercise in lessons. [onChecked] is called for every non-empty check.
 */
@Composable
internal fun WritingPad(ch: Char, expectedStrokes: Int, traditional: Boolean, showGuide: Boolean, onChecked: (WritingCheck) -> Unit) {
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val scope = rememberCoroutineScope()
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val strokes = remember(ch, traditional) { mutableStateListOf<List<InkPoint>>() }
    var current by remember(ch, traditional) { mutableStateOf<List<InkPoint>>(emptyList()) }
    var result by remember(ch, traditional) { mutableStateOf<HandwritingResult?>(null) }
    var order by remember(ch, traditional) { mutableStateOf<StrokeOrderResult?>(null) }
    // Stroke-order demonstration: the animated number of strokes drawn, shown while showDemo is on.
    val demo = remember(ch, traditional) { androidx.compose.animation.core.Animatable(0f) }
    var showDemo by remember(ch, traditional) { mutableStateOf(false) }
    val reduced = PolyglotTheme.motion.reduced
    val medians = StrokeOrder.of(ch)

    if (medians != null) SecondaryButton(s[K.CHARS_SHOW_ORDER], {
        showDemo = true
        scope.launch {
            if (reduced) demo.snapTo(medians.size.toFloat())
            else { demo.snapTo(0f); demo.animateTo(medians.size.toFloat(), androidx.compose.animation.core.tween(medians.size * STROKE_MILLIS, easing = androidx.compose.animation.core.LinearEasing)) }
        }
    }, Modifier.fillMaxWidth())

    val ink = c.textPrimary
    val guide = c.border
    Canvas(
        Modifier.fillMaxWidth().widthIn(max = 360.dp).aspectRatio(1f).background(c.surface, RoundedCornerShape(12.dp))
            .border(1.dp, c.border, RoundedCornerShape(12.dp))
            .semantics { contentDescription = s.f(K.CHARS_BOX_DESC, ch.toString(), strokes.size) }
            .pointerInput(ch, traditional) {
                detectDragGestures(
                    onDragStart = { o -> current = listOf(InkPoint(o.x / size.width, o.y / size.height)); result = null; order = null; showDemo = false },
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
        drawPracticeGrid(guide)
        if (showDemo && medians != null) drawStrokeOrder(medians, demo.value, guide.copy(alpha = 0.7f), c.primary)
        else if (showGuide) drawCharacter(measurer, ch.toString(), guide.copy(alpha = 0.55f), traditional)
        (strokes + listOf(current)).filter { it.isNotEmpty() }.forEach { stroke ->
            val path = Path()
            stroke.forEachIndexed { i, p -> if (i == 0) path.moveTo(p.x * size.width, p.y * size.height) else path.lineTo(p.x * size.width, p.y * size.height) }
            drawPath(path, ink, style = Stroke(width = size.minDimension * 0.045f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SecondaryButton(s[K.CHARS_UNDO], { strokes.removeLastOrNull(); result = null; order = null }, Modifier.weight(1f), enabled = strokes.isNotEmpty())
        SecondaryButton(s[K.CHARS_CLEAR], { strokes.clear(); result = null; order = null }, Modifier.weight(1f), enabled = strokes.isNotEmpty())
    }
    PrimaryButton(s[K.CHARS_CHECK], {
        val r = HandwritingScorer.score(strokes.toList(), characterMask(measurer, ch.toString(), density, traditional), expectedStrokes)
        val o = medians?.let { StrokeOrder.check(strokes.toList(), it) }
        result = r
        order = o
        if (r.verdict != HandwritingVerdict.EMPTY) {
            // The shape check compares with the device font, whose style differs from the stroke data, so a
            // correctly written character is often only "close" there.
            val allInOrder = o?.allCorrect
            val right = (r.verdict == HandwritingVerdict.GOOD && allInOrder != false) || (r.verdict == HandwritingVerdict.CLOSE && allInOrder == true)
            onChecked(WritingCheck(r, o, right))
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
                Text(if (d > 0) s.f(K.CHARS_STROKES_MORE, r.strokesDrawn, expectedStrokes) else s.f(K.CHARS_STROKES_FEWER, r.strokesDrawn, expectedStrokes),
                    style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
            }
            order?.let { o -> Banner(if (o.allCorrect) BannerKind.SUCCESS else BannerKind.INFO, orderMessage(s, o), title = s[K.CHARS_ORDER_TITLE]) }
        }
    }
}

/** One sentence about the learner's stroke order: all right, or the first stroke that is not. */
private fun orderMessage(s: Localizer, o: StrokeOrderResult): String {
    val first = o.firstProblem ?: return s.f(K.CHARS_ORDER_OK, o.expected)
    val n = first + 1
    return when (o.strokes[first]) {
        StrokeMatch.REVERSED -> s.f(K.CHARS_ORDER_REVERSED, n)
        StrokeMatch.OUT_OF_ORDER -> s.f(K.CHARS_ORDER_SWAPPED, n)
        StrokeMatch.DIFFERENT -> s.f(K.CHARS_ORDER_DIFFERENT, n)
        StrokeMatch.MISSING -> s.f(K.CHARS_ORDER_MISSING, n, o.expected)
        StrokeMatch.EXTRA -> s.f(K.CHARS_ORDER_EXTRA, o.expected)
        StrokeMatch.OK -> s.f(K.CHARS_ORDER_OK, o.expected)
    } + " " + s.f(K.CHARS_ORDER_COUNT, o.correct, o.expected)
}
