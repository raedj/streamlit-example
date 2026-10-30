package com.polyglot.academy.ui.feature.learn

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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.polyglot.academy.core.content.Curriculum
import com.polyglot.academy.core.model.Bookmark
import com.polyglot.academy.core.model.BookmarkKind
import com.polyglot.academy.core.model.CefrLevel
import com.polyglot.academy.core.model.CourseUnit
import com.polyglot.academy.core.model.Lesson
import com.polyglot.academy.core.model.LessonProgress
import com.polyglot.academy.ui.app.LocalApp
import com.polyglot.academy.ui.app.ScrollScreen
import com.polyglot.academy.ui.app.rememberSpeaker
import com.polyglot.academy.ui.designsystem.AudioButton
import com.polyglot.academy.ui.designsystem.Banner
import com.polyglot.academy.ui.designsystem.BannerKind
import com.polyglot.academy.ui.designsystem.IconBadge
import com.polyglot.academy.ui.designsystem.LearningText
import com.polyglot.academy.ui.designsystem.LevelBadge
import com.polyglot.academy.ui.designsystem.Pill
import com.polyglot.academy.ui.designsystem.PolyCard
import com.polyglot.academy.ui.designsystem.PolyglotTheme
import com.polyglot.academy.ui.designsystem.PrimaryButton
import com.polyglot.academy.ui.designsystem.SecondaryButton
import com.polyglot.academy.ui.designsystem.SectionHeader
import com.polyglot.academy.ui.designsystem.StateMessage
import com.polyglot.academy.ui.i18n.K
import com.polyglot.academy.ui.i18n.Labels
import com.polyglot.academy.ui.i18n.LocalStrings
import com.polyglot.academy.ui.navigation.Route
import com.polyglot.academy.ui.navigation.Tab
import kotlinx.coroutines.launch

@Composable
fun CoursesScreen(trackId: String?, isTabRoot: Boolean) {
    val app = LocalApp.current
    val s = LocalStrings.current
    val lang = app.profile.activeLearningLanguage
    val curriculum = Curriculum.of(lang)
    val progress by remember(lang) { app.services.store.lessonProgress(lang) }.collectAsState(emptyMap())
    val start = app.profile.enrollment(lang)?.startingLevel ?: CefrLevel.A1
    val track = trackId?.let { id -> curriculum.tracks.firstOrNull { it.id == id } }
    ScrollScreen(track?.let { s.gloss(it.title) } ?: s[K.LEARN_TITLE], showBack = !isTabRoot, tab = if (isTabRoot) Tab.LEARN else null) {
        if (track == null) {
            Text("${Labels.language(s, lang)} · ${s[K.LEARN_GENERAL]}", style = MaterialTheme.typography.bodyLarge, color = PolyglotTheme.colors.textSecondary)
            curriculum.units.filter { it.trackId == CourseUnit.GENERAL_TRACK }.forEach { unit -> UnitCard(unit, curriculum.lessonsById, progress, start, isTrack = false) }
            SectionHeader(s[K.LEARN_TRACKS])
            if (curriculum.tracks.isEmpty()) Text(s[K.TRACK_EMPTY], color = PolyglotTheme.colors.textSecondary)
            curriculum.tracks.forEach { t ->
                PolyCard(onClick = { app.nav.push(Route.Courses(t.id)) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconBadge(Icons.Filled.Work, PolyglotTheme.colors.secondary, PolyglotTheme.colors.secondaryContainer)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(s.gloss(t.title), style = MaterialTheme.typography.titleMedium)
                            Text(s.gloss(t.description), style = MaterialTheme.typography.bodySmall, color = PolyglotTheme.colors.textSecondary)
                        }
                        Pill(s.f(K.TRACK_MIN_LEVEL, t.minimumLevel.label))
                    }
                }
            }
        } else {
            Text(s.gloss(track.description), style = MaterialTheme.typography.bodyLarge, color = PolyglotTheme.colors.textSecondary)
            track.disclaimer?.let { Banner(BannerKind.INFO, s.gloss(it), title = s[K.TRACK_DISCLAIMER_TITLE]) }
            curriculum.unitsForTrack(track.id).forEach { unit -> UnitCard(unit, curriculum.lessonsById, progress, start, isTrack = true) }
            curriculum.scenariosForTrack(track.id).forEach { sc ->
                PolyCard(onClick = { app.nav.push(Route.Conversation("daniel".takeIf { track.id == "business" } ?: "alex", sc.id)) }) {
                    Text(s.gloss(sc.title), style = MaterialTheme.typography.titleSmall)
                    Text(s.f(K.TUTOR_SCENARIO_GOAL, s.gloss(sc.learnerGoal)), style = MaterialTheme.typography.bodySmall, color = PolyglotTheme.colors.textSecondary)
                }
            }
        }
    }
}

