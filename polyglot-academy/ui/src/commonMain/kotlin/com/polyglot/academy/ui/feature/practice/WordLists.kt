package com.polyglot.academy.ui.feature.practice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.polyglot.academy.core.content.WordLessons
import com.polyglot.academy.core.mandarin.StrokeOrder
import com.polyglot.academy.ui.designsystem.StrokeOrderPlayer
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.Languages
import com.polyglot.academy.core.model.ReviewPhase
import com.polyglot.academy.core.model.ReviewState
import com.polyglot.academy.core.service.WordLevelService
import com.polyglot.academy.core.service.WordQuizQuestion
import com.polyglot.academy.core.service.WordTestMode
import com.polyglot.academy.core.service.WordTestResult
import com.polyglot.academy.core.util.TextNormalizer
import com.polyglot.academy.core.wordlist.WordEntry
import com.polyglot.academy.core.wordlist.WordLevels
import com.polyglot.academy.core.wordlist.WordLists
import com.polyglot.academy.ui.app.LoadContent
import com.polyglot.academy.ui.app.LocalApp
import com.polyglot.academy.ui.app.ScrollScreen
import com.polyglot.academy.ui.app.rememberLoader
import com.polyglot.academy.ui.app.rememberSpeaker
import com.polyglot.academy.ui.app.rememberToast
import com.polyglot.academy.ui.designsystem.AppTextField
import com.polyglot.academy.ui.designsystem.AudioButton
import com.polyglot.academy.ui.designsystem.Banner
import com.polyglot.academy.ui.designsystem.BannerKind
import com.polyglot.academy.ui.designsystem.ChoiceCard
import com.polyglot.academy.ui.designsystem.FeedbackPanel
import com.polyglot.academy.ui.designsystem.LabeledProgress
import com.polyglot.academy.ui.designsystem.LearningText
import com.polyglot.academy.ui.designsystem.Pill
import com.polyglot.academy.ui.designsystem.PolyCard
import com.polyglot.academy.ui.designsystem.PolyglotTheme
import com.polyglot.academy.ui.designsystem.PrimaryButton
import com.polyglot.academy.ui.designsystem.QuietButton
import com.polyglot.academy.ui.designsystem.SecondaryButton
import com.polyglot.academy.ui.designsystem.SectionHeader
import com.polyglot.academy.ui.designsystem.StateMessage
import com.polyglot.academy.ui.i18n.K
import com.polyglot.academy.ui.i18n.Labels
import com.polyglot.academy.ui.i18n.Localizer
import com.polyglot.academy.ui.i18n.LocalStrings
import com.polyglot.academy.ui.navigation.Route
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/** The ten levels of the active language's frequency word list. */
@Composable
fun WordLevelsScreen() {
    val app = LocalApp.current
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val lang = app.profile.activeLearningLanguage
    val loader = rememberLoader(lang) { app.services.wordLevels.levels(lang) }
    ScrollScreen(s[K.WORDLEVELS_TITLE]) {
        Text(s.f(K.WORDLEVELS_BODY, Labels.language(s, lang)), style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
        Banner(BannerKind.INFO, s[K.WORDLEVELS_NOTICE])
        LoadContent(loader) { levels ->
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                levels.forEach { p ->
                    PolyCard(onClick = { app.nav.push(Route.WordLevel(p.level)) }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(s.f(K.WORDLEVEL_NAME, p.level), style = MaterialTheme.typography.titleMedium)
                                Text(levelRanks(s, p.level), style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                            }
                            if (p.passed) Pill(s[K.WORDLEVEL_PASSED], container = c.successContainer, content = c.textPrimary)
                        }
                        LabeledProgress(s[K.WORDLEVEL_STARTED], s.f(K.WORDLEVEL_COUNT, p.started, p.total), p.started.toFloat() / p.total.coerceAtLeast(1))
                        LabeledProgress(s[K.WORDLEVEL_KNOWN], s.f(K.WORDLEVEL_COUNT, p.known, p.total), p.known.toFloat() / p.total.coerceAtLeast(1), color = c.success)
                        p.bestTestScore?.let { Text(s.f(K.WORDLEVEL_BEST, percent(it)), style = MaterialTheme.typography.bodySmall, color = c.textSecondary) }
                    }
                }
            }
        }
    }
}

