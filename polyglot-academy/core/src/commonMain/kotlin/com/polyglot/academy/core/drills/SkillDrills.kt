package com.polyglot.academy.core.drills

import com.polyglot.academy.core.mandarin.isHanzi
import com.polyglot.academy.core.model.Exercise
import com.polyglot.academy.core.model.Gloss
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.RolePlayScenario
import com.polyglot.academy.core.model.Skill
import com.polyglot.academy.core.util.TextNormalizer
import com.polyglot.academy.core.wordlist.WordEntry
import com.polyglot.academy.core.wordlist.WordList
import kotlin.random.Random

/*
 * Practice for the four skills that lessons cover least: listening to whole sentences, guided
 * conversation, writing sentences that the app compares with a model, and speed. Everything is built
 * from content the app already has (word-list examples and role-play scenarios), so no new text is
 * added that a model or a native speaker has not seen.
 *
 * Scoring follows the app's rule: only answers the app can check are counted. Choosing a meaning is
 * checked. A typed or spoken sentence is compared with the model, but a different sentence can also be
 * right, so it counts only when it matches the model; otherwise the learner sees the comparison.
 */

/** One example sentence of the word list, with the word it illustrates. */
data class SentenceItem(val entry: WordEntry, val sentence: String, val translation: Gloss) {
    val id: String get() = "${entry.id}#${sentence.hashCode().toUInt().toString(36)}"
}

object SentenceDrills {
    /** Sentences shorter than this are word practice, not sentence listening. */
    fun minLength(language: LanguageCode): Int = if (language == LanguageCode.MANDARIN) 6 else 5

    /**
     * Example sentences of [level], longest first within each word, so the practice uses the long
     * examples from level 5 up. Sentences with the same translation are kept once, so two choices in a
     * question never mean the same thing.
     */
    fun pool(list: WordList, level: Int): List<SentenceItem> {
        val min = minLength(list.language)
        val seen = mutableSetOf<String>()
        return list.level(level).flatMap { e ->
            e.examples.filter { length(it.sentence, list.language) >= min }
                .sortedByDescending { length(it.sentence, list.language) }
                .map { SentenceItem(e, it.sentence, it.translation) }
        }.filter { seen.add(TextNormalizer.basic(it.translation.en)) && seen.add("ar:" + TextNormalizer.basic(it.translation.ar)) }
    }

    fun length(sentence: String, language: LanguageCode): Int = tokens(sentence, language).size

    /** Words, or Han characters for Mandarin. */
    fun tokens(sentence: String, language: LanguageCode): List<String> {
        val clean = TextNormalizer.basic(sentence.replace('۔', ' '))
        return if (language == LanguageCode.MANDARIN) clean.filter(::isHanzi).map { it.toString() }
        else clean.split(' ').filter { t -> t.any { it.isLetterOrDigit() } }
    }

    /**
     * A listening set of [count] questions from [level]: the sentence is only heard, and the learner
     * picks its meaning from three. When [dictation] is true (not for Mandarin, which has no word
     * spaces to compare), every second question asks the learner to type what they heard instead.
     */
    fun listening(list: WordList, level: Int, count: Int, seed: Long, dictation: Boolean = true): List<Exercise> {
        val random = Random(seed)
        val pool = pool(list, level)
        if (pool.size < OPTIONS) return emptyList()
        val typed = dictation && list.language != LanguageCode.MANDARIN
        return pool.distinctBy { it.entry.id }.shuffled(random).take(count).mapIndexed { i, item ->
            if (typed && i % 2 == 1) Exercise.Dictation(
                "drill_dict_${item.id}", Skill.LISTENING, Gloss("Listen and type the whole sentence.", "استمع واكتب الجملة كاملة."),
                item.sentence, listOf(item.sentence), difficulty = 4, explanation = item.translation,
            ) else meaningChoice(item, pool, random)
        }
    }

