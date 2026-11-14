package com.polyglot.academy.core

import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.pronunciation.AzurePronunciationAssessor
import com.polyglot.academy.core.pronunciation.AzureResponse
import com.polyglot.academy.core.pronunciation.PronunciationError
import com.polyglot.academy.core.pronunciation.WordError
import com.polyglot.academy.core.realtime.OpenAiRealtimeProtocol
import com.polyglot.academy.core.realtime.RealtimeEvent
import com.polyglot.academy.core.speech.Wav
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalEncodingApi::class)
class VoiceServicesTest {
    private val detailed = """{"RecognitionStatus":"Success","DisplayText":"Hello world.","NBest":[{"Confidence":0.9,"Lexical":"hello world","Display":"Hello world.",
        "PronunciationAssessment":{"AccuracyScore":72.5,"FluencyScore":88,"CompletenessScore":100,"PronScore":79.4,"ProsodyScore":70.1},
        "Words":[{"Word":"hello","PronunciationAssessment":{"AccuracyScore":95,"ErrorType":"None"},"Phonemes":[{"Phoneme":"h","PronunciationAssessment":{"AccuracyScore":98}},{"Phoneme":"ɛ","PronunciationAssessment":{"AccuracyScore":91}}]},
                 {"Word":"world","PronunciationAssessment":{"AccuracyScore":40,"ErrorType":"Mispronunciation"},"Phonemes":[{"Phoneme":"w","PronunciationAssessment":{"AccuracyScore":85}},{"Phoneme":"ɝ","PronunciationAssessment":{"AccuracyScore":22}},{"Phoneme":"l","PronunciationAssessment":{"AccuracyScore":55}}]}]}]}"""

    @Test fun wavHeaderAndLevels() {
        val pcm = ByteArray(3200) { if (it % 2 == 1) 0x10 else 0 }
        val wav = Wav.encode(pcm, 16_000)
        assertEquals("RIFF", wav.copyOfRange(0, 4).decodeToString()); assertEquals("WAVE", wav.copyOfRange(8, 12).decodeToString())
        assertEquals(44 + pcm.size, wav.size)
        assertEquals(16_000, (wav[24].toInt() and 0xFF) or ((wav[25].toInt() and 0xFF) shl 8))
        assertEquals(100, Wav.durationMillis(pcm.size, 16_000))
        assertEquals(0f, Wav.level(ByteArray(100)))
        assertTrue(Wav.level(pcm) > 0.1f)
    }

    @Test fun azureDetailedResponseIsReadDownToSounds() {
        val r = AzureResponse.parse(detailed, "Azure")
        assertEquals(79.4, r.overall); assertEquals(72.5, r.accuracy); assertEquals(88.0, r.fluency); assertEquals(70.1, r.prosody)
        assertEquals("Hello world.", r.recognizedText)
        assertEquals(listOf(WordError.NONE, WordError.MISPRONUNCIATION), r.words.map { it.error })
        assertTrue(r.words[1].weak && !r.words[0].weak)
        assertEquals(listOf("ɝ", "l"), r.weakPhonemes.map { it.phoneme })
        // Older flat layout.
        val flat = AzureResponse.parse("""{"RecognitionStatus":"Success","NBest":[{"Display":"Hola.","AccuracyScore":60,"PronScore":61,"Words":[{"Word":"hola","AccuracyScore":60,"ErrorType":"None"}]}]}""", "Azure")
        assertEquals(61.0, flat.overall); assertEquals(60.0, flat.words.single().accuracy)
        assertIs<PronunciationError.NoSpeech>(runCatching { AzureResponse.parse("""{"RecognitionStatus":"InitialSilenceTimeout"}""", "Azure") }.exceptionOrNull())
        assertIs<PronunciationError.Invalid>(runCatching { AzureResponse.parse("<html>", "Azure") }.exceptionOrNull())
    }

