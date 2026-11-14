package com.polyglot.academy.core.model

import kotlinx.serialization.Serializable

/**
 * Proficiency bands aligned to CEFR descriptors. In-app placements are estimates, not
 * certified CEFR results.
 */
@Serializable
enum class CefrLevel(val label: String, val ordinalScore: Int) {
    PRE_A1("Pre-A1", 0),
    A1("A1", 1),
    A2("A2", 2),
    B1("B1", 3),
    B2("B2", 4),
    C1("C1", 5),
    C2("C2", 6);

    fun next(): CefrLevel = entries.getOrElse(ordinal + 1) { this }
    fun previous(): CefrLevel = entries.getOrElse(ordinal - 1) { this }

    companion object {
        fun fromScore(score: Double): CefrLevel {
            val idx = score.coerceIn(0.0, 6.0).let { kotlin.math.round(it).toInt() }
            return entries[idx]
        }
    }
}

@Serializable
enum class Skill {
    SPEAKING, LISTENING, READING, WRITING, VOCABULARY, GRAMMAR, PRONUNCIATION, COMPREHENSION, FLUENCY, FUNCTIONAL;

    companion object {
        /** Skills shown in progress summaries; the remainder are derived. */
        val core = listOf(VOCABULARY, GRAMMAR, READING, LISTENING, SPEAKING, WRITING, PRONUNCIATION)
    }
}
