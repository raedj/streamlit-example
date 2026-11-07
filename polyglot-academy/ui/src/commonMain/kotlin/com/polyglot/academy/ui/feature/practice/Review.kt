package com.polyglot.academy.ui.feature.practice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material.icons.filled.TheaterComedy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.RecallRating
import com.polyglot.academy.core.model.SessionKind
import com.polyglot.academy.core.model.StudySession
import com.polyglot.academy.core.service.ReviewCard
import com.polyglot.academy.ui.app.LoadContent
import com.polyglot.academy.ui.app.LocalApp
import com.polyglot.academy.ui.app.ScrollScreen
import com.polyglot.academy.ui.app.rememberLoader
import com.polyglot.academy.ui.app.rememberSpeaker
import com.polyglot.academy.ui.designsystem.AudioButton
import com.polyglot.academy.ui.designsystem.Banner
import com.polyglot.academy.ui.designsystem.ConfirmDialog
import com.polyglot.academy.ui.designsystem.QuietButton
import com.polyglot.academy.ui.designsystem.BannerKind
import com.polyglot.academy.ui.designsystem.HeroIllustration
import com.polyglot.academy.ui.designsystem.HeroKind
import com.polyglot.academy.ui.designsystem.IconBadge
import com.polyglot.academy.ui.designsystem.LearningText
import com.polyglot.academy.ui.designsystem.Pill
import com.polyglot.academy.ui.designsystem.PolyCard
import com.polyglot.academy.ui.designsystem.PolyglotTheme
import com.polyglot.academy.ui.designsystem.PrimaryButton
import com.polyglot.academy.ui.designsystem.SecondaryButton
import com.polyglot.academy.ui.designsystem.StateMessage
import com.polyglot.academy.ui.i18n.K
import com.polyglot.academy.ui.i18n.Labels
import com.polyglot.academy.ui.i18n.LocalStrings
import com.polyglot.academy.ui.navigation.Route
import com.polyglot.academy.ui.navigation.Tab
import kotlinx.coroutines.launch

@Composable
fun PracticeScreen() {
    val app = LocalApp.current
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val isMandarin = app.profile.activeLearningLanguage == LanguageCode.MANDARIN
    ScrollScreen(s[K.PRACTICE_TITLE], showBack = false, tab = Tab.PRACTICE) {
        Text(Labels.language(s, app.profile.activeLearningLanguage), style = MaterialTheme.typography.bodyLarge, color = c.textSecondary)
        // Four places to start, all working without a key; the rest wait behind "More".
        val pinned = listOf(
            Triple(K.PRACTICE_REVIEW to K.PRACTICE_REVIEW_SUB, Icons.Filled.Replay, Route.Review),
            Triple(K.PRACTICE_WORDLISTS to K.PRACTICE_WORDLISTS_SUB, Icons.Filled.FormatListNumbered, Route.WordLevels),
            Triple(K.PRACTICE_SCENARIOS to K.PRACTICE_SCENARIOS_SUB, Icons.Filled.TheaterComedy, Route.Scenarios),
            Triple(K.PRACTICE_READING to K.PRACTICE_READING_SUB, Icons.AutoMirrored.Filled.MenuBook, Route.Reading),
        )
        val more = buildList {
            add(Triple(K.PRACTICE_SPEAKING to K.PRACTICE_SPEAKING_SUB, Icons.Filled.RecordVoiceOver, Route.SpeakingPath))
            add(Triple(K.PRACTICE_PHRASEBOOK to K.PRACTICE_PHRASEBOOK_SUB, Icons.Filled.Luggage, Route.Phrasebook))
            add(Triple(K.PRACTICE_MISTAKES to K.PRACTICE_MISTAKES_SUB, Icons.Filled.ReportProblem, Route.Mistakes))
            add(Triple(K.PRACTICE_GRAMMAR to K.PRACTICE_GRAMMAR_SUB, Icons.Filled.Spellcheck, Route.Grammar))
            add(Triple(K.PRACTICE_LISTENING to K.PRACTICE_LISTENING_SUB, Icons.Filled.Hearing, Route.Listening))
            add(Triple(K.PRACTICE_WRITING to K.PRACTICE_WRITING_SUB, Icons.Filled.Edit, Route.Writing))
            add(Triple(K.PRACTICE_PRONUNCIATION to K.PRACTICE_PRONUNCIATION_SUB, Icons.Filled.Mic, Route.Pronunciation))
            if (isMandarin) add(Triple(K.PRACTICE_TONES to K.PRACTICE_TONES_SUB, Icons.Filled.MusicNote, Route.Tones))
            if (isMandarin) add(Triple(K.PRACTICE_CHARS to K.PRACTICE_CHARS_SUB, Icons.Filled.Draw, Route.Characters))
            if (com.polyglot.academy.core.arabic.ArabicLetters.supports(app.profile.activeLearningLanguage)) add(Triple(K.PRACTICE_LETTERS to K.PRACTICE_LETTERS_SUB, Icons.Filled.Draw, Route.Letters))
            add(Triple(K.PRACTICE_VOCAB to K.PRACTICE_VOCAB_SUB, Icons.Filled.AutoStories, Route.Vocabulary))
            add(Triple(K.PRACTICE_RELAX to K.PRACTICE_RELAX_SUB, Icons.Filled.SelfImprovement, Route.Relax))
        }
        var showMore by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }
        (if (showMore) pinned + more else pinned).forEachIndexed { i, (keys, icon, route) ->
            HubRow(s[keys.first], s[keys.second], icon, c.chartSeries[i % c.chartSeries.size]) { app.nav.push(route) }
        }
        com.polyglot.academy.ui.designsystem.QuietButton(s[if (showMore) K.PRACTICE_LESS else K.PRACTICE_MORE], { showMore = !showMore })
    }
}

