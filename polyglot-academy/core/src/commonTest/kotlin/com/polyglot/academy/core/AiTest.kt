package com.polyglot.academy.core

import com.polyglot.academy.core.ai.AiCapability
import com.polyglot.academy.core.ai.AiError
import com.polyglot.academy.core.ai.AiOutcome
import com.polyglot.academy.core.ai.AiProvider
import com.polyglot.academy.core.ai.AiService
import com.polyglot.academy.core.ai.ChatMessage
import com.polyglot.academy.core.ai.ChatRequest
import com.polyglot.academy.core.ai.ChatResponse
import com.polyglot.academy.core.ai.ChatRole
import com.polyglot.academy.core.ai.ConnectionTestResult
import com.polyglot.academy.core.ai.NetworkState
import com.polyglot.academy.core.ai.PrivacyDecision
import com.polyglot.academy.core.ai.PrivacyPolicy
import com.polyglot.academy.core.ai.ProviderCatalog
import com.polyglot.academy.core.ai.ProviderConfig
import com.polyglot.academy.core.ai.ProviderId
import com.polyglot.academy.core.ai.ProviderRouter
import com.polyglot.academy.core.ai.RetryPolicy
import com.polyglot.academy.core.ai.RouteDecision
import com.polyglot.academy.core.ai.RoutingContext
import com.polyglot.academy.core.ai.SecretRedactor
import com.polyglot.academy.core.ai.SkipReason
import com.polyglot.academy.core.model.DataCategory
import com.polyglot.academy.core.model.PrivacyConsent
import com.polyglot.academy.core.model.PrivacySettings
import com.polyglot.academy.core.model.ProviderLocation
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AiTest {
    private val cloudOn = PrivacySettings(cloudAiEnabled = true, consentVersionAccepted = PrivacyConsent.CURRENT_VERSION)

    @Test fun cloudDisabledByDefault() {
        val d = PrivacyPolicy.evaluate(PrivacySettings(), ProviderLocation.CLOUD, setOf(DataCategory.CONVERSATION_TEXT))
        assertIs<PrivacyDecision.Denied>(d)
    }

    @Test fun sensitiveCategoriesBlockedForCloudEvenWhenEnabled() {
        val d = PrivacyPolicy.evaluate(cloudOn, ProviderLocation.CLOUD, setOf(DataCategory.CONVERSATION_TEXT, DataCategory.PERSONAL_NOTES))
        assertIs<PrivacyDecision.Denied>(d)
        assertEquals(setOf(DataCategory.PERSONAL_NOTES), d.blockedCategories)
    }

    @Test fun perItemConsentAllowsImportedMaterial() {
        assertIs<PrivacyDecision.Denied>(PrivacyPolicy.evaluate(cloudOn, ProviderLocation.CLOUD, setOf(DataCategory.IMPORTED_MATERIAL)))
        assertEquals(PrivacyDecision.Allowed, PrivacyPolicy.evaluate(cloudOn, ProviderLocation.CLOUD, setOf(DataCategory.IMPORTED_MATERIAL), perItemCloudConsent = true))
    }

    @Test fun outdatedConsentBlocksCloud() {
        val d = PrivacyPolicy.evaluate(cloudOn.copy(consentVersionAccepted = 0), ProviderLocation.CLOUD, emptySet())
        assertIs<PrivacyDecision.Denied>(d)
    }

    @Test fun onDeviceAlwaysAllowedLocalNetworkConfigurable() {
        assertEquals(PrivacyDecision.Allowed, PrivacyPolicy.evaluate(PrivacySettings(), ProviderLocation.ON_DEVICE, setOf(DataCategory.VOICE_AUDIO)))
        assertIs<PrivacyDecision.Denied>(PrivacyPolicy.evaluate(PrivacySettings(allowLocalNetworkAi = false), ProviderLocation.LOCAL_NETWORK, emptySet()))
    }

    private fun ctx(
        privacy: PrivacySettings = cloudOn,
        preferred: ProviderId? = ProviderId.OPENAI,
        enabled: Set<ProviderId> = setOf(ProviderId.OPENAI, ProviderId.ANTHROPIC),
        network: NetworkState = NetworkState.ONLINE,
    ) = RoutingContext(
        privacy = privacy, network = network, preferred = preferred,
        configs = ProviderId.entries.associateWith { ProviderConfig(it, enabled = it in enabled) },
        credentialPresent = enabled, onDeviceReady = false,
    )

    @Test fun routerUsesPreferredFirstAndNoSilentCloudFallback() {
        val d = ProviderRouter.route(AiCapability.CHAT, setOf(DataCategory.CONVERSATION_TEXT), ctx())
        assertIs<RouteDecision.Route>(d)
        assertEquals(listOf(ProviderId.OPENAI), d.ordered)
    }

    @Test fun routerAddsCloudFallbackOnlyWhenAllowed() {
        val d = ProviderRouter.route(AiCapability.CHAT, emptySet(), ctx(privacy = cloudOn.copy(allowCloudFallback = true)))
        assertIs<RouteDecision.Route>(d)
        assertEquals(listOf(ProviderId.OPENAI, ProviderId.ANTHROPIC), d.ordered)
    }

    @Test fun routerAlwaysAllowsLocalFallback() {
        val d = ProviderRouter.route(AiCapability.CHAT, emptySet(), ctx(enabled = setOf(ProviderId.OPENAI, ProviderId.LOCAL_SERVER)))
        assertIs<RouteDecision.Route>(d)
        assertEquals(listOf(ProviderId.OPENAI, ProviderId.LOCAL_SERVER), d.ordered)
    }

    @Test fun routerBlocksWhenPrivacyForbidsAndReportsIt() {
        val d = ProviderRouter.route(AiCapability.CHAT, setOf(DataCategory.PERSONAL_NOTES), ctx())
        assertIs<RouteDecision.Blocked>(d)
        assertTrue(d.privacyBlocked)
        assertEquals(SkipReason.PRIVACY, d.candidates.first { it.provider == ProviderId.OPENAI }.skip)
    }

    @Test fun preferredBlockedByPrivacyDoesNotSilentlyGoToAnotherCloud() {
        // Local-network allowed, cloud disabled: only the local server may be used.
        val d = ProviderRouter.route(AiCapability.CHAT, emptySet(),
            ctx(privacy = PrivacySettings(), enabled = setOf(ProviderId.OPENAI, ProviderId.ANTHROPIC, ProviderId.LOCAL_SERVER)))
        assertIs<RouteDecision.Route>(d)
        assertEquals(listOf(ProviderId.LOCAL_SERVER), d.ordered)
        assertEquals(SkipReason.PRIVACY, d.preferredSkipped?.skip)
    }

    @Test fun offlineExcludesNetworkProviders() {
        val d = ProviderRouter.route(AiCapability.CHAT, emptySet(), ctx(network = NetworkState.OFFLINE, enabled = setOf(ProviderId.OPENAI, ProviderId.LOCAL_SERVER)))
        assertIs<RouteDecision.Blocked>(d)
    }

    @Test fun missingCapabilityExcluded() {
        val d = ProviderRouter.route(AiCapability.VISION, emptySet(), ctx(preferred = ProviderId.DEEPSEEK, enabled = setOf(ProviderId.DEEPSEEK)))
        assertIs<RouteDecision.Blocked>(d)
        assertEquals(SkipReason.MISSING_CAPABILITY, d.candidates.first { it.provider == ProviderId.DEEPSEEK }.skip)
    }

    @Test fun azureNeedsDeployment() {
        val c = ctx(preferred = ProviderId.AZURE_OPENAI, enabled = setOf(ProviderId.AZURE_OPENAI))
        val d = ProviderRouter.route(AiCapability.CHAT, emptySet(), c.copy(configs = c.configs + (ProviderId.AZURE_OPENAI to ProviderConfig(ProviderId.AZURE_OPENAI, enabled = true, baseUrl = "https://x.openai.azure.com"))))
        assertIs<RouteDecision.Blocked>(d)
    }

    private class FakeProvider(override val descriptor: com.polyglot.academy.core.ai.ProviderDescriptor, val failures: MutableList<AiError>) : AiProvider {
        var calls = 0
        override suspend fun complete(request: ChatRequest): ChatResponse {
            calls++
            if (failures.isNotEmpty()) throw failures.removeAt(0)
            return ChatResponse("ok from ${descriptor.id}", descriptor.id, "m")
        }
        override suspend fun testConnection() = ConnectionTestResult(true, 1, null, "m")
    }

    private val req = ChatRequest("sys", listOf(ChatMessage(ChatRole.USER, "hi")), setOf(DataCategory.CONVERSATION_TEXT))

    @Test fun serviceRetriesRetryableErrorsThenSucceeds() = runTest {
        val p = FakeProvider(ProviderCatalog.OPENAI, mutableListOf(AiError.Timeout(ProviderId.OPENAI), AiError.Server(ProviderId.OPENAI, 503)))
        val delays = mutableListOf<Long>()
        val svc = AiService(mapOf(ProviderId.OPENAI to p), RetryPolicy(maxRetries = 2), sleeper = { delays += it })
        val out = svc.execute(AiCapability.CHAT, req, ctx())
        assertIs<AiOutcome.Success>(out)
        assertEquals(3, p.calls)
        assertEquals(listOf(800L, 1600L), delays)
    }

    @Test fun serviceDoesNotRetryAuthErrorsAndFallsBackOnlyWhenAllowed() = runTest {
        val openai = FakeProvider(ProviderCatalog.OPENAI, mutableListOf(AiError.Authentication(ProviderId.OPENAI)))
        val claude = FakeProvider(ProviderCatalog.ANTHROPIC, mutableListOf())
        val providers = mapOf(ProviderId.OPENAI to openai, ProviderId.ANTHROPIC to claude)
        val noFallback = AiService(providers, sleeper = {}).execute(AiCapability.CHAT, req, ctx())
        assertIs<AiOutcome.Failure>(noFallback)
        assertIs<AiError.Authentication>(noFallback.error)
        assertEquals(0, claude.calls)
        assertEquals(1, openai.calls)

        openai.failures += AiError.Authentication(ProviderId.OPENAI)
        val withFallback = AiService(providers, sleeper = {}).execute(AiCapability.CHAT, req, ctx(privacy = cloudOn.copy(allowCloudFallback = true)))
        assertIs<AiOutcome.Success>(withFallback)
        assertTrue(withFallback.usedFallback)
        assertEquals(ProviderId.ANTHROPIC, withFallback.response.providerId)
    }

    @Test fun serviceReportsPrivacyBlock() = runTest {
        val out = AiService(emptyMap()).execute(AiCapability.CHAT, req.copy(dataCategories = setOf(DataCategory.VOICE_AUDIO)), ctx())
        assertIs<AiOutcome.Failure>(out)
        assertIs<AiError.BlockedByPrivacy>(out.error)
    }

    @Test fun rateLimitHonoursRetryAfterBounded() {
        val r = RetryPolicy()
        assertEquals(3_000, r.delayFor(0, AiError.RateLimited(ProviderId.OPENAI, 3_000)))
        assertEquals(8_000, r.delayFor(0, AiError.RateLimited(ProviderId.OPENAI, 120_000)))
        assertFalse(AiError.QuotaExceeded(ProviderId.OPENAI).retryable)
    }

    @Test fun redactorRemovesKeys() {
        val s = SecretRedactor.redact("calling with sk-abcdefghijklmnop and AIzaSyA1234567890123456789012 header x-api-key: abcdefgh12345")
        assertFalse("sk-abcdefghijklmnop" in s)
        assertFalse("AIzaSyA" in s)
        assertFalse("abcdefgh12345" in s)
    }
}
