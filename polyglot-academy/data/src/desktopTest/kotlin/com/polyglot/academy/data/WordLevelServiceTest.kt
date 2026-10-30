package com.polyglot.academy.data

import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.RecallRating
import com.polyglot.academy.core.service.AppServices
import com.polyglot.academy.core.service.WordLevelService
import com.polyglot.academy.core.util.FixedTimeSource
import com.polyglot.academy.core.util.SequentialIdGenerator
import com.polyglot.academy.core.wordlist.WordLists
import io.ktor.client.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WordLevelServiceTest {
    private val clock = FixedTimeSource(1_761_300_000_000L)
    private val store = SqlStore(createJvmDriver(), Dispatchers.IO, TimeZone.UTC, clock)
    private val app = AppServices(store, FakePlatform(), HttpClient(), clock, SequentialIdGenerator())

    @Test fun addNextTakesUnstartedWordsInRankOrderAndShowsThemInReview() = runBlocking<Unit> {
        val lang = LanguageCode.SPANISH
        val level = WordLists.of(lang).level(4)
        val first = app.wordLevels.addNext(lang, 4)
        assertEquals(level.take(10).map { it.id }, first.map { it.id })
        val second = app.wordLevels.addNext(lang, 4)
        assertEquals(level.drop(10).take(10).map { it.id }, second.map { it.id })
        assertEquals(20, app.wordLevels.level(lang, 4).started)
        // Word-list words that are not in the hand-written curriculum still become review cards.
        val queue = app.reviews.dueQueue(lang, limit = 50, newLimit = 50)
        assertTrue(queue.any { it.front == level.first().term })
        val card = queue.first { it.front == level.first().term }
        app.reviews.rate(card, RecallRating.EASY, 1000)
        assertEquals(1, app.wordLevels.level(lang, 4).known)
        assertFalse(app.wordLevels.add(level.first()))
    }

    @Test fun quizHasFourDistinctOptionsWithTheAnswerAmongThem() {
        LanguageCode.entries.forEach { lang ->
            val quiz = app.wordLevels.quiz(lang, 10, seed = 7)
            assertEquals(WordLevelService.QUIZ_SIZE, quiz.size)
            assertEquals(quiz.size, quiz.map { it.entry.id }.toSet().size)
            quiz.forEach { q ->
                assertEquals(WordLevelService.OPTIONS, q.options.size)
                assertEquals(q.entry, q.options[q.answerIndex])
                assertEquals(q.options.size, q.options.map { it.meaning.en }.toSet().size, "${lang.tag}: ${q.entry.term}")
                assertTrue(q.options.all { it.level == 10 })
            }
            assertEquals(quiz.map { it.entry.id }, app.wordLevels.quiz(lang, 10, seed = 7).map { it.entry.id })
        }
    }

    @Test fun finishedTestKeepsBestScoreAndQueuesMissedWords() = runBlocking<Unit> {
        val lang = LanguageCode.TURKISH
        assertNull(app.wordLevels.level(lang, 2).bestTestScore)
        val missed = WordLists.of(lang).level(2).take(3)
        val first = app.wordLevels.finishTest(lang, 2, 17, 20, missed)
        assertTrue(first.passed)
        assertEquals(3, first.added)
        app.wordLevels.finishTest(lang, 2, 10, 20, missed)
        val progress = app.wordLevels.level(lang, 2)
        assertEquals(0.85, progress.bestTestScore)
        assertTrue(progress.passed)
        assertEquals(3, progress.started)
        assertNull(app.wordLevels.level(lang, 3).bestTestScore)
        assertEquals(10, app.wordLevels.levels(lang).size)
    }

    @Test fun testReferencesRoundTrip() {
        assertEquals(7, WordLevelService.levelOfReference(WordLevelService.testReference(7)))
        assertNull(WordLevelService.levelOfReference("lesson:7"))
        assertNull(WordLevelService.levelOfReference("wordlevel:11"))
        assertNull(WordLevelService.levelOfReference(null))
    }
}
