package com.polyglot.academy.ui.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.polyglot.academy.core.model.AppSettings
import com.polyglot.academy.core.model.LearnerProfile
import com.polyglot.academy.core.service.AppServices
import com.polyglot.academy.ui.designsystem.PolyglotTheme
import com.polyglot.academy.ui.feature.onboarding.OnboardingFlow
import com.polyglot.academy.ui.i18n.LocalStrings
import com.polyglot.academy.ui.i18n.Localizer
import com.polyglot.academy.ui.navigation.Navigator
import com.polyglot.academy.ui.navigation.Route

/** System back integration (Android back gesture); no-op on platforms without one. */
@Composable
expect fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit)

/**
 * Root of the application UI. Profile and settings are observed from the database so every
 * change (language, theme, accessibility) applies immediately everywhere.
 */
@Composable
fun PolyglotApp(services: AppServices, platformUi: PlatformUi, initialStack: List<Route>? = null) {
    val profile by services.store.profile.collectAsState(initial = null as LearnerProfile?)
    val settings by services.store.settings.collectAsState(initial = null as AppSettings?)
    val p = profile; val st = settings
    val strings = remember(p?.interfaceLanguage) { Localizer(p?.interfaceLanguage ?: com.polyglot.academy.core.model.LanguageCode.ENGLISH) }
    PolyglotTheme(
        themeMode = st?.theme ?: com.polyglot.academy.core.model.ThemeMode.SYSTEM,
        rtl = strings.isRtl,
        highContrast = st?.accessibility?.highContrast == true,
        reducedMotion = st?.accessibility?.reducedMotion == true,
        textScale = if (st?.accessibility?.largerText == true) 1.15f else 1f,
    ) {
        CompositionLocalProvider(LocalStrings provides strings, com.polyglot.academy.ui.designsystem.LocalScriptFonts provides remember(platformUi) { com.polyglot.academy.ui.designsystem.ScriptFonts(urdu = platformUi.urduFont) }) {
            Box(Modifier.fillMaxSize().background(PolyglotTheme.colors.background)) {
                if (p != null && st != null) {
                    val nav = rememberSaveable(saver = Navigator.saver) { Navigator(initialStack ?: listOf(Route.TabRoot(com.polyglot.academy.ui.navigation.Tab.HOME))) }
                    val snackbar = remember { SnackbarHostState() }
                    val scope = AppScope(services, nav, platformUi, snackbar, p, st)
                    CompositionLocalProvider(LocalApp provides scope) {
                        if (!p.onboardingCompleted) OnboardingFlow() else MainShell(nav)
                        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 80.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun MainShell(nav: Navigator) {
    val reduced = PolyglotTheme.motion.reduced
    PlatformBackHandler(enabled = nav.canGoBack) { nav.pop() }
    AnimatedContent(
        targetState = nav.current,
        transitionSpec = {
            if (reduced) ContentTransform(fadeIn(tween(0)), fadeOut(tween(0)))
            else (fadeIn(tween(180)) + slideInHorizontally(tween(220)) { it / 12 }) togetherWith fadeOut(tween(120))
        },
        label = "route",
    ) { route -> RouteContent(route) }
}
