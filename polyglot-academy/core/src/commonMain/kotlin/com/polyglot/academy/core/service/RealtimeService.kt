package com.polyglot.academy.core.service

import com.polyglot.academy.core.ai.AiCapability
import com.polyglot.academy.core.ai.AiOutcome
import com.polyglot.academy.core.ai.ChatMessage
import com.polyglot.academy.core.ai.ChatRequest
import com.polyglot.academy.core.ai.ChatRole
import com.polyglot.academy.core.ai.NetworkState
import com.polyglot.academy.core.ai.PrivacyDecision
import com.polyglot.academy.core.ai.PrivacyPolicy
import com.polyglot.academy.core.ai.ProviderId
import com.polyglot.academy.core.model.CefrLevel
import com.polyglot.academy.core.model.Conversation
import com.polyglot.academy.core.model.ConversationTurn
import com.polyglot.academy.core.model.Correction
import com.polyglot.academy.core.model.DataCategory
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.Languages
import com.polyglot.academy.core.model.ProviderLocation
import com.polyglot.academy.core.model.Speaker
import com.polyglot.academy.core.realtime.OpenAiRealtimeProtocol
import com.polyglot.academy.core.realtime.RealtimeEvent
import com.polyglot.academy.core.realtime.RealtimeSocket
import com.polyglot.academy.core.realtime.RealtimeTransport
import com.polyglot.academy.core.repo.ConversationRepository
import com.polyglot.academy.core.repo.ProfileRepository
import com.polyglot.academy.core.repo.SecretStore
import com.polyglot.academy.core.speech.AudioCapture
import com.polyglot.academy.core.speech.AudioCaptureException
import com.polyglot.academy.core.speech.AudioOutput
import com.polyglot.academy.core.speech.PcmSink
import com.polyglot.academy.core.speech.SpeechAvailability
import com.polyglot.academy.core.speech.Wav
import com.polyglot.academy.core.tutor.CoachProtocol
import com.polyglot.academy.core.tutor.TutorPersona
import com.polyglot.academy.core.tutor.TutorPromptBuilder
import com.polyglot.academy.core.tutor.TutorResponseParser
import com.polyglot.academy.core.util.IdGenerator
import com.polyglot.academy.core.util.TimeSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

enum class RealtimeReadiness { NO_KEY, NO_CONSENT, OFFLINE, READY }

enum class RealtimeStatus { IDLE, CONNECTING, LIVE, ENDED, FAILED }

enum class RealtimeProblem { NO_KEY, NO_CONSENT, OFFLINE, MICROPHONE, PERMISSION, NO_SPEAKER, CONNECTION, SERVER }

data class RealtimeState(
    val status: RealtimeStatus = RealtimeStatus.IDLE,
    val conversationId: String = "",
    val turns: List<ConversationTurn> = emptyList(),
    /** What the tutor is saying right now (transcript so far). */
    val tutorDraft: String = "",
    val tutorSpeaking: Boolean = false,
    val learnerSpeaking: Boolean = false,
    val micLevel: Float = 0f,
    val muted: Boolean = false,
    val problem: RealtimeProblem? = null,
    val serverMessage: String? = null,
    /** Learner turns whose corrections are still being checked. */
    val checking: Set<String> = emptySet(),
)

class RealtimeService(
    private val profiles: ProfileRepository,
    private val conversations: ConversationRepository,
    private val secrets: SecretStore,
    private val network: () -> NetworkState,
    private val capture: () -> AudioCapture,
    private val output: () -> AudioOutput,
    private val transport: RealtimeTransport,
    private val providers: ProviderService,
    private val coach: CoachService,
    private val clock: TimeSource,
    private val ids: IdGenerator,
) {
    suspend fun readiness(): RealtimeReadiness {
        if (secrets.get(secretKeyFor(ProviderId.OPENAI)).isNullOrBlank()) return RealtimeReadiness.NO_KEY
        val privacy = profiles.currentSettings().privacy
        if (PrivacyPolicy.evaluate(privacy, ProviderLocation.CLOUD, setOf(DataCategory.VOICE_AUDIO, DataCategory.CONVERSATION_TEXT)) !is PrivacyDecision.Allowed) return RealtimeReadiness.NO_CONSENT
        if (network() == NetworkState.OFFLINE) return RealtimeReadiness.OFFLINE
        return RealtimeReadiness.READY
    }

    fun session(scope: CoroutineScope, language: LanguageCode, tutor: TutorPersona, topic: String?) =
        RealtimeController(scope, language, tutor, TutorPromptBuilder.cleanTopic(topic.orEmpty()), this, profiles, conversations, secrets, capture(), output(),
            transport, providers, coach, clock, ids)
}

