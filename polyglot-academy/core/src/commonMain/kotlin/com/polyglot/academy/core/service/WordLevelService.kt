package com.polyglot.academy.core.service

import com.polyglot.academy.core.content.WordLessons
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.ReviewPhase
import com.polyglot.academy.core.model.ReviewState
import com.polyglot.academy.core.model.SessionKind
import com.polyglot.academy.core.model.StudySession
import com.polyglot.academy.core.repo.ProgressRepository
import com.polyglot.academy.core.repo.ReviewRepository
import com.polyglot.academy.core.srs.SpacedRepetitionScheduler
import com.polyglot.academy.core.util.IdGenerator
import com.polyglot.academy.core.util.TimeSource
import com.polyglot.academy.core.wordlist.WordEntry
import com.polyglot.academy.core.wordlist.WordLevels
import com.polyglot.academy.core.wordlist.WordLists
import kotlinx.coroutines.flow.first
import kotlin.random.Random

/** Progress in one word level: words in the review queue, words that graduated to review, best test score. */
data class WordLevelProgress(val level: Int, val total: Int, val started: Int, val known: Int, val bestTestScore: Double?) {
    val passed: Boolean get() = (bestTestScore ?: 0.0) >= WordLevelService.PASS_SCORE
}

/** What a level-test question shows and what the learner chooses. */
enum class WordTestMode {
    /** The written word; choose its meaning. */
    MEANING,
    /** The meaning; choose the written word. */
    WORD,
    /** The spoken word only (text-to-speech); choose its meaning. */
    LISTENING,
}

/** Generated lessons of one level ([com.polyglot.academy.core.content.WordLessons]): how many are done, and the next one to do. */
data class WordLessonProgress(val done: Int, val total: Int, val next: Int?)

/** One level-test question: [options] are words of the level and the answer is among them. */
data class WordQuizQuestion(val entry: WordEntry, val options: List<WordEntry>, val answerIndex: Int, val mode: WordTestMode = WordTestMode.MEANING)

/** Result of a finished level test; [added] are the missed words newly put into the review queue. */
data class WordTestResult(val level: Int, val correct: Int, val total: Int, val added: Int) {
    val score: Double get() = if (total == 0) 0.0 else correct.toDouble() / total
    val passed: Boolean get() = score >= WordLevelService.PASS_SCORE
}

/**
 * Connects the frequency word lists to learning: per-level progress, adding words to the review
 * queue in rank order, and a level test. Review states use [WordEntry.reviewId], so words that also
 * exist in the hand-written curriculum keep one shared review state.
 */
