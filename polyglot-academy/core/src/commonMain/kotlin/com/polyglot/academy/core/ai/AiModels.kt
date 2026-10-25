package com.polyglot.academy.core.ai

import com.polyglot.academy.core.model.DataCategory
import com.polyglot.academy.core.model.ProviderLocation
import kotlinx.serialization.Serializable

@Serializable
enum class ProviderId {
    OPENAI, AZURE_OPENAI, GEMINI, ANTHROPIC, DEEPSEEK,
    MISTRAL, GROQ, XAI, OPENROUTER, QWEN,
    /** Any other cloud service with an OpenAI-compatible Chat Completions API (the learner enters address and model). */
    CUSTOM_OPENAI,
    /** OpenAI-compatible server the learner runs themselves (Ollama, llama.cpp server, LM Studio). */
    LOCAL_SERVER,
    /** In-process on-device model runtime. */
    ON_DEVICE,
}

@Serializable
enum class AiCapability { CHAT, STRUCTURED_FEEDBACK, TRANSLATION, VISION }

data class ProviderDescriptor(
    val id: ProviderId,
    val displayName: String,
    val location: ProviderLocation,
    val capabilities: Set<AiCapability>,
    val requiresApiKey: Boolean,
    val requiresBaseUrl: Boolean,
    val defaultModel: String,
    val defaultBaseUrl: String?,
    val documentationUrl: String,
    /** Whether the vendor offers speech APIs; this app uses the platform speech services. */
    val vendorOffersSpeechApis: Boolean,
    /** Model ids offered as quick choices (any other id can be typed or loaded from the account). */
    val suggestedModels: List<String> = listOf(defaultModel),
    /** Where the learner creates an API key. */
    val keyUrl: String? = null,
)

/** Non-secret, persisted configuration. Secrets live in the platform secret store. */
@Serializable
data class ProviderConfig(
    val id: ProviderId,
    val enabled: Boolean = false,
    val model: String? = null,
    val baseUrl: String? = null,
    /** Azure only: deployment name. */
    val deployment: String? = null,
    val lastTest: ConnectionStatus = ConnectionStatus.NEVER_TESTED,
    val lastTestedAtMillis: Long? = null,
    val lastErrorMessage: String? = null,
    val requestCount: Int = 0,
    val approxTokensUsed: Long = 0,
)

@Serializable
enum class ConnectionStatus { NEVER_TESTED, VERIFIED, FAILED }

@Serializable
enum class ChatRole { USER, ASSISTANT }

/** An image sent with a message (vision requests only), already base64-encoded. */
@Serializable
data class ImagePart(val mimeType: String, val base64: String)

@Serializable
data class ChatMessage(val role: ChatRole, val content: String, val images: List<ImagePart> = emptyList())

data class ChatRequest(
    val system: String,
    val messages: List<ChatMessage>,
    /** Every kind of user data present in the request; used for privacy enforcement. */
    val dataCategories: Set<DataCategory>,
    val maxOutputTokens: Int = 700,
    val temperature: Double = 0.7,
    val expectJson: Boolean = false,
    val timeoutMillis: Long = 45_000,
)

data class ChatResponse(
    val text: String,
    val providerId: ProviderId,
    val model: String,
    val inputTokens: Int? = null,
    val outputTokens: Int? = null,
)

/** Classified provider failures. Messages never include secrets or user content. */
sealed class AiError(message: String) : Exception(message) {
    abstract val retryable: Boolean
    class NotConfigured(val provider: ProviderId) : AiError("$provider is not configured") { override val retryable = false }
    class Authentication(val provider: ProviderId) : AiError("$provider rejected the credentials") { override val retryable = false }
    class QuotaExceeded(val provider: ProviderId) : AiError("$provider quota or billing limit reached") { override val retryable = false }
    class RateLimited(val provider: ProviderId, val retryAfterMillis: Long?) : AiError("$provider rate limit") { override val retryable = true }
    class Timeout(val provider: ProviderId) : AiError("$provider timed out") { override val retryable = true }
    class Network(val provider: ProviderId, detail: String) : AiError("Network error contacting $provider: $detail") { override val retryable = true }
    class Server(val provider: ProviderId, val status: Int) : AiError("$provider server error $status") { override val retryable = status >= 500 }
    class InvalidResponse(val provider: ProviderId, detail: String) : AiError("$provider returned an unexpected response: $detail") { override val retryable = false }
    class ContentFiltered(val provider: ProviderId) : AiError("$provider declined to answer") { override val retryable = false }
    class BlockedByPrivacy(val provider: ProviderId?, val categories: Set<DataCategory>) :
        AiError("Blocked by privacy settings: ${categories.joinToString()}") { override val retryable = false }
    class NoProviderAvailable(val reason: String) : AiError(reason) { override val retryable = false }
    class Cancelled : AiError("Cancelled") { override val retryable = false }
}

