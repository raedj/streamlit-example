package com.polyglot.academy.data

import com.polyglot.academy.core.ai.AiError
import com.polyglot.academy.core.ai.AiProvider
import com.polyglot.academy.core.ai.ChatRequest
import com.polyglot.academy.core.ai.ChatResponse
import com.polyglot.academy.core.ai.ConnectionStatus
import com.polyglot.academy.core.ai.ConnectionTestResult
import com.polyglot.academy.core.ai.NetworkState
import com.polyglot.academy.core.ai.ProviderCatalog
import com.polyglot.academy.core.ai.ProviderConfig
import com.polyglot.academy.core.ai.ProviderId
import com.polyglot.academy.core.content.Curriculum
import com.polyglot.academy.core.device.DeviceInfo
import com.polyglot.academy.core.learning.RecommendationKind
import com.polyglot.academy.core.media.ExtractionSupport
import com.polyglot.academy.core.model.CefrLevel
import com.polyglot.academy.core.model.Exercise
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.LanguageEnrollment
import com.polyglot.academy.core.model.MaterialStatus
import com.polyglot.academy.core.model.MaterialType
import com.polyglot.academy.core.model.PrivacyConsent
import com.polyglot.academy.core.model.RecallRating
import com.polyglot.academy.core.model.Speaker
import com.polyglot.academy.core.repo.InMemorySecretStore
import com.polyglot.academy.core.service.AppServices
import com.polyglot.academy.core.service.ConversationNotice
import com.polyglot.academy.core.service.ExerciseOutcome
import com.polyglot.academy.core.service.ImportInput
import com.polyglot.academy.core.service.ImportProgress
import com.polyglot.academy.core.service.OfflineReason
import com.polyglot.academy.core.service.PlatformServices
import com.polyglot.academy.core.service.TutorEngineMode
import com.polyglot.academy.core.service.WritingCheck
import com.polyglot.academy.core.speech.RecognitionEvent
import com.polyglot.academy.core.speech.SpeechAvailability
import com.polyglot.academy.core.speech.SpeechRecognizerService
import com.polyglot.academy.core.speech.SpeechSynthesizerService
import com.polyglot.academy.core.speech.SynthesisEvent
import com.polyglot.academy.core.speech.VoiceState
import com.polyglot.academy.core.tutor.TutorRoster
import com.polyglot.academy.core.util.FixedTimeSource
import com.polyglot.academy.core.util.SequentialIdGenerator
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.TimeZone
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FakeRecognizer(var events: List<RecognitionEvent>) : SpeechRecognizerService {
    var available = SpeechAvailability.AVAILABLE
    override suspend fun availability(localeTag: String) = available
    override fun listen(localeTag: String): Flow<RecognitionEvent> = flowOf(*events.toTypedArray())
    override fun stopListening() {}
    override val processesAudioOnDevice: Boolean? = true
}

class FakeSynth : SpeechSynthesizerService {
    val spoken = mutableListOf<String>()
    override suspend fun isLanguageAvailable(localeTag: String) = true
    override fun speak(text: String, localeTag: String, rate: Float): Flow<SynthesisEvent> { spoken += text; return flowOf(SynthesisEvent.Started, SynthesisEvent.Done) }
    override fun stop() {}
}

class FakePlatform : PlatformServices {
    override val recognizer = FakeRecognizer(listOf(RecognitionEvent.Ready, RecognitionEvent.Partial("I'd"), RecognitionEvent.Final("I'd like a coffee, please", 0.9f, emptyList())))
    override val synthesizer = FakeSynth()
    override val secrets = InMemorySecretStore()
    var net = NetworkState.ONLINE
    override fun network() = net
    override fun deviceInfo() = DeviceInfo(6000, 10000, 34, 8, true, false, 90, false, false, true, "test")
    override val extractionSupport = mapOf(MaterialType.PDF to ExtractionSupport.NOT_IMPLEMENTED)
    var reminder: Triple<Boolean, Int, Int>? = null
    override suspend fun scheduleReminder(enabled: Boolean, hour: Int, minute: Int, days: Set<Int>): Boolean { reminder = Triple(enabled, hour, minute); return true }
    override val platformName = "test"
    var capture: com.polyglot.academy.core.speech.AudioCapture = com.polyglot.academy.core.speech.NoAudioCapture
    override val audioCapture get() = capture
    val output = FakeOutput()
    override val audioOutput: com.polyglot.academy.core.speech.AudioOutput = output
}

class FakeCapture(private val chunks: List<ByteArray>) : com.polyglot.academy.core.speech.AudioCapture {
    override suspend fun availability() = SpeechAvailability.AVAILABLE
    override fun record(sampleRate: Int) = kotlinx.coroutines.flow.flow { chunks.forEach { emit(it) }; kotlinx.coroutines.awaitCancellation() }
}

class FakeOutput : com.polyglot.academy.core.speech.AudioOutput {
    val written = java.util.concurrent.CopyOnWriteArrayList<Byte>()
    @Volatile var clears = 0
    @Volatile var closed = false
    override fun open(sampleRate: Int) = object : com.polyglot.academy.core.speech.PcmSink {
        override fun write(pcm: ByteArray) { written.addAll(pcm.toList()) }
        override fun clear() { clears++ }
        override fun close() { closed = true }
    }
}

class FakeSocket : com.polyglot.academy.core.realtime.RealtimeSocket {
    val toClient = kotlinx.coroutines.channels.Channel<String>(kotlinx.coroutines.channels.Channel.UNLIMITED)
    val sent = java.util.concurrent.CopyOnWriteArrayList<String>()
    override val incoming = toClient.receiveAsFlow()
    override suspend fun send(text: String) { sent += text }
    override suspend fun close() { toClient.close() }
}

class FakeTransport : com.polyglot.academy.core.realtime.RealtimeTransport {
    val socket = FakeSocket()
    var url = ""; var headers = emptyMap<String, String>()
    override suspend fun connect(url: String, headers: Map<String, String>): com.polyglot.academy.core.realtime.RealtimeSocket { this.url = url; this.headers = headers; return socket }
}

