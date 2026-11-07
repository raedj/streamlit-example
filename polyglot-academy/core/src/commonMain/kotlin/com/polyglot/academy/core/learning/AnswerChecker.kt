package com.polyglot.academy.core.learning

import com.polyglot.academy.core.model.ErrorSeverity
import com.polyglot.academy.core.util.TextNormalizer

enum class AnswerVerdict {
    CORRECT,
    /** Right apart from accents/diacritics/tone marks. Counted correct, with a note. */
    CORRECT_WITH_ACCENT_SLIP,
    /** Close (likely typo). Counted incorrect for scheduling, partial credit for scoring. */
    ALMOST,
    INCORRECT,
    EMPTY,
    /** Typed in another script, so probably the wrong keyboard. Not an answer: the learner is asked to switch. */
    WRONG_KEYBOARD,
}

data class AnswerCheck(
    val verdict: AnswerVerdict,
    val score: Double,
    val closestAccepted: String,
    val severity: ErrorSeverity?,
) {
    val isCorrect: Boolean get() = verdict == AnswerVerdict.CORRECT || verdict == AnswerVerdict.CORRECT_WITH_ACCENT_SLIP
}

/** Deterministic checker for typed answers; does not require any AI service. */
object AnswerChecker {
    const val ALMOST_THRESHOLD = 0.85

    fun check(userAnswer: String, accepted: List<String>): AnswerCheck {
        require(accepted.isNotEmpty()) { "accepted answers must not be empty" }
        val basicUser = TextNormalizer.basic(userAnswer)
        if (basicUser.isEmpty()) return AnswerCheck(AnswerVerdict.EMPTY, 0.0, accepted.first(), null)
        if (isWrongKeyboard(basicUser, accepted)) return AnswerCheck(AnswerVerdict.WRONG_KEYBOARD, 0.0, accepted.first(), null)
        accepted.firstOrNull { TextNormalizer.basic(it) == basicUser }?.let {
            return AnswerCheck(AnswerVerdict.CORRECT, 1.0, it, null)
        }
        val looseUser = TextNormalizer.loose(userAnswer)
        accepted.firstOrNull { TextNormalizer.loose(it) == looseUser }?.let {
            return AnswerCheck(AnswerVerdict.CORRECT_WITH_ACCENT_SLIP, 0.9, it, ErrorSeverity.MINOR)
        }
        val best = accepted.maxBy { TextNormalizer.similarity(userAnswer, it) }
        val sim = TextNormalizer.similarity(userAnswer, best)
        // One slip of the finger in a word of four letters or more is a typo, however short the word.
        val oneSlip = TextNormalizer.levenshtein(looseUser, TextNormalizer.loose(best)) == 1 && looseUser.length >= 4
        return if (sim >= ALMOST_THRESHOLD || oneSlip) {
            AnswerCheck(AnswerVerdict.ALMOST, sim * 0.6, best, ErrorSeverity.MINOR)
        } else {
            AnswerCheck(AnswerVerdict.INCORRECT, 0.0, best, ErrorSeverity.MODERATE)
        }
    }

    private fun hasArabic(text: String) = text.any { it in '\u0600'..'\u06FF' }
    private fun hasLatin(text: String) = text.any { it in 'a'..'z' || it in 'A'..'Z' }

    private fun isWrongKeyboard(answer: String, accepted: List<String>): Boolean =
        (hasArabic(answer) && accepted.none(::hasArabic)) || (hasLatin(answer) && !hasArabic(answer) && accepted.all(::hasArabic))

    /** Word-level diff marking which expected words were missed, for dictation feedback. */
    fun missingWords(userAnswer: String, expected: String): List<String> {
        val have = TextNormalizer.loose(userAnswer).split(' ').toMutableList()
        val missing = mutableListOf<String>()
        for (w in TextNormalizer.basic(expected).split(' ')) {
            val lw = TextNormalizer.loose(w)
            if (!have.remove(lw)) missing += w
        }
        return missing
    }
}
