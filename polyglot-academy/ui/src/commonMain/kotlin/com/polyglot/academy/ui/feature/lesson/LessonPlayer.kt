package com.polyglot.academy.ui.feature.lesson

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.polyglot.academy.core.content.Curriculum
import com.polyglot.academy.core.content.WordLessons
import com.polyglot.academy.core.learning.AnswerVerdict
import com.polyglot.academy.core.learning.DifficultyAdjuster
import com.polyglot.academy.core.learning.DifficultyReason
import com.polyglot.academy.core.model.Exercise
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.Languages
import com.polyglot.academy.core.model.Lesson
import com.polyglot.academy.core.model.needsAudio
import com.polyglot.academy.core.service.StudyService
import com.polyglot.academy.core.model.SessionKind
import com.polyglot.academy.core.model.StudySession
import com.polyglot.academy.core.learning.AnswerChecker
import com.polyglot.academy.ui.app.LocalApp
import com.polyglot.academy.ui.app.PlatformBackHandler
import com.polyglot.academy.ui.app.rememberSpeaker
import com.polyglot.academy.ui.designsystem.Banner
import com.polyglot.academy.ui.designsystem.BannerKind
import com.polyglot.academy.ui.designsystem.ConfirmDialog
import com.polyglot.academy.ui.designsystem.ContentColumn
import com.polyglot.academy.ui.designsystem.FeedbackPanel
import com.polyglot.academy.ui.designsystem.HeroIllustration
import com.polyglot.academy.ui.designsystem.HeroKind
import com.polyglot.academy.ui.designsystem.LoadingPlaceholder
import com.polyglot.academy.ui.designsystem.PolyCard
import com.polyglot.academy.ui.designsystem.PolyglotTheme
import com.polyglot.academy.ui.designsystem.PrimaryButton
import com.polyglot.academy.ui.designsystem.ProgressRing
import com.polyglot.academy.ui.designsystem.SecondaryButton
import com.polyglot.academy.ui.designsystem.StateMessage
import com.polyglot.academy.ui.i18n.K
import com.polyglot.academy.ui.i18n.Labels
import com.polyglot.academy.ui.i18n.LocalStrings
import com.polyglot.academy.ui.navigation.Route
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private data class Summary(
    val score: Double, val correct: Int, val total: Int, val newWords: Int, val unlocked: List<String>, val seconds: Int, val next: Lesson?,
    /** Exercises answered wrongly on the first try in this sitting, in lesson order, with that first answer. */
    val mistakes: List<Pair<Exercise, Answer>> = emptyList(),
)

@Composable
fun LessonPlayerScreen(lessonId: String, restart: Boolean) {
    // "Practice again" keeps the same route, so a fresh key is what resets every piece of player state.
    var attempt by rememberSaveable(lessonId) { mutableIntStateOf(0) }
    val app = LocalApp.current
    val s = LocalStrings.current
    // Word-list lessons are generated from a 3000-word list, so they are built off the main thread.
    val generated = WordLessons.isWordLesson(lessonId)
    val lesson = if (generated) produceState<Lesson?>(null, lessonId) { value = WordLessons.load(lessonId) }.value
        else remember(lessonId) { Curriculum.lesson(lessonId) }
    if (lesson == null) {
        if (generated && WordLessons.parse(lessonId) != null) ContentColumn(padding = PaddingValues(top = 48.dp)) { LoadingPlaceholder(s[K.LOADING]) }
        else StateMessage(s[K.ERROR_TITLE], s[K.LESSON_NOT_FOUND], action = s[K.BACK], onAction = { app.nav.pop() })
        return
    }
    key(attempt) { LessonPlayer(lesson, restart || attempt > 0, onRetry = { attempt++ }) }
}

