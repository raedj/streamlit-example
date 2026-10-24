package com.polyglot.academy.core.learning

import com.polyglot.academy.core.model.CefrLevel
import com.polyglot.academy.core.model.CourseUnit
import com.polyglot.academy.core.model.ExerciseAttempt
import com.polyglot.academy.core.model.Lesson
import com.polyglot.academy.core.model.LessonProgress
import com.polyglot.academy.core.model.SessionKind
import com.polyglot.academy.core.model.Skill
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/** Mastery estimate for one skill with an explicit uncertainty measure. */
data class SkillEstimate(
    val skill: Skill,
    /** 0..1 */
    val mastery: Double,
    val attempts: Int,
    /** 0..1, grows with evidence. Below 0.3 the estimate should be shown as "not enough data". */
    val confidence: Double,
) {
    val hasEnoughData: Boolean get() = confidence >= 0.3
}

/**
 * Transparent, rule-based mastery tracker. Mastery is a difficulty-weighted exponential moving
 * average of exercise scores, so recent performance counts more than old performance.
 */
object MasteryTracker {
    const val ALPHA = 0.25

    fun estimate(skill: Skill, attempts: List<ExerciseAttempt>): SkillEstimate {
        val relevant = attempts.filter { it.skill == skill }.sortedBy { it.answeredAtMillis }
        if (relevant.isEmpty()) return SkillEstimate(skill, 0.0, 0, 0.0)
        var m = 0.5
        for (a in relevant) {
            // Harder items move the estimate more when answered correctly, easier ones when failed.
            val weight = if (a.correct) 0.6 + a.difficulty * 0.1 else 1.1 - a.difficulty * 0.1
            val hintPenalty = if (a.hintsUsed > 0) 0.85 else 1.0
            val target = a.score * hintPenalty
            m += ALPHA * weight * (target - m)
            m = m.coerceIn(0.0, 1.0)
        }
        val confidence = 1.0 - 1.0 / (1.0 + relevant.size / 6.0)
        return SkillEstimate(skill, m, relevant.size, confidence)
    }

    fun estimateAll(attempts: List<ExerciseAttempt>, skills: List<Skill> = Skill.core): List<SkillEstimate> =
        skills.map { estimate(it, attempts) }
}

enum class ExplanationDepth { BRIEF, STANDARD, DETAILED }

data class DifficultyDecision(
    val difficulty: Int,
    val hintsAvailable: Boolean,
    val explanationDepth: ExplanationDepth,
    /** Machine-readable reason; the UI maps it to a localised sentence. */
    val reason: DifficultyReason,
    val recentAccuracy: Double?,
)

enum class DifficultyReason { NOT_ENOUGH_DATA, STRONG_RECENT_PERFORMANCE, STRUGGLING_RECENTLY, STEADY, LEARNER_PREFERENCE }

/**
 * Adjusts exercise difficulty (1..5) from a sliding window of recent attempts in one skill.
 * Rules (documented in docs/SYSTEM_REQUIREMENTS.md, SR-LE-03):
 *  - fewer than [MIN_WINDOW] attempts: keep the current difficulty
 *  - accuracy >= 0.85 and median response under the slow threshold: +1
 *  - accuracy < 0.6: -1 and enable hints with detailed explanations
 *  - otherwise: keep
 */
object DifficultyAdjuster {
    const val WINDOW = 8
    const val MIN_WINDOW = 4
    const val SLOW_RESPONSE_MILLIS = 20_000L

