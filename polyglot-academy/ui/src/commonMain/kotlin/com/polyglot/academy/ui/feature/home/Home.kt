package com.polyglot.academy.ui.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.polyglot.academy.core.ai.NetworkState
import com.polyglot.academy.core.learning.Recommendation
import com.polyglot.academy.core.learning.RecommendationKind
import com.polyglot.academy.core.learning.RecommendationReason
import com.polyglot.academy.core.model.Languages
import com.polyglot.academy.core.model.Skill
import com.polyglot.academy.core.planner.DailyPlan
import com.polyglot.academy.core.service.DashboardSnapshot
import com.polyglot.academy.core.tutor.TutorRoster
import com.polyglot.academy.ui.app.AppScope
import com.polyglot.academy.ui.app.LoadContent
import com.polyglot.academy.ui.app.LocalApp
import com.polyglot.academy.ui.app.ScrollScreen
import com.polyglot.academy.ui.app.rememberLoader
import com.polyglot.academy.ui.designsystem.AchievementBadge
import com.polyglot.academy.ui.designsystem.Banner
import com.polyglot.academy.ui.designsystem.BannerKind
import com.polyglot.academy.ui.designsystem.BarChart
import com.polyglot.academy.ui.designsystem.ChartPoint
import com.polyglot.academy.ui.designsystem.HeroIllustration
import com.polyglot.academy.ui.designsystem.HeroKind
import com.polyglot.academy.ui.designsystem.IconBadge
import com.polyglot.academy.ui.designsystem.LabeledProgress
import com.polyglot.academy.ui.designsystem.LevelBadge
import com.polyglot.academy.ui.designsystem.PolyCard
import com.polyglot.academy.ui.designsystem.PolyglotTheme
import com.polyglot.academy.ui.designsystem.PrimaryButton
import com.polyglot.academy.ui.designsystem.ProgressRing
import com.polyglot.academy.ui.designsystem.SectionHeader
import com.polyglot.academy.ui.designsystem.StatTile
import com.polyglot.academy.ui.designsystem.TutorAvatar
import com.polyglot.academy.ui.i18n.K
import com.polyglot.academy.ui.i18n.Labels
import com.polyglot.academy.ui.i18n.LocalStrings
import com.polyglot.academy.ui.i18n.Localizer
import com.polyglot.academy.ui.navigation.Route
import com.polyglot.academy.ui.navigation.Tab
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class HomeData(val snapshot: DashboardSnapshot, val plan: DailyPlan)

@Composable
fun HomeScreen() {
    val app = LocalApp.current
    val s = LocalStrings.current
    val loader = rememberLoader(app.profile.activeLearningLanguage, app.profile.dailyMinutes, app.profile.learningLanguages) {
        HomeData(app.services.dashboard.snapshot(), app.services.plans.today())
    }
    var offline by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { while (true) { offline = app.services.platform.network() == NetworkState.OFFLINE; delay(15_000) } }
    ScrollScreen(title = s[K.APP_NAME], showBack = false, tab = Tab.HOME, actions = {
        LanguageSwitcher()
        IconButton(onClick = { app.nav.push(Route.Search) }) { Icon(Icons.Filled.Search, s[K.SEARCH]) }
    }) {
        if (offline) Banner(BannerKind.WARNING, s[K.OFFLINE_BANNER])
        LoadContent(loader) { data -> HomeContent(data) }
    }
}

@Composable
private fun LanguageSwitcher() {
    val app = LocalApp.current
    val s = LocalStrings.current
    val scope = rememberCoroutineScope()
    var open by remember { mutableStateOf(false) }
    IconButton(onClick = { open = true }) { Icon(Icons.Filled.Translate, s[K.HOME_SWITCH_LANGUAGE]) }
    DropdownMenu(open, onDismissRequest = { open = false }) {
        Languages.all.filter { it.code != app.profile.nativeLanguage }.forEach { lp ->
            DropdownMenuItem(
                text = { Text(lp.displayName(s.language) + if (lp.code == app.profile.activeLearningLanguage) " ✓" else "") },
                onClick = { open = false; scope.launch { app.services.store.updateProfile { it.copy(activeLearningLanguage = lp.code) } } },
            )
        }
    }
}

