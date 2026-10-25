package com.polyglot.academy.ui.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.polyglot.academy.core.ai.ConnectionStatus
import com.polyglot.academy.core.ai.ProviderCatalog
import com.polyglot.academy.core.ai.ProviderConfig
import com.polyglot.academy.core.ai.ProviderId
import com.polyglot.academy.core.device.DeviceCapability
import com.polyglot.academy.core.device.DeviceTier
import com.polyglot.academy.core.localai.Compatibility
import com.polyglot.academy.core.localai.LocalModelCatalog
import com.polyglot.academy.core.model.AppSettings
import com.polyglot.academy.core.model.CefrLevel
import com.polyglot.academy.core.model.CorrectionIntensity
import com.polyglot.academy.core.model.DataCategory
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.Languages
import com.polyglot.academy.core.model.LearningGoal
import com.polyglot.academy.core.model.LearningStyle
import com.polyglot.academy.core.model.PrivacyConsent
import com.polyglot.academy.core.model.ProviderLocation
import com.polyglot.academy.core.model.ThemeMode
import com.polyglot.academy.core.service.secretKeyFor
import com.polyglot.academy.ui.app.LocalApp
import com.polyglot.academy.ui.app.ScrollScreen
import com.polyglot.academy.ui.app.rememberToast
import com.polyglot.academy.ui.designsystem.AppTextField
import com.polyglot.academy.ui.designsystem.Banner
import com.polyglot.academy.ui.designsystem.BannerKind
import com.polyglot.academy.ui.designsystem.ChipGroup
import com.polyglot.academy.ui.designsystem.ChoiceCard
import com.polyglot.academy.ui.designsystem.ConfirmDialog
import com.polyglot.academy.ui.designsystem.Divider
import com.polyglot.academy.ui.designsystem.Pill
import com.polyglot.academy.ui.designsystem.PolyCard
import com.polyglot.academy.ui.designsystem.PolyglotTheme
import com.polyglot.academy.ui.designsystem.PrimaryButton
import com.polyglot.academy.ui.designsystem.QuietButton
import com.polyglot.academy.ui.designsystem.SecondaryButton
import com.polyglot.academy.ui.designsystem.SectionHeader
import com.polyglot.academy.ui.designsystem.SettingsRow
import com.polyglot.academy.ui.designsystem.SwitchRow
import com.polyglot.academy.ui.i18n.K
import com.polyglot.academy.ui.i18n.Labels
import com.polyglot.academy.ui.i18n.LocalStrings
import com.polyglot.academy.ui.navigation.Route
import com.polyglot.academy.ui.navigation.SettingsSection
import kotlinx.coroutines.launch
import com.polyglot.academy.core.repo.RestoreException

@Composable
fun SettingsScreen() {
    val app = LocalApp.current
    val s = LocalStrings.current
    val privacy = app.settings.privacy
    ScrollScreen(s[K.SETTINGS_TITLE]) {
        PolyCard(contentPadding = 0.dp) {
            Column {
                fun go(section: SettingsSection) = app.nav.push(Route.SettingsPage(section))
                SettingsRow(s[K.SET_APPEARANCE], subtitle = Labels.theme(s, app.settings.theme), icon = Icons.Filled.Palette, onClick = { go(SettingsSection.APPEARANCE) })
                SettingsRow(s[K.SET_LANGUAGES], subtitle = Labels.language(s, app.profile.interfaceLanguage), icon = Icons.Filled.Language, onClick = { go(SettingsSection.LANGUAGES) })
                SettingsRow(s[K.SET_LEARNING], subtitle = s.plural(K.MINUTES, app.profile.dailyMinutes), icon = Icons.Filled.School, onClick = { go(SettingsSection.LEARNING) })
                SettingsRow(s[K.SET_SPEECH], icon = Icons.Filled.RecordVoiceOver, onClick = { go(SettingsSection.SPEECH) })
                SettingsRow(s[K.SET_ACCESSIBILITY], icon = Icons.Filled.Accessibility, onClick = { go(SettingsSection.ACCESSIBILITY) })
                Divider()
                SettingsRow(s[K.SET_PRIVACY], subtitle = if (privacy.cloudAiEnabled) s[K.SET_PRIVACY_SUMMARY_CLOUD] else s[K.SET_PRIVACY_SUMMARY_LOCAL], icon = Icons.Filled.Lock, onClick = { go(SettingsSection.PRIVACY) })
                SettingsRow(s[K.SET_AI_PROVIDERS], subtitle = s[K.SET_AI_PROVIDERS_SUB], icon = Icons.Filled.SmartToy, onClick = { go(SettingsSection.PROVIDERS) })
                SettingsRow(s[K.SET_LOCAL_MODELS], subtitle = s[K.SET_LOCAL_MODELS_SUB], icon = Icons.Filled.Memory, onClick = { go(SettingsSection.LOCAL_MODELS) })
                SettingsRow(s[K.SET_NOTIFICATIONS], subtitle = if (app.settings.reminders.enabled) s[K.ON] else s[K.OFF], icon = Icons.Filled.Notifications, onClick = { go(SettingsSection.REMINDERS) })
                SettingsRow(s[K.SET_DATA], icon = Icons.Filled.Storage, onClick = { go(SettingsSection.DATA) })
                SettingsRow(s[K.SET_PERMISSIONS], icon = Icons.Filled.VerifiedUser, onClick = { go(SettingsSection.PERMISSIONS) })
                Divider()
                SettingsRow(s[K.SET_HELP], icon = Icons.AutoMirrored.Filled.HelpOutline, onClick = { go(SettingsSection.HELP) })
                SettingsRow(s[K.SET_ABOUT], icon = Icons.Filled.Info, onClick = { go(SettingsSection.ABOUT) })
            }
        }
    }
}