    @Test fun azureRequestCarriesTheReferenceAndMapsErrors() = runTest { withContext(Dispatchers.Default) {
        var url = ""; var config = ""; var key: String? = null
        val ok = HttpClient(MockEngine { req ->
            url = req.url.toString(); key = req.headers["Ocp-Apim-Subscription-Key"]
            config = Base64.decode(req.headers["Pronunciation-Assessment"]!!).decodeToString()
            respond(detailed, HttpStatusCode.OK, headersOf("Content-Type", "application/json"))
        })
        val report = AzurePronunciationAssessor(ok, "westeurope", { "k-123" }).assess(Wav.encode(ByteArray(32000), 16_000), "Hello world", "en-US")
        assertEquals(2, report.words.size)
        assertTrue(url.startsWith("https://westeurope.stt.speech.microsoft.com/") && "language=en-US" in url, url)
        assertEquals("k-123", key)
        val cfg = Json.parseToJsonElement(config).jsonObject
        assertEquals("Hello world", cfg["ReferenceText"]!!.jsonPrimitive.content); assertEquals("Phoneme", cfg["Granularity"]!!.jsonPrimitive.content)
        val denied = HttpClient(MockEngine { respond("", HttpStatusCode.Unauthorized) })
        assertFailsWith<PronunciationError.Authentication> { AzurePronunciationAssessor(denied, "eastus", { "bad" }).assess(ByteArray(10), "x", "en-US") }
        assertFailsWith<PronunciationError.NotConfigured> { AzurePronunciationAssessor(denied, "eastus", { null }).assess(ByteArray(10), "x", "en-US") }
        assertFalse(AzurePronunciationAssessor(denied, "eastus", { "bad" }).verify())
    } }

    @Test fun regionsAndLocales() {
        assertTrue(AzurePronunciationAssessor.isValidRegion("uaenorth"))
        assertFalse(AzurePronunciationAssessor.isValidRegion("evil.com/x")); assertFalse(AzurePronunciationAssessor.isValidRegion("West Europe"))
        assertEquals("ar-SA", AzurePronunciationAssessor.locale(LanguageCode.ARABIC, "ar"))
        assertEquals("en-GB", AzurePronunciationAssessor.locale(LanguageCode.ENGLISH, "en-GB"))
        assertEquals("es-ES", AzurePronunciationAssessor.locale(LanguageCode.SPANISH, "es-419"))
    }

    @Test fun realtimeProtocolReadsCurrentAndBetaEvents() {
        val update = Json.parseToJsonElement(OpenAiRealtimeProtocol.sessionUpdate("Be kind", "coral", "fr")).jsonObject
        assertEquals("session.update", update["type"]!!.jsonPrimitive.content)
        assertTrue("\"voice\":\"coral\"" in update.toString() && "\"rate\":24000" in update.toString() && "server_vad" in update.toString() && "\"language\":\"fr\"" in update.toString())
        assertEquals("wss://api.openai.com/v1/realtime?model=gpt-realtime", OpenAiRealtimeProtocol.url("gpt-realtime"))
        assertFailsWith<IllegalArgumentException> { OpenAiRealtimeProtocol.url("x&evil=1") }
        val audio = Base64.encode(byteArrayOf(1, 2, 3))
        assertEquals(listOf<Byte>(1, 2, 3), (OpenAiRealtimeProtocol.parse("""{"type":"response.output_audio.delta","delta":"$audio"}""") as RealtimeEvent.TutorAudio).pcm.toList())
        assertIs<RealtimeEvent.TutorAudio>(OpenAiRealtimeProtocol.parse("""{"type":"response.audio.delta","delta":"$audio"}"""))
        assertEquals(RealtimeEvent.LearnerTranscript("i1", "Je voudrais un café"),
            OpenAiRealtimeProtocol.parse("""{"type":"conversation.item.input_audio_transcription.completed","item_id":"i1","transcript":" Je voudrais un café "}"""))
        assertEquals(RealtimeEvent.TutorTranscriptDone("r1", "Bien sûr !"), OpenAiRealtimeProtocol.parse("""{"type":"response.audio_transcript.done","response_id":"r1","transcript":"Bien sûr !"}"""))
        assertEquals(RealtimeEvent.LearnerSpeechStarted, OpenAiRealtimeProtocol.parse("""{"type":"input_audio_buffer.speech_started"}"""))
        assertEquals(RealtimeEvent.Failure("invalid_api_key", "bad key"), OpenAiRealtimeProtocol.parse("""{"type":"error","error":{"code":"invalid_api_key","message":"bad key"}}"""))
        assertNull(OpenAiRealtimeProtocol.parse("""{"type":"rate_limits.updated"}""")); assertNull(OpenAiRealtimeProtocol.parse("not json"))
        assertTrue(OpenAiRealtimeProtocol.appendAudio(byteArrayOf(1, 2, 3)).contains(audio))
    }
}