/** A WebDAV server in memory, shared by several test devices. */
class MemoryRemote : com.polyglot.academy.core.sync.SyncRemote {
    var file: ByteArray? = null
    var version = 0
    var lastPassword = ""
    override suspend fun read() = file?.let { com.polyglot.academy.core.sync.RemoteFile(it, "\"v$version\"") }
    override suspend fun write(bytes: ByteArray, ifMatch: String?, exists: Boolean): String? {
        if (ifMatch != null && ifMatch != "\"v$version\"") throw com.polyglot.academy.core.sync.SyncError.Changed
        if (!exists && file != null) throw com.polyglot.academy.core.sync.SyncError.Changed
        file = bytes; version++
        return "\"v$version\""
    }
}

class FakeAssessor : com.polyglot.academy.core.pronunciation.PronunciationAssessor {
    override val name = "Fake speech"
    val references = mutableListOf<String>()
    override suspend fun assess(wav: ByteArray, referenceText: String, localeTag: String): com.polyglot.academy.core.pronunciation.PronunciationReport {
        references += "$referenceText|$localeTag|${String(wav.copyOfRange(0, 4))}"
        return com.polyglot.academy.core.pronunciation.AzureResponse.parse(DETAILED, name)
    }
    override suspend fun verify() = true
    companion object {
        const val DETAILED = """{"RecognitionStatus":"Success","NBest":[{"Display":"Hello world.","PronunciationAssessment":{"AccuracyScore":70,"PronScore":75},
            "Words":[{"Word":"hello","PronunciationAssessment":{"AccuracyScore":95,"ErrorType":"None"}},
            {"Word":"world","PronunciationAssessment":{"AccuracyScore":40,"ErrorType":"Mispronunciation"},"Phonemes":[{"Phoneme":"ɝ","PronunciationAssessment":{"AccuracyScore":22}}]}]}]}"""
    }
}

class ScriptedProvider(override val descriptor: com.polyglot.academy.core.ai.ProviderDescriptor, val replies: MutableList<Result<String>>) : AiProvider {
    val requests = mutableListOf<ChatRequest>()
    override suspend fun complete(request: ChatRequest): ChatResponse {
        requests += request
        val r = replies.removeAt(0)
        return ChatResponse(r.getOrThrow(), descriptor.id, "fake", 10, 5)
    }
    override suspend fun testConnection() = runCatching { kotlinx.coroutines.runBlocking { complete(ChatRequest("", emptyList(), emptySet())) } }
        .fold({ ConnectionTestResult(true, 5, null, "fake") }, { ConnectionTestResult(false, null, it as? AiError ?: AiError.Network(descriptor.id, "x"), null) })
}

class ServicesIntegrationTest {
    private val clock = FixedTimeSource(1_761_300_000_000L)
    private val platform = FakePlatform()
    private val openai = ScriptedProvider(ProviderCatalog.OPENAI, mutableListOf())
    private val store = SqlStore(createJvmDriver(), Dispatchers.IO, TimeZone.UTC, clock)
    private val assessor = FakeAssessor()
    private val transport = FakeTransport()
    private val app = AppServices(store, platform, HttpClient(), clock, SequentialIdGenerator(), providerFactory = { _, _ -> listOf(openai) },
        assessorFactory = { _, _ -> assessor }, realtimeTransport = transport, syncRemoteFactory = { _, _, pw -> remote.also { it.lastPassword = pw } })
    private val remote = MemoryRemote()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @AfterTest fun tearDown() = scope.cancel()

    private suspend fun enroll(lang: LanguageCode = LanguageCode.ENGLISH, level: CefrLevel = CefrLevel.A1) = store.updateProfile {
        it.copy(activeLearningLanguage = lang, onboardingCompleted = true, explanationLanguage = LanguageCode.ARABIC,
            learningLanguages = listOf(LanguageEnrollment(lang, "x", level, levelFromAssessment = false)))
    }

    private suspend fun enableCloud() {
        store.updateSettings { it.copy(privacy = it.privacy.copy(cloudAiEnabled = true, consentVersionAccepted = PrivacyConsent.CURRENT_VERSION)) }
        store.save(ProviderConfig(ProviderId.OPENAI, enabled = true))
        app.providers.saveKey(ProviderId.OPENAI, "sk-test-key-0000")
        store.setPreferred(ProviderId.OPENAI)
    }

    private suspend fun waitFor(timeout: Long = 5_000, cond: suspend () -> Boolean) = withTimeout(timeout) { while (!cond()) delay(20) }

    @Test fun newLearnerDashboardRecommendsPlacementWithoutFakeStats() = runBlocking<Unit> {
        val snap = app.dashboard.snapshot()
        assertTrue(snap.isNewLearner)
        assertEquals(RecommendationKind.ASSESSMENT, snap.recommendation.kind)
        assertEquals(0, snap.minutesToday)
        assertTrue(snap.summary.observedMinutesByDay.all { it.value == 0.0 })
    }

    @Test fun lessonFlowUpdatesProgressReviewsAchievementsAndDashboard() = runBlocking<Unit> {
        enroll()
        val lesson = Curriculum.of(LanguageCode.ENGLISH).lessonsById.getValue("en_l_greetings")
        val session = app.study.startSession(LanguageCode.ENGLISH, com.polyglot.academy.core.model.SessionKind.LESSON, lesson.id)
        val fill = lesson.exercises.first { it is Exercise.FillInBlank }
        app.study.recordExercise(LanguageCode.ENGLISH, lesson.id, fill, ExerciseOutcome(false, 0.0, "is"), "are", 3000, 0, "My name ___ Sara.")
        app.study.recordExercise(LanguageCode.ENGLISH, lesson.id, fill, ExerciseOutcome(true, 1.0, "is"), "is", 2000, 0, "My name ___ Sara.")
        val unlocked = app.study.completeLesson(lesson, 0.9)
        app.study.updateSession(session, 10, 9, true, 300)
        assertTrue("first_lesson" in unlocked)
        assertTrue(store.lessonProgress(LanguageCode.ENGLISH).first().getValue(lesson.id).mastered)
        assertEquals(lesson.vocabularyIds.size, store.states(LanguageCode.ENGLISH).first().size)
        assertEquals(1, store.mistakes(LanguageCode.ENGLISH).first().size)
        val snap = app.dashboard.snapshot()
        assertTrue(!snap.isNewLearner)
        assertEquals(5, snap.minutesToday)
        assertEquals("en_l_introductions", snap.nextLesson?.id)
        assertEquals(lesson.vocabularyIds.size, snap.dueReviews)
    }

