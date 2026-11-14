package com.polyglot.academy.ui.feature.lesson

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.polyglot.academy.core.content.Curriculum
import com.polyglot.academy.core.learning.AnswerChecker
import com.polyglot.academy.core.learning.AnswerVerdict
import com.polyglot.academy.core.mandarin.Pinyin
import com.polyglot.academy.core.mandarin.ComponentRole
import com.polyglot.academy.core.mandarin.HandwritingVerdict
import com.polyglot.academy.core.mandarin.HanziData
import com.polyglot.academy.core.mandarin.StrokeOrder
import com.polyglot.academy.ui.feature.practice.WritingPad
import com.polyglot.academy.ui.designsystem.Pill
import com.polyglot.academy.ui.designsystem.SwitchRow
import com.polyglot.academy.core.model.Exercise
import com.polyglot.academy.core.model.Gloss
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.Languages
import com.polyglot.academy.core.service.ExerciseOutcome
import com.polyglot.academy.core.speech.RecognitionEvent
import com.polyglot.academy.core.speech.SpeechAvailability
import com.polyglot.academy.core.util.TextNormalizer
import com.polyglot.academy.core.util.seededShuffle
import com.polyglot.academy.ui.app.LocalApp
import com.polyglot.academy.ui.app.Speaker
import com.polyglot.academy.ui.designsystem.AppTextField
import com.polyglot.academy.ui.designsystem.AudioButton
import com.polyglot.academy.ui.designsystem.Banner
import com.polyglot.academy.ui.designsystem.BannerKind
import com.polyglot.academy.ui.designsystem.ChoiceCard
import com.polyglot.academy.ui.designsystem.LearningText
import com.polyglot.academy.ui.designsystem.MicButton
import com.polyglot.academy.ui.designsystem.MicVisualState
import com.polyglot.academy.ui.designsystem.PolyCard
import com.polyglot.academy.ui.designsystem.PolyglotTheme
import com.polyglot.academy.ui.designsystem.PrimaryButton
import com.polyglot.academy.ui.designsystem.QuietButton
import com.polyglot.academy.ui.designsystem.SecondaryButton
import com.polyglot.academy.ui.i18n.K
import com.polyglot.academy.ui.i18n.Labels
import com.polyglot.academy.ui.i18n.LocalStrings
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/** What the learner did; the player turns it into feedback and persistence. */
data class Answer(val outcome: ExerciseOutcome, val userAnswer: String, val hintsUsed: Int, val promptText: String)

/** Context shared by all exercise views. */
data class ExerciseContext(
    val language: LanguageCode,
    /** Language used for glosses/explanations (English or Arabic). */
    val explain: LanguageCode,
    val speaker: Speaker,
    val hintsAvailable: Boolean,
    val showReading: Boolean,
    val traditional: Boolean,
    /** Items whose reading is hidden behind a button (known words, when the learner chose so). */
    val readingHiddenFor: Set<String> = emptySet(),
    /** Other answers nearby, used as distractors in the tiles for typed answers. */
    val tilePool: List<String> = emptyList(),
) {
    fun gloss(g: Gloss) = g.forLanguage(explain)
    fun readingHidden(itemId: String?) = itemId != null && itemId in readingHiddenFor
}

/** Where a screen pins an exercise's buttons. Screens that provide none show the buttons under the exercise. */
class ExerciseActionSlot {
    var content by mutableStateOf<(@Composable () -> Unit)?>(null)
}

/** What Enter does in the current exercise (check, continue), and what number keys choose; see [ExerciseKeys]. */
class ExerciseKeySlot {
    var onEnter: (() -> Unit)? = null
    var onNumber: ((Int) -> Unit)? = null

    /** For [com.polyglot.academy.ui.designsystem.KeyShortcuts]: true when the exercise used the key. */
    fun enter(): Boolean = onEnter?.let { it(); true } ?: false
    fun number(n: Int): Boolean = onNumber?.let { it(n); true } ?: false
}

val LocalExerciseKeys = staticCompositionLocalOf<ExerciseKeySlot?> { null }