private fun greetingKey(nowMillis: Long): K {
    val hour = Instant.fromEpochMilliseconds(nowMillis).toLocalDateTime(TimeZone.currentSystemDefault()).hour
    return when (hour) { in 5..11 -> K.HOME_GREETING_MORNING; in 12..17 -> K.HOME_GREETING_AFTERNOON; else -> K.HOME_GREETING_EVENING }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HomeContent(data: HomeData) {
    val app = LocalApp.current
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val snap = data.snapshot
    val name = snap.profile.displayName.takeIf { it.isNotBlank() }?.let { "، $it".takeIf { s.isRtl } ?: ", $it" } ?: ""

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(s.f(greetingKey(app.services.clock.nowMillis()), name), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.semantics { heading() })
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(s.f(K.HOME_LEARNING, Labels.language(s, snap.language)), style = MaterialTheme.typography.bodyLarge, color = c.textSecondary)
            snap.level?.let { LevelBadge(it, if (snap.levelIsEstimate) s[K.HOME_LEVEL_ESTIMATE] else s[K.HOME_LEVEL_SELF]) }
        }
    }

    if (snap.isNewLearner) {
        PolyCard(containerColor = c.primaryContainer.copy(alpha = if (c.isDark) 0.4f else 0.55f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(s[K.HOME_NEW_TITLE], style = MaterialTheme.typography.titleMedium)
                    Text(s[K.HOME_NEW_BODY], style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
                }
                HeroIllustration(HeroKind.WELCOME, s[K.ILLUSTRATION], size = 88.dp)
            }
        }
    }

    RecommendationCard(snap.recommendation, snap)

    // Daily goal + reviews + streak.
    PolyCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val fraction = snap.minutesToday.toFloat() / snap.dailyGoalMinutes.coerceAtLeast(1)
            ProgressRing(fraction, description = s.f(K.HOME_GOAL_RING_DESC, snap.minutesToday, snap.dailyGoalMinutes), size = 84.dp) {
                Text(s.f(K.PERCENT, (fraction.coerceAtMost(1f) * 100).toInt()), style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(s[K.HOME_DAILY_GOAL], style = MaterialTheme.typography.titleMedium)
                Text(s.f(K.HOME_GOAL_PROGRESS, snap.minutesToday, snap.dailyGoalMinutes), style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
                val planDone = data.plan.items.count { it.completed }
                Text(s.f(K.HOME_PLAN_PROGRESS, planDone, data.plan.items.size), style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
            }
        }
        com.polyglot.academy.ui.designsystem.QuietButton(s[K.HOME_TODAY_PLAN], { app.nav.push(Route.Plan) })
    }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatTile(if (snap.dueReviews > 0) s.plural(K.HOME_REVIEWS_DUE, snap.dueReviews) else s[K.HOME_NO_REVIEWS], s[K.PRACTICE_REVIEW], Icons.Filled.Replay, c.secondary, Modifier.weight(1f))
        StatTile(s.plural(K.HOME_STREAK, snap.summary.currentStreakDays), s[K.PROGRESS_CONSISTENCY], Icons.Filled.LocalFireDepartment, c.accent, Modifier.weight(1f))
    }

    SectionHeader(s[K.HOME_QUICK])
    FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp), maxItemsInEachRow = 4) {
        QuickAction(s[K.QUICK_CONVERSATION], Icons.AutoMirrored.Filled.Chat, Modifier.weight(1f)) {
            val tutor = TutorRoster.byId(snap.profile.preferredTutorId)?.takeIf { it.teaches(snap.language) } ?: TutorRoster.defaultFor(snap.language, snap.level ?: com.polyglot.academy.core.model.CefrLevel.A1)
            app.nav.push(Route.Conversation(tutor.id))
        }
        QuickAction(s[K.QUICK_REVIEW], Icons.Filled.Replay, Modifier.weight(1f)) { app.nav.push(Route.Review) }
        QuickAction(s[K.QUICK_LESSON], Icons.Filled.AutoStories, Modifier.weight(1f)) {
            (snap.interruptedLesson ?: snap.nextLesson)?.let { app.nav.push(Route.LessonOverview(it.id)) } ?: app.nav.switchTab(Tab.LEARN)
        }
        QuickAction(s[K.QUICK_IMPORT], Icons.Filled.FolderOpen, Modifier.weight(1f)) { app.nav.push(Route.Library) }
    }

    TutorShortcut(snap)

    if (!snap.isNewLearner) {
        SectionHeader(s[K.HOME_THIS_WEEK], action = s[K.SEE_ALL], onAction = { app.nav.push(Route.Progress) })
        PolyCard {
            val points = snap.summary.observedMinutesByDay.map { ChartPoint(Labels.dayInitial(s, it.date.dayOfWeek.ordinal + 1), it.value) }
            BarChart(points, s.f(K.HOME_WEEK_CHART_DESC, snap.summary.observedMinutesByDay.joinToString { "${Labels.day(s, it.date.dayOfWeek.ordinal + 1)} ${it.value.toInt()}" }))
        }
        SectionHeader(s[K.HOME_SKILLS])
        PolyCard {
            val measured = snap.skillEstimates.filter { it.hasEnoughData }
            if (measured.isEmpty()) Text(s[K.HOME_SKILLS_EMPTY], style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
            measured.forEach { e -> LabeledProgress(Labels.skill(s, e.skill), s.f(K.PERCENT, (e.mastery * 100).toInt()), e.mastery.toFloat()) }
        }
    }

    if (snap.recentAchievements.isNotEmpty()) {
        SectionHeader(s[K.HOME_ACHIEVEMENTS])
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            snap.recentAchievements.forEach { a ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    AchievementBadge("★", true, Labels.achievement(s, a.id))
                    Text(Labels.achievement(s, a.id), style = MaterialTheme.typography.labelSmall, color = c.textSecondary, maxLines = 2)
                }
            }
        }
    }
}