    @Test fun reviewQueueAndRating() = runBlocking<Unit> {
        enroll()
        app.study.introduceVocabulary(LanguageCode.ENGLISH, listOf("en_hello", "en_coffee"))
        val queue = app.reviews.dueQueue(LanguageCode.ENGLISH)
        assertEquals(setOf("hello", "coffee"), queue.map { it.front }.toSet())
        val (state, _) = app.reviews.rate(queue.first(), RecallRating.EASY, 1000)
        assertEquals(4.0, state.intervalDays)
        assertEquals(1, app.reviews.dueCount(LanguageCode.ENGLISH))
        clock.advanceDays(5.0)
        assertEquals(2, app.reviews.dueCount(LanguageCode.ENGLISH))
    }

    @Test fun planIsGeneratedOnceAndCompletionPersists() = runBlocking<Unit> {
        enroll()
        val plan = app.plans.today()
        assertTrue(plan.items.any { it.kind == com.polyglot.academy.core.planner.PlanItemKind.LESSON })
        assertTrue(plan.totalMinutes <= 15)
        val updated = app.plans.setCompleted(plan.items.first().id, true)
        assertTrue(updated.items.first().completed)
        assertTrue(app.plans.today().items.first().completed)
    }

    @Test fun assessmentResultAppliesLevel() = runBlocking<Unit> {
        val s = app.assessments.start(LanguageCode.SPANISH, CefrLevel.A1)
        while (!s.isFinished) s.answer(s.current!!.question.correctIndex)
        val r = app.assessments.finish(s, LanguageCode.SPANISH)
        assertEquals(1, store.assessments(LanguageCode.SPANISH).first().size)
        app.assessments.applyStartingLevel(LanguageCode.SPANISH, r.estimatedLevel, true)
        val e = store.currentProfile().enrollment(LanguageCode.SPANISH)!!
        assertEquals(r.estimatedLevel, e.startingLevel)
        assertTrue(e.levelFromAssessment)
    }

    @Test fun providerConnectionOnlyVerifiedWhenItAnswers() = runBlocking<Unit> {
        enableCloud()
        openai.replies += Result.success("ok")
        assertTrue(app.providers.testConnection(ProviderId.OPENAI).ok)
        assertEquals(ConnectionStatus.VERIFIED, store.current().getValue(ProviderId.OPENAI).lastTest)
        openai.replies += Result.failure(AiError.Authentication(ProviderId.OPENAI))
        assertTrue(!app.providers.testConnection(ProviderId.OPENAI).ok)
        assertEquals(ConnectionStatus.FAILED, store.current().getValue(ProviderId.OPENAI).lastTest)
        app.providers.saveKey(ProviderId.OPENAI, "sk-new-key-1111")
        assertEquals(ConnectionStatus.NEVER_TESTED, store.current().getValue(ProviderId.OPENAI).lastTest)
        assertEquals("sk-new-key-1111", platform.secrets.get("provider_api_key_openai"))
    }

    @Test fun offlineConversationWithVoiceAndSummary() = runBlocking<Unit> {
        enroll()
        val scenario = Curriculum.of(LanguageCode.ENGLISH).scenarios.first { it.id == "en_s_cafe" }
        val c = app.conversation(scope, LanguageCode.ENGLISH, TutorRoster.byId("emma")!!, scenario)
        c.start()
        waitFor { c.state.value.turns.isNotEmpty() }
        assertEquals(TutorEngineMode.Offline(OfflineReason.NO_PROVIDER_CONFIGURED), c.state.value.engine)
        assertEquals(scenario.tutorOpening, c.state.value.turns.first().text)
        c.startListening()
        waitFor { c.state.value.turns.size >= 3 }
        val learner = c.state.value.turns[1]
        assertEquals(Speaker.LEARNER, learner.speaker)
        assertTrue(learner.wasSpoken)
        assertEquals("Sure. Would you like anything to eat?", c.state.value.turns[2].text)
        waitFor { c.state.value.voice is VoiceState.Idle }
        assertTrue(platform.synthesizer.spoken.isNotEmpty())
        c.savePhrase("I'd like a coffee", "أريد قهوة")
        waitFor { c.state.value.savedPhraseCount == 1 }
        c.end()
        waitFor { c.state.value.summary != null }
        assertEquals(1, c.state.value.summary!!.learnerTurns)
        assertEquals(1, c.state.value.summary!!.savedPhrases)
        assertEquals(VoiceState.Completed, c.state.value.voice)
        assertEquals(3, store.turns(c.state.value.conversationId).first().size)
        assertEquals(1, store.savedPhrases(LanguageCode.ENGLISH).first().size)
    }

    @Test fun aiSpeakingMissionOpensInCharacterAndPassesWhenTasksAreDone() = runBlocking<Unit> {
        enroll(LanguageCode.ENGLISH, CefrLevel.A1); enableCloud()
        val mission = com.polyglot.academy.core.speaking.SpeakingPath.of("a1_intro")!!
        openai.replies += Result.success("""{"reply":"Hi! I'm new here. What's your name?"}""")
        repeat(3) { openai.replies += Result.success("""{"reply":"Nice! Tell me more.","mission_complete":false}""") }
        openai.replies += Result.success("""{"reply":"Great, you did everything!","mission_complete":true}""")
        val c = app.conversation(scope, LanguageCode.ENGLISH, TutorRoster.byId("emma")!!, null, mission = mission)
        c.start(); waitFor { c.state.value.turns.isNotEmpty() }
        assertEquals("Hi! I'm new here. What's your name?", c.state.value.turns.first().text)
        val first = openai.requests.first()
        assertEquals(com.polyglot.academy.core.tutor.TutorPromptBuilder.START_SIGNAL, first.messages.single().content)
        assertTrue("Say where you are from" in first.system)
        listOf("My name is Laila Hassan", "I am from Amman in Jordan", "I work as a nurse there", "I like my job very much").forEachIndexed { i, t ->
            c.sendText(t); waitFor { c.state.value.turns.size >= 3 + 2 * i }
        }
        assertTrue(c.state.value.missionConfirmed)
        // Later requests still start with the start signal, then the real conversation.
        assertEquals(com.polyglot.academy.core.tutor.TutorPromptBuilder.START_SIGNAL, openai.requests.last().messages.first().content)
        c.end(); waitFor { c.state.value.summary != null }
        assertTrue(c.state.value.summary!!.missionResult!!.passed)
        val a1 = app.speaking.progress(LanguageCode.ENGLISH).first()
        assertTrue(a1.missions.first { it.mission.id == "a1_intro" }.passed)
        assertEquals("a1_cafe", com.polyglot.academy.core.speaking.SpeakingProgressCalculator.next(app.speaking.progress(LanguageCode.ENGLISH))?.id)
        val fluency = app.speaking.fluency(LanguageCode.ENGLISH)
        assertEquals(4, fluency.learnerTurns)
        assertEquals(6, fluency.medianWordsPerTurn) // turns of 5, 6, 6 and 6 words
    }

