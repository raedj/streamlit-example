package com.polyglot.academy.ui.feature.progress

import kotlinx.datetime.number
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.polyglot.academy.core.analytics.AnalyticsCalculator
import com.polyglot.academy.core.analytics.LearningSummary
import com.polyglot.academy.core.model.AssessmentResult
import com.polyglot.academy.core.model.Skill
import com.polyglot.academy.core.util.toLocalDate
import com.polyglot.academy.ui.app.LoadContent
import com.polyglot.academy.ui.app.LocalApp
import com.polyglot.academy.ui.app.ScrollScreen
import com.polyglot.academy.ui.app.rememberLoader
import com.polyglot.academy.ui.designsystem.BarChart
import com.polyglot.academy.ui.designsystem.CalendarHeatmap
import com.polyglot.academy.ui.designsystem.ChartPoint
import com.polyglot.academy.ui.designsystem.ChipGroup
import com.polyglot.academy.ui.designsystem.HeroIllustration
import com.polyglot.academy.ui.designsystem.HeroKind
import com.polyglot.academy.ui.designsystem.LevelBadge
import com.polyglot.academy.ui.designsystem.LineChart
import com.polyglot.academy.ui.designsystem.PolyCard
import com.polyglot.academy.ui.designsystem.PolyglotTheme
import com.polyglot.academy.ui.designsystem.SectionHeader
import com.polyglot.academy.ui.designsystem.Segment
import com.polyglot.academy.ui.designsystem.SkillBars
import com.polyglot.academy.ui.designsystem.StackedBar
import com.polyglot.academy.ui.designsystem.StatTile
import com.polyglot.academy.ui.designsystem.StateMessage
import com.polyglot.academy.ui.i18n.K
import com.polyglot.academy.ui.i18n.Labels
import com.polyglot.academy.ui.i18n.LocalStrings
import kotlinx.coroutines.flow.first

data class ProgressData(val summary: LearningSummary, val minutes28: List<com.polyglot.academy.core.analytics.DailyValue>, val assessments: List<AssessmentResult>, val hasData: Boolean)

