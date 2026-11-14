package com.polyglot.academy.ui.feature.tutor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.polyglot.academy.core.content.Curriculum
import com.polyglot.academy.core.model.ConversationTurn
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.Speaker
import com.polyglot.academy.core.service.ConversationController
import com.polyglot.academy.core.service.ConversationState
import com.polyglot.academy.core.service.TutorEngineMode
import com.polyglot.academy.core.speaking.MissionGap
import com.polyglot.academy.core.speaking.SpeakingMission
import com.polyglot.academy.core.speaking.SpeakingPath
import com.polyglot.academy.core.speech.SpeechAvailability
import com.polyglot.academy.core.speech.VoiceState
import com.polyglot.academy.core.tutor.TutorRoster
import com.polyglot.academy.ui.app.LocalApp
import com.polyglot.academy.ui.app.PlatformBackHandler
import com.polyglot.academy.ui.designsystem.AudioButton
import com.polyglot.academy.ui.designsystem.AvatarState
import com.polyglot.academy.ui.designsystem.Banner
import com.polyglot.academy.ui.designsystem.BannerKind
import com.polyglot.academy.ui.designsystem.ConfirmDialog
import com.polyglot.academy.ui.designsystem.ContentColumn
import com.polyglot.academy.ui.designsystem.LearningText
import com.polyglot.academy.ui.designsystem.MicButton
import com.polyglot.academy.ui.designsystem.MicVisualState
import com.polyglot.academy.ui.designsystem.Pill
import com.polyglot.academy.ui.designsystem.PolyCard
import com.polyglot.academy.ui.designsystem.PolyglotTheme
import com.polyglot.academy.ui.designsystem.PrimaryButton
import com.polyglot.academy.ui.designsystem.QuietButton
import com.polyglot.academy.ui.designsystem.ScreenScaffold
import com.polyglot.academy.ui.designsystem.SecondaryButton
import com.polyglot.academy.ui.designsystem.SectionHeader
import com.polyglot.academy.ui.designsystem.StateMessage
import com.polyglot.academy.ui.designsystem.TutorAvatar
import com.polyglot.academy.ui.i18n.K
import com.polyglot.academy.ui.i18n.Labels
import com.polyglot.academy.ui.i18n.LocalStrings
import com.polyglot.academy.ui.i18n.Localizer
import com.polyglot.academy.ui.navigation.Route
import com.polyglot.academy.ui.navigation.SettingsSection
import kotlinx.coroutines.launch

@Composable
fun ConversationScreen(tutorId: String, scenarioId: String?, offline: Boolean) {
    val app = LocalApp.current
    val s = LocalStrings.current
    val tutor = TutorRoster.byId(tutorId)
    val lang = app.profile.activeLearningLanguage
    if (tutor == null) { StateMessage(s[K.ERROR_TITLE], s[K.ERROR_BODY], action = s[K.BACK], onAction = { app.nav.pop() }); return }
    val mission = SpeakingPath.fromConversationKey(scenarioId)
    val topic = scenarioId?.takeIf { it.startsWith(ConversationController.TOPIC_PREFIX) }?.removePrefix(ConversationController.TOPIC_PREFIX)
    val scenario = scenarioId?.let { id -> Curriculum.of(lang).scenarios.firstOrNull { it.id == id } }
    val scope = rememberCoroutineScope()
    val controller = remember(tutorId, scenarioId, lang, offline) { app.services.conversation(scope, lang, tutor, scenario, offline, mission, topic) }
    LaunchedEffect(controller) { controller.start() }
    // Recording and playback must stop as soon as the learner leaves the conversation.
    DisposableEffect(controller) { onDispose { controller.cancelVoice() } }
    val state by controller.state.collectAsState()
    var confirmEnd by remember { mutableStateOf(false) }
    PlatformBackHandler(enabled = state.summary == null && state.turns.size > 1) { confirmEnd = true }
    if (confirmEnd) ConfirmDialog(s[K.CONV_END_TITLE], s[K.CONV_END_BODY], s[K.CONV_END], s[K.CANCEL],
        onConfirm = { confirmEnd = false; controller.end() }, onDismiss = { confirmEnd = false })

    ScreenScaffold(
        title = s.f(K.CONV_TITLE, tutor.name),
        onBack = { if (state.summary == null && state.turns.size > 1) confirmEnd = true else app.nav.pop() },
        backLabel = s[K.BACK],
        actions = {
            if (state.summary == null) {
                IconButton(onClick = controller::toggleTranslations) {
                    Icon(Icons.Filled.Translate, if (state.showTranslations) s[K.HIDE_TRANSLATION] else s[K.SHOW_TRANSLATION])
                }
                QuietButton(s[K.CONV_END], { confirmEnd = true })
            }
        },
    ) { padding ->
        val summary = state.summary
        if (summary != null) {
            Column(Modifier.fillMaxSize().padding(padding)) { SummaryView(state, lang) }
        } else {
            Column(Modifier.fillMaxSize().padding(padding).imePadding()) {
                EngineHeader(state, controller)
                state.mission?.let { MissionPanel(state, it) }
                FreeTalkNotice(state)
                Transcript(state, controller, lang, Modifier.weight(1f))
                InputBar(state, controller, lang)
            }
        }
    }
}

