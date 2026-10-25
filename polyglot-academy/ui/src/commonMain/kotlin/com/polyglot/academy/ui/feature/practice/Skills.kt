package com.polyglot.academy.ui.feature.practice

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.polyglot.academy.core.content.Curriculum
import com.polyglot.academy.core.mandarin.Pinyin
import com.polyglot.academy.core.model.CefrLevel
import com.polyglot.academy.core.model.Exercise
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.SessionKind
import com.polyglot.academy.core.model.Skill
import com.polyglot.academy.core.relax.BreathPhaseKind
import com.polyglot.academy.core.relax.Relaxation
import com.polyglot.academy.core.service.ExerciseOutcome
import com.polyglot.academy.core.service.WritingCheck
import com.polyglot.academy.core.service.WritingFeedback
import com.polyglot.academy.ui.app.LocalApp
import com.polyglot.academy.ui.app.ScrollScreen
import com.polyglot.academy.ui.app.rememberSpeaker
import com.polyglot.academy.ui.designsystem.AppTextField
import com.polyglot.academy.ui.designsystem.AudioButton
import com.polyglot.academy.ui.designsystem.Banner
import com.polyglot.academy.ui.designsystem.BannerKind
import com.polyglot.academy.ui.designsystem.ChipGroup
import com.polyglot.academy.ui.designsystem.ChoiceCard
import com.polyglot.academy.ui.designsystem.FeedbackPanel
import com.polyglot.academy.ui.designsystem.HeroIllustration
import com.polyglot.academy.ui.designsystem.HeroKind
import com.polyglot.academy.ui.designsystem.LearningText
import com.polyglot.academy.ui.designsystem.LevelBadge
import com.polyglot.academy.ui.designsystem.Pill
import com.polyglot.academy.ui.designsystem.PolyCard
import com.polyglot.academy.ui.designsystem.PolyglotTheme
import com.polyglot.academy.ui.designsystem.PrimaryButton
import com.polyglot.academy.ui.designsystem.QuietButton
import com.polyglot.academy.ui.designsystem.SecondaryButton
import com.polyglot.academy.ui.designsystem.SectionHeader
import com.polyglot.academy.ui.designsystem.StateMessage
import com.polyglot.academy.ui.designsystem.SwitchRow
import com.polyglot.academy.ui.feature.lesson.Answer
import com.polyglot.academy.ui.feature.lesson.ExerciseContext
import com.polyglot.academy.ui.feature.lesson.ExerciseView
import com.polyglot.academy.ui.i18n.K
import com.polyglot.academy.ui.i18n.Labels
import com.polyglot.academy.ui.i18n.LocalStrings
import com.polyglot.academy.ui.navigation.Route
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

internal fun explainFor(lang: LanguageCode, preferred: LanguageCode) =
    preferred.takeIf { it != lang } ?: if (lang == LanguageCode.ENGLISH) LanguageCode.ARABIC else LanguageCode.ENGLISH

// ---------------- Grammar ----------------

@Composable
fun GrammarListScreen() {
    val app = LocalApp.current
    val s = LocalStrings.current
    val lang = app.profile.activeLearningLanguage
    ScrollScreen(s[K.GRAMMAR_TITLE]) {
        Curriculum.of(lang).grammar.sortedBy { it.level.ordinal }.forEach { g ->
            PolyCard(onClick = { app.nav.push(Route.GrammarTopic(g.id)) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(s.gloss(g.title), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    LevelBadge(g.level, null)
                }
                Text(s.gloss(g.rule), style = MaterialTheme.typography.bodySmall, color = PolyglotTheme.colors.textSecondary, maxLines = 2)
            }
        }
    }
}

@Composable
fun GrammarTopicScreen(topicId: String) {
    val app = LocalApp.current
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val topic = Curriculum.all.values.firstNotNullOfOrNull { it.grammarById[topicId] }
    val speaker = rememberSpeaker(s)
    ScrollScreen(topic?.let { s.gloss(it.title) } ?: s[K.GRAMMAR_TITLE]) {
        if (topic == null) { StateMessage(s[K.ERROR_TITLE], s[K.LESSON_NOT_FOUND]); return@ScrollScreen }
        LevelBadge(topic.level, null)
        SectionHeader(s[K.GRAMMAR_RULE])
        PolyCard { Text(s.gloss(topic.rule), style = MaterialTheme.typography.bodyLarge) }
        SectionHeader(s[K.GRAMMAR_EXAMPLES])
        topic.examples.forEach { ex ->
            PolyCard(containerColor = if (ex.isIncorrect) c.errorContainer else c.card) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (ex.isIncorrect) Icons.Filled.Cancel else Icons.Filled.CheckCircle, null, tint = if (ex.isIncorrect) c.error else c.success)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        if (ex.isIncorrect) Text(s[K.GRAMMAR_INCORRECT], style = MaterialTheme.typography.labelMedium, color = c.error)
                        LearningText(ex.sentence, topic.language, style = MaterialTheme.typography.titleMedium)
                        Text(s.gloss(ex.translation), style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                    }
                    if (!ex.isIncorrect) AudioButton(s[K.PLAY_AUDIO], { speaker.toggle(ex.sentence, topic.language) }, playing = speaker.playingText == ex.sentence)
                }
            }
        }
        if (topic.commonMistakes.isNotEmpty()) {
            SectionHeader(s[K.GRAMMAR_MISTAKES])
            PolyCard { topic.commonMistakes.forEach { Text("• " + s.gloss(it), style = MaterialTheme.typography.bodyMedium) } }
        }
        Curriculum.of(topic.language).lessons.firstOrNull { it.grammarTopicId == topic.id }?.let { l ->
            PrimaryButton(s[K.GRAMMAR_PRACTISE], { app.nav.push(Route.LessonOverview(l.id)) }, Modifier.fillMaxWidth())
        }
    }
}

