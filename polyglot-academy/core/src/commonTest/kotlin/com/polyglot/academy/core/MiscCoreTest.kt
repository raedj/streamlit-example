package com.polyglot.academy.core

import com.polyglot.academy.core.content.Curriculum
import com.polyglot.academy.core.device.DeviceCapability
import com.polyglot.academy.core.device.DeviceInfo
import com.polyglot.academy.core.device.DeviceTier
import com.polyglot.academy.core.localai.Compatibility
import com.polyglot.academy.core.localai.IncompatibilityReason
import com.polyglot.academy.core.localai.LocalModelCatalog
import com.polyglot.academy.core.mandarin.Pinyin
import com.polyglot.academy.core.media.CaptionParser
import com.polyglot.academy.core.media.ChunkedTextProcessor
import com.polyglot.academy.core.media.ImportPolicy
import com.polyglot.academy.core.media.SentenceSplitter
import com.polyglot.academy.core.media.VocabularyExtractor
import com.polyglot.academy.core.model.Exercise
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.Languages
import com.polyglot.academy.core.model.MaterialType
import com.polyglot.academy.core.relax.BreathPhaseKind
import com.polyglot.academy.core.relax.Relaxation
import com.polyglot.academy.core.util.TextNormalizer
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class MiscCoreTest {
    @Test fun pinyinToneMarksPlacedCorrectly() {
        assertEquals("nǐ hǎo", Pinyin.numberedToMarked("ni3 hao3"))
        assertEquals("xiè", Pinyin.applyTone("xie", 4))
        assertEquals("gǒu", Pinyin.applyTone("gou", 3))
        assertEquals("guì", Pinyin.applyTone("gui", 4))
        assertEquals("lǜ", Pinyin.applyTone("lv", 4))
        assertEquals("Zhōng", Pinyin.applyTone("Zhong", 1))
    }

    @Test fun pinyinParsingAndSyllables() {
        assertEquals(3, Pinyin.parseSyllable("hǎo").tone)
        assertEquals(5, Pinyin.parseSyllable("ma").tone)
        assertEquals(listOf(4, 5), Pinyin.tones("xièxie"))
        assertEquals(listOf("zhong", "guo"), Pinyin.syllables("Zhōngguó").map { it.base.lowercase() })
        assertEquals(listOf("huo", "che", "zhan"), Pinyin.syllables("huǒchēzhàn").map { it.base })
        assertEquals(listOf("tian", "an", "men").size, Pinyin.syllables("tiān'ānmén").size)
        assertEquals(listOf(2, 3), Pinyin.spokenTones("nǐ hǎo"))
    }

    @Test fun everyMandarinVocabularyItemHasParsablePinyinMatchingCharacterCount() {
        val c = Curriculum.of(LanguageCode.MANDARIN)
        for (v in c.vocabulary) {
            val p = v.pronunciation ?: error("missing pinyin for ${v.id}")
            val hanzi = v.term.count { it.code in 0x4E00..0x9FFF }
            assertEquals(hanzi, Pinyin.syllables(p).size, "syllables of '$p' vs characters of ${v.term}")
            v.traditional?.let { assertEquals(v.term.length, it.length, "traditional form length for ${v.id}") }
        }
    }

    @Test fun captionsParse() {
        val srt = "1\n00:00:01,000 --> 00:00:02,500\nHello <i>there</i>\n\n2\n00:00:03,000 --> 00:00:04,000\nSecond line\ncontinues\n"
        val cues = CaptionParser.parse(srt)
        assertEquals(2, cues.size)
        assertEquals(1000, cues[0].startMillis)
        assertEquals("Hello there", cues[0].text)
        assertEquals("Second line continues", cues[1].text)
        val vtt = "WEBVTT\n\n00:01.000 --> 00:02.000\nBonjour\n"
        assertEquals("Bonjour", CaptionParser.parse(vtt).single().text)
    }

    @Test fun sentenceSplitterHandlesMixedScripts() {
        assertEquals(3, SentenceSplitter.split("Hello there. 你好。 ¿Qué tal?").size)
        assertEquals(2, SentenceSplitter.split("مرحبًا؟ كيف حالك").size)
    }

    @Test fun vocabularyExtractorFindsKnownAndNewWords() {
        val dict = Curriculum.of(LanguageCode.ENGLISH).vocabulary.associate { TextNormalizer.basic(it.term) to it.id }
        val ex = VocabularyExtractor(LanguageCode.ENGLISH, dict)
        val c = ex.candidates("The ticket was expensive. The station was busy and the ticket office closed.")
        assertEquals("ticket", c.first().term)
        assertEquals("en_ticket", c.first().knownId)
        assertTrue(c.none { it.term == "the" })
        assertTrue(c.any { it.term == "office" && it.knownId == null })
    }

    @Test fun chineseSegmentationUsesDictionary() {
        val dict = Curriculum.of(LanguageCode.MANDARIN).vocabulary.associate { it.term to it.id }
        val ex = VocabularyExtractor(LanguageCode.MANDARIN, dict)
        assertEquals(listOf("我", "去", "火车站"), ex.segmentChinese("我去火车站。"))
        assertTrue(ex.candidates("我去火车站。明天去火车站。").first().term == "火车站")
    }

    @Test fun chunkedProcessingReportsProgressAndJoins() = runTest {
        val text = (1..2000).joinToString(" ") { "word$it." }
        val chunks = ChunkedTextProcessor.chunks(text, 1000)
        assertTrue(chunks.size > 5)
        assertEquals(text, chunks.joinToString(""))
        val events = ChunkedTextProcessor.process(chunks).toList()
        assertEquals(chunks.size + 1, events.size)
        assertTrue(events.last().done)
        assertEquals(1f, events.last().fraction)
    }

    @Test fun importValidation() {
        assertEquals(MaterialType.CAPTIONS, ImportPolicy.typeFor("application/octet-stream", "movie.SRT"))
        assertEquals(MaterialType.PDF, ImportPolicy.typeFor("application/pdf", "a"))
        assertEquals(null, ImportPolicy.typeFor("application/zip", "a.zip"))
        assertIs<ImportPolicy.Validation.UnsupportedType>(ImportPolicy.validate(null, 10))
        assertIs<ImportPolicy.Validation.TooLarge>(ImportPolicy.validate(MaterialType.TEXT, 50L * 1024 * 1024))
        assertIs<ImportPolicy.Validation.Empty>(ImportPolicy.validate(MaterialType.TEXT, 0))
        assertEquals(ImportPolicy.Validation.Ok, ImportPolicy.validate(MaterialType.TEXT, 100))
    }

    @Test fun breathingFrames() {
        val box = Relaxation.patterns.first { it.id == "box" }
        assertEquals(16, box.cycleSeconds)
        assertEquals(BreathPhaseKind.INHALE, Relaxation.frameAt(box, 1.0, 3).phase.kind)
        assertEquals(BreathPhaseKind.HOLD, Relaxation.frameAt(box, 5.0, 3).phase.kind)
        assertEquals(1, Relaxation.frameAt(box, 17.0, 3).cycle)
        assertTrue(Relaxation.frameAt(box, 48.0, 3).finished)
        assertTrue("does not teach" in Relaxation.disclaimer.en)
    }

    private fun device(ram: Long = 8000, storage: Long = 20000, api: Int = 34, is64: Boolean = true) =
        DeviceInfo(ram, storage, api, 8, is64, false, 80, false, false, true, "test")

    @Test fun deviceTiersAndLocalModelHonesty() {
        assertEquals(DeviceTier.HIGH, DeviceCapability.tier(device()))
        assertEquals(DeviceTier.LOW, DeviceCapability.tier(device(ram = 2000)))
        val c = LocalModelCatalog.compatibility(LocalModelCatalog.models.first(), device())
        assertIs<Compatibility.Incompatible>(c)
        assertEquals(listOf(IncompatibilityReason.RUNTIME_NOT_BUNDLED), c.reasons)
        val weak = LocalModelCatalog.compatibility(LocalModelCatalog.models[1], device(ram = 2000, storage = 100, is64 = false)) as Compatibility.Incompatible
        assertTrue(IncompatibilityReason.INSUFFICIENT_RAM in weak.reasons && IncompatibilityReason.NOT_64_BIT in weak.reasons && IncompatibilityReason.INSUFFICIENT_STORAGE in weak.reasons)
    }

    @Test fun languagesConfigured() {
        assertEquals(8, Languages.all.size)
        assertTrue(Languages.ARABIC.direction == com.polyglot.academy.core.model.TextDirection.RTL)
        assertTrue(Languages.URDU.direction == com.polyglot.academy.core.model.TextDirection.RTL)
        assertTrue(Languages.TURKISH.direction == com.polyglot.academy.core.model.TextDirection.LTR && Languages.MALAY.direction == com.polyglot.academy.core.model.TextDirection.LTR)
        assertEquals(listOf(LanguageCode.TURKISH, LanguageCode.URDU, LanguageCode.MALAY), listOf("tr", "ur", "ms").map { LanguageCode.fromTag(it) })
        // No pronunciation-assessment locale has been confirmed for these yet, so scoring reports it instead of guessing.
        listOf(LanguageCode.TURKISH, LanguageCode.URDU, LanguageCode.MALAY).forEach {
            assertEquals(null, com.polyglot.academy.core.pronunciation.AzurePronunciationAssessor.locale(it, null))
        }
        assertEquals(listOf(LanguageCode.ARABIC, LanguageCode.ENGLISH), Languages.interfaceLanguages.map { it.code })
        assertEquals(LanguageCode.MANDARIN, LanguageCode.fromTag("ZH"))
    }
}

class PinyinPunctuationTest {
    @kotlin.test.Test fun punctuationIsNotASyllable() {
        kotlin.test.assertEquals(listOf("xi", "shou", "jian", "zai", "na", "li"),
            com.polyglot.academy.core.mandarin.Pinyin.syllables("xǐshǒujiān zài nǎlǐ?").map { it.base })
        kotlin.test.assertEquals(4, com.polyglot.academy.core.mandarin.Pinyin.syllables("wǒ duì … guòmǐn").size)
        kotlin.test.assertEquals(2, com.polyglot.academy.core.mandarin.Pinyin.syllables("Xi'an").size)
    }
}
