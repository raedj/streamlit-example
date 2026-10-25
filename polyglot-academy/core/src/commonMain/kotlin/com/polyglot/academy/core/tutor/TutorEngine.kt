package com.polyglot.academy.core.tutor

import com.polyglot.academy.core.ai.ChatMessage
import com.polyglot.academy.core.ai.ChatRole
import com.polyglot.academy.core.model.CefrLevel
import com.polyglot.academy.core.model.CorrectionIntensity
import com.polyglot.academy.core.model.Correction
import com.polyglot.academy.core.model.ErrorSeverity
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.Languages
import com.polyglot.academy.core.model.RolePlayScenario
import com.polyglot.academy.core.speaking.SpeakingMission
import com.polyglot.academy.core.speaking.SpeakingPath
import com.polyglot.academy.core.util.TextNormalizer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject

data class TutorReply(
    val text: String,
    val translation: String?,
    val corrections: List<Correction>,
    val alternatives: List<String>,
    val vocabulary: List<String>,
    val finished: Boolean,
    /** "offline" or a provider id name. */
    val producedBy: String,
    /** True when the reply came from scripted logic rather than a language model. */
    val scripted: Boolean,
    /** The AI tutor judged that the learner completed the speaking mission's tasks. */
    val missionComplete: Boolean = false,
)

data class TutorContext(
    val tutor: TutorPersona,
    val targetLanguage: LanguageCode,
    val explanationLanguage: LanguageCode,
    val level: CefrLevel,
    val intensity: CorrectionIntensity,
    val scenario: RolePlayScenario?,
    val showTranslation: Boolean,
    val recentVocabulary: List<String> = emptyList(),
    val recurringDifficulties: List<String> = emptyList(),
    val mission: SpeakingMission? = null,
    /** A topic the learner typed for a free conversation (already sanitised by [TutorPromptBuilder.cleanTopic]). */
    val topic: String? = null,
)

/** Builds the system prompt sent to cloud/local models. Personality changes instructions, not just the name. */
object TutorPromptBuilder {
    fun systemPrompt(ctx: TutorContext): String {
        val target = Languages.of(ctx.targetLanguage).englishName
        val explain = Languages.of(ctx.explanationLanguage).englishName
        val correction = when (ctx.intensity) {
            CorrectionIntensity.GENTLE -> "Only correct errors that change meaning or block understanding."
            CorrectionIntensity.BALANCED -> "Correct meaning-changing and recurring errors; mention at most one minor error."
            CorrectionIntensity.THOROUGH -> "Correct every error you find, most important first."
        }
        val strategy = when (ctx.tutor.correction) {
            CorrectionStrategy.RECAST -> "Correction style: recast — use the correct form naturally in your reply."
            CorrectionStrategy.PROMPT_SELF_CORRECTION -> "Correction style: point to the error and invite the learner to fix it before giving the answer."
            CorrectionStrategy.EXPLICIT_WITH_RULE -> "Correction style: explicit — give the corrected form and the rule in one sentence."
        }
        val levelGuide = when {
            ctx.level <= CefrLevel.A1 -> "Use very short sentences and the most common words. Explanations may be in $explain."
            ctx.level <= CefrLevel.A2 -> "Use short, simple sentences. Explain grammar in $explain when needed."
            ctx.level <= CefrLevel.B1 -> "Use everyday language. Explain in simple $target, switching to $explain only for difficult points."
            else -> "Use natural language appropriate to level ${ctx.level.label}. Explain in $target."
        }
        return buildString {
            appendLine("You are ${ctx.tutor.name}, a language tutor in a learning app. ${ctx.tutor.behaviourPrompt}")
            appendLine("The learner is practising $target at an estimated CEFR level ${ctx.level.label} (an estimate, not a certified result).")
            appendLine(levelGuide)
            appendLine(correction)
            appendLine(strategy)
            appendLine("Distinguish real errors from optional stylistic alternatives and label alternatives as optional.")
            appendLine("If you are unsure whether something is correct, say so instead of guessing. Do not invent facts about the learner.")
            appendLine("Keep content suitable for a general audience.")
            if (Languages.of(ctx.targetLanguage).has(com.polyglot.academy.core.model.LanguageFeature.TONES)) {
                appendLine("Write Mandarin in simplified characters followed by pinyin with tone marks in parentheses.")
            }
            ctx.scenario?.let {
                appendLine("Role-play scenario: ${it.title.en}. Setting: ${it.setting.en}. Learner goal: ${it.learnerGoal.en}. Stay in the scenario.")
            }
            appendLine(SpeakingPath.coaching(ctx.level))
            appendLine("Keep your own turns to two or three sentences so the learner does most of the talking.")
            ctx.mission?.let { m ->
                appendLine("Speaking mission (${m.level.label}): ${m.title.en}. ${m.brief}")
                appendLine("The learner should: " + m.tasks.mapIndexed { i, t -> "${i + 1}) ${t.en}" }.joinToString("; ") + ".")
                appendLine("Guide the conversation so the learner can do each task, without doing them for the learner.")
                appendLine("If the first learner message is exactly \"$START_SIGNAL\", it is a signal from the app: open the conversation yourself in $target.")
                appendLine("Only when the learner has done every task, say so warmly and set \"mission_complete\" to true; otherwise keep it false.")
            }
            if (ctx.mission == null && ctx.scenario == null) {
                appendLine("This is a free conversation: talk about any topic the learner raises and follow changes of topic naturally. " +
                    "Ask follow-up questions and share short opinions or facts when useful; say so when you are not sure. " +
                    "Politely decline harmful or inappropriate topics and suggest another one.")
                ctx.topic?.let {
                    appendLine("The learner chose this topic: \"$it\". If the first learner message is exactly \"$START_SIGNAL\", it is a signal from the app: " +
                        "open the conversation about this topic yourself in $target, with a question.")
                }
            }
            appendLine("In every reply, first report the learner's errors in the corrections field as described above, then continue the conversation naturally in $target.")
            if (ctx.recentVocabulary.isNotEmpty()) appendLine("Try to reuse some recently studied words: ${ctx.recentVocabulary.take(10).joinToString()}.")
            if (ctx.recurringDifficulties.isNotEmpty()) appendLine("The learner often struggles with: ${ctx.recurringDifficulties.take(5).joinToString()}.")
            appendLine("Respond ONLY with a JSON object of this shape:")
            appendLine("""{"reply": "<your reply in $target>", "reply_translation": "<translation in $explain or empty>", "corrections": [{"original": "...", "corrected": "...", "explanation": "<in $explain>", "severity": "minor|moderate|meaning", "stylistic": false}], "alternatives": ["<optional more natural phrasing>"], "vocabulary": ["<word — meaning in $explain>"]""" +
                (if (ctx.mission != null) """, "mission_complete": false""" else "") + "}")
        }
    }