/** Registers the current exercise's keyboard shortcuts with the screen ([com.polyglot.academy.ui.designsystem.KeyShortcuts]). */
@Composable
private fun ExerciseKeys(onEnter: (() -> Unit)? = null, onNumber: ((Int) -> Unit)? = null) {
    val slot = LocalExerciseKeys.current ?: return
    val enter by rememberUpdatedState(onEnter)
    val number by rememberUpdatedState(onNumber)
    DisposableEffect(slot) {
        val e: (() -> Unit)? = if (onEnter != null) ({ enter?.invoke() }) else null
        val n: ((Int) -> Unit)? = if (onNumber != null) ({ k: Int -> number?.invoke(k) }) else null
        slot.onEnter = e; slot.onNumber = n
        onDispose { if (slot.onEnter === e) slot.onEnter = null; if (slot.onNumber === n) slot.onNumber = null }
    }
}

val LocalExerciseActions = staticCompositionLocalOf<ExerciseActionSlot?> { null }

@Composable
private fun ExerciseActions(content: @Composable () -> Unit) {
    val slot = LocalExerciseActions.current
    if (slot == null) { content(); return }
    val latest by rememberUpdatedState(content)
    DisposableEffect(slot) {
        val pinned: @Composable () -> Unit = { latest() }
        slot.content = pinned
        onDispose { if (slot.content === pinned) slot.content = null }
    }
}

@Composable
fun ExerciseView(exercise: Exercise, ctx: ExerciseContext, answered: Boolean, onAnswer: (Answer) -> Unit) {
    val s = LocalStrings.current
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // The explanation language can differ from the interface language (Arabic hints in the English
        // interface), so the instruction takes the full width and its own direction.
        Text(ctx.gloss(exercise.instruction), Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.titleMedium.copy(textDirection = androidx.compose.ui.text.style.TextDirection.Content),
            color = PolyglotTheme.colors.textSecondary)
        when (exercise) {
            is Exercise.Flashcard -> FlashcardView(exercise, ctx, answered, onAnswer)
            is Exercise.MultipleChoice -> MultipleChoiceView(exercise, ctx, answered, onAnswer)
            is Exercise.FillInBlank -> TypedView(exercise.sentence, exercise.acceptedAnswers, exercise.hint, ctx, answered, onAnswer, sentenceInTarget = true)
            is Exercise.Translate -> {
                if (com.polyglot.academy.core.model.WORD_RECALL_MARK !in exercise.id) Text(s[K.TRANSLATE_PROMPT], style = MaterialTheme.typography.labelLarge, color = PolyglotTheme.colors.textSecondary)
                TypedView(ctx.gloss(exercise.source), exercise.acceptedAnswers, null, ctx, answered, onAnswer, sentenceInTarget = false, tiles = true)
            }
            is Exercise.Dictation -> DictationView(exercise, ctx, answered, onAnswer)
            is Exercise.SentenceOrder -> SentenceOrderView(exercise, ctx, answered, onAnswer)
            is Exercise.Matching -> MatchingView(exercise, ctx, answered, onAnswer)
            is Exercise.SpeakRepeat -> SpeakRepeatView(exercise, ctx, answered, onAnswer)
            is Exercise.ToneChoice -> ToneChoiceView(exercise, ctx, answered, onAnswer)
            is Exercise.WriteCharacter -> WriteCharacterView(exercise, ctx, answered, onAnswer)
            is Exercise.FreeWriting -> FreeWritingView(exercise, ctx, answered, onAnswer)
            is Exercise.GrammarNote -> GrammarNoteView(exercise, ctx, answered, onAnswer)
        }
    }
}

