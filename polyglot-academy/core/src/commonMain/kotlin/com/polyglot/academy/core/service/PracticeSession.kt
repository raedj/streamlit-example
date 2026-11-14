package com.polyglot.academy.core.service

import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.SessionKind
import com.polyglot.academy.core.model.StudySession
import com.polyglot.academy.core.util.TimeSource
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Free practice outside lessons (character writing, listening, tones) recorded as a study session,
 * so its time counts toward the daily goal and the progress charts. The session is created on the
 * first answer and updated after each one. Time runs from [openedAtMillis] (when the screen opened);
 * [StudyService.updateSession] caps it per answered item, so a screen left open does not inflate it.
 * Nothing is recorded when no answer is given.
 */
class PracticeSession internal constructor(
    private val study: StudyService,
    private val clock: TimeSource,
    private val language: LanguageCode,
    private val kind: SessionKind,
    private val reference: String?,
    private val openedAtMillis: Long,
) {
    private val lock = Mutex()
    private var session: StudySession? = null
    var answered = 0
        private set
    var correct = 0
        private set

    suspend fun answer(right: Boolean): StudySession = lock.withLock {
        answered++
        if (right) correct++
        val s = session ?: study.startSession(language, kind, reference)
        val seconds = ((clock.nowMillis() - openedAtMillis) / 1000).toInt().coerceAtLeast(1)
        study.updateSession(s, answered, correct, false, seconds).also { session = it }
    }
}