@Composable
fun ProgressScreen() {
    val app = LocalApp.current
    val s = LocalStrings.current
    val lang = app.profile.activeLearningLanguage
    var range by remember { mutableIntStateOf(7) }
    val loader = rememberLoader(lang) {
        val st = app.services.store
        val now = app.services.clock.nowMillis()
        val sessions = st.sessions(lang).first()
        val attempts = st.attempts(lang).first()
        val calc = AnalyticsCalculator()
        val summary = calc.summarize(sessions, attempts, st.states(lang).first(), st.reviewCount(lang),
            st.lessonProgress(lang).first().values.count { it.completions > 0 }, st.learnerTurnCount(lang), now)
        ProgressData(summary, calc.minutesByDay(sessions, now.toLocalDate(), 28), st.assessments(lang).first(), sessions.isNotEmpty() || attempts.isNotEmpty())
    }
    ScrollScreen(s[K.PROGRESS_TITLE]) {
        Text(Labels.language(s, lang), style = MaterialTheme.typography.bodyLarge, color = PolyglotTheme.colors.textSecondary)
        LoadContent(loader) { data ->
            if (!data.hasData) {
                StateMessage(s[K.PROGRESS_EMPTY_TITLE], s[K.PROGRESS_EMPTY_BODY], illustration = { HeroIllustration(HeroKind.ANALYTICS_EMPTY, s[K.ILLUSTRATION], size = 140.dp) })
                return@LoadContent
            }
            val sum = data.summary
            val c = PolyglotTheme.colors
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile(sum.observedLessonsCompleted.toString(), s[K.TOTAL_LESSONS], Icons.Filled.AutoStories, c.primary, Modifier.weight(1f))
                StatTile(sum.observedWordsReviewed.toString(), s[K.TOTAL_REVIEWS], Icons.Filled.Replay, c.secondary, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile(sum.observedSpeakingTurns.toString(), s[K.TOTAL_SPEAKING], Icons.AutoMirrored.Filled.Chat, c.chartSeries[3], Modifier.weight(1f))
                StatTile(s.plural(K.DAYS, sum.longestStreakDays), s[K.TOTAL_STREAK], Icons.Filled.LocalFireDepartment, c.accent, Modifier.weight(1f))
            }

            SectionHeader(s[K.PROGRESS_TIME])
            ChipGroup(listOf(7, 28), setOf(range), { if (it == 7) s[K.PROGRESS_RANGE_7] else s[K.PROGRESS_RANGE_28] }, { range = it })
            PolyCard {
                val values = if (range == 7) sum.observedMinutesByDay else data.minutes28
                val points = values.map { ChartPoint(if (range == 7) Labels.dayInitial(s, it.date.dayOfWeek.ordinal + 1) else it.date.day.toString(), it.value) }
                val desc = s.f(K.PROGRESS_TIME_DESC, values.joinToString { "${it.date} ${it.value.toInt()}" })
                if (range == 7) BarChart(points, desc) else BarChart(points.filterIndexed { i, _ -> i % 2 == 1 || i == points.lastIndex }, desc)
            }

            SectionHeader(s[K.PROGRESS_CONSISTENCY])
            PolyCard {
                val active = data.minutes28.count { it.value > 0 }
                CalendarHeatmap(data.minutes28.map { ChartPoint(it.date.day.toString(), it.value) }, s.f(K.PROGRESS_CONSISTENCY_DESC, active))
            }

            SectionHeader(s[K.PROGRESS_ACCURACY])
            PolyCard {
                if (sum.observedAccuracyByWeek.isEmpty()) Text(s[K.NOT_ENOUGH_DATA], color = c.textSecondary)
                else LineChart(sum.observedAccuracyByWeek.map { ChartPoint("${it.date.day}/${it.date.month.number}", it.value) },
                    s.f(K.PROGRESS_ACCURACY_DESC, sum.observedAccuracyByWeek.joinToString { "${it.date}: ${(it.value * 100).toInt()}%" }))
            }

            SectionHeader(s[K.PROGRESS_SKILLS])
            PolyCard {
                val items = Skill.core.map { sk ->
                    val acc = sum.skillAccuracy[sk]
                    val n = sum.skillAttempts[sk] ?: 0
                    Triple(Labels.skill(s, sk), acc?.toFloat(), if (acc == null) s[K.NOT_ENOUGH_DATA] else s.f(K.PROGRESS_SKILL_VALUE, (acc * 100).toInt(), n))
                }
                SkillBars(items)
            }

            SectionHeader(s[K.PROGRESS_RETENTION])
            PolyCard {
                val r = sum.estimatedRetention
                val segments = listOf(
                    Segment(s[K.RETENTION_STRONG], r.strong, c.success), Segment(s[K.RETENTION_FADING], r.fading, c.warning),
                    Segment(s[K.RETENTION_WEAK], r.weak, c.error), Segment(s[K.RETENTION_NOT_STARTED], r.notStarted, c.border),
                )
                StackedBar(segments, segments.joinToString { "${it.label} ${it.value}" })
                Text(s[K.PROGRESS_RETENTION_NOTE], style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
            }

            SectionHeader(s[K.PROGRESS_FORECAST])
            PolyCard {
                BarChart(sum.reviewForecast.map { ChartPoint(Labels.dayInitial(s, it.date.dayOfWeek.ordinal + 1), it.value) },
                    s.f(K.PROGRESS_FORECAST_DESC, sum.reviewForecast.joinToString { "${it.date} ${it.value.toInt()}" }), color = c.secondary, highlightLast = false)
            }

            if (data.assessments.isNotEmpty()) {
                SectionHeader(s[K.PROGRESS_ASSESSMENTS])
                data.assessments.reversed().forEach { a ->
                    PolyCard { LevelBadge(a.estimatedLevel, null); Text(s.f(K.ASSESS_RANGE, a.levelLow.label, a.levelHigh.label), color = c.textSecondary) }
                }
            }
            SectionHeader(s[K.PROGRESS_DEFINITIONS])
            Text(s[K.PROGRESS_DEFINITIONS_BODY], style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
        }
    }
}