// ---------------- Listening ----------------

@Composable
fun ListeningScreen() {
    val app = LocalApp.current
    val s = LocalStrings.current
    val scope = rememberCoroutineScope()
    val lang = app.profile.activeLearningLanguage
    val items = Curriculum.of(lang).dictations
    val speaker = rememberSpeaker(s)
    var index by remember { mutableIntStateOf(0) }
    var answered by remember(index) { mutableStateOf(false) }
    var last by remember(index) { mutableStateOf<Answer?>(null) }
    var rate by remember { mutableStateOf(1f) }
    ScrollScreen(s[K.LISTENING_TITLE]) {
        Text(s[K.LISTENING_INTRO], style = MaterialTheme.typography.bodyMedium, color = PolyglotTheme.colors.textSecondary)
        if (items.isEmpty()) { StateMessage(s[K.NOT_ENOUGH_DATA], ""); return@ScrollScreen }
        Text("${s[K.LISTENING_SPEED]}: ${(rate * 100).toInt()}%", style = MaterialTheme.typography.labelLarge)
        Slider(rate, { rate = it }, valueRange = 0.5f..1.25f, steps = 2, modifier = Modifier.semantics { contentDescription = s[K.LISTENING_SPEED] })
        val item = items[index % items.size]
        val ctx = ExerciseContext(lang, explainFor(lang, app.profile.explanationLanguage), speaker, true, true, false)
        PolyCard {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                AudioButton(s[K.PLAY_AUDIO], { speaker.play(item.audioText, lang, rate) }, playing = speaker.playingText == item.audioText)
                Text(s[K.LISTEN_AGAIN], color = PolyglotTheme.colors.textSecondary)
            }
            ExerciseView(item, ctx, answered) { a ->
                answered = true; last = a
                scope.launch { app.services.study.recordExercise(lang, null, item, a.outcome, a.userAnswer, 0, 0, a.promptText) }
            }
        }
        last?.let { a ->
            FeedbackPanel(a.outcome.correct, if (a.outcome.correct) s[K.FEEDBACK_CORRECT] else s[K.FEEDBACK_INCORRECT], s.f(K.FEEDBACK_ANSWER_WAS, item.audioText))
            PrimaryButton(s[K.LISTENING_NEXT], { index++ }, Modifier.fillMaxWidth())
        }
    }
}

// ---------------- Writing ----------------