class WordLevelService(
    private val reviews: ReviewRepository,
    private val progress: ProgressRepository,
    private val scheduler: SpacedRepetitionScheduler,
    private val clock: TimeSource,
    private val ids: IdGenerator,
) {
    suspend fun levels(language: LanguageCode): List<WordLevelProgress> {
        val list = WordLists.load(language)
        val states = reviews.states(language).first().associateBy { it.itemId }
        val best = bestScores(language)
        return (1..list.levelCount).map { level -> summarize(level, list.level(level), states, best[level]) }
    }

    suspend fun level(language: LanguageCode, level: Int): WordLevelProgress {
        val states = reviews.states(language).first().associateBy { it.itemId }
        return summarize(level, WordLists.load(language).level(level), states, bestScores(language)[level])
    }

    /** The level's words, most frequent first (loaded off the main thread). */
    suspend fun words(language: LanguageCode, level: Int): List<WordEntry> = WordLists.load(language).level(level)

    /** Review states of the level's words, keyed by [WordEntry.id]; words never added are absent. */
    suspend fun states(language: LanguageCode, level: Int): Map<String, ReviewState> {
        val states = reviews.states(language).first().associateBy { it.itemId }
        return WordLists.load(language).level(level).mapNotNull { e -> states[e.reviewId]?.let { e.id to it } }.toMap()
    }

    /** Puts the next [count] words of the level that are not yet in the review queue into it, most frequent first. */
    suspend fun addNext(language: LanguageCode, level: Int, count: Int = BATCH): List<WordEntry> {
        val now = clock.nowMillis()
        val added = mutableListOf<WordEntry>()
        for (e in WordLists.load(language).level(level)) {
            if (added.size >= count) break
            if (reviews.state(e.reviewId) == null) { reviews.save(scheduler.newState(e.reviewId, language, now), null); added += e }
        }
        return added
    }

    /** Adds one word to the review queue; returns false when it was already there. */
    suspend fun add(entry: WordEntry): Boolean {
        if (reviews.state(entry.reviewId) != null) return false
        reviews.save(scheduler.newState(entry.reviewId, entry.language, clock.nowMillis()), null)
        return true
    }

    /**
     * A multiple-choice test of [size] words sampled from the level, cycling through [modes]. Wrong
     * options come from the same level, preferably the same part of speech, and never share the
     * answer's meaning or written form, so each question has exactly one right choice in every mode.
     */
    suspend fun quiz(
        language: LanguageCode, level: Int, size: Int = QUIZ_SIZE, seed: Long = clock.nowMillis(),
        modes: List<WordTestMode> = listOf(WordTestMode.MEANING),
    ): List<WordQuizQuestion> {
        require(modes.isNotEmpty()) { "at least one test mode" }
        val words = WordLists.load(language).level(level)
        val random = Random(seed)
        return words.shuffled(random).take(size).mapIndexed { i, entry ->
            fun usable(o: WordEntry) = o.id != entry.id && o.term != entry.term && o.meaning.en != entry.meaning.en && o.meaning.ar != entry.meaning.ar
            val samePos = words.filter { usable(it) && it.partOfSpeech == entry.partOfSpeech }.shuffled(random)
            val others = words.filter { usable(it) && it.partOfSpeech != entry.partOfSpeech }.shuffled(random)
            val distractors = (samePos + others).distinctBy { it.meaning.en }.distinctBy { it.meaning.ar }.distinctBy { it.term }.take(OPTIONS - 1)
            val options = (distractors + entry).shuffled(random)
            WordQuizQuestion(entry, options, options.indexOf(entry), modes[i % modes.size])
        }
    }

    /** Completed generated lessons of the level and the first one not yet completed (null when all are done). */
    suspend fun lessons(language: LanguageCode, level: Int): WordLessonProgress {
        val lp = progress.lessonProgress(language).first()
        val numbers = 1..WordLessons.PER_LEVEL
        val done = numbers.filter { (lp[WordLessons.id(language, level, it)]?.completions ?: 0) > 0 }.toSet()
        return WordLessonProgress(done.size, WordLessons.PER_LEVEL, numbers.firstOrNull { it !in done })
    }

    /** The level to show on the home screen: the first one not yet passed (the last level once all are passed). */
    suspend fun currentLevel(language: LanguageCode): WordLevelProgress {
        val all = levels(language)
        return all.firstOrNull { !it.passed } ?: all.last()
    }

    /** Records a finished test as a study session and puts the missed words into the review queue. */
    suspend fun finishTest(language: LanguageCode, level: Int, correct: Int, total: Int, missed: List<WordEntry>): WordTestResult {
        val now = clock.nowMillis()
        progress.upsertSession(
            StudySession(ids.newId("session"), language, SessionKind.WORD_TEST, testReference(level), now, now, minOf(total * 10, 600), total, correct, true),
        )
        var added = 0
        for (e in missed) if (add(e)) added++
        return WordTestResult(level, correct, total, added)
    }

    private suspend fun bestScores(language: LanguageCode): Map<Int, Double> =
        progress.sessions(language).first()
            .filter { it.kind == SessionKind.WORD_TEST && it.completed && it.itemsCompleted > 0 }
            .mapNotNull { s -> levelOfReference(s.referenceId)?.let { it to s.itemsCorrect.toDouble() / s.itemsCompleted } }
            .groupBy({ it.first }, { it.second }).mapValues { (_, scores) -> scores.max() }

    private fun summarize(level: Int, words: List<WordEntry>, states: Map<String, ReviewState>, best: Double?): WordLevelProgress {
        val own = words.mapNotNull { states[it.reviewId] }
        return WordLevelProgress(level, words.size, own.size, own.count { it.phase == ReviewPhase.REVIEW }, best)
    }

    companion object {
        const val BATCH = 10
        const val QUIZ_SIZE = 20
        const val OPTIONS = 4
        /** Share of correct answers needed to pass a level test. */
        const val PASS_SCORE = 0.8

        fun testReference(level: Int) = "wordlevel:$level"
        fun levelOfReference(reference: String?): Int? =
            reference?.removePrefix("wordlevel:")?.takeIf { reference.startsWith("wordlevel:") }?.toIntOrNull()?.takeIf { it in 1..WordLevels.COUNT }
    }
}