/** Shows the chosen topic, and says plainly when free conversation is running without an AI tutor. */
@Composable
private fun FreeTalkNotice(state: ConversationState) {
    val app = LocalApp.current
    val s = LocalStrings.current
    if (state.topic == null && !state.freeTalkNeedsAi) return
    ContentColumn {
        state.topic?.let { Pill(s.f(K.CONV_TOPIC, it)) }
        if (state.freeTalkNeedsAi) Banner(BannerKind.INFO, s[K.CONV_FREE_NEEDS_AI], action = s[K.CONV_SET_UP_AI],
            onAction = { app.nav.push(Route.SettingsPage(SettingsSection.PROVIDERS)) })
    }
}

/** The mission's goal and tasks, kept visible above the transcript. */
@Composable
private fun MissionPanel(state: ConversationState, mission: SpeakingMission) {
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    var open by remember { mutableStateOf(true) }
    ContentColumn {
        PolyCard(onClick = { open = !open }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(s.gloss(mission.title), style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                Pill(mission.level.label)
                if (state.missionConfirmed) Pill(s[K.MISSION_TASKS_DONE], container = c.success, content = c.onPrimary)
            }
            if (open) {
                Text(s.gloss(mission.canDo), style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                mission.tasks.forEach { Text("• " + s.gloss(it), style = MaterialTheme.typography.bodyMedium) }
                Text(s.f(K.MISSION_TARGET, mission.minTurns, mission.minWordsPerTurn), style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
            }
        }
        if (state.missionNeedsAi) Banner(BannerKind.INFO, s[K.MISSION_NEEDS_AI])
    }
}

private fun avatarState(v: VoiceState) = when (v) {
    is VoiceState.Listening -> AvatarState.LISTENING
    VoiceState.Processing -> AvatarState.THINKING
    is VoiceState.Speaking -> AvatarState.SPEAKING
    else -> AvatarState.IDLE
}

private fun voiceLabel(s: Localizer, st: ConversationState): String = when (val v = st.voice) {
    is VoiceState.Listening -> s[K.CONV_STATE_LISTENING]
    VoiceState.Processing -> s.f(K.CONV_STATE_PROCESSING, st.tutor.name)
    is VoiceState.Speaking -> s.f(K.CONV_STATE_SPEAKING, st.tutor.name)
    VoiceState.Paused -> s[K.CONV_STATE_PAUSED]
    is VoiceState.Failed -> Labels.speechError(s, v.kind)
    VoiceState.Completed -> s[K.CONV_STATE_COMPLETED]
    VoiceState.Idle -> s[K.CONV_STATE_READY]
}

@Composable
private fun EngineHeader(state: ConversationState, controller: ConversationController) {
    val app = LocalApp.current
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    ContentColumn {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TutorAvatar(state.tutor.avatar, s.f(K.TUTOR_AVATAR_DESC, state.tutor.name), size = 56.dp, state = avatarState(state.voice),
                speakingWord = state.speakingWord, language = state.language,
                mood = com.polyglot.academy.core.tutor.AvatarMotion.mood(state.turns, thinking = state.voice == VoiceState.Processing))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                val engine = state.engine
                when (engine) {
                    is TutorEngineMode.Ai -> {
                        Text(s.f(K.CONV_ENGINE_AI, engine.displayName), style = MaterialTheme.typography.labelLarge, color = c.primary)
                        Text(if (engine.isCloud) s.f(K.CONV_ENGINE_AI_CLOUD, engine.displayName) else s[K.CONV_ENGINE_AI_LOCAL], style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                    }
                    is TutorEngineMode.Offline -> {
                        Text(s[K.CONV_ENGINE_OFFLINE], style = MaterialTheme.typography.labelLarge, color = c.secondary)
                        Text(Labels.offlineReason(s, engine.reason), style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                    }
                }
                Text(voiceLabel(s, state), style = MaterialTheme.typography.bodySmall,
                    color = if (state.voice is VoiceState.Listening) c.error else c.textSecondary,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
            }
        }
        val engine = state.engine
        if (engine is TutorEngineMode.Offline) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuietButton(s[K.CONV_SET_UP_AI], { app.nav.push(Route.SettingsPage(SettingsSection.PROVIDERS)) })
            if (engine.reason == com.polyglot.academy.core.service.OfflineReason.PROVIDER_FAILED) QuietButton(s[K.CONV_TRY_AI_AGAIN], { controller.retryWithAi() })
        }
        state.notice?.let { n ->
            Banner(if (n == com.polyglot.academy.core.service.ConversationNotice.PHRASE_SAVED) BannerKind.SUCCESS else BannerKind.INFO,
                s[K.valueOf("CONV_NOTICE_${n.name}")], action = s[K.CLOSE], onAction = controller::dismissNotice)
        }
        (state.voice as? VoiceState.Failed)?.let { f -> Banner(BannerKind.WARNING, Labels.speechError(s, f.kind), action = s[K.CLOSE], onAction = controller::dismissError) }
    }
}

@Composable
private fun Transcript(state: ConversationState, controller: ConversationController, lang: LanguageCode, modifier: Modifier) {
    val s = LocalStrings.current
    val listState = rememberLazyListState()
    var saveTarget by remember { mutableStateOf<ConversationTurn?>(null) }
    LaunchedEffect(state.turns.size) { if (state.turns.isNotEmpty()) listState.animateScrollToItem(state.turns.size) }
    LazyColumn(modifier.fillMaxWidth(), state = listState, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(state.turns, key = { it.id }) { turn ->
            if (turn.speaker == Speaker.LEARNER) LearnerBubble(turn, lang) else TutorBubble(turn, state, controller, lang) { saveTarget = turn }
        }
        item {
            val engine = state.engine
            if (engine is TutorEngineMode.Ai) Text(s[K.CONV_AI_MAY_ERR], style = MaterialTheme.typography.labelSmall, color = PolyglotTheme.colors.textMuted)
        }
    }
    saveTarget?.let { t -> SavePhraseDialog(t.text.substringBefore("\n").replace(Regex("\\([^)]*\\)"), "").trim(), t.translation.orEmpty(), lang,
        onSave = { text, meaning -> controller.savePhrase(text, meaning); saveTarget = null }, onDismiss = { saveTarget = null }) }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TutorBubble(turn: ConversationTurn, state: ConversationState, controller: ConversationController, lang: LanguageCode, onSave: () -> Unit) {
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val speaking = (state.voice as? VoiceState.Speaking)?.turnId == turn.id
    Row(Modifier.fillMaxWidth()) {
        Column(
            Modifier.widthIn(max = 560.dp).clip(RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp))
                .background(c.card).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(state.tutor.name, style = MaterialTheme.typography.labelMedium, color = c.primary)
            LearningText(turn.text, lang, style = MaterialTheme.typography.bodyLarge)
            if (state.showTranslations) turn.translation?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = c.textSecondary) }
            turn.alternatives.forEach { Text(s.f(K.CONV_EXAMPLE, it), style = MaterialTheme.typography.bodySmall, color = c.info) }
            if (turn.vocabularyNotes.isNotEmpty()) {
                Text(s[K.CONV_VOCAB], style = MaterialTheme.typography.labelMedium, color = c.textSecondary)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { turn.vocabularyNotes.forEach { Pill(it) } }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                AudioButton(if (speaking) s[K.CONV_STOP_SPEAKING] else s[K.CONV_REPLAY], { if (speaking) controller.stopSpeaking() else controller.speak(turn) },
                    enabled = state.ttsAvailable, playing = speaking)
                IconButton(onClick = onSave) { Icon(Icons.Filled.BookmarkAdd, s[K.CONV_SAVE], tint = c.textSecondary) }
            }
        }
    }
}

