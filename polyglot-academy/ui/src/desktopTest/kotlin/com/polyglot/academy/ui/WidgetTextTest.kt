package com.polyglot.academy.ui

import com.polyglot.academy.core.model.Gloss
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.service.WidgetSummary
import com.polyglot.academy.core.service.WidgetWord
import com.polyglot.academy.ui.app.WidgetText
import com.polyglot.academy.ui.i18n.Localizer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class WidgetTextTest {
    private val ar = Localizer(LanguageCode.ARABIC)
    private val en = Localizer(LanguageCode.ENGLISH)
    private val word = WidgetWord("你好", "nǐ hǎo", Gloss("hello", "مرحبًا"))
    private fun summary(minutes: Int = 4, goal: Int = 10, due: Int = 3, word: WidgetWord? = this.word, newLearner: Boolean = false) =
        WidgetSummary(LanguageCode.MANDARIN, minutes, goal, due, Gloss("Greetings", "التحيات"), word, newLearner)

    @Test fun progressDueAndWordInTheInterfaceLanguage() {
        val l = WidgetText.lines(summary(), ar)
        assertEquals("الصينية الماندرين", l.title)
        assertEquals("اليوم: 4 من 10 دقائق", l.progress)
        assertEquals("3 كلمات للمراجعة", l.next)
        assertEquals("你好  ·  nǐ hǎo", l.word)
        assertEquals("مرحبًا", l.meaning)
        assertEquals("Today: 4 of 15 min", WidgetText.lines(summary(goal = 15), en).progress)
        assertEquals("اليوم: 0 من 15 دقيقة", WidgetText.lines(summary(minutes = 0, goal = 15), ar).progress)
    }

    @Test fun goalReachedNextLessonAndNoWord() {
        val l = WidgetText.lines(summary(minutes = 12, due = 0, word = null), en)
        assertEquals("Today's goal is done", l.progress)
        assertEquals("Next: Greetings", l.next)
        assertNull(l.word); assertNull(l.wordLabel); assertNull(l.meaning)
    }

    @Test fun withoutALearnerTheWidgetInvitesToStart() {
        assertEquals("افتح التطبيق لتبدأ درسك الأول", WidgetText.lines(null, ar).progress)
        assertEquals("أكاديمية بوليغلوت", WidgetText.lines(null, ar).title)
    }
}
