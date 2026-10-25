package com.polyglot.academy.core

import com.polyglot.academy.core.ai.AiError
import com.polyglot.academy.core.ai.AnthropicProvider
import com.polyglot.academy.core.ai.AzureOpenAiProvider
import com.polyglot.academy.core.ai.ChatMessage
import com.polyglot.academy.core.ai.ChatRequest
import com.polyglot.academy.core.ai.ChatRole
import com.polyglot.academy.core.ai.ConfigSource
import com.polyglot.academy.core.ai.CredentialSource
import com.polyglot.academy.core.ai.GeminiProvider
import com.polyglot.academy.core.ai.OpenAiCompatibleProvider
import com.polyglot.academy.core.ai.ProviderCatalog
import com.polyglot.academy.core.ai.ProviderConfig
import com.polyglot.academy.core.ai.ProviderId
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Provider timeouts use real time; run on Dispatchers.Default instead of the virtual-time test scheduler. */
private fun realTimeTest(block: suspend CoroutineScope.() -> Unit) = runTest { withContext(Dispatchers.Default) { block() } }

class AdapterTest {
    private val json = headersOf(HttpHeaders.ContentType, "application/json")
    private val keys = CredentialSource { "test-key-123" }
    private val noKeys = CredentialSource { null }
    private fun configs(vararg c: ProviderConfig) = ConfigSource { id -> c.firstOrNull { it.id == id } ?: ProviderConfig(id, enabled = true) }
    private val req = ChatRequest("Be a tutor", listOf(ChatMessage(ChatRole.USER, "Hello"), ChatMessage(ChatRole.ASSISTANT, "Hi!"), ChatMessage(ChatRole.USER, "How are you?")), emptySet())

    private var lastRequest: HttpRequestData? = null
    private var lastBody: String = ""

    private fun client(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData) = HttpClient(MockEngine { r ->
        lastRequest = r
        lastBody = (r.body as? TextContent)?.text ?: r.body.toString()
        handler(r)
    })

    @Test fun openAiRequestAndResponse() = realTimeTest {
        val c = client { respond("""{"model":"gpt-x","choices":[{"message":{"content":"Fine, thanks!"},"finish_reason":"stop"}],"usage":{"prompt_tokens":12,"completion_tokens":4}}""", HttpStatusCode.OK, json) }
        val p = OpenAiCompatibleProvider(ProviderCatalog.OPENAI, c, keys, configs(), { 0L })
        val r = p.complete(req)
        assertEquals("Fine, thanks!", r.text)
        assertEquals(12, r.inputTokens)
        assertEquals("https://api.openai.com/v1/chat/completions", lastRequest!!.url.toString())
        assertEquals("Bearer test-key-123", lastRequest!!.headers[HttpHeaders.Authorization])
        val body = Json.parseToJsonElement(lastBody).jsonObject
        val msgs = body["messages"]!!.jsonArray
        assertEquals("system", msgs[0].jsonObject["role"]!!.jsonPrimitive.content)
        assertEquals("assistant", msgs[2].jsonObject["role"]!!.jsonPrimitive.content)
        assertEquals(4, msgs.size)
    }

    @Test fun deepSeekAndLocalServerUseConfiguredBaseUrl() = realTimeTest {
        val c = client { respond("""{"choices":[{"message":{"content":"ok"}}]}""", HttpStatusCode.OK, json) }
        OpenAiCompatibleProvider(ProviderCatalog.DEEPSEEK, c, keys, configs(), { 0L }).complete(req)
        assertEquals("https://api.deepseek.com/chat/completions", lastRequest!!.url.toString())
        val local = OpenAiCompatibleProvider(ProviderCatalog.LOCAL_SERVER, c, noKeys,
            configs(ProviderConfig(ProviderId.LOCAL_SERVER, enabled = true, baseUrl = "http://127.0.0.1:8080/v1/", model = "qwen")), { 0L })
        local.complete(req)
        assertEquals("http://127.0.0.1:8080/v1/chat/completions", lastRequest!!.url.toString())
        assertEquals(null, lastRequest!!.headers[HttpHeaders.Authorization])
        assertTrue("\"qwen\"" in lastBody)
    }

