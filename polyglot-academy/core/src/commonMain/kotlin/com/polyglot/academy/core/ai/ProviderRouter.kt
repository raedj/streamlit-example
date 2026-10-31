package com.polyglot.academy.core.ai

import com.polyglot.academy.core.model.DataCategory
import com.polyglot.academy.core.model.PrivacySettings
import com.polyglot.academy.core.model.ProviderLocation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

enum class NetworkState { ONLINE, OFFLINE }

data class RoutingContext(
    val privacy: PrivacySettings,
    val network: NetworkState,
    val preferred: ProviderId?,
    val configs: Map<ProviderId, ProviderConfig>,
    /** Providers that have a stored credential (or need none). */
    val credentialPresent: Set<ProviderId>,
    /** Providers whose runtime is available on this device (ON_DEVICE only). */
    val onDeviceReady: Boolean,
    val perItemCloudConsent: Boolean = false,
)

enum class SkipReason { DISABLED, NOT_CONFIGURED, MISSING_CAPABILITY, PRIVACY, OFFLINE, RUNTIME_UNAVAILABLE }

data class Candidate(val provider: ProviderId, val skip: SkipReason?, val privacyDenial: PrivacyDecision.Denied? = null)

sealed interface RouteDecision {
    /** [ordered] are attempted in order; the first is the primary. */
    data class Route(val ordered: List<ProviderId>, val preferredSkipped: Candidate?) : RouteDecision
    data class Blocked(val candidates: List<Candidate>, val privacyBlocked: Boolean) : RouteDecision
}

/**
 * Chooses which provider(s) may serve a request.
 *  1. Only enabled, configured providers with the capability are considered.
 *  2. Every candidate is checked against [PrivacyPolicy]; denied providers are never used.
 *  3. Offline: only on-device providers.
 *  4. The preferred provider goes first. Other CLOUD providers are added as fallbacks only if
 *     [PrivacySettings.allowCloudFallback] is on; local/on-device fallbacks are always allowed
 *     because data stays under the learner's control.
 */
object ProviderRouter {
    fun route(capability: AiCapability, categories: Set<DataCategory>, ctx: RoutingContext): RouteDecision {
        val candidates = ProviderCatalog.all.map { d -> evaluate(d, capability, categories, ctx) }
        val usable = candidates.filter { it.skip == null }.map { it.provider }
        val preferredCandidate = ctx.preferred?.let { p -> candidates.first { it.provider == p } }
        if (usable.isEmpty()) {
            return RouteDecision.Blocked(candidates, privacyBlocked = candidates.any { it.skip == SkipReason.PRIVACY })
        }
        val loc = { id: ProviderId -> ProviderCatalog.of(id).location }
        val ordered = buildList {
            val preferredUsable = ctx.preferred?.takeIf { it in usable }
            if (preferredUsable != null) add(preferredUsable)
            // Cloud fallbacks only when explicitly allowed, or when no preference was given at all.
            val allowOtherCloud = ctx.privacy.allowCloudFallback || ctx.preferred == null
            usable.filter { it != preferredUsable }
                .sortedBy { when (loc(it)) { ProviderLocation.ON_DEVICE -> 1; ProviderLocation.LOCAL_NETWORK -> 0; ProviderLocation.CLOUD -> 2 } }
                .forEach { id ->
                    if (loc(id) != ProviderLocation.CLOUD || allowOtherCloud) add(id)
                }
        }
        if (ordered.isEmpty()) return RouteDecision.Blocked(candidates, privacyBlocked = false)
        val skipped = preferredCandidate?.takeIf { it.skip != null }
        return RouteDecision.Route(ordered, skipped)
    }