@Composable
private fun LessonPlayer(lesson: Lesson, restart: Boolean, onRetry: () -> Unit) {
    val app = LocalApp.current
    val s = LocalStrings.current
    val lessonId = lesson.id
    val services = app.services
    val scope = rememberCoroutineScope()
    val speaker = rememberSpeaker(s)
    val enrollment = app.profile.enrollment(lesson.language)
    val explain = app.profile.explanationLanguage.takeIf { it != lesson.language } ?: if (lesson.language == LanguageCode.ENGLISH) LanguageCode.ARABIC else LanguageCode.ENGLISH

    var index by rememberSaveable(lessonId) { mutableIntStateOf(-1) }
    // The furthest exercise reached. Going back below it revisits exercises: they can be answered again for
    // practice, but the first answer keeps the score and the review schedule is not touched a second time.
    var furthest by rememberSaveable(lessonId) { mutableIntStateOf(-1) }
    // Not saveable on purpose: it must reset together with lastAnswer, otherwise the Continue button could disappear.
    var answered by remember(lessonId, index) { mutableStateOf(false) }
    var lastAnswer by remember(lessonId, index) { mutableStateOf<Answer?>(null) }
    // Bumped by "Try again" after a wrong answer, so the exercise starts fresh; the score keeps the first try.
    var attempt by remember(lessonId, index) { mutableIntStateOf(0) }
    val firstTry = remember(lessonId) { mutableStateMapOf<String, Double>() }
    // The first wrong answer of each exercise, for the summary and the round that practises them again.
    val mistakes = remember(lessonId) { mutableStateMapOf<String, Answer>() }
    var practisingMistakes by remember { mutableStateOf(false) }
    var hintsOn by rememberSaveable { mutableStateOf(app.settings.showHintsByDefault) }
    var adaptMessage by remember { mutableStateOf<K?>(null) }
    val adaptSeen = remember { mutableSetOf<K>() }
    var session by remember { mutableStateOf<StudySession?>(null) }
    var summary by remember { mutableStateOf<Summary?>(null) }
    var confirmExit by remember { mutableStateOf(false) }
    var shownAt by remember(index) { mutableStateOf(services.clock.nowMillis()) }
    // Known words whose reading the learner chose to hide (empty unless that setting is on).
    val hiddenReadings by androidx.compose.runtime.produceState(emptySet<String>(), lesson.language, enrollment?.hideReadingWhenKnown) {
        if (enrollment?.hideReadingWhenKnown == true) value = services.reviews.knownIds(lesson.language)
    }
    // Null until checked; false when the device has no text-to-speech voice for the lesson language.
    var voiceAvailable by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(lessonId) {
        val p = services.store.lessonProgress(lesson.language).first()[lesson.id]
        if (index < 0) index = if (restart) 0 else (p?.resumeIndex ?: 0).coerceIn(0, lesson.exercises.lastIndex)
        if (furthest < index) furthest = index
        // A resumed lesson is scored on the exercises answered before the interruption too.
        if (index > 0) services.study.resumedFirstTries(lesson, index).forEach { (id, score) -> if (id !in firstTry) firstTry[id] = score }
        voiceAvailable = services.platform.synthesizer.isLanguageAvailable(Languages.of(lesson.language).defaultVariant.localeTag)
        session = services.study.startSession(lesson.language, SessionKind.LESSON, lesson.id)
    }
    // Leaving mid-lesson keeps the resume point.
    DisposableEffect(lessonId) { onDispose { speaker.stop() } }

    fun exit() {
        if (summary == null && index in 1..lesson.exercises.lastIndex) scope.launch { services.study.saveResumePoint(lesson, index) }
        app.nav.pop()
    }
    PlatformBackHandler(enabled = summary == null) { confirmExit = true }

    fun finish() = scope.launch {
        val gradable = lesson.exercises.filter { it.id in firstTry }
        val score = if (gradable.isEmpty()) 0.0 else gradable.sumOf { firstTry.getValue(it.id) } / gradable.size
        val stateBefore = services.store.states(lesson.language).first().map { it.itemId }.toSet()
        val unlocked = services.study.completeLesson(lesson, score)
        val newWords = lesson.vocabularyIds.count { it !in stateBefore }
        val started = session?.startedAtMillis ?: services.clock.nowMillis()
        val seconds = ((services.clock.nowMillis() - started) / 1000).toInt()
        session?.let { services.study.updateSession(it, gradable.size, gradable.count { g -> firstTry.getValue(g.id) >= 0.99 }, true, seconds) }
        val progress = services.store.lessonProgress(lesson.language).first()
        val next = WordLessons.parse(lesson.id)?.let { (lang, level, number) ->
            if (number < WordLessons.PER_LEVEL) WordLessons.load(WordLessons.id(lang, level, number + 1)) else null
        } ?: Curriculum.of(lesson.language).sequencer.nextLesson(enrollment?.startingLevel ?: lesson.level, progress)?.takeIf { it.id != lesson.id }
        summary = Summary(score, gradable.count { firstTry.getValue(it.id) >= 0.99 }, gradable.size, newWords, unlocked, seconds, next,
            lesson.exercises.mapNotNull { ex -> mistakes[ex.id]?.let { ex to it } })
    }

    fun advance() {
        adaptMessage = null
        if (index >= lesson.exercises.lastIndex) finish()
        else {
            index++
            if (index > furthest) { furthest = index; scope.launch { services.study.saveResumePoint(lesson, index) } }
        }
    }

    fun goBack() {
        if (index <= 0) return
        adaptMessage = null
        index--
    }

    /** True for an exercise the learner has already passed and came back to. */
    val revisiting = index in 0 until furthest

    fun onAnswer(ex: Exercise, a: Answer) {
        if (answered) return
        answered = true
        // Nothing was checked, so there is no verdict to show: go straight on.
        if (!a.outcome.counted) { advance(); return }
        lastAnswer = a
        if (revisiting) return
        // Without a voice a listening exercise is only a guess, so it is neither scored nor recorded.
        if (voiceAvailable == false && ex.needsAudio) return
        if (ex.id !in firstTry) {
            firstTry[ex.id] = StudyService.firstTryScore(a.outcome.correct, a.outcome.score)
            if (!a.outcome.correct) mistakes[ex.id] = a
        }
        scope.launch {
            services.study.recordExercise(lesson.language, lesson.id, ex, a.outcome, a.userAnswer, services.clock.nowMillis() - shownAt, a.hintsUsed, a.promptText)
            // Adapt hints and explanation depth from recent performance in this skill.
            val recent = services.store.recentAttempts(lesson.language, 16).filter { it.skill == ex.skill }
            val decision = DifficultyAdjuster.decide(ex.difficulty, recent, hintsPreferred = app.settings.showHintsByDefault)
            when (decision.reason) {
                DifficultyReason.STRUGGLING_RECENTLY -> { if (!hintsOn) adaptMessage = K.ADAPT_EASIER; hintsOn = true }
                DifficultyReason.STRONG_RECENT_PERFORMANCE -> { if (adaptSeen.add(K.ADAPT_HARDER)) adaptMessage = K.ADAPT_HARDER }
                else -> {}
            }
        }
    }

    if (confirmExit) ConfirmDialog(s[K.PLAYER_EXIT_TITLE], s[K.PLAYER_EXIT_BODY], s[K.PLAYER_EXIT_CONFIRM], s[K.PLAYER_STAY],
        onConfirm = { confirmExit = false; exit() }, onDismiss = { confirmExit = false })

    val c = PolyglotTheme.colors
    val actions = remember { ExerciseActionSlot() }
    val tilePool = remember(lessonId) { lesson.exercises.filterIsInstance<Exercise.Translate>().mapNotNull { it.acceptedAnswers.firstOrNull() } }
    val keys = remember { com.polyglot.academy.ui.feature.lesson.ExerciseKeySlot() }
    CompositionLocalProvider(LocalExerciseActions provides actions, com.polyglot.academy.ui.feature.lesson.LocalExerciseKeys provides keys) { com.polyglot.academy.ui.designsystem.KeyShortcuts(
        focusKey = Triple(index, answered, summary != null),
        onEnter = {
            when {
                summary != null -> false
                answered && lastAnswer != null -> { advance(); true }
                !answered -> keys.enter()
                else -> false
            }
        },
        onNumber = { n -> !answered && summary == null && keys.number(n) },
    ) { Scaffold(containerColor = c.background, topBar = {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp).padding(top = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { if (summary != null) app.nav.pop() else confirmExit = true }) { Icon(Icons.Filled.Close, s[K.CLOSE]) }
            if (summary == null && index > 0) IconButton(onClick = { goBack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, s[K.PLAYER_PREVIOUS]) }
            if (summary == null && index >= 0) {
                val desc = s.f(K.PLAYER_PROGRESS, index + 1, lesson.exercises.size)
                LinearProgressIndicator(
                    progress = { (index + 1).toFloat() / lesson.exercises.size },
                    modifier = Modifier.weight(1f).height(10.dp).clip(RoundedCornerShape(5.dp)).semantics { contentDescription = desc },
                    color = c.primary, trackColor = c.progressTrack, drawStopIndicator = {},
                )
                Text("${index + 1}/${lesson.exercises.size}", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(horizontal = 12.dp))
                // Back at an earlier exercise, the learner can step forward again without answering it.
                if (revisiting && !answered) IconButton(onClick = { advance() }) { Icon(Icons.AutoMirrored.Filled.ArrowForward, s[K.PLAYER_NEXT]) }
            }
        }
    }, bottomBar = {
        // The buttons stay in reach under the thumb and above the keyboard, however long the exercise is.
        Column(Modifier.fillMaxWidth().imePadding().navigationBarsPadding()) {
            val pinned = actions.content
            val feedbackFor = lastAnswer.takeIf { summary == null && answered && index >= 0 }
            if (feedbackFor != null) {
                val ex = lesson.exercises[index]
                ContentColumn(padding = PaddingValues(vertical = 12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (!feedbackFor.outcome.correct && !(voiceAvailable == false && ex.needsAudio)) {
                            SecondaryButton(s[K.RETRY], { answered = false; lastAnswer = null; attempt++; shownAt = services.clock.nowMillis() }, Modifier.weight(1f))
                        }
                        PrimaryButton(if (index >= lesson.exercises.lastIndex) s[K.DONE] else s[K.CONTINUE], { advance() }, Modifier.weight(1f))
                    }
                }
            } else if (summary == null && pinned != null) {
                ContentColumn(padding = PaddingValues(vertical = 12.dp)) { pinned() }
            }
        }
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            ContentColumn(padding = PaddingValues(vertical = 12.dp)) {
                val sum = summary
                if (sum != null && practisingMistakes) {
                    val ctx = ExerciseContext(lesson.language, explain, speaker, hintsOn, enrollment?.showTransliteration != false, enrollment?.useTraditionalCharacters == true, hiddenReadings, tilePool)
                    MistakeRound(sum.mistakes, ctx, explain) { practisingMistakes = false }
                }
                else if (sum != null) SummaryView(lesson, sum, onRetry = onRetry, onPractiseMistakes = { practisingMistakes = true })
                else if (index >= 0) {
                    val ex = lesson.exercises[index]
                    adaptMessage?.let { Banner(BannerKind.INFO, s[it]) }
                    if (revisiting) Banner(BannerKind.INFO, s[K.PLAYER_REVISIT_NOTE])
                    val ctx = ExerciseContext(lesson.language, explain, speaker, hintsOn, enrollment?.showTransliteration != false, enrollment?.useTraditionalCharacters == true, hiddenReadings, tilePool)
                    if (voiceAvailable == false && ex.needsAudio) {
                        Banner(BannerKind.WARNING, s.f(K.LISTEN_NO_VOICE, Labels.language(s, lesson.language)))
                        if (!answered) SecondaryButton(s[K.LISTEN_SKIP], { advance() }, Modifier.fillMaxWidth())
                    }
                    key(index, attempt) { ExerciseView(ex, ctx, answered) { onAnswer(ex, it) } }
                    val a = lastAnswer
                    if (answered && a != null) {
                        val verdict = a.outcome.note?.verdict
                        val title = when {
                            verdict == AnswerVerdict.CORRECT_WITH_ACCENT_SLIP -> s[K.FEEDBACK_CORRECT_ACCENT]
                            a.outcome.correct -> s[K.FEEDBACK_CORRECT]
                            verdict == AnswerVerdict.ALMOST -> s[K.FEEDBACK_ALMOST]
                            else -> s[K.FEEDBACK_INCORRECT]
                        }
                        val detail = buildList {
                            if (a.outcome.expected.isNotBlank() && (!a.outcome.correct || verdict == AnswerVerdict.CORRECT_WITH_ACCENT_SLIP)) add(s.f(K.FEEDBACK_ANSWER_WAS, a.outcome.expected))
                            if (ex is Exercise.Dictation && !a.outcome.correct) {
                                val missing = AnswerChecker.missingWords(a.userAnswer, ex.audioText)
                                if (missing.isNotEmpty()) add(s.f(K.FEEDBACK_MISSING_WORDS, missing.joinToString(", ")))
                            }
                        }.joinToString("\n").ifEmpty { null }
                        FeedbackPanel(a.outcome.correct, title, detail) {
                            ex.explanation?.let { ExplanationRow(it.forLanguage(explain)) }
                        }
                        com.polyglot.academy.ui.feature.common.ReportContentButton(lesson.language, "${lesson.id}/${ex.id}", a.promptText.ifBlank { ex.id })
                    }
                }
            }
        }
    } } }
}

