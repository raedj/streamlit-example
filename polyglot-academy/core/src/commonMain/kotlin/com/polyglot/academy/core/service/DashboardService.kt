package com.polyglot.academy.core.service

import com.polyglot.academy.core.analytics.AnalyticsCalculator
import com.polyglot.academy.core.analytics.LearningSummary
import com.polyglot.academy.core.content.Curriculum
import com.polyglot.academy.core.learning.MasteryTracker
import com.polyglot.academy.core.learning.NextActivityRecommender
import com.polyglot.academy.core.learning.Recommendation
import com.polyglot.academy.core.learning.RecommendationInput
import com.polyglot.academy.core.learning.SkillEstimate
import com.polyglot.academy.core.model.Achievement
import com.polyglot.academy.core.model.AssessmentResult
import com.polyglot.academy.core.model.CefrLevel
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.LearnerProfile
import com.polyglot.academy.core.model.Lesson
import com.polyglot.academy.core.model.Skill
import com.polyglot.academy.core.planner.DailyPlan
import com.polyglot.academy.core.planner.PlannerInput
import com.polyglot.academy.core.planner.StudyPlanner
import com.polyglot.academy.core.repo.ConversationRepository
import com.polyglot.academy.core.repo.PlanRepository
import com.polyglot.academy.core.repo.ProfileRepository
import com.polyglot.academy.core.repo.ProgressRepository
import com.polyglot.academy.core.repo.ReviewRepository
import com.polyglot.academy.core.util.TimeSource
import com.polyglot.academy.core.util.toLocalDate
import kotlinx.coroutines.flow.first
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus

/** Everything the home dashboard shows, computed from stored events (never fabricated). */
data class DashboardSnapshot(
    val profile: LearnerProfile,
    val language: LanguageCode,
    val level: CefrLevel?,
    val levelIsEstimate: Boolean,
    val recommendation: Recommendation,
    val nextLesson: Lesson?,
    val interruptedLesson: Lesson?,
    val dueReviews: Int,
    val unresolvedMistakes: Int,
    val minutesToday: Int,
    val dailyGoalMinutes: Int,
    val skillEstimates: List<SkillEstimate>,
    val summary: LearningSummary,
    val recentAchievements: List<Achievement>,
    val latestAssessment: AssessmentResult?,
    /** True when the learner has not produced any activity yet; UI shows onboarding guidance instead of statistics. */
    val isNewLearner: Boolean,
)

class DashboardService(
    private val profiles: ProfileRepository,
    private val progress: ProgressRepository,
    private val reviews: ReviewRepository,
    private val conversations: ConversationRepository,
    private val reviewService: ReviewService,
    private val study: StudyService,
    private val clock: TimeSource,
    private val zone: TimeZone = TimeZone.currentSystemDefault(),
) {
    private val analytics = AnalyticsCalculator(zone = zone)

    suspend fun snapshot(): DashboardSnapshot {
        val profile = profiles.currentProfile()
        val lang = profile.activeLearningLanguage
        val enrollment = profile.enrollment(lang)
        val curriculum = Curriculum.of(lang)
        val now = clock.nowMillis()
        val attempts = progress.attempts(lang).first()
        val sessions = progress.sessions(lang).first()
        val lessonProgress = progress.lessonProgress(lang).first()
        val mistakes = progress.mistakes(lang).first().filter { !it.resolved }
        val assessments = progress.assessments(lang).first()
        val states = reviews.states(lang).first()
        val due = reviewService.dueCount(lang)
        val startLevel = enrollment?.startingLevel ?: CefrLevel.A1
        val next = curriculum.sequencer.nextLesson(startLevel, lessonProgress)
        val interrupted = study.interruptedLessonId(lang)?.let { curriculum.lessonsById[it] }
        val today = now.toLocalDate(zone)
        val minutesToday = sessions.filter { it.startedAtMillis.toLocalDate(zone) == today }.sumOf { it.activeSeconds } / 60
        val estimates = MasteryTracker.estimateAll(attempts)
        val rec = NextActivityRecommender.recommend(
            RecommendationInput(
                dueReviewCount = due, unresolvedMistakeCount = mistakes.size, interruptedLessonId = interrupted?.id, nextLessonId = next?.id,
                skillEstimates = estimates, lastSessionKind = sessions.maxByOrNull { it.startedAtMillis }?.kind,
                minutesStudiedToday = minutesToday, dailyGoalMinutes = profile.dailyMinutes,
                hasPlacement = enrollment != null, includeRelaxation = profile.includeRelaxation,
            ),
        )
        val summary = analytics.summarize(sessions, attempts, states, reviews.reviewCount(lang), lessonProgress.values.count { it.completions > 0 },
            conversations.learnerTurnCount(lang), now)
        return DashboardSnapshot(
            profile, lang, enrollment?.startingLevel, enrollment?.levelFromAssessment != true, rec, next, interrupted, due, mistakes.size,
            minutesToday, profile.dailyMinutes, estimates, summary,
            progress.achievements.first().sortedByDescending { it.unlockedAtMillis }.take(3), assessments.lastOrNull(),
            isNewLearner = attempts.isEmpty() && sessions.isEmpty(),
        )
    }
}