@Composable
private fun FlashcardView(e: Exercise.Flashcard, ctx: ExerciseContext, answered: Boolean, onAnswer: (Answer) -> Unit) {
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val v = Curriculum.of(ctx.language).vocabularyById[e.vocabularyId]
        // Already loaded by the lesson that contains the card.
        ?: com.polyglot.academy.core.wordlist.WordLists.of(ctx.language).byId[e.vocabularyId]?.toVocabularyItem() ?: return
    var readingShown by remember(e.id) { mutableStateOf(!ctx.readingHidden(e.vocabularyId)) }
    val term = if (ctx.traditional) v.traditional ?: v.term else v.term
    PolyCard {
        Column(Modifier.fillMaxWidth().heightIn(min = 320.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)) {
            LearningText(term, ctx.language, reading = if (ctx.showReading && readingShown) v.pronunciation ?: v.transliteration else null, textAlign = TextAlign.Center,
                style = if (ctx.language == LanguageCode.MANDARIN) PolyglotTheme.learning.hanzi else PolyglotTheme.learning.term)
            if (ctx.showReading && !readingShown) QuietButton(s[K.SHOW_READING], { readingShown = true })
            AudioButton(s[K.PLAY_AUDIO], { ctx.speaker.toggle(v.term, ctx.language) }, playing = ctx.speaker.playingText == v.term)
            Text(ctx.gloss(v.meaning), style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
            v.example?.let { ex ->
                Text(s[K.FLASHCARD_EXAMPLE], style = MaterialTheme.typography.labelMedium, color = c.textSecondary)
                com.polyglot.academy.ui.feature.common.TappableSentence(ex, ctx.language, ctx.speaker, style = MaterialTheme.typography.bodyLarge, center = true)
                v.exampleTranslation?.let { Text(ctx.gloss(it), style = MaterialTheme.typography.bodyMedium, color = c.textSecondary, textAlign = TextAlign.Center) }
            }
        }
    }
    // A word met for the first time cannot be recalled, so it is presented; the exercises that follow check it.
    val present = { onAnswer(Answer(ExerciseOutcome(true, 1.0, "", counted = false), "", 0, v.term)) }
    if (!answered) {
        ExerciseKeys(onEnter = present)
        ExerciseActions { PrimaryButton(s[K.CONTINUE], present, Modifier.fillMaxWidth()) }
    }
}

@Composable
private fun MultipleChoiceView(e: Exercise.MultipleChoice, ctx: ExerciseContext, answered: Boolean, onAnswer: (Answer) -> Unit) {
    val s = LocalStrings.current
    var selected by remember(e.id) { mutableIntStateOf(-1) }
    LaunchedEffect(e.id) { e.audioText?.let { ctx.speaker.play(it, ctx.language) } }
    if (e.audioText != null) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AudioButton(s[K.PLAY_AUDIO], { ctx.speaker.toggle(e.audioText!!, ctx.language) }, playing = ctx.speaker.playingText == e.audioText)
            AudioButton(s[K.LISTEN_SLOW], { ctx.speaker.play(e.audioText!!, ctx.language, 0.7f) })
            Text(s[K.LISTEN_AGAIN], style = MaterialTheme.typography.bodyMedium, color = PolyglotTheme.colors.textSecondary)
        }
    } else if (e.promptGloss != null) {
        Text(ctx.gloss(e.promptGloss!!), style = MaterialTheme.typography.headlineSmall)
        // A sentence with a blank is the question itself; the gloss is only its instruction.
        if ("___" in e.prompt) LearningText(e.prompt, ctx.language, style = PolyglotTheme.learning.sentence)
    } else {
        LearningText(e.prompt, ctx.language, style = PolyglotTheme.learning.sentence)
    }
    // Mixed per question, so the right answer is not usually the first one.
    val order = remember(e.id, e.options.size) { com.polyglot.academy.core.learning.ChoiceOrder.of(e.id, e.options.size) }
    fun choose(i: Int) {
        if (answered) return
        selected = i
        val correct = i == e.correctIndex
        val expected = e.optionGlosses?.getOrNull(e.correctIndex)?.let(ctx::gloss) ?: e.options[e.correctIndex]
        onAnswer(Answer(ExerciseOutcome(correct, if (correct) 1.0 else 0.0, expected), e.options[i], 0, e.prompt))
    }
    if (!answered) ExerciseKeys(onNumber = { k -> order.getOrNull(k - 1)?.let(::choose) })
    order.forEach { i ->
        val opt = e.options[i]
        val label = e.optionGlosses?.getOrNull(i)?.let(ctx::gloss) ?: opt
        val state = when {
            !answered -> null
            i == e.correctIndex -> s[K.FEEDBACK_CORRECT]
            i == selected -> s[K.FEEDBACK_INCORRECT]
            else -> null
        }
        ChoiceCard(
            title = label, selected = selected == i || (answered && i == e.correctIndex),
            subtitle = state, language = ctx.language.takeIf { e.optionGlosses == null },
            onClick = { choose(i) },
        )
    }
}

