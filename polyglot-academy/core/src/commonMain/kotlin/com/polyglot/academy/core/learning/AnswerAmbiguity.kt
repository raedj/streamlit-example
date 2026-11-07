package com.polyglot.academy.core.learning

import com.polyglot.academy.core.model.Gloss

/**
 * Guards single-answer questions against a second correct option: two meanings are treated as
 * overlapping when they share any sense ("big" and "large, big"), in English or in Arabic. Senses
 * are the parts of a gloss separated by commas, semicolons or slashes, without bracketed notes,
 * case or Arabic vowel marks.
 */
object AnswerAmbiguity {
    private val separators = Regex("[,;/،؛]")
    private val brackets = Regex("\\([^)]*\\)")
    private val arabicMarks = Regex("[\\u064B-\\u0652\\u0670\\u0640]")

    fun senses(text: String): Set<String> =
        brackets.replace(text, " ").split(separators)
            .map { arabicMarks.replace(it, "").lowercase().trim().removePrefix("to ").trim() }
            .filter { it.isNotEmpty() }
            .toSet()

    fun overlaps(a: Gloss, b: Gloss): Boolean =
        senses(a.en).intersect(senses(b.en)).isNotEmpty() || senses(a.ar).intersect(senses(b.ar)).isNotEmpty()
}