    @Test fun offlineMissionWithoutRolePlayIsFreePracticeAndDoesNotPass() = runBlocking<Unit> {
        enroll(LanguageCode.FRENCH, CefrLevel.B1)
        val mission = com.polyglot.academy.core.speaking.SpeakingPath.of("b1_story")!!
        val c = app.conversation(scope, LanguageCode.FRENCH, TutorRoster.byId("emma")!!, null, mission = mission)
        c.start(); waitFor { c.state.value.turns.isNotEmpty() }
        assertTrue(c.state.value.missionNeedsAi)
        c.startListening(); waitFor { c.state.value.turns.size >= 3 }
        val spoken = c.state.value.turns[1]
        assertTrue(spoken.wasSpoken && spoken.speakingMillis != null)
        c.end(); waitFor { c.state.value.summary != null }
        val result = c.state.value.summary!!.missionResult!!
        assertFalse(result.passed)
        assertTrue(com.polyglot.academy.core.speaking.MissionGap.TASKS_NOT_CONFIRMED in result.gaps)
        assertTrue(store.conversations(LanguageCode.FRENCH).first().single().scenarioId == mission.conversationKey)
    }

    @Test fun aiTutorTalksAboutAChosenTopicAndCorrectsMistakes() = runBlocking<Unit> {
        enroll(LanguageCode.SPANISH, CefrLevel.A2); enableCloud()
        openai.replies += Result.success("""{"reply":"¡Qué bien! ¿Qué deporte te gusta más?"}""")
        openai.replies += Result.success("""{"reply":"¡El fútbol es genial! ¿Juegas tú también?","corrections":[{"original":"Me gusta mucho el futbol y juego cada semanas","corrected":"Me gusta mucho el fútbol y juego cada semana","explanation":"cada + مفرد: cada semana","severity":"moderate"}]}""")
        val c = app.conversation(scope, LanguageCode.SPANISH, TutorRoster.byId("emma")!!, null, topic = "el deporte\n")
        c.start(); waitFor { c.state.value.turns.isNotEmpty() }
        assertEquals("¡Qué bien! ¿Qué deporte te gusta más?", c.state.value.turns.first().text)
        assertEquals("el deporte", c.state.value.topic)
        assertEquals(com.polyglot.academy.core.tutor.TutorPromptBuilder.START_SIGNAL, openai.requests.first().messages.single().content)
        assertTrue("\"el deporte\"" in openai.requests.first().system)
        c.sendText("Me gusta mucho el futbol y juego cada semanas")
        waitFor { c.state.value.turns.size >= 3 }
        assertEquals("Me gusta mucho el fútbol y juego cada semana", c.state.value.turns[1].corrections.single().corrected)
        assertFalse(c.state.value.freeTalkNeedsAi)
        c.end(); waitFor { c.state.value.summary != null }
        assertEquals("topic:el deporte", store.conversations(LanguageCode.SPANISH).first().single().scenarioId)
    }

    @Test fun freeTalkWithoutAiSaysSoInsteadOfPretending() = runBlocking<Unit> {
        enroll(LanguageCode.FRENCH)
        val c = app.conversation(scope, LanguageCode.FRENCH, TutorRoster.byId("emma")!!, null, topic = "le cinéma")
        c.start(); waitFor { c.state.value.turns.isNotEmpty() }
        assertTrue(c.state.value.freeTalkNeedsAi)
        assertIs<TutorEngineMode.Offline>(c.state.value.engine)
    }

    @Test fun dailyLoopEarnsAndStoresAStreakFreeze() = runBlocking<Unit> {
        enroll()
        val day = 86_400_000L
        (0..6).forEach { back ->
            store.upsertSession(com.polyglot.academy.core.model.StudySession("rev$back", LanguageCode.ENGLISH, com.polyglot.academy.core.model.SessionKind.REVIEW, null,
                clock.nowMillis() - back * day, null, 300, 10, 9, true))
        }
        val loop = app.gamification.today(LanguageCode.ENGLISH)
        assertEquals(7, loop.streakDays)
        assertEquals(1, loop.freezes)
        assertEquals(10 * com.polyglot.academy.core.gamification.XpRules.REVIEW, loop.today.baseXp)
        assertEquals(1, store.currentProfile().streaks.getValue(LanguageCode.ENGLISH).freezes)
    }

    @Test fun aiStoryIsWrittenStoredAndItsWordsCanBeSaved() = runBlocking<Unit> {
        enroll(LanguageCode.SPANISH)
        assertTrue(app.stories.generate(LanguageCode.SPANISH, LanguageCode.ARABIC, CefrLevel.A1, "gatos").isFailure, "no provider: nothing is invented")
        enableCloud()
        openai.replies += Result.success("""{"title":"Mi gato","title_en":"My cat","title_ar":"قطّي","sentences":[{"text":"Tengo un gato.","en":"I have a cat.","ar":"لديّ قط."},
            {"text":"Mi gato se llama Sol.","en":"My cat is called Sol.","ar":"اسم قطي سول."},{"text":"Sol duerme en el sofá.","en":"Sol sleeps on the sofa.","ar":"ينام سول على الأريكة."}],
            "glossary":[{"term":"gato","en":"cat","ar":"قط"},{"term":"duerme","en":"sleeps","ar":"ينام"}],"questions":[{"question":"¿Dónde duerme Sol?","options":["En el sofá","En la cama"],"answer":0}]}""")
        val story = app.stories.generate(LanguageCode.SPANISH, LanguageCode.ARABIC, CefrLevel.A1, "gatos\n").getOrThrow()
        assertTrue("\"gatos\"" in openai.requests.single().system)
        assertEquals(setOf(com.polyglot.academy.core.model.DataCategory.LESSON_CONTENT), openai.requests.single().dataCategories)
        assertEquals(story, app.stories.find(story.id))
        assertTrue(app.stories.all(LanguageCode.SPANISH).first().any { it.id == story.id })
        assertTrue(app.stories.all(LanguageCode.FRENCH).first().none { it.generated })
        val info = com.polyglot.academy.core.stories.StoryText.lookup("duerme", story, app.stories.lookupVocabulary(LanguageCode.SPANISH))!!
        val id = app.stories.saveWord(story, info, story.sentences[2])
        assertEquals(id, app.stories.saveWord(story, info, null), "saving twice does not duplicate")
        assertEquals("duerme", store.userVocabulary(LanguageCode.SPANISH).first().single().term)
        assertNotNull(store.state(id))
        // Generated stories are part of backups.
        val json = store.exportJson(includeConversations = false, includeNotes = false)
        app.stories.delete(story)
        assertNull(app.stories.find(story.id))
        store.importJson(json)
        assertEquals("Mi gato", app.stories.find(story.id)?.title)
    }