@Composable
private fun QuickAction(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, onClick: () -> Unit) {
    val c = PolyglotTheme.colors
    PolyCard(modifier, onClick = onClick, contentPadding = 12.dp) {
        IconBadge(icon, c.primary, c.primaryContainer.copy(alpha = if (c.isDark) 0.5f else 1f), size = 40.dp)
        Text(label, style = MaterialTheme.typography.labelLarge, maxLines = 1)
    }
}

@Composable
private fun TutorShortcut(snap: DashboardSnapshot) {
    val app = LocalApp.current
    val s = LocalStrings.current
    val tutor = TutorRoster.byId(snap.profile.preferredTutorId)?.takeIf { it.teaches(snap.language) }
        ?: TutorRoster.defaultFor(snap.language, snap.level ?: com.polyglot.academy.core.model.CefrLevel.A1)
    PolyCard(onClick = { app.nav.push(Route.Conversation(tutor.id)) }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TutorAvatar(tutor.avatar, s.f(K.TUTOR_AVATAR_DESC, tutor.name), size = 56.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(s.f(K.HOME_TUTOR_SHORTCUT, tutor.name), style = MaterialTheme.typography.titleMedium)
                Text(s.gloss(tutor.tagline), style = MaterialTheme.typography.bodySmall, color = PolyglotTheme.colors.textSecondary)
            }
        }
    }
}

