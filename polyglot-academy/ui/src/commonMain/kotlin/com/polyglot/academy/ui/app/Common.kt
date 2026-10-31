package com.polyglot.academy.ui.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.polyglot.academy.ui.designsystem.ContentColumn
import com.polyglot.academy.ui.designsystem.HeroIllustration
import com.polyglot.academy.ui.designsystem.HeroKind
import com.polyglot.academy.ui.designsystem.LoadingPlaceholder
import com.polyglot.academy.ui.designsystem.PolyglotTheme
import com.polyglot.academy.ui.designsystem.ScreenScaffold
import com.polyglot.academy.ui.designsystem.StateMessage
import com.polyglot.academy.ui.i18n.K
import com.polyglot.academy.ui.i18n.LocalStrings
import com.polyglot.academy.ui.navigation.Tab
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** Renders the loading, error and ready states of a [Loader] consistently. */
@Composable
fun <T> LoadContent(loader: Loader<T>, modifier: Modifier = Modifier, content: @Composable (T) -> Unit) {
    val s = LocalStrings.current
    when (val st = loader.state) {
        Load.Loading -> ContentColumn(modifier.padding(top = 16.dp)) { LoadingPlaceholder(s[K.LOADING]) }
        is Load.Failed -> ContentColumn(modifier) {
            StateMessage(s[K.ERROR_TITLE], s[K.ERROR_BODY], illustration = { HeroIllustration(HeroKind.ERROR, s[K.ILLUSTRATION], size = 120.dp) },
                action = s[K.RETRY], onAction = loader::reload)
        }
        is Load.Ready -> content(st.value)
    }
}

/** Standard scrolling screen: top bar with back, constrained content width, bottom spacing. */
@Composable
fun ScrollScreen(
    title: String,
    showBack: Boolean = true,
    tab: Tab? = null,
    actions: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {},
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    val app = LocalApp.current
    val s = LocalStrings.current
    ScreenScaffold(
        title = title, onBack = if (showBack) ({ app.nav.pop() }) else null, backLabel = s[K.BACK], actions = actions,
        bottomBar = { if (tab != null) AppBottomBar(tab) },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            ContentColumn(padding = PaddingValues(top = 4.dp, bottom = 24.dp), content = content)
        }
    }
}

@Composable
fun AppBottomBar(current: Tab) {
    val app = LocalApp.current
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    NavigationBar(containerColor = c.surface) {
        listOf(
            Triple(Tab.HOME, Icons.Filled.Home, K.NAV_HOME),
            Triple(Tab.LEARN, Icons.Filled.School, K.NAV_LEARN),
            Triple(Tab.PRACTICE, Icons.Filled.FitnessCenter, K.NAV_PRACTICE),
            Triple(Tab.TUTORS, Icons.Filled.RecordVoiceOver, K.NAV_TUTORS),
            Triple(Tab.ME, Icons.Filled.Person, K.NAV_ME),
        ).forEach { (tab, icon, key) ->
            NavigationBarItem(
                selected = tab == current, onClick = { if (tab != current) app.nav.switchTab(tab) },
                icon = { Icon(icon, contentDescription = null) }, label = { Text(s[key], maxLines = 1) },
                colors = NavigationBarItemDefaults.colors(selectedIconColor = c.onPrimaryContainer, selectedTextColor = c.textPrimary, indicatorColor = c.primaryContainer),
            )
        }
    }
}

/** Shows a snackbar from any screen. */
fun AppScope.toast(scope: CoroutineScope, message: String) {
    scope.launch { snackbar.showSnackbar(message) }
}

@Composable
fun rememberToast(): (String) -> Unit {
    val app = LocalApp.current
    val scope = rememberCoroutineScope()
    return { msg -> app.toast(scope, msg) }
}

@Composable
fun VSpace(h: Int) = Spacer(Modifier.height(h.dp))