    /** Hear a sentence and choose its meaning. Wrong choices come from other words of the same level. */
    fun meaningChoice(item: SentenceItem, pool: List<SentenceItem>, random: Random): Exercise.MultipleChoice {
        val others = pool.filter { it.entry.id != item.entry.id && it.sentence != item.sentence }.shuffled(random).take(OPTIONS - 1)
        val options = (others + item).shuffled(random)
        return Exercise.MultipleChoice(
            "drill_hear_${item.id}", Skill.LISTENING, Gloss("Listen and choose what the sentence means.", "استمع واختر معنى الجملة."),
            prompt = item.sentence, options = options.map { it.sentence }, correctIndex = options.indexOf(item),
            difficulty = 3, explanation = item.translation, audioText = item.sentence, optionGlosses = options.map { it.translation },
        )
    }

    /** Sentences to write from their meaning, longest examples of the level first. */
    fun writing(list: WordList, level: Int, count: Int, seed: Long): List<SentenceItem> =
        pool(list, level).distinctBy { it.entry.id }.shuffled(Random(seed)).take(count)

    const val OPTIONS = 3
}

enum class TokenStatus { MATCH, MISSING, EXTRA }

data class ComparedToken(val text: String, val status: TokenStatus)

/**
 * The learner's sentence lined up with the model: [tokens] in reading order, where MATCH is shared,
 * MISSING is in the model only and EXTRA is in the learner's sentence only.
 */
data class SentenceComparison(val tokens: List<ComparedToken>, val modelLength: Int) {
    val matched: Int get() = tokens.count { it.status == TokenStatus.MATCH }
    val missing: List<String> get() = tokens.filter { it.status == TokenStatus.MISSING }.map { it.text }
    val extra: List<String> get() = tokens.filter { it.status == TokenStatus.EXTRA }.map { it.text }
    /** Share of the model found in order in the learner's sentence. */
    val coverage: Double get() = if (modelLength == 0) 0.0 else matched.toDouble() / modelLength
    /** Same words in the same order (case, punctuation and Arabic vowel marks ignored). */
    val exact: Boolean get() = modelLength > 0 && missing.isEmpty() && extra.isEmpty()
}

object SentenceCompare {
    /** Longest common subsequence of the two token lists, so word order counts and nothing is matched twice. */
    fun compare(answer: String, model: String, language: LanguageCode): SentenceComparison {
        val a = SentenceDrills.tokens(answer, language)
        val m = SentenceDrills.tokens(model, language)
        fun key(t: String) = TextNormalizer.loose(t)
        val dp = Array(a.size + 1) { IntArray(m.size + 1) }
        for (i in a.indices.reversed()) for (j in m.indices.reversed())
            dp[i][j] = if (key(a[i]) == key(m[j])) dp[i + 1][j + 1] + 1 else maxOf(dp[i + 1][j], dp[i][j + 1])
        val out = mutableListOf<ComparedToken>()
        var i = 0
        var j = 0
        while (i < a.size && j < m.size) {
            when {
                key(a[i]) == key(m[j]) -> { out += ComparedToken(m[j], TokenStatus.MATCH); i++; j++ }
                dp[i + 1][j] >= dp[i][j + 1] -> { out += ComparedToken(a[i], TokenStatus.EXTRA); i++ }
                else -> { out += ComparedToken(m[j], TokenStatus.MISSING); j++ }
            }
        }
        while (i < a.size) out += ComparedToken(a[i++], TokenStatus.EXTRA)
        while (j < m.size) out += ComparedToken(m[j++], TokenStatus.MISSING)
        return SentenceComparison(out, m.size)
    }
}

/** Result of a timed word sprint. */
data class SprintResult(val correct: Int, val wrong: Int, val seconds: Int) {
    val answered: Int get() = correct + wrong
    val accuracy: Double get() = if (answered == 0) 0.0 else correct.toDouble() / answered
    /** Correct answers per minute: speed that does not reward guessing. */
    val perMinute: Double get() = if (seconds <= 0) 0.0 else correct * 60.0 / seconds
}

