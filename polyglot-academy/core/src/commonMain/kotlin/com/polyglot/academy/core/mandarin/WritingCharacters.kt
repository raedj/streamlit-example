package com.polyglot.academy.core.mandarin

import com.polyglot.academy.core.content.Curriculum
import com.polyglot.academy.core.model.Gloss
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.wordlist.WordLevels
import com.polyglot.academy.core.wordlist.WordList

/** One character to practise writing, with a word it appears in for context. */
data class WritingItem(val character: Char, val word: String, val reading: String?, val meaning: Gloss) {
    /** Hand-checked count where there is one, otherwise the number of strokes in the stroke data. */
    val strokes: Int get() = HanziData.of(character)?.strokes ?: StrokeOrder.of(character)?.size ?: 0
}

/**
 * The sets of characters offered in writing practice: the course characters (the ones with a
 * hand-written breakdown) and, for each word-list level, the characters that level introduces.
 * With [traditional], each character is replaced by its traditional form taken from the word it
 * comes from, where that form differs. Only characters with stroke data are offered.
 */
object WritingCharacters {
    fun course(traditional: Boolean): List<WritingItem> =
        Curriculum.of(LanguageCode.MANDARIN).vocabulary.flatMap { v ->
            v.term.indices.filter { HanziData.of(v.term[it]) != null }.map { i ->
                item(v.term, v.traditional, i, v.pronunciation, v.meaning, traditional)
            }
        }.filter { StrokeOrder.of(it.character) != null }.distinctBy { it.character }

    /** Characters first met in [level]'s words (not in an earlier level), in word-rank order. */
    fun level(list: WordList, level: Int, traditional: Boolean): List<WritingItem> {
        val earlier = list.entries.filter { it.rank < WordLevels.ranks(level).first }.flatMap { it.term.toList() }.toSet()
        return list.entries.filter { it.rank in WordLevels.ranks(level) }.sortedBy { it.rank }.flatMap { e ->
            e.term.indices.filter { e.term[it].isHan() && e.term[it] !in earlier }.map { i ->
                item(e.term, e.extra["trad"], i, e.pronunciation, e.meaning, traditional)
            }
        }.filter { StrokeOrder.of(it.character) != null }.distinctBy { it.character }
    }

    private fun item(term: String, trad: String?, i: Int, reading: String?, meaning: Gloss, traditional: Boolean): WritingItem {
        // The traditional form is used only when it lines up character by character with the term.
        val t = trad?.takeIf { traditional && it.length == term.length }
        val c = t?.get(i)?.takeIf { StrokeOrder.of(it) != null } ?: term[i]
        return WritingItem(c, if (t != null) t else term, reading, meaning)
    }

    private fun Char.isHan() = this in '一'..'鿿'
}
