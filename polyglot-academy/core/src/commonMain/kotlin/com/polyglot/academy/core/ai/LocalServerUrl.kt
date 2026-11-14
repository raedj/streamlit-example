package com.polyglot.academy.core.ai

/**
 * The address of a learner's own OpenAI-compatible server (Ollama, LM Studio, llama.cpp server), as
 * people type it. Ollama answers 404 at `http://computer:11434/models`; its OpenAI-compatible API is
 * under `/v1`, so an address without a path gets `/v1`, and a full endpoint pasted by mistake
 * (`…/v1/chat/completions`, `…/v1/models`) is cut back to the base.
 */
object LocalServerUrl {
    private val endpoints = listOf("/chat/completions", "/completions", "/models")

    fun normalize(url: String): String {
        var u = url.trim().trimEnd('/')
        endpoints.firstOrNull { u.endsWith(it) }?.let { u = u.removeSuffix(it).trimEnd('/') }
        val afterScheme = u.substringAfter("://", u)
        if (afterScheme.isNotEmpty() && '/' !in afterScheme) u += "/v1"
        return u
    }

    /** The server root, for Ollama's own API (`/api/tags`). */
    fun root(base: String): String = base.trimEnd('/').removeSuffix("/v1")
}
