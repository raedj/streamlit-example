package com.polyglot.academy.core.mandarin

import com.polyglot.academy.core.model.Gloss
import com.polyglot.academy.core.wordlist.WordEntry
import com.polyglot.academy.core.wordlist.WordList

/** What a syllable drill practises. */
enum class ToneDrillKind {
    /** 不 is bú before a fourth tone, bù elsewhere. */
    BU,
    /** 一 is yí before a fourth tone, yì before the other tones, yī on its own or at the end. */
    YI,
    /** Of two third tones in a row, the first is said as a second tone. */
    THIRD_TONES,
    /** A syllable said lightly, without its own tone. */
    NEUTRAL,
}

/** "How is this syllable said in this word?": the whole word is shown and heard, one syllable is asked. */
data class SyllableQuestion(
    val kind: ToneDrillKind,
    val entryId: String,
    val word: String,
    /** Index of the asked character and syllable in [word]. */
    val index: Int,
    /** The word as it is said, syllable by syllable. */
    val spoken: List<PinyinSyllable>,
    val options: List<String>,
    val answer: Int,
    val meaning: Gloss,
)

/** "Which word did you hear?": two words whose pinyin differs only in tone, or in a sound learners confuse. */
data class ListeningPair(val heard: WordEntry, val other: WordEntry, val contrast: Contrast) {
    enum class Contrast { TONE, SOUND }
}

/**
 * Tone drills built from the Mandarin word list. The word list writes 不 and 一 as they are said in
 * the word (bú kèqi, yíyàng) and third tones as written in a dictionary (kěyǐ); a 不 or 一 whose
 * written tone disagrees with the rule is left out, and [disagreements] lists those for review (a
 * neutral 不, as in 对不起, is correct and not counted).
 */
object ToneDrills {
    private fun syllables(e: WordEntry): List<PinyinSyllable>? {
        val p = e.pronunciation ?: return null
        val s = Pinyin.syllables(p)
        // One syllable per character; erhua (点儿 → diǎnr) and other mismatches are skipped.
        return s.takeIf { it.size == e.term.length && e.term.all { c -> c in '一'..'鿿' } }
    }

    /** The tone 不 or 一 should have before [next] (null when the rule does not apply). */
    fun expectedTone(c: Char, next: PinyinSyllable?): Int? = when (c) {
        '不' -> when (next?.tone) { null, 5 -> null; 4 -> 2; else -> 4 }
        '一' -> when (next?.tone) { null, 5 -> null; 4 -> 2; else -> 4 }
        else -> null
    }

    fun sandhi(list: WordList): List<SyllableQuestion> = list.entries.flatMap { e ->
        val syl = syllables(e) ?: return@flatMap emptyList()
        buildList {
            e.term.forEachIndexed { i, c ->
                val expected = expectedTone(c, syl.getOrNull(i + 1))
                if (expected != null && syl[i].tone == expected) {
                    val kind = if (c == '不') ToneDrillKind.BU else ToneDrillKind.YI
                    val base = syl[i].base
                    val tones = if (c == '不') listOf(4, 2) else listOf(1, 2, 4)
                    add(SyllableQuestion(kind, e.id, e.term, i, syl, tones.map { Pinyin.applyTone(base, it) }, tones.indexOf(expected), e.meaning))
                }
            }
            // Two third tones in a row (only pairs: longer chains depend on how the phrase is grouped).
            for (i in 0 until syl.size - 1) {
                val chain = syl[i].tone == 3 && syl[i + 1].tone == 3 && syl.getOrNull(i - 1)?.tone != 3 && syl.getOrNull(i + 2)?.tone != 3
                if (chain) {
                    val spoken = syl.toMutableList().also { it[i] = PinyinSyllable(syl[i].base, 2) }
                    add(SyllableQuestion(ToneDrillKind.THIRD_TONES, e.id, e.term, i, spoken, listOf(3, 2).map { Pinyin.applyTone(syl[i].base, it) }, 1, e.meaning))
                }
            }
        }
    }

    fun neutral(list: WordList): List<SyllableQuestion> = list.entries.mapNotNull { e ->
        val syl = syllables(e)?.takeIf { it.size >= 2 } ?: return@mapNotNull null
        val i = syl.indexOfLast { it.tone == 5 }.takeIf { it > 0 } ?: return@mapNotNull null
        SyllableQuestion(ToneDrillKind.NEUTRAL, e.id, e.term, i, syl, (1..5).map { Pinyin.applyTone(syl[i].base, it) }, 4, e.meaning)
    }

    /** Words written with a 不 or 一 whose tone does not follow the rule, for the content reviewers. */
    fun disagreements(list: WordList): List<String> = list.entries.flatMap { e ->
        val syl = syllables(e) ?: return@flatMap emptyList()
        e.term.indices.mapNotNull { i ->
            val expected = expectedTone(e.term[i], syl.getOrNull(i + 1)) ?: return@mapNotNull null
            // A neutral 不 (对不起, 差不多) is right in those words and is not a disagreement.
            if (syl[i].tone != expected && syl[i].tone != 5) "${e.term} ${e.pronunciation}: ${e.term[i]} should be tone $expected" else null
        }
    }

    private val confusable = listOf("zh" to "j", "ch" to "q", "sh" to "x", "z" to "zh", "c" to "ch", "s" to "sh", "n" to "l")

    private fun key(syl: List<PinyinSyllable>) = syl.joinToString(" ") { it.base.lowercase() }

    fun listeningPairs(list: WordList): List<ListeningPair> {
        val withSyl = list.entries.mapNotNull { e -> syllables(e)?.takeIf { it.size <= 2 }?.let { e to it } }
        val byBase = withSyl.groupBy { key(it.second) }
        val pairs = mutableListOf<ListeningPair>()
        // Same sounds, different tones.
        byBase.values.forEach { group ->
            val distinct = group.distinctBy { (_, s) -> s.joinToString { it.marked } }
            for (i in distinct.indices) for (j in i + 1 until distinct.size) {
                // The word is spoken from its characters, so one character with two readings (得 dé / de)
                // cannot be told apart by ear here and is left out.
                if (distinct[i].first.term != distinct[j].first.term) pairs += ListeningPair(distinct[i].first, distinct[j].first, ListeningPair.Contrast.TONE)
            }
        }
        // Same tones, one confusable initial changed (zh/j, ch/q, sh/x, z/zh, c/ch, s/sh, n/l).
        val byMarked = withSyl.associateBy { (_, s) -> s.joinToString(" ") { it.marked } }
        withSyl.forEach { (e, s) ->
            val first = s[0]
            confusable.forEach { (a, b) ->
                if (first.base.startsWith(a) && !(a.length == 1 && first.base.startsWith(a + "h"))) {
                    val swapped = listOf(PinyinSyllable(b + first.base.removePrefix(a), first.tone)) + s.drop(1)
                    byMarked[swapped.joinToString(" ") { it.marked }]?.let { (o, _) -> if (o.term != e.term) pairs += ListeningPair(e, o, ListeningPair.Contrast.SOUND) }
                }
            }
        }
        return pairs.distinctBy { setOf(it.heard.id, it.other.id) }
    }
}
