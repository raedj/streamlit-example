package com.polyglot.academy.ui.feature.practice

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.polyglot.academy.core.ai.AiError
import com.polyglot.academy.core.learning.ReadAloud
import com.polyglot.academy.core.learning.ReadAloudResult
import com.polyglot.academy.core.model.CefrLevel
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.Languages
import com.polyglot.academy.core.model.OpenQuestion
import com.polyglot.academy.core.model.ReadingPassage
import com.polyglot.academy.core.model.SessionKind
import com.polyglot.academy.core.service.OpenAnswerFeedback
import com.polyglot.academy.core.service.ReadingAnswerService
import com.polyglot.academy.core.service.Understanding
import com.polyglot.academy.core.speech.RecognitionEvent
import com.polyglot.academy.core.speech.SpeechAvailability
import com.polyglot.academy.core.stories.StoryText
import com.polyglot.academy.core.stories.WordInfo
import com.polyglot.academy.ui.app.LoadContent
import com.polyglot.academy.ui.app.LocalApp
import com.polyglot.academy.ui.app.ScrollScreen
import com.polyglot.academy.ui.app.rememberLoader
import com.polyglot.academy.ui.app.rememberSpeaker
import com.polyglot.academy.ui.designsystem.AppTextField
import com.polyglot.academy.ui.designsystem.Banner
import com.polyglot.academy.ui.designsystem.BannerKind
import com.polyglot.academy.ui.designsystem.ChipGroup
import com.polyglot.academy.ui.designsystem.ConfirmDialog
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
import com.polyglot.academy.ui.feature.lesson.ExerciseContext
import com.polyglot.academy.ui.feature.lesson.ExerciseView
import com.polyglot.academy.ui.navigation.SettingsSection
import com.polyglot.academy.ui.i18n.K
import com.polyglot.academy.ui.i18n.Labels
import com.polyglot.academy.ui.i18n.LocalStrings
import com.polyglot.academy.ui.navigation.Route
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private val interestSuggestions = listOf("football", "cooking", "travel", "music", "technology", "family", "animals", "history")

