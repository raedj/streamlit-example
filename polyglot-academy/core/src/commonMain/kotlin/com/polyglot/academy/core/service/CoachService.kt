package com.polyglot.academy.core.service

import com.polyglot.academy.core.model.Correction
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.repo.ConversationRepository
import com.polyglot.academy.core.repo.ProgressRepository
import com.polyglot.academy.core.repo.ReviewRepository
import com.polyglot.academy.core.repo.VocabularyRepository
import com.polyglot.academy.core.speaking.FluencyCalculator
import com.polyglot.academy.core.speaking.SpeakingProgressCalculator
import com.polyglot.academy.core.tutor.CoachMemory
import com.polyglot.academy.core.tutor.CoachProtocol
import com.polyglot.academy.core.tutor.CoachStats
import com.polyglot.academy.core.util.IdGenerator
import com.polyglot.academy.core.util.TimeSource
import com.polyglot.academy.core.util.toLocalDate
import kotlinx.coroutines.flow.first

/** The coach's memory: review items for the warm-up, the error log, and progress for titles. */
class CoachService(
    private val progress: ProgressRepository,
    private val reviews: ReviewRepository,
    private val vocabulary: VocabularyRepository,
    private val conversations: ConversationRepository,
    private val clock: TimeSource,
    private val ids: IdGenerator,
) {
    suspend fun memory(language: LanguageCode): CoachMemory = CoachMemory(
        warmUp = CoachProtocol.warmUpItems(language, reviews.states(language).first(), vocabulary.userVocabulary(language).first(), clock.nowMillis()),
        recurringErrors = CoachProtocol.recurringErrors(progress.mistakes(language).first()),
    )

    /** Adds conversation corrections to the error log; repeated errors of one type are counted together. */
    suspend fun record(language: LanguageCode, corrections: List<Correction>) {
        corrections.forEach { c -> CoachProtocol.mistakeFor(c, language, ids.newId("mis"), clock.nowMillis())?.let { progress.recordMistake(it) } }
    }

    suspend fun stats(language: LanguageCode): CoachStats {
        val convs = conversations.conversations(language).first()
        val turns = conversations.learnerTurns(language).takeLast(50)
        val fluency = FluencyCalculator.report(language, conversations.learnerTurns(language), clock.nowMillis().toLocalDate())
        return CoachStats(
            wordsInReview = reviews.states(language).first().size,
            conversations = convs.count { c -> c.summary?.let { Regex("learner_turns=(\\d+)").find(it)?.groupValues?.get(1)?.toIntOrNull() ?: 0 }?.let { it >= 4 } == true },
            recentCorrectionsPerTurn = turns.takeIf { it.size >= 10 }?.let { t -> t.sumOf { it.corrections.count { c -> !c.isStylistic } }.toDouble() / t.size },
            missionsPassed = SpeakingProgressCalculator.levels(convs).sumOf { it.passedCount },
            medianWordsPerTurn = fluency.medianWordsPerTurn,
        )
    }
}
