package com.polyglot.academy.core

import com.polyglot.academy.core.content.Curriculum
import com.polyglot.academy.core.model.CefrLevel
import com.polyglot.academy.core.model.Gloss
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.PartOfSpeech
import com.polyglot.academy.core.wordlist.WordEntry
import com.polyglot.academy.core.wordlist.WordExample
import com.polyglot.academy.core.wordlist.WordLevels
import com.polyglot.academy.core.wordlist.WordLists
import com.polyglot.academy.core.wordlist.WordMatcher
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Checks every word list that has been written so far (see docs/PROJECT_STATE.json for progress). */
class WordListTest {
    private val lists = LanguageCode.entries.map { WordLists.of(it) }.filter { it.entries.isNotEmpty() }

    private fun report(problems: List<String>) = assertTrue(problems.isEmpty(), "${problems.size} problem(s):\n" + problems.take(40).joinToString("\n"))

    @Test fun atLeastOneListExists() = assertTrue(lists.isNotEmpty())

    @Test fun ranksAreConsecutiveInWholeLevelsAndLevelsMatchRanks() {
        for (l in lists) {
            assertEquals((1..l.entries.size).toList(), l.entries.map { it.rank }, "${l.language} ranks")
            assertTrue(l.entries.size % WordLevels.SIZE == 0 && l.entries.size <= WordLevels.TARGET, "${l.language} has ${l.entries.size} entries")
            l.entries.forEach { assertEquals((it.rank - 1) / 300 + 1, it.level) }
            assertEquals(l.entries.size, l.byId.size, "${l.language} ids must be unique")
        }
    }

    @Test fun requiredFieldsAndExampleCounts() = report(lists.flatMap { l ->
        l.entries.flatMap { e ->
            buildList {
                if (listOf(e.term, e.topic, e.meaning.en, e.meaning.ar).any { it.isBlank() }) add("${e.id}: empty field")
                val need = if (e.level <= 3) 2 else 1
                if (e.examples.size < need) add("${e.id}: ${e.examples.size} example(s), needs $need")
                e.examples.filter { it.sentence.isBlank() || it.translation.en.isBlank() || it.translation.ar.isBlank() }.forEach { add("${e.id}: example without both translations: ${it.sentence}") }
                // A neutral tone is written with a leading middle dot (的 ·de).
                if (e.language == LanguageCode.MANDARIN && e.pronunciation?.any { it in "āáǎàēéěèīíǐìōóǒòūúǔùǖǘǚǜ·" } != true) add("${e.id}: pinyin without tone marks")
                if (e.language == LanguageCode.ARABIC && e.term.count { it in 'ء'..'ي' } > 1 && e.term.none { it in 'ً'..'ْ' }) add("${e.id}: Arabic term without vowel marks")
            }
        }
    })

    @Test fun noDuplicateWordWithTheSameMeaningAndPartOfSpeech() = report(lists.flatMap { l ->
        l.entries.groupBy { Triple(it.term.lowercase(), it.partOfSpeech, it.meaning.en.lowercase()) }.filter { it.value.size > 1 }.map { "${l.language}: duplicate ${it.key}" }
    })

    @Test fun everyExampleContainsItsWord() = report(lists.flatMap { l ->
        l.entries.flatMap { e -> e.examples.filterNot { l.matcher.contains(e, it.sentence, it.names) }.map { "${e.id} (${e.term}) not found in: ${it.sentence}" } }
    })

    @Test fun examplesUseWordsFromTheSameLevelOrBelow() = report(lists.flatMap { l ->
        l.entries.flatMap { e ->
            e.examples.filter { l.matcher.coverage(e, it) < WordLevels.EXAMPLE_COVERAGE }
                .map { "${e.id}: ${(l.matcher.coverage(e, it) * 100).toInt()}% in range; outside: ${l.matcher.outOfRange(e, it)} — ${it.sentence}" }
        }
    })

    @Test fun linkedCurriculumWordsKeepTheirReviewId() = report(lists.flatMap { l ->
        val vocab = Curriculum.of(l.language).vocabularyById
        l.entries.filter { it.vocabularyId != null }.flatMap { e ->
            buildList {
                if (e.vocabularyId !in vocab) add("${e.id}: linked id ${e.vocabularyId} does not exist in the curriculum")
                if (e.toVocabularyItem().id != e.vocabularyId) add("${e.id}: review id changed")
            }
        }
    })

