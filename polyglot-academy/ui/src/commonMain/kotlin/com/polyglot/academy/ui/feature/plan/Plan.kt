package com.polyglot.academy.ui.feature.plan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.polyglot.academy.core.content.Curriculum
import com.polyglot.academy.core.model.Skill
import com.polyglot.academy.core.planner.DailyPlan
import com.polyglot.academy.core.planner.PlanItem
import com.polyglot.academy.core.planner.PlanItemKind
import com.polyglot.academy.core.planner.StudyPlanner
import com.polyglot.academy.core.tutor.TutorRoster
import com.polyglot.academy.ui.app.LoadContent
import com.polyglot.academy.ui.app.LocalApp
import com.polyglot.academy.ui.app.ScrollScreen
import com.polyglot.academy.ui.app.rememberLoader
import com.polyglot.academy.ui.designsystem.Banner
import com.polyglot.academy.ui.designsystem.BannerKind
import com.polyglot.academy.ui.designsystem.ChipGroup
import com.polyglot.academy.ui.designsystem.PolyCard
import com.polyglot.academy.ui.designsystem.PolyglotTheme
import com.polyglot.academy.ui.designsystem.SecondaryButton
import com.polyglot.academy.ui.designsystem.SectionHeader
import com.polyglot.academy.ui.feature.home.skillRoute
import com.polyglot.academy.ui.i18n.K
import com.polyglot.academy.ui.i18n.Labels
import com.polyglot.academy.ui.i18n.LocalStrings
import com.polyglot.academy.ui.i18n.Localizer
import com.polyglot.academy.ui.navigation.Route
import kotlinx.coroutines.launch

@Composable
fun PlanScreen() {
    val app = LocalApp.current
    val s = LocalStrings.current
    val scope = rememberCoroutineScope()
    var override by remember { mutableStateOf<DailyPlan?>(null) }
    val loader = rememberLoader(app.profile.activeLearningLanguage, app.profile.dailyMinutes) { app.services.plans.today() }
    ScrollScreen(s[K.PLAN_TITLE]) {
        LoadContent(loader) { loaded ->
            val plan = override ?: loaded
            val c = PolyglotTheme.colors
            Text(s.f(K.PLAN_SUBTITLE, s.plural(K.MINUTES, plan.totalMinutes), s.plural(K.MINUTES, plan.completedMinutes)), style = MaterialTheme.typography.bodyLarge, color = c.textSecondary)
            if (plan.adjustedForMissedDay) Banner(BannerKind.INFO, s[K.PLAN_MISSED_NOTE])
            SectionHeader(s[K.PLAN_TODAY])
            plan.items.forEach { item ->
                PolyCard(onClick = { open(app, item) }, onClickLabel = s[K.PLAN_OPEN]) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = item.completed, onCheckedChange = { done ->
                            scope.launch { override = app.services.plans.setCompleted(item.id, done) }
                        })
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text(itemTitle(s, item), style = MaterialTheme.typography.titleSmall)
                            Text(s.plural(K.MINUTES, item.minutes), style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                        }
                    }
                }
            }
            SectionHeader(s[K.PLAN_DAILY_TIME])
            ChipGroup(listOf(5, 10, 15, 20, 30, 45, 60), setOf(app.profile.dailyMinutes), { s.plural(K.MINUTES, it) }, { m ->
                scope.launch {
                    app.services.store.updateProfile { it.copy(dailyMinutes = m) }
                    override = app.services.plans.today(force = true)
                }
            })
            SecondaryButton(s[K.PLAN_REBUILD], { scope.launch { override = app.services.plans.today(force = true) } })
            SectionHeader(s[K.PLAN_WEEK_FOCUS])
            PolyCard {
                StudyPlanner.weeklyFocus(plan.date, Skill.core).forEach { (d, skill) ->
                    Row { Text(Labels.day(s, d.dayOfWeek.ordinal + 1), Modifier.width(96.dp), style = MaterialTheme.typography.labelLarge); Text(Labels.skill(s, skill), style = MaterialTheme.typography.bodyMedium) }
                }
            }
            SectionHeader(s[K.PLAN_HOW_TITLE])
            Text(s[K.PLAN_HOW_BODY], style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
        }
    }
}

internal fun itemTitle(s: Localizer, item: PlanItem): String = when (item.kind) {
    PlanItemKind.REVIEW -> s[K.PLAN_ITEM_REVIEW]
    PlanItemKind.LESSON -> {
        val title = item.referenceId?.let { id -> Curriculum.lesson(id) }?.let { s.gloss(it.title) } ?: ""
        s.f(K.PLAN_ITEM_LESSON, title)
    }
    PlanItemKind.SKILL_PRACTICE -> s.f(K.PLAN_ITEM_SKILL_PRACTICE, Labels.skill(s, item.skill ?: Skill.VOCABULARY))
    PlanItemKind.CONVERSATION -> s[K.PLAN_ITEM_CONVERSATION]
    PlanItemKind.MISTAKE_REVIEW -> s[K.PLAN_ITEM_MISTAKE_REVIEW]
    PlanItemKind.RELAXATION -> s[K.PLAN_ITEM_RELAXATION]
    PlanItemKind.READING -> s[K.PLAN_ITEM_READING]
    PlanItemKind.LISTENING -> s[K.PLAN_ITEM_LISTENING]
}

private fun open(app: com.polyglot.academy.ui.app.AppScope, item: PlanItem) {
    val nav = app.nav
    when (item.kind) {
        PlanItemKind.REVIEW -> nav.push(Route.Review)
        PlanItemKind.LESSON -> item.referenceId?.let { nav.push(Route.LessonOverview(it)) }
        PlanItemKind.SKILL_PRACTICE -> nav.push(skillRoute(item.skill))
        PlanItemKind.CONVERSATION -> {
            val lang = app.profile.activeLearningLanguage
            val tutor = TutorRoster.byId(app.profile.preferredTutorId)?.takeIf { it.teaches(lang) } ?: TutorRoster.forLanguage(lang).first()
            nav.push(Route.Conversation(tutor.id))
        }
        PlanItemKind.MISTAKE_REVIEW -> nav.push(Route.Mistakes)
        PlanItemKind.RELAXATION -> nav.push(Route.Relax)
        PlanItemKind.READING -> nav.push(Route.Reading)
        PlanItemKind.LISTENING -> nav.push(Route.Listening)
    }
}