@Composable
private fun SummaryView(lesson: Lesson, sum: Summary, onRetry: () -> Unit, onPractiseMistakes: () -> Unit) {
    val app = LocalApp.current
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val pct = (sum.score * 100).toInt()
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        HeroIllustration(HeroKind.SUCCESS, s[K.ILLUSTRATION], size = 120.dp)
        Text(s[K.SUMMARY_TITLE], style = MaterialTheme.typography.headlineSmall)
        ProgressRing(sum.score.toFloat(), description = s.f(K.SUMMARY_SCORE_DESC, pct), size = 120.dp, color = if (sum.score >= com.polyglot.academy.core.model.LessonProgress.PASS_THRESHOLD) c.success else c.accent) {
            Text(s.f(K.SUMMARY_SCORE, pct), style = PolyglotTheme.learning.stat)
        }
        Text(s.f(K.SUMMARY_CORRECT, sum.correct, sum.total), style = MaterialTheme.typography.bodyLarge)
        Text(s.f(K.SUMMARY_TIME, Labels.clockTime(sum.seconds)), style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
    }
    val passed = sum.score >= com.polyglot.academy.core.model.LessonProgress.PASS_THRESHOLD
    Banner(if (passed) BannerKind.SUCCESS else BannerKind.INFO, if (passed) s[K.SUMMARY_PASSED] else s[K.SUMMARY_NOT_PASSED])
    if (sum.newWords > 0) PolyCard { Text(s.plural(K.SUMMARY_NEW_WORDS, sum.newWords), style = MaterialTheme.typography.bodyLarge) }
    sum.unlocked.forEach { Banner(BannerKind.SUCCESS, s.f(K.SUMMARY_UNLOCKED, Labels.achievement(s, it))) }
    MistakeList(sum, onPractiseMistakes)
    sum.next?.let { next ->
        val route = if (WordLessons.isWordLesson(next.id)) Route.LessonPlayer(next.id, restart = true) else Route.LessonOverview(next.id)
        PrimaryButton("${s[K.SUMMARY_NEXT]}: ${s.gloss(next.title)}", { app.nav.replace(route) }, Modifier.fillMaxWidth())
    }
    SecondaryButton(s[K.SUMMARY_RETRY], onRetry, Modifier.fillMaxWidth())
    SecondaryButton(s[K.SUMMARY_HOME], { app.nav.home() }, Modifier.fillMaxWidth())
}

