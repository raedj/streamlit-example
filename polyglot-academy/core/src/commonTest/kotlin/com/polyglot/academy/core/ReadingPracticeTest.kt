package com.polyglot.academy.core

import com.polyglot.academy.core.ai.ProviderId
import com.polyglot.academy.core.content.Curriculum
import com.polyglot.academy.core.learning.ReadAloud
import com.polyglot.academy.core.model.CefrLevel
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.service.ReadingAnswerService
import com.polyglot.academy.core.service.Understanding
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ReadingPracticeTest {
    private val en = LanguageCode.ENGLISH
    private val zh = LanguageCode.MANDARIN

    @Test fun englishAndMandarinHaveThreeReadingsAtEveryLevelFromA1ToB2() {
        listOf(en, zh).forEach { l ->
            val c = Curriculum.of(l)
            listOf(CefrLevel.A1, CefrLevel.A2, CefrLevel.B1, CefrLevel.B2).forEach { level ->
                assertTrue(c.readings.count { it.level == level } >= 3, "$l $level: ${c.readings.filter { it.level == level }.map { it.id }}")
            }
            assertTrue(c.validate().isEmpty(), c.validate().joinToString())
        }
    }

    @Test fun everyEnglishAndMandarinReadingHasAQuestionAndARetelling() {
        listOf(en, zh).forEach { l ->
            Curriculum.of(l).readings.forEach { r ->
                assertTrue(r.openQuestions.any { !it.summary } && r.openQuestions.any { it.summary }, r.id)
            }
        }
    }

    /** If the model answer misses a key point, the offline check would mark good answers as incomplete. */
    @Test fun everyModelAnswerHasAllItsKeyPoints() {
        listOf(en, zh).forEach { l ->
            Curriculum.of(l).readings.flatMap { it.openQuestions }.forEach { o ->
                val fb = ReadingAnswerService.offlineCheck(o.modelAnswer, o, l)
                assertEquals(emptyList(), fb.missingPoints, "${o.id}: ${o.modelAnswer}")
            }
        }
    }

    @Test fun offlineCheckFindsWordFormsAndMissingPoints() {
        val o = Curriculum.reading("en_st_cooking_class")!!.openQuestions.first { !it.summary }
        val good = ReadingAnswerService.offlineCheck("She forgot the salt.", o, en)
        assertTrue(good.looksComplete && good.missingPoints.isEmpty())
        val half = ReadingAnswerService.offlineCheck("There was no pepper.", o, en)
        assertEquals(listOf("salt"), half.missingPoints)
        val none = ReadingAnswerService.offlineCheck("I don't know.", Curriculum.reading("en_st_car_free")!!.openQuestions.first { !it.summary }, en)
        assertFalse(none.looksComplete)
        assertNull(none.understanding, "offline never claims to judge understanding")

        val zo = Curriculum.reading("zh_st_xiayu")!!.openQuestions.first { !it.summary }
        assertTrue(ReadingAnswerService.offlineCheck("她的家很近。", zo, zh).missingPoints.isEmpty())
        assertEquals(listOf("附近"), ReadingAnswerService.offlineCheck("她有两把伞。", zo, zh).missingPoints)
    }

    @Test fun aiFeedbackNeedsAClearJudgement() {
        val o = Curriculum.reading("en_st_cooking_class")!!.openQuestions.first()
        val base = ReadingAnswerService.offlineCheck("She forget salt.", o, en)
        val ok = ReadingAnswerService.parse("""Sure: {"understood":"YES","comment":"Good.","corrections":[{"original":"forget","corrected":"forgot","explanation":"past tense","severity":"minor","stylistic":false}],"improved_version":"She forgot the salt."}""", base, ProviderId.ANTHROPIC).getOrThrow()
        assertEquals(Understanding.YES, ok.understanding)
        assertEquals("forgot", ok.corrections.single().corrected)
        assertEquals("She forgot the salt.", ok.improvedVersion)
        assertEquals("ANTHROPIC", ok.source)
        assertTrue(ReadingAnswerService.parse("""{"understood":"maybe"}""", base, ProviderId.ANTHROPIC).isFailure)
        assertTrue(ReadingAnswerService.parse("no json", base, ProviderId.ANTHROPIC).isFailure)
    }

    @Test fun readAloudMarksSkippedWordsInOrder() {
        val r = ReadAloud.compare("Lina goes to the market with her brother.", "lina goes to market with her brother", en)
        assertEquals(listOf("the"), r.missed)
        assertEquals(7.0 / 8, r.score)
        // "the" twice: only the skipped one is marked.
        val twice = ReadAloud.compare("The cat sat on the mat.", "the cat sat on mat", en)
        assertEquals(listOf(false, false, false, false, true, false), twice.words.map { !it.heard })
        assertEquals(1.0, ReadAloud.compare("I can't come, sorry!", "I can't come sorry", en).score)
        assertEquals(1.0, ReadAloud.compare("Colours", "colors", en).score, "one-letter spelling difference in a long word")
    }

    @Test fun readAloudComparesChineseCharacterByCharacter() {
        val r = ReadAloud.compare("我家有四口人。", "我家有四个人", zh)
        assertEquals(listOf("口"), r.missed)
        assertEquals(listOf("我", "家", "有", "四", "口", "人"), r.words.map { it.text })
        assertEquals(0.0, ReadAloud.compare("我家有四口人。", "", zh).score)
    }
}