@Composable
fun SettingsPageScreen(section: SettingsSection) {
    val s = LocalStrings.current
    val title = when (section) {
        SettingsSection.APPEARANCE -> s[K.SET_APPEARANCE]; SettingsSection.LANGUAGES -> s[K.SET_LANGUAGES]; SettingsSection.LEARNING -> s[K.SET_LEARNING]
        SettingsSection.SPEECH -> s[K.SET_SPEECH]; SettingsSection.ACCESSIBILITY -> s[K.SET_ACCESSIBILITY]; SettingsSection.PRIVACY -> s[K.SET_PRIVACY]
        SettingsSection.PROVIDERS -> s[K.PROV_TITLE]; SettingsSection.LOCAL_MODELS -> s[K.LOCAL_TITLE]; SettingsSection.REMINDERS -> s[K.SET_NOTIFICATIONS]
        SettingsSection.DATA -> s[K.SET_DATA]; SettingsSection.PERMISSIONS -> s[K.SET_PERMISSIONS]; SettingsSection.HELP -> s[K.SET_HELP]; SettingsSection.ABOUT -> s[K.SET_ABOUT]
    }
    ScrollScreen(title) {
        when (section) {
            SettingsSection.APPEARANCE -> Appearance()
            SettingsSection.LANGUAGES -> LanguagesPage()
            SettingsSection.LEARNING -> LearningPage()
            SettingsSection.SPEECH -> SpeechPage()
            SettingsSection.ACCESSIBILITY -> AccessibilityPage()
            SettingsSection.PRIVACY -> PrivacyPage()
            SettingsSection.PROVIDERS -> ProvidersPage()
            SettingsSection.LOCAL_MODELS -> LocalModelsPage()
            SettingsSection.REMINDERS -> RemindersPage()
            SettingsSection.DATA -> DataPage()
            SettingsSection.PERMISSIONS -> PermissionsPage()
            SettingsSection.HELP -> HelpPage()
            SettingsSection.ABOUT -> AboutPage()
        }
    }
}

@Composable
private fun updateSettings(): ((AppSettings) -> AppSettings) -> Unit {
    val app = LocalApp.current
    val scope = rememberCoroutineScope()
    return { t -> scope.launch { app.services.store.updateSettings { t(it) } } }
}

@Composable
private fun Appearance() {
    val app = LocalApp.current
    val s = LocalStrings.current
    val scope = rememberCoroutineScope()
    ThemeMode.entries.forEach { t -> ChoiceCard(Labels.theme(s, t), app.settings.theme == t, onClick = { scope.launch { app.services.store.updateSettings { it.copy(theme = t) } } }) }
}

