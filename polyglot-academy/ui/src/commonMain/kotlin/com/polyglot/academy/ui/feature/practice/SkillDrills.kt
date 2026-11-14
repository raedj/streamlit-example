package com.polyglot.academy.ui.feature.practice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.polyglot.academy.core.content.Curriculum
import com.polyglot.academy.core.drills.ComparedToken
import com.polyglot.academy.core.drills.DialogueCheck
import com.polyglot.academy.core.drills.GuidedDialogue
import com.polyglot.academy.core.drills.ListeningPace
import com.polyglot.academy.core.drills.SentenceCompare
import com.polyglot.academy.core.drills.SentenceComparison
import com.polyglot.academy.core.drills.SentenceDrills
import com.polyglot.academy.core.drills.SentenceItem
import com.polyglot.academy.core.drills.SpeedDrills
import com.polyglot.academy.core.drills.SprintResult
import com.polyglot.academy.core.drills.TokenStatus
import com.polyglot.academy.core.model.CefrLevel
import com.polyglot.academy.core.model.Exercise
import com.polyglot.academy.core.model.Gloss
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.Languages
import com.polyglot.academy.core.model.RolePlayScenario
import com.polyglot.academy.core.model.SessionKind
import com.polyglot.academy.core.model.Skill
import com.polyglot.academy.core.service.ExerciseOutcome
import com.polyglot.academy.core.service.WordQuizQuestion
import com.polyglot.academy.core.service.WritingFeedback
import com.polyglot.academy.core.speech.RecognitionEvent
import com.polyglot.academy.core.speech.SpeechAvailability
import com.polyglot.academy.core.wordlist.WordEntry
import com.polyglot.academy.core.wordlist.WordLevels
import com.polyglot.academy.core.wordlist.WordLists
import com.polyglot.academy.ui.app.LocalApp
import com.polyglot.academy.ui.app.ScrollScreen
import com.polyglot.academy.ui.app.Speaker
import com.polyglot.academy.ui.app.rememberSpeaker
import com.polyglot.academy.ui.designsystem.AppTextField
import com.polyglot.academy.ui.designsystem.AudioButton
import com.polyglot.academy.ui.designsystem.Banner
import com.polyglot.academy.ui.designsystem.BannerKind
import com.polyglot.academy.ui.designsystem.ChipGroup
import com.polyglot.academy.ui.designsystem.ChoiceCard
import com.polyglot.academy.ui.designsystem.FeedbackPanel
import com.polyglot.academy.ui.designsystem.LearningText
import com.polyglot.academy.ui.designsystem.LevelBadge
import com.polyglot.academy.ui.designsystem.MicButton
import com.polyglot.academy.ui.designsystem.MicVisualState
import com.polyglot.academy.ui.designsystem.PolyCard
import com.polyglot.academy.ui.designsystem.PolyglotTheme
import com.polyglot.academy.ui.designsystem.PrimaryButton
import com.polyglot.academy.ui.designsystem.QuietButton
import com.polyglot.academy.ui.designsystem.SecondaryButton
import com.polyglot.academy.ui.designsystem.SectionHeader
import com.polyglot.academy.ui.designsystem.StateMessage
import com.polyglot.academy.ui.feature.lesson.Answer
import com.polyglot.academy.ui.feature.lesson.ExerciseContext
import com.polyglot.academy.ui.feature.lesson.ExerciseView
import com.polyglot.academy.ui.i18n.K
import com.polyglot.academy.ui.i18n.Labels
import com.polyglot.academy.ui.i18n.LocalStrings
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/*
 * Practice for listening to whole sentences, guided conversation, writing sentences and speed
 * (core SkillDrills.kt). All of it uses the word-list examples and role-play scenarios already in the app.
 */

// ---------------- Shared ----------------

/** Word level to practise: the learner's current word level until they pick another. */
@Composable
private fun rememberDrillLevel(lang: LanguageCode): Pair<Int?, (Int) -> Unit> {
    val app = LocalApp.current
    var picked by remember(lang) { mutableStateOf<Int?>(null) }
    val current by produceState<Int?>(null, lang) { value = runCatching { app.services.wordLevels.currentLevel(lang).level }.getOrDefault(1) }
    return (picked ?: current) to { n: Int -> picked = n }
}

