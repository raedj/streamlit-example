package com.polyglot.academy.ui.feature.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.polyglot.academy.core.model.CefrLevel
import com.polyglot.academy.core.model.CorrectionIntensity
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.LanguageEnrollment
import com.polyglot.academy.core.model.Languages
import com.polyglot.academy.core.model.LearningGoal
import com.polyglot.academy.core.model.LearningStyle
import com.polyglot.academy.core.model.PrivacyConsent
import com.polyglot.academy.core.planner.StudyPlanner
import com.polyglot.academy.core.tutor.TutorRoster
import com.polyglot.academy.ui.app.LocalApp
import com.polyglot.academy.ui.designsystem.AppTextField
import com.polyglot.academy.ui.designsystem.Banner
import com.polyglot.academy.ui.designsystem.BannerKind
import com.polyglot.academy.ui.designsystem.ChipGroup
import com.polyglot.academy.ui.designsystem.ChoiceCard
import com.polyglot.academy.ui.designsystem.HeroIllustration
import com.polyglot.academy.ui.designsystem.HeroKind
import com.polyglot.academy.ui.designsystem.PolyCard
import com.polyglot.academy.ui.designsystem.PolyglotTheme
import com.polyglot.academy.ui.designsystem.PrimaryButton
import com.polyglot.academy.ui.designsystem.QuietButton
import com.polyglot.academy.ui.designsystem.SecondaryButton
import com.polyglot.academy.ui.designsystem.SwitchRow
import com.polyglot.academy.ui.designsystem.TutorAvatar
import com.polyglot.academy.ui.i18n.K
import com.polyglot.academy.ui.i18n.Labels
import com.polyglot.academy.ui.i18n.LocalStrings
import com.polyglot.academy.ui.navigation.Route
import kotlinx.coroutines.launch

private enum class Step { WELCOME, INTERFACE, NATIVE, LEARN, VARIANT, LEVEL, GOALS, TIME, STYLE, VOICE, PRIVACY, REMINDER, PLAN }

