package com.polyglot.academy.core.pronunciation

import com.polyglot.academy.core.model.LanguageCode
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

enum class WordError { NONE, MISPRONUNCIATION, OMISSION, INSERTION, UNEXPECTED_BREAK, MISSING_BREAK, MONOTONE }

data class PhonemeScore(val phoneme: String, val accuracy: Double)

data class WordScore(val word: String, val accuracy: Double?, val error: WordError, val phonemes: List<PhonemeScore>) {
    val weak: Boolean get() = error != WordError.NONE || (accuracy ?: 100.0) < PronunciationReport.WEAK_BELOW
}

/** Scores from a pronunciation-assessment service (0–100). Null where the service did not report a value. */
data class PronunciationReport(
    val recognizedText: String,
    val overall: Double?,
    val accuracy: Double?,
    val fluency: Double?,
    val completeness: Double?,
    val prosody: Double?,
    val words: List<WordScore>,
    val provider: String,
) {
    /** The weakest sounds across the attempt, for targeted practice. */
    val weakPhonemes: List<PhonemeScore>
        get() = words.flatMap { it.phonemes }.filter { it.accuracy < WEAK_BELOW }.groupBy { it.phoneme }
            .map { (p, l) -> PhonemeScore(p, l.minOf { it.accuracy }) }.sortedBy { it.accuracy }.take(5)

    companion object { const val WEAK_BELOW = 60.0 }
}

sealed class PronunciationError(message: String) : Exception(message) {
    object NotConfigured : PronunciationError("not_configured")
    object NoConsent : PronunciationError("no_consent")
    object Offline : PronunciationError("offline")
    object Authentication : PronunciationError("authentication")
    object Quota : PronunciationError("quota")
    object NoSpeech : PronunciationError("no_speech")
    object TooShort : PronunciationError("too_short")
    /** The assessment service has no confirmed locale for this course language. */
    object UnsupportedLanguage : PronunciationError("unsupported_language")
    class Network(detail: String) : PronunciationError("network: $detail")
    class Server(val status: Int) : PronunciationError("server: HTTP $status")
    class Invalid(detail: String) : PronunciationError("invalid: $detail")
}

/** A service that scores a recording against the text the learner was asked to read. */
interface PronunciationAssessor {
    val name: String
    suspend fun assess(wav: ByteArray, referenceText: String, localeTag: String): PronunciationReport
    /** Checks the key without sending audio. */
    suspend fun verify(): Boolean
}

/**
 * Azure AI Speech pronunciation assessment over the short-audio REST API (up to about 30 s of
 * 16 kHz mono WAV). Key and region come from the learner's own Azure resource.
 */
class AzurePronunciationAssessor(
    private val http: HttpClient,
    private val region: String,
    private val key: suspend () -> String?,
    private val timeoutMillis: Long = 30_000,
) : PronunciationAssessor {
    override val name = "Azure AI Speech"

    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun assess(wav: ByteArray, referenceText: String, localeTag: String): PronunciationReport {
        val k = key() ?: throw PronunciationError.NotConfigured
        val config = buildJsonObject {
            put("ReferenceText", referenceText)
            put("GradingSystem", "HundredMark")
            put("Granularity", "Phoneme")
            put("Dimension", "Comprehensive")
            put("EnableMiscue", "True")
            if (localeTag.startsWith("en")) { put("PhonemeAlphabet", "IPA"); put("EnableProsodyAssessment", "True") }
        }
        val url = "https://${host(region)}.stt.speech.microsoft.com/speech/recognition/conversation/cognitiveservices/v1?language=$localeTag&format=detailed"
        val (status, text) = send {
            http.post(url) {
                header("Ocp-Apim-Subscription-Key", k)
                header("Pronunciation-Assessment", Base64.encode(config.toString().encodeToByteArray()))
                header("Accept", "application/json")
                contentType(ContentType.parse("audio/wav; codecs=audio/pcm; samplerate=16000"))
                setBody(wav)
            }.let { it.status.value to it.bodyAsText() }
        }
        when {
            status == 401 || status == 403 -> throw PronunciationError.Authentication
            status == 429 -> throw PronunciationError.Quota
            status >= 500 -> throw PronunciationError.Server(status)
            status !in 200..299 -> throw PronunciationError.Invalid("HTTP $status")
        }
        return AzureResponse.parse(text, name)
    }

    override suspend fun verify(): Boolean {
        val k = key() ?: return false
        val (status, _) = send {
            http.post("https://${host(region)}.api.cognitive.microsoft.com/sts/v1.0/issueToken") {
                header("Ocp-Apim-Subscription-Key", k)
                setBody("")
            }.let { it.status.value to it.bodyAsText() }
        }
        return status in 200..299
    }

    private suspend fun <T> send(block: suspend () -> T): T = try {
        withTimeout(timeoutMillis) { block() }
    } catch (e: TimeoutCancellationException) {
        throw PronunciationError.Network("timeout")
    } catch (e: CancellationException) {
        throw e
    } catch (e: PronunciationError) {
        throw e
    } catch (e: Exception) {
        throw PronunciationError.Network(e::class.simpleName ?: "IO")
    }

    companion object {
        /** Region names are lowercase letters and digits only (e.g. "westeurope"); anything else is rejected. */
        fun isValidRegion(region: String) = region.matches(Regex("[a-z0-9]{3,30}"))
        private fun host(region: String): String { require(isValidRegion(region)) { "invalid region" }; return region }

        /**
         * Locales the assessment supports for each course language; the course's regional variant is used when it is one of them.
         * Null when no locale has been confirmed for the language (Turkish, Urdu and Malay are not listed until verified).
         */
        fun locale(language: LanguageCode, variantLocale: String?): String? {
            val supported = when (language) {
                LanguageCode.ENGLISH -> listOf("en-US", "en-GB", "en-AU")
                LanguageCode.SPANISH -> listOf("es-ES", "es-MX")
                LanguageCode.FRENCH -> listOf("fr-FR", "fr-CA")
                LanguageCode.MANDARIN -> listOf("zh-CN")
                LanguageCode.ARABIC -> listOf("ar-SA", "ar-EG")
                LanguageCode.TURKISH, LanguageCode.URDU, LanguageCode.MALAY -> return null
            }
            return variantLocale?.takeIf { it in supported } ?: supported.first()
        }
    }
}