@Composable
private fun TypedView(
    prompt: String, accepted: List<String>, hint: Gloss?, ctx: ExerciseContext, answered: Boolean, onAnswer: (Answer) -> Unit, sentenceInTarget: Boolean,
    tiles: Boolean = false,
) {
    val s = LocalStrings.current
    var text by remember(prompt) { mutableStateOf("") }
    var hints by remember(prompt) { mutableIntStateOf(0) }
    var empty by remember(prompt) { mutableStateOf(false) }
    var wrongKeyboard by remember(prompt) { mutableStateOf(false) }
    fun submit() {
        if (answered) return
        val check = AnswerChecker.check(text, accepted)
        if (check.verdict == AnswerVerdict.EMPTY) { empty = true; return }
        wrongKeyboard = check.verdict == AnswerVerdict.WRONG_KEYBOARD
        if (wrongKeyboard) return
        onAnswer(Answer(ExerciseOutcome(check.isCorrect, check.score, check.closestAccepted, check), text, hints, prompt))
    }
    if (sentenceInTarget) LearningText(prompt.replace("___", "_____"), ctx.language, style = PolyglotTheme.learning.sentence)
    else Text(prompt, style = MaterialTheme.typography.headlineSmall)
    if (tiles && !answered) RecallInput(text, { text = it; empty = false }, ctx.language, accepted.first(), ctx.tilePool, prompt,
        supporting = if (empty) s[K.FEEDBACK_EMPTY] else null, isError = empty, onSubmit = ::submit)
    else AppTextField(text, { text = it; empty = false }, s[K.TYPE_ANSWER], isError = empty, supporting = if (empty) s[K.FEEDBACK_EMPTY] else null, onSubmit = ::submit,
        textStyle = com.polyglot.academy.ui.designsystem.styleFor(ctx.language, MaterialTheme.typography.bodyLarge).copy(localeList = com.polyglot.academy.ui.designsystem.localeFor(Languages.of(ctx.language).defaultVariant.localeTag),
            textDirection = com.polyglot.academy.ui.designsystem.textDirectionFor(ctx.language)))
    if (wrongKeyboard) Banner(BannerKind.WARNING, s[K.FEEDBACK_KEYBOARD])
    if (ctx.hintsAvailable && !answered) {
        if (hints == 0) QuietButton(s[K.HINT_SHOW], { hints = 1 })
        else Banner(BannerKind.INFO, hint?.let(ctx::gloss) ?: s.f(K.HINT_FIRST_LETTERS, accepted.first().take(maxOf(1, accepted.first().length / 3))))
    }
    if (!answered) {
        ExerciseKeys(onEnter = ::submit)
        ExerciseActions { PrimaryButton(s[K.CHECK], ::submit, Modifier.fillMaxWidth()) }
    }
}