    @Test fun everyOpenAiCompatibleCloudProviderUsesItsOwnAddressAndKey() = realTimeTest {
        val c = client { respond("""{"choices":[{"message":{"content":"ok"}}]}""", HttpStatusCode.OK, json) }
        val expected = mapOf(
            ProviderCatalog.MISTRAL to "https://api.mistral.ai/v1/chat/completions",
            ProviderCatalog.GROQ to "https://api.groq.com/openai/v1/chat/completions",
            ProviderCatalog.XAI to "https://api.x.ai/v1/chat/completions",
            ProviderCatalog.OPENROUTER to "https://openrouter.ai/api/v1/chat/completions",
            ProviderCatalog.QWEN to "https://dashscope-intl.aliyuncs.com/compatible-mode/v1/chat/completions",
        )
        expected.forEach { (d, url) ->
            OpenAiCompatibleProvider(d, c, keys, configs(), { 0L }).complete(req)
            assertEquals(url, lastRequest!!.url.toString())
            assertEquals("Bearer test-key-123", lastRequest!!.headers[HttpHeaders.Authorization])
            assertTrue("\"${d.defaultModel}\"" in lastBody && "\"max_tokens\"" in lastBody, d.displayName)
        }
        assertFailsWith<AiError.NotConfigured> { OpenAiCompatibleProvider(ProviderCatalog.MISTRAL, c, noKeys, configs(), { 0L }).complete(req) }
        // Any other OpenAI-compatible service: address and model come from the learner, HTTPS only.
        val custom = { url: String, model: String? -> OpenAiCompatibleProvider(ProviderCatalog.CUSTOM_OPENAI, c, keys,
            configs(ProviderConfig(ProviderId.CUSTOM_OPENAI, enabled = true, baseUrl = url, model = model)), { 0L }) }
        custom("https://api.together.xyz/v1", "meta-llama/Llama-3.3-70B-Instruct-Turbo").complete(req)
        assertEquals("https://api.together.xyz/v1/chat/completions", lastRequest!!.url.toString())
        assertFailsWith<AiError.NotConfigured> { custom("http://api.example.com/v1", "m").complete(req) }
        assertFailsWith<AiError.NotConfigured> { custom("https://api.example.com/v1", null).complete(req) }
    }

    @Test fun openAiNewerModelsGetTheirOwnParameters() = realTimeTest {
        val c = client { respond("""{"choices":[{"message":{"content":"ok"}}]}""", HttpStatusCode.OK, json) }
        val p = { m: String -> OpenAiCompatibleProvider(ProviderCatalog.OPENAI, c, keys, configs(ProviderConfig(ProviderId.OPENAI, enabled = true, model = m)), { 0L }) }
        p("gpt-4.1-mini").complete(req)
        assertTrue("\"max_completion_tokens\"" in lastBody && "\"temperature\"" in lastBody && "\"max_tokens\"" !in lastBody)
        p("gpt-5-mini").complete(req)
        assertTrue("\"max_completion_tokens\"" in lastBody && "\"temperature\"" !in lastBody, "reasoning models accept only the default temperature")
        p("o4-mini").complete(req)
        assertTrue("\"temperature\"" !in lastBody)
    }

    @Test fun modelListsAreLoadedFromTheAccount() = realTimeTest {
        val oa = client { respond("""{"data":[{"id":"gpt-4.1"},{"id":"gpt-4.1-mini"},{"id":"gpt-4.1"}]}""", HttpStatusCode.OK, json) }
        assertEquals(listOf("gpt-4.1", "gpt-4.1-mini"), OpenAiCompatibleProvider(ProviderCatalog.OPENAI, oa, keys, configs(), { 0L }).listModels())
        assertEquals("https://api.openai.com/v1/models", lastRequest!!.url.toString())
        assertEquals("GET", lastRequest!!.method.value)
        val an = client { respond("""{"data":[{"id":"claude-sonnet-5-5","type":"model"},{"id":"claude-haiku-5-5","type":"model"}]}""", HttpStatusCode.OK, json) }
        assertEquals(listOf("claude-haiku-5-5", "claude-sonnet-5-5"), AnthropicProvider(an, keys, configs(), { 0L }).listModels())
        assertEquals("test-key-123", lastRequest!!.headers["x-api-key"])
        val ge = client { respond("""{"models":[{"name":"models/gemini-2.5-flash","supportedGenerationMethods":["generateContent","countTokens"]},
            {"name":"models/text-embedding-004","supportedGenerationMethods":["embedContent"]}]}""", HttpStatusCode.OK, json) }
        assertEquals(listOf("gemini-2.5-flash"), GeminiProvider(ge, keys, configs(), { 0L }).listModels())
        assertFalse("key=" in lastRequest!!.url.toString())
        assertEquals(null, AzureOpenAiProvider(oa, keys, configs(), { 0L }).listModels(), "Azure has deployments, not a model list")
        val denied = client { respond("", HttpStatusCode.Unauthorized) }
        assertFailsWith<AiError.Authentication> { OpenAiCompatibleProvider(ProviderCatalog.GROQ, denied, keys, configs(), { 0L }).listModels() }
    }