@Composable
private fun RecommendationCard(rec: Recommendation, snap: DashboardSnapshot) {
    val app = LocalApp.current
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val title = recommendationTitle(s, rec, snap)
    val reason = if (rec.reason == RecommendationReason.MANY_REVIEWS_DUE) s.plural(K.REASON_MANY_REVIEWS_DUE, snap.dueReviews) else Labels.reason(s, rec.reason)
    PolyCard(containerColor = c.card) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(Icons.Filled.Bolt, c.onPrimary, c.primary, size = 40.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(s[K.HOME_RECOMMENDED], style = MaterialTheme.typography.labelMedium, color = c.primary)
                Text(title, style = MaterialTheme.typography.titleMedium)
            }
        }
        Text(reason, style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
        if (rec.kind != RecommendationKind.ALL_DONE) {
            PrimaryButton(if (rec.estimatedMinutes > 0) "${s[K.HOME_START]} · ${s.plural(K.MINUTES, rec.estimatedMinutes)}" else s[K.HOME_START],
                { openRecommendation(app, rec, snap) }, Modifier.fillMaxWidth())
        }
    }
}

internal fun recommendationTitle(s: Localizer, rec: Recommendation, snap: DashboardSnapshot): String = when (rec.kind) {
    RecommendationKind.REVIEW_DUE -> s[K.REC_REVIEW_DUE]
    RecommendationKind.MISTAKE_REVIEW -> s[K.REC_MISTAKE_REVIEW]
    RecommendationKind.CONTINUE_LESSON -> s.f(K.REC_CONTINUE_LESSON, snap.interruptedLesson?.let { s.gloss(it.title) } ?: "")
    RecommendationKind.NEXT_LESSON -> s.f(K.REC_NEXT_LESSON, snap.nextLesson?.let { s.gloss(it.title) } ?: "")
    RecommendationKind.SKILL_PRACTICE -> s.f(K.REC_SKILL_PRACTICE, Labels.skill(s, rec.skill ?: Skill.VOCABULARY))
    RecommendationKind.CONVERSATION -> s[K.REC_CONVERSATION]
    RecommendationKind.ASSESSMENT -> s[K.REC_ASSESSMENT]
    RecommendationKind.RELAXATION -> s[K.REC_RELAXATION]
    RecommendationKind.ALL_DONE -> s[K.REC_ALL_DONE]
}

internal fun openRecommendation(app: AppScope, rec: Recommendation, snap: DashboardSnapshot) {
    val nav = app.nav
    when (rec.kind) {
        RecommendationKind.REVIEW_DUE -> nav.push(Route.Review)
        RecommendationKind.MISTAKE_REVIEW -> nav.push(Route.Mistakes)
        RecommendationKind.CONTINUE_LESSON, RecommendationKind.NEXT_LESSON -> rec.referenceId?.let { nav.push(Route.LessonOverview(it)) }
        RecommendationKind.SKILL_PRACTICE -> nav.push(skillRoute(rec.skill))
        RecommendationKind.CONVERSATION -> {
            val tutor = TutorRoster.byId(snap.profile.preferredTutorId)?.takeIf { it.teaches(snap.language) } ?: TutorRoster.defaultFor(snap.language, snap.level ?: com.polyglot.academy.core.model.CefrLevel.A1)
            nav.push(Route.Conversation(tutor.id))
        }
        RecommendationKind.ASSESSMENT -> nav.push(Route.Assessment(snap.language))
        RecommendationKind.RELAXATION -> nav.push(Route.Relax)
        RecommendationKind.ALL_DONE -> {}
    }
}

internal fun skillRoute(skill: Skill?): Route = when (skill) {
    Skill.READING, Skill.COMPREHENSION -> Route.Reading
    Skill.LISTENING -> Route.Listening
    Skill.WRITING -> Route.Writing
    Skill.PRONUNCIATION -> Route.Pronunciation
    Skill.GRAMMAR -> Route.Grammar
    Skill.SPEAKING, Skill.FLUENCY, Skill.FUNCTIONAL -> Route.Scenarios
    else -> Route.Review
}
