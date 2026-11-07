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
import com.polyglot.academy.core.ai.AiError
import com.polyglot.academy.core.ai.ProviderConfig
import com.polyglot.academy.core.ai.ProviderId
import com.polyglot.academy.core.device.DeviceCapability
import com.polyglot.academy.core.device.DeviceTier
import com.polyglot.academy.core.localai.Compatibility
import com.polyglot.academy.core.localai.LocalModelCatalog
import com.polyglot.academy.core.localai.IncompatibilityReason
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.Alignment
import com.polyglot.academy.core.model.AppSettings
import com.polyglot.academy.core.model.CefrLevel
import com.polyglot.academy.core.model.CorrectionIntensity
import com.polyglot.academy.core.model.DataCategory
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.Languages
import com.polyglot.academy.core.util.toLocalDate
import kotlinx.coroutines.flow.first
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
                SettingsRow(s[K.SET_NOTIFICATIONS], subtitle = if (app.settings.reminders.enabled) s[K.ON] else s[K.OFF], icon = Icons.Filled.Notifications, onClick = { go(SettingsSection.REMINDERS) })
                SettingsRow(s[K.SET_DATA], icon = Icons.Filled.Storage, onClick = { go(SettingsSection.DATA) })
                SettingsRow(s[K.SET_PERMISSIONS], icon = Icons.Filled.VerifiedUser, onClick = { go(SettingsSection.PERMISSIONS) })
                Divider()
                SettingsRow(s[K.SET_HELP], icon = Icons.AutoMirrored.Filled.HelpOutline, onClick = { go(SettingsSection.HELP) })
                SettingsRow(s[K.SET_ABOUT], icon = Icons.Filled.Info, onClick = { go(SettingsSection.ABOUT) })
                Divider()
                // Keys and models are for those who want them; most learners never need to see them.
                var advanced by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }
                SettingsRow(s[K.SET_ADVANCED], subtitle = s[K.SET_ADVANCED_SUB], icon = Icons.Filled.Memory, onClick = { advanced = !advanced })
                if (advanced) {
                    SettingsRow(s[K.SET_AI_PROVIDERS], subtitle = s[K.SET_AI_PROVIDERS_SUB], icon = Icons.Filled.SmartToy, onClick = { go(SettingsSection.PROVIDERS) })
                    SettingsRow(s[K.SET_VOICE], subtitle = s[K.SET_VOICE_SUB], icon = Icons.Filled.RecordVoiceOver, onClick = { go(SettingsSection.VOICE) })
                    SettingsRow(s[K.SET_LOCAL_MODELS], subtitle = s[K.SET_LOCAL_MODELS_SUB], icon = Icons.Filled.Memory, onClick = { go(SettingsSection.LOCAL_MODELS) })
                }
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
        SettingsSection.PROVIDERS -> s[K.PROV_TITLE]; SettingsSection.VOICE -> s[K.SET_VOICE]; SettingsSection.LOCAL_MODELS -> s[K.LOCAL_TITLE]; SettingsSection.REMINDERS -> s[K.SET_NOTIFICATIONS]
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
            SettingsSection.VOICE -> VoicePage()
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
            if (lp.has(com.polyglot.academy.core.model.LanguageFeature.TRANSLITERATION) && e.showTransliteration) SwitchRow(s[K.SET_TRANSLIT_KNOWN], e.hideReadingWhenKnown, { v ->
                scope.launch { app.services.store.updateProfile { pr -> pr.copy(learningLanguages = pr.learningLanguages.map { if (it.language == e.language) it.copy(hideReadingWhenKnown = v) else it }) } }
            }, subtitle = s[K.SET_TRANSLIT_KNOWN_SUB])
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
    SectionHeader(s[K.GAME_SET_GOAL])
    ChipGroup(com.polyglot.academy.core.gamification.XpRules.goalChoices, setOf(p.dailyXpGoal), { s.f(K.GAME_XP, it) }, { x -> up { it.copy(dailyXpGoal = x) } })
    SectionHeader(s[K.SET_GOALS])
    ChipGroup(LearningGoal.entries, p.goals, { Labels.goal(s, it) }, { g -> up { it.copy(goals = if (g in it.goals) it.goals - g else it.goals + g) } })
    SectionHeader(s[K.SET_STYLE])
    LearningStyle.entries.forEach { st -> ChoiceCard(Labels.style(s, st), p.learningStyle == st, onClick = { up { it.copy(learningStyle = st) } }, subtitle = Labels.styleDesc(s, st)) }
    SectionHeader(s[K.TUTOR_CORRECTION_LEVEL])
    CorrectionIntensity.entries.forEach { ci -> ChoiceCard(Labels.correction(s, ci), p.correctionIntensity == ci, onClick = { up { it.copy(correctionIntensity = ci) } }, subtitle = Labels.correctionDesc(s, ci)) }
    PolyCard(contentPadding = 0.dp) { SwitchRow(s[K.SET_RELAX_IN_PLAN], p.includeRelaxation, { v -> up { it.copy(includeRelaxation = v) } }) }
    PolyCard(contentPadding = 0.dp) { SwitchRow(s[K.SET_SHOW_POINTS], p.showPointsAndStreak, { v -> up { it.copy(showPointsAndStreak = v) } }, subtitle = s[K.SET_SHOW_POINTS_SUB]) }
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
    SecondaryButton(s[K.PRIV_TITLE], { app.nav.push(Route.PrivacyNotice) }, Modifier.fillMaxWidth())
    PolyCard(contentPadding = 0.dp) {
        SwitchRow(s[K.SET_CLOUD_AI], p.cloudAiEnabled, { v ->
            if (v && p.consentVersionAccepted < PrivacyConsent.CURRENT_VERSION) consent = true
            else update { it.copy(privacy = it.privacy.copy(cloudAiEnabled = v)) }
        }, subtitle = s[K.SET_CLOUD_AI_SUB])
        SwitchRow(s[K.SET_LOCAL_NETWORK], p.allowLocalNetworkAi, { v -> update { it.copy(privacy = it.privacy.copy(allowLocalNetworkAi = v)) } }, subtitle = s[K.SET_LOCAL_NETWORK_SUB])
        SwitchRow(s[K.SET_FALLBACK], p.allowCloudFallback, { v -> update { it.copy(privacy = it.privacy.copy(allowCloudFallback = v)) } }, subtitle = s[K.SET_FALLBACK_SUB], enabled = p.cloudAiEnabled)
        SwitchRow(s[K.SET_BACKUP], p.includeInBackup, { v -> update { it.copy(privacy = it.privacy.copy(includeInBackup = v)) } }, subtitle = s[K.SET_BACKUP_SUB])
        if (app.services.platform.hasHomeWidget) SwitchRow(s[K.SET_WIDGET_WORD], p.widgetShowsWord, { v -> update { it.copy(privacy = it.privacy.copy(widgetShowsWord = v)) } }, subtitle = s[K.SET_WIDGET_WORD_SUB])
    }
    SectionHeader(s[K.SET_CATEGORIES])
    PolyCard(contentPadding = 0.dp) {
        DataCategory.entries.forEach { cat ->
            val allowed = cat in p.cloudAllowedCategories
            SwitchRow(Labels.category(s, cat), allowed, { v ->
                update { it.copy(privacy = it.privacy.copy(cloudAllowedCategories = if (v) it.privacy.cloudAllowedCategories + cat else it.privacy.cloudAllowedCategories - cat)) }
            }, subtitle = Labels.categoryDesc(s, cat), enabled = p.cloudAiEnabled)
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
    FeatureProviders(configs, keys, preferred)
}

/** Lets the learner pick a different provider for each feature; "Preferred" follows the preferred provider. */
@Composable
private fun FeatureProviders(configs: Map<ProviderId, ProviderConfig>, keys: Set<ProviderId>, preferred: ProviderId?) {
    val app = LocalApp.current
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val scope = rememberCoroutineScope()
    val ready = ProviderCatalog.all.filter { d ->
        val cfg = configs[d.id] ?: ProviderConfig(d.id)
        d.id != ProviderId.ON_DEVICE && cfg.enabled && (!d.requiresApiKey || d.id in keys)
    }
    SectionHeader(s[K.PROV_PER_FEATURE])
    Text(s[K.PROV_PER_FEATURE_BODY], style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
    if (ready.isEmpty()) { Text(s[K.PROV_PER_FEATURE_NONE], style = MaterialTheme.typography.bodyMedium, color = c.textMuted); return }
    com.polyglot.academy.core.ai.AiCapability.entries.forEach { cap ->
        val options = listOf<ProviderId?>(null) + ready.filter { cap in it.capabilities }.map { it.id }
        val chosen = app.settings.aiFeatureProviders[cap]
        PolyCard {
            Text(Labels.capability(s, cap), style = MaterialTheme.typography.titleSmall)
            ChipGroup(options, setOf(chosen), { id ->
                id?.let { ProviderCatalog.of(it).displayName } ?: (s[K.PROV_FEATURE_DEFAULT] + (preferred?.let { " (${ProviderCatalog.of(it).displayName})" } ?: ""))
            }, { id -> scope.launch { app.services.providers.setFeatureProvider(cap, id) } })
            if (options.size == 1) Text(s[K.PROV_FEATURE_UNSUPPORTED], style = MaterialTheme.typography.bodySmall, color = c.textMuted)
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
    var accountModels by remember { mutableStateOf(emptyList<String>()) }
    var loadingModels by remember { mutableStateOf(false) }
    var modelsError by remember { mutableStateOf<String?>(null) }
    var modelNote by remember { mutableStateOf<String?>(null) }
    var keySaves by remember { mutableStateOf(0) }
    var keyWarning by remember { mutableStateOf<String?>(null) }
    var urlError by remember { mutableStateOf<String?>(null) }
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
                SecondaryButton(s[K.PROV_KEY_SAVE], { scope.launch {
                    // Saved anyway (formats can change), but a project number or a name pasted instead of the key is caught here.
                    keyWarning = com.polyglot.academy.core.ai.ApiKeyText.unexpectedFormat(id, keyInput)?.let { s.f(K.PROV_KEY_FORMAT, d.displayName, it) }
                    app.services.providers.saveKey(id, keyInput); keyInput = ""; hasKey = app.services.providers.hasKey(id); if (hasKey) keySaves++
                } }, Modifier.weight(1f), enabled = keyInput.isNotBlank())
                if (hasKey) QuietButton(s[K.PROV_KEY_REMOVE], { scope.launch { app.services.platform.secrets.remove(secretKeyFor(id)); hasKey = false } }, color = c.error)
            }
            keyWarning?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = c.warning) }
            Text(s.f(K.PROV_KEY_STORAGE, app.services.platform.secrets.protectionDescription), style = MaterialTheme.typography.labelSmall, color = c.textMuted)
        }
        d.keyUrl?.let { Text(s.f(K.PROV_GET_KEY, it), style = MaterialTheme.typography.bodySmall, color = c.textSecondary) }
        SectionHeader(s[K.PROV_MODEL])
        AppTextField(model, { model = it }, s[K.PROV_MODEL],
            supporting = if (d.defaultModel.isBlank()) s[K.PROV_MODEL_REQUIRED] else s.f(K.PROV_MODEL_DEFAULT, d.defaultModel))
        if (d.suggestedModels.isNotEmpty()) {
            Text(s[K.PROV_MODEL_SUGGESTED], style = MaterialTheme.typography.labelLarge)
            ChipGroup(d.suggestedModels, setOf(model.ifBlank { d.defaultModel }), { it }, { model = it })
        }
        // A local server's address is completed (/v1) and shown that way, so what is saved is what is used.
        // Anything that is not a web address (often the key, pasted into the wrong field) is cleared, never
        // saved or sent; a provider with its own address then uses that one.
        fun cleanBase(): String? {
            val b = baseUrl.trim().ifBlank { null } ?: return null.also { urlError = null }
            val u = if (id == ProviderId.LOCAL_SERVER) com.polyglot.academy.core.ai.LocalServerUrl.normalize(b) else b
            if (!com.polyglot.academy.core.ai.ProviderUrl.isWebAddress(u)) {
                urlError = s[if (d.defaultBaseUrl != null) K.PROV_BASE_URL_INVALID_DEFAULT else K.PROV_BASE_URL_INVALID]
                baseUrl = ""
                return null
            }
            urlError = null
            return u.also { if (id == ProviderId.LOCAL_SERVER) baseUrl = it }
        }
        fun host() = com.polyglot.academy.core.ai.ProviderUrl.host(cleanBase() ?: d.defaultBaseUrl.orEmpty())
        // What went wrong, in the learner's words: a rejected key, no network, a model the key does not have...
        fun explain(e: Throwable?): String {
            val name = d.displayName
            val chosen = model.ifBlank { d.defaultModel }
            return when (e) {
                is AiError.InvalidResponse -> if ("404" !in e.message.orEmpty()) e.message.orEmpty()
                    // HTTP 404 from a local server: the address or the model name is wrong, not the network.
                    else if (id == ProviderId.LOCAL_SERVER) s.f(K.PROV_LOCAL_404, cleanBase() ?: d.defaultBaseUrl.orEmpty(), chosen)
                    else s.f(K.PROV_ERR_MODEL, chosen)
                is AiError.Authentication -> s.f(K.PROV_ERR_AUTH, name)
                // The exception type says which step failed: finding the server, reaching it, or securing the connection.
                is AiError.Network -> when {
                    "UnknownHost" in e.detail -> s.f(K.PROV_ERR_DNS, name, host())
                    "SSL" in e.detail || "Cert" in e.detail -> s.f(K.PROV_ERR_TLS, name, host())
                    listOf("ConnectException", "NoRouteToHost", "SocketTimeout", "PortUnreachable").any { it in e.detail } -> s.f(K.PROV_ERR_BLOCKED, name, host())
                    else -> s.f(K.PROV_ERR_NETWORK, name)
                } + " " + s.f(K.PROV_ERR_DETAIL, e.detail)
                is AiError.Timeout -> s.f(K.PROV_ERR_TIMEOUT, name)
                is AiError.QuotaExceeded -> s.f(K.PROV_ERR_QUOTA, name)
                is AiError.RateLimited -> s.f(K.PROV_ERR_RATE, name)
                is AiError.RegionNotSupported -> s.f(K.PROV_ERR_REGION, name)
                is AiError.Server -> s.f(K.PROV_ERR_SERVER, name, e.status)
                is AiError.NotConfigured -> s.f(K.PROV_ERR_NOT_CONFIGURED, name)
                else -> e?.message.orEmpty()
            }
        }
        // Loads the account's models; when the chosen model is not among them, picks one that is and saves it.
        suspend fun loadModels(): Boolean {
            loadingModels = true; modelsError = null; modelNote = null
            saveCfg { it.copy(baseUrl = cleanBase(), deployment = deployment.trim().ifBlank { null }) }.join()
            val ok = app.services.providers.listModels(id).fold(
                { list ->
                    if (list == null) modelsError = s[K.PROV_MODELS_UNSUPPORTED] else {
                        accountModels = list
                        if (list.isEmpty()) modelsError = s[K.PROV_MODELS_EMPTY]
                        com.polyglot.academy.core.ai.ModelChoice.replacementFor(model, d.defaultModel, list)?.let { pick ->
                            modelNote = s.f(K.PROV_MODEL_SWITCHED, model.ifBlank { d.defaultModel }, pick)
                            model = pick
                            saveCfg { it.copy(model = pick) }.join()
                        }
                    }
                    true
                },
                { e -> accountModels = emptyList(); modelsError = s.f(K.PROV_MODELS_FAILED, explain(e)); false })
            loadingModels = false
            return ok
        }
        suspend fun test() {
            testing = true; testMessage = null
            saveCfg { it.copy(model = model.trim().ifBlank { null }, baseUrl = cleanBase(), deployment = deployment.trim().ifBlank { null }) }.join()
            val r = app.services.providers.testConnection(id)
            testMessage = if (r.ok) true to s.f(K.PROV_TEST_OK, r.latencyMillis ?: 0) else false to s.f(K.PROV_TEST_FAILED, explain(r.error))
            testing = false
        }
        // A cloud key is enough: its models are loaded when the page opens, and after a new key is saved the
        // provider is switched on, a model it has is chosen and the connection is tested without further taps.
        LaunchedEffect(hasKey, keySaves) {
            if (!hasKey || !d.requiresApiKey || d.location != ProviderLocation.CLOUD) return@LaunchedEffect
            // A provider on the learner's own server (Azure, a custom one) needs its address first.
            if (d.requiresBaseUrl && cleanBase() == null) { modelNote = s[K.PROV_NEED_BASE_URL]; return@LaunchedEffect }
            if (keySaves > 0) {
                if (loadModels()) {
                    saveCfg { it.copy(enabled = true) }.join()
                    if (id == ProviderId.AZURE_OPENAI && deployment.isBlank()) { modelNote = s[K.PROV_NEED_DEPLOYMENT]; return@LaunchedEffect }
                    test()
                    if (testMessage?.first == true && preferred == null) app.services.store.setPreferred(id)
                }
            } else if (accountModels.isEmpty()) loadModels()
        }
        SecondaryButton(if (loadingModels) s[K.PROV_MODELS_LOADING] else s[K.PROV_MODELS_LOAD], {
            scope.launch { loadModels() }
        }, Modifier.fillMaxWidth(), enabled = !loadingModels && (hasKey || !d.requiresApiKey))
        modelsError?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = c.warning) }
        modelNote?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = c.textSecondary) }
        if (accountModels.isNotEmpty()) {
            // Typing filters the list; a model picked from it (or chosen automatically) shows the whole list.
            val filter = model.trim().lowercase().takeIf { model.trim() !in accountModels }.orEmpty()
            val shown = accountModels.filter { filter.isEmpty() || filter in it.lowercase() || it == model }.take(40)
            Text(s.plural(K.PROV_MODELS_FOUND, accountModels.size, shown.size), style = MaterialTheme.typography.labelLarge)
            ChipGroup(shown, setOf(model), { it }, { model = it })
        }
        if (d.requiresBaseUrl || d.defaultBaseUrl != null) {
            AppTextField(baseUrl, { baseUrl = it }, s[K.PROV_BASE_URL], supporting = d.defaultBaseUrl)
            urlError?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = c.warning) }
            if (d.defaultBaseUrl != null && baseUrl.isNotBlank() && baseUrl.trim().trimEnd('/') != d.defaultBaseUrl)
                QuietButton(s[K.PROV_BASE_URL_RESET], { baseUrl = ""; urlError = null; saveCfg { it.copy(baseUrl = null) } })
        }
        if (id == ProviderId.AZURE_OPENAI) AppTextField(deployment, { deployment = it }, s[K.PROV_DEPLOYMENT])
        SecondaryButton(s[K.SAVE], { saveCfg { it.copy(model = model.trim().ifBlank { null }, baseUrl = cleanBase(), deployment = deployment.trim().ifBlank { null }) } }, Modifier.fillMaxWidth())
        PrimaryButton(if (testing) s[K.PROV_TESTING] else s[K.PROV_TEST], { scope.launch { test() } }, Modifier.fillMaxWidth(), loading = testing, enabled = cfg.enabled && !testing)
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
    app.services.materials.transcription?.let { t ->
        var ready by remember { mutableStateOf(t.isModelReady()) }
        SectionHeader(s[K.TRANSCRIBE_MODEL_TITLE])
        PolyCard {
            Text(s.f(if (ready) K.TRANSCRIBE_MODEL_READY else K.TRANSCRIBE_MODEL_ABSENT, (t.modelSizeBytes / 1_000_000).toInt()), style = MaterialTheme.typography.bodyMedium)
            if (ready) QuietButton(s[K.TRANSCRIBE_MODEL_DELETE], { t.deleteModel(); ready = t.isModelReady() })
        }
    }
    SectionHeader(s[K.LOCAL_DEVICE])
    PolyCard {
        Text(when (DeviceCapability.tier(info)) { DeviceTier.LOW -> s[K.LOCAL_TIER_LOW]; DeviceTier.MID -> s[K.LOCAL_TIER_MID]; DeviceTier.HIGH -> s[K.LOCAL_TIER_HIGH] }, style = MaterialTheme.typography.titleSmall)
        Text(s.f(K.LOCAL_DEVICE_INFO, info.totalRamMb, info.availableStorageMb, info.cpuCores), style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
    }
    val runtime = app.services.platform.localLlm
    val scope = rememberCoroutineScope()
    val configs by remember { app.services.store.configs }.collectAsState(emptyMap())
    val preferred by remember { app.services.store.preferredProvider }.collectAsState(null)
    // Bumped after a download or deletion so the cards re-read what is on the phone.
    var version by remember { mutableIntStateOf(0) }
    var progress by remember { mutableStateOf<Pair<String, Float>?>(null) }
    var confirm by remember { mutableStateOf<com.polyglot.academy.core.localai.LocalModelSpec?>(null) }
    var tryResult by remember { mutableStateOf<Pair<String, String>?>(null) }
    var trying by remember { mutableStateOf<String?>(null) }
    val chosen = configs[ProviderId.ON_DEVICE]?.model
    Banner(BannerKind.WARNING, s[K.LOCAL_EXPERIMENTAL], title = s[K.LOCAL_EXPERIMENTAL_TITLE])
    LocalModelCatalog.models.forEach { m ->
        val comp = LocalModelCatalog.compatibility(m, info, setOfNotNull(runtime?.runtime))
        val downloaded = remember(version) { runtime?.isDownloaded(m) == true }
        val inUse = downloaded && chosen == m.id && preferred == ProviderId.ON_DEVICE
        PolyCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(m.displayName, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                if (inUse) Pill(s[K.LOCAL_IN_USE])
            }
            Text(s.f(K.LOCAL_MODEL_SIZE, m.downloadSizeMb, m.minRamMb, m.license), style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
            Text(m.languagesNote, style = MaterialTheme.typography.bodySmall, color = c.textMuted)
            // Memory and storage are warnings for a downloaded model; a missing runtime blocks everything.
            if (comp is Compatibility.Incompatible) comp.reasons.forEach { r -> Text("• " + s[K.valueOf("LOCAL_REASON_${r.name}")], style = MaterialTheme.typography.bodySmall, color = c.warning) }
            val blocked = comp is Compatibility.Incompatible && IncompatibilityReason.RUNTIME_NOT_BUNDLED in comp.reasons
            when {
                runtime == null || blocked -> {}
                progress?.first == m.id -> {
                    val f = progress!!.second
                    Text(s.f(K.TRANSCRIBE_DOWNLOADING, (f * 100).toInt()))
                    LinearProgressIndicator(progress = { f }, modifier = Modifier.fillMaxWidth(), drawStopIndicator = {})
                }
                !downloaded -> {
                    SecondaryButton(s.f(K.LOCAL_DOWNLOAD, m.downloadSizeMb), { confirm = m }, Modifier.fillMaxWidth(), enabled = progress == null)
                    // A failed download says so here (it resumes where it stopped when tried again).
                    tryResult?.takeIf { it.first == m.id }?.let { Text(it.second, style = MaterialTheme.typography.bodySmall, color = c.warning) }
                }
                else -> {
                    if (!inUse) PrimaryButton(s[K.LOCAL_USE], {
                        scope.launch {
                            val cfg = configs[ProviderId.ON_DEVICE] ?: com.polyglot.academy.core.ai.ProviderConfig(ProviderId.ON_DEVICE)
                            app.services.providers.saveConfig(cfg.copy(model = m.id, enabled = true))
                            app.services.store.setPreferred(ProviderId.ON_DEVICE)
                        }
                    }, Modifier.fillMaxWidth())
                    SecondaryButton(if (trying == m.id) s[K.PROV_TESTING] else s[K.LOCAL_TRY], {
                        trying = m.id; tryResult = null
                        scope.launch {
                            val cfg = configs[ProviderId.ON_DEVICE] ?: com.polyglot.academy.core.ai.ProviderConfig(ProviderId.ON_DEVICE)
                            if (cfg.model != m.id) app.services.providers.saveConfig(cfg.copy(model = m.id, enabled = true))
                            val started = app.services.clock.nowMillis()
                            val r = app.services.providers.testConnection(ProviderId.ON_DEVICE)
                            val seconds = ((app.services.clock.nowMillis() - started) / 1000).toInt()
                            tryResult = m.id to (if (r.ok) s.plural(K.LOCAL_TRY_OK, seconds) else s.f(K.PROV_TEST_FAILED, r.error?.message.orEmpty()))
                            trying = null
                        }
                    }, Modifier.fillMaxWidth(), enabled = trying == null)
                    tryResult?.takeIf { it.first == m.id }?.let { Text(it.second, style = MaterialTheme.typography.bodySmall) }
                    QuietButton(s[K.LOCAL_DELETE], {
                        runtime.delete(m)
                        if (inUse) scope.launch { app.services.store.setPreferred(null) }
                        version++
                    }, color = c.error)
                }
            }
        }
    }
    confirm?.let { m ->
        ConfirmDialog(s[K.LOCAL_CONFIRM_TITLE], s.f(K.LOCAL_CONFIRM_BODY, m.downloadSizeMb, m.displayName), s.f(K.LOCAL_DOWNLOAD, m.downloadSizeMb), s[K.CANCEL],
            onConfirm = {
                confirm = null
                scope.launch {
                    progress = m.id to 0f
                    val r = runtime!!.download(m) { progress = m.id to it }
                    progress = null
                    version++
                    if (r.isFailure) tryResult = m.id to s[K.TRANSCRIBE_DOWNLOAD_FAILED]
                }
            }, onDismiss = { confirm = null })
    }
    if (runtime != null) Text(s[K.LOCAL_QUALITY], style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
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
    SyncSection()
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
    // Reports of content errors stay here until the learner exports them for the reviewers.
    val reports = app.settings.contentReports
    SectionHeader(s[K.SET_REPORTS])
    Text(if (reports.isEmpty()) s[K.SET_REPORTS_NONE] else s.plural(K.SET_REPORTS_COUNT, reports.size), style = MaterialTheme.typography.bodySmall, color = PolyglotTheme.colors.textSecondary)
    reports.take(5).forEach { r ->
        Text("• ${com.polyglot.academy.ui.feature.common.issueLabel(s, r.issue)} · ${r.itemText}" + (if (r.note.isNotBlank()) " — ${r.note}" else ""),
            style = MaterialTheme.typography.bodySmall)
    }
    if (reports.isNotEmpty()) {
        // The learner sees what the file holds before it is written; the app never sends it.
        SecondaryButton(s[K.SET_REPORTS_EXPORT], { confirm = K.SET_REPORTS_EXPORT }, Modifier.fillMaxWidth())
        SecondaryButton(s[K.SET_REPORTS_DELETE], { confirm = K.SET_REPORTS_DELETE }, Modifier.fillMaxWidth())
    }
    // For a trial: the app has no server or analytics, so testers can keep and send screen counts themselves.
    val usage = app.settings.usageLog
    SectionHeader(s[K.USAGE_TITLE])
    Text(s[K.USAGE_SUB], style = MaterialTheme.typography.bodySmall, color = PolyglotTheme.colors.textSecondary)
    PolyCard(contentPadding = 0.dp) { SwitchRow(s[K.USAGE_SWITCH], usage.enabled, { on -> scope.launch { app.services.usage.setEnabled(on) } }) }
    if (usage.enabled) {
        val since = usage.startedAtMillis?.let { it.toLocalDate().toString() }.orEmpty()
        Text(s.plural(K.USAGE_STATUS, usage.days.size, since), style = MaterialTheme.typography.bodySmall, color = PolyglotTheme.colors.textSecondary)
        SecondaryButton(s[K.USAGE_EXPORT], {
            scope.launch {
                val results = Languages.all.flatMap { app.services.store.assessments(it.code).first() }
                val json = app.services.usage.exportJson(results)
                val where = app.platformUi.saveExport("polyglot-usage-log.json", json)
                toast(where?.let { s.f(K.SET_EXPORT_SAVED_TO, it) } ?: s.f(K.SET_EXPORT_DONE, "${json.length / 1024 + 1} KB"))
            }
        }, Modifier.fillMaxWidth())
    }
    SectionHeader(s[K.SET_RESTORE])
    Text(s[K.SET_RESTORE_SUB], style = MaterialTheme.typography.bodySmall, color = PolyglotTheme.colors.textSecondary)
    SecondaryButton(s[K.SET_RESTORE], { confirm = K.SET_RESTORE }, Modifier.fillMaxWidth(), enabled = app.platformUi.canPickFiles)
    if (!app.platformUi.canPickFiles) Text(s[K.SET_RESTORE_UNAVAILABLE], style = MaterialTheme.typography.bodySmall, color = PolyglotTheme.colors.textSecondary)
    SectionHeader(s[K.DELETE])
    SecondaryButton(s[K.SET_DELETE_CONV], { confirm = K.SET_DELETE_CONV }, Modifier.fillMaxWidth())
    SecondaryButton(s[K.SET_DELETE_MATERIALS], { confirm = K.SET_DELETE_MATERIALS }, Modifier.fillMaxWidth())
    SecondaryButton(s[K.SET_DELETE_ALL], { confirm = K.SET_DELETE_ALL }, Modifier.fillMaxWidth())
    if (confirm == K.SET_REPORTS_EXPORT) {
        ConfirmDialog(s[K.SET_REPORTS_EXPORT], s[K.SET_REPORTS_EXPORT_BODY], s[K.SET_REPORTS_EXPORT_CONFIRM], s[K.CANCEL],
            onConfirm = {
                confirm = null
                scope.launch {
                    val tsv = app.services.contentReports.exportTsv()
                    val where = app.platformUi.saveExport("polyglot-content-reports.tsv", tsv)
                    toast(where?.let { s.f(K.SET_EXPORT_SAVED_TO, it) } ?: s.f(K.SET_EXPORT_DONE, "${tsv.length / 1024 + 1} KB"))
                }
            }, onDismiss = { confirm = null })
    }
    if (confirm == K.SET_REPORTS_DELETE) {
        ConfirmDialog(s[K.SET_REPORTS_DELETE], s[K.SET_REPORTS_DELETE_BODY], s[K.DELETE], s[K.CANCEL], destructive = true,
            onConfirm = { confirm = null; scope.launch { app.services.contentReports.clear() } }, onDismiss = { confirm = null })
    }
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
    // Only the three delete buttons: any other key (reports, restore) has its own dialog above. Before 2026-11-07 this
    // also opened over "Delete reports" with a button that deleted everything; found by the export test below.
    confirm?.takeIf { it in setOf(K.SET_DELETE_CONV, K.SET_DELETE_MATERIALS, K.SET_DELETE_ALL) }?.let { what ->
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
