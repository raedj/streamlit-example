package com.polyglot.academy.ui.feature.me

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.polyglot.academy.core.model.CefrLevel
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.LanguageEnrollment
import com.polyglot.academy.core.model.Languages
import com.polyglot.academy.core.model.LearningGoal
import com.polyglot.academy.core.util.toLocalDate
import com.polyglot.academy.ui.app.LocalApp
import com.polyglot.academy.ui.app.ScrollScreen
import com.polyglot.academy.ui.designsystem.AppTextField
import com.polyglot.academy.ui.designsystem.ChipGroup
import com.polyglot.academy.ui.designsystem.LevelBadge
import com.polyglot.academy.ui.designsystem.Pill
import com.polyglot.academy.ui.designsystem.PolyCard
import com.polyglot.academy.ui.designsystem.PolyglotTheme
import com.polyglot.academy.ui.designsystem.QuietButton
import com.polyglot.academy.ui.designsystem.SectionHeader
import com.polyglot.academy.ui.designsystem.StateMessage
import com.polyglot.academy.ui.feature.practice.HubRow
import com.polyglot.academy.ui.i18n.K
import com.polyglot.academy.ui.i18n.Labels
import com.polyglot.academy.ui.i18n.LocalStrings
import com.polyglot.academy.ui.navigation.Route
import com.polyglot.academy.ui.navigation.SettingsSection
import com.polyglot.academy.ui.navigation.Tab
import kotlinx.coroutines.launch

@Composable
fun MeScreen() {
    val app = LocalApp.current
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val scope = rememberCoroutineScope()
    val profile = app.profile
    var name by remember(profile.displayName) { mutableStateOf(profile.displayName) }
    ScrollScreen(s[K.ME_TITLE], showBack = false, tab = Tab.ME) {
        PolyCard {
            AppTextField(name, { name = it.take(40) }, s[K.PROFILE_NAME])
            if (name != profile.displayName) QuietButton(s[K.SAVE], { scope.launch { app.services.store.updateProfile { it.copy(displayName = name.trim()) } } })
        }
        SectionHeader(s[K.PROFILE_LANGUAGES])
        val enrolled = profile.learningLanguages.map { it.language }.toSet() + profile.activeLearningLanguage
        enrolled.forEach { code ->
            val e = profile.enrollment(code)
            PolyCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(Labels.language(s, code), style = MaterialTheme.typography.titleMedium)
                        Text(Languages.of(code).nativeName, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                    }
                    e?.let { LevelBadge(it.startingLevel, if (it.levelFromAssessment) s[K.HOME_LEVEL_ESTIMATE] else s[K.HOME_LEVEL_SELF]) }
                }
                if (code == profile.activeLearningLanguage) Pill(s[K.PROFILE_ACTIVE], container = c.primary, content = c.onPrimary)
                else QuietButton(s.f(K.PROFILE_MAKE_ACTIVE, Labels.language(s, code)), { scope.launch { app.services.store.updateProfile { it.copy(activeLearningLanguage = code) } } })
                if (e == null) QuietButton(s[K.ME_ASSESS], { app.nav.push(Route.Assessment(code)) })
            }
        }
        val available = Languages.all.map { it.code }.filter { it !in enrolled && it != profile.nativeLanguage }
        if (available.isNotEmpty()) {
            Text(s[K.PROFILE_ADD_LANGUAGE], style = MaterialTheme.typography.titleSmall)
            ChipGroup(available, emptySet(), { Labels.language(s, it) }, { code ->
                scope.launch {
                    app.services.store.updateProfile { p ->
                        p.copy(activeLearningLanguage = code, learningLanguages = p.learningLanguages + LanguageEnrollment(code, Languages.of(code).defaultVariantId, CefrLevel.A1, false))
                    }
                }
            })
        }
        SectionHeader(s[K.PROFILE_GOALS])
        ChipGroup(LearningGoal.entries, profile.goals, { Labels.goal(s, it) }, { g ->
            scope.launch { app.services.store.updateProfile { it.copy(goals = if (g in it.goals) it.goals - g else it.goals + g) } }
        })
        Spacer(Modifier.width(1.dp))
        HubRow(s[K.ME_PROGRESS], "", Icons.Filled.Insights, c.primary) { app.nav.push(Route.Progress) }
        HubRow(s[K.ME_PLAN], "", Icons.Filled.CalendarMonth, c.secondary) { app.nav.push(Route.Plan) }
        HubRow(s[K.ME_VOCAB], "", Icons.Filled.AutoStories, c.accent) { app.nav.push(Route.Vocabulary) }
        HubRow(s[K.ME_LIBRARY], "", Icons.Filled.FolderOpen, c.chartSeries[3]) { app.nav.push(Route.Library) }
        HubRow(s[K.ME_ASSESS], "", Icons.Filled.Assessment, c.chartSeries[4]) { app.nav.push(Route.Assessment(profile.activeLearningLanguage)) }
        HubRow(s[K.ME_HISTORY], "", Icons.Filled.History, c.chartSeries[5]) { app.nav.push(Route.History) }
        HubRow(s[K.ME_SEARCH], "", Icons.Filled.Search, c.primary) { app.nav.push(Route.Search) }
        HubRow(s[K.ME_SETTINGS], "", Icons.Filled.Settings, c.textSecondary) { app.nav.push(Route.Settings) }
        HubRow(s[K.ME_HELP], "", Icons.AutoMirrored.Filled.Help, c.info) { app.nav.push(Route.SettingsPage(SettingsSection.HELP)) }
    }
}

@Composable
fun HistoryScreen() {
    val app = LocalApp.current
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val lang = app.profile.activeLearningLanguage
    val sessions by remember(lang) { app.services.store.sessions(lang) }.collectAsState(emptyList())
    ScrollScreen(s[K.ME_HISTORY]) {
        val done = sessions.filter { it.completed }.sortedByDescending { it.startedAtMillis }
        if (done.isEmpty()) StateMessage(s[K.ME_HISTORY], s[K.HISTORY_EMPTY])
        done.groupBy { it.startedAtMillis.toLocalDate() }.forEach { (date, list) ->
            SectionHeader(date.toString())
            list.forEach { sess ->
                PolyCard {
                    Text(Labels.session(s, sess.kind), style = MaterialTheme.typography.titleSmall)
                    Text(s.f(K.SESSION_ITEMS, sess.itemsCorrect, sess.itemsCompleted, Labels.clockTime(sess.activeSeconds)), style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                }
            }
        }
    }
}

internal fun explainFor(lang: LanguageCode, preferred: LanguageCode) =
    preferred.takeIf { it != lang } ?: if (lang == LanguageCode.ENGLISH) LanguageCode.ARABIC else LanguageCode.ENGLISH
