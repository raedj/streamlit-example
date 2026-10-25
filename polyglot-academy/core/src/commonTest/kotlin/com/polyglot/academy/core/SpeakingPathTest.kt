package com.polyglot.academy.core

import com.polyglot.academy.core.content.Curriculum
import com.polyglot.academy.core.model.CefrLevel
import com.polyglot.academy.core.model.Conversation
import com.polyglot.academy.core.model.ConversationTurn
import com.polyglot.academy.core.model.CorrectionIntensity
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.Speaker
import com.polyglot.academy.core.speaking.FluencyCalculator
import com.polyglot.academy.core.speaking.MissionEvaluator
import com.polyglot.academy.core.speaking.MissionGap
import com.polyglot.academy.core.speaking.SpeakingPath
import com.polyglot.academy.core.speaking.SpeakingProgressCalculator
import com.polyglot.academy.core.speaking.SpeechUnits
import com.polyglot.academy.core.tutor.TutorContext
import com.polyglot.academy.core.tutor.TutorPromptBuilder
import com.polyglot.academy.core.tutor.TutorResponseParser
import com.polyglot.academy.core.tutor.TutorRoster
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SpeakingPathTest {
    @Test fun pathCoversA1ToC1WithRisingDemands() {
        val ms = SpeakingPath.missions
        assertEquals(ms.size, ms.map { it.id }.toSet().size, "duplicate mission ids")
        SpeakingPath.levels.forEach { l -> assertEquals(6, ms.count { it.level == l }, "missions at $l") }
        val demands = SpeakingPath.levels.map { l -> ms.first { it.level == l }.let { it.minTurns to it.minWordsPerTurn } }
        assertEquals(demands.sortedWith(compareBy({ it.first }, { it.second })), demands)
        assertTrue(demands.zipWithNext().all { (a, b) -> b.second > a.second })
        val allScenarioIds = Curriculum.all.values.flatMap { c -> c.scenarios.map { it.id } }.toSet()
        ms.forEach { m ->
            assertEquals(3, m.tasks.size, m.id)
            assertTrue(m.tasks.all { it.en.isNotBlank() && it.ar.isNotBlank() } && m.canDo.ar.isNotBlank(), m.id)
            m.offlineScenarioIds.forEach { assertTrue(it in allScenarioIds, "${m.id} links unknown scenario $it") }
        }
        assertEquals(SpeakingPath.of("b1_story"), SpeakingPath.fromConversationKey("mission:b1_story"))
        assertNull(SpeakingPath.fromConversationKey("en_s_cafe"))
    }

    @Test fun missionPromptAsksAiToCoachAndSignalCompletion() {
        val m = SpeakingPath.of("b2_debate")!!
        val prompt = TutorPromptBuilder.systemPrompt(TutorContext(TutorRoster.tutors.first(), LanguageCode.SPANISH, LanguageCode.ARABIC, CefrLevel.B2,
            CorrectionIntensity.BALANCED, null, true, mission = m))
        assertTrue("Give two advantages" in prompt && TutorPromptBuilder.MISSION_START in prompt && "\"mission_complete\"" in prompt, prompt)
        assertTrue("counter-arguments" in prompt, "level coaching missing")
        val plain = TutorPromptBuilder.systemPrompt(TutorContext(TutorRoster.tutors.first(), LanguageCode.SPANISH, LanguageCode.ARABIC, CefrLevel.A1,
            CorrectionIntensity.BALANCED, null, true))
        assertFalse("mission_complete" in plain)
        assertTrue(TutorResponseParser.parse("""{"reply":"¡Muy bien!","mission_complete":true}""", "OPENAI").missionComplete)
        assertFalse(TutorResponseParser.parse("""{"reply":"Sigue."}""", "OPENAI").missionComplete)
    }

    @Test fun wordCountsPerLanguage() {
        assertEquals(5, SpeechUnits.words("I'd like a big coffee.", LanguageCode.ENGLISH))
        assertEquals(4, SpeechUnits.words("Quisiera un café, gracias", LanguageCode.SPANISH))
        assertEquals(4, SpeechUnits.words("أريد قهوة من فضلك", LanguageCode.ARABIC))
        // 6 characters ≈ 4 words (estimate).
        assertEquals(4, SpeechUnits.words("我要一杯咖啡。", LanguageCode.MANDARIN))
        assertEquals(0, SpeechUnits.words("...", LanguageCode.FRENCH))
    }

    private fun turn(i: Int, text: String, ms: Long? = null, day: Int = 20) = ConversationTurn("t$i", "c", Speaker.LEARNER, text,
        createdAtMillis = LocalDate(2026, 10, day).toEpochDays() * 86_400_000L, wasSpoken = ms != null, speakingMillis = ms)

    @Test fun missionNeedsTurnsLengthAndConfirmation() {
        val m = SpeakingPath.of("a1_intro")!! // 4 turns, median 3 words
        val good = (1..4).map { turn(it, "My name is Sara and I am from Cairo") }
        assertTrue(MissionEvaluator.evaluate(m, LanguageCode.ENGLISH, good, tasksConfirmed = true).passed)
        val r = MissionEvaluator.evaluate(m, LanguageCode.ENGLISH, listOf(turn(1, "Hi"), turn(2, "Sara")), tasksConfirmed = false)
        assertEquals(listOf(MissionGap.TOO_FEW_TURNS, MissionGap.ANSWERS_TOO_SHORT, MissionGap.TASKS_NOT_CONFIRMED), r.gaps)
    }

    @Test fun fluencyReportUsesOnlyObservedData() {
        assertNull(FluencyCalculator.report(LanguageCode.ENGLISH, emptyList(), LocalDate(2026, 10, 25)).medianWordsPerTurn)
        // 12 words in 6 s = 120 wpm, three timed spoken turns; one typed turn.
        val turns = (1..3).map { turn(it, "I usually take the bus to work because it is cheap", ms = 6_000) } + turn(4, "Yes")
        val r = FluencyCalculator.report(LanguageCode.ENGLISH, turns, LocalDate(2026, 10, 25))
        assertEquals(110, r.wordsPerMinute)
        assertEquals(0.75, r.spokenShare)
        assertEquals(11, r.longestTurnWords)
        assertNull(r.lexicalVariety, "needs at least 50 words")
        assertEquals(8, r.weekly.size)
        assertEquals(4, r.weekly.last { it.learnerTurns > 0 }.learnerTurns)
    }

    @Test fun progressComesFromStoredMissionConversations() {
        val conv = { id: String, key: String, passed: Boolean ->
            Conversation(id, LanguageCode.FRENCH, "x", key, 1L, 2L, summary = if (passed) "learner_turns=5 ${MissionEvaluator.PASSED_MARK}" else "learner_turns=1")
        }
        val levels = SpeakingProgressCalculator.levels(listOf(conv("1", "mission:a1_intro", true), conv("2", "mission:a1_cafe", false), conv("3", "fr_s_reunion", true)))
        val a1 = levels.first()
        assertEquals(1, a1.passedCount)
        assertEquals(1, a1.missions.first { it.mission.id == "a1_cafe" }.attempts)
        assertEquals("a1_cafe", SpeakingProgressCalculator.next(levels)?.id)
    }
}