    private suspend fun allowVoice() = store.updateSettings { it.copy(privacy = it.privacy.copy(cloudAllowedCategories = it.privacy.cloudAllowedCategories + com.polyglot.academy.core.model.DataCategory.VOICE_AUDIO)) }
    private fun loudPcm(millis: Int) = ByteArray(16 * millis * 2) { i -> if (i % 4 < 2) 0 else 0x30 }

    @Test fun pronunciationIsScoredOnlyWithAKeyAndVoiceConsent() = runBlocking<Unit> {
        enroll()
        val pron = app.pronunciation
        assertEquals(com.polyglot.academy.core.service.PronunciationReadiness.NOT_CONFIGURED, pron.readiness())
        assertFalse(pron.configure("west europe/x", "key"))
        assertTrue(pron.configure("West Europe", "speech-key-1"))
        assertEquals("westeurope", store.currentSettings().voiceServices.azureRegion)
        assertFalse("speech-key-1" in store.exportJson(true, true), "keys never go into exports")
        enableCloud()
        assertEquals(com.polyglot.academy.core.service.PronunciationReadiness.NO_CONSENT, pron.readiness(), "voice audio is off by default")
        assertTrue(pron.assess(LanguageCode.ENGLISH, "Hello world", loudPcm(1000)).exceptionOrNull() is com.polyglot.academy.core.pronunciation.PronunciationError.NoConsent)
        assertTrue(assessor.references.isEmpty())
        allowVoice()
        assertEquals(com.polyglot.academy.core.service.PronunciationReadiness.READY, pron.readiness())
        assertTrue(pron.assess(LanguageCode.ENGLISH, "Hello world", ByteArray(32_000)).exceptionOrNull() is com.polyglot.academy.core.pronunciation.PronunciationError.NoSpeech)
        assertTrue(pron.assess(LanguageCode.ENGLISH, "Hello world", loudPcm(200)).exceptionOrNull() is com.polyglot.academy.core.pronunciation.PronunciationError.TooShort)
        val report = pron.assess(LanguageCode.ENGLISH, "Hello world", loudPcm(1500)).getOrThrow()
        assertEquals(75.0, report.overall)
        assertEquals("Hello world|en-US|RIFF", assessor.references.single())
        assertEquals(com.polyglot.academy.core.model.SessionKind.PRONUNCIATION, store.sessions(LanguageCode.ENGLISH).first().single().kind)
        assertEquals("pronunciation:world", store.mistakes(LanguageCode.ENGLISH).first().single().sourceId)
        assertEquals(1, app.gamification.today(LanguageCode.ENGLISH).today.pronunciationChecks)
    }

    @Test fun liveVoiceCallStreamsAudioAndCorrectsTheTranscript() = runBlocking<Unit> {
        enroll(LanguageCode.FRENCH, CefrLevel.A2); enableCloud()
        assertEquals(com.polyglot.academy.core.service.RealtimeReadiness.NO_CONSENT, app.realtime.readiness())
        allowVoice()
        assertEquals(com.polyglot.academy.core.service.RealtimeReadiness.READY, app.realtime.readiness())
        platform.capture = FakeCapture(listOf(loudPcm(100), loudPcm(100)))
        val call = app.realtime.session(scope, LanguageCode.FRENCH, TutorRoster.byId("emma")!!, "le cinéma")
        call.start()
        waitFor { transport.socket.sent.isNotEmpty() }
        assertEquals("Bearer sk-test-key-0000", transport.headers["Authorization"])
        assertTrue("model=gpt-realtime" in transport.url)
        val update = transport.socket.sent.first()
        assertTrue("session.update" in update && "Emma" in update && "le cinéma" in update && "French" in update, update)
        transport.socket.toClient.send("""{"type":"session.updated"}""")
        waitFor { transport.socket.sent.any { "response.create" in it } && transport.socket.sent.count { "input_audio_buffer.append" in it } == 2 }
        assertEquals(com.polyglot.academy.core.service.RealtimeStatus.LIVE, call.state.value.status)
        val audio = java.util.Base64.getEncoder().encodeToString(byteArrayOf(5, 6, 7))
        transport.socket.toClient.send("""{"type":"response.output_audio.delta","delta":"$audio"}""")
        transport.socket.toClient.send("""{"type":"response.output_audio_transcript.done","transcript":"Bonjour ! Quel film aimes-tu ?"}""")
        waitFor { call.state.value.turns.size == 1 }
        assertEquals(listOf<Byte>(5, 6, 7), platform.output.written.toList())
        openai.replies += Result.success("""{"corrections":[{"original":"je aime","corrected":"j'aime","explanation":"élision","severity":"minor","hint":"Que devient je devant une voyelle ?","register":"neutral","category":"spelling"}]}""")
        transport.socket.toClient.send("""{"type":"input_audio_buffer.speech_started"}""")
        transport.socket.toClient.send("""{"type":"conversation.item.input_audio_transcription.completed","item_id":"i1","transcript":"je aime les films d'action"}""")
        waitFor { call.state.value.turns.getOrNull(1)?.corrections?.isNotEmpty() == true }
        assertEquals(1, platform.output.clears, "the learner interrupting stops playback")
        val learner = call.state.value.turns[1]
        assertTrue(learner.wasSpoken)
        assertEquals("Que devient je devant une voyelle ?", learner.corrections.single().hint)
        assertTrue("Learner said: je aime les films d'action" in openai.requests.last().messages.single().content)
        assertEquals(setOf(com.polyglot.academy.core.model.DataCategory.CONVERSATION_TEXT), openai.requests.last().dataCategories)
        call.end()
        waitFor { call.state.value.status == com.polyglot.academy.core.service.RealtimeStatus.ENDED }
        waitFor { store.conversations(LanguageCode.FRENCH).first().singleOrNull()?.summary != null }
        val conv = store.conversations(LanguageCode.FRENCH).first().single()
        assertTrue("mode=realtime" in conv.summary!! && "learner_turns=1" in conv.summary!!)
        assertEquals("topic:le cinéma", conv.scenarioId)
        assertEquals(2, store.turns(conv.id).first().size)
        assertEquals("conversation:spelling", store.mistakes(LanguageCode.FRENCH).first().single().sourceId)
        assertTrue(platform.output.closed)
    }