/** What went wrong on the first try, each with the learner's answer and the right one, and a button to practise them. */
@Composable
private fun MistakeList(sum: Summary, onPractise: () -> Unit) {
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    if (sum.total == 0) return
    if (sum.mistakes.isEmpty()) {
        Banner(BannerKind.SUCCESS, s[K.SUMMARY_NO_MISTAKES])
        return
    }
    com.polyglot.academy.ui.designsystem.SectionHeader(s.f(K.SUMMARY_MISTAKES, sum.mistakes.size))
    val big = MaterialTheme.typography.bodyLarge.copy(textDirection = androidx.compose.ui.text.style.TextDirection.Content)
    val small = MaterialTheme.typography.bodyMedium.copy(textDirection = androidx.compose.ui.text.style.TextDirection.Content)
    sum.mistakes.forEach { (ex, a) ->
        PolyCard {
            Text(a.promptText.ifBlank { s.gloss(ex.instruction) }, Modifier.fillMaxWidth(), style = big)
            val expected = a.outcome.expected
            if (a.userAnswer.isNotBlank() && a.userAnswer != expected) Text(s.f(K.SUMMARY_YOUR_ANSWER, a.userAnswer), Modifier.fillMaxWidth(), style = small, color = c.error)
            if (expected.isNotBlank()) Text(s.f(K.FEEDBACK_ANSWER_WAS, expected), Modifier.fillMaxWidth(), style = small, color = c.success)
        }
    }
    PrimaryButton(s[K.SUMMARY_PRACTISE_MISTAKES], onPractise, Modifier.fillMaxWidth())
}