@Composable
private fun UnitCard(unit: CourseUnit, lessons: Map<String, Lesson>, progress: Map<String, LessonProgress>, start: CefrLevel, isTrack: Boolean) {
    val app = LocalApp.current
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val unitLessons = unit.lessonIds.mapNotNull { lessons[it] }
    val sequencer = Curriculum.of(unit.language).sequencer
    PolyCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(s.gloss(unit.title), style = MaterialTheme.typography.titleMedium)
                Text(s.gloss(unit.description), style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
            }
            LevelBadge(unit.level, null)
        }
        Text(s.f(K.LEARN_UNIT_PROGRESS, unitLessons.count { progress[it.id]?.mastered == true }, unitLessons.size), style = MaterialTheme.typography.labelMedium, color = c.textSecondary)
        unitLessons.forEach { lesson ->
            val p = progress[lesson.id]
            val unlocked = isTrack || sequencer.isUnlocked(lesson, start, progress)
            val below = !isTrack && lesson.level < start
            val (icon, tint, state) = when {
                p?.mastered == true -> Triple(Icons.Filled.CheckCircle, c.success, s[K.LESSON_MASTERED])
                !unlocked -> Triple(Icons.Filled.Lock, c.textMuted, s[K.LOCKED])
                else -> Triple(Icons.Filled.PlayCircle, c.primary, lessonState(s, p))
            }
            Row(
                Modifier.fillMaxWidth().semantics(mergeDescendants = true) { stateDescription = state }
                    .then(if (unlocked) Modifier.clickable(role = Role.Button) { app.nav.push(Route.LessonOverview(lesson.id)) } else Modifier),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f).padding(vertical = 6.dp)) {
                    Text(s.gloss(lesson.title), style = MaterialTheme.typography.bodyLarge, color = if (unlocked) c.textPrimary else c.textMuted)
                    Text(
                        listOfNotNull(s.plural(K.MINUTES, lesson.estimatedMinutes), state, if (below) s[K.LESSON_BELOW_LEVEL] else null, if (!unlocked) s[K.LESSON_LOCKED_HINT] else null).joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall, color = c.textSecondary,
                    )
                }
            }
        }
    }
}

/** A progress row also exists for a lesson that was only started (its resume point), so a best score needs a completion. */
private fun lessonState(s: com.polyglot.academy.ui.i18n.Localizer, p: LessonProgress?): String = when {
    p == null -> s[K.NOT_STARTED]
    p.completions > 0 -> s.f(K.LESSON_BEST, (p.bestScore * 100).toInt())
    p.resumeIndex > 0 -> s.f(K.LESSON_RESUME, p.resumeIndex + 1)
    else -> s[K.NOT_STARTED]
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LessonOverviewScreen(lessonId: String) {
    val app = LocalApp.current
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val scope = rememberCoroutineScope()
    val lesson = Curriculum.lesson(lessonId)
    if (lesson == null) {
        ScrollScreen(s[K.LEARN_TITLE]) { StateMessage(s[K.ERROR_TITLE], s[K.LESSON_NOT_FOUND], action = s[K.BACK], onAction = { app.nav.pop() }) }
        return
    }
    val curriculum = Curriculum.of(lesson.language)
    val progress by remember(lesson.language) { app.services.store.lessonProgress(lesson.language) }.collectAsState(emptyMap())
    val bookmarks by remember { app.services.store.bookmarks() }.collectAsState(emptyList())
    val bookmarked = bookmarks.any { it.kind == BookmarkKind.LESSON && it.referenceId == lesson.id }
    val speaker = rememberSpeaker(s)
    val enrollment = app.profile.enrollment(lesson.language)
    ScrollScreen(s.gloss(lesson.title), actions = {
        IconButton(onClick = { scope.launch { app.services.store.toggleBookmark(Bookmark(app.services.ids.newId("bm"), BookmarkKind.LESSON, lesson.id, app.services.clock.nowMillis())) } }) {
            Icon(if (bookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder, if (bookmarked) s[K.LESSON_UNBOOKMARK] else s[K.LESSON_BOOKMARK])
        }
    }) {
        Text(s.gloss(lesson.summary), style = MaterialTheme.typography.bodyLarge, color = c.textSecondary)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            LevelBadge(lesson.level, null)
            Pill(Labels.skill(s, lesson.primarySkill))
            Pill(s.plural(K.MINUTES, lesson.estimatedMinutes))
            Pill(s.plural(K.LESSON_EXERCISES, lesson.exercises.size))
        }
        val p = progress[lesson.id]
        if (p != null && p.resumeIndex > 0) {
            PrimaryButton(s.f(K.LESSON_RESUME, p.resumeIndex + 1), { app.nav.push(Route.LessonPlayer(lesson.id)) }, Modifier.fillMaxWidth())
            SecondaryButton(s[K.LESSON_RESTART], { app.nav.push(Route.LessonPlayer(lesson.id, restart = true)) }, Modifier.fillMaxWidth())
        } else {
            PrimaryButton(s[K.LESSON_START], { app.nav.push(Route.LessonPlayer(lesson.id, restart = true)) }, Modifier.fillMaxWidth())
        }
        p?.let { if (it.completions > 0) Text(s.f(K.LESSON_BEST, (it.bestScore * 100).toInt()) + if (it.mastered) " · ${s[K.LESSON_MASTERED]}" else "", color = c.textSecondary) }
        lesson.grammarTopicId?.let { gid -> curriculum.grammarById[gid] }?.let { g ->
            SectionHeader(s[K.LESSON_GRAMMAR])
            PolyCard(onClick = { app.nav.push(Route.GrammarTopic(g.id)) }) {
                Text(s.gloss(g.title), style = MaterialTheme.typography.titleSmall)
                Text(s.gloss(g.rule), style = MaterialTheme.typography.bodyMedium, color = c.textSecondary, maxLines = 3)
            }
        }
        SectionHeader(s[K.LESSON_VOCAB])
        PolyCard {
            lesson.vocabularyIds.mapNotNull { curriculum.vocabularyById[it] }.forEach { v ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.weight(1f)) {
                        val reading = if (enrollment?.showTransliteration != false) v.pronunciation ?: v.transliteration else null
                        val term = if (enrollment?.useTraditionalCharacters == true) v.traditional ?: v.term else v.term
                        LearningText(term, lesson.language, style = MaterialTheme.typography.titleMedium, reading = reading)
                        Text(s.gloss(v.meaning), style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                    }
                    AudioButton(s[K.PLAY_AUDIO], { speaker.toggle(v.term, lesson.language) }, playing = speaker.playingText == v.term)
                }
            }
        }
    }
}