    /** Sent as the first user message so the AI opens the conversation itself (missions, chosen topics). */
    const val START_SIGNAL = "[start conversation]"

    /** A learner-typed topic made safe to quote in the prompt: one line, no quotes, at most 120 characters. */
    fun cleanTopic(raw: String): String? =
        raw.replace(Regex("[\\r\\n\\t\"|]+"), " ").replace(Regex(" {2,}"), " ").trim().take(120).ifBlank { null }

    fun history(turns: List<Pair<Boolean, String>>, maxTurns: Int = 16): List<ChatMessage> =
        turns.takeLast(maxTurns).map { (isLearner, text) -> ChatMessage(if (isLearner) ChatRole.USER else ChatRole.ASSISTANT, text) }
}

/** Parses model output; tolerates prose around the JSON and falls back to plain text. */
object TutorResponseParser {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    fun parse(raw: String, producedBy: String): TutorReply {
        val obj = extractJsonObject(raw)
        if (obj == null) {
            return TutorReply(raw.trim(), null, emptyList(), emptyList(), emptyList(), false, producedBy, scripted = false)
        }
        fun s(key: String) = (obj[key] as? JsonPrimitive)?.takeIf { it.isString }?.content?.trim()
        fun list(key: String) = (obj[key] as? JsonArray)?.mapNotNull { (it as? JsonPrimitive)?.content?.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
        val corrections = corrections(obj)
        val reply = s("reply")?.takeIf { it.isNotEmpty() } ?: return TutorReply(raw.trim(), null, emptyList(), emptyList(), emptyList(), false, producedBy, false)
        return TutorReply(reply, s("reply_translation")?.takeIf { it.isNotEmpty() }, corrections, list("alternatives"), list("vocabulary"),
            finished = false, producedBy = producedBy, scripted = false,
            missionComplete = (obj["mission_complete"] as? JsonPrimitive)?.content == "true")
    }

    /** Reads a "corrections" array, dropping entries where nothing actually changed. */
    fun corrections(obj: JsonObject): List<Correction> = (obj["corrections"] as? JsonArray)?.mapNotNull { el ->
        val c = el as? JsonObject ?: return@mapNotNull null
        fun cs(k: String) = (c[k] as? JsonPrimitive)?.content?.trim().orEmpty()
        val original = cs("original"); val corrected = cs("corrected")
        if (original.isEmpty() || corrected.isEmpty() || original == corrected) return@mapNotNull null
        Correction(
            original = original, corrected = corrected, explanation = cs("explanation"),
            severity = when (cs("severity").lowercase()) {
                "meaning", "meaning_changing", "major" -> ErrorSeverity.MEANING_CHANGING
                "minor" -> ErrorSeverity.MINOR
                else -> ErrorSeverity.MODERATE
            },
            isStylistic = (c["stylistic"] as? JsonPrimitive)?.content == "true",
        )
    } ?: emptyList()

