package com.polyglot.academy.core.service

import com.polyglot.academy.core.drills.SpeedDrills
import com.polyglot.academy.core.drills.SprintResult
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.SessionKind
import com.polyglot.academy.core.model.StudySession
import com.polyglot.academy.core.repo.ProgressRepository
import com.polyglot.academy.core.util.IdGenerator
import com.polyglot.academy.core.util.TimeSource
import com.polyglot.academy.core.wordlist.WordEntry
import kotlinx.coroutines.flow.first

/** Records of the skill drills (SkillDrills.kt) that need more than a practice session: speed records. */
class DrillService(
    private val progress: ProgressRepository,
    private val wordLevels: WordLevelService,
    private val clock: TimeSource,
    private val ids: IdGenerator,
) {
    /**
     * Saves a finished word sprint as a study session and puts the missed words into the review queue,
     * like a level test. Returns the best number of correct answers for the level before this one
     * (null for the first sprint), so the screen can say whether it was a new record.
     */
    suspend fun finishSprint(language: LanguageCode, level: Int, result: SprintResult, missed: List<WordEntry>): Int? {
        val before = bestSprint(language, level)
        val now = clock.nowMillis()
        progress.upsertSession(
            StudySession(ids.newId("session"), language, SessionKind.SPEED, SpeedDrills.sprintReference(level),
                now - result.seconds * 1000L, now, result.seconds, result.answered, result.correct, true),
        )
        missed.distinctBy { it.id }.forEach { wordLevels.add(it) }
        return before
    }

    /** Most correct answers in one sprint of [level], or null when none was finished. */
    suspend fun bestSprint(language: LanguageCode, level: Int): Int? =
        progress.sessions(language).first()
            .filter { it.kind == SessionKind.SPEED && it.completed && it.referenceId == SpeedDrills.sprintReference(level) }
            .maxOfOrNull { it.itemsCorrect }
}
