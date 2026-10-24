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
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.TimeZone
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
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
    private val app = AppServices(store, platform, HttpClient(), clock, SequentialIdGenerator(), providerFactory = { _, _ -> listOf(openai) })
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
        openai.replies += Result.success("""{"reply":"Great! Where do you work?","reply_translation":"رائع! أين تعمل؟","corrections":[{"original":"I am work in bank","corrected":"I work in a bank","explanation":"لا نستخدم am مع فعل آخر","severity":"moderate"}],"alternatives":[],"vocabulary":["bank — بنك"]}""")
        val c = app.conversation(scope, LanguageCode.ENGLISH, TutorRoster.byId("oliver")!!, null)
        c.start(); waitFor { c.state.value.turns.isNotEmpty() }
        assertIs<TutorEngineMode.Ai>(c.state.value.engine)
        c.sendText("I am work in bank")
        waitFor { c.state.value.turns.size >= 3 }
        val turns = c.state.value.turns
        assertEquals("I work in a bank", turns[1].corrections.single().corrected)
        assertEquals("Great! Where do you work?", turns[2].text)
        assertEquals("رائع! أين تعمل؟", turns[2].translation)
        val req = openai.requests.single()
        assertEquals(setOf(com.polyglot.academy.core.model.DataCategory.CONVERSATION_TEXT), req.dataCategories)
        assertTrue("Oliver" in req.system)
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
        assertEquals(MaterialStatus.UNSUPPORTED, pdf.material.status)
        val rejected = app.materials.import(LanguageCode.ENGLISH, ImportInput("Zip", "application/zip", "a.zip", 10, null), false).toList().single()
        assertIs<ImportProgress.Rejected>(rejected)
    }

    @Test fun captionsImport() = runBlocking<Unit> {
        val srt = "1\n00:00:01,000 --> 00:00:02,000\nWhere is the station?\n\n2\n00:00:03,000 --> 00:00:04,000\nGo straight on.\n"
        val done = app.materials.import(LanguageCode.ENGLISH, ImportInput("Clip", "application/x-subrip", "clip.srt", srt.length.toLong(), srt), false).toList().last() as ImportProgress.Done
        assertEquals("Where is the station?\nGo straight on.", done.material.extractedText)
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