    @Test fun encryptedSyncKeepsTwoDevicesInStep() = runBlocking<Unit> {
        val day = 86_400_000L
        fun review(store: SqlStore, id: String, at: Long) = runBlocking {
            store.upsertSession(com.polyglot.academy.core.model.StudySession(id, LanguageCode.ENGLISH, com.polyglot.academy.core.model.SessionKind.REVIEW, null, at, null, 120, 5, 5, true))
        }
        // Device A has learned something and connects first.
        enroll()
        review(store, "a1", clock.nowMillis() - day)
        assertTrue(app.sync.configure("http://dav.example.com", "me", "pw", "a long passphrase").isFailure, "plain HTTP to the internet is refused")
        assertTrue(app.sync.configure("https://dav.example.com/files/me", "me", "pw", "short").isFailure, "passphrase too short")
        app.sync.configure("https://dav.example.com/files/me/", "me", "pw", "a long passphrase").getOrThrow()
        assertTrue(app.sync.isConfigured())
        assertEquals(com.polyglot.academy.core.service.SyncOutcome.UPLOADED, app.sync.sync().getOrThrow())
        assertEquals("pw", remote.lastPassword)
        assertFalse("\"sessions\"" in remote.file!!.decodeToString() || "\"a1\"" in remote.file!!.decodeToString(), "the server only sees ciphertext")
        assertEquals(com.polyglot.academy.core.service.SyncOutcome.UP_TO_DATE, app.sync.sync().getOrThrow())

        // Device B is new: it takes the server copy and keeps its own settings.
        val storeB = SqlStore(createJvmDriver(), Dispatchers.IO, TimeZone.UTC, clock)
        val appB = AppServices(storeB, FakePlatform(), HttpClient(), clock, SequentialIdGenerator(), providerFactory = { _, _ -> listOf(openai) }, syncRemoteFactory = { _, _, _ -> remote })
        storeB.updateSettings { it.copy(theme = com.polyglot.academy.core.model.ThemeMode.DARK) }
        appB.sync.configure("https://dav.example.com/files/me", "me", "pw", "a wrong passphrase").getOrThrow()
        assertTrue(appB.sync.sync().exceptionOrNull() is com.polyglot.academy.core.sync.SyncError.WrongPassphrase)
        appB.sync.configure("https://dav.example.com/files/me", "me", "", "a long passphrase").getOrThrow()
        assertEquals(com.polyglot.academy.core.service.SyncOutcome.DOWNLOADED, appB.sync.sync().getOrThrow())
        assertEquals(listOf("a1"), storeB.sessions(LanguageCode.ENGLISH).first().map { it.id })
        assertEquals(LanguageCode.ENGLISH, storeB.currentProfile().activeLearningLanguage)
        assertEquals(com.polyglot.academy.core.model.ThemeMode.DARK, storeB.currentSettings().theme, "device settings stay on the device")
        assertTrue(appB.sync.isConfigured(), "the sync connection survives the download")
        assertEquals(com.polyglot.academy.core.service.SyncOutcome.UP_TO_DATE, appB.sync.sync().getOrThrow())

        // B studies; A picks it up.
        review(storeB, "b1", clock.nowMillis())
        assertEquals(com.polyglot.academy.core.service.SyncOutcome.UPLOADED, appB.sync.sync().getOrThrow())
        assertEquals(com.polyglot.academy.core.service.SyncOutcome.DOWNLOADED, app.sync.sync().getOrThrow())
        assertEquals(setOf("a1", "b1"), store.sessions(LanguageCode.ENGLISH).first().map { it.id }.toSet())

        // Both study offline: the learner chooses.
        review(store, "a2", clock.nowMillis()); review(storeB, "b2", clock.nowMillis())
        assertEquals(com.polyglot.academy.core.service.SyncOutcome.UPLOADED, appB.sync.sync().getOrThrow())
        assertEquals(com.polyglot.academy.core.service.SyncOutcome.CONFLICT, app.sync.sync().getOrThrow())
        assertTrue(store.sessions(LanguageCode.ENGLISH).first().any { it.id == "a2" }, "nothing is overwritten until the learner chooses")
        assertEquals(com.polyglot.academy.core.service.SyncOutcome.DOWNLOADED, app.sync.sync(com.polyglot.academy.core.service.ConflictChoice.USE_SERVER).getOrThrow())
        assertEquals(setOf("a1", "b1", "b2"), store.sessions(LanguageCode.ENGLISH).first().map { it.id }.toSet())

        app.sync.disconnect()
        assertFalse(app.sync.isConfigured())
        assertTrue(platform.secrets.get(com.polyglot.academy.core.service.SyncService.PASSPHRASE) == null)
    }

    @Test fun speechUnavailableShowsNoticeInsteadOfHanging() = runBlocking<Unit> {
        enroll()
        platform.recognizer.available = SpeechAvailability.PERMISSION_DENIED
        val c = app.conversation(scope, LanguageCode.ENGLISH, TutorRoster.byId("emma")!!, null)
        c.start(); waitFor { c.state.value.turns.isNotEmpty() }
        c.startListening()
        assertEquals(ConversationNotice.SPEECH_UNAVAILABLE, c.state.value.notice)
        assertEquals(VoiceState.Idle, c.state.value.voice)
    }

