package com.polyglot.academy.core.ai

/**
 * Models that reason before they answer, on the OpenAI-style APIs. Their reasoning counts against the
 * output limit, so a short request (the connection test asks for 16 tokens) can end before any text;
 * they get extra room on top of what the request asks for. Recognised by name, as no API reports it.
 */
object ReasoningModels {
    const val EXTRA_TOKENS = 2048

    private val patterns = listOf(
        Regex("^gpt-5"), Regex("^o\\d"), Regex("^gpt-oss"),        // OpenAI (also through Groq and OpenRouter)
        Regex("reasoner"), Regex("(^|-)r1($|-)"),                    // DeepSeek
        Regex("^grok-3-mini"), Regex("^grok-4"),                     // xAI
        Regex("^qwq"), Regex("^qwen3"), Regex("thinking"),           // Qwen and others
        Regex("^magistral"),                                         // Mistral
    )

    /** "openai/o4-mini" on OpenRouter is the same model as "o4-mini". */
    fun isReasoning(model: String): Boolean = model.lowercase().substringAfterLast('/').let { m -> patterns.any { it.containsMatchIn(m) } }

    fun extraTokens(model: String): Int = if (isReasoning(model)) EXTRA_TOKENS else 0
}
