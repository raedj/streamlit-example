package com.polyglot.academy.core.ai

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

internal val providerJson = Json { ignoreUnknownKeys = true; isLenient = true; explicitNulls = false }

/** Credentials are fetched lazily per request so they are never held in long-lived objects. */
fun interface CredentialSource {
    suspend fun apiKey(provider: ProviderId): String?
}

internal suspend fun postJson(
    client: HttpClient,
    provider: ProviderId,
    url: String,
    headers: Map<String, String>,
    body: JsonObject,
    timeoutMillis: Long,
): JsonObject {
    val text: String
    val status: Int
    var retryAfter: Long? = null
    try {
        val response = withTimeout(timeoutMillis) {
            client.post(url) {
                contentType(ContentType.Application.Json)
                headers.forEach { (k, v) -> header(k, v) }
                setBody(body.toString())
            }
        }
        status = response.status.value
        retryAfter = response.headers[HttpHeaders.RetryAfter]?.toLongOrNull()?.times(1000)
        text = withTimeout(timeoutMillis) { response.bodyAsText() }
    } catch (e: TimeoutCancellationException) {
        throw AiError.Timeout(provider)
    } catch (e: CancellationException) {
        throw e
    } catch (e: AiError) {
        throw e
    } catch (e: Exception) {
        throw AiError.Network(provider, networkFailureKind(e))
    }
    if (status !in 200..299) throw classifyHttpError(provider, status, text, retryAfter)
    return try {
        providerJson.parseToJsonElement(text).jsonObject
    } catch (e: Exception) {
        throw AiError.InvalidResponse(provider, "body is not a JSON object")
    }
}

internal suspend fun getJson(client: HttpClient, provider: ProviderId, url: String, headers: Map<String, String>, timeoutMillis: Long): JsonObject {
    val status: Int
    val text: String
    try {
        val response = withTimeout(timeoutMillis) { client.get(url) { headers.forEach { (k, v) -> header(k, v) } } }
        status = response.status.value
        text = withTimeout(timeoutMillis) { response.bodyAsText() }
    } catch (e: TimeoutCancellationException) {
        throw AiError.Timeout(provider)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        throw AiError.Network(provider, networkFailureKind(e))
    }
    if (status !in 200..299) throw classifyHttpError(provider, status, text, null)
    return try { providerJson.parseToJsonElement(text).jsonObject } catch (e: Exception) { throw AiError.InvalidResponse(provider, "body is not a JSON object") }
}

/**
 * The most telling exception type in the chain (the HTTP client often wraps the real one), so the
 * learner can be told "the address was not found" or "a secure connection failed" instead of only
 * "network error". Only the type name is kept: messages may contain addresses or headers.
 */
internal fun networkFailureKind(e: Throwable): String {
    val names = generateSequence(e) { it.cause.takeIf { c -> c !== it } }.take(8).mapNotNull { it::class.simpleName }.toList()
    val telling = listOf("UnknownHost", "SSL", "Certificate", "CertPath", "ConnectException", "NoRouteToHost", "SocketTimeout", "PortUnreachable", "URLParser", "URLDecode")
    return names.firstOrNull { n -> telling.any { it in n } } ?: names.firstOrNull() ?: "IO"
}

internal fun classifyHttpError(provider: ProviderId, status: Int, body: String, retryAfterMillis: Long?): AiError {
    val lower = body.lowercase()
    return when {
        status == 401 || status == 403 -> AiError.Authentication(provider)
        status == 402 -> AiError.QuotaExceeded(provider)
        status == 429 && ("insufficient_quota" in lower || "billing" in lower || "resource_exhausted" in lower && "quota" in lower) ->
            AiError.QuotaExceeded(provider)
        status == 429 -> AiError.RateLimited(provider, retryAfterMillis)
        // Gemini answers a wrong key with 400 INVALID_ARGUMENT / API_KEY_INVALID, not 401.
        status == 400 && ("api_key_invalid" in lower || "api key not valid" in lower || "invalid_api_key" in lower || "invalid api key" in lower) ->
            AiError.Authentication(provider)
        status == 400 && "location is not supported" in lower -> AiError.RegionNotSupported(provider)
        status == 404 -> AiError.InvalidResponse(provider, "model or endpoint not found (HTTP 404)")
        status >= 500 -> AiError.Server(provider, status)
        "safety" in lower || "content_filter" in lower || "content_policy" in lower -> AiError.ContentFiltered(provider)
        else -> AiError.InvalidResponse(provider, "HTTP $status" + (providerErrorMessage(body)?.let { ": $it" } ?: ""))
    }
}

/** The provider's own short error text (e.g. error.message), without anything that looks like a key. */
internal fun providerErrorMessage(body: String): String? {
    val json = runCatching { providerJson.parseToJsonElement(body) }.getOrNull() ?: return null
    val msg = (json.path("error", "message") ?: json.path("message") ?: json.path("error")).str()?.trim()
    return msg?.takeIf { it.isNotEmpty() }?.let { SecretRedactor.redact(it).take(200) }
}

internal fun JsonElement?.path(vararg keys: Any): JsonElement? {
    var cur: JsonElement? = this
    for (k in keys) {
        cur = when (k) {
            is String -> (cur as? JsonObject)?.get(k)
            is Int -> (cur as? kotlinx.serialization.json.JsonArray)?.getOrNull(k)
            else -> null
        }
        if (cur == null) return null
    }
    return cur
}

internal fun JsonElement?.str(): String? = (this as? kotlinx.serialization.json.JsonPrimitive)?.takeIf { it.isString }?.content
internal fun JsonElement?.int(): Int? = (this as? kotlinx.serialization.json.JsonPrimitive)?.content?.toIntOrNull()