/**
 * The exercises missed on the first try, once more, right after the lesson. Practice only: the lesson score
 * and the review schedule were set by the first answers and are not changed here.
 */
@Composable
private fun MistakeRound(items: List<Pair<Exercise, Answer>>, ctx: ExerciseContext, explain: LanguageCode, onDone: () -> Unit) {
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    var i by remember { mutableIntStateOf(0) }
    var attempt by remember { mutableIntStateOf(0) }
    var last by remember { mutableStateOf<Answer?>(null) }
    var right by remember { mutableIntStateOf(0) }
    val scored = remember { mutableSetOf<Int>() }
    val keys = remember { ExerciseKeySlot() }
    fun next() { i++; last = null; attempt = 0 }
    com.polyglot.academy.ui.designsystem.KeyShortcuts(Triple(i, attempt, last != null), onEnter = {
        when {
            i >= items.size -> { onDone(); true }
            last != null -> { next(); true }
            else -> keys.enter()
        }
    }, onNumber = { n -> i < items.size && last == null && keys.number(n) }) {
        // The buttons of each exercise sit under it here, not in the player's bottom bar.
        CompositionLocalProvider(LocalExerciseActions provides null, LocalExerciseKeys provides keys) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(s[K.LMISTAKE_TITLE], style = MaterialTheme.typography.headlineSmall)
                Text(s[K.LMISTAKE_NOTE], style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                if (i >= items.size) {
                    FeedbackPanel(right * 2 >= items.size, s.f(K.LMISTAKE_DONE, right, items.size), null, cue = false)
                    PrimaryButton(s[K.LMISTAKE_BACK], onDone, Modifier.fillMaxWidth())
                    return@Column
                }
                Text("${i + 1}/${items.size}", style = MaterialTheme.typography.labelLarge)
                val ex = items[i].first
                key(i, attempt) {
                    ExerciseView(ex, ctx, last != null) { a ->
                        if (last != null) return@ExerciseView
                        if (!a.outcome.counted) { next(); return@ExerciseView }
                        last = a
                        if (scored.add(i) && a.outcome.correct) right++
                    }
                }
                last?.let { a ->
                    val detail = if (!a.outcome.correct && a.outcome.expected.isNotBlank()) s.f(K.FEEDBACK_ANSWER_WAS, a.outcome.expected) else null
                    FeedbackPanel(a.outcome.correct, s[if (a.outcome.correct) K.FEEDBACK_CORRECT else K.FEEDBACK_INCORRECT], detail) {
                        ex.explanation?.let { ExplanationRow(it.forLanguage(explain)) }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (!a.outcome.correct) SecondaryButton(s[K.RETRY], { last = null; attempt++ }, Modifier.weight(1f))
                        PrimaryButton(if (i >= items.lastIndex) s[K.DONE] else s[K.CONTINUE], { next() }, Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
