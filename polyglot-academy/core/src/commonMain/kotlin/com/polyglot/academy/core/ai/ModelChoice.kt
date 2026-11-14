package com.polyglot.academy.core.ai

/** Picks a model the account actually has, once its model list is loaded. */
object ModelChoice {
    /**
     * The model to use: the chosen one (or the provider's default) when the account lists it, otherwise
     * a fast general model from the list (names with "flash", "mini" or "haiku", newest name first), otherwise
     * the first listed. Null when the list is empty or the current choice is fine.
     */
    fun replacementFor(current: String?, defaultModel: String, available: List<String>): String? {
        if (available.isEmpty()) return null
        val wanted = current?.trim()?.ifBlank { null } ?: defaultModel
        if (wanted in available) return null
        val stable = available.filterNot { n -> listOf("preview", "exp", "thinking", "tts", "transcribe", "audio", "realtime", "search", "image", "embedding").any { it in n.lowercase() } }.ifEmpty { available }
        val fast = stable.filter { n -> listOf("flash", "mini", "haiku").any { it in n.lowercase() } && "lite" !in n.lowercase() }
        return (fast.ifEmpty { stable }).sortedDescending().first()
    }
}
