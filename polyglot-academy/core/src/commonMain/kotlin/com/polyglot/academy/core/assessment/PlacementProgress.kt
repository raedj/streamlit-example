package com.polyglot.academy.core.assessment

import com.polyglot.academy.core.model.AssessmentResult

/**
 * Whether a learner moved between placement tests: the same test taken again is the app's only
 * measure of learning, as opposed to activity. The first and the latest result are compared.
 */
object PlacementProgress {
    /** Two weeks of study before a retest says something. */
    const val RETEST_AFTER_DAYS = 14

    data class Change(
        val first: AssessmentResult,
        val latest: AssessmentResult,
        /** CEFR steps between the two estimates (A2 to B1 is +1). */
        val levelSteps: Int,
        val firstShareRight: Double,
        val latestShareRight: Double,
    )

    fun change(history: List<AssessmentResult>): Change? {
        val sorted = history.sortedBy { it.takenAtMillis }
        if (sorted.size < 2) return null
        val first = sorted.first()
        val latest = sorted.last()
        return Change(first, latest, latest.estimatedLevel.ordinal - first.estimatedLevel.ordinal, share(first), share(latest))
    }

    fun retestDue(history: List<AssessmentResult>, nowMillis: Long): Boolean {
        val latest = history.maxByOrNull { it.takenAtMillis } ?: return false
        return nowMillis - latest.takenAtMillis >= RETEST_AFTER_DAYS * DAY
    }

    private fun share(a: AssessmentResult) = if (a.questionsAnswered == 0) 0.0 else a.correctAnswers.toDouble() / a.questionsAnswered

    private const val DAY = 24L * 60 * 60 * 1000
}