/** A typed answer with an optional row of tiles, so those who cannot type this script yet can still produce it. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecallInput(
    value: String, onValue: (String) -> Unit, language: LanguageCode, answer: String, others: List<String>, key: String,
    supporting: String? = null, isError: Boolean = false, allowTiles: Boolean = true, onSubmit: (() -> Unit)? = null,
) {
    val s = LocalStrings.current
    // Most learners cannot type characters without a pinyin keyboard, so Mandarin opens with the tiles.
    var showTiles by remember(key) { mutableStateOf(allowTiles && language == LanguageCode.MANDARIN) }
    AppTextField(value, onValue, s[K.TYPE_ANSWER], isError = isError, supporting = supporting, onSubmit = onSubmit,
        textStyle = com.polyglot.academy.ui.designsystem.styleFor(language, MaterialTheme.typography.bodyLarge).copy(
            localeList = com.polyglot.academy.ui.designsystem.localeFor(Languages.of(language).defaultVariant.localeTag),
            textDirection = com.polyglot.academy.ui.designsystem.textDirectionFor(language)))
    if (allowTiles) QuietButton(s[if (showTiles) K.RECALL_HIDE_TILES else K.RECALL_SHOW_TILES], { showTiles = !showTiles })
    if (showTiles) {
        val wordTiles = com.polyglot.academy.core.learning.Recall.usesWordTiles(answer)
        val tiles = remember(key, answer) { com.polyglot.academy.core.learning.Recall.tiles(answer, others, key.hashCode()) }
        androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            tiles.forEach { tile ->
                SecondaryButton(if (tile == " ") s[K.RECALL_SPACE] else tile, { onValue(if (wordTiles && value.isNotBlank()) "${value.trimEnd()} $tile" else value + tile) })
            }
            QuietButton(s[K.RECALL_DELETE], { onValue(if (wordTiles) value.trimEnd().substringBeforeLast(' ', "") else value.dropLast(1)) })
        }
    }
}

@Composable
private fun DictationView(e: Exercise.Dictation, ctx: ExerciseContext, answered: Boolean, onAnswer: (Answer) -> Unit) {
    val s = LocalStrings.current
    var text by remember(e.id) { mutableStateOf("") }
    var wrongKeyboard by remember(e.id) { mutableStateOf(false) }
    LaunchedEffect(e.id) { ctx.speaker.play(e.audioText, ctx.language) }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        AudioButton(s[K.PLAY_AUDIO], { ctx.speaker.toggle(e.audioText, ctx.language) }, playing = ctx.speaker.playingText == e.audioText)
        SecondaryButton(s[K.LISTEN_SLOW], { ctx.speaker.play(e.audioText, ctx.language, 0.65f) })
    }
    fun submit() {
        if (answered) return
        val check = AnswerChecker.check(text, e.acceptedAnswers)
        if (check.verdict == AnswerVerdict.EMPTY) return
        wrongKeyboard = check.verdict == AnswerVerdict.WRONG_KEYBOARD
        if (wrongKeyboard) return
        onAnswer(Answer(ExerciseOutcome(check.isCorrect, maxOf(check.score, TextNormalizer.similarity(text, e.audioText) * 0.8), e.audioText, check), text, 0, e.audioText))
    }
    AppTextField(text, { text = it }, s[K.TYPE_ANSWER], singleLine = false, minLines = 2, onSubmit = ::submit,
        textStyle = com.polyglot.academy.ui.designsystem.styleFor(ctx.language, MaterialTheme.typography.bodyLarge).copy(
            localeList = com.polyglot.academy.ui.designsystem.localeFor(Languages.of(ctx.language).defaultVariant.localeTag),
            textDirection = com.polyglot.academy.ui.designsystem.textDirectionFor(ctx.language)))
    if (wrongKeyboard) Banner(BannerKind.WARNING, s[K.FEEDBACK_KEYBOARD])
    if (!answered) {
        ExerciseKeys(onEnter = ::submit)
        ExerciseActions { PrimaryButton(s[K.CHECK], ::submit, Modifier.fillMaxWidth()) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SentenceOrderView(e: Exercise.SentenceOrder, ctx: ExerciseContext, answered: Boolean, onAnswer: (Answer) -> Unit) {
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val shuffled = remember(e.id) {
        val idx = e.tokens.indices.toList().seededShuffle(e.id)
        if (idx == e.tokens.indices.toList()) idx.reversed() else idx
    }
    val chosen = remember(e.id) { mutableStateListOf<Int>() }
    Text(s.f(K.ORDER_MEANING, ctx.gloss(e.translation)), style = MaterialTheme.typography.bodyLarge, color = c.textSecondary)
    // Answer line
    FlowRow(
        Modifier.fillMaxWidth().heightIn(min = 64.dp).clip(RoundedCornerShape(12.dp)).border(1.dp, c.border, RoundedCornerShape(12.dp)).padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        chosen.forEach { i -> TokenChip(e.tokens[i], ctx.language, filled = true) { if (!answered) chosen.remove(i) } }
    }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        shuffled.filter { it !in chosen }.forEach { i -> TokenChip(e.tokens[i], ctx.language, filled = false) { if (!answered) chosen.add(i) } }
    }
    if (!answered) ExerciseActions { Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SecondaryButton(s[K.ORDER_RESET], { chosen.clear() }, Modifier.weight(1f))
        PrimaryButton(s[K.CHECK], {
            val answer = chosen.joinToString(e.separator) { e.tokens[it] }
            val expected = e.tokens.joinToString(e.separator)
            val correct = TextNormalizer.basic(answer) == TextNormalizer.basic(expected)
            onAnswer(Answer(ExerciseOutcome(correct, if (correct) 1.0 else 0.0, expected), answer, 0, ctx.gloss(e.translation)))
        }, Modifier.weight(1f), enabled = chosen.size == e.tokens.size)
    } }
}

@Composable
private fun TokenChip(text: String, language: LanguageCode, filled: Boolean, onClick: () -> Unit) {
    val c = PolyglotTheme.colors
    Box(
        Modifier.clip(RoundedCornerShape(10.dp)).background(if (filled) c.primaryContainer else c.card).border(1.dp, if (filled) c.primary else c.border, RoundedCornerShape(10.dp))
            .clickable(role = Role.Button, onClick = onClick).heightIn(min = 48.dp).padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) { LearningText(text, language, style = MaterialTheme.typography.titleMedium) }
}

@Composable
private fun MatchingView(e: Exercise.Matching, ctx: ExerciseContext, answered: Boolean, onAnswer: (Answer) -> Unit) {
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val left = remember(e.id) { e.pairs.indices.toList().seededShuffle(e.id + "L") }
    val right = remember(e.id) { e.pairs.indices.toList().seededShuffle(e.id + "R") }
    var pickedLeft by remember(e.id) { mutableIntStateOf(-1) }
    val matched = remember(e.id) { mutableStateListOf<Int>() }
    var mistakes by remember(e.id) { mutableIntStateOf(0) }
    var wrong by remember(e.id) { mutableStateOf(false) }
    Text(s[K.MATCH_INSTRUCTION], style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
    if (wrong) Banner(BannerKind.WARNING, s[K.MATCH_WRONG])
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            left.forEach { i ->
                MatchCell(e.pairs[i].first, ctx.language, selected = pickedLeft == i, done = i in matched) { if (i !in matched && !answered) { pickedLeft = i; wrong = false } }
            }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            right.forEach { j ->
                MatchCell(ctx.gloss(e.pairs[j].second), null, selected = false, done = j in matched) {
                    if (pickedLeft < 0 || j in matched || answered) return@MatchCell
                    if (pickedLeft == j) {
                        matched.add(j); pickedLeft = -1; wrong = false
                        if (matched.size == e.pairs.size) {
                            val score = (1.0 - mistakes.toDouble() / e.pairs.size).coerceIn(0.0, 1.0)
                            onAnswer(Answer(ExerciseOutcome(mistakes == 0, score, ""), "mistakes=$mistakes", 0, "matching"))
                        }
                    } else { mistakes++; wrong = true }
                }
            }
        }
    }
}

@Composable
private fun MatchCell(text: String, language: LanguageCode?, selected: Boolean, done: Boolean, onClick: () -> Unit) {
    val c = PolyglotTheme.colors
    val shape = RoundedCornerShape(12.dp)
    Box(
        Modifier.fillMaxWidth().heightIn(min = 56.dp).clip(shape)
            .background(when { done -> c.successContainer; selected -> c.primaryContainer; else -> c.card })
            .border(if (selected) 2.dp else 1.dp, when { done -> c.success; selected -> c.primary; else -> c.border }, shape)
            .clickable(enabled = !done, role = Role.Button, onClick = onClick)
            .semantics { this.selected = selected; if (done) stateDescription = "✓" }
            .padding(10.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (language != null) LearningText(text, language, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        else Text(text, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
    }
}

@Composable
private fun SpeakRepeatView(e: Exercise.SpeakRepeat, ctx: ExerciseContext, answered: Boolean, onAnswer: (Answer) -> Unit) {
    val app = LocalApp.current
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val scope = rememberCoroutineScope()
    val tag = Languages.of(ctx.language).defaultVariant.localeTag
    var availability by remember { mutableStateOf<SpeechAvailability?>(null) }
    var listening by remember(e.id) { mutableStateOf(false) }
    var heard by remember(e.id) { mutableStateOf<String?>(null) }
    var error by remember(e.id) { mutableStateOf<String?>(null) }
    var job by remember { mutableStateOf<Job?>(null) }
    LaunchedEffect(Unit) { availability = app.services.platform.recognizer.availability(tag) }
    PolyCard {
        LearningText(e.targetText, ctx.language, style = PolyglotTheme.learning.sentence, reading = if (ctx.showReading) e.pronunciationGuide else null)
        Text(ctx.gloss(e.translation), style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AudioButton(s[K.PLAY_AUDIO], { ctx.speaker.toggle(e.targetText, ctx.language) }, playing = ctx.speaker.playingText == e.targetText)
            AudioButton(s[K.LISTEN_SLOW], { ctx.speaker.play(e.targetText, ctx.language, 0.7f) })
        }
    }
    val canRecognize = availability == SpeechAvailability.AVAILABLE || availability == SpeechAvailability.PERMISSION_REQUIRED
    if (canRecognize && !answered) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(s[K.SPEAK_TAP_MIC], style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
            MicButton(
                if (listening) MicVisualState.LISTENING else MicVisualState.IDLE,
                label = if (listening) s[K.CONV_MIC_STOP] else s[K.CONV_MIC_START],
                stateLabel = if (listening) s[K.CONV_STATE_LISTENING] else s[K.CONV_STATE_READY],
                onClick = {
                    if (listening) { app.services.platform.recognizer.stopListening(); return@MicButton }
                    job?.cancel()
                    job = scope.launch {
                        if (availability == SpeechAvailability.PERMISSION_REQUIRED && !app.platformUi.requestMicrophonePermission()) {
                            availability = SpeechAvailability.PERMISSION_DENIED; return@launch
                        }
                        ctx.speaker.stop(); listening = true; error = null
                        try {
                            app.services.platform.recognizer.listen(tag).collect { ev ->
                                when (ev) {
                                    is RecognitionEvent.Final -> heard = ev.text
                                    is RecognitionEvent.Error -> error = Labels.speechError(s, ev.kind)
                                    else -> {}
                                }
                            }
                        } finally { listening = false }
                    }
                },
            )
            if (listening) QuietButton(s[K.CONV_CANCEL_RECORDING], { job?.cancel(); listening = false })
        }
    }
    error?.let { Banner(BannerKind.WARNING, it) }
    heard?.let { h ->
        val match = TextNormalizer.similarity(h, e.targetText)
        PolyCard {
            Text(s.f(K.SPEAK_HEARD, h), style = MaterialTheme.typography.bodyLarge)
            Text(s.f(K.SPEAK_MATCH, (match * 100).toInt()), style = MaterialTheme.typography.titleMedium)
            Text(s[K.SPEAK_DISCLAIMER], style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
        }
        if (!answered) ExerciseActions { Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SecondaryButton(s[K.SPEAK_TRY_AGAIN], { heard = null }, Modifier.weight(1f))
            PrimaryButton(s[K.CONTINUE], { onAnswer(Answer(ExerciseOutcome(match >= 0.75, match, e.targetText), h, 0, e.targetText)) }, Modifier.weight(1f))
        } }
    }
    if (!canRecognize && availability != null && !answered) {
        Banner(BannerKind.INFO, s[K.SPEAK_UNAVAILABLE])
        // Nothing here can hear the learner, so saying it aloud is practice and earns no score.
        ExerciseActions {
            PrimaryButton(s[K.SPEAK_SELF_DONE], { onAnswer(Answer(ExerciseOutcome(true, 1.0, e.targetText, counted = false), "", 0, e.targetText)) }, Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun ToneChoiceView(e: Exercise.ToneChoice, ctx: ExerciseContext, answered: Boolean, onAnswer: (Answer) -> Unit) {
    val s = LocalStrings.current
    var selected by remember(e.id) { mutableIntStateOf(0) }
    PolyCard {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            LearningText(e.character, LanguageCode.MANDARIN, style = PolyglotTheme.learning.hanzi, textAlign = TextAlign.Center)
            Text(e.syllable, style = PolyglotTheme.learning.pinyin)
            Text(ctx.gloss(e.meaning), style = MaterialTheme.typography.bodyMedium, color = PolyglotTheme.colors.textSecondary)
            AudioButton(s.f(K.TONE_LISTEN, e.character), { ctx.speaker.toggle(e.character, LanguageCode.MANDARIN) }, playing = ctx.speaker.playingText == e.character)
        }
    }
    fun choose(t: Int) {
        if (answered) return
        selected = t
        onAnswer(Answer(ExerciseOutcome(t == e.correctTone, if (t == e.correctTone) 1.0 else 0.0, Pinyin.applyTone(e.syllable, e.correctTone)), Pinyin.applyTone(e.syllable, t), 0, e.character))
    }
    if (!answered) ExerciseKeys(onNumber = { t -> if (t in 1..5) choose(t) })
    (1..5).forEach { t ->
        val marked = Pinyin.applyTone(e.syllable, t)
        ChoiceCard("$marked · ${s[K.valueOf("TONE_$t")]}", selected == t || (answered && t == e.correctTone), onClick = { choose(t) })
    }
}

/**
 * Write a character on a practice grid. The faded guide can be switched off to write from memory. A right
 * drawing scores 1, a close one half; a wrong one can be tried again.
 */