/**
 * Live speech-to-speech conversation. Audio streams both ways; the learner can interrupt the
 * tutor. Each learner transcript is checked separately with the text tutor so corrections
 * (four-part, with error log) appear in the transcript, as in typed conversations.
 */
class RealtimeController internal constructor(
    private val scope: CoroutineScope,
    val language: LanguageCode,
    val tutor: TutorPersona,
    val topic: String?,
    private val service: RealtimeService,
    private val profiles: ProfileRepository,
    private val conversations: ConversationRepository,
    private val secrets: SecretStore,
    private val capture: AudioCapture,
    private val output: AudioOutput,
    private val transport: RealtimeTransport,
    private val providers: ProviderService,
    private val coach: CoachService,
    private val clock: TimeSource,
    private val ids: IdGenerator,
) {
    private val _state = MutableStateFlow(RealtimeState())
    val state: StateFlow<RealtimeState> = _state.asStateFlow()

    private var socket: RealtimeSocket? = null
    private var sink: PcmSink? = null
    private var micJob: Job? = null
    private var readJob: Job? = null
    private var opened = false
    private var startedAt = 0L
    private var speechStartedAt: Long? = null
    private var lastSpeechMillis: Long? = null

    fun start() = scope.launch {
        if (_state.value.status == RealtimeStatus.CONNECTING || _state.value.status == RealtimeStatus.LIVE) return@launch
        val problem = when (service.readiness()) {
            RealtimeReadiness.NO_KEY -> RealtimeProblem.NO_KEY
            RealtimeReadiness.NO_CONSENT -> RealtimeProblem.NO_CONSENT
            RealtimeReadiness.OFFLINE -> RealtimeProblem.OFFLINE
            RealtimeReadiness.READY -> when (capture.availability()) {
                SpeechAvailability.AVAILABLE -> null
                SpeechAvailability.PERMISSION_REQUIRED, SpeechAvailability.PERMISSION_DENIED -> RealtimeProblem.PERMISSION
                else -> RealtimeProblem.MICROPHONE
            }
        }
        if (problem != null) { _state.update { it.copy(status = RealtimeStatus.FAILED, problem = problem) }; return@launch }
        val out = output.open(OpenAiRealtimeProtocol.SAMPLE_RATE)
        if (out == null) { _state.update { it.copy(status = RealtimeStatus.FAILED, problem = RealtimeProblem.NO_SPEAKER) }; return@launch }
        sink = out
        val id = ids.newId("conv")
        startedAt = clock.nowMillis()
        _state.update { RealtimeState(status = RealtimeStatus.CONNECTING, conversationId = id) }
        val settings = profiles.currentSettings().voiceServices
        val key = secrets.get(secretKeyFor(ProviderId.OPENAI)).orEmpty()
        val ws = try {
            transport.connect(OpenAiRealtimeProtocol.url(settings.realtimeModel), OpenAiRealtimeProtocol.headers(key))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            fail(RealtimeProblem.CONNECTION, e.message); return@launch
        }
        socket = ws
        conversations.upsertConversation(Conversation(id, language, tutor.id, topic?.let { ConversationController.TOPIC_PREFIX + it }, startedAt))
        val memory = coach.memory(language)
        ws.send(OpenAiRealtimeProtocol.sessionUpdate(instructions(memory), settings.realtimeVoice, language.tag))
        readJob = scope.launch { readLoop(ws) }
    }

    private suspend fun instructions(memory: com.polyglot.academy.core.tutor.CoachMemory): String {
        val profile = profiles.currentProfile()
        val level = profile.enrollment(language)?.startingLevel ?: CefrLevel.A1
        val target = Languages.of(language).englishName
        val explain = Languages.of(profile.explanationLanguage.takeIf { it != language } ?: if (language == LanguageCode.ENGLISH) LanguageCode.ARABIC else LanguageCode.ENGLISH).englishName
        return buildString {
            appendLine("You are ${tutor.name}, a friendly spoken-language tutor in a learning app. ${tutor.behaviourPrompt}")
            appendLine("This is a live voice conversation in $target with a learner at an estimated CEFR level ${level.label}.")
            appendLine(when {
                level <= CefrLevel.A2 -> "Speak slowly and clearly with short, simple sentences and common words. You may explain briefly in $explain if the learner is lost."
                level <= CefrLevel.B1 -> "Speak clearly at a natural but unhurried pace, with everyday language."
                else -> "Speak naturally for level ${level.label}."
            })
            appendLine("Keep each turn to one to three sentences so the learner does most of the talking.")
            appendLine("When the learner makes an error, repeat the correct form naturally in your reply (a recast) and, at most once per turn, say the correction briefly. Do not lecture.")
            appendLine("Introduce at most one new word or phrase per turn. End every turn with one specific question or a short task. Be warm, encouraging and patient.")
            appendLine("Talk about any topic the learner raises; politely decline harmful or inappropriate topics. Say so when you are not sure of a fact.")
            topic?.let { appendLine("The learner chose this topic: \"$it\".") }
            profile.coachTrack?.let { appendLine(CoachProtocol.track(it, language)) }
            if (memory.warmUp.isNotEmpty()) appendLine(CoachProtocol.warmUp(memory.warmUp))
            if (memory.recurringErrors.isNotEmpty()) appendLine(CoachProtocol.recurring(memory.recurringErrors))
            if (language == LanguageCode.ARABIC) appendLine("Use Modern Standard Arabic.")
            append("Start now: greet the learner by voice in $target and ask your first question.")
        }
    }

    private suspend fun readLoop(ws: RealtimeSocket) {
        try {
            ws.incoming.collect { raw ->
                when (val e = OpenAiRealtimeProtocol.parse(raw) ?: return@collect) {
                    RealtimeEvent.SessionReady -> if (!opened) {
                        opened = true
                        _state.update { it.copy(status = RealtimeStatus.LIVE) }
                        ws.send(OpenAiRealtimeProtocol.createResponse())
                        startMic(ws)
                    }
                    RealtimeEvent.LearnerSpeechStarted -> {
                        sink?.clear()
                        speechStartedAt = clock.nowMillis()
                        _state.update { it.copy(learnerSpeaking = true, tutorSpeaking = false) }
                    }
                    RealtimeEvent.LearnerSpeechStopped -> {
                        lastSpeechMillis = speechStartedAt?.let { clock.nowMillis() - it }
                        _state.update { it.copy(learnerSpeaking = false) }
                    }
                    is RealtimeEvent.TutorAudio -> { sink?.write(e.pcm); if (!_state.value.tutorSpeaking) _state.update { it.copy(tutorSpeaking = true) } }
                    is RealtimeEvent.TutorTranscriptDelta -> _state.update { it.copy(tutorDraft = it.tutorDraft + e.text) }
                    is RealtimeEvent.TutorTranscriptDone -> {
                        val text = e.text.ifBlank { _state.value.tutorDraft.trim() }
                        _state.update { it.copy(tutorDraft = "") }
                        if (text.isNotBlank()) addTurn(ConversationTurn(ids.newId("turn"), _state.value.conversationId, Speaker.TUTOR, text,
                            createdAtMillis = clock.nowMillis(), producedBy = "openai-realtime", wasSpoken = true))
                    }
                    is RealtimeEvent.LearnerTranscript -> learnerSaid(e.text)
                    RealtimeEvent.ResponseDone -> _state.update { it.copy(tutorSpeaking = false) }
                    is RealtimeEvent.Failure -> {
                        val fatal = e.code in setOf("invalid_api_key", "insufficient_quota", "model_not_found", "session_expired")
                        if (fatal) fail(RealtimeProblem.SERVER, e.message) else _state.update { it.copy(serverMessage = e.message) }
                    }
                }
            }
            if (_state.value.status == RealtimeStatus.LIVE || _state.value.status == RealtimeStatus.CONNECTING) {
                if (opened) end() else fail(RealtimeProblem.CONNECTION, null)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            fail(RealtimeProblem.CONNECTION, e.message)
        }
    }

    private fun startMic(ws: RealtimeSocket) {
        micJob = scope.launch {
            try {
                capture.record(OpenAiRealtimeProtocol.SAMPLE_RATE).collect { chunk ->
                    if (_state.value.muted) { _state.update { it.copy(micLevel = 0f) }; return@collect }
                    _state.update { it.copy(micLevel = Wav.level(chunk)) }
                    ws.send(OpenAiRealtimeProtocol.appendAudio(chunk))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: AudioCaptureException) {
                fail(RealtimeProblem.MICROPHONE, e.kind.name)
            } catch (e: Exception) {
                fail(RealtimeProblem.CONNECTION, e.message)
            }
        }
    }

    private suspend fun learnerSaid(text: String) {
        val turn = ConversationTurn(ids.newId("turn"), _state.value.conversationId, Speaker.LEARNER, text, createdAtMillis = clock.nowMillis(),
            wasSpoken = true, speakingMillis = lastSpeechMillis)
        addTurn(turn)
        _state.update { it.copy(checking = it.checking + turn.id) }
        val lastTutor = _state.value.turns.lastOrNull { it.speaker == Speaker.TUTOR }?.text
        scope.launch {
            val corrections = withTimeoutOrNull(25_000) { check(text, lastTutor) }.orEmpty()
            if (corrections.isNotEmpty()) {
                val updated = turn.copy(corrections = corrections)
                conversations.addTurn(updated)
                coach.record(language, corrections)
                _state.update { s -> s.copy(turns = s.turns.map { if (it.id == turn.id) updated else it }) }
            }
            _state.update { it.copy(checking = it.checking - turn.id) }
        }
    }

    /** Text-only check of one spoken sentence, with the same four-part corrections as typed chat. */
    private suspend fun check(text: String, lastTutor: String?): List<Correction> {
        val profile = profiles.currentProfile()
        val target = Languages.of(language).englishName
        val explainCode = profile.explanationLanguage.takeIf { it != language } ?: if (language == LanguageCode.ENGLISH) LanguageCode.ARABIC else LanguageCode.ENGLISH
        val explain = Languages.of(explainCode).englishName
        val level = profile.enrollment(language)?.startingLevel ?: CefrLevel.A1
        val system = """You check one spoken sentence from a $target learner at estimated CEFR level ${level.label}.
It is a speech-recognition transcript: ignore punctuation, capital letters and anything that is probably a recognition error.
Report only real grammar, vocabulary or word-choice errors, most important first, at most three. Mark optional style suggestions as stylistic.
For each error give the corrected form, a very short reason in $explain, the register of the corrected form, an error type (${CoachProtocol.errorCategories.joinToString()}), and for a minor error a short guiding question as "hint".
Respond ONLY with JSON: {"corrections":[{"original":"","corrected":"","explanation":"","severity":"minor|moderate|meaning","stylistic":false,"hint":"","register":"formal|informal|neutral","category":""}]} and an empty list if the sentence is fine."""
        val user = (lastTutor?.let { "Tutor said: $it\n" } ?: "") + "Learner said: $text"
        val outcome = providers.execute(AiCapability.STRUCTURED_FEEDBACK,
            ChatRequest(system, listOf(ChatMessage(ChatRole.USER, user)), setOf(DataCategory.CONVERSATION_TEXT), maxOutputTokens = 500, temperature = 0.1, expectJson = true))
        return (outcome as? AiOutcome.Success)?.let { TutorResponseParser.extractJsonObject(it.response.text) }?.let { TutorResponseParser.corrections(it) }.orEmpty()
    }

    private suspend fun addTurn(t: ConversationTurn) {
        conversations.addTurn(t)
        _state.update { it.copy(turns = it.turns + t) }
    }

    fun setMuted(muted: Boolean) = _state.update { it.copy(muted = muted) }

    /** Stops the tutor talking (the learner can also simply start speaking). */
    fun interrupt() = scope.launch {
        sink?.clear()
        socket?.send(OpenAiRealtimeProtocol.cancelResponse())
        _state.update { it.copy(tutorSpeaking = false) }
    }

    fun end() = scope.launch { finish(RealtimeStatus.ENDED, null, null) }

    private suspend fun fail(problem: RealtimeProblem, message: String?) = finish(RealtimeStatus.FAILED, problem, message)

    private suspend fun finish(status: RealtimeStatus, problem: RealtimeProblem?, message: String?) {
        val prev = _state.value
        if (prev.status == RealtimeStatus.ENDED || (prev.status == RealtimeStatus.FAILED && problem == null)) return
        _state.update { it.copy(status = status, problem = problem ?: it.problem, serverMessage = message ?: it.serverMessage, tutorSpeaking = false, learnerSpeaking = false, micLevel = 0f) }
        micJob?.cancel(); micJob = null
        runCatching { socket?.close() }; socket = null
        sink?.close(); sink = null
        if (prev.conversationId.isNotEmpty()) {
            val learner = prev.turns.count { it.speaker == Speaker.LEARNER }
            conversations.upsertConversation(Conversation(prev.conversationId, language, tutor.id, topic?.let { ConversationController.TOPIC_PREFIX + it }, startedAt, clock.nowMillis(),
                summary = "learner_turns=$learner corrections=${_state.value.turns.sumOf { it.corrections.size }} mode=realtime"))
        }
        readJob?.cancel()
    }
}