/** Parser for the "detailed" JSON response; tolerates both the nested and the older flat score layout. */
object AzureResponse {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    fun parse(text: String, provider: String): PronunciationReport {
        val root = runCatching { json.parseToJsonElement(text).jsonObject }.getOrNull() ?: throw PronunciationError.Invalid("not JSON")
        when (root.s("RecognitionStatus")) {
            "Success", null -> Unit
            "NoMatch", "InitialSilenceTimeout", "BabbleTimeout" -> throw PronunciationError.NoSpeech
            else -> throw PronunciationError.Invalid(root.s("RecognitionStatus") ?: "unknown status")
        }
        val best = (root["NBest"] as? JsonArray)?.firstOrNull() as? JsonObject ?: throw PronunciationError.NoSpeech
        fun score(o: JsonObject, k: String): Double? = (o["PronunciationAssessment"] as? JsonObject)?.d(k) ?: o.d(k)
        val words = (best["Words"] as? JsonArray).orEmpty().mapNotNull { it as? JsonObject }.map { w ->
            val pa = w["PronunciationAssessment"] as? JsonObject
            val err = (pa?.s("ErrorType") ?: w.s("ErrorType")).orEmpty()
            WordScore(
                word = w.s("Word").orEmpty(),
                accuracy = score(w, "AccuracyScore"),
                error = when (err.lowercase()) {
                    "mispronunciation" -> WordError.MISPRONUNCIATION
                    "omission" -> WordError.OMISSION
                    "insertion" -> WordError.INSERTION
                    "unexpectedbreak", "unexpected_break" -> WordError.UNEXPECTED_BREAK
                    "missingbreak", "missing_break" -> WordError.MISSING_BREAK
                    "monotone" -> WordError.MONOTONE
                    else -> WordError.NONE
                },
                phonemes = (w["Phonemes"] as? JsonArray).orEmpty().mapNotNull { it as? JsonObject }.mapNotNull { p ->
                    val ph = p.s("Phoneme") ?: return@mapNotNull null
                    score(p, "AccuracyScore")?.let { PhonemeScore(ph, it) }
                },
            )
        }
        return PronunciationReport(
            recognizedText = best.s("Display") ?: root.s("DisplayText") ?: best.s("Lexical").orEmpty(),
            overall = score(best, "PronScore"), accuracy = score(best, "AccuracyScore"), fluency = score(best, "FluencyScore"),
            completeness = score(best, "CompletenessScore"), prosody = score(best, "ProsodyScore"), words = words, provider = provider,
        )
    }

    private fun JsonObject.s(k: String): String? = (this[k] as? JsonPrimitive)?.takeIf { it.isString }?.content
    private fun JsonObject.d(k: String): Double? = (this[k] as? JsonPrimitive)?.content?.toDoubleOrNull()
}
