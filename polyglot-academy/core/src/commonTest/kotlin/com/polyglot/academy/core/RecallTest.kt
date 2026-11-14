package com.polyglot.academy.core

import com.polyglot.academy.core.content.Curriculum
import com.polyglot.academy.core.learning.AnswerChecker
import com.polyglot.academy.core.learning.AnswerVerdict
import com.polyglot.academy.core.learning.Recall
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.RecallRating
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RecallTest {
    private fun word(language: LanguageCode, id: String) = Curriculum.of(language).vocabularyById.getValue("${language.tag}_$id")
    private fun verdict(answer: String, language: LanguageCode, id: String) = AnswerChecker.check(answer, Recall.acceptedAnswers(word(language, id))).verdict

    @Test fun englishDoesNotFailALeftOutArticle() {
        assertEquals(AnswerVerdict.CORRECT, verdict("the bill", LanguageCode.ENGLISH, "bill"))
        assertEquals(AnswerVerdict.CORRECT, verdict("Bill", LanguageCode.ENGLISH, "bill"))
    }

    @Test fun mandarinAcceptsCharactersAndPinyin() {
        assertEquals(AnswerVerdict.CORRECT, verdict("你好", LanguageCode.MANDARIN, "nihao"))
        assertEquals(AnswerVerdict.CORRECT, verdict("nǐ hǎo", LanguageCode.MANDARIN, "nihao"))
        assertEquals(AnswerVerdict.CORRECT_WITH_ACCENT_SLIP, verdict("ni hao", LanguageCode.MANDARIN, "nihao"))
        assertEquals(AnswerVerdict.CORRECT_WITH_ACCENT_SLIP, verdict("nihao", LanguageCode.MANDARIN, "nihao"))
        assertEquals(AnswerVerdict.CORRECT, verdict("謝謝", LanguageCode.MANDARIN, "xiexie"))
        assertEquals(AnswerVerdict.INCORRECT, verdict("xiexie", LanguageCode.MANDARIN, "nihao"))
    }

    @Test fun malayIsCheckedAsTyped() {
        assertEquals(AnswerVerdict.CORRECT, verdict("selamat pagi", LanguageCode.MALAY, "selamat_pagi"))
        assertEquals(AnswerVerdict.ALMOST, verdict("selamat pagy", LanguageCode.MALAY, "selamat_pagi"))
        assertEquals(AnswerVerdict.INCORRECT, verdict("terima kasih", LanguageCode.MALAY, "selamat_pagi"))
    }

    @Test fun anotherKeyboardIsNotAWrongAnswer() {
        assertEquals(AnswerVerdict.WRONG_KEYBOARD, verdict("مرحبا", LanguageCode.ENGLISH, "hello"))
        assertEquals(AnswerVerdict.WRONG_KEYBOARD, verdict("مرحبا", LanguageCode.MANDARIN, "nihao"))
        assertEquals(AnswerVerdict.WRONG_KEYBOARD, AnswerChecker.check("salam", listOf("سلام")).verdict)
    }

    @Test fun oneSlipIsATypoOnlyInAWordLongEnough() {
        assertEquals(AnswerVerdict.ALMOST, AnswerChecker.check("helo", listOf("hello")).verdict)
        assertEquals(AnswerVerdict.INCORRECT, AnswerChecker.check("cat", listOf("bat")).verdict, "another word, not a typo")
    }

    @Test fun theVerdictSetsTheSchedule() {
        assertEquals(RecallRating.GOOD, Recall.rating(AnswerChecker.check("hello", listOf("hello"))))
        assertEquals(RecallRating.HARD, Recall.rating(AnswerChecker.check("helllo", listOf("hello"))))
        assertEquals(RecallRating.AGAIN, Recall.rating(AnswerChecker.check("cat", listOf("hello"))))
    }

    @Test fun tilesCanBuildTheAnswerInAnyScript() {
        fun built(answer: String) = Recall.tiles(answer, listOf("thank you", "再见", "terima kasih"), 7)
        assertTrue(built("hello").containsAll("hello".map { it.toString() }))
        assertTrue(" " in built("good morning"), "two words need a space")
        assertTrue(built("你好").containsAll(listOf("你", "好")))
        assertTrue(built("你好").size > 2, "a character answer needs distractors or there is nothing to recall")
        assertTrue(built("Could we have the bill").containsAll(listOf("Could", "we", "have", "the", "bill")))
        assertEquals(built("hello"), built("hello"), "the same every time")
    }
}