    fun decide(
        current: Int,
        recentForSkill: List<ExerciseAttempt>,
        preferHarder: Boolean = false,
        hintsPreferred: Boolean = true,
    ): DifficultyDecision {
        val window = recentForSkill.sortedByDescending { it.answeredAtMillis }.take(WINDOW)
        if (window.size < MIN_WINDOW) {
            val d = if (preferHarder) min(5, current + 1) else current
            return DifficultyDecision(d.coerceIn(1, 5), hintsPreferred, ExplanationDepth.STANDARD,
                if (preferHarder) DifficultyReason.LEARNER_PREFERENCE else DifficultyReason.NOT_ENOUGH_DATA, null)
        }
        val accuracy = window.count { it.correct }.toDouble() / window.size
        val sortedTimes = window.map { it.responseMillis }.sorted()
        val median = sortedTimes[sortedTimes.size / 2]
        return when {
            accuracy >= 0.85 && median < SLOW_RESPONSE_MILLIS ->
                DifficultyDecision(min(5, current + 1), hintsPreferred && current + 1 < 4, ExplanationDepth.BRIEF, DifficultyReason.STRONG_RECENT_PERFORMANCE, accuracy)
            accuracy < 0.6 ->
                DifficultyDecision(max(1, current - 1), true, ExplanationDepth.DETAILED, DifficultyReason.STRUGGLING_RECENTLY, accuracy)
            else -> DifficultyDecision(current.coerceIn(1, 5), hintsPreferred, ExplanationDepth.STANDARD, DifficultyReason.STEADY, accuracy)
        }
    }
}

/**
 * Orders lessons for a language. Lessons below the learner's starting level are treated as
 * optional review; the next lesson is the first unmastered lesson at or above the starting level
 * whose prerequisites are mastered (or optional).
 */
class LessonSequencer(private val units: List<CourseUnit>, private val lessons: Map<String, Lesson>) {

    fun orderedLessons(): List<Lesson> = units
        .sortedWith(compareBy({ it.level.ordinal }, { units.indexOf(it) }))
        .flatMap { u -> u.lessonIds.mapNotNull { lessons[it] } }

    fun nextLesson(startingLevel: CefrLevel, progress: Map<String, LessonProgress>): Lesson? {
        val ordered = orderedLessons()
        return ordered.firstOrNull { lesson ->
            lesson.level >= startingLevel &&
                progress[lesson.id]?.mastered != true &&
                lesson.prerequisites.all { pre -> progress[pre]?.mastered == true || (lessons[pre]?.level ?: lesson.level) < startingLevel }
        } ?: ordered.firstOrNull { progress[it.id]?.mastered != true }
    }

    fun isUnlocked(lesson: Lesson, startingLevel: CefrLevel, progress: Map<String, LessonProgress>): Boolean =
        lesson.level < startingLevel || lesson.prerequisites.all { pre ->
            progress[pre]?.mastered == true || (lessons[pre]?.level ?: lesson.level) < startingLevel
        }

    /** Fraction of lessons at a level that are mastered. */
    fun levelCompletion(level: CefrLevel, progress: Map<String, LessonProgress>): Double {
        val atLevel = orderedLessons().filter { it.level == level }
        if (atLevel.isEmpty()) return 0.0
        return atLevel.count { progress[it.id]?.mastered == true }.toDouble() / atLevel.size
    }
}

enum class RecommendationKind { REVIEW_DUE, MISTAKE_REVIEW, CONTINUE_LESSON, NEXT_LESSON, SKILL_PRACTICE, CONVERSATION, ASSESSMENT, RELAXATION, ALL_DONE }

enum class RecommendationReason {
    MANY_REVIEWS_DUE,
    RECURRING_MISTAKES,
    LESSON_INTERRUPTED,
    NEXT_IN_PATH,
    WEAKEST_SKILL,
    INTERLEAVE_SKILLS,
    APPLY_IN_CONVERSATION,
    NO_PLACEMENT_YET,
    GOAL_MET_TODAY,
    PRE_SPEAKING_CALM,
}

data class Recommendation(
    val kind: RecommendationKind,
    val referenceId: String?,
    val reason: RecommendationReason,
    val skill: Skill? = null,
    val estimatedMinutes: Int = 5,
)