    @Test fun aiConversationParsesCorrectionsAndSendsOnlyConversationText() = runBlocking<Unit> {
        enroll(); enableCloud()
        openai.replies += Result.success("""{"reply":"Welcome! Which track would you like: everyday fluency, business, travel or IELTS/TOEFL?"}""")
        openai.replies += Result.success("""{"reply":"Great! Where do you work?","reply_translation":"رائع! أين تعمل؟","track":"business","corrections":[{"original":"I am work in bank","corrected":"I work in a bank","explanation":"لا نستخدم am مع فعل آخر","severity":"moderate","register":"neutral","category":"tense"}],"alternatives":[],"vocabulary":["bank — بنك"]}""")
        val c = app.conversation(scope, LanguageCode.ENGLISH, TutorRoster.byId("oliver")!!, null)
        c.start(); waitFor { c.state.value.turns.isNotEmpty() }
        assertIs<TutorEngineMode.Ai>(c.state.value.engine)
        // The coach opens the first session itself and asks for a track.
        assertTrue("Which track" in c.state.value.turns.single().text)
        assertTrue("ask which track" in openai.requests.first().system)
        c.sendText("Business, please. I am work in bank")
        waitFor { c.state.value.turns.size >= 3 }
        val turns = c.state.value.turns
        assertEquals("I work in a bank", turns[1].corrections.single().corrected)
        assertEquals(com.polyglot.academy.core.model.Register.NEUTRAL, turns[1].corrections.single().register)
        assertEquals("Great! Where do you work?", turns[2].text)
        assertEquals("رائع! أين تعمل؟", turns[2].translation)
        val req = openai.requests.last()
        assertEquals(setOf(com.polyglot.academy.core.model.DataCategory.CONVERSATION_TEXT), req.dataCategories)
        assertTrue("Oliver" in req.system)
        // The chosen track is remembered, and the error goes into the hidden error log.
        assertEquals(com.polyglot.academy.core.model.CoachTrack.BUSINESS, store.currentProfile().coachTrack)
        assertEquals("conversation:tense", store.mistakes(LanguageCode.ENGLISH).first().single().sourceId)
    }

    @Test fun coachWarmsUpWithReviewItemsAndRetestsRecurringErrors() = runBlocking<Unit> {
        enroll(); enableCloud()
        store.updateProfile { it.copy(coachTrack = com.polyglot.academy.core.model.CoachTrack.TRAVEL) }
        val word = Curriculum.of(LanguageCode.ENGLISH).vocabulary.first()
        store.save(com.polyglot.academy.core.model.ReviewState(word.id, LanguageCode.ENGLISH, lastReviewedAtMillis = 1, dueAtMillis = 0), null)
        repeat(2) { app.coach.record(LanguageCode.ENGLISH, listOf(com.polyglot.academy.core.model.Correction("I goed", "I went", "", com.polyglot.academy.core.model.ErrorSeverity.MINOR, category = "tense"))) }
        assertEquals(2, store.mistakes(LanguageCode.ENGLISH).first().single().occurrences)
        openai.replies += Result.success("""{"reply":"Welcome back! Quick quiz: what does '${word.term}' mean?"}""")
        openai.replies += Result.success("""{"reply":"Well done! Where are you travelling next?"}""")
        val c = app.conversation(scope, LanguageCode.ENGLISH, TutorRoster.byId("oliver")!!, null)
        c.start(); waitFor { c.state.value.turns.isNotEmpty() }
        val first = openai.requests.first().system
        assertTrue("${word.term} — ${word.meaning.en}" in first, first)
        assertTrue("Error log (recurring mistakes): tense (2×)" in first)
        assertTrue("Track: travel" in first && "ask which track" !in first)
        c.sendText("I know it")
        waitFor { c.state.value.turns.size >= 3 }
        assertFalse("short quiz questions" in openai.requests.last().system, "the warm-up happens once per session")
    }

    @Test fun aiFailureFallsBackToOfflineWithNotice() = runBlocking<Unit> {
        enroll(); enableCloud()
        openai.replies += Result.failure(AiError.Authentication(ProviderId.OPENAI))
        val c = app.conversation(scope, LanguageCode.ENGLISH, TutorRoster.byId("emma")!!, null)
        c.start(); waitFor { c.state.value.turns.isNotEmpty() }
        c.sendText("I'd like a coffee please")
        waitFor { c.state.value.turns.size >= 3 }
        assertEquals(ConversationNotice.FELL_BACK_TO_OFFLINE, c.state.value.notice)
        assertEquals(TutorEngineMode.Offline(OfflineReason.PROVIDER_FAILED), c.state.value.engine)
        assertTrue(c.state.value.voice !is VoiceState.Processing)
    }

    @Test fun cloudDisabledMeansNoRequestIsMade() = runBlocking<Unit> {
        enroll(); enableCloud()
        store.updateSettings { it.copy(privacy = it.privacy.copy(cloudAiEnabled = false)) }
        val c = app.conversation(scope, LanguageCode.ENGLISH, TutorRoster.byId("emma")!!, null)
        c.start(); waitFor { c.state.value.turns.isNotEmpty() }
        assertEquals(TutorEngineMode.Offline(OfflineReason.CLOUD_DISABLED_BY_PRIVACY), c.state.value.engine)
        c.sendText("hello"); waitFor { c.state.value.turns.size >= 3 }
        assertTrue(openai.requests.isEmpty())
    }

    @Test fun writingFeedbackOfflineAndAi() = runBlocking<Unit> {
        enroll()
        val prompt = Curriculum.of(LanguageCode.ENGLISH).writingPrompts.first()
        val off = app.writing.offlineFeedback("my name is omar and I live in Amman", prompt, LanguageCode.ENGLISH)
        assertTrue(WritingCheck.TOO_SHORT in off.checks)
        assertTrue(WritingCheck.MISSING_CAPITAL in off.checks)
        assertTrue(WritingCheck.MISSING_END_PUNCTUATION in off.checks)
        assertEquals(listOf("my name is", "I live in"), off.usedSuggestions)
        val blocked = app.writing.aiFeedback("text", prompt, LanguageCode.ENGLISH, LanguageCode.ARABIC, CefrLevel.A1)
        assertTrue(blocked.isFailure)
        enableCloud()
        openai.replies += Result.success("""{"corrections":[{"original":"my name","corrected":"My name","explanation":"capital","severity":"minor"}],"improved_version":"My name is Omar.","comment":"Good start"}""")
        val ai = app.writing.aiFeedback("my name is omar", prompt, LanguageCode.ENGLISH, LanguageCode.ARABIC, CefrLevel.A1).getOrThrow()
        assertEquals("My name is Omar.", ai.improvedVersion)
        assertEquals(1, ai.corrections.size)
    }