    @Test fun anthropicFormat() = realTimeTest {
        val c = client { respond("""{"model":"claude-haiku-5-5","content":[{"type":"text","text":"Hello "},{"type":"text","text":"there"}],"stop_reason":"end_turn","usage":{"input_tokens":5,"output_tokens":2}}""", HttpStatusCode.OK, json) }
        val r = AnthropicProvider(c, keys, configs(), { 0L }).complete(req)
        assertEquals("Hello there", r.text)
        assertEquals("https://api.anthropic.com/v1/messages", lastRequest!!.url.toString())
        assertEquals("test-key-123", lastRequest!!.headers["x-api-key"])
        assertEquals("2023-06-01", lastRequest!!.headers["anthropic-version"])
        val body = Json.parseToJsonElement(lastBody).jsonObject
        assertEquals("Be a tutor", body["system"]!!.jsonPrimitive.content)
        assertEquals(3, body["messages"]!!.jsonArray.size)
    }

    @Test fun geminiFormat() = realTimeTest {
        val c = client { respond("""{"candidates":[{"content":{"parts":[{"text":"Hola"}]},"finishReason":"STOP"}],"usageMetadata":{"promptTokenCount":3,"candidatesTokenCount":1}}""", HttpStatusCode.OK, json) }
        val r = GeminiProvider(c, keys, configs(), { 0L }).complete(req.copy(expectJson = true))
        assertEquals("Hola", r.text)
        assertTrue(lastRequest!!.url.toString().endsWith("/models/gemini-2.5-flash:generateContent"))
        assertEquals("test-key-123", lastRequest!!.headers["x-goog-api-key"])
        assertFalse("key=" in lastRequest!!.url.toString(), "API key must not be placed in the URL")
        val body = Json.parseToJsonElement(lastBody).jsonObject
        assertEquals("model", body["contents"]!!.jsonArray[1].jsonObject["role"]!!.jsonPrimitive.content)
        assertTrue("responseMimeType" in lastBody)
    }

    private val visionReq = ChatRequest("Transcribe", listOf(ChatMessage(ChatRole.USER, "Read this",
        images = listOf(com.polyglot.academy.core.ai.ImagePart("image/jpeg", "QUJD")))), emptySet())

    @Test fun imagesUseEachVendorsDocumentedShape() = realTimeTest {
        val oc = client { respond("""{"choices":[{"message":{"content":"text"}}]}""", HttpStatusCode.OK, json) }
        OpenAiCompatibleProvider(ProviderCatalog.OPENAI, oc, keys, configs(), { 0L }).complete(visionReq)
        val oParts = Json.parseToJsonElement(lastBody).jsonObject["messages"]!!.jsonArray[1].jsonObject["content"]!!.jsonArray
        assertEquals("text", oParts[0].jsonObject["type"]!!.jsonPrimitive.content)
        assertEquals("data:image/jpeg;base64,QUJD", oParts[1].jsonObject["image_url"]!!.jsonObject["url"]!!.jsonPrimitive.content)

        val ac = client { respond("""{"content":[{"type":"text","text":"text"}]}""", HttpStatusCode.OK, json) }
        AnthropicProvider(ac, keys, configs(), { 0L }).complete(visionReq)
        val aParts = Json.parseToJsonElement(lastBody).jsonObject["messages"]!!.jsonArray[0].jsonObject["content"]!!.jsonArray
        val source = aParts[0].jsonObject["source"]!!.jsonObject
        assertEquals("base64", source["type"]!!.jsonPrimitive.content)
        assertEquals("image/jpeg", source["media_type"]!!.jsonPrimitive.content)
        assertEquals("QUJD", source["data"]!!.jsonPrimitive.content)
        assertEquals("text", aParts[1].jsonObject["type"]!!.jsonPrimitive.content)

        val gc = client { respond("""{"candidates":[{"content":{"parts":[{"text":"text"}]}}]}""", HttpStatusCode.OK, json) }
        GeminiProvider(gc, keys, configs(), { 0L }).complete(visionReq)
        val gParts = Json.parseToJsonElement(lastBody).jsonObject["contents"]!!.jsonArray[0].jsonObject["parts"]!!.jsonArray
        assertEquals("QUJD", gParts[0].jsonObject["inlineData"]!!.jsonObject["data"]!!.jsonPrimitive.content)
        assertEquals("Read this", gParts[1].jsonObject["text"]!!.jsonPrimitive.content)

        // Text-only messages keep the plain string form.
        OpenAiCompatibleProvider(ProviderCatalog.OPENAI, oc, keys, configs(), { 0L }).complete(req)
        assertEquals("Hello", Json.parseToJsonElement(lastBody).jsonObject["messages"]!!.jsonArray[1].jsonObject["content"]!!.jsonPrimitive.content)
    }