class PlanService(
    private val profiles: ProfileRepository,
    private val progress: ProgressRepository,
    private val plans: PlanRepository,
    private val reviewService: ReviewService,
    private val clock: TimeSource,
    private val zone: TimeZone = TimeZone.currentSystemDefault(),
) {
    /** Returns today's stored plan, or generates and stores one. Regenerate with [force] after settings change. */
    suspend fun today(force: Boolean = false): DailyPlan {
        val profile = profiles.currentProfile()
        val lang = profile.activeLearningLanguage
        val today = clock.nowMillis().toLocalDate(zone)
        if (!force) plans.plan(lang, today).first()?.let { return it }
        val curriculum = Curriculum.of(lang)
        val lp = progress.lessonProgress(lang).first()
        val next = curriculum.sequencer.nextLesson(profile.enrollment(lang)?.startingLevel ?: CefrLevel.A1, lp)
        val activeDates = plans.activeDates(lang)
        val plan = StudyPlanner.dailyPlan(
            PlannerInput(
                date = today, dailyMinutes = profile.dailyMinutes, dueReviews = reviewService.dueCount(lang),
                unresolvedMistakes = progress.mistakes(lang).first().count { !it.resolved }, nextLessonId = next?.id,
                nextLessonMinutes = next?.estimatedMinutes ?: 8, skillEstimates = MasteryTracker.estimateAll(progress.attempts(lang).first()),
                goals = profile.goals, includeRelaxation = profile.includeRelaxation,
                missedYesterday = activeDates.isNotEmpty() && today.minus(DatePeriod(days = 1)) !in activeDates,
                preferredSkills = preferredSkills(profile),
            ),
        )
        plans.savePlan(lang, plan)
        return plan
    }

    suspend fun setCompleted(itemId: String, completed: Boolean): DailyPlan {
        val plan = today()
        val updated = plan.copy(items = plan.items.map { if (it.id == itemId) it.copy(completed = completed) else it })
        plans.savePlan(profiles.currentProfile().activeLearningLanguage, updated)
        return updated
    }

    private fun preferredSkills(p: LearnerProfile): Set<Skill> = when (p.learningStyle) {
        com.polyglot.academy.core.model.LearningStyle.AUDITORY -> setOf(Skill.LISTENING)
        com.polyglot.academy.core.model.LearningStyle.READING_WRITING -> setOf(Skill.READING)
        com.polyglot.academy.core.model.LearningStyle.CONVERSATIONAL -> setOf(Skill.SPEAKING)
        else -> emptySet()
    }
}
