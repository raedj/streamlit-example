package com.polyglot.academy.core.ai

/**
 * API keys as people paste them on phones: with spaces, line breaks and invisible direction marks
 * (common with Arabic, Urdu and Hebrew keyboards). None of these belong to a key.
 */
object ApiKeyText {
    private fun invisible(c: Char) = c.isWhitespace() || c == '\uFEFF' || c in '\u200B'..'\u200F' || c in '\u202A'..'\u202E' || c in '\u2060'..'\u2069'

    fun clean(raw: String): String = raw.filterNot(::invisible)

    // How each provider's keys start, from their key pages. Used only for a hint: formats can change.
    private val prefixes = mapOf(
        ProviderId.GEMINI to "AIza", ProviderId.ANTHROPIC to "sk-ant-", ProviderId.OPENROUTER to "sk-or-",
        ProviderId.OPENAI to "sk-", ProviderId.DEEPSEEK to "sk-", ProviderId.GROQ to "gsk_", ProviderId.XAI to "xai-",
    )

    /** The usual start of [provider]'s keys when [key] does not have it, or null when it looks right. */
    fun unexpectedFormat(provider: ProviderId, key: String): String? {
        val prefix = prefixes[provider] ?: return null
        return prefix.takeIf { !clean(key).startsWith(it) }
    }

    /** Printable ASCII only, which every provider's keys use and an HTTP header can carry. */
    fun isSendable(key: String): Boolean = key.isNotEmpty() && key.all { it in '!'..'~' }
}