/** First-run setup. Every choice is saved only at the end, and can be changed later in Settings. */
@Composable
fun OnboardingFlow() {
    val app = LocalApp.current
    val s = LocalStrings.current
    val scope = rememberCoroutineScope()
    var stepIndex by rememberSaveable { mutableIntStateOf(0) }
    var nativeLang by rememberSaveable { mutableStateOf(LanguageCode.ARABIC) }
    var learnLang by rememberSaveable { mutableStateOf(LanguageCode.ENGLISH) }
    var variant by rememberSaveable { mutableStateOf(Languages.ENGLISH.defaultVariantId) }
    var takeTest by rememberSaveable { mutableStateOf(true) }
    var selfLevel by rememberSaveable { mutableStateOf(CefrLevel.A1) }
    var goals by rememberSaveable { mutableStateOf(setOf<LearningGoal>()) }
    var minutes by rememberSaveable { mutableIntStateOf(15) }
    var style by rememberSaveable { mutableStateOf(LearningStyle.MIXED) }
    var correction by rememberSaveable { mutableStateOf(CorrectionIntensity.BALANCED) }
    var allowCloud by rememberSaveable { mutableStateOf(false) }
    var reminder by rememberSaveable { mutableStateOf(false) }
    var reminderHour by rememberSaveable { mutableIntStateOf(19) }
    var name by rememberSaveable { mutableStateOf("") }
    var skipped by rememberSaveable { mutableStateOf(setOf<String>()) }
    var saving by rememberSaveable { mutableStateOf(false) }

    val steps = Step.entries.filter { it != Step.VARIANT || Languages.of(learnLang).variants.size > 1 }
    val step = steps[stepIndex.coerceIn(0, steps.lastIndex)]
    val optional = step in setOf(Step.GOALS, Step.TIME, Step.STYLE, Step.VOICE, Step.REMINDER)
    fun next() { if (stepIndex < steps.lastIndex) stepIndex++ }
    fun back() { if (stepIndex > 0) stepIndex-- }

    fun finish() {
        if (saving) return
        saving = true
        scope.launch {
            val services = app.services
            val enrollment = if (takeTest) null else LanguageEnrollment(learnLang, variant, selfLevel, levelFromAssessment = false)
            val tutor = TutorRoster.defaultFor(learnLang, if (takeTest) CefrLevel.A1 else selfLevel)
            services.store.updateSettings { st ->
                st.copy(
                    privacy = st.privacy.copy(cloudAiEnabled = allowCloud, consentVersionAccepted = if (allowCloud) PrivacyConsent.CURRENT_VERSION else st.privacy.consentVersionAccepted),
                    reminders = st.reminders.copy(enabled = reminder, hour = reminderHour, minute = 0),
                )
            }
            if (reminder) {
                val granted = app.platformUi.requestNotificationPermission()
                services.platform.scheduleReminder(granted, reminderHour, 0, setOf(1, 2, 3, 4, 5, 6, 7))
            }
            if (takeTest) app.nav.push(Route.Assessment(learnLang, fromOnboarding = true))
            services.store.updateProfile { p ->
                p.copy(
                    displayName = name.trim(), interfaceLanguage = s.language, nativeLanguage = nativeLang,
                    explanationLanguage = if (s.language == LanguageCode.ARABIC || nativeLang == LanguageCode.ARABIC) LanguageCode.ARABIC else LanguageCode.ENGLISH,
                    activeLearningLanguage = learnLang,
                    learningLanguages = listOfNotNull(enrollment) + p.learningLanguages.filter { it.language != learnLang },
                    goals = goals, dailyMinutes = minutes, learningStyle = style, correctionIntensity = correction,
                    preferredTutorId = tutor.id, onboardingCompleted = true, skippedOnboardingSteps = skipped,
                )
            }
        }
    }

    val c = PolyglotTheme.colors
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding()) {
        if (step != Step.WELCOME) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(s.f(K.ONB_STEP, stepIndex, steps.size - 1), style = MaterialTheme.typography.labelMedium, color = c.textSecondary)
                LinearProgressIndicator(progress = { stepIndex.toFloat() / (steps.size - 1) }, modifier = Modifier.fillMaxWidth(), color = c.primary, trackColor = c.progressTrack, drawStopIndicator = {})
            }
        }
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
            Column(
                Modifier.widthIn(max = 640.dp).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                when (step) {
                    Step.WELCOME -> Welcome()
                    Step.INTERFACE -> {
                        Header(s[K.ONB_INTERFACE_TITLE], s[K.ONB_INTERFACE_BODY])
                        Languages.interfaceLanguages.forEach { lp ->
                            ChoiceCard(lp.nativeName, s.language == lp.code, onClick = {
                                scope.launch { app.services.store.updateProfile { it.copy(interfaceLanguage = lp.code) } }
                            }, subtitle = lp.displayName(s.language).takeIf { it != lp.nativeName })
                        }
                    }
                    Step.NATIVE -> {
                        Header(s[K.ONB_NATIVE_TITLE], s[K.ONB_NATIVE_BODY])
                        Languages.all.forEach { lp ->
                            ChoiceCard(lp.displayName(s.language), nativeLang == lp.code, onClick = {
                                nativeLang = lp.code
                                if (learnLang == lp.code) learnLang = Languages.all.first { it.code != lp.code }.code.also { variant = Languages.of(it).defaultVariantId }
                            }, subtitle = lp.nativeName)
                        }
                    }
                    Step.LEARN -> {
                        Header(s[K.ONB_LEARN_TITLE], s[K.ONB_LEARN_BODY])
                        Languages.all.filter { it.code != nativeLang }.forEach { lp ->
                            ChoiceCard(lp.displayName(s.language), learnLang == lp.code, onClick = {
                                learnLang = lp.code; variant = lp.defaultVariantId
                            }, subtitle = lp.nativeName)
                        }
                    }
                    Step.VARIANT -> {
                        Header(s[K.ONB_VARIANT_TITLE], s[K.ONB_VARIANT_BODY])
                        Languages.of(learnLang).variants.forEach { v ->
                            ChoiceCard(if (s.language == LanguageCode.ARABIC) v.arabicName else v.englishName, variant == v.id, onClick = { variant = v.id })
                        }
                    }
                    Step.LEVEL -> {
                        Header(s.f(K.ONB_LEVEL_TITLE, Labels.language(s, learnLang)), s[K.ONB_LEVEL_BODY])
                        ChoiceCard(s[K.ONB_LEVEL_TEST], takeTest, onClick = { takeTest = true }, subtitle = s[K.ONB_LEVEL_TEST_SUB])
                        ChoiceCard(s[K.ONB_LEVEL_SELF], !takeTest, onClick = { takeTest = false })
                        if (!takeTest) {
                            Languages.of(learnLang).supportedLevels.forEach { l ->
                                ChoiceCard("${l.label} · ${Labels.level(s, l)}", selfLevel == l, onClick = { selfLevel = l })
                            }
                        }
                        Text(s[K.ONB_LEVEL_NOTE], style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                    }
                    Step.GOALS -> {
                        Header(s[K.ONB_GOALS_TITLE], s[K.ONB_GOALS_BODY])
                        LearningGoal.entries.forEach { g ->
                            ChoiceCard(Labels.goal(s, g), g in goals, onClick = { goals = if (g in goals) goals - g else goals + g }, multiSelect = true)
                        }
                    }
                    Step.TIME -> {
                        Header(s[K.ONB_TIME_TITLE], s[K.ONB_TIME_BODY])
                        listOf(5 to K.ONB_TIME_5, 10 to K.ONB_TIME_10, 15 to K.ONB_TIME_15, 30 to K.ONB_TIME_30, 60 to K.ONB_TIME_60).forEach { (m, k) ->
                            ChoiceCard(s.plural(K.ONB_TIME_OPTION, m),minutes == m, onClick = { minutes = m }, subtitle = s[k])
                        }
                    }
                    Step.STYLE -> {
                        Header(s[K.ONB_STYLE_TITLE], s[K.ONB_STYLE_BODY])
                        LearningStyle.entries.forEach { st -> ChoiceCard(Labels.style(s, st), style == st, onClick = { style = st }, subtitle = Labels.styleDesc(s, st)) }
                        Spacer(Modifier.height(8.dp))
                        Text(s[K.ONB_CORRECTION_TITLE], style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
                        CorrectionIntensity.entries.forEach { ci ->
                            ChoiceCard(Labels.correction(s, ci), correction == ci, onClick = { correction = ci }, subtitle = Labels.correctionDesc(s, ci))
                        }
                    }
                    Step.VOICE -> {
                        HeroIllustration(HeroKind.MICROPHONE, s[K.ILLUSTRATION], Modifier.align(Alignment.CenterHorizontally), size = 140.dp)
                        Header(s[K.ONB_VOICE_TITLE], s[K.ONB_VOICE_BODY])
                        listOf(K.ONB_VOICE_POINT_1, K.ONB_VOICE_POINT_2, K.ONB_VOICE_POINT_3).forEach { Bullet(s[it]) }
                        Banner(BannerKind.INFO, s[K.ONB_VOICE_LATER])
                    }
                    Step.PRIVACY -> {
                        HeroIllustration(HeroKind.PRIVACY, s[K.ILLUSTRATION], Modifier.align(Alignment.CenterHorizontally), size = 130.dp)
                        Header(s[K.ONB_PRIVACY_TITLE], s[K.ONB_PRIVACY_BODY])
                        ChoiceCard(s[K.ONB_PRIVACY_LOCAL], !allowCloud, onClick = { allowCloud = false }, subtitle = s[K.ONB_PRIVACY_LOCAL_SUB])
                        ChoiceCard(s[K.ONB_PRIVACY_CLOUD], allowCloud, onClick = { allowCloud = true }, subtitle = s[K.ONB_PRIVACY_CLOUD_SUB])
                        Banner(BannerKind.INFO, s[K.ONB_PRIVACY_NOTE])
                    }
                    Step.REMINDER -> {
                        Header(s[K.ONB_NOTIF_TITLE], s[K.ONB_NOTIF_BODY])
                        PolyCard(contentPadding = 0.dp) { SwitchRow(s[K.ONB_NOTIF_ENABLE], reminder, { reminder = it }) }
                        if (reminder) {
                            Text(s[K.ONB_NOTIF_TIME], style = MaterialTheme.typography.titleSmall)
                            ChipGroup(listOf(7, 8, 12, 18, 19, 20, 21), setOf(reminderHour), { h -> "${h.toString().padStart(2, '0')}:00" }, { reminderHour = it })
                        }
                    }
                    Step.PLAN -> {
                        HeroIllustration(HeroKind.PLAN, s[K.ILLUSTRATION], Modifier.align(Alignment.CenterHorizontally), size = 130.dp)
                        Header(s[K.ONB_PLAN_TITLE], s[K.ONB_PLAN_BODY])
                        val tutor = TutorRoster.defaultFor(learnLang, if (takeTest) CefrLevel.A1 else selfLevel)
                        PolyCard {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TutorAvatar(tutor.avatar, s.f(K.TUTOR_AVATAR_DESC, tutor.name), size = 56.dp)
                                Spacer(Modifier.size(12.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(s.f(K.ONB_PLAN_LANGUAGE, Labels.language(s, learnLang)), style = MaterialTheme.typography.titleMedium)
                                    Text(if (takeTest) s[K.ONB_PLAN_LEVEL_PENDING] else s.f(K.ONB_PLAN_LEVEL, "${selfLevel.label} · ${Labels.level(s, selfLevel)}"), style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
                                    Text(s.f(K.ONB_PLAN_DAILY, s.plural(K.MINUTES, minutes)), style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
                                    Text(s.f(K.ONB_PLAN_TUTOR, tutor.name), style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
                                }
                            }
                        }
                        Banner(BannerKind.INFO, s.f(K.ONB_PLAN_ESTIMATE, StudyPlanner.estimatedWeeksPerLevel(minutes, (if (takeTest) CefrLevel.A1 else selfLevel).ordinal)))
                        AppTextField(name, { name = it.take(40) }, s[K.ONB_NAME_LABEL])
                    }
                }
            }
        }
        // Bottom actions stay reachable above the keyboard and system bars.
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            when (step) {
                Step.WELCOME -> PrimaryButton(s[K.ONB_GET_STARTED], { next() }, Modifier.fillMaxWidth().widthIn(max = 480.dp))
                Step.PLAN -> PrimaryButton(if (takeTest) s[K.ONB_START_TEST] else s[K.ONB_START_LEARNING], { finish() }, Modifier.fillMaxWidth(), loading = saving)
                else -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SecondaryButton(s[K.BACK], { back() }, Modifier.weight(1f))
                    PrimaryButton(s[K.CONTINUE], { next() }, Modifier.weight(2f))
                }
            }
            if (optional) QuietButton(s[K.SKIP_FOR_NOW], { skipped = skipped + step.name; next() })
        }
    }
}

