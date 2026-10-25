package com.polyglot.academy.core.service

import com.polyglot.academy.core.ai.AiCapability
import com.polyglot.academy.core.ai.AiError
import com.polyglot.academy.core.ai.AiOutcome
import com.polyglot.academy.core.ai.ChatRequest
import com.polyglot.academy.core.ai.ProviderCatalog
import com.polyglot.academy.core.ai.ProviderId
import com.polyglot.academy.core.ai.RouteDecision
import com.polyglot.academy.core.content.Curriculum
import com.polyglot.academy.core.model.CefrLevel
import com.polyglot.academy.core.model.Conversation
import com.polyglot.academy.core.model.ConversationTurn
import com.polyglot.academy.core.model.DataCategory
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.Languages
import com.polyglot.academy.core.model.RolePlayScenario
import com.polyglot.academy.core.model.SavedPhrase
import com.polyglot.academy.core.model.SessionKind
import com.polyglot.academy.core.model.Speaker
import com.polyglot.academy.core.model.StudySession
import com.polyglot.academy.core.repo.ConversationRepository
import com.polyglot.academy.core.repo.ProfileRepository
import com.polyglot.academy.core.repo.VocabularyRepository
import com.polyglot.academy.core.speaking.MissionEvaluator
import com.polyglot.academy.core.speaking.MissionResult
import com.polyglot.academy.core.speaking.SpeakingMission
import com.polyglot.academy.core.speech.RecognitionEvent
import com.polyglot.academy.core.speech.SpeechAvailability
import com.polyglot.academy.core.speech.SpeechErrorKind
import com.polyglot.academy.core.speech.SpeechRecognizerService
import com.polyglot.academy.core.speech.SpeechSynthesizerService
import com.polyglot.academy.core.speech.SynthesisEvent
import com.polyglot.academy.core.speech.VoiceEvent
import com.polyglot.academy.core.speech.VoiceState
import com.polyglot.academy.core.speech.VoiceStateMachine
import com.polyglot.academy.core.speech.Visemes
import com.polyglot.academy.core.tutor.OfflineTutorEngine
import com.polyglot.academy.core.tutor.TutorContext
import com.polyglot.academy.core.tutor.TutorPersona
import com.polyglot.academy.core.tutor.TutorPromptBuilder
import com.polyglot.academy.core.tutor.TutorReply
import com.polyglot.academy.core.tutor.TutorResponseParser
import com.polyglot.academy.core.util.IdGenerator
import com.polyglot.academy.core.util.TimeSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface TutorEngineMode {
    data class Ai(val provider: ProviderId, val displayName: String, val isCloud: Boolean) : TutorEngineMode
    /** Scripted partner; [reason] says why AI is not used. */
    data class Offline(val reason: OfflineReason) : TutorEngineMode
}

enum class OfflineReason { NO_PROVIDER_CONFIGURED, CLOUD_DISABLED_BY_PRIVACY, OFFLINE_NETWORK, PROVIDER_FAILED, USER_CHOSE_OFFLINE }

enum class ConversationNotice { FELL_BACK_TO_OFFLINE, USED_FALLBACK_PROVIDER, SPEECH_UNAVAILABLE, TTS_UNAVAILABLE, PHRASE_SAVED, AI_ERROR_RETRYABLE }

data class ConversationSummary(
    val learnerTurns: Int,
    val tutorTurns: Int,
    val corrections: List<com.polyglot.academy.core.model.Correction>,
    val savedPhrases: Int,
    val scenarioCompleted: Boolean,
    val durationSeconds: Int,
    val unlockedAchievements: List<String>,
    val missionResult: MissionResult? = null,
)