data class ConnectionTestResult(val ok: Boolean, val latencyMillis: Long?, val error: AiError?, val model: String?)

interface AiProvider {
    val descriptor: ProviderDescriptor
    suspend fun complete(request: ChatRequest): ChatResponse
    suspend fun testConnection(): ConnectionTestResult
    /** Model ids available to the learner's key, or null when the provider has no listing API. */
    suspend fun listModels(): List<String>? = null
}

object ProviderCatalog {
    val OPENAI = ProviderDescriptor(
        ProviderId.OPENAI, "OpenAI", ProviderLocation.CLOUD,
        setOf(AiCapability.CHAT, AiCapability.STRUCTURED_FEEDBACK, AiCapability.TRANSLATION, AiCapability.VISION),
        requiresApiKey = true, requiresBaseUrl = false, defaultModel = "gpt-4.1-mini",
        defaultBaseUrl = "https://api.openai.com/v1", documentationUrl = "https://platform.openai.com/docs/api-reference/chat",
        vendorOffersSpeechApis = true,
        suggestedModels = listOf("gpt-4.1-mini", "gpt-4.1", "gpt-4o-mini", "gpt-5-mini", "gpt-5"),
        keyUrl = "https://platform.openai.com/api-keys",
    )
    val AZURE_OPENAI = ProviderDescriptor(
        ProviderId.AZURE_OPENAI, "Azure OpenAI (Microsoft)", ProviderLocation.CLOUD,
        setOf(AiCapability.CHAT, AiCapability.STRUCTURED_FEEDBACK, AiCapability.TRANSLATION),
        requiresApiKey = true, requiresBaseUrl = true, defaultModel = "deployment",
        defaultBaseUrl = null, documentationUrl = "https://learn.microsoft.com/azure/ai-services/openai/reference",
        vendorOffersSpeechApis = true, suggestedModels = emptyList(), keyUrl = "https://portal.azure.com",
    )
    val GEMINI = ProviderDescriptor(
        ProviderId.GEMINI, "Google Gemini", ProviderLocation.CLOUD,
        setOf(AiCapability.CHAT, AiCapability.STRUCTURED_FEEDBACK, AiCapability.TRANSLATION, AiCapability.VISION),
        requiresApiKey = true, requiresBaseUrl = false, defaultModel = "gemini-2.5-flash",
        defaultBaseUrl = "https://generativelanguage.googleapis.com/v1beta", documentationUrl = "https://ai.google.dev/api/generate-content",
        vendorOffersSpeechApis = true,
        suggestedModels = listOf("gemini-2.5-flash", "gemini-2.5-flash-lite", "gemini-2.5-pro"),
        keyUrl = "https://aistudio.google.com/apikey",
    )
    val ANTHROPIC = ProviderDescriptor(
        ProviderId.ANTHROPIC, "Anthropic Claude", ProviderLocation.CLOUD,
        setOf(AiCapability.CHAT, AiCapability.STRUCTURED_FEEDBACK, AiCapability.TRANSLATION, AiCapability.VISION),
        requiresApiKey = true, requiresBaseUrl = false, defaultModel = "claude-haiku-5-5",
        defaultBaseUrl = "https://api.anthropic.com/v1", documentationUrl = "https://docs.anthropic.com/en/api/messages",
        vendorOffersSpeechApis = false,
        suggestedModels = listOf("claude-haiku-5-5", "claude-sonnet-5-5", "claude-opus-5-5"),
        keyUrl = "https://console.anthropic.com/settings/keys",
    )
    val DEEPSEEK = ProviderDescriptor(
        ProviderId.DEEPSEEK, "DeepSeek", ProviderLocation.CLOUD,
        setOf(AiCapability.CHAT, AiCapability.STRUCTURED_FEEDBACK, AiCapability.TRANSLATION),
        requiresApiKey = true, requiresBaseUrl = false, defaultModel = "deepseek-chat",
        defaultBaseUrl = "https://api.deepseek.com", documentationUrl = "https://api-docs.deepseek.com/",
        vendorOffersSpeechApis = false,
        suggestedModels = listOf("deepseek-chat", "deepseek-reasoner"),
        keyUrl = "https://platform.deepseek.com/api_keys",
    )
    private val textCaps = setOf(AiCapability.CHAT, AiCapability.STRUCTURED_FEEDBACK, AiCapability.TRANSLATION)
    val MISTRAL = ProviderDescriptor(
        ProviderId.MISTRAL, "Mistral AI", ProviderLocation.CLOUD, textCaps,
        requiresApiKey = true, requiresBaseUrl = false, defaultModel = "mistral-small-latest",
        defaultBaseUrl = "https://api.mistral.ai/v1", documentationUrl = "https://docs.mistral.ai/api/",
        vendorOffersSpeechApis = false,
        suggestedModels = listOf("mistral-small-latest", "mistral-medium-latest", "mistral-large-latest"),
        keyUrl = "https://console.mistral.ai/api-keys",
    )
    val GROQ = ProviderDescriptor(
        ProviderId.GROQ, "Groq", ProviderLocation.CLOUD, textCaps,
        requiresApiKey = true, requiresBaseUrl = false, defaultModel = "llama-3.3-70b-versatile",
        defaultBaseUrl = "https://api.groq.com/openai/v1", documentationUrl = "https://console.groq.com/docs/api-reference",
        vendorOffersSpeechApis = true,
        suggestedModels = listOf("llama-3.3-70b-versatile", "llama-3.1-8b-instant"),
        keyUrl = "https://console.groq.com/keys",
    )
    val XAI = ProviderDescriptor(
        ProviderId.XAI, "xAI Grok", ProviderLocation.CLOUD, textCaps,
        requiresApiKey = true, requiresBaseUrl = false, defaultModel = "grok-3-mini",
        defaultBaseUrl = "https://api.x.ai/v1", documentationUrl = "https://docs.x.ai/docs/api-reference",
        vendorOffersSpeechApis = false,
        suggestedModels = listOf("grok-3-mini", "grok-3", "grok-4"),
        keyUrl = "https://console.x.ai",
    )
    val OPENROUTER = ProviderDescriptor(
        ProviderId.OPENROUTER, "OpenRouter (many models, one key)", ProviderLocation.CLOUD, textCaps,
        requiresApiKey = true, requiresBaseUrl = false, defaultModel = "openai/gpt-4o-mini",
        defaultBaseUrl = "https://openrouter.ai/api/v1", documentationUrl = "https://openrouter.ai/docs/api-reference/overview",
        vendorOffersSpeechApis = false,
        suggestedModels = listOf("openai/gpt-4o-mini", "anthropic/claude-sonnet-4", "google/gemini-2.5-flash", "deepseek/deepseek-chat", "meta-llama/llama-3.3-70b-instruct", "mistralai/mistral-small"),
        keyUrl = "https://openrouter.ai/keys",
    )
    val QWEN = ProviderDescriptor(
        ProviderId.QWEN, "Alibaba Qwen (Model Studio)", ProviderLocation.CLOUD, textCaps,
        requiresApiKey = true, requiresBaseUrl = false, defaultModel = "qwen-plus",
        defaultBaseUrl = "https://dashscope-intl.aliyuncs.com/compatible-mode/v1", documentationUrl = "https://www.alibabacloud.com/help/en/model-studio/compatibility-of-openai-with-dashscope",
        vendorOffersSpeechApis = true,
        suggestedModels = listOf("qwen-plus", "qwen-turbo", "qwen-max"),
        keyUrl = "https://modelstudio.console.alibabacloud.com",
    )
    val CUSTOM_OPENAI = ProviderDescriptor(
        ProviderId.CUSTOM_OPENAI, "Other (OpenAI-compatible service)", ProviderLocation.CLOUD, textCaps,
        requiresApiKey = true, requiresBaseUrl = true, defaultModel = "",
        defaultBaseUrl = null, documentationUrl = "https://platform.openai.com/docs/api-reference/chat",
        vendorOffersSpeechApis = false, suggestedModels = emptyList(),
    )
    val LOCAL_SERVER = ProviderDescriptor(
        ProviderId.LOCAL_SERVER, "Local server (OpenAI-compatible)", ProviderLocation.LOCAL_NETWORK,
        setOf(AiCapability.CHAT, AiCapability.STRUCTURED_FEEDBACK, AiCapability.TRANSLATION),
        requiresApiKey = false, requiresBaseUrl = true, defaultModel = "llama3.2",
        defaultBaseUrl = "http://127.0.0.1:11434/v1", documentationUrl = "https://github.com/ollama/ollama/blob/main/docs/openai.md",
        vendorOffersSpeechApis = false,
        suggestedModels = listOf("llama3.2", "qwen2.5", "gemma3", "mistral"),
    )
    val ON_DEVICE = ProviderDescriptor(
        ProviderId.ON_DEVICE, "On-device model", ProviderLocation.ON_DEVICE,
        setOf(AiCapability.CHAT),
        requiresApiKey = false, requiresBaseUrl = false, defaultModel = "none",
        defaultBaseUrl = null, documentationUrl = "docs/AI_PROVIDER_MATRIX.md",
        vendorOffersSpeechApis = false,
    )

    val all = listOf(OPENAI, AZURE_OPENAI, GEMINI, ANTHROPIC, DEEPSEEK, MISTRAL, GROQ, XAI, OPENROUTER, QWEN, CUSTOM_OPENAI, LOCAL_SERVER, ON_DEVICE)
    fun of(id: ProviderId) = all.first { it.id == id }
}