@Composable
private fun LanguagesPage() {
    val app = LocalApp.current
    val s = LocalStrings.current
    val scope = rememberCoroutineScope()
    val p = app.profile
    SectionHeader(s[K.SET_INTERFACE_LANGUAGE])
    Languages.interfaceLanguages.forEach { lp -> ChoiceCard(lp.nativeName, p.interfaceLanguage == lp.code, onClick = { scope.launch { app.services.store.updateProfile { it.copy(interfaceLanguage = lp.code) } } }) }
    SectionHeader(s[K.SET_EXPLANATION_LANGUAGE])
    listOf(LanguageCode.ARABIC, LanguageCode.ENGLISH).forEach { code ->
        ChoiceCard(Labels.language(s, code), p.explanationLanguage == code, onClick = { scope.launch { app.services.store.updateProfile { it.copy(explanationLanguage = code) } } })
    }
    SectionHeader(s[K.SET_NATIVE_LANGUAGE])
    ChipGroup(Languages.all.map { it.code }, setOf(p.nativeLanguage), { Labels.language(s, it) }, { code -> scope.launch { app.services.store.updateProfile { it.copy(nativeLanguage = code) } } })
    p.learningLanguages.forEach { e ->
        val lp = Languages.of(e.language)
        SectionHeader(Labels.language(s, e.language))
        if (lp.variants.size > 1) {
            Text(s.f(K.SET_VARIANT, Labels.language(s, e.language)), style = MaterialTheme.typography.titleSmall)
            ChipGroup(lp.variants, lp.variants.filter { it.id == e.variantId }.toSet(), { if (s.language == LanguageCode.ARABIC) it.arabicName else it.englishName }, { v ->
                scope.launch { app.services.store.updateProfile { pr -> pr.copy(learningLanguages = pr.learningLanguages.map { if (it.language == e.language) it.copy(variantId = v.id) else it }) } }
            })
        }
        Text(s.f(K.SET_LEVEL, Labels.language(s, e.language)), style = MaterialTheme.typography.titleSmall)
        ChipGroup(CefrLevel.entries, setOf(e.startingLevel), { it.label }, { l ->
            scope.launch { app.services.assessments.applyStartingLevel(e.language, l, fromAssessment = false) }
        })
        PolyCard(contentPadding = 0.dp) {
            if (lp.has(com.polyglot.academy.core.model.LanguageFeature.TRANSLITERATION)) SwitchRow(s[K.SET_TRANSLIT], e.showTransliteration, { v ->
                scope.launch { app.services.store.updateProfile { pr -> pr.copy(learningLanguages = pr.learningLanguages.map { if (it.language == e.language) it.copy(showTransliteration = v) else it }) } }
            })
            if (lp.has(com.polyglot.academy.core.model.LanguageFeature.SIMPLIFIED_TRADITIONAL)) SwitchRow(s[K.SET_TRADITIONAL], e.useTraditionalCharacters, { v ->
                scope.launch { app.services.store.updateProfile { pr -> pr.copy(learningLanguages = pr.learningLanguages.map { if (it.language == e.language) it.copy(useTraditionalCharacters = v) else it }) } }
            })
        }
    }
}

@Composable
private fun LearningPage() {
    val app = LocalApp.current
    val s = LocalStrings.current
    val scope = rememberCoroutineScope()
    val p = app.profile
    fun up(t: (com.polyglot.academy.core.model.LearnerProfile) -> com.polyglot.academy.core.model.LearnerProfile) = scope.launch { app.services.store.updateProfile(t) }
    SectionHeader(s[K.SET_DAILY_MINUTES])
    ChipGroup(listOf(5, 10, 15, 20, 30, 45, 60), setOf(p.dailyMinutes), { s.plural(K.MINUTES, it) }, { m -> up { it.copy(dailyMinutes = m) } })
    SectionHeader(s[K.SET_GOALS])
    ChipGroup(LearningGoal.entries, p.goals, { Labels.goal(s, it) }, { g -> up { it.copy(goals = if (g in it.goals) it.goals - g else it.goals + g) } })
    SectionHeader(s[K.SET_STYLE])
    LearningStyle.entries.forEach { st -> ChoiceCard(Labels.style(s, st), p.learningStyle == st, onClick = { up { it.copy(learningStyle = st) } }, subtitle = Labels.styleDesc(s, st)) }
    SectionHeader(s[K.TUTOR_CORRECTION_LEVEL])
    CorrectionIntensity.entries.forEach { ci -> ChoiceCard(Labels.correction(s, ci), p.correctionIntensity == ci, onClick = { up { it.copy(correctionIntensity = ci) } }, subtitle = Labels.correctionDesc(s, ci)) }
    PolyCard(contentPadding = 0.dp) { SwitchRow(s[K.SET_RELAX_IN_PLAN], p.includeRelaxation, { v -> up { it.copy(includeRelaxation = v) } }) }
}

