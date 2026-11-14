package com.polyglot.academy.core

import com.polyglot.academy.core.analytics.AnalyticsCalculator
import com.polyglot.academy.core.assessment.DiagnosticSession
import com.polyglot.academy.core.content.Curriculum
import com.polyglot.academy.core.learning.SkillEstimate
import com.polyglot.academy.core.model.CefrLevel
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.LearningGoal
import com.polyglot.academy.core.model.RecallRating
import com.polyglot.academy.core.model.SessionKind
import com.polyglot.academy.core.model.Skill
import com.polyglot.academy.core.model.StudySession
import com.polyglot.academy.core.planner.PlanItemKind
import com.polyglot.academy.core.planner.PlannerInput
import com.polyglot.academy.core.planner.StudyPlanner
import com.polyglot.academy.core.srs.SpacedRepetitionScheduler
import com.polyglot.academy.core.util.MILLIS_PER_DAY
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AssessmentPlannerAnalyticsTest {
    private val bank = Curriculum.of(LanguageCode.ENGLISH).assessment

    private fun run(answerCorrectUpTo: CefrLevel): com.polyglot.academy.core.model.AssessmentResult {
        val session = DiagnosticSession(LanguageCode.ENGLISH, bank, CefrLevel.A1)
        while (!session.isFinished) {
            val item = session.current!!
            val correct = item.level <= answerCorrectUpTo
            session.answer(if (correct) item.question.correctIndex else (item.question.correctIndex + 1) % item.question.options.size)
        }
        return session.result("r", 0)
    }

    @Test fun diagnosticPlacesStrongLearnerHigh() {
        val r = run(CefrLevel.B2)
        assertEquals(CefrLevel.B2, r.estimatedLevel)
        assertTrue(r.levelLow <= r.estimatedLevel && r.levelHigh >= r.estimatedLevel)
    }

    @Test fun diagnosticPlacesBeginnerLow() {
        val r = run(CefrLevel.PRE_A1)
        assertTrue(r.estimatedLevel <= CefrLevel.A1, "got ${r.estimatedLevel}")
    }

    @Test fun diagnosticPlacesA2Learner() {
        val r = run(CefrLevel.A2)
        assertEquals(CefrLevel.A2, r.estimatedLevel)
        assertTrue(r.questionsAnswered in 8..14)
    }

    @Test fun diagnosticRotatesSkillsAndNeverRepeatsItems() {
        val s = DiagnosticSession(LanguageCode.ENGLISH, bank, CefrLevel.A1)
        val seen = mutableListOf<String>()
        while (!s.isFinished) { seen += s.current!!.id; s.answer(s.current!!.question.correctIndex) }
        assertEquals(seen.size, seen.distinct().size)
    }

    @Test fun everyLanguageCanRunADiagnostic() {
        for (c in Curriculum.all.values) {
            val s = DiagnosticSession(c.language, c.assessment)
            var guard = 0
            while (!s.isFinished && guard++ < 50) s.answer(0)
            assertTrue(s.isFinished, "diagnostic for ${c.language} never finished")
        }
    }

    private fun plannerInput(minutes: Int = 15, due: Int = 20, relax: Boolean = false, missed: Boolean = false) = PlannerInput(
        date = LocalDate(2026, 10, 24), dailyMinutes = minutes, dueReviews = due, unresolvedMistakes = 0, nextLessonId = "l1",
        nextLessonMinutes = 8, skillEstimates = emptyList(), goals = setOf(LearningGoal.TRAVEL), includeRelaxation = relax, missedYesterday = missed,
    )

    @Test fun planFitsBudgetExactly() {
        for (m in listOf(5, 10, 15, 30, 60)) {
            val p = StudyPlanner.dailyPlan(plannerInput(minutes = m))
            assertTrue(p.totalMinutes <= m, "budget $m total ${p.totalMinutes}")
            assertTrue(p.totalMinutes >= m - 3, "budget $m total ${p.totalMinutes}")
        }
    }

    @Test fun reviewsCappedAtFortyPercent() {
        val p = StudyPlanner.dailyPlan(plannerInput(minutes = 20, due = 500))
        assertEquals(8, p.items.first { it.kind == PlanItemKind.REVIEW }.minutes)
    }

    @Test fun missedDayDoesNotEnlargePlan() {
        val normal = StudyPlanner.dailyPlan(plannerInput(missed = false))
        val missed = StudyPlanner.dailyPlan(plannerInput(missed = true))
        assertEquals(normal.totalMinutes, missed.totalMinutes)
        assertTrue(missed.adjustedForMissedDay)
    }

    @Test fun relaxationOnlyWhenEnabled() {
        assertTrue(StudyPlanner.dailyPlan(plannerInput()).items.none { it.kind == PlanItemKind.RELAXATION })
        assertTrue(StudyPlanner.dailyPlan(plannerInput(relax = true)).items.any { it.kind == PlanItemKind.RELAXATION })
    }

    @Test fun focusSkillUsesWeakestMeasuredSkill() {
        val i = plannerInput().copy(skillEstimates = listOf(SkillEstimate(Skill.READING, 0.3, 10, 0.6), SkillEstimate(Skill.GRAMMAR, 0.6, 10, 0.6)))
        assertEquals(Skill.READING, StudyPlanner.focusSkill(i))
        assertEquals(Skill.LISTENING, StudyPlanner.focusSkill(plannerInput()))
    }

    @Test fun backlogSpreading() {
        assertEquals(listOf(30, 30, 10), StudyPlanner.spreadBacklog(70, 3, 30))
        assertEquals(listOf(30, 30, 50), StudyPlanner.spreadBacklog(110, 3, 30))
    }

    @Test fun analyticsStreaksAndMinutes() {
        val zone = TimeZone.UTC
        val calc = AnalyticsCalculator(zone = zone)
        val day = MILLIS_PER_DAY
        val base = 1_761_264_000_000L // 2025-10-24T00:00Z
        val sessions = listOf(0, 1, 2, 4).map { d ->
            StudySession("s$d", LanguageCode.ENGLISH, SessionKind.LESSON, null, base - d * day + 3_600_000, null, 600, 5, 4, true)
        }
        val summary = calc.summarize(sessions, emptyList(), emptyList(), 0, 0, 0, base + 7_200_000)
        assertEquals(3, summary.currentStreakDays)
        assertEquals(3, summary.longestStreakDays)
        assertEquals(7, summary.observedMinutesByDay.size)
        assertEquals(10.0, summary.observedMinutesByDay.last().value)
        assertEquals(0.0, summary.observedMinutesByDay[summary.observedMinutesByDay.size - 4].value)
    }

    @Test fun retentionBuckets() {
        val s = SpacedRepetitionScheduler()
        val t0 = 1_700_000_000_000L
        val fresh = s.review(s.newState("a", LanguageCode.ENGLISH, t0), RecallRating.EASY, t0).state
        val calc = AnalyticsCalculator(s, TimeZone.UTC)
        val b = calc.retention(listOf(fresh, s.newState("b", LanguageCode.ENGLISH, t0)), t0 + 1000)
        assertEquals(1, b.strong); assertEquals(1, b.notStarted)
        val later = calc.retention(listOf(fresh), t0 + 60 * MILLIS_PER_DAY)
        assertEquals(1, later.weak)
    }
}