@Composable
private fun WriteCharacterView(e: Exercise.WriteCharacter, ctx: ExerciseContext, answered: Boolean, onAnswer: (Answer) -> Unit) {
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val ch = e.character.first()
    val entry = HanziData.of(ch)
    val strokes = entry?.strokes ?: StrokeOrder.of(ch)?.size ?: 0
    var showGuide by remember(e.id) { mutableStateOf(true) }
    PolyCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                LearningText(e.character, LanguageCode.MANDARIN, style = PolyglotTheme.learning.hanzi)
                LearningText(e.word, LanguageCode.MANDARIN, style = MaterialTheme.typography.titleMedium, reading = if (ctx.showReading) e.reading else null)
                Text(ctx.gloss(e.meaning), style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
            }
            AudioButton(s[K.PLAY_AUDIO], { ctx.speaker.toggle(e.word, LanguageCode.MANDARIN) }, playing = ctx.speaker.playingText == e.word)
        }
        Text(s.f(K.CHARS_STROKES, strokes), style = MaterialTheme.typography.labelLarge)
        if (entry != null && entry.components.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                entry.components.forEach { part ->
                    val role = when (part.role) { ComponentRole.SOUND -> s[K.CHARS_ROLE_SOUND]; ComponentRole.MEANING -> s[K.CHARS_ROLE_MEANING]; ComponentRole.OTHER -> s[K.CHARS_ROLE_PART] }
                    Pill("${part.form} · ${ctx.gloss(part.meaning)} · $role")
                }
            }
        }
    }
    PolyCard(contentPadding = 0.dp) { SwitchRow(s[K.CHARS_GUIDE], showGuide, { showGuide = it }, subtitle = s[K.CHARS_GUIDE_SUB]) }
    WritingPad(ch, strokes, traditional = false, showGuide = showGuide) { check ->
        if (answered) return@WritingPad
        val score = when { check.right -> 1.0; check.result.verdict == HandwritingVerdict.CLOSE -> 0.5; else -> 0.0 }
        onAnswer(Answer(ExerciseOutcome(check.right, score, e.character), e.character, 0, e.character))
    }
}

