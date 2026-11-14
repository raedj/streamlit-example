package com.polyglot.academy.core

import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.stories.StoryText
import com.polyglot.academy.core.wordlist.WordLists
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Tapping a word of a sentence to see its meaning (TappableSentence in the UI). */
class WordLookupTest {
    @Test fun everyTermLooksUpToAnEntryWithTheSameTerm() {
        val problems = LanguageCode.entries.flatMap { lang ->
            val list = WordLists.of(lang)
            val n = { t: String? -> t?.let { list.matcher.normalize(it).trim() } }
            list.entries.filter { n(list.lookup(it.term)?.term) != n(it.term) }.map { "$lang ${it.id} ${it.term} -> ${list.lookup(it.term)?.id}" }
        }
        // Homographs are listed once per meaning; the lookup gives the most frequent one, which is written the same.
        assertTrue(problems.isEmpty(), problems.take(20).joinToString("\n"))
    }

    @Test fun mostWordsOfTheExamplesCanBeLookedUpAndTheExampleWordIsFound() {
        val report = LanguageCode.entries.mapNotNull { lang ->
            val list = WordLists.of(lang)
            var words = 0; var known = 0; var examples = 0; var found = 0
            list.entries.filterIndexed { i, _ -> i % 7 == 0 }.forEach { e ->
                e.examples.forEach { ex ->
                    examples++
                    val toks = StoryText.tokens(ex.sentence, lang, list.tapTerms).filter { it.isWord && it.text !in ex.names && it.text.any(Char::isLetter) }
                    val hits = toks.map { list.lookup(it.text) }
                    words += toks.size; known += hits.count { it != null }
                    if (hits.any { it?.term == e.term } || ' ' in e.term.trim()) found++
                }
            }
            val knownShare = known.toDouble() / words
            val foundShare = found.toDouble() / examples
            // Examples are written mostly from list words (85% within rank + 300), so most taps have an answer.
            if (knownShare < 0.85 || foundShare < 0.85) "$lang known=${(knownShare * 1000).toInt()}‰ found=${(foundShare * 1000).toInt()}‰" else null
        }
        assertTrue(report.isEmpty(), report.joinToString("\n"))
    }

    @Test fun inflectedFormsAndPhrasesLeadToTheirEntry() {
        val en = WordLists.of(LanguageCode.ENGLISH)
        assertEquals("book", en.lookup("books")?.term)
        assertEquals("book", en.lookup("Book")?.term)
        assertNull(en.lookup("zzzzqx"))
        assertNull(en.lookup("  "))
        val zh = WordLists.of(LanguageCode.MANDARIN)
        val term = zh.entries.first { it.term.length == 2 }.term
        assertEquals(term, zh.lookup(term)?.term)
        val tr = WordLists.of(LanguageCode.TURKISH)
        tr.entries.firstOrNull { ' ' in it.term }?.let { assertEquals(it.term, tr.lookup(it.term)?.term) }
    }
}