data class RecommendationInput(
    val dueReviewCount: Int,
    val unresolvedMistakeCount: Int,
    val interruptedLessonId: String?,
    val nextLessonId: String?,
    val skillEstimates: List<SkillEstimate>,
    val lastSessionKind: SessionKind?,
    val minutesStudiedToday: Int,
    val dailyGoalMinutes: Int,
    val hasPlacement: Boolean,
    val includeRelaxation: Boolean,
)

/**
 * Picks the single most useful next activity. Rules are applied in priority order so the
 * choice is explainable; [RecommendationReason] is shown to the learner.
 */
object NextActivityRecommender {
    const val REVIEW_THRESHOLD = 10
    const val MISTAKE_THRESHOLD = 5

    fun recommend(i: RecommendationInput): Recommendation {
        if (!i.hasPlacement) return Recommendation(RecommendationKind.ASSESSMENT, null, RecommendationReason.NO_PLACEMENT_YET, estimatedMinutes = 10)
        i.interruptedLessonId?.let { return Recommendation(RecommendationKind.CONTINUE_LESSON, it, RecommendationReason.LESSON_INTERRUPTED) }
        if (i.dueReviewCount >= REVIEW_THRESHOLD && i.lastSessionKind != SessionKind.REVIEW) {
            return Recommendation(RecommendationKind.REVIEW_DUE, null, RecommendationReason.MANY_REVIEWS_DUE,
                estimatedMinutes = (i.dueReviewCount / 4).coerceIn(3, 15))
        }
        if (i.unresolvedMistakeCount >= MISTAKE_THRESHOLD && i.lastSessionKind != SessionKind.REVIEW) {
            return Recommendation(RecommendationKind.MISTAKE_REVIEW, null, RecommendationReason.RECURRING_MISTAKES)
        }
        if (i.minutesStudiedToday >= i.dailyGoalMinutes && i.dueReviewCount == 0) {
            return Recommendation(RecommendationKind.ALL_DONE, null, RecommendationReason.GOAL_MET_TODAY, estimatedMinutes = 0)
        }
        // Interleave: after a lesson, practise the weakest measured skill (or converse).
        if (i.lastSessionKind == SessionKind.LESSON) {
            val weakest = i.skillEstimates.filter { it.hasEnoughData }.minByOrNull { it.mastery }
            if (weakest != null && weakest.mastery < 0.7) {
                return if (weakest.skill == Skill.SPEAKING) {
                    if (i.includeRelaxation) Recommendation(RecommendationKind.RELAXATION, null, RecommendationReason.PRE_SPEAKING_CALM, estimatedMinutes = 2)
                    else Recommendation(RecommendationKind.CONVERSATION, null, RecommendationReason.WEAKEST_SKILL, Skill.SPEAKING)
                } else Recommendation(RecommendationKind.SKILL_PRACTICE, null, RecommendationReason.WEAKEST_SKILL, weakest.skill)
            }
            return Recommendation(RecommendationKind.CONVERSATION, null, RecommendationReason.APPLY_IN_CONVERSATION, Skill.SPEAKING, 8)
        }
        i.nextLessonId?.let { return Recommendation(RecommendationKind.NEXT_LESSON, it, RecommendationReason.NEXT_IN_PATH, estimatedMinutes = 8) }
        if (i.dueReviewCount > 0) return Recommendation(RecommendationKind.REVIEW_DUE, null, RecommendationReason.MANY_REVIEWS_DUE)
        return Recommendation(RecommendationKind.CONVERSATION, null, RecommendationReason.APPLY_IN_CONVERSATION, Skill.SPEAKING, 8)
    }
}

/** Rolling accuracy with a Wilson lower bound, useful to avoid over-reacting to small samples. */
fun wilsonLowerBound(correct: Int, total: Int, z: Double = 1.96): Double {
    if (total == 0) return 0.0
    val p = correct.toDouble() / total
    val denom = 1 + z * z / total
    val centre = p + z * z / (2 * total)
    val margin = z * sqrt(p * (1 - p) / total + z * z / (4.0 * total * total))
    return (centre - margin) / denom
}
