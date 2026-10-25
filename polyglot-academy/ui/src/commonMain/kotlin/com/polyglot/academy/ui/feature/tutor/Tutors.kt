package com.polyglot.academy.ui.feature.tutor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.polyglot.academy.core.content.Curriculum
import com.polyglot.academy.core.model.CefrLevel
import com.polyglot.academy.core.model.CorrectionIntensity
import com.polyglot.academy.core.model.CourseUnit
import com.polyglot.academy.core.model.RolePlayScenario
import com.polyglot.academy.core.tutor.TutorPersona
import com.polyglot.academy.core.tutor.TutorRoster
import com.polyglot.academy.ui.app.LocalApp
import com.polyglot.academy.ui.app.ScrollScreen
import com.polyglot.academy.ui.designsystem.ChoiceCard
import com.polyglot.academy.ui.designsystem.LevelBadge
import com.polyglot.academy.ui.designsystem.Pill
import com.polyglot.academy.ui.designsystem.PolyCard
import com.polyglot.academy.ui.designsystem.PolyglotTheme
import com.polyglot.academy.ui.designsystem.PrimaryButton
import com.polyglot.academy.ui.designsystem.SecondaryButton
import com.polyglot.academy.ui.designsystem.SectionHeader
import com.polyglot.academy.ui.designsystem.StateMessage
import com.polyglot.academy.ui.designsystem.SwitchRow
import com.polyglot.academy.ui.designsystem.TutorAvatar
import com.polyglot.academy.ui.i18n.K
import com.polyglot.academy.ui.i18n.Labels
import com.polyglot.academy.ui.i18n.LocalStrings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.polyglot.academy.core.service.ConversationController
import com.polyglot.academy.core.tutor.TutorPromptBuilder
import com.polyglot.academy.ui.designsystem.AppTextField
import com.polyglot.academy.ui.designsystem.ChipGroup
import com.polyglot.academy.ui.navigation.Route
import com.polyglot.academy.ui.navigation.Tab
import kotlinx.coroutines.launch

@Composable
fun TutorsScreen() {
    val app = LocalApp.current
    val s = LocalStrings.current
    val lang = app.profile.activeLearningLanguage
    ScrollScreen(s[K.TUTORS_TITLE], showBack = false, tab = Tab.TUTORS) {
        Text(s.f(K.TUTORS_FOR, Labels.language(s, lang)), style = MaterialTheme.typography.bodyLarge, color = PolyglotTheme.colors.textSecondary)
        FreeTalkCard()
        TutorRoster.forLanguage(lang).forEach { t -> TutorCard(t, selected = t.id == app.profile.preferredTutorId) { app.nav.push(Route.TutorDetail(t.id)) } }
        Text(s[K.TUTOR_AVATAR_NOTE], style = MaterialTheme.typography.bodySmall, color = PolyglotTheme.colors.textMuted)
    }
}

