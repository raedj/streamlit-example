package com.polyglot.academy.core.ai

import io.ktor.client.HttpClient
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import kotlinx.serialization.json.addJsonObject

/** Settings an adapter needs at call time. */
fun interface ConfigSource {
    fun config(provider: ProviderId): ProviderConfig
}

private suspend fun timed(block: suspend () -> ChatResponse, now: () -> Long): Pair<ChatResponse, Long> {
    val start = now(); val r = block(); return r to (now() - start)
}

abstract class BaseHttpProvider(
    protected val client: HttpClient,
    protected val credentials: CredentialSource,
    protected val configs: ConfigSource,
    private val clock: () -> Long,
) : AiProvider {
    protected val config: ProviderConfig get() = configs.config(descriptor.id)
    protected val model: String get() = config.model?.takeIf { it.isNotBlank() } ?: descriptor.defaultModel
    protected val baseUrl: String
        get() = (config.baseUrl?.takeIf { it.isNotBlank() } ?: descriptor.defaultBaseUrl ?: throw AiError.NotConfigured(descriptor.id)).trimEnd('/')

    /** Model ids from the provider's model-list endpoint (sorted, without duplicates). */
    protected fun ids(json: kotlinx.serialization.json.JsonElement?, field: String = "id"): List<String> =
        (json as? JsonArray).orEmpty().mapNotNull { it.path(field).str() }.distinct().sorted()

    protected suspend fun requireKey(): String =
        credentials.apiKey(descriptor.id)?.takeIf { it.isNotBlank() } ?: throw AiError.NotConfigured(descriptor.id)

    override suspend fun testConnection(): ConnectionTestResult = try {
        val (r, ms) = timed({
            complete(ChatRequest(system = "Reply with the single word: ok", messages = listOf(ChatMessage(ChatRole.USER, "ping")),
                dataCategories = emptySet(), maxOutputTokens = 16, temperature = 0.0, timeoutMillis = 20_000))
        }, clock)
        ConnectionTestResult(ok = true, latencyMillis = ms, error = null, model = r.model)
    } catch (e: AiError) {
        ConnectionTestResult(ok = false, latencyMillis = null, error = e, model = null)
    }
}

/** OpenAI Chat Completions format; also used by DeepSeek and local OpenAI-compatible servers. */
open class OpenAiCompatibleProvider(
    override val descriptor: ProviderDescriptor,
    client: HttpClient,
    credentials: CredentialSource,
    configs: ConfigSource,
    clock: () -> Long,
) : BaseHttpProvider(client, credentials, configs, clock) {

    protected open suspend fun endpointAndHeaders(): Pair<String, Map<String, String>> {
        val headers = if (descriptor.requiresApiKey) mapOf("Authorization" to "Bearer ${requireKey()}")
        else credentials.apiKey(descriptor.id)?.takeIf { it.isNotBlank() }?.let { mapOf("Authorization" to "Bearer $it") } ?: emptyMap()
        // A learner-entered cloud address must use HTTPS so the key is never sent in clear text.
        if (descriptor.id == ProviderId.CUSTOM_OPENAI && !baseUrl.startsWith("https://")) throw AiError.NotConfigured(descriptor.id)
        return "$baseUrl/chat/completions" to headers
    }

    protected open fun includeModelInBody() = true

    /**
     * OpenAI's newer models take `max_completion_tokens` (and reasoning models such as gpt-5 and
     * the o-series accept only the default temperature); other compatible services use `max_tokens`.
     */
    private val usesOpenAiParameters get() = descriptor.id == ProviderId.OPENAI || descriptor.id == ProviderId.AZURE_OPENAI
    private fun isReasoningModel(m: String) = m.lowercase().let { it.startsWith("gpt-5") || Regex("^o\\d").containsMatchIn(it) }

    override suspend fun listModels(): List<String>? {
        if (descriptor.id == ProviderId.AZURE_OPENAI) return null
        val (_, headers) = endpointAndHeaders()
        return ids(getJson(client, descriptor.id, "$baseUrl/models", headers, 20_000)["data"])
    }

    override suspend fun complete(request: ChatRequest): ChatResponse {
        val (url, headers) = endpointAndHeaders()
        if (descriptor.id == ProviderId.CUSTOM_OPENAI && model.isBlank()) throw AiError.NotConfigured(descriptor.id)
        val body = buildJsonObject {
            if (includeModelInBody()) put("model", model)
            if (usesOpenAiParameters) put("max_completion_tokens", request.maxOutputTokens) else put("max_tokens", request.maxOutputTokens)
            if (!(usesOpenAiParameters && isReasoningModel(model))) put("temperature", request.temperature)
            if (request.expectJson) putJsonObject("response_format") { put("type", "json_object") }
            putJsonArray("messages") {
                addJsonObject { put("role", "system"); put("content", request.system) }
                request.messages.forEach { m ->
                    addJsonObject {
                        put("role", if (m.role == ChatRole.USER) "user" else "assistant")
                        if (m.images.isEmpty()) put("content", m.content)
                        else putJsonArray("content") {
                            addJsonObject { put("type", "text"); put("text", m.content) }
                            m.images.forEach { img ->
                                addJsonObject { put("type", "image_url"); putJsonObject("image_url") { put("url", "data:${img.mimeType};base64,${img.base64}") } }
                            }
                        }
                    }
                }
            }
        }
        val json = postJson(client, descriptor.id, url, headers, body, request.timeoutMillis)
        val choice = json.path("choices", 0)
        if (choice.path("finish_reason").str() == "content_filter") throw AiError.ContentFiltered(descriptor.id)
        val text = choice.path("message", "content").str() ?: throw AiError.InvalidResponse(descriptor.id, "missing choices[0].message.content")
        return ChatResponse(text, descriptor.id, json.path("model").str() ?: model,
            json.path("usage", "prompt_tokens").int(), json.path("usage", "completion_tokens").int())
    }
}