@Composable
private fun FreeWritingView(e: Exercise.FreeWriting, ctx: ExerciseContext, answered: Boolean, onAnswer: (Answer) -> Unit) {
    val app = LocalApp.current
    val s = LocalStrings.current
    var text by remember(e.id) { mutableStateOf("") }
    val words = app.services.writing.countWords(text, ctx.language)
    Text(ctx.gloss(e.prompt), style = MaterialTheme.typography.titleLarge)
    if (e.suggestedVocabulary.isNotEmpty()) Text(s.f(K.WRITE_SUGGESTED, e.suggestedVocabulary.joinToString(", ")), color = PolyglotTheme.colors.textSecondary)
    AppTextField(text, { text = it }, s[K.WRITING_PLACEHOLDER], singleLine = false, minLines = 5, supporting = s.f(K.WRITE_COUNT, words, e.minWords))
    if (!answered) ExerciseActions { PrimaryButton(s[K.WRITE_SUBMIT], {
        val fb = app.services.writing.offlineFeedback(text, e, ctx.language)
        // A word count says nothing about whether the text is right, so writing is practice and earns no score.
        onAnswer(Answer(ExerciseOutcome(fb.wordCount >= e.minWords, 1.0, "", counted = false), text, 0, e.prompt.en))
    }, Modifier.fillMaxWidth(), enabled = text.isNotBlank()) }
}