    @Test fun azureUsesDeploymentAndApiKeyHeader() = realTimeTest {
        val c = client { respond("""{"choices":[{"message":{"content":"ok"}}]}""", HttpStatusCode.OK, json) }
        val p = AzureOpenAiProvider(c, keys, configs(ProviderConfig(ProviderId.AZURE_OPENAI, enabled = true, baseUrl = "https://res.openai.azure.com", deployment = "tutor")), { 0L })
        p.complete(req)
        assertEquals("https://res.openai.azure.com/openai/deployments/tutor/chat/completions?api-version=2024-10-21", lastRequest!!.url.toString())
        assertEquals("test-key-123", lastRequest!!.headers["api-key"])
        assertFalse("\"model\"" in lastBody)
    }

    @Test fun missingKeyIsNotConfigured() = realTimeTest {
        val c = client { respond("{}", HttpStatusCode.OK, json) }
        assertFailsWith<AiError.NotConfigured> { OpenAiCompatibleProvider(ProviderCatalog.OPENAI, c, noKeys, configs(), { 0L }).complete(req) }
    }

    @Test fun httpErrorsAreClassified() = realTimeTest {
        suspend fun errorFor(status: HttpStatusCode, body: String = "{}", headers: io.ktor.http.Headers = json): AiError {
            val c = client { respond(body, status, headers) }
            return try { OpenAiCompatibleProvider(ProviderCatalog.OPENAI, c, keys, configs(), { 0L }).complete(req); error("expected failure") } catch (e: AiError) { e }
        }
        assertTrue(errorFor(HttpStatusCode.Unauthorized) is AiError.Authentication)
        assertTrue(errorFor(HttpStatusCode.TooManyRequests, """{"error":{"code":"insufficient_quota"}}""") is AiError.QuotaExceeded)
        val rl = errorFor(HttpStatusCode.TooManyRequests, "{}", headersOf(HttpHeaders.RetryAfter, "2"))
        assertTrue(rl is AiError.RateLimited && rl.retryAfterMillis == 2000L)
        assertTrue(errorFor(HttpStatusCode.ServiceUnavailable).let { it is AiError.Server && it.retryable })
        assertTrue(errorFor(HttpStatusCode.NotFound) is AiError.InvalidResponse)
        assertTrue(errorFor(HttpStatusCode.OK, "not json") is AiError.InvalidResponse)
        assertTrue(errorFor(HttpStatusCode.OK, """{"choices":[]}""") is AiError.InvalidResponse)
        assertTrue(errorFor(HttpStatusCode.OK, """{"choices":[{"message":{"content":""},"finish_reason":"content_filter"}]}""") is AiError.ContentFiltered)
    }

    @Test fun errorsDoNotLeakKeys() = realTimeTest {
        val c = client { respond("{}", HttpStatusCode.Unauthorized, json) }
        val e = assertFailsWith<AiError> { OpenAiCompatibleProvider(ProviderCatalog.OPENAI, c, keys, configs(), { 0L }).complete(req) }
        assertFalse("test-key-123" in (e.message ?: ""))
    }

    @Test fun timeoutIsClassified() = realTimeTest {
        val c = client { delay(5_000); respond("{}", HttpStatusCode.OK, json) }
        assertFailsWith<AiError.Timeout> {
            OpenAiCompatibleProvider(ProviderCatalog.OPENAI, c, keys, configs(), { 0L }).complete(req.copy(timeoutMillis = 100))
        }
    }

    @Test fun connectionTestReportsFailureWithoutThrowing() = realTimeTest {
        val c = client { respond("{}", HttpStatusCode.Unauthorized, json) }
        val r = AnthropicProvider(c, keys, configs(), { 0L }).testConnection()
        assertFalse(r.ok)
        assertTrue(r.error is AiError.Authentication)
    }
}
