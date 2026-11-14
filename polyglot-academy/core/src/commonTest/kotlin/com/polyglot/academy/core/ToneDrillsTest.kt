package com.polyglot.academy.core

import com.polyglot.academy.core.mandarin.ListeningPair
import com.polyglot.academy.core.mandarin.ToneDrillKind
import com.polyglot.academy.core.mandarin.ToneDrills
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.wordlist.WordLists
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ToneDrillsTest {
    private val list = WordLists.of(LanguageCode.MANDARIN)

    @Test fun buAndYiInTheWordListFollowTheToneRules() {
        // The word list writes 不 and 一 as said in the word; each must match the rule for the next tone.
        assertEquals(emptyList(), ToneDrills.disagreements(list))
    }

    @Test fun sandhiQuestionsAskTheChangedSyllable() {
        val qs = ToneDrills.sandhi(list)
        val bukeqi = qs.first { it.word == "不客气" && it.kind == ToneDrillKind.BU }
        assertEquals("bú", bukeqi.options[bukeqi.answer])
        val yiyang = qs.first { it.word == "一样" }
        assertEquals("yí", yiyang.options[yiyang.answer])
        val yiqi = qs.first { it.word == "一起" }
        assertEquals("yì", yiqi.options[yiqi.answer])
        val keyi = qs.first { it.word == "可以" && it.kind == ToneDrillKind.THIRD_TONES }
        assertEquals("ké", keyi.options[keyi.answer])
        assertEquals("kéyǐ", keyi.spoken.joinToString("") { it.marked })
        assertTrue(ToneDrillKind.entries.filter { it != ToneDrillKind.NEUTRAL }.all { k -> qs.count { it.kind == k } >= 5 }, qs.groupingBy { it.kind }.eachCount().toString())
        assertTrue(qs.all { it.options.toSet().size == it.options.size && it.answer in it.options.indices })
    }

    @Test fun neutralQuestionsPointAtAnUnmarkedSyllable() {
        val qs = ToneDrills.neutral(list)
        assertTrue(qs.size >= 20, "${qs.size}")
        val xiexie = qs.first { it.word == "谢谢" }
        assertEquals(1, xiexie.index)
        assertEquals("xie", xiexie.options[xiexie.answer])
    }

    @Test fun listeningPairsDifferOnlyInToneOrAConfusableSound() {
        val pairs = ToneDrills.listeningPairs(list)
        assertTrue(pairs.count { it.contrast == ListeningPair.Contrast.TONE } >= 10, "${pairs.size}")
        assertTrue(pairs.all { it.heard.term != it.other.term && it.heard.pronunciation != it.other.pronunciation })
    }
}
