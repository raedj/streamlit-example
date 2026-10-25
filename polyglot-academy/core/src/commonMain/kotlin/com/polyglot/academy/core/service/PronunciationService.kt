package com.polyglot.academy.core.service

import com.polyglot.academy.core.ai.NetworkState
import com.polyglot.academy.core.ai.PrivacyDecision
import com.polyglot.academy.core.ai.PrivacyPolicy
import com.polyglot.academy.core.model.DataCategory
import com.polyglot.academy.core.model.ErrorSeverity
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.Languages
import com.polyglot.academy.core.model.Mistake
import com.polyglot.academy.core.model.ProviderLocation
import com.polyglot.academy.core.model.SessionKind
import com.polyglot.academy.core.model.Skill
import com.polyglot.academy.core.pronunciation.AzurePronunciationAssessor
import com.polyglot.academy.core.pronunciation.PronunciationAssessor
import com.polyglot.academy.core.pronunciation.PronunciationError
import com.polyglot.academy.core.pronunciation.PronunciationReport
import com.polyglot.academy.core.repo.ProfileRepository
import com.polyglot.academy.core.repo.ProgressRepository
import com.polyglot.academy.core.repo.SecretStore
import com.polyglot.academy.core.speech.Wav
import com.polyglot.academy.core.util.IdGenerator
import com.polyglot.academy.core.util.TimeSource

enum class PronunciationReadiness { NOT_CONFIGURED, NO_CONSENT, OFFLINE, READY }

/** Detailed pronunciation scoring (per word and sound) through the learner's own speech-service key. */
class PronunciationService(
    private val profiles: ProfileRepository,
    private val progress: ProgressRepository,
    private val secrets: SecretStore,
    private val network: () -> NetworkState,
    private val study: StudyService,
    private val clock: TimeSource,
    private val ids: IdGenerator,
    private val assessorFactory: (String, suspend () -> String?) -> PronunciationAssessor,
) {
    suspend fun region(): String = profiles.currentSettings().voiceServices.azureRegion

    suspend fun hasKey(): Boolean = !secrets.get(KEY).isNullOrBlank()

    /** Saves the region and, when given, a new key. Returns false for an invalid region. */
    suspend fun configure(region: String, key: String?): Boolean {
        val r = region.trim().lowercase().replace(" ", "")
        if (!AzurePronunciationAssessor.isValidRegion(r)) return false
        profiles.updateSettings { it.copy(voiceServices = it.voiceServices.copy(azureRegion = r)) }
        key?.trim()?.takeIf { it.isNotEmpty() }?.let { secrets.put(KEY, it) }
        return true
    }

    suspend fun removeKey() { secrets.remove(KEY) }

    suspend fun readiness(): PronunciationReadiness {
        val settings = profiles.currentSettings()
        if (!AzurePronunciationAssessor.isValidRegion(settings.voiceServices.azureRegion) || !hasKey()) return PronunciationReadiness.NOT_CONFIGURED
        if (PrivacyPolicy.evaluate(settings.privacy, ProviderLocation.CLOUD, setOf(DataCategory.VOICE_AUDIO)) !is PrivacyDecision.Allowed) return PronunciationReadiness.NO_CONSENT
        if (network() == NetworkState.OFFLINE) return PronunciationReadiness.OFFLINE
        return PronunciationReadiness.READY
    }

    /** Checks the key with the token endpoint (no audio is sent). */
    suspend fun verify(): Result<Boolean> = runCatching {
        val region = region()
        if (!AzurePronunciationAssessor.isValidRegion(region)) throw PronunciationError.NotConfigured
        assessorFactory(region) { secrets.get(KEY) }.verify()
    }

    suspend fun assess(language: LanguageCode, referenceText: String, pcm: ByteArray, sampleRate: Int = SAMPLE_RATE): Result<PronunciationReport> = runCatching {
        when (readiness()) {
            PronunciationReadiness.NOT_CONFIGURED -> throw PronunciationError.NotConfigured
            PronunciationReadiness.NO_CONSENT -> throw PronunciationError.NoConsent
            PronunciationReadiness.OFFLINE -> throw PronunciationError.Offline
            PronunciationReadiness.READY -> Unit
        }
        if (Wav.durationMillis(pcm.size, sampleRate) < MIN_MILLIS) throw PronunciationError.TooShort
        if (Wav.level(pcm) < SILENCE_LEVEL) throw PronunciationError.NoSpeech
        val locale = AzurePronunciationAssessor.locale(language, Languages.of(language).defaultVariant.localeTag)
        val report = assessorFactory(region()) { secrets.get(KEY) }.assess(Wav.encode(pcm, sampleRate), referenceText, locale)
        record(language, referenceText, report, Wav.durationMillis(pcm.size, sampleRate))
        report
    }

    private suspend fun record(language: LanguageCode, reference: String, report: PronunciationReport, millis: Long) {
        val session = study.startSession(language, SessionKind.PRONUNCIATION, null)
        study.updateSession(session, 1, if ((report.overall ?: report.accuracy ?: 0.0) >= GOOD_SCORE) 1 else 0, true, (millis / 1000).toInt().coerceAtLeast(1))
        report.words.filter { it.weak && it.word.isNotBlank() }.take(5).forEach { w ->
            progress.recordMistake(Mistake(ids.newId("mis"), language, Skill.PRONUNCIATION, "pronunciation:${w.word.lowercase()}", reference, w.word, w.word,
                w.phonemes.filter { it.accuracy < PronunciationReport.WEAK_BELOW }.joinToString { "/${it.phoneme}/" }.ifEmpty { null },
                ErrorSeverity.MINOR, "pronunciation", clock.nowMillis()))
        }
    }

    companion object {
        const val KEY = "speech.azure.key"
        const val SAMPLE_RATE = 16_000
        const val MAX_SECONDS = 20
        private const val MIN_MILLIS = 500L
        private const val SILENCE_LEVEL = 0.004f
        private const val GOOD_SCORE = 70.0
    }
}
