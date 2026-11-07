package com.polyglot.academy.core.ai

import com.polyglot.academy.core.model.DataCategory
import com.polyglot.academy.core.model.PrivacySettings
import com.polyglot.academy.core.model.ProviderLocation

sealed interface PrivacyDecision {
    data object Allowed : PrivacyDecision
    data class Denied(val blockedCategories: Set<DataCategory>, val reason: DenialReason) : PrivacyDecision
}

enum class DenialReason { CLOUD_AI_DISABLED, CATEGORY_NOT_ALLOWED_FOR_CLOUD, LOCAL_NETWORK_DISABLED, ITEM_NOT_CONSENTED, CONSENT_OUTDATED }

/**
 * Single enforcement point for "may this data go to this provider?". Every outbound AI request
 * passes through [evaluate]; the router never bypasses it, including on fallback.
 */
object PrivacyPolicy {
    fun evaluate(
        settings: PrivacySettings,
        location: ProviderLocation,
        categories: Set<DataCategory>,
        perItemCloudConsent: Boolean = false,
    ): PrivacyDecision = when (location) {
        ProviderLocation.ON_DEVICE -> PrivacyDecision.Allowed
        ProviderLocation.LOCAL_NETWORK ->
            if (settings.allowLocalNetworkAi) PrivacyDecision.Allowed
            else PrivacyDecision.Denied(categories, DenialReason.LOCAL_NETWORK_DISABLED)
        ProviderLocation.CLOUD -> when {
            !settings.cloudAiEnabled -> PrivacyDecision.Denied(categories, DenialReason.CLOUD_AI_DISABLED)
            settings.consentVersionAccepted < com.polyglot.academy.core.model.PrivacyConsent.CURRENT_VERSION ->
                PrivacyDecision.Denied(categories, DenialReason.CONSENT_OUTDATED)
            else -> {
                val blocked = categories.filterNot { cat ->
                    cat in settings.cloudAllowedCategories ||
                        // Imported material may be allowed per item even if the category is off.
                        (cat == DataCategory.IMPORTED_MATERIAL && perItemCloudConsent)
                }.toSet()
                if (blocked.isEmpty()) PrivacyDecision.Allowed
                else PrivacyDecision.Denied(blocked, if (DataCategory.IMPORTED_MATERIAL in blocked) DenialReason.ITEM_NOT_CONSENTED else DenialReason.CATEGORY_NOT_ALLOWED_FOR_CLOUD)
            }
        }
    }
}

/** Removes things that look like credentials from text that might be logged. */
object SecretRedactor {
    private val patterns = listOf(
        Regex("sk-[A-Za-z0-9_\\-]{8,}"),
        Regex("sk-ant-[A-Za-z0-9_\\-]{8,}"),
        Regex("AIza[0-9A-Za-z_\\-]{20,}"),
        Regex("(?i)(api[-_ ]?key|authorization|x-api-key|bearer)\\s*[:=]?\\s*[A-Za-z0-9_\\-\\.]{8,}"),
    )

    fun redact(text: String): String = patterns.fold(text) { acc, r -> r.replace(acc, "[REDACTED]") }
}
