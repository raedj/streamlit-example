package com.polyglot.academy.core.service

import com.polyglot.academy.core.ai.AiCapability
import com.polyglot.academy.core.ai.AiError
import com.polyglot.academy.core.ai.AiOutcome
import com.polyglot.academy.core.ai.ChatMessage
import com.polyglot.academy.core.ai.ChatRequest
import com.polyglot.academy.core.ai.ChatRole
import com.polyglot.academy.core.ai.ProviderId
import com.polyglot.academy.core.mandarin.isHanzi
import com.polyglot.academy.core.model.Correction
import com.polyglot.academy.core.model.DataCategory
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.Languages
import com.polyglot.academy.core.model.OpenQuestion
import com.polyglot.academy.core.model.ReadingPassage
import com.polyglot.academy.core.tutor.TutorResponseParser
import com.polyglot.academy.core.util.TextNormalizer
import kotlinx.serialization.json.JsonPrimitive

/** How well an answer shows the text was understood. */
enum class Understanding { YES, PARTLY, NO }

data class OpenAnswerFeedback(
    /** Key points found in the answer (the first spelling of each), and those not found. */
    val foundPoints: List<String>,
    val missingPoints: List<String>,
    /** From an AI provider; null offline, where the app can only look for the key points. */
    val understanding: Understanding?,
    val corrections: List<Correction>,
    val improvedVersion: String?,
    val comment: String?,
    /** "offline", or the id of the AI provider that judged the answer. */
    val source: String,
) {
    /** Offline, an answer that has at least half of the key points looks complete. */
    val looksComplete: Boolean get() = foundPoints.size * 2 >= foundPoints.size + missingPoints.size
}

/**
 * Checks answers to [OpenQuestion]s. The offline check is always available; the AI check sends only the
 * learner's answer with the app's own text and question, and only when privacy settings allow writing to be sent.
 */
class ReadingAnswerService(private val providers: ProviderService) {

    suspend fun aiCheck(answer: String, question: OpenQuestion, passage: ReadingPassage, explanationLanguage: LanguageCode): Result<OpenAnswerFeedback> {
        val base = offlineCheck(answer, question, passage.language)
        val target = Languages.of(passage.language).englishName
        val explain = Languages.of(explanationLanguage).englishName
        val system = """You are a reading tutor for a $target learner at level ${passage.level.label}.
The learner read the text below and answered a question about it in $target, in their own words.
First judge whether the answer shows the text was understood: "yes", "partly" or "no". Any answer that is true to the text counts; it does not have to match the model answer.
Then correct the $target of the answer. Separate necessary corrections from optional stylistic alternatives (stylistic: true). Do not correct what is already right.
Write the comment and explanations in $explain, briefly and kindly, at a level the learner can follow. If unsure, say so rather than guessing.
Respond ONLY with JSON: {"understood":"yes|partly|no","comment":"","corrections":[{"original":"","corrected":"","explanation":"","severity":"minor|moderate|meaning","stylistic":false}],"improved_version":""}"""
        val task = if (question.summary) "Task: retell the text in a few sentences." else "Question: ${question.prompt}"
        val user = "Text:\n${passage.sentences.joinToString(" ")}\n\n$task\nModel answer (one possible answer): ${question.modelAnswer}\n\nLearner's answer:\n$answer"
        val outcome = providers.execute(AiCapability.STRUCTURED_FEEDBACK,
            ChatRequest(system, listOf(ChatMessage(ChatRole.USER, user)), setOf(DataCategory.WRITING_SUBMISSION), maxOutputTokens = 900, temperature = 0.2, expectJson = true))
        return when (outcome) {
            is AiOutcome.Failure -> Result.failure(outcome.error)
            is AiOutcome.Success -> parse(outcome.response.text, base, outcome.response.providerId)
        }
    }

    companion object {
        fun offlineCheck(answer: String, question: OpenQuestion, language: LanguageCode): OpenAnswerFeedback {
            val (found, missing) = question.keyPoints.partition { point -> point.split('|').any { contains(answer, it, language) } }
            return OpenAnswerFeedback(found.map(::firstSpelling), missing.map(::firstSpelling), null, emptyList(), null, null, "offline")
        }

        /** Reads the provider's JSON; an answer without a clear judgement is an error, never a guess. */
        internal fun parse(text: String, base: OpenAnswerFeedback, provider: ProviderId): Result<OpenAnswerFeedback> {
            val obj = TutorResponseParser.extractJsonObject(text) ?: return Result.failure(AiError.InvalidResponse(provider, "no JSON feedback"))
            val understood = when ((obj["understood"] as? JsonPrimitive)?.content?.trim()?.lowercase()) {
                "yes" -> Understanding.YES
                "partly" -> Understanding.PARTLY
                "no" -> Understanding.NO
                else -> return Result.failure(AiError.InvalidResponse(provider, "no judgement"))
            }
            return Result.success(base.copy(
                understanding = understood,
                corrections = TutorResponseParser.corrections(obj),
                improvedVersion = (obj["improved_version"] as? JsonPrimitive)?.content?.takeIf { it.isNotBlank() },
                comment = (obj["comment"] as? JsonPrimitive)?.content?.takeIf { it.isNotBlank() },
                source = provider.name,
            ))
        }

        private fun firstSpelling(point: String) = point.substringBefore('|').trim()

        /** Chinese: the characters appear in the answer. Other languages: the word appears, or a longer form of it ("market" in "markets"). */
        private fun contains(answer: String, point: String, language: LanguageCode): Boolean {
            val p = TextNormalizer.loose(point)
            if (p.isEmpty()) return false
            val a = TextNormalizer.loose(answer)
            if (language == LanguageCode.MANDARIN || p.any(::isHanzi)) return p.replace(" ", "") in a.replace(" ", "")
            val words = a.split(' ')
            val pw = p.split(' ')
            return (0..words.size - pw.size).any { start ->
                pw.indices.all { k -> val w = words[start + k]; w == pw[k] || w == pw[k] + "s" || w == pw[k] + "es" || (pw[k].length >= 4 && w.startsWith(pw[k]) && w.length - pw[k].length <= 3) }
            }
        }
    }
}
