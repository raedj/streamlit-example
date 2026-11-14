package com.polyglot.academy.core.learning

import com.polyglot.academy.core.model.ReviewPhase
import com.polyglot.academy.core.model.ReviewState

/**
 * Whether a lesson's words have lasted. Passing a lesson only shows the learner could answer straight
 * after studying; a word counts here once spaced reviews have pushed its next review a week away.
 */
object LessonStanding {
    const val HELD_INTERVAL_DAYS = 7.0
    const val HELD_SHARE = 0.8

    fun isHeld(vocabularyIds: List<String>, states: Map<String, ReviewState>): Boolean {
        if (vocabularyIds.isEmpty()) return false
        val held = vocabularyIds.count { id -> states[id]?.let { it.phase == ReviewPhase.REVIEW && it.intervalDays >= HELD_INTERVAL_DAYS } == true }
        return held.toDouble() / vocabularyIds.size >= HELD_SHARE
    }
}
