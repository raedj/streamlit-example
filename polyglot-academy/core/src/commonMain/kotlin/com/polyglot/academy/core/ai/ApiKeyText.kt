package com.polyglot.academy.core.ai

/**
 * API keys as people paste them on phones: with spaces, line breaks and invisible direction marks
 * (common with Arabic, Urdu and Hebrew keyboards). None of these belong to a key.
 */
object ApiKeyText {
    private fun invisible(c: Char) = c.isWhitespace() || c == '\uFEFF' || c in '\u200B'..'\u200F' || c in '\u202A'..'\u202E' || c in '\u2060'..'\u2069'

    fun clean(raw: String): String = raw.filterNot(::invisible)

    /** Printable ASCII only, which every provider's keys use and an HTTP header can carry. */
    fun isSendable(key: String): Boolean = key.isNotEmpty() && key.all { it in '!'..'~' }
}
