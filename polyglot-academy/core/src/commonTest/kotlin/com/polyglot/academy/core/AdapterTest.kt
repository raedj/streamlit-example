package com.polyglot.academy.core

import com.polyglot.academy.core.ai.AiError
import com.polyglot.academy.core.ai.AnthropicProvider
import com.polyglot.academy.core.ai.ApiKeyText
import com.polyglot.academy.core.ai.AzureOpenAiProvider
import com.polyglot.academy.core.ai.ChatMessage
import com.polyglot.academy.core.ai.ChatRequest
import com.polyglot.academy.core.ai.ChatRole
import com.polyglot.academy.core.ai.ConfigSource
import com.polyglot.academy.core.ai.CredentialSource
import com.polyglot.academy.core.ai.GeminiProvider
import com.polyglot.academy.core.ai.ModelChoice
import com.polyglot.academy.core.ai.OpenAiCompatibleProvider
import com.polyglot.academy.core.ai.ProviderCatalog
import com.polyglot.academy.core.ai.ProviderConfig
import com.polyglot.academy.core.ai.ProviderId
import com.polyglot.academy.core.ai.ProviderUrl
import com.polyglot.academy.core.ai.ReasoningModels
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

    @Test fun localServerAddressIsCompletedAndOllamaModelsAreListed() = realTimeTest {
        assertEquals("http://192.168.1.5:11434/v1", com.polyglot.academy.core.ai.LocalServerUrl.normalize(" http://192.168.1.5:11434/ "))
        assertEquals("http://pc:11434/v1", com.polyglot.academy.core.ai.LocalServerUrl.normalize("http://pc:11434/v1/chat/completions"))
        assertEquals("http://pc:1234/v1", com.polyglot.academy.core.ai.LocalServerUrl.normalize("http://pc:1234/v1/models"))
        assertEquals("http://pc:8080/api/v1", com.polyglot.academy.core.ai.LocalServerUrl.normalize("http://pc:8080/api/v1"), "a path the learner chose is kept")
        // An Ollama-like server: the OpenAI API under /v1, nothing at /models.
        val seen = mutableListOf<String>()
        val ollama = client { r ->
            seen += r.url.encodedPath
            when (r.url.encodedPath) {
                "/v1/models" -> respond("""{"object":"list","data":[{"id":"qwen2.5:1.5b"},{"id":"gemma3:1b"}]}""", HttpStatusCode.OK, json)
                "/v1/chat/completions" -> respond("""{"model":"qwen2.5:1.5b","choices":[{"message":{"content":"ok"},"finish_reason":"stop"}]}""", HttpStatusCode.OK, json)
                else -> respond("404 page not found", HttpStatusCode.NotFound)
            }
        }
        val typed = configs(ProviderConfig(ProviderId.LOCAL_SERVER, enabled = true, baseUrl = "http://127.0.0.1:11434", model = "qwen2.5:1.5b"))
        val local = OpenAiCompatibleProvider(ProviderCatalog.LOCAL_SERVER, ollama, noKeys, typed, { 0L })
        assertEquals(listOf("gemma3:1b", "qwen2.5:1.5b"), local.listModels())
        assertEquals("ok", local.complete(req).text)
        assertEquals(listOf("/v1/models", "/v1/chat/completions"), seen)
        // An older Ollama without the OpenAI model list: its own list is used.
        val old = client { r ->
            if (r.url.encodedPath == "/api/tags") respond("""{"models":[{"name":"llama3.2:latest"},{"name":"qwen2.5:0.5b"}]}""", HttpStatusCode.OK, json)
            else respond("404 page not found", HttpStatusCode.NotFound)
        }
        assertEquals(listOf("llama3.2:latest", "qwen2.5:0.5b"), OpenAiCompatibleProvider(ProviderCatalog.LOCAL_SERVER, old, noKeys, typed, { 0L }).listModels())
        // Nothing there at all: the 404 is reported, not hidden.
        val nothing = client { respond("not found", HttpStatusCode.NotFound) }
        assertFailsWith<AiError.InvalidResponse> { OpenAiCompatibleProvider(ProviderCatalog.LOCAL_SERVER, nothing, noKeys, typed, { 0L }).listModels() }
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

    @Test fun geminiKeyAndRegionErrorsAreNamedAndThinkingLeavesRoomForTheAnswer() = realTimeTest {
        // A wrong Gemini key is HTTP 400, not 401.
        val badKey = client { respond("""{"error":{"code":400,"message":"API key not valid. Please pass a valid API key.","status":"INVALID_ARGUMENT",
            "details":[{"@type":"type.googleapis.com/google.rpc.ErrorInfo","reason":"API_KEY_INVALID"}]}}""", HttpStatusCode.BadRequest, json) }
        assertTrue(GeminiProvider(badKey, keys, configs(), { 0L }).testConnection().error is AiError.Authentication)
        assertFailsWith<AiError.Authentication> { GeminiProvider(badKey, keys, configs(), { 0L }).listModels() }
        val region = client { respond("""{"error":{"code":400,"message":"User location is not supported for the API use.","status":"FAILED_PRECONDITION"}}""", HttpStatusCode.BadRequest, json) }
        assertTrue(GeminiProvider(region, keys, configs(), { 0L }).testConnection().error is AiError.RegionNotSupported)
        // Any other 400 keeps the provider's own words, without anything key-like.
        val other = client { respond("""{"error":{"message":"Bad field for key AIzaSyA1234567890abcdefghijkl"}}""", HttpStatusCode.BadRequest, json) }
        val e = GeminiProvider(other, keys, configs(), { 0L }).testConnection().error!!
        assertTrue("Bad field" in e.message!! && "AIza" !in e.message!!, e.message)

        // Gemini 2.5 Flash: thinking off, so the connection test's 16 tokens are all for the answer.
        val ok = client { respond("""{"candidates":[{"content":{"parts":[{"text":"ok"}]},"finishReason":"STOP"}]}""", HttpStatusCode.OK, json) }
        assertTrue(GeminiProvider(ok, keys, configs(), { 0L }).testConnection().ok)
        val cfg = Json.parseToJsonElement(lastBody).jsonObject["generationConfig"]!!.jsonObject
        assertEquals("0", cfg["thinkingConfig"]!!.jsonObject["thinkingBudget"]!!.jsonPrimitive.content)
        assertEquals("16", cfg["maxOutputTokens"]!!.jsonPrimitive.content)
        // Pro cannot turn thinking off: it gets extra room; a model without thinking gets no setting.
        GeminiProvider(ok, keys, configs(ProviderConfig(ProviderId.GEMINI, enabled = true, model = "gemini-2.5-pro")), { 0L }).testConnection()
        assertTrue(Json.parseToJsonElement(lastBody).jsonObject["generationConfig"]!!.jsonObject["maxOutputTokens"]!!.jsonPrimitive.content.toInt() > 256)
        GeminiProvider(ok, keys, configs(ProviderConfig(ProviderId.GEMINI, enabled = true, model = "gemma-3-27b-it")), { 0L }).testConnection()
        assertFalse("thinkingConfig" in lastBody)
        // An answer cut off before any text says so.
        val cut = client { respond("""{"candidates":[{"content":{"role":"model"},"finishReason":"MAX_TOKENS"}]}""", HttpStatusCode.OK, json) }
        assertTrue("MAX_TOKENS" in GeminiProvider(cut, keys, configs(), { 0L }).testConnection().error!!.message!!)
    }

    @Test fun aModelTheAccountHasIsChosenAutomatically() {
        val listed = listOf("gemini-2.0-flash", "gemini-2.5-flash-lite", "gemini-2.5-pro", "gemini-3-flash", "gemini-3-flash-preview-tts")
        assertEquals("gemini-3-flash", ModelChoice.replacementFor(null, "gemini-2.5-flash", listed))
        assertEquals(null, ModelChoice.replacementFor("gemini-2.5-pro", "gemini-2.5-flash", listed), "a listed choice is kept")
        assertEquals(null, ModelChoice.replacementFor(null, "gemini-2.5-flash", listOf("gemini-2.5-flash", "gemini-3-flash")), "the default is kept when listed")
        assertEquals("gpt-4.1-mini", ModelChoice.replacementFor("gpt-old", "gpt-x", listOf("gpt-4.1", "gpt-4.1-mini", "gpt-4o-mini-tts")))
        assertEquals(null, ModelChoice.replacementFor("x", "y", emptyList()))
    }

    @Test fun pastedKeysLoseInvisibleCharactersAndOddOnesAreReportedAsTheKey() = realTimeTest {
        assertEquals("AIzaSyABC-123_x", ApiKeyText.clean("\u200F AIzaSy\u200BABC-123_x\n"))
        val ok = client { respond("""{"models":[]}""", HttpStatusCode.OK, json) }
        GeminiProvider(ok, CredentialSource { "\u202Bgood-key\u202C " }, configs(), { 0L }).listModels()
        assertEquals("good-key", lastRequest!!.headers["x-goog-api-key"])
        // A letter outside ASCII cannot go into a header: the key is named, not the network.
        assertFailsWith<AiError.Authentication> { GeminiProvider(ok, CredentialSource { "مفتاح" }, configs(), { 0L }).listModels() }
    }

    @Test fun reasoningModelsGetRoomToThinkBeforeTheAnswer() = realTimeTest {
        val ok = client { respond("""{"choices":[{"message":{"content":"ok"},"finish_reason":"stop"}]}""", HttpStatusCode.OK, json) }
        fun limit(field: String) = Json.parseToJsonElement(lastBody).jsonObject[field]!!.jsonPrimitive.content.toInt()
        fun cfg(id: ProviderId, m: String) = configs(ProviderConfig(id, enabled = true, model = m))
        assertTrue(OpenAiCompatibleProvider(ProviderCatalog.OPENAI, ok, keys, cfg(ProviderId.OPENAI, "o4-mini"), { 0L }).testConnection().ok)
        assertEquals(16 + ReasoningModels.EXTRA_TOKENS, limit("max_completion_tokens"))
        OpenAiCompatibleProvider(ProviderCatalog.OPENAI, ok, keys, cfg(ProviderId.OPENAI, "gpt-4.1-mini"), { 0L }).testConnection()
        assertEquals(16, limit("max_completion_tokens"), "a model without reasoning keeps the asked limit")
        OpenAiCompatibleProvider(ProviderCatalog.DEEPSEEK, ok, keys, cfg(ProviderId.DEEPSEEK, "deepseek-reasoner"), { 0L }).testConnection()
        assertEquals(16 + ReasoningModels.EXTRA_TOKENS, limit("max_tokens"))
        OpenAiCompatibleProvider(ProviderCatalog.XAI, ok, keys, cfg(ProviderId.XAI, "grok-3-mini"), { 0L }).testConnection()
        assertEquals(16 + ReasoningModels.EXTRA_TOKENS, limit("max_tokens"))
        assertTrue(ReasoningModels.isReasoning("openai/o4-mini") && ReasoningModels.isReasoning("deepseek/deepseek-r1") && ReasoningModels.isReasoning("qwen3-32b"))
        assertFalse(ReasoningModels.isReasoning("deepseek-chat") || ReasoningModels.isReasoning("openai/gpt-4o-mini") || ReasoningModels.isReasoning("mistral-small-latest"))
        // Still cut off: said so, not "missing content".
        val cut = client { respond("""{"choices":[{"message":{"content":""},"finish_reason":"length"}]}""", HttpStatusCode.OK, json) }
        val e = OpenAiCompatibleProvider(ProviderCatalog.OPENAI, cut, keys, cfg(ProviderId.OPENAI, "o3"), { 0L }).testConnection().error!!
        assertTrue("cut off" in e.message!!, e.message)
    }

    @Test fun networkFailuresKeepTheirKindAndBadAddressesAreNeverUsed() = realTimeTest {
        class UnknownHostException : Exception("generativelanguage.googleapis.com")
        val wrapped = IllegalStateException("engine failed", UnknownHostException())
        assertEquals("UnknownHostException", com.polyglot.academy.core.ai.networkFailureKind(wrapped))
        assertEquals("IllegalStateException", com.polyglot.academy.core.ai.networkFailureKind(IllegalStateException("x")))
        val down = client { throw UnknownHostException() }
        val e = GeminiProvider(down, keys, configs(), { 0L }).testConnection().error
        assertTrue(e is AiError.Network && e.detail == "UnknownHostException", e.toString())

        assertTrue(ProviderUrl.isWebAddress("https://generativelanguage.googleapis.com/v1beta") && ProviderUrl.isWebAddress("http://10.0.2.2:8788/v1"))
        assertFalse(ProviderUrl.isWebAddress("AIzaSyA-not-an-address") || ProviderUrl.isWebAddress("generativelanguage.googleapis.com") || ProviderUrl.isWebAddress("https://a b"))
        assertEquals("generativelanguage.googleapis.com", ProviderUrl.host("https://generativelanguage.googleapis.com/v1beta"))
        // A key saved in the address field by mistake: the provider's own address is used, the key is not sent as a URL.
        val ok = client { respond("""{"models":[]}""", HttpStatusCode.OK, json) }
        GeminiProvider(ok, keys, configs(ProviderConfig(ProviderId.GEMINI, enabled = true, baseUrl = "AIzaSyB-pasted-here")), { 0L }).listModels()
        assertTrue(lastRequest!!.url.toString().startsWith("https://generativelanguage.googleapis.com/v1beta/models"), lastRequest!!.url.toString())

        assertEquals("AIza", ApiKeyText.unexpectedFormat(ProviderId.GEMINI, "123456789012"), "a project number is not a key")
        assertEquals(null, ApiKeyText.unexpectedFormat(ProviderId.GEMINI, " AIzaSyA1234567890 "))
        assertEquals("sk-ant-", ApiKeyText.unexpectedFormat(ProviderId.ANTHROPIC, "sk-proj-abc"))
        assertEquals(null, ApiKeyText.unexpectedFormat(ProviderId.MISTRAL, "anything"), "no known format, no hint")
    }
}