class AzureOpenAiProvider(
    client: HttpClient, credentials: CredentialSource, configs: ConfigSource, clock: () -> Long,
) : OpenAiCompatibleProvider(ProviderCatalog.AZURE_OPENAI, client, credentials, configs, clock) {
    override suspend fun endpointAndHeaders(): Pair<String, Map<String, String>> {
        val deployment = config.deployment?.takeIf { it.isNotBlank() } ?: throw AiError.NotConfigured(descriptor.id)
        return "$baseUrl/openai/deployments/$deployment/chat/completions?api-version=$API_VERSION" to mapOf("api-key" to requireKey())
    }
    override fun includeModelInBody() = false
    companion object { const val API_VERSION = "2024-10-21" }
}

class AnthropicProvider(
    client: HttpClient, credentials: CredentialSource, configs: ConfigSource, clock: () -> Long,
) : BaseHttpProvider(client, credentials, configs, clock) {
    override val descriptor = ProviderCatalog.ANTHROPIC

    private suspend fun headers() = mapOf("x-api-key" to requireKey(), "anthropic-version" to "2023-06-01")

    override suspend fun listModels(): List<String> = ids(getJson(client, descriptor.id, "$baseUrl/models?limit=100", headers(), 20_000)["data"])

    override suspend fun complete(request: ChatRequest): ChatResponse {
        val body = buildJsonObject {
            put("model", model)
            put("max_tokens", request.maxOutputTokens)
            put("temperature", request.temperature)
            put("system", request.system)
            putJsonArray("messages") {
                request.messages.forEach { m ->
                    addJsonObject {
                        put("role", if (m.role == ChatRole.USER) "user" else "assistant")
                        if (m.images.isEmpty()) put("content", m.content)
                        else putJsonArray("content") {
                            m.images.forEach { img ->
                                addJsonObject {
                                    put("type", "image")
                                    putJsonObject("source") { put("type", "base64"); put("media_type", img.mimeType); put("data", img.base64) }
                                }
                            }
                            addJsonObject { put("type", "text"); put("text", m.content) }
                        }
                    }
                }
            }
        }
        val json = postJson(client, descriptor.id, "$baseUrl/messages", headers(), body, request.timeoutMillis)
        val content = json["content"] as? JsonArray ?: throw AiError.InvalidResponse(descriptor.id, "missing content")
        if (json.path("stop_reason").str() == "refusal") throw AiError.ContentFiltered(descriptor.id)
        val text = content.mapNotNull { block -> if (block.path("type").str() == "text") block.path("text").str() else null }.joinToString("")
        if (text.isEmpty()) throw AiError.InvalidResponse(descriptor.id, "no text blocks")
        return ChatResponse(text, descriptor.id, json.path("model").str() ?: model,
            json.path("usage", "input_tokens").int(), json.path("usage", "output_tokens").int())
    }
}