    private fun evaluate(d: ProviderDescriptor, capability: AiCapability, categories: Set<DataCategory>, ctx: RoutingContext): Candidate {
        val cfg = ctx.configs[d.id]
        if (d.id == ProviderId.ON_DEVICE) {
            if (!ctx.onDeviceReady) return Candidate(d.id, SkipReason.RUNTIME_UNAVAILABLE)
        } else {
            if (cfg == null || !cfg.enabled) return Candidate(d.id, SkipReason.DISABLED)
            if (d.requiresApiKey && d.id !in ctx.credentialPresent) return Candidate(d.id, SkipReason.NOT_CONFIGURED)
            if (d.requiresBaseUrl && cfg.baseUrl.isNullOrBlank() && d.defaultBaseUrl == null) return Candidate(d.id, SkipReason.NOT_CONFIGURED)
            if (d.id == ProviderId.AZURE_OPENAI && cfg.deployment.isNullOrBlank()) return Candidate(d.id, SkipReason.NOT_CONFIGURED)
            if (d.defaultModel.isBlank() && cfg.model.isNullOrBlank()) return Candidate(d.id, SkipReason.NOT_CONFIGURED)
        }
        if (capability !in d.capabilities) return Candidate(d.id, SkipReason.MISSING_CAPABILITY)
        if (ctx.network == NetworkState.OFFLINE && d.location != ProviderLocation.ON_DEVICE) return Candidate(d.id, SkipReason.OFFLINE)
        val decision = PrivacyPolicy.evaluate(ctx.privacy, d.location, categories, ctx.perItemCloudConsent)
        if (decision is PrivacyDecision.Denied) return Candidate(d.id, SkipReason.PRIVACY, decision)
        return Candidate(d.id, null)
    }
}

data class RetryPolicy(val maxRetries: Int = 2, val baseDelayMillis: Long = 800, val maxDelayMillis: Long = 8_000) {
    fun delayFor(attempt: Int, error: AiError): Long {
        val hinted = (error as? AiError.RateLimited)?.retryAfterMillis
        val exp = (baseDelayMillis shl attempt).coerceAtMost(maxDelayMillis)
        return (hinted ?: exp).coerceAtMost(maxDelayMillis)
    }
}

sealed interface AiOutcome {
    data class Success(val response: ChatResponse, val usedFallback: Boolean, val failedProviders: List<Pair<ProviderId, AiError>>) : AiOutcome
    data class Failure(val error: AiError, val attempts: List<Pair<ProviderId, AiError>>) : AiOutcome
}

/**
 * Executes a request through the routed providers with bounded retries per provider and
 * policy-respecting fallback. Never routes to a provider the router did not approve.
 */
class AiService(
    private val providers: Map<ProviderId, AiProvider>,
    private val retry: RetryPolicy = RetryPolicy(),
    private val sleeper: suspend (Long) -> Unit = { delay(it) },
) {
    suspend fun execute(capability: AiCapability, request: ChatRequest, ctx: RoutingContext): AiOutcome {
        val decision = ProviderRouter.route(capability, request.dataCategories, ctx)
        if (decision is RouteDecision.Blocked) {
            val privacy = decision.candidates.firstNotNullOfOrNull { it.privacyDenial }
            val err = if (decision.privacyBlocked && privacy != null) AiError.BlockedByPrivacy(null, privacy.blockedCategories)
            else AiError.NoProviderAvailable("No enabled AI provider can handle this request")
            return AiOutcome.Failure(err, emptyList())
        }
        decision as RouteDecision.Route
        val failures = mutableListOf<Pair<ProviderId, AiError>>()
        for (id in decision.ordered) {
            val provider = providers[id] ?: continue
            var attempt = 0
            while (true) {
                try {
                    val r = provider.complete(request)
                    return AiOutcome.Success(r, usedFallback = id != decision.ordered.first() || decision.preferredSkipped != null, failedProviders = failures)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: AiError) {
                    if (e.retryable && attempt < retry.maxRetries) {
                        sleeper(retry.delayFor(attempt, e)); attempt++; continue
                    }
                    failures += id to e
                    break
                }
            }
        }
        return AiOutcome.Failure(failures.last().second, failures)
    }
}