@Composable
private fun SpeechPage() {
    val app = LocalApp.current
    val s = LocalStrings.current
    val update = updateSettings()
    val sp = app.settings.speech
    Text(s[K.SET_SPEECH_ENGINE], style = MaterialTheme.typography.bodyMedium, color = PolyglotTheme.colors.textSecondary)
    val onDevice = app.services.platform.recognizer.processesAudioOnDevice
    Banner(BannerKind.INFO, s.f(K.SET_SPEECH_ON_DEVICE, when (onDevice) { true -> s[K.SET_SPEECH_STATUS_YES]; false -> s[K.SET_SPEECH_STATUS_NO]; null -> s[K.SET_SPEECH_STATUS_UNKNOWN] }))
    Text("${s[K.SET_SPEECH_RATE]}: ${(sp.speechRate * 100).toInt()}%", style = MaterialTheme.typography.titleSmall)
    Slider(sp.speechRate, { v -> update { it.copy(speech = it.speech.copy(speechRate = v)) } }, valueRange = 0.5f..1.5f, steps = 3, modifier = Modifier.semantics { contentDescription = s[K.SET_SPEECH_RATE] })
    PolyCard(contentPadding = 0.dp) {
        SwitchRow(s[K.SET_AUTOPLAY], sp.autoPlayTutorReplies, { v -> update { it.copy(speech = it.speech.copy(autoPlayTutorReplies = v)) } })
        SwitchRow(s[K.SET_SHOW_TRANSCRIPT], sp.showTranscriptWhileListening, { v -> update { it.copy(speech = it.speech.copy(showTranscriptWhileListening = v)) } })
    }
}

@Composable
private fun AccessibilityPage() {
    val app = LocalApp.current
    val s = LocalStrings.current
    val update = updateSettings()
    val a = app.settings.accessibility
    PolyCard(contentPadding = 0.dp) {
        SwitchRow(s[K.SET_REDUCED_MOTION], a.reducedMotion, { v -> update { it.copy(accessibility = it.accessibility.copy(reducedMotion = v)) } }, subtitle = s[K.SET_REDUCED_MOTION_SUB])
        SwitchRow(s[K.SET_LARGER_TEXT], a.largerText, { v -> update { it.copy(accessibility = it.accessibility.copy(largerText = v)) } }, subtitle = s[K.SET_LARGER_TEXT_SUB])
        SwitchRow(s[K.SET_HIGH_CONTRAST], a.highContrast, { v -> update { it.copy(accessibility = it.accessibility.copy(highContrast = v)) } })
        SwitchRow(s[K.SET_HAPTICS], a.hapticFeedback, { v -> update { it.copy(accessibility = it.accessibility.copy(hapticFeedback = v)) } })
        SwitchRow(s[K.SET_TRANSLATIONS_DEFAULT], app.settings.showTranslationsByDefault, { v -> update { it.copy(showTranslationsByDefault = v) } })
        SwitchRow(s[K.SET_HINTS_DEFAULT], app.settings.showHintsByDefault, { v -> update { it.copy(showHintsByDefault = v) } })
    }
}

@Composable
private fun PrivacyPage() {
    val app = LocalApp.current
    val s = LocalStrings.current
    val update = updateSettings()
    val p = app.settings.privacy
    var consent by remember { mutableStateOf(false) }
    Text(s[K.SET_PRIVACY_INTRO], style = MaterialTheme.typography.bodyMedium, color = PolyglotTheme.colors.textSecondary)
    PolyCard(contentPadding = 0.dp) {
        SwitchRow(s[K.SET_CLOUD_AI], p.cloudAiEnabled, { v ->
            if (v && p.consentVersionAccepted < PrivacyConsent.CURRENT_VERSION) consent = true
            else update { it.copy(privacy = it.privacy.copy(cloudAiEnabled = v)) }
        }, subtitle = s[K.SET_CLOUD_AI_SUB])
        SwitchRow(s[K.SET_LOCAL_NETWORK], p.allowLocalNetworkAi, { v -> update { it.copy(privacy = it.privacy.copy(allowLocalNetworkAi = v)) } }, subtitle = s[K.SET_LOCAL_NETWORK_SUB])
        SwitchRow(s[K.SET_FALLBACK], p.allowCloudFallback, { v -> update { it.copy(privacy = it.privacy.copy(allowCloudFallback = v)) } }, subtitle = s[K.SET_FALLBACK_SUB], enabled = p.cloudAiEnabled)
        SwitchRow(s[K.SET_BACKUP], p.includeInBackup, { v -> update { it.copy(privacy = it.privacy.copy(includeInBackup = v)) } }, subtitle = s[K.SET_BACKUP_SUB])
    }
    SectionHeader(s[K.SET_CATEGORIES])
    PolyCard(contentPadding = 0.dp) {
        DataCategory.entries.forEach { cat ->
            val allowed = cat in p.cloudAllowedCategories
            // Raw audio is never sent in this version, so the switch is shown off and disabled.
            SwitchRow(Labels.category(s, cat), allowed && cat != DataCategory.VOICE_AUDIO, { v ->
                update { it.copy(privacy = it.privacy.copy(cloudAllowedCategories = if (v) it.privacy.cloudAllowedCategories + cat else it.privacy.cloudAllowedCategories - cat)) }
            }, subtitle = Labels.categoryDesc(s, cat), enabled = p.cloudAiEnabled && cat != DataCategory.VOICE_AUDIO)
        }
    }
    if (consent) AlertDialog(onDismissRequest = { consent = false }, title = { Text(s[K.SET_CONSENT_TITLE]) }, text = { Text(s[K.SET_CONSENT_BODY]) },
        confirmButton = { QuietButton(s[K.SET_CONSENT_ACCEPT], {
            consent = false
            update { it.copy(privacy = it.privacy.copy(cloudAiEnabled = true, consentVersionAccepted = PrivacyConsent.CURRENT_VERSION)) }
        }) },
        dismissButton = { QuietButton(s[K.CANCEL], { consent = false }) })
}

