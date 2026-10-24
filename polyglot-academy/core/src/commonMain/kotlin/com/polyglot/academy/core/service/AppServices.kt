package com.polyglot.academy.core.service

import com.polyglot.academy.core.ai.AiProvider
import com.polyglot.academy.core.ai.CredentialSource
import com.polyglot.academy.core.ai.NetworkState
import com.polyglot.academy.core.ai.ProviderConfig
import com.polyglot.academy.core.ai.ProviderId
import com.polyglot.academy.core.ai.createHttpProviders
import com.polyglot.academy.core.device.DeviceInfo
import com.polyglot.academy.core.media.ExtractionSupport
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.MaterialType
import com.polyglot.academy.core.model.RolePlayScenario
import com.polyglot.academy.core.repo.LearningStore
import com.polyglot.academy.core.repo.SecretStore
import com.polyglot.academy.core.speech.SpeechRecognizerService
import com.polyglot.academy.core.speech.SpeechSynthesizerService
import com.polyglot.academy.core.srs.SpacedRepetitionScheduler
import com.polyglot.academy.core.tutor.TutorPersona
import com.polyglot.academy.core.util.IdGenerator
import com.polyglot.academy.core.util.TimeSource
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope

/** Platform capabilities injected by the Android app (or the desktop harness / tests). */
interface PlatformServices {
    val recognizer: SpeechRecognizerService
    val synthesizer: SpeechSynthesizerService
    val secrets: SecretStore
    fun network(): NetworkState
    fun deviceInfo(): DeviceInfo
    val extractionSupport: Map<MaterialType, ExtractionSupport>
    /** Schedules or cancels the daily reminder. Returns false when the platform refuses (e.g. permission denied). */
    suspend fun scheduleReminder(enabled: Boolean, hour: Int, minute: Int, days: Set<Int>): Boolean
    val platformName: String
}

/** Manual dependency graph (see ADR-004). One instance per process. */
class AppServices(
    val store: LearningStore,
    val platform: PlatformServices,
    httpClient: HttpClient,
    val clock: TimeSource = TimeSource.System,
    val ids: IdGenerator = IdGenerator.Random,
    providerFactory: ((CredentialSource, (ProviderId) -> ProviderConfig) -> List<AiProvider>)? = null,
) {
    val scheduler = SpacedRepetitionScheduler()
    val achievements = AchievementService(store, store, clock)
    val study = StudyService(store, store, achievements, scheduler, clock, ids)
    val reviews = ReviewService(store, store, scheduler, achievements, clock)
    val dashboard = DashboardService(store, store, store, store, reviews, study, clock)
    val plans = PlanService(store, store, store, reviews, clock)
    val assessments = AssessmentService(store, store, achievements, clock, ids)
    val providers = ProviderService(
        store, platform.secrets, store, platform::network,
        providerFactory ?: { creds, cfg -> createHttpProviders(httpClient, creds, { id -> cfg(id) }, clock::nowMillis) },
        clock,
    )
    val writing = WritingService(providers)
    val materials = MaterialService(store, store, reviews, clock, ids, platform.extractionSupport)

    fun conversation(scope: CoroutineScope, language: LanguageCode, tutor: TutorPersona, scenario: RolePlayScenario?, forceOffline: Boolean = false) =
        ConversationController(scope, language, tutor, scenario, store, store, store, reviews, providers, study, achievements,
            platform.recognizer, platform.synthesizer, clock, ids, forceOffline)

    /** Erases all stored data, credentials and the reminder schedule. */
    suspend fun deleteEverything() {
        store.deleteAllData()
        platform.secrets.keys().forEach { platform.secrets.remove(it) }
        platform.scheduleReminder(false, 0, 0, emptySet())
    }
}
