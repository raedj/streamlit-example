package com.polyglot.academy.core.service

import com.polyglot.academy.core.content.Curriculum
import com.polyglot.academy.core.model.Gloss
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.RecallRating
import com.polyglot.academy.core.model.ReviewPhase
import com.polyglot.academy.core.model.ReviewState
import com.polyglot.academy.core.model.SavedPhrase
import com.polyglot.academy.core.model.VocabularyItem
import com.polyglot.academy.core.repo.ReviewRepository
import com.polyglot.academy.core.repo.VocabularyRepository
import com.polyglot.academy.core.srs.SpacedRepetitionScheduler
import com.polyglot.academy.core.util.TimeSource
import com.polyglot.academy.core.wordlist.WordLists
import kotlinx.coroutines.flow.first

/** A reviewable card: built-in or user vocabulary, a saved phrase, or a word from the frequency word lists. */
data class ReviewCard(val state: ReviewState, val front: String, val back: Gloss, val pronunciation: String?, val example: String?, val vocabulary: VocabularyItem?)

class ReviewService(
    private val reviews: ReviewRepository,
    private val vocabulary: VocabularyRepository,
    private val scheduler: SpacedRepetitionScheduler,
    private val achievements: AchievementService,
    private val clock: TimeSource,
) {
    suspend fun resolve(language: LanguageCode): Map<String, VocabularyItem> {
        val builtIn = Curriculum.of(language).vocabularyById
        val user = vocabulary.userVocabulary(language).first().associateBy { it.id }
        return builtIn + user
    }

    /** Due cards, learning steps first, then overdue reviews (most overdue first), then new cards up to [newLimit]. */
    suspend fun dueQueue(language: LanguageCode, limit: Int = 30, newLimit: Int = 10): List<ReviewCard> {
        val now = clock.nowMillis()
        val items = resolve(language)
        val phrases = vocabulary.savedPhrases(language).first().associateBy { it.id }
        val states = reviews.states(language).first().filter { scheduler.isDue(it, now) }
        val learning = states.filter { it.phase == ReviewPhase.LEARNING || it.phase == ReviewPhase.RELEARNING }.sortedBy { it.dueAtMillis }
        val due = states.filter { it.phase == ReviewPhase.REVIEW }.sortedBy { it.dueAtMillis }
        val fresh = states.filter { it.phase == ReviewPhase.NEW }.take(newLimit)
        return (learning + due + fresh).mapNotNull { s -> card(s, items, phrases) }.take(limit)
    }

    suspend fun dueCount(language: LanguageCode): Int {
        val now = clock.nowMillis()
        return reviews.states(language).first().count { scheduler.isDue(it, now) }
    }

    private fun card(s: ReviewState, items: Map<String, VocabularyItem>, phrases: Map<String, SavedPhrase>): ReviewCard? {
        items[s.itemId]?.let { v -> return ReviewCard(s, v.term, v.meaning, v.pronunciation ?: v.transliteration, v.example, v) }
        phrases[s.itemId]?.let { p -> return ReviewCard(s, p.text, Gloss(p.meaning, p.meaning), null, p.note, null) }
        WordLists.of(s.language).byId[s.itemId]?.toVocabularyItem()?.let { v -> return ReviewCard(s, v.term, v.meaning, v.pronunciation ?: v.transliteration, v.example, v) }
        return null
    }

    suspend fun rate(card: ReviewCard, rating: RecallRating, responseMillis: Long?): Pair<ReviewState, List<String>> {
        val r = scheduler.review(card.state, rating, clock.nowMillis(), responseMillis)
        reviews.save(r.state, r.log)
        return r.state to achievements.evaluateAfterReview(card.state.language)
    }

    fun preview(card: ReviewCard): Map<RecallRating, Long> = scheduler.preview(card.state, clock.nowMillis())

    suspend fun addToReview(language: LanguageCode, itemId: String) {
        if (reviews.state(itemId) == null) reviews.save(scheduler.newState(itemId, language, clock.nowMillis()), null)
    }
}
