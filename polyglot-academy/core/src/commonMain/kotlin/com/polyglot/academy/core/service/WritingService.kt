package com.polyglot.academy.core.service

import com.polyglot.academy.core.ai.AiCapability
import com.polyglot.academy.core.ai.AiOutcome
import com.polyglot.academy.core.ai.ChatMessage
import com.polyglot.academy.core.ai.ChatRequest
import com.polyglot.academy.core.ai.ChatRole
import com.polyglot.academy.core.mandarin.isHanzi
import com.polyglot.academy.core.model.CefrLevel
import com.polyglot.academy.core.model.Correction
import com.polyglot.academy.core.model.DataCategory
import com.polyglot.academy.core.model.Exercise
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.Languages
import com.polyglot.academy.core.tutor.TutorResponseParser
import com.polyglot.academy.core.util.TextNormalizer
import kotlinx.serialization.json.JsonPrimitive

enum class WritingCheck { TOO_SHORT, SUGGESTED_WORDS_MISSING, REPEATED_WORDS, MISSING_CAPITAL, MISSING_END_PUNCTUATION, GOOD_LENGTH, USED_SUGGESTED_WORDS }

data class WritingFeedback(
    val wordCount: Int,
    val checks: List<WritingCheck>,
    val usedSuggestions: List<String>,
    val missingSuggestions: List<String>,
    val repeatedWords: List<String>,
    /** From an AI provider, when allowed; null in offline mode. */
    val corrections: List<Correction>,
    val improvedVersion: String?,
    val overallComment: String?,
    val source: String,
)

/** Offline checks are always available; AI corrections are added only when privacy settings allow. */
class WritingService(private val providers: ProviderService) {

    fun countWords(text: String, language: LanguageCode): Int =
        if (language == LanguageCode.MANDARIN) text.count(::isHanzi) else text.split(Regex("\\s+")).count { it.any(Char::isLetter) }

    fun offlineFeedback(text: String, prompt: Exercise.FreeWriting, language: LanguageCode): WritingFeedback {
        val words = countWords(text, language)
        val normalized = TextNormalizer.loose(text)
        val used = prompt.suggestedVocabulary.filter { TextNormalizer.loose(it) in normalized }
        val missing = prompt.suggestedVocabulary - used.toSet()
        val tokens = TextNormalizer.basic(text).split(' ').filter { it.length > 3 }
        val repeated = tokens.groupingBy { it }.eachCount().filter { it.value >= 3 }.keys.toList()
        val checks = buildList {
            if (words < prompt.minWords) add(WritingCheck.TOO_SHORT) else add(WritingCheck.GOOD_LENGTH)
            if (missing.isNotEmpty() && used.isEmpty()) add(WritingCheck.SUGGESTED_WORDS_MISSING)
            if (used.isNotEmpty()) add(WritingCheck.USED_SUGGESTED_WORDS)
            if (repeated.isNotEmpty()) add(WritingCheck.REPEATED_WORDS)
            val latin = language in setOf(LanguageCode.ENGLISH, LanguageCode.SPANISH, LanguageCode.FRENCH, LanguageCode.TURKISH, LanguageCode.MALAY)
            val trimmed = text.trim()
            if (latin && trimmed.firstOrNull()?.let { it.isLetter() && it.isLowerCase() } == true) add(WritingCheck.MISSING_CAPITAL)
            if (trimmed.isNotEmpty() && trimmed.last() !in ".!?。！？؟…۔") add(WritingCheck.MISSING_END_PUNCTUATION)
        }
        return WritingFeedback(words, checks, used, missing, repeated, emptyList(), null, null, "offline")
    }

    suspend fun aiFeedback(text: String, prompt: Exercise.FreeWriting, language: LanguageCode, explanationLanguage: LanguageCode, level: CefrLevel): Result<WritingFeedback> {
        val base = offlineFeedback(text, prompt, language)
        val target = Languages.of(language).englishName
        val explain = Languages.of(explanationLanguage).englishName
        val system = """You are a writing tutor for a $target learner at estimated level ${level.label}.
Correct the learner's text. Separate necessary corrections from optional stylistic alternatives (stylistic: true).
Explain each correction briefly in $explain. If unsure, say so rather than guessing.
Respond ONLY with JSON: {"corrections":[{"original":"","corrected":"","explanation":"","severity":"minor|moderate|meaning","stylistic":false}],"improved_version":"","comment":""}"""
        val outcome = providers.execute(AiCapability.STRUCTURED_FEEDBACK,
            ChatRequest(system, listOf(ChatMessage(ChatRole.USER, "Task: ${prompt.prompt.en}\n\nText:\n$text")), setOf(DataCategory.WRITING_SUBMISSION), maxOutputTokens = 900, temperature = 0.2, expectJson = true))
        return when (outcome) {
            is AiOutcome.Failure -> Result.failure(outcome.error)
            is AiOutcome.Success -> {
                val obj = TutorResponseParser.extractJsonObject(outcome.response.text)
                    ?: return Result.failure(com.polyglot.academy.core.ai.AiError.InvalidResponse(outcome.response.providerId, "no JSON feedback"))
                Result.success(base.copy(
                    corrections = TutorResponseParser.corrections(obj),
                    improvedVersion = (obj.get("improved_version") as? JsonPrimitive)?.content?.takeIf { it.isNotBlank() },
                    overallComment = (obj.get("comment") as? JsonPrimitive)?.content?.takeIf { it.isNotBlank() },
                    source = outcome.response.providerId.name,
                ))
            }
        }
    }
}