@Composable
private fun LevelPicker(level: Int, onPick: (Int) -> Unit) {
    val s = LocalStrings.current
    Text(s[K.DRILL_LEVEL], style = MaterialTheme.typography.labelLarge)
    ChipGroup((1..WordLevels.COUNT).toList(), setOf(level), { "$it · ${WordLevels.cefr(it).label}" }, onPick)
}

@Composable
private fun voiceAvailable(lang: LanguageCode): Boolean? {
    val app = LocalApp.current
    val v by produceState<Boolean?>(null, lang) { value = app.services.platform.synthesizer.isLanguageAvailable(Languages.of(lang).defaultVariant.localeTag) }
    return v
}

@Composable
private fun RoundProgress(index: Int, total: Int) {
    val s = LocalStrings.current
    Text(s.f(K.DRILL_PROGRESS, index + 1, total), style = MaterialTheme.typography.labelLarge, color = PolyglotTheme.colors.textSecondary)
    LinearProgressIndicator(progress = { (index + 1).toFloat() / total }, modifier = Modifier.fillMaxWidth())
}

/** The sentence shown after the question, with its meaning and a replay button. */
@Composable
private fun SentenceReveal(sentence: String, translation: Gloss, lang: LanguageCode, explain: LanguageCode, speaker: Speaker) {
    val s = LocalStrings.current
    PolyCard {
        Text(s[K.DRILL_SENTENCE_WAS], style = MaterialTheme.typography.labelMedium, color = PolyglotTheme.colors.textSecondary)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AudioButton(s[K.PLAY_AUDIO], { speaker.toggle(sentence, lang) }, playing = speaker.playingText == sentence)
            com.polyglot.academy.ui.feature.common.TappableSentence(sentence, lang, speaker, Modifier.weight(1f))
        }
        Text(translation.forLanguage(explain), style = MaterialTheme.typography.bodyMedium, color = PolyglotTheme.colors.textSecondary)
    }
}

// ---------------- Listening to whole sentences ----------------

@Composable
fun SentenceListeningScreen() {
    val app = LocalApp.current
    val s = LocalStrings.current
    val scope = rememberCoroutineScope()
    val lang = app.profile.activeLearningLanguage
    val explain = explainFor(lang, app.profile.explanationLanguage)
    val speaker = rememberSpeaker(s)
    val (level, pick) = rememberDrillLevel(lang)
    val voice = voiceAvailable(lang)
    var round by remember { mutableStateOf<List<Exercise>?>(null) }
    var index by remember { mutableIntStateOf(0) }
    var correct by remember { mutableIntStateOf(0) }
    var last by remember { mutableStateOf<Answer?>(null) }
    var practice by remember { mutableStateOf(app.services.study.practice(lang, SessionKind.LISTENING, "drill:sentences")) }
    val keys = remember { com.polyglot.academy.ui.feature.lesson.ExerciseKeySlot() }
    val inRound = round?.let { index < it.size } == true
    com.polyglot.academy.ui.designsystem.KeyShortcuts(Triple(round, index, last != null), onEnter = {
        when {
            inRound && last != null -> { index++; last = null; true }
            inRound -> keys.enter()
            else -> false
        }
    }, onNumber = { n -> inRound && last == null && keys.number(n) }) {
    CompositionLocalProvider(com.polyglot.academy.ui.feature.lesson.LocalExerciseKeys provides keys) {
    ScrollScreen(s[K.PRACTICE_SENT_LISTEN]) {
        if (voice == false) Banner(BannerKind.WARNING, s.f(K.LISTEN_NO_VOICE, Labels.language(s, lang)))
        val set = round
        if (set == null || index >= set.size) {
            Text(s[K.DRILL_LISTEN_INTRO], style = MaterialTheme.typography.bodyMedium, color = PolyglotTheme.colors.textSecondary)
            if (set != null) FeedbackPanel(correct * 2 >= set.size, s.f(K.DRILL_DONE, correct, set.size), null, cue = false)
            if (level == null) return@ScrollScreen
            LevelPicker(level, pick)
            PrimaryButton(s[if (set == null) K.DRILL_START else K.DRILL_AGAIN], {
                scope.launch {
                    round = SentenceDrills.listening(WordLists.load(lang), level, ROUND, app.services.clock.nowMillis())
                    index = 0; correct = 0; last = null
                    practice = app.services.study.practice(lang, SessionKind.LISTENING, "drill:sentences:$level")
                }
            }, Modifier.fillMaxWidth())
            if (set != null && set.isEmpty()) StateMessage(s[K.NOT_ENOUGH_DATA], "")
            return@ScrollScreen
        }
        RoundProgress(index, set.size)
        val item = set[index]
        val ctx = ExerciseContext(lang, explain, speaker, hintsAvailable = false, showReading = false, traditional = false)
        PolyCard {
            ExerciseView(item, ctx, last != null) { a ->
                last = a
                if (a.outcome.correct) correct++
                // Without a voice the learner could not hear the sentence, so the answer is not recorded.
                if (voice != false) scope.launch {
                    app.services.study.recordExercise(lang, null, item, a.outcome, a.userAnswer, 0, 0, a.promptText)
                    practice.answer(a.outcome.correct)
                }
            }
        }
        last?.let { a ->
            val sentence = (item as? Exercise.Dictation)?.audioText ?: (item as Exercise.MultipleChoice).audioText.orEmpty()
            if (item is Exercise.Dictation) FeedbackPanel(a.outcome.correct, s[if (a.outcome.correct) K.FEEDBACK_CORRECT else K.FEEDBACK_INCORRECT], null)
            SentenceReveal(sentence, item.explanation ?: Gloss("", ""), lang, explain, speaker)
            PrimaryButton(s[K.DRILL_NEXT], { index++; last = null }, Modifier.fillMaxWidth())
        }
        if (voice == false && last == null) SecondaryButton(s[K.LISTEN_SKIP], { index++ }, Modifier.fillMaxWidth())
    }
    } }
}

