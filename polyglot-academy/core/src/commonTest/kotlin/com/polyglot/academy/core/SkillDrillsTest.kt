package com.polyglot.academy.core

import com.polyglot.academy.core.content.Curriculum
import com.polyglot.academy.core.drills.GuidedDialogue
import com.polyglot.academy.core.drills.ListeningPace
import com.polyglot.academy.core.drills.SentenceCompare
import com.polyglot.academy.core.drills.SentenceDrills
import com.polyglot.academy.core.drills.SpeedDrills
import com.polyglot.academy.core.drills.SprintResult
import com.polyglot.academy.core.drills.TokenStatus
import com.polyglot.academy.core.model.Exercise
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.wordlist.WordLists
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SkillDrillsTest {
    private val trial = listOf(LanguageCode.ENGLISH, LanguageCode.MANDARIN, LanguageCode.MALAY)

    @Test fun everyLevelOfEveryLanguageHasEnoughSentencesToListenTo() {
        val problems = LanguageCode.entries.flatMap { lang ->
            val list = WordLists.of(lang)
            (1..list.levelCount).mapNotNull { level ->
                val n = SentenceDrills.pool(list, level).distinctBy { it.entry.id }.size
                if (n < 50) "$lang level $level: only $n words with a sentence of ${SentenceDrills.minLength(lang)}+" else null
            }
        }
        assertTrue(problems.isEmpty(), problems.joinToString("\n"))
    }

    @Test fun listeningQuestionsHaveOneRightMeaningAndHideTheText() {
        for (lang in trial) {
            val list = WordLists.of(lang)
            for (level in listOf(1, 5, 10)) {
                val set = SentenceDrills.listening(list, level, 10, seed = 7)
                assertEquals(10, set.size, "$lang $level")
                set.filterIsInstance<Exercise.MultipleChoice>().forEach { q ->
                    assertEquals(SentenceDrills.OPTIONS, q.options.size)
                    assertEquals(q.options[q.correctIndex], q.audioText, "the heard sentence is the right option")
                    val meanings = q.optionGlosses!!.map { it.ar }
                    assertEquals(meanings.size, meanings.toSet().size, "two choices with the same meaning: $meanings")
                }
                if (lang == LanguageCode.MANDARIN) assertTrue(set.none { it is Exercise.Dictation }, "no dictation for Mandarin")
                else assertEquals(5, set.count { it is Exercise.Dictation })
            }
        }
    }

    @Test fun upperLevelSentencesAreLongerThanBeginnerOnes() {
        for (lang in trial) {
            val list = WordLists.of(lang)
            fun avg(level: Int) = SentenceDrills.pool(list, level).map { SentenceDrills.length(it.sentence, lang) }.average()
            assertTrue(avg(9) > avg(1) + 3, "$lang: level 9 ${avg(9)}, level 1 ${avg(1)}")
        }
    }

    @Test fun comparisonKeepsWordOrderAndIgnoresCaseAndPunctuation() {
        val c = SentenceCompare.compare("i drink coffee every morning", "I drink tea every morning.", LanguageCode.ENGLISH)
        assertEquals(listOf("i", "drink", "every", "morning"), c.tokens.filter { it.status == TokenStatus.MATCH }.map { it.text })
        assertEquals(listOf("tea"), c.missing)
        assertEquals(listOf("coffee"), c.extra)
        assertFalse(c.exact)
        assertEquals(0.8, c.coverage, 1e-9)
        assertTrue(SentenceCompare.compare("Where is the station?", "where is the station", LanguageCode.ENGLISH).exact)
        // Swapped words: only one of them can be in order.
        assertEquals(1, SentenceCompare.compare("morning good", "good morning", LanguageCode.ENGLISH).matched)
    }

    @Test fun mandarinIsComparedByCharacter() {
        val c = SentenceCompare.compare("我喝茶。", "我喝咖啡。", LanguageCode.MANDARIN)
        assertEquals(2, c.matched)
        assertEquals(listOf("咖", "啡"), c.missing)
        assertEquals(listOf("茶"), c.extra)
    }

    @Test fun sprintSpeedCountsOnlyCorrectAnswers() {
        val r = SprintResult(correct = 18, wrong = 6, seconds = 60)
        assertEquals(18.0, r.perMinute, 1e-9)
        assertEquals(0.75, r.accuracy, 1e-9)
        assertEquals(3, SpeedDrills.levelOf(SpeedDrills.sprintReference(3)))
    }

    @Test fun listeningPaceRisesWithAStreakAndFallsAfterAMistake() {
        val pace = ListeningPace()
        pace.answer(true)
        assertEquals(1.0f, pace.rate)
        pace.answer(true)
        assertEquals(1.1f, pace.rate)
        pace.answer(false)
        assertEquals(1.0f, pace.rate)
        // Best is the fastest speed answered correctly; 1.1 was missed.
        assertEquals(1.0f, pace.best)
        repeat(20) { pace.answer(true) }
        assertEquals(SpeedDrills.MAX_RATE, pace.rate)
        assertEquals(SpeedDrills.MAX_RATE, pace.best)
        repeat(20) { pace.answer(false) }
        assertEquals(SpeedDrills.MIN_RATE, pace.rate)
    }

    @Test fun guidedDialogueFollowsTheScenarioAndAcceptsItsModelAnswers() {
        for (lang in trial) for (scenario in Curriculum.of(lang).scenarios) {
            val steps = GuidedDialogue.steps(scenario)
            assertEquals(scenario.beats.size, steps.size)
            assertEquals(scenario.tutorOpening, steps.first().partnerLine)
            steps.forEachIndexed { i, step ->
                if (i > 0) assertEquals(scenario.beats[i - 1].tutorLine, step.partnerLine)
                assertTrue(GuidedDialogue.check(step.modelAnswer, step, lang).passed, "${scenario.id} step $i: model answer not accepted")
            }
        }
    }

    @Test fun guidedDialogueNeedsAKeywordOrTheModel() {
        val scenario = Curriculum.of(LanguageCode.ENGLISH).scenarios.first()
        val step = GuidedDialogue.steps(scenario).first()
        assertFalse(GuidedDialogue.check("zzz qqq", step, LanguageCode.ENGLISH).passed)
        assertTrue(GuidedDialogue.check(step.keywords.first(), step, LanguageCode.ENGLISH).passed)
    }
}