    @Test fun checksRejectWordsOutsideTheListOnRealData() {
        val en = WordLists.of(LanguageCode.ENGLISH)
        if (en.entries.isEmpty()) return
        val the = en.byRank.getValue(1)
        assertEquals(listOf("zebra", "spaghetti"), en.matcher.outOfRange(the, WordExample("The zebra eats spaghetti.", Gloss("", ""))))
        assertTrue(en.matcher.coverage(the, WordExample("The zebra eats spaghetti.", Gloss("", ""))) < WordLevels.EXAMPLE_COVERAGE)
        val dog = en.entries.first { it.term == "dog" }
        assertFalse(en.matcher.contains(dog, "The cat is sleeping."))
        assertTrue(en.matcher.contains(en.entries.first { it.term == "go" }, "She went home."))
    }

    @Test fun levelsMapApproximatelyToCefr() {
        assertEquals(listOf(CefrLevel.A1, CefrLevel.A1, CefrLevel.A2, CefrLevel.A2, CefrLevel.B1, CefrLevel.B1, CefrLevel.B2, CefrLevel.B2, CefrLevel.C1, CefrLevel.C1),
            (1..10).map { WordLevels.cefr(it) })
        assertEquals(301..600, WordLevels.ranks(2))
        assertEquals(10, WordLevels.levelOf(3000))
    }

    private fun entry(lang: LanguageCode, rank: Int, term: String, forms: List<String> = emptyList(), pos: PartOfSpeech = PartOfSpeech.NOUN) =
        WordEntry(lang, rank, "${lang.tag}_w_$rank", term, pos, "t", Gloss(term, term), null, forms, emptyMap(), null, emptyList())