object SpeedDrills {
    const val SPRINT_SECONDS = 60
    const val MIN_RATE = 0.8f
    const val MAX_RATE = 1.5f
    const val RATE_STEP = 0.1f
    /** Correct answers in a row before the voice gets faster. */
    const val STREAK_TO_SPEED_UP = 2

    fun sprintReference(level: Int) = "speed:words:$level"
    fun listeningReference(level: Int) = "speed:listening:$level"
    fun levelOf(reference: String?): Int? = reference?.substringAfterLast(':')?.toIntOrNull()
}

/**
 * Speech rate for fast listening: it rises by one step after [SpeedDrills.STREAK_TO_SPEED_UP] right
 * answers in a row and falls one step after a wrong answer, within [SpeedDrills.MIN_RATE]..[SpeedDrills.MAX_RATE].
 */
class ListeningPace(start: Float = 1.0f) {
    var rate: Float = start.coerceIn(SpeedDrills.MIN_RATE, SpeedDrills.MAX_RATE)
        private set
    var best: Float = rate
        private set
    private var streak = 0

    fun answer(correct: Boolean): Float {
        if (correct) {
            best = maxOf(best, rate)
            streak++
            if (streak >= SpeedDrills.STREAK_TO_SPEED_UP) { rate = step(rate + SpeedDrills.RATE_STEP); streak = 0 }
        } else {
            streak = 0
            rate = step(rate - SpeedDrills.RATE_STEP)
        }
        return rate
    }

    private fun step(r: Float) = (kotlin.math.round(r * 10) / 10f).coerceIn(SpeedDrills.MIN_RATE, SpeedDrills.MAX_RATE)
}

/** One exchange of a guided conversation: the partner speaks, the learner answers from a hint. */
data class DialogueStep(
    val partnerLine: String,
    val partnerTranslation: Gloss,
    val hint: Gloss,
    val modelAnswer: String,
    val keywords: List<String>,
)

/** Result of one guided answer. */
data class DialogueCheck(val comparison: SentenceComparison, val keywordsFound: List<String>) {
    /**
     * The answer has one of the step's key words, the same rule as the scripted partner in
     * conversations (OfflineTutorEngine), or matches the model. Wording may differ from the model.
     */
    val passed: Boolean get() = comparison.exact || keywordsFound.isNotEmpty()
}

object GuidedDialogue {
    /**
     * The steps of a scenario in order. The opening line asks the first question and each step's
     * partner line is the reply to the step before, so the learner always answers what was just said.
     */
    fun steps(scenario: RolePlayScenario): List<DialogueStep> {
        val lines = listOf(scenario.tutorOpening to scenario.tutorOpeningTranslation) +
            scenario.beats.map { it.tutorLine to it.tutorLineTranslation }
        return scenario.beats.mapIndexed { i, beat ->
            DialogueStep(lines[i].first, lines[i].second, beat.hintForLearner, beat.modelAnswer, beat.expectedKeywords)
        }
    }

    /** Partner line after the last answer, if the scenario has one. */
    fun closingLine(scenario: RolePlayScenario): Pair<String, Gloss>? =
        scenario.beats.lastOrNull()?.let { it.tutorLine to it.tutorLineTranslation }

    fun check(answer: String, step: DialogueStep, language: LanguageCode): DialogueCheck {
        val said = " " + TextNormalizer.loose(answer) + " "
        // As in OfflineTutorEngine: Chinese has no spaces, so a substring; other languages from a word start.
        val found = step.keywords.filter { kw ->
            val k = TextNormalizer.loose(kw)
            k.isNotEmpty() && if (k.any(::isHanzi)) k in said else " $k" in said
        }
        return DialogueCheck(SentenceCompare.compare(answer, step.modelAnswer, language), found)
    }
}