@Composable
private fun Welcome() {
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    Column(Modifier.fillMaxWidth().padding(top = 24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        HeroIllustration(HeroKind.WELCOME, s[K.ILLUSTRATION], size = 200.dp)
        Text(s[K.APP_NAME], style = MaterialTheme.typography.labelLarge, color = c.primary)
        Text(s[K.ONB_WELCOME_TITLE], style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center, modifier = Modifier.semantics { heading() })
        Text(s[K.ONB_WELCOME_BODY], style = MaterialTheme.typography.bodyLarge, color = c.textSecondary, textAlign = TextAlign.Center)
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(K.ONB_WELCOME_POINT_1, K.ONB_WELCOME_POINT_2, K.ONB_WELCOME_POINT_3).forEach { Bullet(s[it]) }
        }
        // Interface language can be switched right here too.
        val app = LocalApp.current
        val scope = rememberCoroutineScope()
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Languages.interfaceLanguages.forEach { lp ->
                QuietButton(lp.nativeName, { scope.launch { app.services.store.updateProfile { it.copy(interfaceLanguage = lp.code) } } },
                    color = if (s.language == lp.code) c.primary else c.textSecondary)
            }
        }
    }
}

@Composable
private fun Header(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.semantics { heading() })
        Text(body, style = MaterialTheme.typography.bodyLarge, color = PolyglotTheme.colors.textSecondary)
    }
}

@Composable
internal fun Bullet(text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(Icons.Filled.CheckCircle, null, tint = PolyglotTheme.colors.success, modifier = Modifier.size(20.dp).padding(top = 2.dp))
        Spacer(Modifier.size(10.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = PolyglotTheme.colors.textPrimary)
    }
}