// ---------------- Writing sentences ----------------

@Composable
fun SentenceWritingScreen() {
    val app = LocalApp.current
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val scope = rememberCoroutineScope()
    val lang = app.profile.activeLearningLanguage
    val explain = explainFor(lang, app.profile.explanationLanguage)
    val speaker = rememberSpeaker(s)
    val (level, pick) = rememberDrillLevel(lang)
    var round by remember { mutableStateOf<List<SentenceItem>?>(null) }
    var index by remember { mutableIntStateOf(0) }
    var exact by remember { mutableIntStateOf(0) }
    var text by remember(round, index) { mutableStateOf("") }
    var compared by remember(round, index) { mutableStateOf<SentenceComparison?>(null) }
    var ai by remember(round, index) { mutableStateOf<WritingFeedback?>(null) }
    var aiError by remember(round, index) { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    fun checkWriting(item: SentenceItem) {
        if (text.isBlank() || compared != null) return
        val cmp = SentenceCompare.compare(text, item.sentence, lang)
        compared = cmp
        if (cmp.exact) {
            exact++
            // Only a sentence equal to the model is known to be right, so only that one is recorded.
            val ex = Exercise.Translate("drill_write_${item.id}", Skill.WRITING, Gloss("Write the sentence.", "اكتب الجملة."), item.translation, listOf(item.sentence))
            scope.launch { app.services.study.recordExercise(lang, null, ex, ExerciseOutcome(true, 1.0, item.sentence), text, 0, 0, item.translation.en) }
        }
    }
    val current = round?.getOrNull(index)
    com.polyglot.academy.ui.designsystem.KeyShortcuts(Triple(round, index, compared != null), onEnter = {
        when {
            current == null -> false
            compared != null -> { index++; true }
            text.isNotBlank() -> { checkWriting(current); true }
            else -> false
        }
    }) {
    ScrollScreen(s[K.PRACTICE_SENT_WRITE]) {
        val set = round
        if (set == null || index >= set.size) {
            Text(s.f(K.DRILL_WRITE_INTRO, Labels.language(s, lang)), style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
            if (set != null && set.isNotEmpty()) FeedbackPanel(true, s.f(K.DRILL_WRITE_DONE, exact, set.size), null, cue = false)
            if (level == null) return@ScrollScreen
            LevelPicker(level, pick)
            PrimaryButton(s[if (set == null) K.DRILL_START else K.DRILL_AGAIN], {
                scope.launch { round = SentenceDrills.writing(WordLists.load(lang), level, WRITE_ROUND, app.services.clock.nowMillis()); index = 0; exact = 0 }
            }, Modifier.fillMaxWidth())
            return@ScrollScreen
        }
        RoundProgress(index, set.size)
        val item = set[index]
        PolyCard {
            Text(item.translation.forLanguage(explain), style = MaterialTheme.typography.headlineSmall)
            Text(s.f(K.DRILL_WRITE_USE, item.entry.term), style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
        }
        AppTextField(text, { text = it }, s[K.DRILL_WRITE_PLACEHOLDER], singleLine = false, minLines = 3, onSubmit = { checkWriting(item) },
            textStyle = com.polyglot.academy.ui.designsystem.styleFor(lang, MaterialTheme.typography.bodyLarge))
        if (compared == null) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PrimaryButton(s[K.DRILL_WRITE_CHECK], { checkWriting(item) }, Modifier.weight(1f), enabled = text.isNotBlank())
            }
        }
        compared?.let { cmp ->
            if (cmp.exact) FeedbackPanel(true, s[K.DRILL_WRITE_EXACT], null)
            else {
                PolyCard {
                    Text(s.f(K.DRILL_WRITE_DIFFERENT, cmp.missing.size + cmp.extra.size, cmp.modelLength), style = MaterialTheme.typography.bodyMedium)
                    Text(s[K.DRILL_WRITE_YOURS], style = MaterialTheme.typography.labelMedium, color = c.textSecondary)
                    Text(marked(cmp.tokens.filter { it.status != TokenStatus.MISSING }, lang, c.error), style = PolyglotTheme.learning.sentence)
                    Text(s[K.DRILL_WRITE_MODEL], style = MaterialTheme.typography.labelMedium, color = c.textSecondary)
                    Text(marked(cmp.tokens.filter { it.status != TokenStatus.EXTRA }, lang, c.success), style = PolyglotTheme.learning.sentence)
                }
            }
            SentenceReveal(item.sentence, item.translation, lang, explain, speaker)
            if (!cmp.exact && ai == null) SecondaryButton(s[K.DRILL_WRITE_AI], {
                loading = true
                scope.launch {
                    val prompt = Exercise.FreeWriting(
                        "drill_write_ai_${item.id}", Skill.WRITING, Gloss("Write the sentence.", "اكتب الجملة."),
                        Gloss("Write in ${Languages.of(lang).englishName} a sentence meaning: \"${item.translation.en}\" (a model answer is \"${item.sentence}\"; other correct wordings are fine).",
                            "اكتب جملة معناها: «${item.translation.ar}»"),
                        minWords = 1, suggestedVocabulary = listOf(item.entry.term),
                    )
                    app.services.writing.aiFeedback(text, prompt, lang, explain, app.profile.enrollment(lang)?.startingLevel ?: CefrLevel.A1)
                        .onSuccess { ai = it; aiError = null }
                        .onFailure { e ->
                            aiError = if (e is com.polyglot.academy.core.ai.AiError.BlockedByPrivacy || e is com.polyglot.academy.core.ai.AiError.NoProviderAvailable) s[K.WRITING_AI_BLOCKED]
                            else s.f(K.WRITING_AI_FAILED, e.message ?: "")
                        }
                    loading = false
                }
            }, Modifier.fillMaxWidth(), enabled = !loading)
            aiError?.let { Banner(BannerKind.WARNING, it) }
            ai?.let { WritingFeedbackView(it, lang) }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (!cmp.exact) SecondaryButton(s[K.SPEAK_TRY_AGAIN], { compared = null; ai = null }, Modifier.weight(1f))
                PrimaryButton(s[K.DRILL_NEXT], { index++ }, Modifier.weight(1f))
            }
        }
    }
    }
}