/** The rule, in the moment it is about to be used, instead of on a page before the lesson that many would skip. */
@Composable
private fun GrammarNoteView(e: Exercise.GrammarNote, ctx: ExerciseContext, answered: Boolean, onAnswer: (Answer) -> Unit) {
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val topic = Curriculum.grammar(e.topicId)
    if (topic != null) PolyCard {
        Text(ctx.gloss(topic.title), style = MaterialTheme.typography.titleMedium)
        Text(ctx.gloss(topic.rule), style = MaterialTheme.typography.bodyLarge)
        topic.examples.filter { !it.isIncorrect }.take(3).forEach { ex ->
            LearningText(ex.sentence, ctx.language, style = MaterialTheme.typography.bodyLarge)
            Text(ctx.gloss(ex.translation), style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
        }
    }
    val next = { onAnswer(Answer(ExerciseOutcome(true, 1.0, "", counted = false), "", 0, e.topicId)) }
    if (!answered) {
        ExerciseKeys(onEnter = next)
        ExerciseActions { PrimaryButton(s[K.CONTINUE], next, Modifier.fillMaxWidth()) }
    }
}

/** Small lightbulb row for explanations after answering. */
@Composable
fun ExplanationRow(text: String) {
    Row(verticalAlignment = Alignment.Top, modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = text }) {
        Icon(Icons.Filled.Lightbulb, null, tint = PolyglotTheme.colors.accent)
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}