@Composable
private fun LearnerBubble(turn: ConversationTurn, lang: LanguageCode) {
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Column(
            Modifier.widthIn(max = 520.dp).clip(RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomEnd = 16.dp, bottomStart = 16.dp))
                .background(c.primaryContainer).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(s[K.CONV_YOU] + if (turn.wasSpoken) " · ${s[K.CONV_SPOKEN]}" else "", style = MaterialTheme.typography.labelMedium, color = c.onPrimaryContainer)
            LearningText(turn.text, lang, style = MaterialTheme.typography.bodyLarge, color = c.onPrimaryContainer)
            turn.corrections.forEach { cr -> CorrectionCard(cr, lang) }
        }
    }
}

/**
 * One correction in four parts: the error, the fix, the reason and the register. For a minor
 * error with a guiding question, the fix stays hidden until the learner asks, so they can try first.
 */
@Composable
internal fun CorrectionCard(cr: com.polyglot.academy.core.model.Correction, lang: LanguageCode) {
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val socratic = cr.hint != null && cr.severity == com.polyglot.academy.core.model.ErrorSeverity.MINOR && !cr.isStylistic
    var revealed by rememberSaveable(cr.original, cr.corrected) { mutableStateOf(!socratic) }
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(c.card).padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(if (cr.isStylistic) s[K.CONV_STYLISTIC] else "${s[K.CONV_CORRECTION]} · ${Labels.severity(s, cr.severity)}" +
            (cr.category?.let { " · " + Labels.errorCategory(s, it) } ?: ""),
            style = MaterialTheme.typography.labelMedium, color = if (cr.isStylistic) c.info else c.warning)
        Text(s[K.CORR_ERROR], style = MaterialTheme.typography.labelSmall, color = c.textMuted)
        LearningText(cr.original, lang, style = MaterialTheme.typography.bodyMedium, color = c.error)
        if (!revealed) {
            Text(s[K.CORR_TRY_YOURSELF], style = MaterialTheme.typography.labelSmall, color = c.textMuted)
            LearningText(cr.hint.orEmpty(), lang, style = MaterialTheme.typography.bodyMedium)
            QuietButton(s[K.CORR_SHOW_ANSWER], { revealed = true })
        } else {
            Text(s[K.CORR_FIX], style = MaterialTheme.typography.labelSmall, color = c.textMuted)
            LearningText(cr.corrected, lang, style = MaterialTheme.typography.bodyMedium, color = c.success)
            if (cr.explanation.isNotBlank()) {
                Text(s[K.CORR_REASON], style = MaterialTheme.typography.labelSmall, color = c.textMuted)
                Text(cr.explanation, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
            }
            cr.register?.let {
                Text(s[K.CORR_CONTEXT], style = MaterialTheme.typography.labelSmall, color = c.textMuted)
                Text(Labels.register(s, it), style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
            }
        }
    }
}

@Composable
private fun InputBar(state: ConversationState, controller: ConversationController, lang: LanguageCode) {
    val app = LocalApp.current
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val scope = rememberCoroutineScope()
    var text by rememberSaveable { mutableStateOf("") }
    var showExample by remember(state.hint) { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth().background(c.surface).navigationBarsPadding().padding(horizontal = 12.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        state.hint?.let { hint ->
            Text(s.f(K.CONV_HINT, hint), style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
            val last = state.turns.lastOrNull { it.speaker == Speaker.TUTOR }
            val example = last?.alternatives?.firstOrNull()
            if (example != null) {
                if (showExample) Text(s.f(K.CONV_EXAMPLE, example), style = MaterialTheme.typography.bodySmall, color = c.info)
                else QuietButton(s[K.CONV_SHOW_EXAMPLE], { showExample = true })
            }
        }
        if (state.voice is VoiceState.Listening) {
            val partial = (state.voice as VoiceState.Listening).partial
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(10.dp).clip(CircleShape).background(c.error))
                Spacer(Modifier.width(8.dp))
                Text(partial.ifBlank { s[K.CONV_STATE_LISTENING] }, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f).semantics { liveRegion = LiveRegionMode.Polite })
                QuietButton(s[K.CONV_CANCEL_RECORDING], controller::cancelVoice, color = c.error)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = text, onValueChange = { text = it }, modifier = Modifier.weight(1f), placeholder = { Text(s[K.CONV_TYPE]) },
                maxLines = 4, shape = RoundedCornerShape(16.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(localeList = com.polyglot.academy.ui.designsystem.localeFor(com.polyglot.academy.core.model.Languages.of(lang).defaultVariant.localeTag)),
                enabled = !state.isBusy,
            )
            IconButton(onClick = { controller.sendText(text); text = "" }, enabled = text.isNotBlank() && !state.isBusy) {
                Icon(Icons.AutoMirrored.Filled.Send, s[K.CONV_SEND], tint = if (text.isNotBlank()) c.primary else c.onDisabled)
            }
            val avail = state.recognitionAvailable
            val micState = when {
                state.voice is VoiceState.Listening -> MicVisualState.LISTENING
                state.voice is VoiceState.Processing -> MicVisualState.PROCESSING
                avail == SpeechAvailability.AVAILABLE || avail == SpeechAvailability.PERMISSION_REQUIRED -> MicVisualState.IDLE
                else -> MicVisualState.UNAVAILABLE
            }
            MicButton(
                micState,
                label = when (micState) { MicVisualState.LISTENING -> s[K.CONV_MIC_STOP]; MicVisualState.UNAVAILABLE -> s[K.CONV_MIC_UNAVAILABLE]; else -> s[K.CONV_MIC_START] },
                stateLabel = voiceLabel(s, state),
                level = (state.voice as? VoiceState.Listening)?.level ?: 0f,
                onClick = {
                    when (micState) {
                        MicVisualState.LISTENING -> controller.stopListening()
                        MicVisualState.UNAVAILABLE -> controller.startListening() // shows the explanatory notice
                        else -> scope.launch {
                            if (avail == SpeechAvailability.PERMISSION_REQUIRED) {
                                app.platformUi.requestMicrophonePermission()
                                controller.refreshSpeechAvailability()
                            }
                            controller.startListening()
                        }
                    }
                },
            )
        }
        Text(s[K.CONV_PRIVACY_NOTE], style = MaterialTheme.typography.labelSmall, color = c.textMuted)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(s[K.CONV_SPEED], style = MaterialTheme.typography.labelSmall, color = c.textSecondary)
            Slider(state.speechRate, controller::setSpeechRate, valueRange = 0.5f..1.5f, modifier = Modifier.weight(1f).semantics { contentDescription = s[K.CONV_SPEED] })
        }
    }
}