/** Tokens joined back into a sentence; tokens that are not shared get [color] and a mark. */
private fun marked(tokens: List<ComparedToken>, lang: LanguageCode, color: androidx.compose.ui.graphics.Color) = buildAnnotatedString {
    val sep = if (lang == LanguageCode.MANDARIN) "" else " "
    tokens.forEachIndexed { i, t ->
        if (i > 0) append(sep)
        if (t.status == TokenStatus.MATCH) append(t.text)
        else withStyle(SpanStyle(color = color, fontWeight = FontWeight.Bold, textDecoration = if (t.status == TokenStatus.EXTRA) TextDecoration.LineThrough else TextDecoration.Underline)) { append(t.text) }
    }
}

// ---------------- Speed ----------------

private enum class SpeedMode { WORDS, LISTENING }

@Composable
fun SpeedDrillScreen() {
    val app = LocalApp.current
    val s = LocalStrings.current
    val lang = app.profile.activeLearningLanguage
    val (level, pick) = rememberDrillLevel(lang)
    var mode by remember { mutableStateOf(SpeedMode.WORDS) }
    var running by remember { mutableStateOf(false) }
    ScrollScreen(s[K.PRACTICE_SPEED]) {
        if (!running) {
            ChipGroup(SpeedMode.entries, setOf(mode), { s[if (it == SpeedMode.WORDS) K.DRILL_SPEED_WORDS else K.DRILL_SPEED_LISTEN] }, { mode = it })
            if (level == null) return@ScrollScreen
            LevelPicker(level, pick)
        }
        if (level == null) return@ScrollScreen
        when (mode) {
            SpeedMode.WORDS -> WordSprint(lang, level) { running = it }
            SpeedMode.LISTENING -> FastListening(lang, level) { running = it }
        }
    }
}

