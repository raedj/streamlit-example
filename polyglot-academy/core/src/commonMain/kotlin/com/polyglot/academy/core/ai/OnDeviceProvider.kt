package com.polyglot.academy.core.ai

import com.polyglot.academy.core.localai.LocalLlmRuntime
import com.polyglot.academy.core.localai.LocalModelCatalog

/**
 * Conversation with a chat model running on the phone. Nothing leaves the device, so the privacy policy
 * allows it without the cloud consent. The model is the learner's choice in On-device models; the
 * conversation is trimmed from the oldest turn so that it fits the model's context.
 */
class OnDeviceProvider(
    private val runtime: LocalLlmRuntime,
    private val configs: ConfigSource,
) : AiProvider {
    override val descriptor = ProviderCatalog.ON_DEVICE

    companion object {
        /** Roughly 1,500 tokens of history in a 2,048-token context, leaving room for the answer. */
        const val MAX_PROMPT_CHARS = 4_500
        const val MAX_ANSWER_TOKENS = 256
    }

    private fun model() = LocalModelCatalog.byId(configs.config(ProviderId.ON_DEVICE).model)
        ?: throw AiError.NotConfigured(ProviderId.ON_DEVICE)

    override suspend fun complete(request: ChatRequest): ChatResponse {
        val model = model()
        if (!runtime.isDownloaded(model)) throw AiError.NotConfigured(ProviderId.ON_DEVICE)
        // Images cannot be read by these models; only the text goes in.
        val turns = request.messages.map { (if (it.role == ChatRole.USER) "user" else "assistant") to it.content }
        val kept = ArrayDeque<Pair<String, String>>()
        var chars = request.system.length
        for (t in turns.asReversed()) {
            if (chars + t.second.length > MAX_PROMPT_CHARS && kept.isNotEmpty()) break
            kept.addFirst(t); chars += t.second.length
        }
        val text = try {
            runtime.chat(model, listOf("system" to request.system) + kept, minOf(request.maxOutputTokens, MAX_ANSWER_TOKENS), request.temperature.toFloat())
        } catch (e: AiError) {
            throw e
        } catch (e: Exception) {
            throw AiError.InvalidResponse(ProviderId.ON_DEVICE, e.message ?: "on-device model failed")
        }
        if (text.isBlank()) throw AiError.InvalidResponse(ProviderId.ON_DEVICE, "empty answer")
        return ChatResponse(text.trim(), ProviderId.ON_DEVICE, model.id)
    }

    override suspend fun testConnection(): ConnectionTestResult = try {
        val r = complete(ChatRequest("Reply with the single word: ok", listOf(ChatMessage(ChatRole.USER, "ping")), emptySet(), maxOutputTokens = 8, temperature = 0.0))
        ConnectionTestResult(ok = true, latencyMillis = null, error = null, model = r.model)
    } catch (e: AiError) {
        ConnectionTestResult(ok = false, latencyMillis = null, error = e, model = null)
    }
}
