package com.polyglot.academy.core.service

import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.repo.ConversationRepository
import com.polyglot.academy.core.speaking.FluencyCalculator
import com.polyglot.academy.core.speaking.FluencyReport
import com.polyglot.academy.core.speaking.LevelProgress
import com.polyglot.academy.core.speaking.SpeakingProgressCalculator
import com.polyglot.academy.core.util.TimeSource
import com.polyglot.academy.core.util.toLocalDate
import kotlinx.coroutines.flow.first

/** Progress on the speaking path and speaking measurements, derived from stored conversations. */
class SpeakingService(private val conversations: ConversationRepository, private val clock: TimeSource) {
    suspend fun progress(language: LanguageCode): List<LevelProgress> =
        SpeakingProgressCalculator.levels(conversations.conversations(language).first())

    suspend fun fluency(language: LanguageCode): FluencyReport =
        FluencyCalculator.report(language, conversations.learnerTurns(language), clock.nowMillis().toLocalDate())
}