@Composable
private fun SavePhraseDialog(initial: String, meaning: String, lang: LanguageCode, onSave: (String, String) -> Unit, onDismiss: () -> Unit) {
    val s = LocalStrings.current
    var text by remember { mutableStateOf(initial) }
    var m by remember { mutableStateOf(meaning) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s[K.CONV_SAVE_TITLE]) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(text, { text = it }, label = { Text(s[K.CONV_SAVE_PHRASE]) },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(localeList = com.polyglot.academy.ui.designsystem.localeFor(com.polyglot.academy.core.model.Languages.of(lang).defaultVariant.localeTag)))
                OutlinedTextField(m, { m = it }, label = { Text(s[K.CONV_SAVE_MEANING]) })
            }
        },
        confirmButton = { QuietButton(s[K.SAVE], { onSave(text, m) }, enabled = text.isNotBlank()) },
        dismissButton = { QuietButton(s[K.CANCEL], onDismiss) },
    )
}

@Composable
private fun SummaryView(state: ConversationState, lang: LanguageCode) {
    val app = LocalApp.current
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val sum = state.summary ?: return
    Column(Modifier.fillMaxSize().padding(bottom = 24.dp)) {
        ContentColumn(padding = PaddingValues(top = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TutorAvatar(state.tutor.avatar, s.f(K.TUTOR_AVATAR_DESC, state.tutor.name), size = 64.dp)
                Spacer(Modifier.width(12.dp))
                Text(s[K.CONV_SUMMARY_TITLE], style = MaterialTheme.typography.headlineSmall)
            }
            PolyCard {
                Text(s.plural(K.CONV_SUMMARY_TURNS, sum.learnerTurns), style = MaterialTheme.typography.titleMedium)
                Text(s.f(K.CONV_SUMMARY_DURATION, Labels.clockTime(sum.durationSeconds)), color = c.textSecondary)
                if (sum.savedPhrases > 0) Text(s.plural(K.CONV_SUMMARY_SAVED, sum.savedPhrases), color = c.textSecondary)
                if (sum.scenarioCompleted) Text(s[K.CONV_SUMMARY_SCENARIO_DONE], color = c.success)
            }
            sum.missionResult?.let { r ->
                if (r.passed) Banner(BannerKind.SUCCESS, s[K.MISSION_PASSED])
                else Banner(BannerKind.INFO, s[K.MISSION_NOT_YET] + " " + r.gaps.joinToString(" ") { gap ->
                    when (gap) {
                        MissionGap.TOO_FEW_TURNS -> s.f(K.MISSION_GAP_TURNS, r.learnerTurns, state.mission?.minTurns ?: 0)
                        MissionGap.ANSWERS_TOO_SHORT -> s.f(K.MISSION_GAP_WORDS, r.medianWords, state.mission?.minWordsPerTurn ?: 0)
                        MissionGap.TASKS_NOT_CONFIRMED -> s[K.MISSION_GAP_TASKS]
                    }
                })
            }
            sum.unlockedAchievements.forEach { Banner(BannerKind.SUCCESS, s.f(K.SUMMARY_UNLOCKED, Labels.achievement(s, it))) }
            SectionHeader(s[K.CONV_SUMMARY_CORRECTIONS])
            if (sum.corrections.isEmpty()) Text(s[K.CONV_SUMMARY_NO_CORRECTIONS], color = c.textSecondary)
            sum.corrections.forEach { cr ->
                PolyCard {
                    LearningText(cr.original, lang, style = MaterialTheme.typography.bodyMedium, color = c.error)
                    LearningText(cr.corrected, lang, style = MaterialTheme.typography.bodyLarge, color = c.success)
                    if (cr.explanation.isNotBlank()) Text(cr.explanation, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                }
            }
            val again = state.mission?.conversationKey ?: state.scenario?.id ?: state.topic?.let { ConversationController.TOPIC_PREFIX + it }
            PrimaryButton(s[K.CONV_SUMMARY_NEW], { app.nav.replace(Route.Conversation(state.tutor.id, again)) }, Modifier.fillMaxWidth())
            if (state.mission != null) SecondaryButton(s[K.SPEAKING_TITLE], { app.nav.replace(Route.SpeakingPath) }, Modifier.fillMaxWidth())
            SecondaryButton(s[K.SUMMARY_HOME], { app.nav.home() }, Modifier.fillMaxWidth())
        }
    }
}