@Composable
private fun WordSprint(lang: LanguageCode, level: Int, onRunning: (Boolean) -> Unit) {
    val app = LocalApp.current
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val scope = rememberCoroutineScope()
    val explain = explainFor(lang, app.profile.explanationLanguage)
    var questions by remember(level) { mutableStateOf<List<WordQuizQuestion>?>(null) }
    var index by remember(level) { mutableIntStateOf(0) }
    var left by remember(level) { mutableIntStateOf(SpeedDrills.SPRINT_SECONDS) }
    var correct by remember(level) { mutableIntStateOf(0) }
    val missed = remember(level) { mutableStateListOf<WordEntry>() }
    var result by remember(level) { mutableStateOf<Pair<SprintResult, Int?>?>(null) }
    val best by produceState<Int?>(null, level, result) { value = app.services.drills.bestSprint(lang, level) }
    val qs = questions
    if (qs == null || result != null) {
        Text(s[K.DRILL_SPRINT_INTRO], style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
        result?.let { (r, before) ->
            FeedbackPanel(true, s.f(K.DRILL_SPRINT_RESULT, r.correct, (r.accuracy * 100).toInt()),
                if (before == null || r.correct > before) s[K.DRILL_SPRINT_RECORD] else s.f(K.DRILL_SPRINT_BEST, before), cue = false)
            if (missed.isNotEmpty()) PolyCard {
                Text(s[K.DRILL_SPRINT_MISSED], style = MaterialTheme.typography.labelLarge)
                missed.distinctBy { it.id }.forEach { e -> Text("${e.term} — ${e.meaning.forLanguage(explain)}", style = MaterialTheme.typography.bodyMedium) }
            }
        } ?: best?.let { Text(s.f(K.DRILL_SPRINT_BEST, it), style = MaterialTheme.typography.bodyMedium) }
        PrimaryButton(s[if (result == null) K.DRILL_START else K.DRILL_AGAIN], {
            scope.launch {
                questions = app.services.wordLevels.quiz(lang, level, size = SPRINT_POOL)
                index = 0; correct = 0; missed.clear(); left = SpeedDrills.SPRINT_SECONDS; result = null
                onRunning(true)
            }
        }, Modifier.fillMaxWidth())
        return
    }
    LaunchedEffect(qs) {
        while (left > 0) { delay(1000); left-- }
        val r = SprintResult(correct, missed.size, SpeedDrills.SPRINT_SECONDS)
        result = r to app.services.drills.finishSprint(lang, level, r, missed.toList())
        onRunning(false)
    }
    Text(s.f(K.DRILL_SECONDS_LEFT, left), style = MaterialTheme.typography.titleLarge, color = if (left <= 10) c.error else c.textPrimary)
    LinearProgressIndicator(progress = { left.toFloat() / SpeedDrills.SPRINT_SECONDS }, modifier = Modifier.fillMaxWidth())
    val q = qs[index % qs.size]
    val cues = com.polyglot.academy.ui.designsystem.LocalAnswerCues.current
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    fun choose(i: Int) {
        if (left <= 0 || i !in q.options.indices) return
        val right = i == q.answerIndex
        if (right) correct++ else missed += q.entry
        // A wrong answer is felt and heard at once, so the learner notices it without stopping.
        if (cues.haptics && !right) haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.Reject)
        cues.playSound(right)
        index++
    }
    com.polyglot.academy.ui.designsystem.KeyShortcuts(Unit, onEnter = { false }, onNumber = { n -> choose(n - 1); true }) {
        Column(verticalArrangement = Arrangement.spacedBy(PolyglotTheme.spacing.lg)) {
            PolyCard {
                LearningText(q.entry.term, lang, style = if (lang == LanguageCode.MANDARIN) PolyglotTheme.learning.hanzi else PolyglotTheme.learning.term)
            }
            q.options.forEachIndexed { i, o -> ChoiceCard(o.meaning.forLanguage(explain), false, onClick = { choose(i) }) }
        }
    }
}