    fun extractJsonObject(raw: String): JsonObject? {
        val start = raw.indexOf('{')
        if (start < 0) return null
        var depth = 0; var inString = false; var escape = false
        for (i in start until raw.length) {
            val c = raw[i]
            if (inString) {
                when { escape -> escape = false; c == '\\' -> escape = true; c == '"' -> inString = false }
                continue
            }
            when (c) {
                '"' -> inString = true
                '{' -> depth++
                '}' -> { depth--; if (depth == 0) return runCatching { json.parseToJsonElement(raw.substring(start, i + 1)).jsonObject }.getOrNull() }
            }
        }
        return null
    }
}

/**
 * Scripted practice partner that works fully offline. It walks through the beats of a
 * [RolePlayScenario], accepts any reply containing one of the expected keywords, gives hints,
 * and after two unsuccessful tries shows a model answer. It is clearly labelled in the UI as a
 * scripted partner, not an AI model, and does not judge grammar beyond keyword matching and
 * similarity to the model answer.
 */
class OfflineTutorEngine(private val scenario: RolePlayScenario, private val explanationLanguage: LanguageCode) {
    var beatIndex = 0
        private set
    private var misses = 0
    val isFinished: Boolean get() = beatIndex >= scenario.beats.size

    fun opening(): TutorReply = TutorReply(
        scenario.tutorOpening, scenario.tutorOpeningTranslation.forLanguage(explanationLanguage),
        emptyList(), emptyList(), emptyList(), false, OFFLINE, scripted = true,
    )

    fun currentHint(): String? = scenario.beats.getOrNull(beatIndex)?.hintForLearner?.forLanguage(explanationLanguage)
    fun currentModelAnswer(): String? = scenario.beats.getOrNull(beatIndex)?.modelAnswer

    fun respond(learnerText: String): TutorReply {
        val beat = scenario.beats.getOrNull(beatIndex)
            ?: return TutorReply(closing(), null, emptyList(), emptyList(), emptyList(), true, OFFLINE, true)
        val normalized = " " + TextNormalizer.loose(learnerText) + " "
        val matched = beat.expectedKeywords.any { kw ->
            val k = TextNormalizer.loose(kw)
            // Chinese has no spaces: substring match. Others: match on word boundaries.
            if (k.any { it.code in 0x4E00..0x9FFF }) normalized.contains(k) else normalized.contains(" $k ") || normalized.contains(" $k")
        }
        if (matched || misses >= 2) {
            val corrections = mutableListOf<Correction>()
            val sim = TextNormalizer.similarity(learnerText, beat.modelAnswer)
            if (matched && sim in 0.55..0.97) {
                corrections += Correction(learnerText.trim(), beat.modelAnswer, alternativeNote(), ErrorSeverity.MINOR, isStylistic = true)
            }
            val usedModel = !matched
            misses = 0
            beatIndex++
            val finished = isFinished
            val text = if (usedModel) "${beat.modelAnswer}\n\n${beat.tutorLine}" else beat.tutorLine
            return TutorReply(
                text = if (finished) text + "\n\n" + closing() else text,
                translation = beat.tutorLineTranslation.forLanguage(explanationLanguage),
                corrections = corrections,
                alternatives = if (usedModel) listOf(beat.modelAnswer) else emptyList(),
                vocabulary = emptyList(), finished = finished, producedBy = OFFLINE, scripted = true,
            )
        }
        misses++
        return TutorReply(
            text = beat.hintForLearner.forLanguage(explanationLanguage),
            translation = null, corrections = emptyList(), alternatives = listOf(beat.modelAnswer),
            vocabulary = emptyList(), finished = false, producedBy = OFFLINE, scripted = true,
        )
    }

    private fun alternativeNote() = if (explanationLanguage == LanguageCode.ARABIC) "صياغة أخرى شائعة (اختيارية)" else "A common way to say this (optional)"

    private fun closing() = when (scenario.language) {
        LanguageCode.ENGLISH -> "Great job — that's the end of this scenario!"
        LanguageCode.SPANISH -> "¡Muy bien! Hemos terminado esta situación."
        LanguageCode.FRENCH -> "Très bien ! Ce scénario est terminé."
        LanguageCode.MANDARIN -> "很好！这个情景练习结束了。(Hěn hǎo! Zhège qíngjǐng liànxí jiéshù le.)"
        LanguageCode.ARABIC -> "أحسنت! انتهى هذا الموقف."
    }

    companion object { const val OFFLINE = "offline" }
}
