package com.polyglot.academy.core

import com.polyglot.academy.core.content.Curriculum
import com.polyglot.academy.core.model.CefrLevel
import com.polyglot.academy.core.model.CoachTrack
import com.polyglot.academy.core.model.ConversationTurn
import com.polyglot.academy.core.model.Correction
import com.polyglot.academy.core.model.CorrectionIntensity
import com.polyglot.academy.core.model.ErrorSeverity
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.Register
import com.polyglot.academy.core.model.ReviewState
import com.polyglot.academy.core.model.Skill
import com.polyglot.academy.core.model.Speaker
import com.polyglot.academy.core.tutor.CoachProtocol
import com.polyglot.academy.core.tutor.CoachStats
import com.polyglot.academy.core.tutor.CoachTitle
import com.polyglot.academy.core.tutor.CoachTitles
import com.polyglot.academy.core.tutor.TutorContext
import com.polyglot.academy.core.tutor.TutorPromptBuilder
import com.polyglot.academy.core.tutor.TutorResponseParser
import com.polyglot.academy.core.tutor.TutorRoster
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CoachTest {
    private fun ctx(lang: LanguageCode = LanguageCode.ENGLISH) = TutorContext(
        TutorRoster.tutors.first(), lang, LanguageCode.ARABIC, CefrLevel.A2, CorrectionIntensity.BALANCED, null, true)

    private fun turn(text: String, errors: Int = 0) = ConversationTurn("t", "c", Speaker.LEARNER, text,
        corrections = List(errors) { Correction("a$it", "b$it", "", ErrorSeverity.MODERATE) }, createdAtMillis = 0)

    @Test fun promptCarriesTheWholeProtocol() {
        val p = TutorPromptBuilder.systemPrompt(ctx())
        assertTrue("at most one new language point or three new words" in p)
        assertTrue("role-play" in p)
        assertTrue("the register of the corrected form" in p)
        assertTrue("\"hint\"" in p && "Socratic" in p)
        assertTrue("end your reply with exactly one specific question or one short practice task" in p)
        assertTrue("encouraging" in p)
        CoachProtocol.errorCategories.forEach { assertTrue(it in p, it) }
        assertFalse("\"track\"" in p, "no track question unless asked for")
        assertFalse(CoachProtocol.STRUGGLING in p)
    }

    @Test fun firstSessionAsksForTheTrackAndTheExamMatchesTheLanguage() {
        val p = TutorPromptBuilder.systemPrompt(ctx(LanguageCode.FRENCH).copy(askTrack = true))
        assertTrue("Welcome them warmly and ask which track" in p)
        assertTrue("DELF or DALF" in p && "\"track\"" in p)
        assertTrue("IELTS or TOEFL" in CoachProtocol.track(CoachTrack.EXAM, LanguageCode.ENGLISH))
        assertTrue("HSK" in CoachProtocol.examName(LanguageCode.MANDARIN))
        assertTrue("estimate" in CoachProtocol.track(CoachTrack.EXAM, LanguageCode.SPANISH), "exam scores are never presented as official")
        val withTrack = TutorPromptBuilder.systemPrompt(ctx().copy(coachTrack = CoachTrack.BUSINESS))
        assertTrue("corporate register" in withTrack)
        assertFalse("ask which track" in withTrack)
    }

    @Test fun warmUpErrorLogAndFrustrationGoIntoThePrompt() {
        val p = TutorPromptBuilder.systemPrompt(ctx().copy(warmUp = listOf("bill — الفاتورة"), recurringDifficulties = listOf("tense (3×): I go → I went"), struggling = true))
        assertTrue("one or two short quiz questions" in p && "bill — الفاتورة" in p)
        assertTrue("Error log (recurring mistakes): tense (3×)" in p)
        assertTrue(CoachProtocol.STRUGGLING in p)
    }

    @Test fun parserReadsTheFourPartCorrectionAndTrack() {
        val r = TutorResponseParser.parse("""{"reply":"Nice! Which hotel?","track":"Travel","corrections":[
            {"original":"I goed","corrected":"I went","explanation":"فعل شاذ","severity":"minor","hint":"What is the past of go?","register":"neutral","category":"Tense"},
            {"original":"Hey boss","corrected":"Dear Mr Smith","explanation":"","severity":"moderate","register":"formal","category":"nonsense"}]}""", "openai")
        assertEquals(CoachTrack.TRAVEL, r.track)
        val (a, b) = r.corrections
        assertEquals("What is the past of go?", a.hint)
        assertEquals(Register.NEUTRAL, a.register)
        assertEquals("tense", a.category)
        assertNull(b.hint)
        assertEquals(Register.FORMAL, b.register)
        assertNull(CoachProtocol.parseTrack(""))
    }

    @Test fun frustrationAndRepeatedErrorsAreDetected() {
        assertFalse(CoachProtocol.isStruggling(emptyList()))
        assertFalse(CoachProtocol.isStruggling(listOf(turn("I like football"))))
        assertTrue(CoachProtocol.isStruggling(listOf(turn("This is too hard"))))
        assertTrue(CoachProtocol.isStruggling(listOf(turn("هذا صعب جدا"))))
        assertTrue(CoachProtocol.isStruggling(List(3) { turn("x", errors = 2) }))
        assertFalse(CoachProtocol.isStruggling(listOf(turn("x", 2), turn("x", 2), turn("x", 1))))
    }

    @Test fun errorLogGroupsByTypeAndSkipsStyle() {
        val m = CoachProtocol.mistakeFor(Correction("I goed", "I went", "past", ErrorSeverity.MINOR, category = "tense"), LanguageCode.ENGLISH, "m1", 5)!!
        assertEquals("conversation:tense", m.sourceId)
        assertEquals(Skill.SPEAKING, m.skill)
        assertEquals("conversation:other", CoachProtocol.mistakeFor(Correction("a", "b", "", ErrorSeverity.MINOR, category = "made_up"), LanguageCode.ENGLISH, "m2", 5)!!.sourceId)
        assertNull(CoachProtocol.mistakeFor(Correction("a", "b", "", ErrorSeverity.MINOR, isStylistic = true), LanguageCode.ENGLISH, "m3", 5))
        val log = listOf(m.copy(occurrences = 4), m.copy(id = "x", category = "article", occurrences = 1), m.copy(id = "y", category = "spelling", occurrences = 3, resolved = true))
        assertEquals(listOf("tense (4×): I goed → I went"), CoachProtocol.recurringErrors(log))
    }

    @Test fun warmUpPrefersDueThenWeakWords() {
        val vocab = Curriculum.of(LanguageCode.SPANISH).vocabulary.take(4)
        val now = 1_000_000L
        val states = listOf(
            ReviewState(vocab[0].id, LanguageCode.SPANISH, ease = 2.8, lastReviewedAtMillis = 1, dueAtMillis = now + 10),
            ReviewState(vocab[1].id, LanguageCode.SPANISH, ease = 2.5, lastReviewedAtMillis = 1, dueAtMillis = now - 10),
            ReviewState(vocab[2].id, LanguageCode.SPANISH, ease = 1.4, lastReviewedAtMillis = 1, dueAtMillis = now + 10),
            ReviewState(vocab[3].id, LanguageCode.SPANISH), // never reviewed: not quizzed yet
        )
        val items = CoachProtocol.warmUpItems(LanguageCode.SPANISH, states, emptyList(), now, limit = 2)
        assertEquals(listOf(vocab[1], vocab[2]).map { "${it.term} — ${it.meaning.en}" }, items)
    }

    @Test fun titlesAreEarnedInOrderFromRealProgress() {
        val start = CoachStats(0, 0, null, 0, null)
        assertEquals(CoachTitle.FIRST_STEPS, CoachTitles.current(start))
        assertEquals(CoachTitle.WORD_EXPLORER, CoachTitles.next(start))
        assertEquals(CoachTitle.WORD_EXPLORER, CoachTitles.current(start.copy(wordsInReview = 25)))
        val pro = CoachStats(30, 10, 0.2, 0, 8)
        assertEquals(CoachTitle.PHRASING_PRO, CoachTitles.current(pro))
        assertEquals(CoachTitle.CONVERSATION_STARTER, CoachTitles.current(pro.copy(recentCorrectionsPerTurn = 0.5)))
        // Missions alone do not skip earlier titles.
        assertEquals(CoachTitle.FIRST_STEPS, CoachTitles.current(start.copy(missionsPassed = 25, medianWordsPerTurn = 15)))
        val top = CoachStats(30, 12, 0.1, 21, 13)
        assertEquals(CoachTitle.FLUENT_VOICE, CoachTitles.current(top))
        assertNull(CoachTitles.next(top))
    }
}