data class ConversationState(
    val conversationId: String,
    val tutor: TutorPersona,
    val language: LanguageCode,
    val scenario: RolePlayScenario?,
    val turns: List<ConversationTurn> = emptyList(),
    val voice: VoiceState = VoiceState.Idle,
    val engine: TutorEngineMode = TutorEngineMode.Offline(OfflineReason.NO_PROVIDER_CONFIGURED),
    val notice: ConversationNotice? = null,
    val hint: String? = null,
    val recognitionAvailable: SpeechAvailability = SpeechAvailability.NOT_INSTALLED,
    val ttsAvailable: Boolean = false,
    val showTranslations: Boolean = true,
    val speechRate: Float = 1f,
    val summary: ConversationSummary? = null,
    val lastAiError: String? = null,
    val savedPhraseCount: Int = 0,
    val mission: SpeakingMission? = null,
    /** The mission's tasks were confirmed (by the AI tutor, or by finishing the built-in role-play). */
    val missionConfirmed: Boolean = false,
    /** The word the speech engine reports it is saying now (null if the engine gives no progress). */
    val speakingWord: String? = null,
    /** The topic of a free conversation the learner chose, if any. */
    val topic: String? = null,
) {
    /** Free conversation (no scenario or mission) without an AI tutor: the offline partner can only follow a short script. */
    val freeTalkNeedsAi: Boolean get() = scenario == null && mission == null && engine is TutorEngineMode.Offline

    /** Offline, a mission without a matching built-in role-play can only be practised freely. */
    val missionNeedsAi: Boolean get() = mission != null && engine is TutorEngineMode.Offline && mission.offlineScenario(language) == null

    val isBusy: Boolean get() = voice is VoiceState.Processing
}

/**
 * Drives one tutor conversation: chooses AI or the offline scripted partner, keeps the voice
 * state machine consistent, persists every turn and computes a local summary at the end.
 */