@Composable
internal fun HubRow(title: String, subtitle: String, icon: ImageVector, tint: Color, onClick: () -> Unit) {
    val c = PolyglotTheme.colors
    PolyCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(icon, tint, tint.copy(alpha = 0.14f))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
            }
        }
    }
}

@Composable
fun ReviewScreen() {
    val app = LocalApp.current
    val s = LocalStrings.current
    val lang = app.profile.activeLearningLanguage
    val loader = rememberLoader(lang) { app.services.reviews.dueQueue(lang) }
    ScrollScreen(s[K.REVIEW_TITLE]) {
        LoadContent(loader) { queue -> ReviewSession(queue, lang) }
    }
}

/** A word recalled from its meaning. The app checks the answer, so the schedule rests on what the learner produced. */
@Composable
private fun WordRecall(card: ReviewCard, lang: LanguageCode, others: List<String>, speaker: com.polyglot.academy.ui.app.Speaker, onDone: (RecallRating) -> Unit) {
    val app = LocalApp.current
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val scope = rememberCoroutineScope()
    val enrollment = app.profile.enrollment(lang)
    var text by remember { mutableStateOf("") }
    var check by remember { mutableStateOf<com.polyglot.academy.core.learning.AnswerCheck?>(null) }
    var gaveUp by remember { mutableStateOf(false) }
    var problem by remember { mutableStateOf<K?>(null) }
    PolyCard {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(s[K.REVIEW_RECALL_PROMPT], style = MaterialTheme.typography.labelLarge, color = c.textSecondary)
            // The same language lessons explain words in, which can differ from the interface language.
            Text(card.back.forLanguage(explainFor(lang, app.profile.explanationLanguage)), style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        }
    }
    val result = check
    if (result == null && !gaveUp) {
        com.polyglot.academy.ui.feature.lesson.RecallInput(text, { text = it; problem = null }, lang, card.front, others, card.state.itemId,
            supporting = problem?.let { s[it] }, isError = problem != null)
        PrimaryButton(s[K.CHECK], {
            val r = com.polyglot.academy.core.learning.AnswerChecker.check(text, card.recallAnswers)
            when (r.verdict) {
                com.polyglot.academy.core.learning.AnswerVerdict.EMPTY -> problem = K.FEEDBACK_EMPTY
                com.polyglot.academy.core.learning.AnswerVerdict.WRONG_KEYBOARD -> problem = K.FEEDBACK_KEYBOARD
                else -> check = r
            }
        }, Modifier.fillMaxWidth())
        SecondaryButton(s[K.REVIEW_DONT_KNOW], { gaveUp = true }, Modifier.fillMaxWidth())
        return
    }
    val correct = result?.isCorrect == true
    val title = when {
        result == null -> s[K.REVIEW_HERE_IT_IS]
        correct -> s[K.FEEDBACK_CORRECT]
        result.verdict == com.polyglot.academy.core.learning.AnswerVerdict.ALMOST -> s[K.FEEDBACK_ALMOST]
        else -> s[K.FEEDBACK_INCORRECT]
    }
    com.polyglot.academy.ui.designsystem.FeedbackPanel(correct, title, null)
    PolyCard {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            val front = if (enrollment?.useTraditionalCharacters == true) card.vocabulary?.traditional ?: card.front else card.front
            LearningText(front, lang, reading = if (enrollment?.showTransliteration != false) card.pronunciation else null, textAlign = TextAlign.Center,
                style = if (lang == LanguageCode.MANDARIN) PolyglotTheme.learning.hanzi else PolyglotTheme.learning.term)
            AudioButton(s[K.PLAY_AUDIO], { speaker.toggle(card.front, lang) }, playing = speaker.playingText == card.front)
            card.example?.let { LearningText(it, lang, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center) }
        }
    }
    PrimaryButton(s[K.CONTINUE], { onDone(result?.let(com.polyglot.academy.core.learning.Recall::rating) ?: RecallRating.AGAIN) }, Modifier.fillMaxWidth())
    // The checker can wrongly reject a valid answer, and no one else can catch it: the learner can overrule it, and the report keeps a count.
    if (result != null && !correct) QuietButton(s[K.REVIEW_I_WAS_RIGHT], {
        scope.launch {
            app.services.contentReports.report(lang, card.state.itemId, "${card.front} ← $text", com.polyglot.academy.core.model.ContentIssue.AMBIGUOUS_ANSWER, "Answer marked wrong; learner says it was right")
            onDone(RecallRating.GOOD)
        }
    })
    com.polyglot.academy.ui.feature.common.ReportContentButton(lang, card.state.itemId, "${card.front} — ${s.gloss(card.back)}")
}

