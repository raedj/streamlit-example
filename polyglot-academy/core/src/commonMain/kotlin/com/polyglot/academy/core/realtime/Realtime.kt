package com.polyglot.academy.core.realtime

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.header
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/** What the realtime connection reports, independent of the vendor's wire format. */
sealed interface RealtimeEvent {
    data object SessionReady : RealtimeEvent
    /** The learner started talking (server voice detection); playback of the tutor should stop. */
    data object LearnerSpeechStarted : RealtimeEvent
    data object LearnerSpeechStopped : RealtimeEvent
    data class LearnerTranscript(val itemId: String?, val text: String) : RealtimeEvent
    data class TutorAudio(val pcm: ByteArray) : RealtimeEvent
    data class TutorTranscriptDelta(val responseId: String?, val text: String) : RealtimeEvent
    data class TutorTranscriptDone(val responseId: String?, val text: String) : RealtimeEvent
    data object ResponseDone : RealtimeEvent
    data class Failure(val code: String?, val message: String) : RealtimeEvent
}

/** A text WebSocket. Abstracted so the session logic can be tested without a network. */
interface RealtimeSocket {
    val incoming: Flow<String>
    suspend fun send(text: String)
    suspend fun close()
}

fun interface RealtimeTransport {
    suspend fun connect(url: String, headers: Map<String, String>): RealtimeSocket
}

/** Ktor WebSocket transport (OkHttp engine on Android and desktop). */
class KtorRealtimeTransport(base: HttpClient) : RealtimeTransport {
    private val client = base.config { install(WebSockets) }

    override suspend fun connect(url: String, headers: Map<String, String>): RealtimeSocket {
        val session = client.webSocketSession(url) { headers.forEach { (k, v) -> header(k, v) } }
        return object : RealtimeSocket {
            override val incoming: Flow<String> = session.incoming.receiveAsFlow().mapNotNull { (it as? Frame.Text)?.readText() }
            override suspend fun send(text: String) = session.send(Frame.Text(text))
            override suspend fun close() = session.close()
        }
    }
}

object RealtimeVoices {
    val all = listOf("alloy", "ash", "ballad", "coral", "echo", "sage", "shimmer", "verse", "marin", "cedar")
}

/**
 * OpenAI Realtime API wire format (WebSocket, 24 kHz PCM16). Both the current event names and the
 * older beta names are accepted when parsing, so either server version works.
 */
@OptIn(ExperimentalEncodingApi::class)
object OpenAiRealtimeProtocol {
    const val SAMPLE_RATE = 24_000
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    fun url(model: String): String {
        require(model.matches(Regex("[A-Za-z0-9._-]{1,60}"))) { "invalid model id" }
        return "wss://api.openai.com/v1/realtime?model=$model"
    }

    fun headers(apiKey: String) = mapOf("Authorization" to "Bearer $apiKey")

    fun sessionUpdate(instructions: String, voice: String, transcriptionLanguage: String?): String = buildJsonObject {
        put("type", "session.update")
        putJsonObject("session") {
            put("type", "realtime")
            put("instructions", instructions)
            put("output_modalities", buildJsonArray { add(JsonPrimitive("audio")) })
            putJsonObject("audio") {
                putJsonObject("input") {
                    putJsonObject("format") { put("type", "audio/pcm"); put("rate", SAMPLE_RATE) }
                    putJsonObject("transcription") { put("model", "gpt-4o-mini-transcribe"); transcriptionLanguage?.let { put("language", it) } }
                    putJsonObject("turn_detection") { put("type", "server_vad"); put("silence_duration_ms", 700); put("create_response", true); put("interrupt_response", true) }
                }
                putJsonObject("output") {
                    putJsonObject("format") { put("type", "audio/pcm"); put("rate", SAMPLE_RATE) }
                    put("voice", voice)
                }
            }
        }
    }.toString()

    fun appendAudio(pcm: ByteArray): String = buildJsonObject {
        put("type", "input_audio_buffer.append"); put("audio", Base64.encode(pcm))
    }.toString()

    /** Asks the model to speak first (used to open the session). */
    fun createResponse(): String = buildJsonObject { put("type", "response.create") }.toString()

    fun cancelResponse(): String = buildJsonObject { put("type", "response.cancel") }.toString()

    fun parse(text: String): RealtimeEvent? {
        val o = runCatching { json.parseToJsonElement(text).jsonObject }.getOrNull() ?: return null
        fun s(obj: JsonObject?, k: String) = (obj?.get(k) as? JsonPrimitive)?.takeIf { it.isString }?.content
        return when (s(o, "type")) {
            "session.created", "session.updated" -> RealtimeEvent.SessionReady
            "input_audio_buffer.speech_started" -> RealtimeEvent.LearnerSpeechStarted
            "input_audio_buffer.speech_stopped" -> RealtimeEvent.LearnerSpeechStopped
            "conversation.item.input_audio_transcription.completed" ->
                s(o, "transcript")?.trim()?.takeIf { it.isNotEmpty() }?.let { RealtimeEvent.LearnerTranscript(s(o, "item_id"), it) }
            "response.output_audio.delta", "response.audio.delta" ->
                s(o, "delta")?.let { runCatching { RealtimeEvent.TutorAudio(Base64.decode(it)) }.getOrNull() }
            "response.output_audio_transcript.delta", "response.audio_transcript.delta" ->
                s(o, "delta")?.let { RealtimeEvent.TutorTranscriptDelta(s(o, "response_id"), it) }
            "response.output_audio_transcript.done", "response.audio_transcript.done" ->
                RealtimeEvent.TutorTranscriptDone(s(o, "response_id"), s(o, "transcript").orEmpty().trim())
            "response.done" -> RealtimeEvent.ResponseDone
            "error" -> (o["error"] as? JsonObject).let { e -> RealtimeEvent.Failure(s(e, "code"), s(e, "message") ?: "error") }
            else -> null
        }
    }
}