/** One level: progress, adding the next words to reviews, the level test, and the searchable word list. */
@Composable
fun WordLevelScreen(level: Int) {
    val app = LocalApp.current
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val scope = rememberCoroutineScope()
    val toast = rememberToast()
    val lang = app.profile.activeLearningLanguage
    val speaker = rememberSpeaker(s)
    var query by rememberSaveable { mutableStateOf("") }
    var shown by rememberSaveable { mutableIntStateOf(PAGE) }
    val loader = rememberLoader(lang, level) {
        LevelData(app.services.wordLevels.level(lang, level), app.services.wordLevels.states(lang, level),
            app.services.wordLevels.words(lang, level), app.services.wordLevels.lessons(lang, level))
    }
    ScrollScreen(s.f(K.WORDLEVEL_NAME, level)) {
        Text(levelRanks(s, level), style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
        LoadContent(loader) { (progress, states, words, lessons) ->
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PolyCard {
                    Text(s[K.WORDLEVEL_LESSONS], style = MaterialTheme.typography.titleMedium)
                    Text(s[K.WORDLEVEL_LESSONS_BODY], style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                    LabeledProgress(s.f(K.WORDLEVEL_LESSONS_DONE, lessons.done, lessons.total), "", lessons.done.toFloat() / lessons.total.coerceAtLeast(1))
                    val next = lessons.next
                    if (next != null) {
                        val r = WordLessons.ranks(level, next)
                        PrimaryButton(s.f(K.WORDLEVEL_LESSON_NEXT, next, r.first, r.last), { app.nav.push(Route.LessonPlayer(WordLessons.id(lang, level, next))) }, Modifier.fillMaxWidth())
                    } else Text(s[K.WORDLEVEL_LESSONS_ALL], style = MaterialTheme.typography.bodyMedium)
                }
                PolyCard {
                    LabeledProgress(s[K.WORDLEVEL_STARTED], s.f(K.WORDLEVEL_COUNT, progress.started, progress.total), progress.started.toFloat() / progress.total.coerceAtLeast(1))
                    LabeledProgress(s[K.WORDLEVEL_KNOWN], s.f(K.WORDLEVEL_COUNT, progress.known, progress.total), progress.known.toFloat() / progress.total.coerceAtLeast(1), color = c.success)
                    progress.bestTestScore?.let { Text(s.f(K.WORDLEVEL_BEST, percent(it)), style = MaterialTheme.typography.bodySmall, color = c.textSecondary) }
                    if (progress.started < progress.total) {
                        SecondaryButton(s.f(K.WORDLEVEL_ADD_NEXT, WordLevelService.BATCH), {
                            scope.launch {
                                val added = app.services.wordLevels.addNext(lang, level)
                                toast(s.plural(K.WORDLEVEL_ADDED, added.size))
                                loader.reload()
                            }
                        }, Modifier.fillMaxWidth())
                    } else Text(s[K.WORDLEVEL_ALL_ADDED], style = MaterialTheme.typography.bodyMedium)
                    if (progress.started > 0) SecondaryButton(s[K.WORDLEVEL_REVIEW], { app.nav.push(Route.Review) }, Modifier.fillMaxWidth())
                    SecondaryButton(s[K.WORDLEVEL_TEST], { app.nav.push(Route.WordTest(level)) }, Modifier.fillMaxWidth())
                }
                AppTextField(query, { query = it }, s[K.WORDLEVEL_SEARCH])
                val needle = TextNormalizer.loose(query)
                val filtered = if (query.isBlank()) words else words.filter { e ->
                    TextNormalizer.loose(e.term).contains(needle) || TextNormalizer.loose(s.gloss(e.meaning)).contains(needle) ||
                        e.forms.any { TextNormalizer.loose(it).contains(needle) } || e.pronunciation?.let { TextNormalizer.loose(it).contains(needle) } == true
                }
                if (filtered.isEmpty()) StateMessage(s[K.WORDLEVEL_SEARCH], s[K.WORDLEVEL_NO_MATCH])
                filtered.take(shown).forEach { e ->
                    PolyCard(onClick = { app.nav.push(Route.WordDetail(e.id)) }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${e.rank}", style = MaterialTheme.typography.labelMedium, color = c.textSecondary, modifier = Modifier.padding(end = 12.dp))
                            Column(Modifier.weight(1f)) {
                                LearningText(e.term, lang, style = MaterialTheme.typography.titleMedium, reading = e.pronunciation.takeIf { lang == LanguageCode.MANDARIN })
                                Text(s.gloss(e.meaning), style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                            }
                            AudioButton(s[K.PLAY_AUDIO], { speaker.toggle(e.term, lang) }, playing = speaker.playingText == e.term)
                        }
                        Pill(statusLabel(s, states[e.id]))
                    }
                }
                if (filtered.size > shown) QuietButton(s[K.WORDLEVEL_SHOW_MORE], { shown += PAGE })
            }
        }
    }
}

/** One word: meaning, pronunciation, grammar fields, forms and examples, with audio and adding to reviews. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WordDetailScreen(entryId: String) {
    val app = LocalApp.current
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val scope = rememberCoroutineScope()
    val toast = rememberToast()
    val lang = app.profile.activeLearningLanguage
    val speaker = rememberSpeaker(s)
    val list = produceState<com.polyglot.academy.core.wordlist.WordList?>(null, lang) { value = WordLists.load(lang) }.value
    if (list == null) {
        ScrollScreen(s[K.WORDLEVELS_TITLE]) { com.polyglot.academy.ui.designsystem.LoadingPlaceholder(s[K.LOADING]) }
        return
    }
    val entry = list.byId[entryId]
    if (entry == null) {
        ScrollScreen(s[K.WORDLEVELS_TITLE]) { StateMessage(s[K.WORDLEVELS_TITLE], s[K.WORD_NOT_FOUND]) }
        return
    }
    val loader = rememberLoader(entryId) { app.services.store.state(entry.reviewId) }
    ScrollScreen(entry.term) {
        PolyCard {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(s.f(K.WORD_RANK, entry.rank, entry.level), style = MaterialTheme.typography.labelMedium, color = c.textSecondary)
                LearningText(entry.term, lang, reading = entry.pronunciation, textAlign = TextAlign.Center,
                    style = if (lang == LanguageCode.MANDARIN) PolyglotTheme.learning.hanzi else PolyglotTheme.learning.term)
                AudioButton(s[K.PLAY_AUDIO], { speaker.toggle(entry.term, lang) }, playing = speaker.playingText == entry.term)
                Pill(Labels.partOfSpeech(s, entry.partOfSpeech))
            }
        }
        SectionHeader(s[K.WORD_MEANING])
        Text(s.gloss(entry.meaning), style = MaterialTheme.typography.titleMedium)
        val details = details(s, entry)
        if (details.isNotEmpty()) {
            SectionHeader(s[K.WORD_DETAILS])
            PolyCard {
                details.forEach { (label, value) ->
                    Row {
                        Text(label, style = MaterialTheme.typography.bodyMedium, color = c.textSecondary, modifier = Modifier.weight(1f))
                        LearningText(value, lang, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
        val hanzi = entry.term.filter { StrokeOrder.of(it) != null }
        if (lang == LanguageCode.MANDARIN && hanzi.isNotEmpty()) {
            var replay by remember(entryId) { mutableIntStateOf(0) }
            SectionHeader(s[K.WORD_STROKES], action = s[K.WORD_STROKES_REPLAY], onAction = { replay++ })
            // Chinese is written left to right, also in the Arabic interface.
            androidx.compose.runtime.CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    hanzi.forEach { ch ->
                        StrokeOrderPlayer(StrokeOrder.of(ch)!!, s.f(K.WORD_STROKES_DESC, ch.toString(), StrokeOrder.of(ch)!!.size), replay)
                    }
                }
            }
            Text(s[K.WORD_STROKES_SOURCE], style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
        }
        if (entry.forms.isNotEmpty()) {
            SectionHeader(s[K.WORD_FORMS])
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                entry.forms.forEach { Pill(it) }
            }
        }
        if (entry.examples.isNotEmpty()) {
            SectionHeader(s[K.WORD_EXAMPLES])
            entry.examples.forEach { ex ->
                PolyCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            LearningText(ex.sentence, lang, style = MaterialTheme.typography.bodyLarge)
                            Text(s.gloss(ex.translation), style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                        }
                        AudioButton(s[K.PLAY_AUDIO], { speaker.toggle(ex.sentence, lang) }, playing = speaker.playingText == ex.sentence)
                    }
                }
            }
        }
        LoadContent(loader) { state ->
            if (state == null) PrimaryButton(s[K.WORD_ADD], {
                scope.launch { app.services.wordLevels.add(entry); toast(s[K.WORD_ADDED]); loader.reload() }
            }, Modifier.fillMaxWidth())
            else Pill(statusLabel(s, state))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            list.byRank[entry.rank - 1]?.let { prev -> SecondaryButton(s[K.WORD_PREV], { app.nav.replace(Route.WordDetail(prev.id)) }, Modifier.weight(1f)) }
            list.byRank[entry.rank + 1]?.let { next -> SecondaryButton(s[K.WORD_NEXT], { app.nav.replace(Route.WordDetail(next.id)) }, Modifier.weight(1f)) }
        }
        Text(s[K.WORDLEVELS_NOTICE], style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
    }
}

/** Multiple-choice level test of 20 words: word to meaning, meaning to word, listening, or mixed; missed words go into reviews. */
@Composable
fun WordTestScreen(level: Int) {
    val app = LocalApp.current
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val scope = rememberCoroutineScope()
    val lang = app.profile.activeLearningLanguage
    val speaker = rememberSpeaker(s)
    val voice by produceState<Boolean?>(null, lang) {
        value = app.services.platform.synthesizer.isLanguageAvailable(Languages.of(lang).defaultVariant.localeTag)
    }
    var choice by rememberSaveable { mutableStateOf(TestChoice.MIXED) }
    var attempt by remember { mutableIntStateOf(0) }
    var quiz by remember(attempt) { mutableStateOf<List<WordQuizQuestion>>(emptyList()) }
    var index by remember(attempt) { mutableIntStateOf(0) }
    var chosen by remember(attempt) { mutableStateOf<Int?>(null) }
    val missed = remember(attempt) { mutableStateListOf<WordEntry>() }
    var result by remember(attempt) { mutableStateOf<WordTestResult?>(null) }
    ScrollScreen(s.f(K.WORDTEST_TITLE, level)) {
        val done = result
        when {
            quiz.isEmpty() -> {
                Text(s.f(K.WORDTEST_BODY, WordLevelService.QUIZ_SIZE, percent(WordLevelService.PASS_SCORE)), style = MaterialTheme.typography.bodyLarge)
                SectionHeader(s[K.WORDTEST_MODE])
                val choices = TestChoice.entries.filter { it != TestChoice.LISTENING || voice != false }
                choices.forEach { ch -> ChoiceCard(s[ch.label], selected = choice == ch, onClick = { choice = ch }) }
                if (voice == false) Banner(BannerKind.INFO, s.f(K.WORDTEST_NO_VOICE, Labels.language(s, lang)))
                PrimaryButton(s[K.WORDTEST_START], {
                    val pick = if (voice == false && choice == TestChoice.LISTENING) TestChoice.MIXED else choice
                    scope.launch { quiz = app.services.wordLevels.quiz(lang, level, modes = pick.modes(voice != false)) }
                }, Modifier.fillMaxWidth())
            }
            done != null -> {
                StateMessage(s.f(K.WORDTEST_RESULT, done.correct, done.total, percent(done.score)), s[if (done.passed) K.WORDTEST_PASSED else K.WORDTEST_FAILED])
                if (done.added > 0) Text(s.plural(K.WORDLEVEL_ADDED, done.added), style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
                if (missed.isNotEmpty()) {
                    SectionHeader(s[K.WORDTEST_MISSED])
                    missed.forEach { e ->
                        PolyCard(onClick = { app.nav.push(Route.WordDetail(e.id)) }) {
                            LearningText(e.term, lang, style = MaterialTheme.typography.titleMedium)
                            Text(s.gloss(e.meaning), style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                        }
                    }
                }
                PrimaryButton(s[K.WORDTEST_BACK], { app.nav.pop() }, Modifier.fillMaxWidth())
                SecondaryButton(s[K.WORDTEST_AGAIN], { attempt++ }, Modifier.fillMaxWidth())
            }
            else -> {
                val q: WordQuizQuestion = quiz[index]
                if (q.mode == WordTestMode.LISTENING) LaunchedEffect(attempt, index) { speaker.play(q.entry.term, lang) }
                Text(s.f(K.WORDTEST_QUESTION, index + 1, quiz.size), style = MaterialTheme.typography.labelLarge, color = c.textSecondary)
                PolyCard {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val termStyle = if (lang == LanguageCode.MANDARIN) PolyglotTheme.learning.hanzi else PolyglotTheme.learning.term
                        when (q.mode) {
                            WordTestMode.MEANING -> {
                                Text(s[K.WORDTEST_PROMPT], style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
                                LearningText(q.entry.term, lang, textAlign = TextAlign.Center, style = termStyle)
                                AudioButton(s[K.PLAY_AUDIO], { speaker.toggle(q.entry.term, lang) }, playing = speaker.playingText == q.entry.term)
                            }
                            WordTestMode.WORD -> {
                                Text(s[K.WORDTEST_PROMPT_WORD], style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
                                Text(s.gloss(q.entry.meaning), style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                            }
                            WordTestMode.LISTENING -> {
                                Text(s[K.WORDTEST_PROMPT_LISTEN], style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    AudioButton(s[K.PLAY_AUDIO], { speaker.toggle(q.entry.term, lang) }, playing = speaker.playingText == q.entry.term)
                                    AudioButton(s[K.LISTEN_SLOW], { speaker.play(q.entry.term, lang, 0.7f) })
                                }
                                if (chosen != null) LearningText(q.entry.term, lang, textAlign = TextAlign.Center, style = termStyle)
                            }
                        }
                    }
                }
                q.options.forEachIndexed { i, o ->
                    val label = if (q.mode == WordTestMode.WORD) o.term else s.gloss(o.meaning)
                    ChoiceCard(label, selected = chosen == i, language = lang.takeIf { q.mode == WordTestMode.WORD }, onClick = {
                        if (chosen == null) { chosen = i; if (i != q.answerIndex) missed += q.entry }
                    })
                }
                chosen?.let { pick ->
                    val right = pick == q.answerIndex
                    val answer = if (q.mode == WordTestMode.MEANING) s.gloss(q.entry.meaning) else "${q.entry.term} — ${s.gloss(q.entry.meaning)}"
                    FeedbackPanel(right, s[if (right) K.WORDTEST_CORRECT else K.WORDTEST_WRONG], if (right && q.mode == WordTestMode.MEANING) null else s.f(K.WORDTEST_ANSWER, answer))
                    val last = index + 1 >= quiz.size
                    PrimaryButton(s[if (last) K.WORDTEST_FINISH else K.WORDTEST_NEXT], {
                        if (!last) { index++; chosen = null }
                        else scope.launch { result = app.services.wordLevels.finishTest(lang, level, quiz.size - missed.size, quiz.size, missed.toList()) }
                    }, Modifier.fillMaxWidth())
                }
            }
        }
    }
}

/** The question types offered before a level test. */
private enum class TestChoice(val label: K) {
    MIXED(K.WORDTEST_MODE_MIXED), MEANING(K.WORDTEST_MODE_MEANING), WORD(K.WORDTEST_MODE_WORD), LISTENING(K.WORDTEST_MODE_LISTENING);

    fun modes(voice: Boolean): List<WordTestMode> = when (this) {
        MIXED -> listOfNotNull(WordTestMode.MEANING, WordTestMode.WORD, WordTestMode.LISTENING.takeIf { voice })
        MEANING -> listOf(WordTestMode.MEANING)
        WORD -> listOf(WordTestMode.WORD)
        LISTENING -> listOf(WordTestMode.LISTENING)
    }
}

private const val PAGE = 60

private data class LevelData(
    val progress: com.polyglot.academy.core.service.WordLevelProgress,
    val states: Map<String, ReviewState>,
    val words: List<WordEntry>,
    val lessons: com.polyglot.academy.core.service.WordLessonProgress,
)

private fun percent(score: Double): Int = (score * 100).roundToInt()

private fun levelRanks(s: Localizer, level: Int): String {
    val r = WordLevels.ranks(level)
    return s.f(K.WORDLEVEL_RANKS, r.first, r.last, Labels.level(s, WordLevels.cefr(level)))
}

private fun statusLabel(s: Localizer, st: ReviewState?): String = when (st?.phase) {
    null -> s[K.WORD_STATUS_NOT_ADDED]
    ReviewPhase.NEW -> s[K.VOCAB_STATUS_NEW]
    ReviewPhase.LEARNING, ReviewPhase.RELEARNING -> s[K.VOCAB_STATUS_LEARNING]
    ReviewPhase.REVIEW -> s[K.VOCAB_STATUS_REVIEW]
}

/** Grammar fields worth showing for the word, as (label, value) pairs. */
private fun details(s: Localizer, e: WordEntry): List<Pair<String, String>> = buildList {
    val x = e.extra
    x["gender"]?.let { g ->
        when (g) { "m" -> K.WORD_GENDER_M; "f" -> K.WORD_GENDER_F; "mf" -> K.WORD_GENDER_MF; else -> null }?.let { add(s[K.WORD_EXTRA_GENDER] to s[it]) }
    }
    x["article"]?.let { add(s[K.WORD_EXTRA_ARTICLE] to it) }
    x["plural"]?.takeIf { it != "—" }?.let { add(s[K.WORD_EXTRA_PLURAL] to it) }
    x["root"]?.let { add(s[K.WORD_EXTRA_ROOT] to it) }
    x["masdar"]?.let { add(s[K.WORD_EXTRA_MASDAR] to it) }
    x["present"]?.let { add(s[K.WORD_EXTRA_PRESENT] to it) }
    x["measure"]?.let { add(s[K.WORD_EXTRA_MEASURE] to it) }
    x["trad"]?.let { add(s[K.WORD_EXTRA_TRAD] to it) }
    x["hsk"]?.let { add(s[K.WORD_EXTRA_HSK] to it) }
    x["stem"]?.let { add(s[K.WORD_EXTRA_STEM] to it) }
    if (x["irregular"] == "yes") add(s[K.WORD_IRREGULAR] to "✓")
}