/** A grammar or sentence item from an earlier lesson, answered again and checked the same way. */
@Composable
private fun SentenceReview(card: ReviewCard, exercise: com.polyglot.academy.core.model.Exercise, lang: LanguageCode, speaker: com.polyglot.academy.ui.app.Speaker, onDone: (RecallRating) -> Unit) {
    val app = LocalApp.current
    val s = LocalStrings.current
    var last by remember { mutableStateOf<com.polyglot.academy.ui.feature.lesson.Answer?>(null) }
    val ctx = com.polyglot.academy.ui.feature.lesson.ExerciseContext(lang, explainFor(lang, app.profile.explanationLanguage), speaker, false,
        app.profile.enrollment(lang)?.showTransliteration != false, app.profile.enrollment(lang)?.useTraditionalCharacters == true)
    com.polyglot.academy.ui.feature.lesson.ExerciseView(exercise, ctx, last != null) { last = it }
    last?.let { a ->
        val outcome = a.outcome
        com.polyglot.academy.ui.designsystem.FeedbackPanel(outcome.correct, s[if (outcome.correct) K.FEEDBACK_CORRECT else K.FEEDBACK_INCORRECT],
            outcome.expected.takeIf { it.isNotBlank() && !outcome.correct }?.let { s.f(K.FEEDBACK_ANSWER_WAS, it) }) {
            exercise.explanation?.let { com.polyglot.academy.ui.feature.lesson.ExplanationRow(it.forLanguage(ctx.explain)) }
        }
        PrimaryButton(s[K.CONTINUE], {
            onDone(outcome.note?.let(com.polyglot.academy.core.learning.Recall::rating) ?: if (outcome.correct) RecallRating.GOOD else RecallRating.AGAIN)
        }, Modifier.fillMaxWidth())
        com.polyglot.academy.ui.feature.common.ReportContentButton(lang, exercise.id, a.promptText.ifBlank { exercise.id })
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReviewSession(queue: List<ReviewCard>, lang: LanguageCode) {
    val app = LocalApp.current
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val scope = rememberCoroutineScope()
    val speaker = rememberSpeaker(s)
    var cards by remember(queue) { mutableStateOf(queue) }
    var position by remember(queue) { mutableIntStateOf(0) }
    var revealed by remember(queue) { mutableStateOf(false) }
    var reviewed by remember(queue) { mutableIntStateOf(0) }
    var remembered by remember(queue) { mutableIntStateOf(0) }
    var session by remember { mutableStateOf<StudySession?>(null) }
    var shownAt by remember { mutableStateOf(app.services.clock.nowMillis()) }
    val enrollment = app.profile.enrollment(lang)

    if (queue.isEmpty()) {
        StateMessage(s[K.REVIEW_EMPTY_TITLE], s[K.REVIEW_EMPTY_BODY], illustration = { HeroIllustration(HeroKind.REVIEW_DONE, s[K.ILLUSTRATION], size = 140.dp) },
            action = s[K.SUMMARY_HOME], onAction = { app.nav.home() })
        Text(s[K.REVIEW_HOW], style = MaterialTheme.typography.bodySmall, color = c.textSecondary, textAlign = TextAlign.Center)
        return
    }
    if (position >= cards.size) {
        StateMessage(s[K.REVIEW_DONE_TITLE], s.plural(K.REVIEW_DONE_BODY, reviewed), illustration = { HeroIllustration(HeroKind.SUCCESS, s[K.ILLUSTRATION], size = 140.dp) },
            action = s[K.SUMMARY_HOME], onAction = { app.nav.home() })
        return
    }
    val card = cards[position]
    Text(s.plural(K.REVIEW_DUE_COUNT, cards.size - position), style = MaterialTheme.typography.labelLarge, color = c.textSecondary)
    if (card.state.isLeech) Banner(BannerKind.INFO, s[K.REVIEW_LEECH])
    fun finish(r: RecallRating) {
        scope.launch {
            if (session == null) session = app.services.study.startSession(lang, SessionKind.REVIEW, null)
            val (newState, _) = app.services.reviews.rate(card, r, app.services.clock.nowMillis() - shownAt)
            reviewed++
            if (r != RecallRating.AGAIN) remembered++
            // Cards still in short learning steps come back later in the same session.
            if (r == RecallRating.AGAIN) cards = cards + card.copy(state = newState)
            session?.let { session = app.services.study.updateSession(it, reviewed, remembered, position + 1 >= cards.size, reviewed * 12) }
            position++; revealed = false; shownAt = app.services.clock.nowMillis()
        }
    }
    val exercise = card.exercise
    if (exercise != null) androidx.compose.runtime.key(position) { SentenceReview(card, exercise, lang, speaker, ::finish) }
    else if (card.recallAnswers.isNotEmpty()) {
        val others = remember(cards) { cards.map { it.front }.filter { it.isNotBlank() } }
        androidx.compose.runtime.key(position) { WordRecall(card, lang, others, speaker, ::finish) }
    }
    else { PolyCard(Modifier.heightIn(min = 240.dp)) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            val front = if (enrollment?.useTraditionalCharacters == true) card.vocabulary?.traditional ?: card.front else card.front
            LearningText(front, lang, reading = if (revealed && enrollment?.showTransliteration != false) card.pronunciation else null, textAlign = TextAlign.Center,
                style = if (lang == LanguageCode.MANDARIN) PolyglotTheme.learning.hanzi else PolyglotTheme.learning.term)
            AudioButton(s[K.PLAY_AUDIO], { speaker.toggle(card.front, lang) }, playing = speaker.playingText == card.front)
            if (revealed) {
                Text(s.gloss(card.back), style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                card.example?.let { LearningText(it, lang, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center) }
            }
        }
    }
    if (revealed) com.polyglot.academy.ui.feature.common.ReportContentButton(lang, card.state.itemId, "${card.front} — ${s.gloss(card.back)}")
    if (!revealed) PrimaryButton(s[K.REVIEW_SHOW], { revealed = true }, Modifier.fillMaxWidth())
    else {
        Text(s[K.REVIEW_RATE], style = MaterialTheme.typography.titleSmall)
        val preview = remember(card) { app.services.reviews.preview(card) }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RecallRating.entries.forEach { r ->
                val label = s[K.valueOf("RATING_${r.name}")]
                val interval = Labels.duration(s, preview.getValue(r))
                val color = when (r) { RecallRating.AGAIN -> c.error; RecallRating.HARD -> c.warning; RecallRating.GOOD -> c.primary; RecallRating.EASY -> c.success }
                Button(
                    onClick = { finish(r) },
                    modifier = Modifier.weight(1f).heightIn(min = 64.dp).semantics { contentDescription = s.f(K.RATING_DESC, label, interval) },
                    colors = ButtonDefaults.buttonColors(containerColor = color.copy(alpha = 0.15f), contentColor = c.textPrimary),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(label, style = MaterialTheme.typography.labelLarge)
                        Text(interval, style = MaterialTheme.typography.labelSmall, color = c.textSecondary)
                    }
                }
            }
        }
    } }
    // The learner decides: an item can wait a day or a week, or leave the reviews altogether.
    var confirmRemove by remember(card) { mutableStateOf(false) }
    fun skip() { position++; revealed = false; shownAt = app.services.clock.nowMillis() }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        QuietButton(s[K.REVIEW_POSTPONE_DAY], { scope.launch { app.services.reviews.postpone(card.state.itemId, 1); skip() } })
        QuietButton(s[K.REVIEW_POSTPONE_WEEK], { scope.launch { app.services.reviews.postpone(card.state.itemId, 7); skip() } })
        QuietButton(s[K.REVIEW_REMOVE], { confirmRemove = true }, color = c.error)
    }
    if (confirmRemove) ConfirmDialog(s[K.REVIEW_REMOVE_TITLE], s[K.REVIEW_REMOVE_BODY], s[K.REVIEW_REMOVE], s[K.CANCEL],
        onConfirm = { confirmRemove = false; scope.launch { app.services.reviews.remove(card.state.itemId); skip() } },
        onDismiss = { confirmRemove = false }, destructive = true)
    Text(s[K.REVIEW_HOW], style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
}

