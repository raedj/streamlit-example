package com.polyglot.academy.core

import com.polyglot.academy.core.content.Curriculum
import com.polyglot.academy.core.model.CefrLevel
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.stories.StoryGenerator
import com.polyglot.academy.core.stories.StoryText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StoriesTest {
    private fun story(id: String) = Curriculum.all.values.flatMap { it.readings }.single { it.id == id }

    @Test fun everyLanguageHasGradedStoriesAtThreeLevels() {
        LanguageCode.entries.forEach { l ->
            val levels = Curriculum.of(l).readings.filter { it.glossary.isNotEmpty() }.map { it.level }.toSet()
            assertTrue(levels.containsAll(setOf(CefrLevel.A1, CefrLevel.A2, CefrLevel.B1)), "$l: $levels")
            assertTrue(Curriculum.of(l).validate().isEmpty(), Curriculum.of(l).validate().joinToString())
        }
    }

    @Test fun phrasesAreTappedAsOneAndWordsAreFound() {
        val s = story("en_st_lost_bag")
        val known = StoryText.knownTerms(s, Curriculum.of(LanguageCode.ENGLISH).vocabulary)
        val tokens = StoryText.tokens(s.sentences[3], LanguageCode.ENGLISH, known).filter { it.isWord }.map { it.text }
        assertTrue("information desk" in tokens, tokens.toString())
        assertEquals("مكتب الاستعلامات", StoryText.lookup("information desk", s, emptyList())!!.meaning.ar)
        assertEquals(listOf("It", "was", "his", "bag", "Karim", "was", "so", "relieved"), StoryText.tokens(s.sentences[7], LanguageCode.ENGLISH).filter { it.isWord }.map { it.text })
    }

    @Test fun frenchElisionArabicPrefixesAndChineseSegmentation() {
        val fr = story("fr_st_valise")
        assertEquals("airport", StoryText.lookup("l'aéroport", fr, emptyList())!!.meaning.en)
        val frTokens = StoryText.tokens(fr.sentences[3], LanguageCode.FRENCH, StoryText.knownTerms(fr, emptyList())).filter { it.isWord }.map { it.text }
        assertTrue("comptoir d'information" in frTokens, frTokens.toString())
        val ar = story("ar_st_tatawwu")
        assertEquals("he encourages", StoryText.lookup("يشجّع", ar, emptyList())!!.meaning.en)
        assertNotNull(StoryText.lookup("والقصص", ar, emptyList()), "attached و + article are stripped")
        val zh = story("zh_st_xinglixiang")
        val zhTokens = StoryText.tokens(zh.sentences[5], LanguageCode.MANDARIN, StoryText.knownTerms(zh, Curriculum.of(LanguageCode.MANDARIN).vocabulary))
        assertTrue(zhTokens.any { it.text == "电话号码" } && zhTokens.any { it.text == "地址" }, zhTokens.toString())
        assertEquals("diànhuà hàomǎ", StoryText.lookup("电话号码", zh, emptyList())!!.reading)
        assertNull(StoryText.lookup("。", zh, emptyList()))
    }

    @Test fun generatedStoriesAreValidatedBeforeTheyAreShown() {
        val raw = """Here: {"title":"Mi gato","title_en":"My cat","title_ar":"قطّي","sentences":[
            {"text":"Tengo un gato.","en":"I have a cat.","ar":"لديّ قط."},{"text":"Se llama Sol.","en":"His name is Sol.","ar":"اسمه سول."},
            {"text":"Sol duerme mucho.","en":"Sol sleeps a lot.","ar":"سول ينام كثيرًا."}],
            "glossary":[{"term":"gato","en":"cat","ar":"قط"},{"term":"perro","en":"dog","ar":"كلب"},{"term":"duerme","en":"sleeps","ar":""}],
            "questions":[{"question":"¿Cómo se llama el gato?","options":["Sol","Luna","Sol"],"answer":0},{"question":"¿Qué hace Sol?","options":["Duerme","Come"],"answer":0},
            {"question":"x","options":["a","b"],"answer":5}]}"""
        val ok = assertIs<StoryGenerator.Parsed.Ok>(StoryGenerator.parse(raw, "story_1", LanguageCode.SPANISH, CefrLevel.A1, "gatos", 5)).story
        assertEquals(listOf("gato", "duerme"), ok.glossary.map { it.term }, "terms not in the text are dropped")
        assertEquals("sleeps", ok.glossary[1].meaning.ar, "missing Arabic falls back to English")
        assertEquals(1, ok.questions.size, "duplicate options and bad answer indexes are dropped")
        assertTrue(ok.generated && ok.interest == "gatos")
        assertIs<StoryGenerator.Parsed.Invalid>(StoryGenerator.parse("""{"title":"x","sentences":[{"text":"a"}]}""", "s", LanguageCode.SPANISH, CefrLevel.A1, "x", 0))
        assertIs<StoryGenerator.Parsed.Invalid>(StoryGenerator.parse("no json", "s", LanguageCode.SPANISH, CefrLevel.A1, "x", 0))
        assertEquals("football and cooking", StoryGenerator.cleanInterest(" football\n\"and\" {cooking} "))
        val p = StoryGenerator.systemPrompt(LanguageCode.MANDARIN, LanguageCode.ARABIC, CefrLevel.A2, "football", listOf("茶"))
        assertTrue("pinyin" in p && "\"football\"" in p && "茶" in p && "6–9" in p)
    }
}
