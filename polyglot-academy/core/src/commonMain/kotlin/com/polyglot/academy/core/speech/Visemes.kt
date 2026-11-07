package com.polyglot.academy.core.speech

import com.polyglot.academy.core.model.LanguageCode

/** Simplified mouth shapes for the 2D tutor avatar. */
enum class MouthShape { REST, CLOSED, OPEN, WIDE, ROUND, TEETH }

/**
 * Approximate mouth shapes from spelling. The text-to-speech engine reports which word is being
 * spoken; within that word the avatar steps through these shapes. This is a visual cue derived
 * from letters, not phoneme-accurate lip-sync, and it is described that way in the docs.
 */
object Visemes {
    fun shapes(word: String, language: LanguageCode): List<MouthShape> =
        if (language == LanguageCode.MANDARIN) word.filter { it in '一'..'鿿' }.flatMap { listOf(MouthShape.OPEN, MouthShape.WIDE) }
        else word.lowercase().mapNotNull { shapeOf(it) }

    /** The shape at [phase] (0..1) through the word currently spoken, or REST if nothing is known. */
    fun shapeAt(word: String?, language: LanguageCode, phase: Float): MouthShape {
        val list = word?.let { shapes(it, language) }.orEmpty()
        if (list.isEmpty()) return MouthShape.REST
        return list[(phase.coerceIn(0f, 0.999f) * list.size).toInt()]
    }

    /** The word containing [charStart] in [text] (TTS range events point at words). */
    fun wordAt(text: String, charStart: Int): String? {
        if (charStart !in text.indices || text[charStart].isWhitespace()) return null
        var s = charStart
        var e = charStart
        while (s > 0 && !text[s - 1].isWhitespace()) s--
        while (e < text.length && !text[e].isWhitespace()) e++
        return text.substring(s, e).takeIf { it.isNotBlank() }
    }

    private fun shapeOf(c: Char): MouthShape? = when (c) {
        'a', 'á', 'à', 'â', 'ا', 'آ', 'ى', 'ع' -> MouthShape.OPEN
        'o', 'ó', 'ô', 'u', 'ú', 'ù', 'û', 'ü', 'w', 'و', 'ؤ' -> MouthShape.ROUND
        'e', 'é', 'è', 'ê', 'i', 'í', 'î', 'y', 'ي', 'ئ' -> MouthShape.WIDE
        'm', 'b', 'p', 'م', 'ب' -> MouthShape.CLOSED
        'f', 'v', 'ف' -> MouthShape.TEETH
        // Other consonants barely change the outline of a small cartoon mouth, so they add no step.
        else -> null
    }
}