@Composable
fun MistakesScreen() {
    val app = LocalApp.current
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val scope = rememberCoroutineScope()
    val lang = app.profile.activeLearningLanguage
    val mistakes by remember(lang) { app.services.store.mistakes(lang) }.collectAsState(null)
    var showResolved by remember { mutableStateOf(false) }
    ScrollScreen(s[K.MISTAKES_TITLE]) {
        val list = mistakes
        if (list == null) { com.polyglot.academy.ui.designsystem.LoadingPlaceholder(s[K.LOADING]); return@ScrollScreen }
        val open = list.filter { !it.resolved }
        if (open.isEmpty()) StateMessage(s[K.MISTAKES_EMPTY_TITLE], s[K.MISTAKES_EMPTY_BODY], illustration = { HeroIllustration(HeroKind.REVIEW_DONE, s[K.ILLUSTRATION], size = 120.dp) })
        open.sortedByDescending { it.occurrences }.forEach { m ->
            PolyCard {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Pill(Labels.skill(s, m.skill)); Pill(Labels.severity(s, m.severity)); Pill(s.plural(K.MISTAKE_TIMES, m.occurrences))
                }
                LearningText(m.prompt, lang, style = MaterialTheme.typography.titleMedium)
                Text(s.f(K.MISTAKE_YOURS, m.userAnswer), style = MaterialTheme.typography.bodyMedium, color = c.error)
                Text(s.f(K.MISTAKE_CORRECT, m.correctAnswer), style = MaterialTheme.typography.bodyMedium, color = c.success)
                m.explanation?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = c.textSecondary) }
                SecondaryButton(s[K.MISTAKE_RESOLVE], { scope.launch { app.services.store.resolveMistake(m.id) } })
            }
        }
        if (list.any { it.resolved }) QuietButton(s[K.MISTAKES_SHOW_RESOLVED], { showResolved = !showResolved })
        if (showResolved) list.filter { it.resolved }.forEach { m ->
            PolyCard { Text("${m.prompt} → ${m.correctAnswer}", style = MaterialTheme.typography.bodyMedium, color = c.textSecondary); Text(s[K.MISTAKE_RESOLVED], style = MaterialTheme.typography.labelSmall) }
        }
    }
}

