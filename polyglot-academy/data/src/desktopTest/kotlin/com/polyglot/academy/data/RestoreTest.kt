package com.polyglot.academy.data

import com.polyglot.academy.core.content.Curriculum
import com.polyglot.academy.core.model.Achievement
import com.polyglot.academy.core.model.AssessmentKind
import com.polyglot.academy.core.model.AssessmentResult
import com.polyglot.academy.core.model.Bookmark
import com.polyglot.academy.core.model.BookmarkKind
import com.polyglot.academy.core.model.CefrLevel
import com.polyglot.academy.core.model.Conversation
import com.polyglot.academy.core.model.ConversationTurn
import com.polyglot.academy.core.model.ErrorSeverity
import com.polyglot.academy.core.model.ExerciseAttempt
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.LessonProgress
import com.polyglot.academy.core.model.Mistake
import com.polyglot.academy.core.model.Note
import com.polyglot.academy.core.model.RecallRating
import com.polyglot.academy.core.model.SavedPhrase
import com.polyglot.academy.core.model.Skill
import com.polyglot.academy.core.model.Speaker
import com.polyglot.academy.core.repo.RestoreException
import com.polyglot.academy.core.srs.SpacedRepetitionScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RestoreTest {
    private fun store() = SqlStore(createJvmDriver(), Dispatchers.IO, TimeZone.UTC)

    private suspend fun SqlStore.fill() {
        updateProfile { it.copy(displayName = "Huda", onboardingCompleted = true) }
        updateSettings { it.copy(privacy = it.privacy.copy(cloudAiEnabled = true, consentVersionAccepted = 1)) }
        saveLessonProgress(LessonProgress("es_l_cafe", LanguageCode.SPANISH, 0.8, 2, 5L, 0))
        recordAttempt(ExerciseAttempt("a1", "es_l_cafe", "e1", LanguageCode.SPANISH, Skill.VOCABULARY, 2, true, 1.0, 3000, 0, 10L, "hola"))
        val sch = SpacedRepetitionScheduler()
        val r = sch.review(sch.newState("es_hola", LanguageCode.SPANISH, 0), RecallRating.GOOD, 0)
        save(r.state, r.log)
        recordMistake(Mistake("m1", LanguageCode.SPANISH, Skill.GRAMMAR, "ex", "Yo ___", "es", "soy", null, ErrorSeverity.MODERATE, "ser", 1L))
        saveAssessment(AssessmentResult("as1", LanguageCode.SPANISH, AssessmentKind.DIAGNOSTIC, 9L, CefrLevel.A2, CefrLevel.A1, CefrLevel.B1, mapOf(Skill.GRAMMAR to 0.6), 12, 8))
        unlock(Achievement("first_lesson", 3L))
        addVocabulary(Curriculum.of(LanguageCode.SPANISH).vocabulary.first().copy(id = "user_v1"))
        savePhrase(SavedPhrase("p1", LanguageCode.SPANISH, "¿Qué tal?", "How are you?", "casual", null, 4L))
        toggleBookmark(Bookmark("b1", BookmarkKind.LESSON, "es_l_cafe", 5L))
        upsertNote(Note("n1", LanguageCode.SPANISH, "Ser/estar", "permanent vs state", 6L, 7L))
        upsertConversation(Conversation("c1", LanguageCode.SPANISH, "lucia", null, 8L, endedAtMillis = 9L, summary = "café"))
        addTurn(ConversationTurn("t1", "c1", Speaker.LEARNER, "Quiero un café", createdAtMillis = 8L))
    }

    @Test fun fullExportRoundTripsIntoAFreshDevice() = runBlocking<Unit> {
        val a = store().apply { fill() }
        val json = a.exportJson(includeConversations = true, includeNotes = true)
        val b = store()
        val report = b.importJson(json)
        assertEquals(2, report.formatVersion)
        assertEquals(0, report.skipped)
        val es = LanguageCode.SPANISH
        assertEquals("Huda", b.currentProfile().displayName)
        assertEquals(a.lessonProgress(es).first(), b.lessonProgress(es).first())
        assertEquals(a.attempts(es).first(), b.attempts(es).first())
        assertEquals(a.states(es).first(), b.states(es).first())
        assertEquals(1, b.reviewCount(es))
        assertEquals(a.mistakes(es).first(), b.mistakes(es).first())
        assertEquals(a.assessments(es).first(), b.assessments(es).first())
        assertEquals(a.userVocabulary(es).first(), b.userVocabulary(es).first())
        assertEquals(a.savedPhrases(es).first(), b.savedPhrases(es).first())
        assertEquals(a.bookmarks().first(), b.bookmarks().first())
        assertEquals(a.notes().first(), b.notes().first())
        assertEquals(a.conversations(es).first(), b.conversations(es).first())
        assertEquals(a.turns("c1").first(), b.turns("c1").first())
        assertFalse(b.unlock(Achievement("first_lesson", 99L)), "achievement should already be restored")
        // The restored data exports again.
        assertTrue("Quiero un café" in b.exportJson(true, true))
    }

    @Test fun restoreTurnsCloudAiOffAndReplacesExistingData() = runBlocking<Unit> {
        val a = store().apply { fill() }
        val json = a.exportJson(includeConversations = false, includeNotes = false)
        val b = store()
        b.upsertNote(Note("old", null, "Will be replaced", "", 1, 1))
        b.importJson(json)
        val privacy = b.settings.first().privacy
        assertFalse(privacy.cloudAiEnabled)
        assertEquals(0, privacy.consentVersionAccepted)
        assertTrue(b.notes().first().isEmpty())
        assertTrue(b.conversations(LanguageCode.SPANISH).first().isEmpty())
    }

    @Test fun invalidFilesAreRejectedWithoutTouchingData() = runBlocking<Unit> {
        val s = store()
        s.updateProfile { it.copy(displayName = "Keep") }
        assertEquals("not_json", assertFailsWith<RestoreException> { s.importJson("not json") }.message)
        assertEquals("not_an_export", assertFailsWith<RestoreException> { s.importJson("""{"format":"other","version":1}""") }.message)
        assertEquals("newer_version", assertFailsWith<RestoreException> { s.importJson("""{"format":"$EXPORT_FORMAT","version":99}""") }.message)
        assertEquals("Keep", s.currentProfile().displayName)
    }

    @Test fun version1ExportsRestoreWhatTheyContainAndSkipReducedRecords() = runBlocking<Unit> {
        val v1 = """{"format":"$EXPORT_FORMAT","version":1,"profile":{"displayName":"Old"},
            "savedPhrases":[{"language":"FRENCH","text":"Salut","meaning":"Hi"}],
            "mistakes":[{"broken":true}]}"""
        val s = store()
        val report = s.importJson(v1)
        assertEquals(1, report.formatVersion)
        assertEquals("Old", s.currentProfile().displayName)
        assertTrue(s.savedPhrases(LanguageCode.FRENCH).first().isEmpty())
        assertEquals(1, report.skipped)
    }
}
