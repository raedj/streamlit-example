package com.polyglot.academy.core.ai

import com.polyglot.academy.core.model.DataCategory
import com.polyglot.academy.core.model.ProviderLocation
import kotlinx.serialization.Serializable

@Serializable
enum class ProviderId {
    OPENAI, AZURE_OPENAI, GEMINI, ANTHROPIC, DEEPSEEK,
    /** OpenAI-compatible server the learner runs themselves (Ollama, llama.cpp server, LM Studio). */
    LOCAL_SERVER,
    /** In-process on-device model runtime. */
    ON_DEVICE,
}

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

@Serializable
data class ChatMessage(val role: ChatRole, val content: String)

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
}

object ProviderCatalog {
    val OPENAI = ProviderDescriptor(
        ProviderId.OPENAI, "OpenAI", ProviderLocation.CLOUD,
        setOf(AiCapability.CHAT, AiCapability.STRUCTURED_FEEDBACK, AiCapability.TRANSLATION, AiCapability.VISION),
        requiresApiKey = true, requiresBaseUrl = false, defaultModel = "gpt-4.1-mini",
        defaultBaseUrl = "https://api.openai.com/v1", documentationUrl = "https://platform.openai.com/docs/api-reference/chat",
        vendorOffersSpeechApis = true,
    )
    val AZURE_OPENAI = ProviderDescriptor(
        ProviderId.AZURE_OPENAI, "Azure OpenAI (Microsoft)", ProviderLocation.CLOUD,
        setOf(AiCapability.CHAT, AiCapability.STRUCTURED_FEEDBACK, AiCapability.TRANSLATION),
        requiresApiKey = true, requiresBaseUrl = true, defaultModel = "deployment",
        defaultBaseUrl = null, documentationUrl = "https://learn.microsoft.com/azure/ai-services/openai/reference",
        vendorOffersSpeechApis = true,
    )
    val GEMINI = ProviderDescriptor(
        ProviderId.GEMINI, "Google Gemini", ProviderLocation.CLOUD,
        setOf(AiCapability.CHAT, AiCapability.STRUCTURED_FEEDBACK, AiCapability.TRANSLATION, AiCapability.VISION),
        requiresApiKey = true, requiresBaseUrl = false, defaultModel = "gemini-2.5-flash",
        defaultBaseUrl = "https://generativelanguage.googleapis.com/v1beta", documentationUrl = "https://ai.google.dev/api/generate-content",
        vendorOffersSpeechApis = true,
    )
    val ANTHROPIC = ProviderDescriptor(
        ProviderId.ANTHROPIC, "Anthropic Claude", ProviderLocation.CLOUD,
        setOf(AiCapability.CHAT, AiCapability.STRUCTURED_FEEDBACK, AiCapability.TRANSLATION, AiCapability.VISION),
        requiresApiKey = true, requiresBaseUrl = false, defaultModel = "claude-haiku-5-5",
        defaultBaseUrl = "https://api.anthropic.com/v1", documentationUrl = "https://docs.anthropic.com/en/api/messages",
        vendorOffersSpeechApis = false,
    )
    val DEEPSEEK = ProviderDescriptor(
        ProviderId.DEEPSEEK, "DeepSeek", ProviderLocation.CLOUD,
        setOf(AiCapability.CHAT, AiCapability.STRUCTURED_FEEDBACK, AiCapability.TRANSLATION),
        requiresApiKey = true, requiresBaseUrl = false, defaultModel = "deepseek-chat",
        defaultBaseUrl = "https://api.deepseek.com", documentationUrl = "https://api-docs.deepseek.com/",
        vendorOffersSpeechApis = false,
    )
    val LOCAL_SERVER = ProviderDescriptor(
        ProviderId.LOCAL_SERVER, "Local server (OpenAI-compatible)", ProviderLocation.LOCAL_NETWORK,
        setOf(AiCapability.CHAT, AiCapability.STRUCTURED_FEEDBACK, AiCapability.TRANSLATION),
        requiresApiKey = false, requiresBaseUrl = true, defaultModel = "llama3.2",
        defaultBaseUrl = "http://127.0.0.1:11434/v1", documentationUrl = "https://github.com/ollama/ollama/blob/main/docs/openai.md",
        vendorOffersSpeechApis = false,
    )
    val ON_DEVICE = ProviderDescriptor(
        ProviderId.ON_DEVICE, "On-device model", ProviderLocation.ON_DEVICE,
        setOf(AiCapability.CHAT),
        requiresApiKey = false, requiresBaseUrl = false, defaultModel = "none",
        defaultBaseUrl = null, documentationUrl = "docs/AI_PROVIDER_MATRIX.md",
        vendorOffersSpeechApis = false,
    )

    val all = listOf(OPENAI, AZURE_OPENAI, GEMINI, ANTHROPIC, DEEPSEEK, LOCAL_SERVER, ON_DEVICE)
    fun of(id: ProviderId) = all.first { it.id == id }
}
