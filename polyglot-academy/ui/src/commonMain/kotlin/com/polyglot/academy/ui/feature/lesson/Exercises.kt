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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
) {
    fun gloss(g: Gloss) = g.forLanguage(explain)
}

@Composable
fun ExerciseView(exercise: Exercise, ctx: ExerciseContext, answered: Boolean, onAnswer: (Answer) -> Unit) {
    val s = LocalStrings.current
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(ctx.gloss(exercise.instruction), style = MaterialTheme.typography.titleMedium, color = PolyglotTheme.colors.textSecondary)
        when (exercise) {
            is Exercise.Flashcard -> FlashcardView(exercise, ctx, answered, onAnswer)
            is Exercise.MultipleChoice -> MultipleChoiceView(exercise, ctx, answered, onAnswer)
            is Exercise.FillInBlank -> TypedView(exercise.sentence, exercise.acceptedAnswers, exercise.hint, ctx, answered, onAnswer, sentenceInTarget = true)
            is Exercise.Translate -> {
                Text(s[K.TRANSLATE_PROMPT], style = MaterialTheme.typography.labelLarge, color = PolyglotTheme.colors.textSecondary)
                TypedView(ctx.gloss(exercise.source), exercise.acceptedAnswers, null, ctx, answered, onAnswer, sentenceInTarget = false)
            }
            is Exercise.Dictation -> DictationView(exercise, ctx, answered, onAnswer)
            is Exercise.SentenceOrder -> SentenceOrderView(exercise, ctx, answered, onAnswer)
            is Exercise.Matching -> MatchingView(exercise, ctx, answered, onAnswer)
            is Exercise.SpeakRepeat -> SpeakRepeatView(exercise, ctx, answered, onAnswer)
            is Exercise.ToneChoice -> ToneChoiceView(exercise, ctx, answered, onAnswer)
            is Exercise.FreeWriting -> FreeWritingView(exercise, ctx, answered, onAnswer)
        }
    }
}

@Composable
private fun FlashcardView(e: Exercise.Flashcard, ctx: ExerciseContext, answered: Boolean, onAnswer: (Answer) -> Unit) {
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val v = Curriculum.of(ctx.language).vocabularyById[e.vocabularyId] ?: return
    var revealed by remember(e.id) { mutableStateOf(false) }
    val term = if (ctx.traditional) v.traditional ?: v.term else v.term
    PolyCard(Modifier.heightIn(min = 220.dp)) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            LearningText(term, ctx.language, reading = if (ctx.showReading) v.pronunciation ?: v.transliteration else null, textAlign = TextAlign.Center,
                style = if (ctx.language == LanguageCode.MANDARIN) PolyglotTheme.learning.hanzi else PolyglotTheme.learning.term)
            AudioButton(s[K.PLAY_AUDIO], { ctx.speaker.toggle(v.term, ctx.language) }, playing = ctx.speaker.playingText == v.term)
            if (revealed) {
                Text(ctx.gloss(v.meaning), style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                v.example?.let { ex ->
                    Text(s[K.FLASHCARD_EXAMPLE], style = MaterialTheme.typography.labelMedium, color = c.textSecondary)
                    LearningText(ex, ctx.language, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                    v.exampleTranslation?.let { Text(ctx.gloss(it), style = MaterialTheme.typography.bodyMedium, color = c.textSecondary, textAlign = TextAlign.Center) }
                }
            }
        }
    }
    if (!revealed) PrimaryButton(s[K.FLASHCARD_REVEAL], { revealed = true }, Modifier.fillMaxWidth())
    else if (!answered) Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SecondaryButton(s[K.FLASHCARD_DIDNT], { onAnswer(Answer(ExerciseOutcome(false, 0.0, ctx.gloss(v.meaning)), "didn't know", 0, v.term)) }, Modifier.weight(1f))
        PrimaryButton(s[K.FLASHCARD_KNEW], { onAnswer(Answer(ExerciseOutcome(true, 1.0, ctx.gloss(v.meaning)), "knew", 0, v.term)) }, Modifier.weight(1f))
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
    } else {
        LearningText(e.prompt, ctx.language, style = PolyglotTheme.learning.sentence)
    }
    e.options.forEachIndexed { i, opt ->
        val label = e.optionGlosses?.getOrNull(i)?.let(ctx::gloss) ?: opt
        val state = when {
            !answered -> null
            i == e.correctIndex -> s[K.FEEDBACK_CORRECT]
            i == selected -> s[K.FEEDBACK_INCORRECT]
            else -> null
        }
        ChoiceCard(
            title = label, selected = selected == i || (answered && i == e.correctIndex),
            subtitle = state,
            onClick = {
                if (answered) return@ChoiceCard
                selected = i
                val correct = i == e.correctIndex
                val expected = e.optionGlosses?.getOrNull(e.correctIndex)?.let(ctx::gloss) ?: e.options[e.correctIndex]
                onAnswer(Answer(ExerciseOutcome(correct, if (correct) 1.0 else 0.0, expected), opt, 0, e.prompt))
            },
        )
    }
}

