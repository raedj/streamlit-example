package com.polyglot.academy.core

import com.polyglot.academy.core.ai.AiCapability
import com.polyglot.academy.core.ai.AiError
import com.polyglot.academy.core.ai.ChatMessage
import com.polyglot.academy.core.ai.ChatRequest
import com.polyglot.academy.core.ai.ChatRole
import com.polyglot.academy.core.ai.NetworkState
import com.polyglot.academy.core.ai.OnDeviceProvider
import com.polyglot.academy.core.ai.ProviderConfig
import com.polyglot.academy.core.ai.ProviderId
import com.polyglot.academy.core.ai.ProviderRouter
import com.polyglot.academy.core.ai.RouteDecision
import com.polyglot.academy.core.ai.RoutingContext
import com.polyglot.academy.core.localai.LocalLlmRuntime
import com.polyglot.academy.core.localai.LocalModelCatalog
import com.polyglot.academy.core.localai.LocalModelSpec
import com.polyglot.academy.core.localai.LocalRuntime
import com.polyglot.academy.core.model.DataCategory
import com.polyglot.academy.core.model.PrivacySettings
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class OnDeviceModelTest {
    private class FakeRuntime(var downloaded: Boolean = true) : LocalLlmRuntime {
        val asked = mutableListOf<List<Pair<String, String>>>()
        override val runtime = LocalRuntime.LLAMA_CPP_GGUF
        override fun isDownloaded(model: LocalModelSpec) = downloaded
        override suspend fun download(model: LocalModelSpec, onProgress: (Float) -> Unit) = Result.success(Unit)
        override fun delete(model: LocalModelSpec) { downloaded = false }
        override suspend fun chat(model: LocalModelSpec, messages: List<Pair<String, String>>, maxTokens: Int, temperature: Float): String {
            asked += messages; return " ¡Hola! "
        }
    }

    private val model = LocalModelCatalog.models.first()
    private fun configs(id: String? = model.id) = com.polyglot.academy.core.ai.ConfigSource { ProviderConfig(it, enabled = true, model = id) }

    @Test fun catalogHasCheckedDownloads() {
        LocalModelCatalog.models.forEach { m ->
            assertTrue(m.downloadUrl.startsWith("https://huggingface.co/Qwen/") && m.downloadUrl.endsWith(m.fileName), m.id)
            assertEquals(64, m.sha256.length, m.id)
            assertEquals(m.downloadSizeMb, m.sizeBytes / 1_000_000, m.id)
        }
    }

    @Test fun conversationIsTrimmedToFitAndTheSystemMessageComesFirst() = runTest {
        val runtime = FakeRuntime()
        val provider = OnDeviceProvider(runtime, configs())
        val long = (1..40).map { ChatMessage(if (it % 2 == 1) ChatRole.USER else ChatRole.ASSISTANT, "message $it " + "x".repeat(200)) }
        val r = provider.complete(ChatRequest("You are a patient Spanish tutor.", long, setOf(DataCategory.CONVERSATION_TEXT)))
        assertEquals("¡Hola!", r.text)
        assertEquals(ProviderId.ON_DEVICE, r.providerId)
        val sent = runtime.asked.single()
        assertEquals("system" to "You are a patient Spanish tutor.", sent.first())
        assertTrue(sent.last().second.startsWith("message 40"), "the newest turn is kept")
        assertTrue(sent.size < long.size + 1, "older turns are dropped")
        assertTrue(sent.sumOf { it.second.length } <= OnDeviceProvider.MAX_PROMPT_CHARS + 250)
    }

    @Test fun aMissingModelIsReportedNotGuessed() = runTest {
        assertFailsWith<AiError.NotConfigured> { OnDeviceProvider(FakeRuntime(downloaded = false), configs()).complete(ChatRequest("s", listOf(ChatMessage(ChatRole.USER, "hi")), emptySet())) }
        assertFailsWith<AiError.NotConfigured> { OnDeviceProvider(FakeRuntime(), configs(id = "unknown")).complete(ChatRequest("s", listOf(ChatMessage(ChatRole.USER, "hi")), emptySet())) }
    }

    @Test fun onDeviceChatWorksOfflineWithCloudAiOff() {
        val ctx = RoutingContext(PrivacySettings(cloudAiEnabled = false), NetworkState.OFFLINE, ProviderId.ON_DEVICE, emptyMap(), emptySet(), onDeviceReady = true)
        val d = ProviderRouter.route(AiCapability.CHAT, setOf(DataCategory.CONVERSATION_TEXT), ctx)
        assertEquals(listOf(ProviderId.ON_DEVICE), assertIs<RouteDecision.Route>(d).ordered)
        // Not for structured feedback, which small models do not produce reliably.
        assertIs<RouteDecision.Blocked>(ProviderRouter.route(AiCapability.STRUCTURED_FEEDBACK, setOf(DataCategory.WRITING_SUBMISSION), ctx))
        assertIs<RouteDecision.Blocked>(ProviderRouter.route(AiCapability.CHAT, emptySet(), ctx.copy(onDeviceReady = false)))
    }

    @Test fun aModelOnThePhoneGetsAShortTutorPrompt() {
        val ctx = com.polyglot.academy.core.tutor.TutorContext(com.polyglot.academy.core.tutor.TutorRoster.tutors.first(), com.polyglot.academy.core.model.LanguageCode.ENGLISH,
            com.polyglot.academy.core.model.LanguageCode.ARABIC, com.polyglot.academy.core.model.CefrLevel.A2, com.polyglot.academy.core.model.CorrectionIntensity.BALANCED,
            null, true, topic = "cooking")
        val full = com.polyglot.academy.core.tutor.TutorPromptBuilder.systemPrompt(ctx)
        val short = com.polyglot.academy.core.tutor.TutorPromptBuilder.compactPrompt(ctx)
        assertTrue(short.length * 4 < full.length, "short ${short.length} vs full ${full.length} characters")
        assertTrue("ONLY in English" in short && "A2" in short && "Never write in Arabic" in short && "cooking" in short && "Correction:" in short)
        assertTrue("JSON" !in short, "a small model gets plain text to write, not JSON")
        // A plain answer: the correction line becomes a correction of the learner's last message.
        val p = com.polyglot.academy.core.tutor.TutorResponseParser
        val r = p.parseCompact("Correction: I like cooking pasta with my family on Sundays.\nThat sounds lovely! What sauce do you make?",
            "I likes cook pasta with my family on Sunday", "ON_DEVICE")
        assertEquals("That sounds lovely! What sauce do you make?", r.text)
        assertEquals("I likes cook pasta with my family on Sunday", r.corrections.single().original)
        assertEquals("I like cooking pasta with my family on Sundays.", r.corrections.single().corrected)
        // No mistake, or the same sentence back: no correction card.
        assertTrue(p.parseCompact("Great! Do you cook every week?", "I cook every Sunday.", "ON_DEVICE").corrections.isEmpty())
        assertTrue(p.parseCompact("Correction: I cook every Sunday.\nNice!", "I cook every Sunday.", "ON_DEVICE").corrections.isEmpty())
        // A model that answers in JSON anyway is still read.
        assertEquals("Nice!", p.parseCompact("""{"reply": "Nice!", "corrections": []}""", "x", "ON_DEVICE").text)
    }
}
