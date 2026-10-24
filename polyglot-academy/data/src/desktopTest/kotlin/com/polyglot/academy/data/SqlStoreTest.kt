package com.polyglot.academy.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.polyglot.academy.core.ai.ProviderConfig
import com.polyglot.academy.core.ai.ProviderId
import com.polyglot.academy.core.content.Curriculum
import com.polyglot.academy.core.model.Achievement
import com.polyglot.academy.core.model.AssessmentKind
import com.polyglot.academy.core.model.AssessmentResult
import com.polyglot.academy.core.model.Bookmark
import com.polyglot.academy.core.model.BookmarkKind
import com.polyglot.academy.core.model.CefrLevel
import com.polyglot.academy.core.model.Conversation
import com.polyglot.academy.core.model.ConversationTurn
import com.polyglot.academy.core.model.Correction
import com.polyglot.academy.core.model.ErrorSeverity
import com.polyglot.academy.core.model.ExerciseAttempt
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.LessonProgress
import com.polyglot.academy.core.model.Mistake
import com.polyglot.academy.core.model.Note
import com.polyglot.academy.core.model.RecallRating
import com.polyglot.academy.core.model.Skill
import com.polyglot.academy.core.model.Speaker
import com.polyglot.academy.core.planner.DailyPlan
import com.polyglot.academy.core.planner.PlanItem
import com.polyglot.academy.core.planner.PlanItemKind
import com.polyglot.academy.core.srs.SpacedRepetitionScheduler
import com.polyglot.academy.data.db.PolyglotDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import java.io.File
import java.nio.file.Files
import java.util.Properties
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SqlStoreTest {
    private fun store() = SqlStore(createJvmDriver(), Dispatchers.IO, TimeZone.UTC)

    @Test fun profileAndSettingsDefaultsAndUpdates() = runBlocking<Unit> {
        val s = store()
        assertFalse(s.currentProfile().onboardingCompleted)
        s.updateProfile { it.copy(displayName = "Laila", interfaceLanguage = LanguageCode.ARABIC, onboardingCompleted = true) }
        assertEquals("Laila", s.profile.first().displayName)
        assertTrue(s.currentProfile().onboardingCompleted)
        s.updateSettings { it.copy(privacy = it.privacy.copy(cloudAiEnabled = true)) }
        assertTrue(s.settings.first().privacy.cloudAiEnabled)
    }

    @Test fun attemptsSessionsAndProgressRoundTrip() = runBlocking<Unit> {
        val s = store()
        repeat(3) { i ->
            s.recordAttempt(ExerciseAttempt("a$i", "l1", "e$i", LanguageCode.SPANISH, Skill.GRAMMAR, 2, i != 1, if (i != 1) 1.0 else 0.0, 4000, 0, 1000L + i, "x"))
        }
        assertEquals(3, s.attempts(LanguageCode.SPANISH).first().size)
        assertEquals(listOf("a2", "a1"), s.recentAttempts(LanguageCode.SPANISH, 2).map { it.id })
        assertTrue(s.attempts(LanguageCode.FRENCH).first().isEmpty())
        s.saveLessonProgress(LessonProgress("es_l_cafe", LanguageCode.SPANISH, 0.9, 1, 5L, 0))
        assertTrue(s.lessonProgress(LanguageCode.SPANISH).first().getValue("es_l_cafe").mastered)
    }

    @Test fun repeatedMistakesAreCountedNotDuplicated() = runBlocking<Unit> {
        val s = store()
        val m = Mistake("m1", LanguageCode.ENGLISH, Skill.GRAMMAR, "ex1", "I ___ a student", "is", "am", null, ErrorSeverity.MODERATE, "to_be", 1L)
        s.recordMistake(m)
        s.recordMistake(m.copy(id = "m2", userAnswer = "are", occurredAtMillis = 2L))
        val list = s.mistakes(LanguageCode.ENGLISH).first()
        assertEquals(1, list.size)
        assertEquals(2, list[0].occurrences)
        assertEquals("are", list[0].userAnswer)
        s.resolveMistake("m1")
        assertTrue(s.mistakes(LanguageCode.ENGLISH).first().single().resolved)
        // A new mistake after resolution creates a fresh record.
        s.recordMistake(m.copy(id = "m3"))
        assertEquals(2, s.mistakes(LanguageCode.ENGLISH).first().size)
    }

    @Test fun reviewStatesAndLogs() = runBlocking<Unit> {
        val s = store()
        val sch = SpacedRepetitionScheduler()
        val r = sch.review(sch.newState("en_hello", LanguageCode.ENGLISH, 0), RecallRating.GOOD, 0)
        s.save(r.state, r.log)
        assertEquals(r.state, s.state("en_hello"))
        assertEquals(1, s.states(LanguageCode.ENGLISH).first().size)
        assertEquals(1, s.reviewCount(LanguageCode.ENGLISH))
    }

    @Test fun assessmentsAchievementsAndPlans() = runBlocking<Unit> {
        val s = store()
        s.saveAssessment(AssessmentResult("as1", LanguageCode.MANDARIN, AssessmentKind.DIAGNOSTIC, 10L, CefrLevel.A1, CefrLevel.PRE_A1, CefrLevel.A2,
            mapOf(Skill.GRAMMAR to 0.5), 10, 6))
        assertEquals(mapOf(Skill.GRAMMAR to 0.5), s.assessments(LanguageCode.MANDARIN).first().single().skillScores)
        assertTrue(s.unlock(Achievement("first_lesson", 1)))
        assertFalse(s.unlock(Achievement("first_lesson", 2)))
        val plan = DailyPlan(LocalDate(2026, 10, 24), listOf(PlanItem("review", PlanItemKind.REVIEW, 5)), false)
        s.savePlan(LanguageCode.ENGLISH, plan)
        assertEquals(plan, s.plan(LanguageCode.ENGLISH, LocalDate(2026, 10, 24)).first())
        assertNull(s.plan(LanguageCode.ENGLISH, LocalDate(2026, 10, 25)).first())
    }

    @Test fun conversationsAndTurns() = runBlocking<Unit> {
        val s = store()
        s.upsertConversation(Conversation("c1", LanguageCode.ENGLISH, "emma", null, 1L))
        s.addTurn(ConversationTurn("t1", "c1", Speaker.LEARNER, "I am work", createdAtMillis = 2L))
        s.addTurn(ConversationTurn("t2", "c1", Speaker.TUTOR, "Oh, where do you work?", corrections = listOf(Correction("I am work", "I work", "no am", ErrorSeverity.MODERATE)),
            alternatives = listOf("Where's your office?"), createdAtMillis = 3L, producedBy = "offline"))
        val turns = s.turns("c1").first()
        assertEquals(listOf("t1", "t2"), turns.map { it.id })
        assertEquals("I work", turns[1].corrections.single().corrected)
        assertEquals(1, s.learnerTurnCount(LanguageCode.ENGLISH))
    }

    @Test fun libraryVocabularyBookmarksNotesProviders() = runBlocking<Unit> {
        val s = store()
        val v = Curriculum.of(LanguageCode.FRENCH).vocabulary.first().copy(id = "user_1")
        s.addVocabulary(v)
        assertEquals(v, s.userVocabulary(LanguageCode.FRENCH).first().single())
        assertTrue(s.toggleBookmark(Bookmark("b1", BookmarkKind.LESSON, "fr_l_cafe", 1)))
        assertEquals(1, s.bookmarks().first().size)
        assertFalse(s.toggleBookmark(Bookmark("b2", BookmarkKind.LESSON, "fr_l_cafe", 2)))
        assertTrue(s.bookmarks().first().isEmpty())
        s.upsertNote(Note("n1", null, "Tip", "Use être for age? No — avoir!", 1, 1))
        assertEquals("Tip", s.notes().first().single().title)
        assertEquals(ProviderId.entries.size, s.current().size)
        s.save(ProviderConfig(ProviderId.GEMINI, enabled = true, model = "gemini-x"))
        assertEquals("gemini-x", s.current().getValue(ProviderId.GEMINI).model)
        s.setPreferred(ProviderId.GEMINI)
        assertEquals(ProviderId.GEMINI, s.preferredProvider.first())
    }

    @Test fun exportRespectsOptionsAndDeleteAllClearsEverything() = runBlocking<Unit> {
        val s = store()
        s.updateProfile { it.copy(displayName = "Omar") }
        s.upsertConversation(Conversation("c1", LanguageCode.ENGLISH, "emma", null, 1L))
        s.addTurn(ConversationTurn("t1", "c1", Speaker.LEARNER, "private sentence", createdAtMillis = 2L))
        s.upsertNote(Note("n1", null, "Secret note", "body", 1, 1))
        val minimal = s.exportJson(includeConversations = false, includeNotes = false)
        assertTrue("Omar" in minimal)
        assertFalse("private sentence" in minimal)
        assertFalse("Secret note" in minimal)
        val full = s.exportJson(includeConversations = true, includeNotes = true)
        assertTrue("private sentence" in full && "Secret note" in full)

        s.deleteAllData()
        assertEquals("", s.currentProfile().displayName)
        assertTrue(s.turns("c1").first().isEmpty())
        assertTrue(s.notes().first().isEmpty())
        assertFalse("Omar" in s.exportJson(true, true))
    }

    @Test fun migrationFromVersion1PreservesData() {
        // 1.db is the schema snapshot generated from version 1 of the .sq files.
        val snapshot = File("src/commonMain/sqldelight/databases/1.db")
        assertTrue(snapshot.exists(), "missing v1 schema snapshot")
        val tmp = Files.createTempFile("polyglot-v1", ".db").toFile()
        snapshot.copyTo(tmp, overwrite = true)
        val url = "jdbc:sqlite:${tmp.absolutePath}"
        JdbcSqliteDriver(url, Properties()).use { raw ->
            raw.execute(null, "INSERT INTO bookmark(id, kind, reference_id, created_at) VALUES ('b1','LESSON','en_l_cafe',1)", 0)
            raw.execute(null, "PRAGMA user_version = 1", 0)
        }
        // Opening with the current schema runs 1.sqm.
        val driver = JdbcSqliteDriver(url, Properties(), PolyglotDatabase.Schema)
        val db = PolyglotDatabase(driver)
        val row = db.libraryQueries.allBookmarks().executeAsList().single()
        assertEquals("en_l_cafe", row.reference_id)
        assertNull(row.label)
        assertEquals(2L, PolyglotDatabase.Schema.version)
        driver.close()
        tmp.delete()
    }
}

class ConversationIntegrityTest {
    @Test fun updatingAConversationKeepsItsTurns() = runBlocking<Unit> {
        val s = SqlStore(createJvmDriver(), Dispatchers.IO, TimeZone.UTC)
        s.upsertConversation(Conversation("c1", LanguageCode.ENGLISH, "emma", null, 1L))
        s.addTurn(ConversationTurn("t1", "c1", Speaker.LEARNER, "hello", createdAtMillis = 2L))
        s.upsertConversation(Conversation("c1", LanguageCode.ENGLISH, "emma", null, 1L, endedAtMillis = 9L, summary = "done"))
        assertEquals(1, s.turns("c1").first().size)
        assertEquals("done", s.conversations(LanguageCode.ENGLISH).first().single().summary)
    }
}