@Composable
private fun FastListening(lang: LanguageCode, level: Int, onRunning: (Boolean) -> Unit) {
    val app = LocalApp.current
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val scope = rememberCoroutineScope()
    val explain = explainFor(lang, app.profile.explanationLanguage)
    val speaker = rememberSpeaker(s)
    val voice = voiceAvailable(lang)
    var round by remember(level) { mutableStateOf<List<Exercise.MultipleChoice>?>(null) }
    var index by remember(level) { mutableIntStateOf(0) }
    var chosen by remember(level, index) { mutableIntStateOf(-1) }
    var pace by remember(level) { mutableStateOf(ListeningPace()) }
    var correct by remember(level) { mutableIntStateOf(0) }
    var practice by remember { mutableStateOf(app.services.study.practice(lang, SessionKind.SPEED, SpeedDrills.listeningReference(level))) }
    if (voice == false) { Banner(BannerKind.WARNING, s.f(K.LISTEN_NO_VOICE, Labels.language(s, lang))); return }
    val set = round
    if (set == null || index >= set.size) {
        Text(s[K.DRILL_FAST_INTRO], style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
        if (set != null && set.isNotEmpty()) FeedbackPanel(correct * 2 >= set.size, s.f(K.DRILL_DONE, correct, set.size),
            s.f(K.DRILL_FAST_RATE, (pace.rate * 100).toInt(), (pace.best * 100).toInt()), cue = false)
        PrimaryButton(s[if (set == null) K.DRILL_START else K.DRILL_AGAIN], {
            scope.launch {
                round = SentenceDrills.listening(WordLists.load(lang), level, ROUND, app.services.clock.nowMillis(), dictation = false).filterIsInstance<Exercise.MultipleChoice>()
                index = 0; correct = 0; pace = ListeningPace(pace.rate)
                practice = app.services.study.practice(lang, SessionKind.SPEED, SpeedDrills.listeningReference(level))
                onRunning(true)
            }
        }, Modifier.fillMaxWidth())
        return
    }
    RoundProgress(index, set.size)
    Text(s.f(K.DRILL_FAST_RATE, (pace.rate * 100).toInt(), (pace.best * 100).toInt()), style = MaterialTheme.typography.titleMedium)
    val q = set[index]
    val sentence = q.audioText.orEmpty()
    LaunchedEffect(q.id) { speaker.play(sentence, lang, pace.rate) }
    AudioButton(s[K.PLAY_AUDIO], { speaker.play(sentence, lang, pace.rate) }, playing = speaker.playingText == sentence)
    val cues = com.polyglot.academy.ui.designsystem.LocalAnswerCues.current
    fun choose(i: Int) {
        if (chosen >= 0 || i !in q.options.indices) return
        chosen = i
        val right = i == q.correctIndex
        if (right) correct++
        pace.answer(right)
        cues.playSound(right)
        scope.launch {
            app.services.study.recordExercise(lang, null, q, ExerciseOutcome(right, if (right) 1.0 else 0.0, q.optionGlosses!![q.correctIndex].en), q.options[i], 0, 0, sentence)
            practice.answer(right)
        }
    }
    fun next() {
        index++
        if (index >= set.size) onRunning(false)
    }
    com.polyglot.academy.ui.designsystem.KeyShortcuts(Pair(index, chosen >= 0), onEnter = { if (chosen >= 0) { next(); true } else false }, onNumber = { n -> if (chosen < 0) { choose(n - 1); true } else false }) {
        Column(verticalArrangement = Arrangement.spacedBy(PolyglotTheme.spacing.lg)) {
            q.optionGlosses.orEmpty().forEachIndexed { i, g ->
                ChoiceCard(g.forLanguage(explain), chosen == i || (chosen >= 0 && i == q.correctIndex),
                    subtitle = when { chosen < 0 -> null; i == q.correctIndex -> s[K.FEEDBACK_CORRECT]; i == chosen -> s[K.FEEDBACK_INCORRECT]; else -> null },
                    onClick = { choose(i) })
            }
            if (chosen >= 0) {
                SentenceReveal(sentence, q.explanation ?: Gloss("", ""), lang, explain, speaker)
                PrimaryButton(s[K.DRILL_NEXT], { next() }, Modifier.fillMaxWidth())
            }
        }
    }
}

// ---------------- Guided conversation ----------------

@Composable
fun GuidedDialogueScreen() {
    val app = LocalApp.current
    val s = LocalStrings.current
    val lang = app.profile.activeLearningLanguage
    val explain = explainFor(lang, app.profile.explanationLanguage)
    var scenario by remember(lang) { mutableStateOf<RolePlayScenario?>(null) }
    ScrollScreen(s[K.PRACTICE_GUIDED]) {
        val sc = scenario
        if (sc == null) {
            Text(s[K.DRILL_GUIDED_INTRO], style = MaterialTheme.typography.bodyMedium, color = PolyglotTheme.colors.textSecondary)
            SectionHeader(s[K.DRILL_GUIDED_CHOOSE])
            val all = Curriculum.of(lang).scenarios.sortedBy { it.level.ordinal }
            if (all.isEmpty()) StateMessage(s[K.NOT_ENOUGH_DATA], "")
            all.forEach { x ->
                ChoiceCard(x.title.forLanguage(explain), false, onClick = { scenario = x }, subtitle = x.setting.forLanguage(explain), leading = { LevelBadge(x.level, null) })
            }
        } else GuidedRun(sc, lang, explain) { scenario = null }
    }
}

@Composable
private fun GuidedRun(scenario: RolePlayScenario, lang: LanguageCode, explain: LanguageCode, onExit: () -> Unit) {
    val app = LocalApp.current
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val scope = rememberCoroutineScope()
    val speaker = rememberSpeaker(s)
    val steps = remember(scenario.id) { GuidedDialogue.steps(scenario) }
    val tag = Languages.of(lang).defaultVariant.localeTag
    var index by remember(scenario.id) { mutableIntStateOf(0) }
    var passed by remember(scenario.id) { mutableIntStateOf(0) }
    var shownAt by remember(scenario.id, index) { mutableStateOf(app.services.clock.nowMillis()) }
    var showText by remember(scenario.id, index) { mutableStateOf(false) }
    var text by remember(scenario.id, index) { mutableStateOf("") }
    var check by remember(scenario.id, index) { mutableStateOf<Pair<DialogueCheck, Int>?>(null) }
    var counted by remember(scenario.id, index) { mutableStateOf(false) }
    var availability by remember { mutableStateOf<SpeechAvailability?>(null) }
    var listening by remember { mutableStateOf(false) }
    var micError by remember(index) { mutableStateOf<String?>(null) }
    var job by remember { mutableStateOf<Job?>(null) }
    val practice = remember(scenario.id) { app.services.study.practice(lang, SessionKind.CONVERSATION, "drill:guided:${scenario.id}") }
    LaunchedEffect(Unit) { availability = app.services.platform.recognizer.availability(tag) }
    Text(scenario.title.forLanguage(explain), style = MaterialTheme.typography.titleLarge)
    if (index >= steps.size) {
        GuidedDialogue.closingLine(scenario)?.let { (line, tr) -> SentenceReveal(line, tr, lang, explain, speaker) }
        FeedbackPanel(passed * 2 >= steps.size, s.f(K.DRILL_GUIDED_DONE, passed, steps.size), null, cue = false)
        PrimaryButton(s[K.DRILL_AGAIN], { index = 0; passed = 0 }, Modifier.fillMaxWidth())
        SecondaryButton(s[K.DRILL_GUIDED_CHOOSE], onExit, Modifier.fillMaxWidth())
        return
    }
    RoundProgress(index, steps.size)
    val step = steps[index]
    LaunchedEffect(scenario.id, index) { speaker.play(step.partnerLine, lang) }
    PolyCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AudioButton(s[K.PLAY_AUDIO], { speaker.toggle(step.partnerLine, lang) }, playing = speaker.playingText == step.partnerLine)
            AudioButton(s[K.LISTEN_SLOW], { speaker.play(step.partnerLine, lang, 0.7f) })
            if (!showText) QuietButton(s[K.DRILL_SHOW_TEXT], { showText = true })
        }
        if (showText) {
            com.polyglot.academy.ui.feature.common.TappableSentence(step.partnerLine, lang, speaker)
            Text(step.partnerTranslation.forLanguage(explain), style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
        }
    }
    Text(s.f(K.DRILL_HINT, step.hint.forLanguage(explain)), style = MaterialTheme.typography.titleMedium)
    fun submit(answer: String) {
        val result = GuidedDialogue.check(answer, step, lang)
        check = result to ((app.services.clock.nowMillis() - shownAt) / 1000).toInt()
        if (!counted) {
            counted = true
            if (result.passed) passed++
            scope.launch { practice.answer(result.passed) }
        }
    }
    if (check == null) {
        val canRecognize = availability == SpeechAvailability.AVAILABLE || availability == SpeechAvailability.PERMISSION_REQUIRED
        if (canRecognize) Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                        speaker.stop(); listening = true; micError = null
                        try {
                            app.services.platform.recognizer.listen(tag).collect { ev ->
                                when (ev) {
                                    is RecognitionEvent.Final -> { text = ev.text; submit(ev.text) }
                                    is RecognitionEvent.Error -> micError = Labels.speechError(s, ev.kind)
                                    else -> {}
                                }
                            }
                        } finally { listening = false }
                    }
                },
            )
        }
        micError?.let { Banner(BannerKind.WARNING, it) }
        AppTextField(text, { text = it }, s[K.DRILL_ANSWER_TYPE], singleLine = false, minLines = 2,
            textStyle = com.polyglot.academy.ui.designsystem.styleFor(lang, MaterialTheme.typography.bodyLarge))
        PrimaryButton(s[K.DRILL_CHECK], { submit(text) }, Modifier.fillMaxWidth(), enabled = text.isNotBlank())
        return
    }
    val (result, seconds) = check!!
    if (text.isNotBlank()) Text(s.f(K.SPEAK_HEARD, text), style = MaterialTheme.typography.bodyLarge)
    FeedbackPanel(result.passed, if (result.passed) s.f(K.DRILL_GUIDED_PASS, seconds) else s[K.DRILL_GUIDED_FAIL], null)
    PolyCard {
        Text(s[K.DRILL_MODEL_ANSWER], style = MaterialTheme.typography.labelMedium, color = c.textSecondary)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AudioButton(s[K.PLAY_AUDIO], { speaker.toggle(step.modelAnswer, lang) }, playing = speaker.playingText == step.modelAnswer)
            com.polyglot.academy.ui.feature.common.TappableSentence(step.modelAnswer, lang, speaker, Modifier.weight(1f))
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SecondaryButton(s[K.SPEAK_TRY_AGAIN], { check = null; text = ""; shownAt = app.services.clock.nowMillis() }, Modifier.weight(1f))
        PrimaryButton(s[K.DRILL_NEXT], { index++ }, Modifier.weight(1f))
    }
}

private const val ROUND = 10
private const val WRITE_ROUND = 8
/** More words than anyone answers in a minute, so the sprint does not repeat itself. */
private const val SPRINT_POOL = 80
