package com.polyglot.academy.core.assessment

import com.polyglot.academy.core.model.AssessmentKind
import com.polyglot.academy.core.model.AssessmentResult
import com.polyglot.academy.core.model.CefrLevel
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.Skill
import com.polyglot.academy.core.wordlist.WordEntry
import com.polyglot.academy.core.wordlist.WordLevels
import com.polyglot.academy.core.wordlist.WordList
import kotlin.random.Random

/**
 * A progress measure that lessons do not teach to: words sampled from across the whole list, most of them
 * not yet studied, produced from their meaning within a time limit. Sampling differently each time avoids the
 * practice effect of retaking the same test. With so few words it is rough, and it measures vocabulary only.
 */
object WordCheck {
    const val PER_LEVEL = 2
    const val SECONDS_PER_WORD = 30L
    const val SIZE = PER_LEVEL * WordLevels.COUNT

    fun sample(list: WordList, seed: Int): List<WordEntry> {
        val random = Random(seed)
        return (1..WordLevels.COUNT).flatMap { level -> list.level(level).shuffled(random).take(PER_LEVEL) }
    }

    /** Words of the list a score suggests, since each sampled word stands for its share of its level. */
    fun estimatedWords(correct: Int): Int = correct * WordLevels.SIZE / PER_LEVEL

    fun result(id: String, language: LanguageCode, nowMillis: Long, answers: List<Pair<WordEntry, Boolean>>): AssessmentResult {
        val correct = answers.count { it.second }
        // The highest band where at least half the sampled words came back; the app does not show it as a level.
        val band = answers.groupBy { WordLevels.cefr(WordLevels.levelOf(it.first.rank)) }
            .filterValues { words -> words.count { it.second } * 2 >= words.size }.keys.maxByOrNull { it.ordinal } ?: CefrLevel.PRE_A1
        return AssessmentResult(
            id = id, language = language, kind = AssessmentKind.PROGRESS_CHECK, takenAtMillis = nowMillis,
            estimatedLevel = band, levelLow = band, levelHigh = band,
            skillScores = mapOf(Skill.VOCABULARY to if (answers.isEmpty()) 0.0 else correct.toDouble() / answers.size),
            questionsAnswered = answers.size, correctAnswers = correct,
        )
    }
}