@Composable
private fun ProvidersPage() {
    val app = LocalApp.current
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val configs by remember { app.services.store.configs }.collectAsState(emptyMap())
    val preferred by remember { app.services.store.preferredProvider }.collectAsState(null)
    var keys by remember { mutableStateOf(emptySet<ProviderId>()) }
    LaunchedEffect(configs) { keys = ProviderId.entries.filter { app.services.providers.hasKey(it) }.toSet() }
    Text(s[K.PROV_INTRO], style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
    if (!app.settings.privacy.cloudAiEnabled) Banner(BannerKind.WARNING, s[K.PROV_CLOUD_OFF], action = s[K.PROV_OPEN_PRIVACY], onAction = { app.nav.push(Route.SettingsPage(SettingsSection.PRIVACY)) })
    Banner(BannerKind.INFO, s[K.PROV_SUBSCRIPTION])
    ProviderCatalog.all.forEach { d ->
        val cfg = configs[d.id] ?: ProviderConfig(d.id)
        val status = providerStatus(s, d.id, cfg, d.id in keys)
        PolyCard(onClick = { if (d.id == ProviderId.ON_DEVICE) app.nav.push(Route.SettingsPage(SettingsSection.LOCAL_MODELS)) else app.nav.push(Route.ProviderDetail(d.id)) }) {
            Row {
                Column(Modifier.weight(1f)) {
                    Text(d.displayName, style = MaterialTheme.typography.titleMedium)
                    Text(Labels.location(s, d.location), style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                }
                if (preferred == d.id) Pill(s[K.PROV_PREFERRED_BADGE], container = c.primary, content = c.onPrimary)
            }
            Text(status.first, style = MaterialTheme.typography.labelLarge, color = status.second)
        }
    }
}

@Composable
internal fun providerStatus(s: com.polyglot.academy.ui.i18n.Localizer, id: ProviderId, cfg: ProviderConfig, hasKey: Boolean): Pair<String, androidx.compose.ui.graphics.Color> {
    val c = PolyglotTheme.colors
    val d = ProviderCatalog.of(id)
    return when {
        id == ProviderId.ON_DEVICE -> s[K.PROV_STATUS_NO_RUNTIME] to c.textMuted
        !cfg.enabled -> s[K.PROV_STATUS_OFF] to c.textMuted
        d.requiresApiKey && !hasKey -> s[K.PROV_STATUS_NEEDS_KEY] to c.warning
        (d.requiresBaseUrl && cfg.baseUrl.isNullOrBlank() && d.defaultBaseUrl == null) || (id == ProviderId.AZURE_OPENAI && cfg.deployment.isNullOrBlank()) -> s[K.PROV_STATUS_NEEDS_CONFIG] to c.warning
        cfg.lastTest == ConnectionStatus.VERIFIED -> s[K.PROV_STATUS_VERIFIED] to c.success
        cfg.lastTest == ConnectionStatus.FAILED -> s[K.PROV_STATUS_FAILED] to c.error
        else -> s[K.PROV_STATUS_UNTESTED] to c.info
    }
}

@Composable
fun ProviderDetailScreen(id: ProviderId) {
    val app = LocalApp.current
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val scope = rememberCoroutineScope()
    val d = ProviderCatalog.of(id)
    val configs by remember { app.services.store.configs }.collectAsState(emptyMap())
    val preferred by remember { app.services.store.preferredProvider }.collectAsState(null)
    val cfg = configs[id] ?: ProviderConfig(id)
    var hasKey by remember { mutableStateOf(false) }
    var keyInput by remember { mutableStateOf("") }
    var model by remember(cfg.model) { mutableStateOf(cfg.model.orEmpty()) }
    var baseUrl by remember(cfg.baseUrl) { mutableStateOf(cfg.baseUrl.orEmpty()) }
    var deployment by remember(cfg.deployment) { mutableStateOf(cfg.deployment.orEmpty()) }
    var testing by remember { mutableStateOf(false) }
    var testMessage by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    LaunchedEffect(id) { hasKey = app.services.providers.hasKey(id) }
    fun saveCfg(t: (ProviderConfig) -> ProviderConfig) = scope.launch { app.services.providers.saveConfig(t(app.services.store.current().getValue(id))) }
    ScrollScreen(d.displayName) {
        val status = providerStatus(s, id, cfg, hasKey)
        Text(status.first, style = MaterialTheme.typography.titleSmall, color = status.second)
        Text(if (d.location == ProviderLocation.CLOUD) s.f(K.PROV_PRIVACY_CLOUD, d.displayName) else s[K.PROV_PRIVACY_LOCAL], style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
        if (id == ProviderId.AZURE_OPENAI) Banner(BannerKind.INFO, s[K.PROV_AZURE_NOTE])
        PolyCard(contentPadding = 0.dp) { SwitchRow(s[K.PROV_ENABLED], cfg.enabled, { v -> saveCfg { it.copy(enabled = v) } }) }
        if (d.requiresApiKey || id == ProviderId.LOCAL_SERVER) {
            SectionHeader(s[K.PROV_API_KEY] + if (!d.requiresApiKey) " (${s[K.OPTIONAL]})" else "")
            if (hasKey) Text(s[K.PROV_KEY_SAVED], style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
            AppTextField(keyInput, { keyInput = it }, s[K.PROV_API_KEY], secret = true)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SecondaryButton(s[K.PROV_KEY_SAVE], { scope.launch { app.services.providers.saveKey(id, keyInput); keyInput = ""; hasKey = app.services.providers.hasKey(id) } }, Modifier.weight(1f), enabled = keyInput.isNotBlank())
                if (hasKey) QuietButton(s[K.PROV_KEY_REMOVE], { scope.launch { app.services.platform.secrets.remove(secretKeyFor(id)); hasKey = false } }, color = c.error)
            }
            Text(s.f(K.PROV_KEY_STORAGE, app.services.platform.secrets.protectionDescription), style = MaterialTheme.typography.labelSmall, color = c.textMuted)
        }
        SectionHeader(s[K.PROV_MODEL])
        AppTextField(model, { model = it }, s[K.PROV_MODEL], supporting = s.f(K.PROV_MODEL_DEFAULT, d.defaultModel))
        if (d.requiresBaseUrl || d.defaultBaseUrl != null) AppTextField(baseUrl, { baseUrl = it }, s[K.PROV_BASE_URL], supporting = d.defaultBaseUrl)
        if (id == ProviderId.AZURE_OPENAI) AppTextField(deployment, { deployment = it }, s[K.PROV_DEPLOYMENT])
        SecondaryButton(s[K.SAVE], { saveCfg { it.copy(model = model.trim().ifBlank { null }, baseUrl = baseUrl.trim().ifBlank { null }, deployment = deployment.trim().ifBlank { null }) } }, Modifier.fillMaxWidth())
        PrimaryButton(if (testing) s[K.PROV_TESTING] else s[K.PROV_TEST], {
            testing = true; testMessage = null
            scope.launch {
                saveCfg { it.copy(model = model.trim().ifBlank { null }, baseUrl = baseUrl.trim().ifBlank { null }, deployment = deployment.trim().ifBlank { null }) }.join()
                val r = app.services.providers.testConnection(id)
                testMessage = if (r.ok) true to s.f(K.PROV_TEST_OK, r.latencyMillis ?: 0) else false to s.f(K.PROV_TEST_FAILED, r.error?.message ?: "")
                testing = false
            }
        }, Modifier.fillMaxWidth(), loading = testing, enabled = cfg.enabled)
        testMessage?.let { (ok, msg) -> Banner(if (ok) BannerKind.SUCCESS else BannerKind.ERROR, msg) }
        if (preferred == id) Text(s[K.PROV_IS_PREFERRED], color = c.success)
        else QuietButton(s[K.PROV_MAKE_PREFERRED], { scope.launch { app.services.store.setPreferred(id) } })
        SectionHeader(s[K.PROV_FEATURES])
        PolyCard { d.capabilities.forEach { Text("• " + Labels.capability(s, it)) }; Text(s[K.PROV_SPEECH_NOTE], style = MaterialTheme.typography.bodySmall, color = c.textSecondary) }
        if (cfg.requestCount > 0) Text(s.f(K.PROV_USAGE, cfg.requestCount, cfg.approxTokensUsed), style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
        Text(s.f(K.PROV_DOCS, d.documentationUrl), style = MaterialTheme.typography.bodySmall, color = c.textMuted)
    }
}

@Composable
private fun LocalModelsPage() {
    val app = LocalApp.current
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val info = remember { app.services.platform.deviceInfo() }
    Text(s[K.LOCAL_INTRO], style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
    SectionHeader(s[K.LOCAL_DEVICE])
    PolyCard {
        Text(when (DeviceCapability.tier(info)) { DeviceTier.LOW -> s[K.LOCAL_TIER_LOW]; DeviceTier.MID -> s[K.LOCAL_TIER_MID]; DeviceTier.HIGH -> s[K.LOCAL_TIER_HIGH] }, style = MaterialTheme.typography.titleSmall)
        Text(s.f(K.LOCAL_DEVICE_INFO, info.totalRamMb, info.availableStorageMb, info.cpuCores), style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
    }
    LocalModelCatalog.models.forEach { m ->
        val comp = LocalModelCatalog.compatibility(m, info)
        PolyCard {
            Text(m.displayName, style = MaterialTheme.typography.titleSmall)
            Text(s.f(K.LOCAL_MODEL_SIZE, m.downloadSizeMb, m.minRamMb, m.license), style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
            Text(m.languagesNote, style = MaterialTheme.typography.bodySmall, color = c.textMuted)
            if (comp is Compatibility.Incompatible) comp.reasons.forEach { r -> Text("• " + s[K.valueOf("LOCAL_REASON_${r.name}")], style = MaterialTheme.typography.bodySmall, color = c.warning) }
        }
    }
    Banner(BannerKind.INFO, s[K.LOCAL_ALTERNATIVE], action = s[K.PROV_TITLE], onAction = { app.nav.push(Route.ProviderDetail(ProviderId.LOCAL_SERVER)) })
}

@Composable
private fun RemindersPage() {
    val app = LocalApp.current
    val s = LocalStrings.current
    val scope = rememberCoroutineScope()
    val toast = rememberToast()
    val r = app.settings.reminders
    fun apply(next: com.polyglot.academy.core.model.ReminderSettings) = scope.launch {
        app.services.store.updateSettings { it.copy(reminders = next) }
        val granted = if (next.enabled) app.platformUi.requestNotificationPermission() else true
        val ok = app.services.platform.scheduleReminder(next.enabled && granted, next.hour, next.minute, next.days)
        toast(if (ok && granted) s[K.SET_REMINDER_SAVED] else s[K.SET_REMINDER_FAILED])
    }
    PolyCard(contentPadding = 0.dp) { SwitchRow(s[K.SET_REMINDER_ENABLE], r.enabled, { v -> apply(r.copy(enabled = v)) }) }
    if (r.enabled) {
        SectionHeader(s[K.SET_REMINDER_TIME])
        ChipGroup((6..22).toList(), setOf(r.hour), { "${it.toString().padStart(2, '0')}:00" }, { h -> apply(r.copy(hour = h, minute = 0)) })
        SectionHeader(s[K.SET_REMINDER_DAYS])
        ChipGroup((1..7).toList(), r.days, { Labels.day(s, it) }, { d -> apply(r.copy(days = if (d in r.days) r.days - d else r.days + d)) })
        val pad = { h: Int -> "${h.toString().padStart(2, '0')}:00" }
        Text(s.f(K.SET_QUIET_HOURS, pad(r.quietHoursStart), pad(r.quietHoursEnd)), style = MaterialTheme.typography.bodySmall, color = PolyglotTheme.colors.textSecondary)
        if (r.hour >= r.quietHoursStart || r.hour < r.quietHoursEnd) Banner(BannerKind.INFO, s[K.SET_REMINDER_QUIET_CONFLICT])
    }
}

@Composable
private fun DataPage() {
    val app = LocalApp.current
    val s = LocalStrings.current
    val scope = rememberCoroutineScope()
    val toast = rememberToast()
    var includeConv by remember { mutableStateOf(false) }
    var includeNotes by remember { mutableStateOf(false) }
    var confirm by remember { mutableStateOf<K?>(null) }
    SectionHeader(s[K.SET_EXPORT])
    Text(s[K.SET_EXPORT_SUB], style = MaterialTheme.typography.bodySmall, color = PolyglotTheme.colors.textSecondary)
    PolyCard(contentPadding = 0.dp) {
        SwitchRow(s[K.SET_EXPORT_CONV], includeConv, { includeConv = it })
        SwitchRow(s[K.SET_EXPORT_NOTES], includeNotes, { includeNotes = it })
    }
    PrimaryButton(s[K.SET_EXPORT], {
        scope.launch {
            val json = app.services.store.exportJson(includeConv, includeNotes)
            val where = app.platformUi.saveExport("polyglot-academy-export.json", json)
            toast(where?.let { s.f(K.SET_EXPORT_SAVED_TO, it) } ?: s.f(K.SET_EXPORT_DONE, "${json.length / 1024 + 1} KB"))
        }
    }, Modifier.fillMaxWidth())
    SectionHeader(s[K.SET_RESTORE])
    Text(s[K.SET_RESTORE_SUB], style = MaterialTheme.typography.bodySmall, color = PolyglotTheme.colors.textSecondary)
    SecondaryButton(s[K.SET_RESTORE], { confirm = K.SET_RESTORE }, Modifier.fillMaxWidth(), enabled = app.platformUi.canPickFiles)
    if (!app.platformUi.canPickFiles) Text(s[K.SET_RESTORE_UNAVAILABLE], style = MaterialTheme.typography.bodySmall, color = PolyglotTheme.colors.textSecondary)
    SectionHeader(s[K.DELETE])
    SecondaryButton(s[K.SET_DELETE_CONV], { confirm = K.SET_DELETE_CONV }, Modifier.fillMaxWidth())
    SecondaryButton(s[K.SET_DELETE_MATERIALS], { confirm = K.SET_DELETE_MATERIALS }, Modifier.fillMaxWidth())
    SecondaryButton(s[K.SET_DELETE_ALL], { confirm = K.SET_DELETE_ALL }, Modifier.fillMaxWidth())
    if (confirm == K.SET_RESTORE) {
        ConfirmDialog(s[K.SET_RESTORE], s[K.SET_RESTORE_BODY], s[K.SET_RESTORE_CONFIRM], s[K.CANCEL], destructive = true,
            onConfirm = {
                confirm = null
                scope.launch {
                    val file = app.platformUi.pickFile() ?: return@launch
                    val text = file.text ?: return@launch toast(s[K.SET_RESTORE_INVALID])
                    toast(try {
                        val r = app.services.restore(text) { app.platformUi.requestNotificationPermission() }
                        val n = r.counts.values.sum()
                        if (r.skipped == 0) s.f(K.SET_RESTORE_DONE, n) else s.f(K.SET_RESTORE_SKIPPED, n, r.skipped)
                    } catch (e: RestoreException) {
                        when (e.message) { "newer_version" -> s[K.SET_RESTORE_NEWER]; "file_too_large" -> s[K.SET_RESTORE_TOO_LARGE]; else -> s[K.SET_RESTORE_INVALID] }
                    })
                }
            }, onDismiss = { confirm = null })
    }
    confirm?.takeIf { it != K.SET_RESTORE }?.let { what ->
        ConfirmDialog(s[what], if (what == K.SET_DELETE_ALL) s[K.SET_DELETE_ALL_BODY] else s[what], s[K.SET_DELETE_CONFIRM], s[K.CANCEL], destructive = true,
            onConfirm = {
                confirm = null
                scope.launch {
                    when (what) {
                        K.SET_DELETE_CONV -> app.services.store.deleteConversations()
                        K.SET_DELETE_MATERIALS -> app.services.store.deleteImportedMaterials()
                        else -> app.services.deleteEverything()
                    }
                    toast(s[K.SET_DELETED])
                }
            }, onDismiss = { confirm = null })
    }
}

@Composable
private fun PermissionsPage() {
    val s = LocalStrings.current
    listOf(K.PERM_MIC to K.PERM_MIC_BODY, K.PERM_NOTIF to K.PERM_NOTIF_BODY, K.PERM_FILES to K.PERM_FILES_BODY, K.PERM_CAMERA to K.PERM_CAMERA_BODY, K.PERM_INTERNET to K.PERM_INTERNET_BODY).forEach { (t, b) ->
        PolyCard { Text(s[t], style = MaterialTheme.typography.titleSmall); Text(s[b], style = MaterialTheme.typography.bodyMedium, color = PolyglotTheme.colors.textSecondary) }
    }
    val app = LocalApp.current
    SecondaryButton(s[K.SETTINGS_TITLE], { app.platformUi.openAppSettings() }, Modifier.fillMaxWidth())
}

@Composable
private fun HelpPage() {
    val s = LocalStrings.current
    (1..6).forEach { i ->
        PolyCard {
            Text(s[K.valueOf("HELP_Q$i")], style = MaterialTheme.typography.titleSmall)
            Text(s[K.valueOf("HELP_A$i")], style = MaterialTheme.typography.bodyMedium, color = PolyglotTheme.colors.textSecondary)
        }
    }
}

@Composable
private fun AboutPage() {
    val app = LocalApp.current
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    Text(s[K.APP_NAME], style = MaterialTheme.typography.headlineSmall)
    Text(s.f(K.ABOUT_VERSION, app.platformUi.appVersion), color = c.textSecondary)
    Text(s.f(K.ABOUT_PLATFORM, app.services.platform.platformName), color = c.textSecondary)
    listOf(K.ABOUT_BODY, K.ABOUT_AI, K.ABOUT_LEVELS, K.ABOUT_LICENSES).forEach { Text(s[it], style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 4.dp)) }
}
