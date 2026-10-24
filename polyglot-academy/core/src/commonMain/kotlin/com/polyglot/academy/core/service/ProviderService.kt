package com.polyglot.academy.core.service

import com.polyglot.academy.core.ai.AiCapability
import com.polyglot.academy.core.ai.AiOutcome
import com.polyglot.academy.core.ai.AiProvider
import com.polyglot.academy.core.ai.AiService
import com.polyglot.academy.core.ai.ChatRequest
import com.polyglot.academy.core.ai.ConnectionStatus
import com.polyglot.academy.core.ai.ConnectionTestResult
import com.polyglot.academy.core.ai.CredentialSource
import com.polyglot.academy.core.ai.NetworkState
import com.polyglot.academy.core.ai.ProviderCatalog
import com.polyglot.academy.core.ai.ProviderConfig
import com.polyglot.academy.core.ai.ProviderId
import com.polyglot.academy.core.ai.RouteDecision
import com.polyglot.academy.core.ai.ProviderRouter
import com.polyglot.academy.core.ai.RoutingContext
import com.polyglot.academy.core.ai.SecretRedactor
import com.polyglot.academy.core.model.DataCategory
import com.polyglot.academy.core.repo.ProfileRepository
import com.polyglot.academy.core.repo.ProviderConfigRepository
import com.polyglot.academy.core.repo.SecretStore
import com.polyglot.academy.core.util.TimeSource
import kotlinx.coroutines.flow.first

fun secretKeyFor(id: ProviderId) = "provider_api_key_${id.name.lowercase()}"

/** Bridges stored configuration, secrets, connectivity and the AI service. */
class ProviderService(
    private val configs: ProviderConfigRepository,
    private val secrets: SecretStore,
    private val profiles: ProfileRepository,
    private val network: () -> NetworkState,
    private val providerFactory: (CredentialSource, (ProviderId) -> ProviderConfig) -> List<AiProvider>,
    private val clock: TimeSource,
    private val onDeviceReady: () -> Boolean = { false },
) {
    @kotlin.concurrent.Volatile private var cachedConfigs: Map<ProviderId, ProviderConfig> = emptyMap()
    val credentials = CredentialSource { id -> secrets.get(secretKeyFor(id)) }
    private val providers: Map<ProviderId, AiProvider> by lazy {
        providerFactory(credentials) { id -> cachedConfigs[id] ?: ProviderConfig(id) }.associateBy { it.descriptor.id }
    }
    private val service by lazy { AiService(providers) }

    suspend fun hasKey(id: ProviderId) = !secrets.get(secretKeyFor(id)).isNullOrBlank()

    suspend fun saveKey(id: ProviderId, key: String) {
        if (key.isBlank()) secrets.remove(secretKeyFor(id)) else secrets.put(secretKeyFor(id), key.trim())
        // A new key invalidates the previous verification.
        val cfg = configs.current().getValue(id)
        configs.save(cfg.copy(lastTest = ConnectionStatus.NEVER_TESTED, lastErrorMessage = null))
    }

    suspend fun saveConfig(config: ProviderConfig) = configs.save(config)

    suspend fun routingContext(perItemCloudConsent: Boolean = false): RoutingContext {
        val cfg = configs.current()
        cachedConfigs = cfg
        val present = ProviderCatalog.all.filter { d -> !d.requiresApiKey || hasKey(d.id) }.map { it.id }.toSet()
        val preferred = configs.preferredProvider.first()
        return RoutingContext(profiles.currentSettings().privacy, network(), preferred, cfg, present, onDeviceReady(), perItemCloudConsent)
    }

    suspend fun route(capability: AiCapability, categories: Set<DataCategory>): RouteDecision =
        ProviderRouter.route(capability, categories, routingContext())

    suspend fun execute(capability: AiCapability, request: ChatRequest, perItemCloudConsent: Boolean = false): AiOutcome {
        val outcome = service.execute(capability, request, routingContext(perItemCloudConsent))
        if (outcome is AiOutcome.Success) {
            val c = configs.current().getValue(outcome.response.providerId)
            configs.save(c.copy(requestCount = c.requestCount + 1,
                approxTokensUsed = c.approxTokensUsed + (outcome.response.inputTokens ?: 0) + (outcome.response.outputTokens ?: 0)))
        }
        return outcome
    }

    /** Runs a real request; the provider is only marked VERIFIED when it actually answered. */
    suspend fun testConnection(id: ProviderId): ConnectionTestResult {
        cachedConfigs = configs.current()
        val provider = providers[id] ?: return ConnectionTestResult(false, null, com.polyglot.academy.core.ai.AiError.NotConfigured(id), null)
        val result = provider.testConnection()
        val cfg = configs.current().getValue(id)
        configs.save(cfg.copy(
            lastTest = if (result.ok) ConnectionStatus.VERIFIED else ConnectionStatus.FAILED,
            lastTestedAtMillis = clock.nowMillis(),
            lastErrorMessage = result.error?.message?.let(SecretRedactor::redact),
        ))
        return result
    }
}
