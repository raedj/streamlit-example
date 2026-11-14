package com.polyglot.academy.core.planner

import com.polyglot.academy.core.learning.SkillEstimate
import com.polyglot.academy.core.model.LearningGoal
import com.polyglot.academy.core.model.Skill
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlinx.serialization.Serializable
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Serializable
enum class PlanItemKind { REVIEW, LESSON, SKILL_PRACTICE, CONVERSATION, MISTAKE_REVIEW, RELAXATION, READING, LISTENING }

@Serializable
data class PlanItem(
    val id: String,
    val kind: PlanItemKind,
    val minutes: Int,
    val referenceId: String? = null,
    val skill: Skill? = null,
    val completed: Boolean = false,
)

@Serializable
data class DailyPlan(val date: LocalDate, val items: List<PlanItem>, val adjustedForMissedDay: Boolean) {
    val totalMinutes: Int get() = items.sumOf { it.minutes }
    val completedMinutes: Int get() = items.filter { it.completed }.sumOf { it.minutes }
    val completion: Double get() = if (totalMinutes == 0) 0.0 else completedMinutes.toDouble() / totalMinutes
}

data class PlannerInput(
    val date: LocalDate,
    val dailyMinutes: Int,
    val dueReviews: Int,
    val unresolvedMistakes: Int,
    val nextLessonId: String?,
    val nextLessonMinutes: Int,
    val skillEstimates: List<SkillEstimate>,
    val goals: Set<LearningGoal>,
    val includeRelaxation: Boolean,
    val missedYesterday: Boolean,
    val preferredSkills: Set<Skill> = emptySet(),
)

/**
 * Builds a daily plan that fits the learner's chosen time budget.
 * Allocation rules (SR-PL-01):
 *  - reviews first, ~0.4 min per due item, capped at 40% of the budget (min 3 min if any due)
 *  - one lesson (its estimated length, capped at 50% of budget)
 *  - remaining time goes to the weakest measured skill, or a goal-aligned skill
 *  - budgets >= 15 min get a short conversation; relaxation is added only if enabled
 *  - after a missed day the plan is NOT enlarged; backlog is spread over coming days instead
 */
object StudyPlanner {
    fun dailyPlan(input: PlannerInput): DailyPlan {
        val budget = input.dailyMinutes.coerceIn(5, 180)
        val items = mutableListOf<PlanItem>()
        var remaining = budget

        if (input.includeRelaxation) {
            items += PlanItem("relax", PlanItemKind.RELAXATION, 2)
            remaining -= 2
        }
        if (input.dueReviews > 0) {
            val cap = (budget * 0.4).roundToInt()
            val m = min(cap, max(3, (input.dueReviews * 0.4).roundToInt()))
            items += PlanItem("review", PlanItemKind.REVIEW, m)
            remaining -= m
        }
        if (input.unresolvedMistakes >= 3 && remaining >= 6) {
            items += PlanItem("mistakes", PlanItemKind.MISTAKE_REVIEW, 3)
            remaining -= 3
        }
        if (input.nextLessonId != null && remaining >= 4) {
            val m = min(input.nextLessonMinutes, max(4, (budget * 0.5).roundToInt())).coerceAtMost(remaining)
            items += PlanItem("lesson", PlanItemKind.LESSON, m, referenceId = input.nextLessonId)
            remaining -= m
        }
        if (budget >= 15 && remaining >= 5) {
            items += PlanItem("conversation", PlanItemKind.CONVERSATION, 5, skill = Skill.SPEAKING)
            remaining -= 5
        }
        if (remaining >= 3) {
            val skill = focusSkill(input)
            val kind = when (skill) {
                Skill.READING -> PlanItemKind.READING
                Skill.LISTENING -> PlanItemKind.LISTENING
                else -> PlanItemKind.SKILL_PRACTICE
            }
            items += PlanItem("practice", kind, remaining, skill = skill)
        }
        return DailyPlan(input.date, items, adjustedForMissedDay = input.missedYesterday)
    }

    fun focusSkill(input: PlannerInput): Skill {
        val measured = input.skillEstimates.filter { it.hasEnoughData && it.skill != Skill.SPEAKING }
        measured.minByOrNull { it.mastery }?.takeIf { it.mastery < 0.75 }?.let { return it.skill }
        input.preferredSkills.firstOrNull()?.let { return it }
        return when {
            LearningGoal.EXAM in input.goals -> Skill.GRAMMAR
            LearningGoal.WORK in input.goals -> Skill.WRITING
            LearningGoal.TRAVEL in input.goals || LearningGoal.CONVERSATION in input.goals -> Skill.LISTENING
            else -> Skill.VOCABULARY
        }
    }

    /** Spreads a review backlog over [days] so no day exceeds [maxPerDay] reviews. */
    fun spreadBacklog(backlog: Int, days: Int, maxPerDay: Int): List<Int> {
        val out = MutableList(days) { 0 }
        var left = backlog
        for (i in 0 until days) {
            val take = min(left, maxPerDay)
            out[i] = take
            left -= take
        }
        if (left > 0) out[days - 1] += left
        return out
    }

    /** Rotating weekly skill focus, interleaving skills instead of blocking them. */
    fun weeklyFocus(start: LocalDate, skills: List<Skill>): List<Pair<LocalDate, Skill>> {
        val pool = skills.ifEmpty { Skill.core }
        return (0 until 7).map { d -> start.plus(DatePeriod(days = d)) to pool[d % pool.size] }
    }

    /** Rough estimate of weeks to reach a target level at the chosen daily time. Labeled as an estimate in the UI. */
    fun estimatedWeeksPerLevel(dailyMinutes: Int, fromLevelOrdinal: Int): Int {
        // Guided-learning-hours style heuristic: higher levels need more hours.
        val hoursForLevel = listOf(40, 60, 100, 180, 200, 250, 300).getOrElse(fromLevelOrdinal) { 300 }
        val hoursPerWeek = max(1.0, dailyMinutes * 6 / 60.0)
        return (hoursForLevel / hoursPerWeek).roundToInt()
    }
}