/** Start a conversation with the AI tutor about any topic the learner types or picks. */
@Composable
private fun FreeTalkCard() {
    val app = LocalApp.current
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val lang = app.profile.activeLearningLanguage
    var topic by rememberSaveable { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val suggestions = (1..8).map { s[K.valueOf("FREE_TOPIC_$it")] }
    PolyCard {
        Text(s[K.FREE_TALK_TITLE], style = MaterialTheme.typography.titleMedium)
        Text(s[K.FREE_TALK_BODY], style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
        Text(s[K.COACH_TRACK_LABEL], style = MaterialTheme.typography.titleSmall)
        ChipGroup(com.polyglot.academy.core.model.CoachTrack.entries, setOfNotNull(app.profile.coachTrack), { Labels.coachTrack(s, it, lang) }, { t ->
            scope.launch { app.services.store.updateProfile { it.copy(coachTrack = if (it.coachTrack == t) null else t) } }
        })
        AppTextField(topic, { topic = it.take(120) }, s[K.FREE_TALK_TOPIC_LABEL])
        ChipGroup(suggestions, setOf(topic), { it }, { topic = it })
        PrimaryButton(s[K.FREE_TALK_START], {
            val tutor = TutorRoster.forLanguage(lang).let { list -> list.firstOrNull { it.id == app.profile.preferredTutorId } ?: list.first() }
            val key = TutorPromptBuilder.cleanTopic(topic)?.let { ConversationController.TOPIC_PREFIX + it }
            app.nav.push(Route.Conversation(tutor.id, key))
        }, Modifier.fillMaxWidth())
        Text(s[K.FREE_TALK_NOTE], style = MaterialTheme.typography.bodySmall, color = c.textMuted)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TutorCard(t: TutorPersona, selected: Boolean, onClick: () -> Unit) {
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    PolyCard(onClick = onClick, containerColor = if (selected) c.primaryContainer.copy(alpha = if (c.isDark) 0.35f else 0.5f) else c.card) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TutorAvatar(t.avatar, s.f(K.TUTOR_AVATAR_DESC, t.name), size = 64.dp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(t.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f, fill = false))
                    if (selected) { Spacer(Modifier.width(8.dp)); Pill(s[K.TUTOR_SELECTED], container = c.primary, content = c.onPrimary) }
                }
                Text(s.gloss(t.tagline), style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Pill(Labels.teachingStyle(s, t.style))
                    Pill(s.f(K.TUTOR_LEVELS, t.minLevel.label, t.maxLevel.label))
                    if (t.languages.isEmpty()) Pill(s[K.TUTORS_ALL_LANGUAGES])
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TutorDetailScreen(tutorId: String) {
    val app = LocalApp.current
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val scope = rememberCoroutineScope()
    val tutor = TutorRoster.byId(tutorId)
    ScrollScreen(tutor?.name ?: s[K.TUTORS_TITLE]) {
        if (tutor == null) { StateMessage(s[K.ERROR_TITLE], s[K.ERROR_BODY]); return@ScrollScreen }
        val lang = app.profile.activeLearningLanguage
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TutorAvatar(tutor.avatar, s.f(K.TUTOR_AVATAR_DESC, tutor.name), size = 120.dp)
            Text(tutor.name, style = MaterialTheme.typography.headlineSmall)
            Text(s.gloss(tutor.tagline), style = MaterialTheme.typography.bodyLarge, color = c.textSecondary)
        }
        PolyCard {
            Text(s.gloss(tutor.bio), style = MaterialTheme.typography.bodyLarge)
            Text(Labels.correctionStrategy(s, tutor.correction), style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
            Text(s[K.TUTOR_FOCUS], style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { tutor.focusAreas.forEach { Pill(s.gloss(it)) } }
        }
        val isSelected = app.profile.preferredTutorId == tutor.id
        if (!isSelected) SecondaryButton(s.f(K.TUTOR_SELECT, tutor.name), {
            scope.launch { app.services.store.updateProfile { it.copy(preferredTutorId = tutor.id) } }
        }, Modifier.fillMaxWidth())
        PrimaryButton(s[K.TUTOR_START], { app.nav.push(Route.Conversation(tutor.id)) }, Modifier.fillMaxWidth(), enabled = tutor.teaches(lang))
        SectionHeader(s[K.TUTOR_SETTINGS])
        Text(s[K.TUTOR_CORRECTION_LEVEL], style = MaterialTheme.typography.titleSmall)
        CorrectionIntensity.entries.forEach { ci ->
            ChoiceCard(Labels.correction(s, ci), app.profile.correctionIntensity == ci, onClick = {
                scope.launch { app.services.store.updateProfile { it.copy(correctionIntensity = ci) } }
            }, subtitle = Labels.correctionDesc(s, ci))
        }
        PolyCard(contentPadding = 0.dp) {
            SwitchRow(s[K.TUTOR_SHOW_TRANSLATIONS], app.settings.showTranslationsByDefault, { v ->
                scope.launch { app.services.store.updateSettings { it.copy(showTranslationsByDefault = v) } }
            })
        }
        val scenarios = Curriculum.of(lang).scenarios.filter { it.trackId == CourseUnit.GENERAL_TRACK || tutor.id == "daniel" || tutor.id == "alex" }
        if (scenarios.isNotEmpty() && tutor.teaches(lang)) {
            SectionHeader(s[K.TUTOR_SCENARIOS])
            scenarios.forEach { sc -> ScenarioCard(sc) { app.nav.push(Route.Conversation(tutor.id, sc.id)) } }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ScenarioCard(sc: RolePlayScenario, onClick: () -> Unit) {
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    PolyCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(s.gloss(sc.title), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            LevelBadge(sc.level, null)
        }
        Text(s.gloss(sc.setting), style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
        Text(s.f(K.TUTOR_SCENARIO_GOAL, s.gloss(sc.learnerGoal)), style = MaterialTheme.typography.bodyMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { sc.usefulPhrases.take(4).forEach { Pill(it) } }
    }
}

@Composable
fun ScenariosScreen() {
    val app = LocalApp.current
    val s = LocalStrings.current
    val lang = app.profile.activeLearningLanguage
    val level = app.profile.enrollment(lang)?.startingLevel ?: CefrLevel.A1
    ScrollScreen(s[K.SCENARIOS_TITLE]) {
        Curriculum.of(lang).scenarios.sortedBy { kotlin.math.abs(it.level.ordinal - level.ordinal) }.forEach { sc ->
            val tutor = if (sc.trackId == CourseUnit.GENERAL_TRACK) TutorRoster.byId("alex")!! else TutorRoster.byId("daniel")!!
            ScenarioCard(sc) { app.nav.push(Route.Conversation(tutor.id, sc.id)) }
        }
    }
}