    @Test fun matcherHandlesInflectionPerLanguage() {
        val en = listOf(entry(LanguageCode.ENGLISH, 1, "go", listOf("went", "gone")), entry(LanguageCode.ENGLISH, 2, "city"), entry(LanguageCode.ENGLISH, 3, "stop"), entry(LanguageCode.ENGLISH, 400, "zebra"))
        val m = WordMatcher(LanguageCode.ENGLISH, en)
        assertTrue(m.contains(en[0], "She went home.") && m.contains(en[1], "Two big cities.") && m.contains(en[2], "It stopped."))
        assertEquals(listOf("zebra"), m.outOfRange(en[0], WordExample("Go, zebra, go!", Gloss("", ""))))
        assertTrue(m.tokens("Ali and Mona go.", listOf("Ali", "Mona")) == listOf("and", "go"))
        assertEquals(listOf("mi", "hermana", "se", "llama"), WordMatcher(LanguageCode.SPANISH, emptyList()).tokens("Mi hermana se llama Ana.", listOf("Ana")),
            "a name is removed as a whole word only")

        val fr = listOf(entry(LanguageCode.FRENCH, 1, "le", listOf("l'", "la", "les")), entry(LanguageCode.FRENCH, 2, "eau"), entry(LanguageCode.FRENCH, 3, "petit"))
        val mf = WordMatcher(LanguageCode.FRENCH, fr)
        assertTrue(mf.contains(fr[0], "J'aime l'eau.") && mf.contains(fr[1], "L'eau est froide.") && mf.contains(fr[2], "Une petite maison."))

        val ar = listOf(entry(LanguageCode.ARABIC, 1, "كِتَاب"), entry(LanguageCode.ARABIC, 2, "مَدْرَسَة"))
        val ma = WordMatcher(LanguageCode.ARABIC, ar)
        assertTrue(ma.contains(ar[0], "قرأتُ الكتابَ أمس.") && ma.contains(ar[0], "وبكتابه") && ma.contains(ar[1], "ذهبت إلى المدرسة"))
        val go = WordEntry(LanguageCode.ARABIC, 3, "ar_w_go", "ذَهَبَ", PartOfSpeech.VERB, "t", Gloss("go", "ذهب"), null, emptyList(),
            mapOf("present" to "يَذْهَبُ"), null, emptyList())
        val book = WordEntry(LanguageCode.ARABIC, 4, "ar_w_book", "كِتَاب", PartOfSpeech.NOUN, "t", Gloss("book", "كتاب"), null, emptyList(),
            mapOf("plural" to "كُتُب"), null, emptyList())
        val mg = WordMatcher(LanguageCode.ARABIC, listOf(go, book, entry(LanguageCode.ARABIC, 5, "كَبِير")))
        assertTrue(listOf("سأذهب غدًا", "نذهب معًا", "ذهبوا").all { mg.contains(go, it) }, "present prefixes and past endings")
        assertTrue(mg.contains(book, "قرأت الكتب") && mg.contains(book, "كتبهم"), "broken plural, also with a pronoun")
        assertTrue(mg.candidates("الكبيرة").contains("كبير"), "feminine ending")
        assertFalse(mg.contains(book, "ذهبوا إلى البيت"))

        val tr = listOf(entry(LanguageCode.TURKISH, 1, "ev"), entry(LanguageCode.TURKISH, 2, "gelmek", pos = PartOfSpeech.VERB))
        val mt = WordMatcher(LanguageCode.TURKISH, tr)
        assertTrue(mt.contains(tr[0], "Annem evde.") && mt.contains(tr[1], "Yarın geliyorum."))
        assertEquals(listOf("annem", "evde"), mt.tokens("Annem İstanbul'da evde.", listOf("İstanbul")))
        val tr2 = listOf(entry(LanguageCode.TURKISH, 1, "bu"), entry(LanguageCode.TURKISH, 2, "kitap"), entry(LanguageCode.TURKISH, 3, "beklemek", pos = PartOfSpeech.VERB),
            entry(LanguageCode.TURKISH, 4, "gitmek", pos = PartOfSpeech.VERB), entry(LanguageCode.TURKISH, 5, "çocuk"))
        val mt2 = WordMatcher(LanguageCode.TURKISH, tr2)
        assertTrue(mt2.contains(tr2[1], "Kitabı okudum.") && mt2.contains(tr2[2], "Seni bekliyorum.") && mt2.contains(tr2[3], "Okula gidiyoruz.")
            && mt2.contains(tr2[4], "Çocuğun adı ne?") && mt2.contains(tr2[0], "Bunlar ne?"), "suffixes, softening and narrowing")
        assertEquals(null, mt2.rankOf("bulmak"), "a stem needs a complete chain of suffixes after it")

        val ur = listOf(entry(LanguageCode.URDU, 1, "جانا", pos = PartOfSpeech.VERB), entry(LanguageCode.URDU, 2, "لڑکا"),
            entry(LanguageCode.URDU, 3, "لڑکی"), entry(LanguageCode.URDU, 4, "اچھا", pos = PartOfSpeech.ADJECTIVE))
        val mu = WordMatcher(LanguageCode.URDU, ur)
        assertTrue(mu.contains(ur[0], "وہ روز اسکول جاتی ہے۔") && mu.contains(ur[0], "ہم کل جائیں گے۔") && mu.contains(ur[1], "لڑکوں نے کھانا کھایا۔")
            && mu.contains(ur[2], "دو لڑکیاں آئیں۔") && mu.contains(ur[3], "یہ اچھی کتاب ہے۔"), "Urdu endings and verb stems")
        assertFalse(mu.contains(ur[3], "لڑکا آیا۔"))

        val ms = listOf(entry(LanguageCode.MALAY, 1, "beli"), entry(LanguageCode.MALAY, 2, "tulis"))
        val mm = WordMatcher(LanguageCode.MALAY, ms)
        assertTrue(mm.contains(ms[0], "Dia membeli roti.") && mm.contains(ms[1], "Surat itu ditulis semalam.") && mm.contains(ms[1], "Saya menulis surat."))

        val zh = listOf(entry(LanguageCode.MANDARIN, 1, "我"), entry(LanguageCode.MANDARIN, 2, "喜欢"), entry(LanguageCode.MANDARIN, 3, "中国"))
        val mz = WordMatcher(LanguageCode.MANDARIN, zh)
        assertEquals(listOf("我", "喜欢", "中国"), mz.tokens("我喜欢中国。"))
        assertFalse(mz.contains(zh[2], "我喜欢你。"))
        assertEquals(listOf("我", "喜欢"), mz.tokens("我喜欢小明。", listOf("小明")), "names are removed inside unspaced Chinese text")
        assertEquals(listOf("你"), mz.outOfRange(zh[0], WordExample("我喜欢你。", Gloss("", ""))))
    }
}