@Composable
private fun TypedView(
    prompt: String, accepted: List<String>, hint: Gloss?, ctx: ExerciseContext, answered: Boolean, onAnswer: (Answer) -> Unit, sentenceInTarget: Boolean,
) {
    val s = LocalStrings.current
    var text by remember(prompt) { mutableStateOf("") }
    var hints by remember(prompt) { mutableIntStateOf(0) }
    var empty by remember(prompt) { mutableStateOf(false) }
    if (sentenceInTarget) LearningText(prompt.replace("___", "_____"), ctx.language, style = PolyglotTheme.learning.sentence)
    else Text(prompt, style = MaterialTheme.typography.headlineSmall)
    AppTextField(text, { text = it; empty = false }, s[K.TYPE_ANSWER], isError = empty, supporting = if (empty) s[K.FEEDBACK_EMPTY] else null,
        textStyle = MaterialTheme.typography.bodyLarge.copy(localeList = com.polyglot.academy.ui.designsystem.localeFor(Languages.of(ctx.language).defaultVariant.localeTag),
            textDirection = com.polyglot.academy.ui.designsystem.textDirectionFor(ctx.language)))
    if (ctx.hintsAvailable && !answered) {
        if (hints == 0) QuietButton(s[K.HINT_SHOW], { hints = 1 })
        else Banner(BannerKind.INFO, hint?.let(ctx::gloss) ?: s.f(K.HINT_FIRST_LETTERS, accepted.first().take(maxOf(1, accepted.first().length / 3))))
    }
    if (!answered) PrimaryButton(s[K.CHECK], {
        val check = AnswerChecker.check(text, accepted)
        if (check.verdict == AnswerVerdict.EMPTY) { empty = true; return@PrimaryButton }
        onAnswer(Answer(ExerciseOutcome(check.isCorrect, check.score, check.closestAccepted, check), text, hints, prompt))
    }, Modifier.fillMaxWidth())
}

@Composable
private fun DictationView(e: Exercise.Dictation, ctx: ExerciseContext, answered: Boolean, onAnswer: (Answer) -> Unit) {
    val s = LocalStrings.current
    var text by remember(e.id) { mutableStateOf("") }
    LaunchedEffect(e.id) { ctx.speaker.play(e.audioText, ctx.language) }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        AudioButton(s[K.PLAY_AUDIO], { ctx.speaker.toggle(e.audioText, ctx.language) }, playing = ctx.speaker.playingText == e.audioText)
        SecondaryButton(s[K.LISTEN_SLOW], { ctx.speaker.play(e.audioText, ctx.language, 0.65f) })
    }
    AppTextField(text, { text = it }, s[K.TYPE_ANSWER], singleLine = false, minLines = 2)
    if (!answered) PrimaryButton(s[K.CHECK], {
        val check = AnswerChecker.check(text, e.acceptedAnswers)
        if (check.verdict == AnswerVerdict.EMPTY) return@PrimaryButton
        onAnswer(Answer(ExerciseOutcome(check.isCorrect, maxOf(check.score, TextNormalizer.similarity(text, e.audioText) * 0.8), e.audioText, check), text, 0, e.audioText))
    }, Modifier.fillMaxWidth())
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
    if (!answered) Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SecondaryButton(s[K.ORDER_RESET], { chosen.clear() }, Modifier.weight(1f))
        PrimaryButton(s[K.CHECK], {
            val answer = chosen.joinToString(e.separator) { e.tokens[it] }
            val expected = e.tokens.joinToString(e.separator)
            val correct = TextNormalizer.basic(answer) == TextNormalizer.basic(expected)
            onAnswer(Answer(ExerciseOutcome(correct, if (correct) 1.0 else 0.0, expected), answer, 0, ctx.gloss(e.translation)))
        }, Modifier.weight(1f), enabled = chosen.size == e.tokens.size)
    }
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
        if (!answered) Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SecondaryButton(s[K.SPEAK_TRY_AGAIN], { heard = null }, Modifier.weight(1f))
            PrimaryButton(s[K.CONTINUE], { onAnswer(Answer(ExerciseOutcome(match >= 0.75, match, e.targetText), h, 0, e.targetText)) }, Modifier.weight(1f))
        }
    }
    if (!canRecognize && availability != null && !answered) {
        Banner(BannerKind.INFO, s[K.SPEAK_UNAVAILABLE])
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SecondaryButton(s[K.SPEAK_SELF_RETRY], { onAnswer(Answer(ExerciseOutcome(false, 0.5, e.targetText), "self:retry", 0, e.targetText)) }, Modifier.weight(1f))
            PrimaryButton(s[K.SPEAK_SELF_GOOD], { onAnswer(Answer(ExerciseOutcome(true, 1.0, e.targetText), "self:good", 0, e.targetText)) }, Modifier.weight(1f))
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
    (1..5).forEach { t ->
        val marked = Pinyin.applyTone(e.syllable, t)
        ChoiceCard("$marked · ${s[K.valueOf("TONE_$t")]}", selected == t || (answered && t == e.correctTone), onClick = {
            if (answered) return@ChoiceCard
            selected = t
            onAnswer(Answer(ExerciseOutcome(t == e.correctTone, if (t == e.correctTone) 1.0 else 0.0, Pinyin.applyTone(e.syllable, e.correctTone)), marked, 0, e.character))
        })
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
    if (!answered) PrimaryButton(s[K.WRITE_SUBMIT], {
        val fb = app.services.writing.offlineFeedback(text, e, ctx.language)
        onAnswer(Answer(ExerciseOutcome(fb.wordCount >= e.minWords, (fb.wordCount.toDouble() / e.minWords).coerceAtMost(1.0), ""), text, 0, e.prompt.en))
    }, Modifier.fillMaxWidth(), enabled = text.isNotBlank())
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
