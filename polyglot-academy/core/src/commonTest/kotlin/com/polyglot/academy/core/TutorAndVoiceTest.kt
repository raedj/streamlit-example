package com.polyglot.academy.core

import com.polyglot.academy.core.content.Curriculum
import com.polyglot.academy.core.model.CefrLevel
import com.polyglot.academy.core.model.CorrectionIntensity
import com.polyglot.academy.core.model.ErrorSeverity
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.speech.RecognitionEvent
import com.polyglot.academy.core.speech.SpeechErrorKind
import com.polyglot.academy.core.speech.VoiceEvent
import com.polyglot.academy.core.speech.VoiceState
import com.polyglot.academy.core.speech.VoiceStateMachine
import com.polyglot.academy.core.tutor.OfflineTutorEngine
import com.polyglot.academy.core.tutor.TutorContext
import com.polyglot.academy.core.tutor.TutorPromptBuilder
import com.polyglot.academy.core.tutor.TutorResponseParser
import com.polyglot.academy.core.tutor.TutorRoster
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class TutorAndVoiceTest {
    @Test fun rosterHasTenTutorsWithUniqueIdsAndEveryLanguageCovered() {
        assertEquals(10, TutorRoster.tutors.size)
        assertEquals(10, TutorRoster.tutors.map { it.id }.toSet().size)
        LanguageCode.entries.forEach { assertTrue(TutorRoster.forLanguage(it).isNotEmpty(), "no tutor for $it") }
        assertEquals("liwei", TutorRoster.defaultFor(LanguageCode.MANDARIN, CefrLevel.A1).id)
        assertEquals("morgan", TutorRoster.forLanguage(LanguageCode.ENGLISH).first { it.suits(CefrLevel.C2) && it.languages.isEmpty() }.id)
    }

    @Test fun personalityChangesThePrompt() {
        val base = TutorContext(TutorRoster.byId("emma")!!, LanguageCode.ENGLISH, LanguageCode.ARABIC, CefrLevel.A1, CorrectionIntensity.GENTLE, null, true)
        val emma = TutorPromptBuilder.systemPrompt(base)
        val oliver = TutorPromptBuilder.systemPrompt(base.copy(tutor = TutorRoster.byId("oliver")!!, intensity = CorrectionIntensity.THOROUGH))
        assertTrue("recast" in emma)
        assertTrue("explicit" in oliver)
        assertTrue("Only correct errors that change meaning" in emma)
        assertTrue("Correct every error" in oliver)
        assertTrue("Arabic" in emma)
        assertNotEquals(emma, oliver)
        val zh = TutorPromptBuilder.systemPrompt(base.copy(tutor = TutorRoster.byId("liwei")!!, targetLanguage = LanguageCode.MANDARIN))
        assertTrue("pinyin" in zh)
    }

    @Test fun parserReadsJsonInsideProse() {
        val raw = """Sure! {"reply":"Nice! What do you do?","reply_translation":"رائع! ماذا تعمل؟","corrections":[{"original":"I am work","corrected":"I work","explanation":"no am","severity":"moderate"},{"original":"same","corrected":"same","explanation":"","severity":"minor"}],"alternatives":["What's your job?"],"vocabulary":["job — عمل"]} hope that helps"""
        val r = TutorResponseParser.parse(raw, "OPENAI")
        assertEquals("Nice! What do you do?", r.reply())
        assertEquals(1, r.corrections.size)
        assertEquals(ErrorSeverity.MODERATE, r.corrections[0].severity)
        assertEquals(listOf("What's your job?"), r.alternatives)
    }

    private fun com.polyglot.academy.core.tutor.TutorReply.reply() = text

    @Test fun parserFallsBackToPlainText() {
        val r = TutorResponseParser.parse("Just text, no json", "X")
        assertEquals("Just text, no json", r.text)
        assertTrue(r.corrections.isEmpty())
        val braces = TutorResponseParser.parse("""{"reply": "He said \"{hi}\""}""", "X")
        assertEquals("He said \"{hi}\"", braces.text)
    }

    @Test fun offlineTutorWalksScenarioWithHintsAndModelAnswers() {
        val scenario = Curriculum.of(LanguageCode.ENGLISH).scenarios.first { it.id == "en_s_cafe" }
        val e = OfflineTutorEngine(scenario, LanguageCode.ARABIC)
        assertTrue(e.opening().scripted)
        val hint = e.respond("hmm")
        assertEquals(0, e.beatIndex)
        assertTrue(hint.alternatives.isNotEmpty())
        val ok = e.respond("I would like a coffee please")
        assertEquals(1, e.beatIndex)
        assertEquals("Sure. Would you like anything to eat?", ok.text)
        e.respond("???"); e.respond("???")
        val forced = e.respond("???")
        assertEquals(2, e.beatIndex)
        assertTrue(forced.text.startsWith("Yes, a sandwich, please."))
        e.respond("for here")
        val last = e.respond("Could I have the bill?")
        assertTrue(last.finished)
        assertTrue(e.isFinished)
    }

    @Test fun offlineTutorMatchesChineseWithoutSpaces() {
        val scenario = Curriculum.of(LanguageCode.MANDARIN).scenarios.first { it.id == "zh_s_tea" }
        val e = OfflineTutorEngine(scenario, LanguageCode.ENGLISH)
        e.respond("我要一杯茶")
        assertEquals(1, e.beatIndex)
    }

    @Test fun everyScenarioCanBeCompletedWithModelAnswers() {
        Curriculum.all.values.flatMap { it.scenarios }.forEach { s ->
            val e = OfflineTutorEngine(s, LanguageCode.ENGLISH)
            s.beats.forEach { b -> e.respond(b.modelAnswer) }
            assertTrue(e.isFinished, "scenario ${s.id}: model answers must satisfy their own beats (stopped at ${e.beatIndex})")
        }
    }

    @Test fun voiceStateMachineHappyPath() {
        var s: VoiceState = VoiceState.Idle
        s = VoiceStateMachine.reduce(s, VoiceEvent.StartListening)
        assertIs<VoiceState.Listening>(s)
        s = VoiceStateMachine.reduce(s, VoiceEvent.Recognition(RecognitionEvent.Partial("hel")))
        assertEquals("hel", (s as VoiceState.Listening).partial)
        s = VoiceStateMachine.reduce(s, VoiceEvent.Recognition(RecognitionEvent.Final("hello", 0.9f, emptyList())))
        assertEquals(VoiceState.Processing, s)
        s = VoiceStateMachine.reduce(s, VoiceEvent.SpeakStarted("t1"))
        assertIs<VoiceState.Speaking>(s)
        s = VoiceStateMachine.reduce(s, VoiceEvent.SpeakFinished)
        assertEquals(VoiceState.Idle, s)
    }

    @Test fun voiceStateMachineErrorsAlwaysHaveExit() {
        var s: VoiceState = VoiceStateMachine.reduce(VoiceState.Listening("", 0f), VoiceEvent.Recognition(RecognitionEvent.Final(" ", null, emptyList())))
        assertEquals(VoiceState.Failed(SpeechErrorKind.NO_SPEECH_DETECTED), s)
        assertEquals(VoiceState.Idle, VoiceStateMachine.reduce(s, VoiceEvent.DismissError))
        assertIs<VoiceState.Listening>(VoiceStateMachine.reduce(s, VoiceEvent.StartListening))
        assertEquals(VoiceState.Failed(SpeechErrorKind.NETWORK), VoiceStateMachine.reduce(VoiceState.Processing, VoiceEvent.ReplyFailed))
        // Processing must never get stuck: a cancel always returns to idle.
        assertEquals(VoiceState.Idle, VoiceStateMachine.reduce(VoiceState.Processing, VoiceEvent.Cancel))
        assertEquals(VoiceState.Completed, VoiceStateMachine.reduce(VoiceState.Speaking("x"), VoiceEvent.Finish))
        assertEquals(VoiceState.Paused, VoiceStateMachine.reduce(VoiceState.Speaking("x"), VoiceEvent.Pause))
        // Listening ignores reply events.
        val l = VoiceState.Listening("a", 0.2f)
        assertEquals(l, VoiceStateMachine.reduce(l, VoiceEvent.ReplyReady))
    }

    @Test fun everyStateHandlesEveryEventWithoutThrowing() {
        val states = listOf(VoiceState.Idle, VoiceState.Listening("", 0f), VoiceState.Processing, VoiceState.Speaking("t"), VoiceState.Paused,
            VoiceState.Failed(SpeechErrorKind.UNKNOWN), VoiceState.Completed)
        val events = listOf(VoiceEvent.StartListening, VoiceEvent.Recognition(RecognitionEvent.Ready), VoiceEvent.SubmitTyped, VoiceEvent.ReplyReady,
            VoiceEvent.SpeakStarted("t"), VoiceEvent.SpeakFinished, VoiceEvent.SpeakFailed(SpeechErrorKind.TTS_FAILED), VoiceEvent.ReplyFailed,
            VoiceEvent.Pause, VoiceEvent.Resume, VoiceEvent.Cancel, VoiceEvent.Finish, VoiceEvent.DismissError)
        for (s in states) for (e in events) VoiceStateMachine.reduce(s, e)
    }
}