class GeminiProvider(
    client: HttpClient, credentials: CredentialSource, configs: ConfigSource, clock: () -> Long,
) : BaseHttpProvider(client, credentials, configs, clock) {
    override val descriptor = ProviderCatalog.GEMINI

    /** Only models that support generateContent, without the "models/" prefix. */
    override suspend fun listModels(): List<String> {
        val json = getJson(client, descriptor.id, "$baseUrl/models?pageSize=200", mapOf("x-goog-api-key" to requireKey()), 20_000)
        return (json["models"] as? JsonArray).orEmpty().filter { m ->
            (m.path("supportedGenerationMethods") as? JsonArray).orEmpty().any { it.str() == "generateContent" }
        }.mapNotNull { it.path("name").str()?.removePrefix("models/") }.distinct().sorted()
    }

    override suspend fun complete(request: ChatRequest): ChatResponse {
        val body = buildJsonObject {
            putJsonObject("systemInstruction") { putJsonArray("parts") { addJsonObject { put("text", request.system) } } }
            put("contents", buildJsonArray {
                request.messages.forEach { m ->
                    addJsonObject {
                        put("role", if (m.role == ChatRole.USER) "user" else "model")
                        putJsonArray("parts") {
                            m.images.forEach { img ->
                                addJsonObject { putJsonObject("inlineData") { put("mimeType", img.mimeType); put("data", img.base64) } }
                            }
                            addJsonObject { put("text", m.content) }
                        }
                    }
                }
            })
            putJsonObject("generationConfig") {
                put("maxOutputTokens", request.maxOutputTokens)
                put("temperature", request.temperature)
                if (request.expectJson) put("responseMimeType", "application/json")
            }
        }
        val headers = mapOf("x-goog-api-key" to requireKey())
        val json = postJson(client, descriptor.id, "$baseUrl/models/$model:generateContent", headers, body, request.timeoutMillis)
        if (json.path("promptFeedback", "blockReason") != null) throw AiError.ContentFiltered(descriptor.id)
        val candidate = json.path("candidates", 0)
        if (candidate.path("finishReason").str() == "SAFETY") throw AiError.ContentFiltered(descriptor.id)
        val parts = candidate.path("content", "parts") as? JsonArray ?: throw AiError.InvalidResponse(descriptor.id, "missing candidates[0].content.parts")
        val text = parts.mapNotNull { it.path("text").str() }.joinToString("")
        if (text.isEmpty()) throw AiError.InvalidResponse(descriptor.id, "empty text")
        return ChatResponse(text, descriptor.id, json.path("modelVersion").str() ?: model,
            json.path("usageMetadata", "promptTokenCount").int(), json.path("usageMetadata", "candidatesTokenCount").int())
    }
}

/** Factory for every HTTP-based provider. */
fun createHttpProviders(
    client: HttpClient,
    credentials: CredentialSource,
    configs: ConfigSource,
    clock: () -> Long,
): List<AiProvider> = listOf(
    OpenAiCompatibleProvider(ProviderCatalog.OPENAI, client, credentials, configs, clock),
    AzureOpenAiProvider(client, credentials, configs, clock),
    GeminiProvider(client, credentials, configs, clock),
    AnthropicProvider(client, credentials, configs, clock),
    OpenAiCompatibleProvider(ProviderCatalog.DEEPSEEK, client, credentials, configs, clock),
    OpenAiCompatibleProvider(ProviderCatalog.MISTRAL, client, credentials, configs, clock),
    OpenAiCompatibleProvider(ProviderCatalog.GROQ, client, credentials, configs, clock),
    OpenAiCompatibleProvider(ProviderCatalog.XAI, client, credentials, configs, clock),
    OpenAiCompatibleProvider(ProviderCatalog.OPENROUTER, client, credentials, configs, clock),
    OpenAiCompatibleProvider(ProviderCatalog.QWEN, client, credentials, configs, clock),
    OpenAiCompatibleProvider(ProviderCatalog.CUSTOM_OPENAI, client, credentials, configs, clock),
    OpenAiCompatibleProvider(ProviderCatalog.LOCAL_SERVER, client, credentials, configs, clock),
)