/** Graded stories (built in and AI-written) and a form to have the AI write one about an interest. */
@Composable
fun ReadingListScreen() {
    val app = LocalApp.current
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val scope = rememberCoroutineScope()
    val lang = app.profile.activeLearningLanguage
    val stories by remember(lang) { app.services.stories.all(lang) }.collectAsState(null)
    var interest by remember { mutableStateOf("") }
    var level by remember(lang) { mutableStateOf(app.profile.enrollment(lang)?.startingLevel ?: CefrLevel.A1) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var needsAi by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<ReadingPassage?>(null) }

    ScrollScreen(s[K.READING_TITLE]) {
        Text(s[K.STORY_INTRO], style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
        SectionHeader(s[K.STORY_CREATE])
        PolyCard {
            Text(s[K.STORY_CREATE_BODY], style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
            AppTextField(interest, { interest = it.take(80) }, s[K.STORY_INTEREST])
            ChipGroup((app.profile.interests + interestSuggestions.map { s[K.valueOf("STORY_TOPIC_" + it.uppercase())] }).distinct(), emptySet(), { it }, { interest = it })
            Text(s[K.STORY_LEVEL], style = MaterialTheme.typography.labelLarge)
            ChipGroup(listOf(CefrLevel.A1, CefrLevel.A2, CefrLevel.B1, CefrLevel.B2), setOf(level), { it.label }, { level = it })
            PrimaryButton(s[K.STORY_WRITE], {
                loading = true; error = null; needsAi = false
                scope.launch {
                    app.services.stories.generate(lang, explainFor(lang, app.profile.explanationLanguage), level, interest)
                        .onSuccess { st ->
                            val clean = interest.trim()
                            if (clean.isNotEmpty() && clean !in app.profile.interests) app.services.store.updateProfile { it.copy(interests = (listOf(clean) + it.interests).take(8)) }
                            interest = ""
                            app.nav.push(Route.ReadingPassage(st.id))
                        }
                        .onFailure { e ->
                            needsAi = e is AiError.BlockedByPrivacy || e is AiError.NoProviderAvailable
                            error = if (needsAi) s[K.STORY_NEEDS_AI] else s.f(K.STORY_FAILED, e.message ?: "")
                        }
                    loading = false
                }
            }, Modifier.fillMaxWidth(), enabled = interest.isNotBlank() && !loading, loading = loading)
            error?.let { msg ->
                if (needsAi) Banner(BannerKind.INFO, msg, action = s[K.CONV_SET_UP_AI], onAction = { app.nav.push(Route.SettingsPage(SettingsSection.PROVIDERS)) })
                else Banner(BannerKind.WARNING, msg)
            }
            Text(s[K.STORY_AI_NOTE], style = MaterialTheme.typography.bodySmall, color = c.textMuted)
        }

        val list = stories ?: return@ScrollScreen
        val mine = list.filter { it.generated }
        if (mine.isNotEmpty()) {
            SectionHeader(s[K.STORY_MINE])
            mine.forEach { st -> StoryRow(st, onDelete = { deleting = st }) }
        }
        SectionHeader(s[K.STORY_BUILT_IN])
        list.filter { !it.generated }.forEach { st -> StoryRow(st, null) }
    }
    deleting?.let { st ->
        ConfirmDialog(s[K.STORY_DELETE], s.f(K.STORY_DELETE_BODY, st.title), s[K.DELETE], s[K.CANCEL],
            onConfirm = { scope.launch { app.services.stories.delete(st) }; deleting = null }, onDismiss = { deleting = null })
    }
}

@Composable
private fun StoryRow(st: ReadingPassage, onDelete: (() -> Unit)?) {
    val app = LocalApp.current
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    PolyCard(onClick = { app.nav.push(Route.ReadingPassage(st.id)) }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                LearningText(st.title, st.language, style = MaterialTheme.typography.titleMedium)
                Text(s.gloss(st.titleTranslation), style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
            }
            LevelBadge(st.level, null)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            if (st.generated) Pill(s[K.STORY_AI_WRITTEN])
            st.interest?.let { Pill(it) }
            if (st.glossary.isNotEmpty()) Pill(s[K.STORY_TAPPABLE])
            if (onDelete != null) { Column(Modifier.weight(1f)) {}; QuietButton(s[K.DELETE], onDelete) }
        }
    }
}

private class ReaderData(val story: ReadingPassage, val vocabulary: List<com.polyglot.academy.core.model.VocabularyItem>, val known: Set<String>)

/** Reader: every word can be tapped for its meaning and saved to reviews; sentences can be played and translated. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReadingPassageScreen(passageId: String) {
    val app = LocalApp.current
    val s = LocalStrings.current
    val loader = rememberLoader(passageId) {
        app.services.stories.find(passageId)?.let { st ->
            val vocab = app.services.stories.lookupVocabulary(st.language)
            ReaderData(st, vocab, StoryText.knownTerms(st, vocab))
        }
    }
    ScrollScreen(s[K.READING_TITLE]) {
        LoadContent(loader) { data ->
            if (data == null) StateMessage(s[K.ERROR_TITLE], s[K.LESSON_NOT_FOUND]) else StoryReader(data)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StoryReader(data: ReaderData) {
    val app = LocalApp.current
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val scope = rememberCoroutineScope()
    val speaker = rememberSpeaker(s)
    val story = data.story
    val lang = story.language
    val explain = explainFor(lang, app.profile.explanationLanguage)
    var translated by remember { mutableStateOf(setOf<Int>()) }
    var selected by remember { mutableStateOf<Pair<String, Int>?>(null) }
    val saved = remember { mutableStateMapOf<String, Boolean>() }
    val answers = remember { mutableStateMapOf<String, Boolean>() }
    var finished by remember { mutableStateOf(false) }
    val tag = Languages.of(lang).defaultVariant.localeTag
    var availability by remember { mutableStateOf<SpeechAvailability?>(null) }
    LaunchedEffect(Unit) { availability = app.services.platform.recognizer.availability(tag) }
    val canRecognize = availability == SpeechAvailability.AVAILABLE || availability == SpeechAvailability.PERMISSION_REQUIRED
    var listeningAt by remember { mutableStateOf<Int?>(null) }
    var aloudJob by remember { mutableStateOf<Job?>(null) }
    val aloud = remember { mutableStateMapOf<Int, ReadAloudResult>() }
    var aloudError by remember { mutableStateOf<Pair<Int, String>?>(null) }
    fun readAloud(i: Int) {
        if (listeningAt == i) { app.services.platform.recognizer.stopListening(); return }
        aloudJob?.cancel()
        aloudJob = scope.launch {
            if (availability == SpeechAvailability.PERMISSION_REQUIRED && !app.platformUi.requestMicrophonePermission()) {
                availability = SpeechAvailability.PERMISSION_DENIED; return@launch
            }
            speaker.stop(); listeningAt = i; aloudError = null
            try {
                app.services.platform.recognizer.listen(tag).collect { ev ->
                    when (ev) {
                        is RecognitionEvent.Final -> aloud[i] = ReadAloud.compare(story.sentences[i], ev.text, lang)
                        is RecognitionEvent.Error -> aloudError = i to Labels.speechError(s, ev.kind)
                        else -> {}
                    }
                }
            } finally { listeningAt = null }
        }
    }

    LearningText(story.title, lang, style = MaterialTheme.typography.headlineSmall)
    Text(story.titleTranslation.forLanguage(explain), style = MaterialTheme.typography.bodyLarge, color = c.textSecondary)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        LevelBadge(story.level, null)
        if (story.generated) Pill(s[K.STORY_AI_WRITTEN])
    }
    if (story.generated) Banner(BannerKind.INFO, s[K.STORY_AI_CHECK])
    else Text(s[K.READING_NOT_REVIEWED], style = MaterialTheme.typography.bodySmall, color = c.textMuted)
    Banner(BannerKind.INFO, s[K.STORY_TAP_HINT])
    SecondaryButton(s[K.READING_PLAY_ALL], { speaker.toggle(story.sentences.joinToString(" "), lang) })

    PolyCard {
        story.sentences.forEachIndexed { i, sentence ->
            Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                FlowRow(verticalArrangement = Arrangement.Center) {
                    StoryText.tokens(sentence, lang, data.known).forEach { t ->
                        if (!t.isWord) LearningText(t.text, lang, style = PolyglotTheme.learning.sentence)
                        else {
                            val isSel = selected?.first == t.text && selected?.second == i
                            LearningText(t.text, lang, style = PolyglotTheme.learning.sentence, color = if (isSel) c.primary else c.textPrimary,
                                modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(if (isSel) c.selection else c.card)
                                    .clickable(role = Role.Button) { selected = t.text to i; speaker.play(t.text, lang) })
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { speaker.play(sentence, lang) }) { Icon(Icons.AutoMirrored.Filled.VolumeUp, s[K.PLAY_AUDIO]) }
                    IconButton(onClick = { translated = if (i in translated) translated - i else translated + i }) { Icon(Icons.Filled.Translate, s[K.STORY_TRANSLATE]) }
                    if (canRecognize) IconButton(onClick = { readAloud(i) }) {
                        if (listeningAt == i) Icon(Icons.Filled.Stop, s[K.CONV_MIC_STOP], tint = c.error) else Icon(Icons.Filled.Mic, s[K.READING_ALOUD])
                    }
                }
                aloud[i]?.let { ReadAloudView(it, lang) }
                aloudError?.takeIf { it.first == i }?.let { Text(it.second, style = MaterialTheme.typography.bodySmall, color = c.warning) }
                if (i in translated) Text(story.sentenceTranslations[i].forLanguage(explain), style = MaterialTheme.typography.bodyMedium, color = c.textSecondary,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
            }
        }
    }

    selected?.let { (word, sentenceIndex) ->
        val info: WordInfo? = remember(word) { StoryText.lookup(word, story, data.vocabulary) }
        PolyCard(containerColor = c.primaryContainer.copy(alpha = if (c.isDark) 0.35f else 0.5f), modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }) {
            LearningText(info?.term ?: word, lang, style = MaterialTheme.typography.titleLarge, reading = info?.reading)
            if (info == null) Text(s[K.STORY_NO_MEANING], style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
            else {
                Text(info.meaning.forLanguage(explain), style = MaterialTheme.typography.bodyLarge)
                if (saved[info.term] == true) Text(s[K.STORY_WORD_SAVED], color = c.success, style = MaterialTheme.typography.labelLarge)
                else QuietButton(s[K.STORY_SAVE_WORD], {
                    scope.launch { app.services.stories.saveWord(story, info, story.sentences[sentenceIndex]); saved[info.term] = true }
                })
            }
        }
    }

    if (story.questions.isNotEmpty()) {
        SectionHeader(s[K.READING_QUESTIONS])
        val ctx = ExerciseContext(lang, explain, speaker, hintsAvailable = false, showReading = false, traditional = false)
        story.questions.forEach { q ->
            PolyCard {
                ExerciseView(q, ctx, answered = q.id in answers) { a ->
                    answers[q.id] = a.outcome.correct
                    scope.launch { app.services.study.recordExercise(lang, null, q, a.outcome, a.userAnswer, 0, 0, a.promptText) }
                }
            }
        }
        if (answers.size == story.questions.size) {
            Banner(BannerKind.SUCCESS, s.f(K.READING_RESULT, answers.values.count { it }, story.questions.size))
            LaunchedEffect(Unit) { finished = true }
        }
    } else if (!finished) PrimaryButton(s[K.STORY_MARK_READ], { finished = true }, Modifier.fillMaxWidth())

    if (story.openQuestions.isNotEmpty()) {
        SectionHeader(s[K.READING_OPEN])
        Text(s[K.READING_OPEN_BODY], style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
        story.openQuestions.forEach { o -> OpenQuestionCard(story, o, explain) }
    }

    if (finished) {
        Text(s[K.STORY_DONE], color = c.success, style = MaterialTheme.typography.titleSmall)
        LaunchedEffect(story.id) {
            val sess = app.services.study.startSession(lang, SessionKind.READING, story.id)
            app.services.study.updateSession(sess, answers.size, answers.values.count { it }, true, 30 * story.sentences.size)
        }
    }
}

/** The sentence as the recognizer heard it: heard words in green, missed words in red. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReadAloudView(r: ReadAloudResult, lang: LanguageCode) {
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    Column(Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite }) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(if (lang == LanguageCode.MANDARIN) 0.dp else 6.dp)) {
            r.words.forEach { w -> LearningText(w.text, lang, style = MaterialTheme.typography.bodyLarge, color = if (w.heard) c.success else c.error) }
        }
        Text(s.f(K.READING_ALOUD_SCORE, (r.score * 100).toInt()), style = MaterialTheme.typography.labelLarge)
        if (r.missed.isEmpty()) Text(s[K.READING_ALOUD_ALL], style = MaterialTheme.typography.bodySmall, color = c.success)
        else Text(s.f(K.READING_ALOUD_MISSED, r.missed.joinToString(if (lang == LanguageCode.MANDARIN) " " else ", ")), style = MaterialTheme.typography.bodySmall)
        Text(s[K.READING_ALOUD_NOTE], style = MaterialTheme.typography.bodySmall, color = c.textMuted)
    }
}

/** A question answered in the learner's own words: an offline key-word check, or AI judgement and corrections when allowed. */
@Composable
private fun OpenQuestionCard(story: ReadingPassage, o: OpenQuestion, explain: LanguageCode) {
    val app = LocalApp.current
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val scope = rememberCoroutineScope()
    val lang = story.language
    var text by remember(o.id) { mutableStateOf("") }
    var feedback by remember(o.id) { mutableStateOf<OpenAnswerFeedback?>(null) }
    var aiError by remember(o.id) { mutableStateOf<String?>(null) }
    var loading by remember(o.id) { mutableStateOf(false) }
    PolyCard {
        if (o.summary) Pill(s[K.READING_RETELL])
        LearningText(o.prompt, lang, style = MaterialTheme.typography.titleMedium)
        Text(o.promptGloss.forLanguage(explain), style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
        AppTextField(text, { text = it.take(1200) }, s[K.READING_ANSWER], singleLine = false, minLines = if (o.summary) 4 else 2)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SecondaryButton(s[K.READING_CHECK_OFFLINE], {
                feedback = ReadingAnswerService.offlineCheck(text, o, lang); aiError = null
            }, Modifier.weight(1f), enabled = text.isNotBlank())
            PrimaryButton(s[K.READING_CHECK_AI], {
                loading = true
                scope.launch {
                    app.services.readingAnswers.aiCheck(text, o, story, explain)
                        .onSuccess { feedback = it; aiError = null }
                        .onFailure { e ->
                            aiError = if (e is AiError.BlockedByPrivacy || e is AiError.NoProviderAvailable) s[K.READING_AI_BLOCKED]
                            else s.f(K.READING_AI_FAILED, e.message ?: "")
                            feedback = ReadingAnswerService.offlineCheck(text, o, lang)
                        }
                    loading = false
                }
            }, Modifier.weight(1f), enabled = text.isNotBlank() && !loading, loading = loading)
        }
        Text(s[K.READING_SEND_NOTE], style = MaterialTheme.typography.bodySmall, color = c.textMuted)
    }
    aiError?.let { Banner(BannerKind.WARNING, it) }
    feedback?.let { fb -> OpenAnswerFeedbackView(fb, o, lang) }
}

@Composable
private fun OpenAnswerFeedbackView(fb: OpenAnswerFeedback, o: OpenQuestion, lang: LanguageCode) {
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    PolyCard(modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }) {
        val understanding = fb.understanding
        if (understanding == null) Text(s[K.READING_OFFLINE_NOTE], style = MaterialTheme.typography.labelMedium, color = c.textSecondary)
        else {
            Text(s.f(K.WRITING_SOURCE_AI, fb.source), style = MaterialTheme.typography.labelMedium, color = c.textSecondary)
            val (key, color) = when (understanding) {
                Understanding.YES -> K.READING_UNDERSTOOD_YES to c.success
                Understanding.PARTLY -> K.READING_UNDERSTOOD_PARTLY to c.warning
                Understanding.NO -> K.READING_UNDERSTOOD_NO to c.error
            }
            Text(s[key], style = MaterialTheme.typography.titleSmall, color = color)
        }
        if (fb.foundPoints.isNotEmpty()) Text(s.f(K.READING_POINTS_FOUND, fb.foundPoints.joinToString(", ")), style = MaterialTheme.typography.bodyMedium, color = c.success)
        if (fb.missingPoints.isNotEmpty()) Text(s.f(K.READING_POINTS_MISSING, fb.missingPoints.joinToString(", ")), style = MaterialTheme.typography.bodyMedium)
        fb.comment?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
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
    PolyCard {
        Text(s[K.READING_MODEL_ANSWER], style = MaterialTheme.typography.labelLarge, color = c.textSecondary)
        LearningText(o.modelAnswer, lang, style = MaterialTheme.typography.bodyLarge)
    }
}