@Composable
fun WritingScreen() {
    val app = LocalApp.current
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val scope = rememberCoroutineScope()
    val lang = app.profile.activeLearningLanguage
    val prompts = Curriculum.of(lang).writingPrompts + Curriculum.of(lang).lessons.flatMap { it.exercises }.filterIsInstance<Exercise.FreeWriting>()
    val explain = explainFor(lang, app.profile.explanationLanguage)
    var selected by remember { mutableStateOf(prompts.firstOrNull()) }
    var text by remember(selected) { mutableStateOf("") }
    var feedback by remember(selected) { mutableStateOf<WritingFeedback?>(null) }
    var aiError by remember(selected) { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val drafts = remember(selected) { mutableStateListOf<Pair<Int, String>>() }
    ScrollScreen(s[K.WRITING_TITLE]) {
        val prompt = selected ?: run { StateMessage(s[K.NOT_ENOUGH_DATA], ""); return@ScrollScreen }
        SectionHeader(s[K.WRITING_CHOOSE])
        prompts.distinctBy { it.id }.forEach { p ->
            ChoiceCard(p.prompt.forLanguage(explain), p.id == prompt.id, onClick = { selected = p }, subtitle = s.f(K.WRITING_MIN, p.minWords))
        }
        if (prompt.suggestedVocabulary.isNotEmpty()) Text(s.f(K.WRITE_SUGGESTED, prompt.suggestedVocabulary.joinToString(", ")), color = c.textSecondary)
        val words = app.services.writing.countWords(text, lang)
        AppTextField(text, { text = it }, s[K.WRITING_PLACEHOLDER], singleLine = false, minLines = 6, supporting = s.f(K.WRITE_COUNT, words, prompt.minWords))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SecondaryButton(s[K.WRITING_CHECK_BASIC], {
                feedback = app.services.writing.offlineFeedback(text, prompt, lang); aiError = null
                drafts.add(drafts.size + 1 to text)
                scope.launch {
                    app.services.study.recordExercise(lang, null, prompt, ExerciseOutcome(words >= prompt.minWords, (words.toDouble() / prompt.minWords).coerceAtMost(1.0), ""), text, 0, 0, prompt.prompt.en)
                }
            }, Modifier.weight(1f), enabled = text.isNotBlank())
            PrimaryButton(s[K.WRITING_CHECK_AI], {
                loading = true
                scope.launch {
                    val level = app.profile.enrollment(lang)?.startingLevel ?: CefrLevel.A1
                    app.services.writing.aiFeedback(text, prompt, lang, explain, level)
                        .onSuccess { feedback = it; aiError = null }
                        .onFailure { e ->
                            aiError = if (e is com.polyglot.academy.core.ai.AiError.BlockedByPrivacy || e is com.polyglot.academy.core.ai.AiError.NoProviderAvailable) s[K.WRITING_AI_BLOCKED]
                            else s.f(K.WRITING_AI_FAILED, e.message ?: "")
                            feedback = app.services.writing.offlineFeedback(text, prompt, lang)
                        }
                    drafts.add(drafts.size + 1 to text)
                    loading = false
                }
            }, Modifier.weight(1f), enabled = text.isNotBlank(), loading = loading)
        }
        aiError?.let { Banner(BannerKind.WARNING, it) }
        feedback?.let { fb -> WritingFeedbackView(fb, lang) }
        if (drafts.size > 1) {
            SectionHeader(s[K.WRITING_DRAFTS])
            drafts.forEach { (n, d) -> Text(s.f(K.WRITING_DRAFT_N, n, s.plural(K.WORDS, app.services.writing.countWords(d, lang))), style = MaterialTheme.typography.bodySmall, color = c.textSecondary) }
        }
    }
}

