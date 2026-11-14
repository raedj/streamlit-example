package com.polyglot.academy.core.learning

import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.RecallRating
import com.polyglot.academy.core.model.VocabularyItem
import kotlin.random.Random

/** Recalling a word from its meaning: which answers count, how a verdict schedules it, and tiles for those who cannot type it. */
object Recall {
    private val englishLeadWords = listOf("the ", "to ", "a ", "an ")

    /**
     * Fair answers for a word. English learners are not failed for leaving out "the" or "to"; Mandarin can be
     * answered in characters, either character set, or pinyin with or without tone marks and spaces.
     */
    fun acceptedAnswers(item: VocabularyItem): List<String> = buildList {
        addAll(item.term.split('/').map { it.trim() }.filter { it.isNotEmpty() })
        add(item.term)
        item.traditional?.let { add(it) }
        if (item.language == LanguageCode.ENGLISH) {
            englishLeadWords.firstOrNull { item.term.lowercase().startsWith(it) }?.let { add(item.term.drop(it.length)) }
        }
        if (item.language == LanguageCode.MANDARIN) item.pronunciation?.let { add(it); add(it.replace(" ", "")) }
    }.distinct()

    fun rating(check: AnswerCheck): RecallRating = when {
        check.isCorrect -> RecallRating.GOOD
        check.verdict == AnswerVerdict.ALMOST -> RecallRating.HARD
        else -> RecallRating.AGAIN
    }

    /** Whole words when the answer is long enough to have an order to get right, single letters or characters otherwise. */
    fun usesWordTiles(answer: String): Boolean = answer.trim().split(' ').count { it.isNotEmpty() } >= 3

    /** Shuffled pieces of [answer], each shown once and usable again, with a few distractors taken from [others]. */
    fun tiles(answer: String, others: List<String>, seed: Int): List<String> {
        val random = Random(seed)
        fun words(text: String) = text.trim().split(' ').filter { it.isNotEmpty() }
        fun letters(text: String) = text.lowercase().map { it.toString() }.filter { it != " " }
        val own = if (usesWordTiles(answer)) words(answer).distinct() else letters(answer).distinct() + if (' ' in answer.trim()) listOf(" ") else emptyList()
        val pool = (if (usesWordTiles(answer)) others.flatMap(::words) else others.flatMap(::letters)).distinct().filter { it !in own }
        return (own + pool.shuffled(random).take(3)).shuffled(random)
    }
}