class ConversationController(
    private val scope: CoroutineScope,
    private val language: LanguageCode,
    private val tutor: TutorPersona,
    private val scenario: RolePlayScenario?,
    private val profiles: ProfileRepository,
    private val conversations: ConversationRepository,
    private val vocabulary: VocabularyRepository,
    private val reviewService: ReviewService,
    private val providers: ProviderService,
    private val study: StudyService,
    private val achievements: AchievementService,
    private val recognizer: SpeechRecognizerService,
    private val synthesizer: SpeechSynthesizerService,
    private val clock: TimeSource,
    private val ids: IdGenerator,
    private val forceOffline: Boolean = false,
    private val mission: SpeakingMission? = null,
    /** Free conversation on a topic the learner chose; the AI opens the conversation on it. */
    topic: String? = null,
) {
    private val topic: String? = topic?.let { TutorPromptBuilder.cleanTopic(it) }
    private val _state = MutableStateFlow(ConversationState(ids.newId("conv"), tutor, language, scenario, mission = mission, topic = this.topic))
    val state: StateFlow<ConversationState> = _state.asStateFlow()

    /** Stored with the conversation so progress (missions, scenarios) can be derived later. */
    private val conversationKey: String? = mission?.conversationKey ?: scenario?.id ?: this.topic?.let { TOPIC_PREFIX + it }
    /** Missions and chosen topics are opened by the AI itself, prompted by the start signal. */
    private val aiOpensItself = mission != null || this.topic != null
    private val missionScenario: RolePlayScenario? = mission?.offlineScenario(language)
    private var speechStartedAt: Long? = null
    private var listenStartedAt: Long = 0

    private val effectiveScenario: RolePlayScenario = scenario ?: missionScenario
        ?: Curriculum.of(language).scenarios.firstOrNull { it.trackId == com.polyglot.academy.core.model.CourseUnit.GENERAL_TRACK }
        ?: Curriculum.of(language).scenarios.first()
    private lateinit var offline: OfflineTutorEngine
    private var session: StudySession? = null
    private var listenJob: Job? = null
    private var speakJob: Job? = null
    private var explanationLanguage = LanguageCode.ENGLISH
    private var level = CefrLevel.A1
    private val localeTag = Languages.of(language).defaultVariant.localeTag

    private fun dispatch(e: VoiceEvent) = _state.update { it.copy(voice = VoiceStateMachine.reduce(it.voice, e)) }

    fun start() = scope.launch {
        val profile = profiles.currentProfile()
        val settings = profiles.currentSettings()
        explanationLanguage = profile.explanationLanguage.takeIf { it != language } ?: if (language == LanguageCode.ENGLISH) LanguageCode.ARABIC else LanguageCode.ENGLISH
        level = profile.enrollment(language)?.startingLevel ?: CefrLevel.A1
        offline = OfflineTutorEngine(effectiveScenario, explanationLanguage)
        val engine = if (forceOffline) TutorEngineMode.Offline(OfflineReason.USER_CHOSE_OFFLINE) else chooseEngine()
        val recognition = recognizer.availability(localeTag)
        val tts = synthesizer.isLanguageAvailable(localeTag)
        _state.update {
            it.copy(engine = engine, recognitionAvailable = recognition, ttsAvailable = tts, showTranslations = settings.showTranslationsByDefault,
                speechRate = settings.speech.speechRate * Languages.of(language).defaultSpeechRate, hint = offline.currentHint().takeIf { engine is TutorEngineMode.Offline })
        }
        conversations.upsertConversation(Conversation(_state.value.conversationId, language, tutor.id, conversationKey, clock.nowMillis()))
        session = study.startSession(language, SessionKind.CONVERSATION, conversationKey)
        val opening = when {
            engine is TutorEngineMode.Offline -> offline.opening()
            aiOpensItself -> aiSelfOpening() ?: if (mission != null) offline.opening() else aiOpening()
            else -> aiOpening()
        }
        addTutorTurn(opening, autoplay = settings.speech.autoPlayTutorReplies)
    }

    /** For a mission the AI opens in character; the start signal itself is not stored as a turn. */
    private suspend fun aiSelfOpening(): TutorReply? = aiReply(listOf(com.polyglot.academy.core.ai.ChatMessage(com.polyglot.academy.core.ai.ChatRole.USER, TutorPromptBuilder.START_SIGNAL)))

    private suspend fun chooseEngine(): TutorEngineMode = when (val d = providers.route(AiCapability.CHAT, setOf(DataCategory.CONVERSATION_TEXT))) {
        is RouteDecision.Route -> {
            val id = d.ordered.first()
            val desc = ProviderCatalog.of(id)
            TutorEngineMode.Ai(id, desc.displayName, desc.location == com.polyglot.academy.core.model.ProviderLocation.CLOUD)
        }
        is RouteDecision.Blocked -> TutorEngineMode.Offline(
            when {
                d.privacyBlocked -> OfflineReason.CLOUD_DISABLED_BY_PRIVACY
                d.candidates.any { it.skip == com.polyglot.academy.core.ai.SkipReason.OFFLINE } -> OfflineReason.OFFLINE_NETWORK
                else -> OfflineReason.NO_PROVIDER_CONFIGURED
            },
        )
    }

    private fun aiOpening(): TutorReply {
        // The AI conversation starts from the scenario opening so the learner always sees an immediate prompt.
        val s = scenario
        val text = s?.tutorOpening ?: greeting()
        return TutorReply(text, s?.tutorOpeningTranslation?.forLanguage(explanationLanguage), emptyList(), emptyList(), emptyList(), false, "scripted-opening", scripted = true)
    }

    private fun greeting() = when (language) {
        LanguageCode.ENGLISH -> "Hi! I'm ${tutor.name}. What would you like to talk about today?"
        LanguageCode.SPANISH -> "¡Hola! Soy ${tutor.name}. ¿De qué te gustaría hablar hoy?"
        LanguageCode.FRENCH -> "Bonjour ! Je suis ${tutor.name}. De quoi aimerais-tu parler aujourd'hui ?"
        LanguageCode.MANDARIN -> "你好！我是${tutor.name}。今天你想聊什么？(Nǐ hǎo! Jīntiān nǐ xiǎng liáo shénme?)"
        LanguageCode.ARABIC -> "مرحبًا! أنا ${tutor.name}. عمّ تحب أن نتحدث اليوم؟"
    }

    fun toggleTranslations() = _state.update { it.copy(showTranslations = !it.showTranslations) }
    fun dismissNotice() = _state.update { it.copy(notice = null) }
    fun dismissError() = dispatch(VoiceEvent.DismissError)
    fun setSpeechRate(rate: Float) = _state.update { it.copy(speechRate = rate.coerceIn(0.5f, 1.5f)) }

    fun sendText(text: String, spoken: Boolean = false, speakingMillis: Long? = null) {
        val trimmed = text.trim()
        // A spoken turn arrives while the voice state is already Processing (set by the recognizer's final result).
        if (trimmed.isEmpty() || (!spoken && _state.value.isBusy) || _state.value.summary != null) return
        synthesizer.stop(); speakJob?.cancel()
        if (!spoken) dispatch(VoiceEvent.SubmitTyped)
        scope.launch {
            val turn = ConversationTurn(ids.newId("turn"), _state.value.conversationId, Speaker.LEARNER, trimmed, createdAtMillis = clock.nowMillis(),
                wasSpoken = spoken, speakingMillis = speakingMillis.takeIf { spoken })
            conversations.addTurn(turn)
            _state.update { it.copy(turns = it.turns + turn) }
            val reply = when (val engine = _state.value.engine) {
                is TutorEngineMode.Offline -> offline.respond(trimmed)
                is TutorEngineMode.Ai -> aiReply() ?: offline.respond(trimmed)
            }
            val confirmed = reply.missionComplete || (missionScenario != null && offline.isFinished && _state.value.engine is TutorEngineMode.Offline)
            if (mission != null && confirmed) _state.update { it.copy(missionConfirmed = true) }
            // Attach corrections to the learner turn they refer to.
            if (reply.corrections.isNotEmpty()) {
                val corrected = turn.copy(corrections = reply.corrections)
                conversations.addTurn(corrected)
                _state.update { s -> s.copy(turns = s.turns.map { if (it.id == turn.id) corrected else it }) }
            }
            dispatch(VoiceEvent.ReplyReady)
            addTutorTurn(reply, autoplay = profiles.currentSettings().speech.autoPlayTutorReplies)
            _state.update { it.copy(hint = if (it.engine is TutorEngineMode.Offline) offline.currentHint() else null) }
        }
    }

    private suspend fun aiReply(override: List<com.polyglot.academy.core.ai.ChatMessage>? = null): TutorReply? {
        val s = _state.value
        val ctx = TutorContext(tutor, language, explanationLanguage, level, profiles.currentProfile().correctionIntensity, scenario, s.showTranslations, mission = mission, topic = topic)
        val turns = TutorPromptBuilder.history(s.turns.filter { it.speaker != Speaker.SYSTEM }.map { (it.speaker == Speaker.LEARNER) to it.text })
        // A mission conversation always begins with the start signal, so the model sees why it spoke first.
        val history = override ?: if (aiOpensItself) listOf(com.polyglot.academy.core.ai.ChatMessage(com.polyglot.academy.core.ai.ChatRole.USER, TutorPromptBuilder.START_SIGNAL)) + turns else turns
        val outcome = providers.execute(AiCapability.CHAT, ChatRequest(TutorPromptBuilder.systemPrompt(ctx), history, setOf(DataCategory.CONVERSATION_TEXT), expectJson = true))
        return when (outcome) {
            is AiOutcome.Success -> {
                if (outcome.usedFallback) _state.update { it.copy(notice = ConversationNotice.USED_FALLBACK_PROVIDER) }
                TutorResponseParser.parse(outcome.response.text, outcome.response.providerId.name)
            }
            is AiOutcome.Failure -> {
                val reason = if (outcome.error is AiError.BlockedByPrivacy) OfflineReason.CLOUD_DISABLED_BY_PRIVACY else OfflineReason.PROVIDER_FAILED
                _state.update { it.copy(engine = TutorEngineMode.Offline(reason), notice = ConversationNotice.FELL_BACK_TO_OFFLINE, lastAiError = outcome.error.message) }
                null
            }
        }
    }

    private suspend fun addTutorTurn(reply: TutorReply, autoplay: Boolean) {
        val turn = ConversationTurn(
            ids.newId("turn"), _state.value.conversationId, Speaker.TUTOR, reply.text,
            translation = reply.translation, alternatives = reply.alternatives, vocabularyNotes = reply.vocabulary,
            createdAtMillis = clock.nowMillis(), producedBy = reply.producedBy,
        )
        conversations.addTurn(turn)
        _state.update { it.copy(turns = it.turns + turn) }
        if (autoplay && _state.value.ttsAvailable) speak(turn)
        if (reply.finished) end()
    }

    fun speak(turn: ConversationTurn) {
        if (!_state.value.ttsAvailable) { _state.update { it.copy(notice = ConversationNotice.TTS_UNAVAILABLE) }; return }
        speakJob?.cancel()
        speakJob = scope.launch {
            val spoken = spokenText(turn.text)
            synthesizer.speak(spoken, localeTag, _state.value.speechRate).collect { e ->
                when (e) {
                    SynthesisEvent.Started -> dispatch(VoiceEvent.SpeakStarted(turn.id))
                    SynthesisEvent.Done -> { dispatch(VoiceEvent.SpeakFinished); _state.update { it.copy(speakingWord = null) } }
                    is SynthesisEvent.Error -> { dispatch(VoiceEvent.SpeakFailed(e.kind)); _state.update { it.copy(speakingWord = null) } }
                    is SynthesisEvent.Progress -> _state.update { it.copy(speakingWord = Visemes.wordAt(spoken, e.charStart)) }
                }
            }
        }
    }

    /** Pinyin in parentheses is for reading only; the TTS engine should read the characters. */
    private fun spokenText(text: String) = if (language == LanguageCode.MANDARIN) text.replace(Regex("\\([^)]*\\)"), "") else text

    fun stopSpeaking() {
        synthesizer.stop(); speakJob?.cancel(); dispatch(VoiceEvent.SpeakFinished)
        _state.update { it.copy(speakingWord = null) }
    }

    fun startListening() {
        val s = _state.value
        if (s.recognitionAvailable != SpeechAvailability.AVAILABLE) {
            _state.update { it.copy(notice = ConversationNotice.SPEECH_UNAVAILABLE) }
            return
        }
        stopSpeaking()
        dispatch(VoiceEvent.StartListening)
        listenJob?.cancel()
        listenStartedAt = clock.nowMillis()
        speechStartedAt = null
        listenJob = scope.launch {
            recognizer.listen(localeTag).collect { e ->
                dispatch(VoiceEvent.Recognition(e))
                // Speaking time runs from the first recognised words to the final result: an approximation.
                if (e is RecognitionEvent.Partial && speechStartedAt == null) speechStartedAt = clock.nowMillis()
                if (e is RecognitionEvent.Final && e.text.isNotBlank()) {
                    sendText(e.text, spoken = true, speakingMillis = clock.nowMillis() - (speechStartedAt ?: listenStartedAt))
                }
            }
        }
    }

    fun stopListening() = recognizer.stopListening()

    /** Re-checks recognizer availability, e.g. after the learner granted microphone permission. */
    suspend fun refreshSpeechAvailability(): SpeechAvailability {
        val r = recognizer.availability(localeTag)
        _state.update { it.copy(recognitionAvailable = r) }
        return r
    }

    /** Cancel recording immediately (no transcript is sent). */
    fun cancelVoice() {
        listenJob?.cancel(); listenJob = null
        stopSpeaking()
        dispatch(VoiceEvent.Cancel)
    }

    fun retryWithAi() = scope.launch {
        val engine = chooseEngine()
        _state.update { it.copy(engine = engine, notice = null) }
    }

    fun savePhrase(text: String, meaning: String) = scope.launch {
        val phrase = SavedPhrase(ids.newId("phrase"), language, text.trim(), meaning.trim(), null, _state.value.conversationId, clock.nowMillis())
        vocabulary.savePhrase(phrase)
        reviewService.addToReview(language, phrase.id)
        _state.update { it.copy(notice = ConversationNotice.PHRASE_SAVED, savedPhraseCount = it.savedPhraseCount + 1) }
    }

    fun end() = scope.launch {
        if (_state.value.summary != null) return@launch
        cancelVoice()
        val s = _state.value
        val learner = s.turns.count { it.speaker == Speaker.LEARNER }
        val start = s.turns.firstOrNull()?.createdAtMillis ?: clock.nowMillis()
        val duration = ((clock.nowMillis() - start) / 1000).toInt()
        val missionResult = mission?.let { MissionEvaluator.evaluate(it, language, s.turns.filter { t -> t.speaker == Speaker.LEARNER }, s.missionConfirmed) }
        val summaryText = "learner_turns=$learner corrections=${s.turns.sumOf { it.corrections.size }}" +
            if (missionResult?.passed == true) " ${MissionEvaluator.PASSED_MARK}" else ""
        conversations.upsertConversation(Conversation(s.conversationId, language, tutor.id, conversationKey, start, clock.nowMillis(), summary = summaryText))
        session?.let { study.updateSession(it, learner, learner, completed = true, activeSeconds = duration) }
        val unlocked = achievements.evaluateConversation(learner)
        dispatch(VoiceEvent.Finish)
        _state.update {
            it.copy(summary = ConversationSummary(learner, s.turns.count { t -> t.speaker == Speaker.TUTOR }, s.turns.flatMap { t -> t.corrections },
                s.savedPhraseCount, scenarioCompleted = this@ConversationController::offline.isInitialized && offline.isFinished, durationSeconds = duration, unlockedAchievements = unlocked,
                missionResult = missionResult))
        }
    }

    companion object {
        /** Stored as the conversation key of a chosen-topic conversation. */
        const val TOPIC_PREFIX = "topic:"
    }
}
