package com.polyglot.academy.ui.feature.practice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.polyglot.academy.core.model.CefrLevel
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.speaking.FluencyReport
import com.polyglot.academy.core.speaking.LevelProgress
import com.polyglot.academy.core.speaking.MissionStatus
import com.polyglot.academy.core.speaking.SpeakingMission
import com.polyglot.academy.core.speaking.SpeakingProgressCalculator
import com.polyglot.academy.core.tutor.CoachStats
import com.polyglot.academy.core.tutor.CoachTitles
import com.polyglot.academy.core.tutor.TutorPersona
import com.polyglot.academy.core.tutor.TutorRoster
import com.polyglot.academy.ui.app.LoadContent
import com.polyglot.academy.ui.app.LocalApp
import com.polyglot.academy.ui.app.ScrollScreen
import com.polyglot.academy.ui.app.rememberLoader
import com.polyglot.academy.ui.designsystem.BarChart
import com.polyglot.academy.ui.designsystem.ChartPoint
import com.polyglot.academy.ui.designsystem.Pill
import com.polyglot.academy.ui.designsystem.PolyCard
import com.polyglot.academy.ui.designsystem.PolyglotTheme
import com.polyglot.academy.ui.designsystem.PrimaryButton
import com.polyglot.academy.ui.designsystem.QuietButton
import com.polyglot.academy.ui.designsystem.SectionHeader
import com.polyglot.academy.ui.designsystem.StatTile
import com.polyglot.academy.ui.i18n.K
import com.polyglot.academy.ui.i18n.Labels
import com.polyglot.academy.ui.i18n.LocalStrings
import com.polyglot.academy.ui.navigation.Route

private class SpeakingData(val levels: List<LevelProgress>, val fluency: FluencyReport, val stats: CoachStats)

/** The tutor who teaches [language] and suits the mission level, falling back to any tutor for the language. */
internal fun tutorFor(language: LanguageCode, level: CefrLevel): TutorPersona =
    TutorRoster.tutors.firstOrNull { it.teaches(language) && it.suits(level) }
        ?: TutorRoster.tutors.firstOrNull { it.teaches(language) } ?: TutorRoster.tutors.first()

@Composable
fun SpeakingPathScreen() {
    val app = LocalApp.current
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val lang = app.profile.activeLearningLanguage
    val loader = rememberLoader(lang) { SpeakingData(app.services.speaking.progress(lang), app.services.speaking.fluency(lang), app.services.coach.stats(lang)) }
    fun start(m: SpeakingMission) = app.nav.push(Route.Conversation(tutorFor(lang, m.level).id, m.conversationKey))

    ScrollScreen(s[K.SPEAKING_TITLE]) {
        Text(Labels.language(s, lang), style = MaterialTheme.typography.bodyLarge, color = c.textSecondary)
        Text(s[K.SPEAKING_BODY], style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
        LoadContent(loader) { data ->
            CoachTitleCard(data.stats)
            SpeakingStats(data.fluency)
            SpeakingProgressCalculator.next(data.levels)?.let { m ->
                SectionHeader(s[K.SPEAKING_NEXT])
                PolyCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(s.gloss(m.title), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        Pill(m.level.label)
                    }
                    Text(s.gloss(m.canDo), style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
                    PrimaryButton(s[K.SPEAKING_START], { start(m) }, Modifier.fillMaxWidth())
                }
            } ?: PolyCard { Text(s[K.SPEAKING_ALL_DONE], style = MaterialTheme.typography.titleMedium, color = c.success) }

            data.levels.forEach { level ->
                SectionHeader(s.f(K.SPEAKING_LEVEL, level.level.label, level.passedCount, level.missions.size))
                level.missions.forEach { status -> MissionRow(status) { start(status.mission) } }
            }
            SectionHeader(s[K.SPEAKING_ABOUT_TITLE])
            Text(s[K.SPEAKING_ABOUT], style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
        }
    }
}

/** The learner's current encouragement title and what the next one needs. */
@Composable
internal fun CoachTitleCard(stats: CoachStats) {
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val current = CoachTitles.current(stats)
    PolyCard {
        Text(s[K.COACH_TITLE_LABEL], style = MaterialTheme.typography.labelMedium, color = c.textMuted)
        Text(Labels.coachTitle(s, current), style = MaterialTheme.typography.titleLarge, color = c.primary)
        CoachTitles.next(stats)?.let { next ->
            Text(s.f(K.COACH_TITLE_NEXT, Labels.coachTitle(s, next), s[K.valueOf("COACH_NEED_" + next.name)]), style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
        } ?: Text(s[K.COACH_TITLE_TOP], style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
    }
}

@Composable
private fun MissionRow(status: MissionStatus, onStart: () -> Unit) {
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val m = status.mission
    PolyCard(onClick = onStart) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(s.gloss(m.title), style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            when {
                status.passed -> Pill(s[K.SPEAKING_PASSED], container = c.success, content = c.onPrimary)
                status.attempts > 0 -> Pill(s.plural(K.SPEAKING_ATTEMPTS, status.attempts))
                else -> Pill(s[K.SPEAKING_NEW])
            }
        }
        Text(s.gloss(m.canDo), style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
        QuietButton(if (status.passed) s[K.SPEAKING_AGAIN] else s[K.SPEAKING_START], onStart)
    }
}

@Composable
private fun SpeakingStats(r: FluencyReport) {
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    SectionHeader(s[K.SPEAKING_STATS])
    if (r.learnerTurns == 0) {
        Text(s[K.SPEAKING_NO_DATA], style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
        return
    }
    val dash = "—"
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatTile(r.medianWordsPerTurn?.toString() ?: dash, s[K.SPEAKING_WORDS_PER_TURN], Icons.AutoMirrored.Filled.Chat, c.primary, Modifier.weight(1f))
        StatTile(r.longestTurnWords?.toString() ?: dash, s[K.SPEAKING_LONGEST], Icons.Filled.AutoStories, c.secondary, Modifier.weight(1f))
    }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatTile(r.wordsPerMinute?.toString() ?: dash, s[K.SPEAKING_WPM], Icons.Filled.Speed, c.accent, Modifier.weight(1f))
        StatTile(r.spokenShare?.let { "${(it * 100).toInt()}%" } ?: dash, s[K.SPEAKING_SPOKEN_SHARE], Icons.Filled.Mic, c.chartSeries[3], Modifier.weight(1f))
    }
    PolyCard {
        Text(r.lexicalVariety?.let { s.f(K.SPEAKING_VARIETY, (it * 100).toInt()) } ?: s[K.SPEAKING_VARIETY_LOW], style = MaterialTheme.typography.bodyMedium)
        r.correctionsPerTurn?.let { Text(s.f(K.SPEAKING_CORRECTIONS, ((it * 10) * 10).toInt() / 10.0), style = MaterialTheme.typography.bodyMedium) }
        if (r.wordsPerMinute == null) Text(s[K.SPEAKING_WPM_NONE], style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
        Text(s[K.SPEAKING_STATS_NOTE], style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
    }
    if (r.weekly.any { it.learnerTurns > 0 }) {
        PolyCard {
            Text(s[K.SPEAKING_WEEKLY], style = MaterialTheme.typography.titleSmall)
            BarChart(r.weekly.map { ChartPoint("${it.weekStart.dayOfMonth}/${it.weekStart.monthNumber}", it.medianWords.toDouble()) },
                s.f(K.SPEAKING_WEEKLY_DESC, r.weekly.joinToString { "${it.weekStart}: ${it.medianWords}" }))
        }
    }
}