    @Test fun materialImportExtractCandidatesAndDelete() = runBlocking<Unit> {
        enroll()
        val text = "The ticket office at the station is busy. I bought a ticket and a coffee. The ticket was cheap."
        val events = app.materials.import(LanguageCode.ENGLISH, ImportInput("Trip", "text/plain", "trip.txt", text.length.toLong(), text), cloudAllowed = false).toList()
        val done = events.last() as ImportProgress.Done
        assertEquals(MaterialStatus.READY_FOR_REVIEW, done.material.status)
        val cands = app.materials.candidates(done.material)
        assertEquals("ticket", cands.first().term)
        val office = cands.first { it.term == "office" }
        val added = app.materials.addToDeck(done.material, listOf(cands.first() to "", office to "مكتب"))
        assertEquals(2, added.size)
        assertEquals("مكتب", store.userVocabulary(LanguageCode.ENGLISH).first().single().meaning.en)
        val stored = store.materials(LanguageCode.ENGLISH).first().single()
        assertEquals(MaterialStatus.PROCESSED, stored.status)
        app.materials.delete(stored)
        assertTrue(store.materials(null).first().isEmpty())
        assertTrue(store.userVocabulary(LanguageCode.ENGLISH).first().isEmpty())

        val pdf = app.materials.import(LanguageCode.ENGLISH, ImportInput("Doc", "application/pdf", "a.pdf", 1000, null), false).toList().last() as ImportProgress.Done
        assertEquals(MaterialStatus.UNSUPPORTED, pdf.material.status, "no bytes handed over")
        val img = app.materials.import(LanguageCode.ENGLISH, ImportInput("Pic", "image/png", "a.png", 1000, null), false).toList().last() as ImportProgress.Done
        assertEquals(MaterialStatus.UNSUPPORTED, img.material.status)
        val rejected = app.materials.import(LanguageCode.ENGLISH, ImportInput("Zip", "application/zip", "a.zip", 10, null), false).toList().single()
        assertIs<ImportProgress.Rejected>(rejected)
    }

    @Test fun pdfImportExtractsTextOnDeviceAndReportsUnreadableFiles() = runBlocking<Unit> {
        val bytes = javaClass.getResourceAsStream("/pdf/reportlab_winansi.pdf")!!.readBytes()
        val done = app.materials.import(LanguageCode.FRENCH, ImportInput("Gare", "application/pdf", "gare.pdf", bytes.size.toLong(), null, bytes), false).toList().last() as ImportProgress.Done
        assertEquals(MaterialStatus.READY_FOR_REVIEW, done.material.status)
        assertTrue("Café crème à côté de la gare." in done.material.extractedText.orEmpty())
        assertTrue(app.materials.candidates(done.material).isNotEmpty())
        val junk = "%PDF-1.4\n1 0 obj << /Type /Catalog >> endobj\n".encodeToByteArray()
        val failed = app.materials.import(LanguageCode.FRENCH, ImportInput("X", "application/pdf", "x.pdf", junk.size.toLong(), null, junk), false).toList().last() as ImportProgress.Done
        assertEquals(MaterialStatus.FAILED, failed.material.status)
        assertEquals("pdf_no_text", failed.material.errorMessage)
    }

    @Test fun photoTextIsReadWithVisionAiOnlyWhenCloudIsAllowed() = runBlocking<Unit> {
        val photo = byteArrayOf(1, 2, 3, 4)
        val done = app.materials.import(LanguageCode.FRENCH, ImportInput("Menu", "image/jpeg", "menu.jpg", 4, null, photo), false).toList().last() as ImportProgress.Done
        assertEquals(MaterialStatus.UNSUPPORTED, done.material.status)
        assertEquals("needs_ocr", done.material.errorMessage)
        assertTrue(app.materials.canTryOcr(done.material))
        val pages = listOf(com.polyglot.academy.core.service.OcrPage("image/jpeg", photo), com.polyglot.academy.core.service.OcrPage("image/jpeg", photo))

        // Cloud AI off: nothing is sent.
        val (_, blocked) = app.materials.recognizeText(done.material, pages)
        assertIs<com.polyglot.academy.core.service.OcrResult.Failed>(blocked)
        assertTrue(openai.requests.isEmpty())

        enableCloud()
        openai.replies += Result.success("Café : 2 €\nCroissant : 1,50 €")
        openai.replies += Result.success("NO_TEXT")
        val (updated, result) = app.materials.recognizeText(done.material, pages)
        val text = assertIs<com.polyglot.academy.core.service.OcrResult.Text>(result)
        assertEquals(2, text.pagesRead)
        assertEquals(1, text.pagesWithoutText)
        assertEquals(MaterialStatus.READY_FOR_REVIEW, updated.status)
        assertTrue("Croissant" in updated.extractedText.orEmpty())
        val req = openai.requests.first()
        assertEquals(setOf(com.polyglot.academy.core.model.DataCategory.IMPORTED_MATERIAL), req.dataCategories)
        assertEquals("AQIDBA==", req.messages.single().images.single().base64)
        assertTrue("French" in req.messages.single().content)
    }

    @Test fun captionsImport() = runBlocking<Unit> {
        val srt = "1\n00:00:01,000 --> 00:00:02,000\nWhere is the station?\n\n2\n00:00:03,000 --> 00:00:04,000\nGo straight on.\n"
        val done = app.materials.import(LanguageCode.ENGLISH, ImportInput("Clip", "application/x-subrip", "clip.srt", srt.length.toLong(), srt), false).toList().last() as ImportProgress.Done
        assertEquals("Where is the station?\nGo straight on.", done.material.extractedText)
    }

    @Test fun restoreReappliesTheRestoredReminder() = runBlocking<Unit> {
        store.updateSettings { it.copy(reminders = it.reminders.copy(enabled = true, hour = 7, minute = 30)) }
        val json = store.exportJson(includeConversations = false, includeNotes = false)
        platform.reminder = null
        app.restore(json) { true }
        assertEquals(Triple(true, 7, 30), platform.reminder)
        app.restore(json) { false }
        assertEquals(false, platform.reminder?.first, "no permission means no alarm")
    }

    @Test fun deleteEverythingRemovesDataSecretsAndReminder() = runBlocking<Unit> {
        enroll(); enableCloud()
        app.deleteEverything()
        assertNull(platform.secrets.get("provider_api_key_openai"))
        assertTrue(!store.currentProfile().onboardingCompleted)
        assertEquals(false, platform.reminder?.first)
        assertNotNull(platform.reminder)
    }
}