@Composable
private fun WritingFeedbackView(fb: WritingFeedback, lang: LanguageCode) {
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    PolyCard {
        Text(if (fb.source == "offline") s[K.WRITING_SOURCE_OFFLINE] else s.f(K.WRITING_SOURCE_AI, fb.source), style = MaterialTheme.typography.labelMedium, color = c.textSecondary)
        fb.checks.forEach { check ->
            val text = when (check) {
                WritingCheck.USED_SUGGESTED_WORDS -> s.f(K.WCHECK_USED_SUGGESTED_WORDS, fb.usedSuggestions.joinToString(", "))
                WritingCheck.REPEATED_WORDS -> s.f(K.WCHECK_REPEATED_WORDS, fb.repeatedWords.joinToString(", "))
                else -> s[K.valueOf("WCHECK_${check.name}")]
            }
            val good = check == WritingCheck.GOOD_LENGTH || check == WritingCheck.USED_SUGGESTED_WORDS
            Row(verticalAlignment = Alignment.Top) {
                Icon(if (good) Icons.Filled.CheckCircle else Icons.Filled.Cancel, null, tint = if (good) c.success else c.warning, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(text, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
    if (fb.corrections.isNotEmpty()) {
        SectionHeader(s[K.WRITING_CORRECTIONS])
        fb.corrections.forEach { cr ->
            PolyCard(containerColor = if (cr.isStylistic) c.infoContainer else c.card) {
                if (cr.isStylistic) Text(s[K.WRITING_OPTIONAL_STYLE], style = MaterialTheme.typography.labelMedium, color = c.info)
                else Pill(Labels.severity(s, cr.severity))
                LearningText(cr.original, lang, style = MaterialTheme.typography.bodyLarge, color = c.error)
                LearningText(cr.corrected, lang, style = MaterialTheme.typography.bodyLarge, color = c.success)
                if (cr.explanation.isNotBlank()) Text(cr.explanation, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
            }
        }
    }
    fb.improvedVersion?.let { SectionHeader(s[K.WRITING_IMPROVED]); PolyCard { LearningText(it, lang, style = MaterialTheme.typography.bodyLarge) } }
    fb.overallComment?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
}

// ---------------- Pronunciation & tones ----------------

@Composable
fun PronunciationScreen() {
    val app = LocalApp.current
    val s = LocalStrings.current
    val scope = rememberCoroutineScope()
    val lang = app.profile.activeLearningLanguage
    val items = Curriculum.of(lang).lessons.flatMap { it.exercises }.filterIsInstance<Exercise.SpeakRepeat>()
    val speaker = rememberSpeaker(s)
    var index by remember { mutableIntStateOf(0) }
    var answeredKey by remember { mutableStateOf<Int?>(null) }
    ScrollScreen(s[K.PRON_TITLE]) {
        Text(s[K.PRON_BODY], style = MaterialTheme.typography.bodyMedium, color = PolyglotTheme.colors.textSecondary)
        if (items.isEmpty()) return@ScrollScreen
        val item = items[index % items.size]
        val ctx = ExerciseContext(lang, explainFor(lang, app.profile.explanationLanguage), speaker, false, app.profile.enrollment(lang)?.showTransliteration != false, false)
        ExerciseView(item, ctx, answeredKey == index) { a ->
            answeredKey = index
            scope.launch { app.services.study.recordExercise(lang, null, item, a.outcome, a.userAnswer, 0, 0, a.promptText) }
        }
        if (answeredKey == index) PrimaryButton(s[K.NEXT], { index++ }, Modifier.fillMaxWidth())
        DetailedPronunciationCard(item.targetText, lang)
        Text(s[K.SPEAK_DISCLAIMER], style = MaterialTheme.typography.bodySmall, color = PolyglotTheme.colors.textSecondary)
    }
}

@Composable
fun TonesScreen() {
    val app = LocalApp.current
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val scope = rememberCoroutineScope()
    val speaker = rememberSpeaker(s)
    val zh = Curriculum.of(LanguageCode.MANDARIN)
    val toneItems = remember { zh.lessons.flatMap { it.exercises }.filterIsInstance<Exercise.ToneChoice>().distinctBy { it.id } }
    var index by remember { mutableIntStateOf(0) }
    var answered by remember(index) { mutableStateOf(false) }
    var correct by remember { mutableIntStateOf(0) }
    var total by remember { mutableIntStateOf(0) }
    var traditional by remember { mutableStateOf(app.profile.enrollment(LanguageCode.MANDARIN)?.useTraditionalCharacters == true) }
    ScrollScreen(s[K.TONES_TITLE]) {
        if (app.profile.activeLearningLanguage != LanguageCode.MANDARIN) Banner(BannerKind.INFO, s[K.TONES_NOT_MANDARIN])
        Text(s[K.TONES_BODY], style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
        Banner(BannerKind.INFO, s[K.TONES_SANDHI])
        if (total > 0) Text(s.f(K.TONES_SCORE, correct, total), style = MaterialTheme.typography.titleSmall)
        val item = toneItems[index % toneItems.size]
        val ctx = ExerciseContext(LanguageCode.MANDARIN, explainFor(LanguageCode.MANDARIN, app.profile.explanationLanguage), speaker, false, true, traditional)
        PolyCard {
            ExerciseView(item, ctx, answered) { a ->
                answered = true; total++; if (a.outcome.correct) correct++
                scope.launch { app.services.study.recordExercise(LanguageCode.MANDARIN, null, item, a.outcome, a.userAnswer, 0, 0, a.promptText) }
            }
        }
        if (answered) PrimaryButton(s[K.NEXT], { index++ }, Modifier.fillMaxWidth())
        SectionHeader(s[K.TONES_CHARACTERS])
        PolyCard(contentPadding = 0.dp) { SwitchRow(s[K.TONES_CHARACTERS], traditional, { traditional = it }) }
        PolyCard {
            zh.vocabulary.filter { it.term.length == 2 }.take(8).forEach { v ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LearningText(if (traditional) v.traditional ?: v.term else v.term, LanguageCode.MANDARIN, style = MaterialTheme.typography.titleLarge, reading = v.pronunciation, modifier = Modifier.weight(1f))
                    Text(Pinyin.tones(v.pronunciation.orEmpty()).joinToString("-"), style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(horizontal = 12.dp))
                    AudioButton(s[K.PLAY_AUDIO], { speaker.toggle(v.term, LanguageCode.MANDARIN) }, playing = speaker.playingText == v.term)
                }
            }
        }
    }
}

// ---------------- Relaxation ----------------

@Composable
fun RelaxScreen() {
    val app = LocalApp.current
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    var pattern by remember { mutableStateOf(Relaxation.patterns.first()) }
    var rounds by remember { mutableIntStateOf(3) }
    var running by remember { mutableStateOf(false) }
    var elapsed by remember { mutableStateOf(0.0) }
    var finished by remember { mutableStateOf(false) }
    LaunchedEffect(running) {
        if (!running) return@LaunchedEffect
        val start = app.services.clock.nowMillis()
        val sess = app.services.study.startSession(app.profile.activeLearningLanguage, SessionKind.RELAXATION, pattern.id)
        while (isActive && running) {
            elapsed = (app.services.clock.nowMillis() - start) / 1000.0
            if (Relaxation.frameAt(pattern, elapsed, rounds).finished) {
                running = false; finished = true
                app.services.study.updateSession(sess, rounds, rounds, true, elapsed.toInt())
            }
            delay(100)
        }
    }
    ScrollScreen(s[K.RELAX_TITLE]) {
        Text(s[K.RELAX_BODY], style = MaterialTheme.typography.bodyLarge, color = c.textSecondary)
        Banner(BannerKind.INFO, s.gloss(Relaxation.disclaimer))
        if (!running) {
            Relaxation.patterns.forEach { p -> ChoiceCard(s.gloss(p.title), pattern == p, onClick = { pattern = p; finished = false }, subtitle = s.gloss(p.description) + " · " + s.gloss(p.purpose)) }
            Text(s[K.RELAX_ROUNDS], style = MaterialTheme.typography.titleSmall)
            ChipGroup(listOf(2, 3, 5, 8), setOf(rounds), { it.toString() }, { rounds = it })
            if (finished) { Banner(BannerKind.SUCCESS, s[K.RELAX_DONE]); Text(s[K.RELAX_REFLECT], style = MaterialTheme.typography.bodyMedium) }
            PrimaryButton(s[K.RELAX_START], { elapsed = 0.0; finished = false; running = true }, Modifier.fillMaxWidth())
        } else {
            val frame = Relaxation.frameAt(pattern, elapsed, rounds)
            val label = when (frame.phase.kind) { BreathPhaseKind.INHALE -> s[K.RELAX_INHALE]; BreathPhaseKind.HOLD -> s[K.RELAX_HOLD]; BreathPhaseKind.EXHALE -> s[K.RELAX_EXHALE]; BreathPhaseKind.HOLD_EMPTY -> s[K.RELAX_HOLD_EMPTY] }
            val target = when (frame.phase.kind) {
                BreathPhaseKind.INHALE -> 0.45f + 0.55f * frame.phaseProgress.toFloat()
                BreathPhaseKind.HOLD -> 1f
                BreathPhaseKind.EXHALE -> 1f - 0.55f * frame.phaseProgress.toFloat()
                BreathPhaseKind.HOLD_EMPTY -> 0.45f
            }
            val scale by animateFloatAsState(if (PolyglotTheme.motion.reduced) 0.75f else target, tween(100))
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(220.dp), contentAlignment = Alignment.Center) {
                    Canvas(Modifier.size(220.dp)) {
                        drawCircle(c.primary.copy(alpha = 0.15f), size.minDimension / 2)
                        drawCircle(c.primary.copy(alpha = 0.55f), size.minDimension / 2 * scale, Offset(size.width / 2, size.height / 2))
                    }
                    Text(label, style = MaterialTheme.typography.headlineSmall, color = c.textPrimary, textAlign = TextAlign.Center,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                }
                Text(s.f(K.RELAX_SECONDS, (frame.phase.seconds - frame.secondsIntoPhase).toInt().coerceAtLeast(0) + 1), style = MaterialTheme.typography.titleMedium)
                Text(s.f(K.RELAX_CYCLE, frame.cycle + 1, rounds), color = c.textSecondary)
                SecondaryButton(s[K.RELAX_STOP], { running = false }, Modifier.fillMaxWidth())
            }
        }
    }
}

