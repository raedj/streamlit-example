package com.polyglot.academy.ui

import com.polyglot.academy.core.ai.AiCapability
import com.polyglot.academy.core.learning.RecommendationReason
import com.polyglot.academy.core.model.CefrLevel
import com.polyglot.academy.core.model.CorrectionIntensity
import com.polyglot.academy.core.model.DataCategory
import com.polyglot.academy.core.model.ErrorSeverity
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.LearningGoal
import com.polyglot.academy.core.model.LearningStyle
import com.polyglot.academy.core.model.MaterialStatus
import com.polyglot.academy.core.model.MaterialType
import com.polyglot.academy.core.model.ProviderLocation
import com.polyglot.academy.core.model.SessionKind
import com.polyglot.academy.core.model.Skill
import com.polyglot.academy.core.model.ThemeMode
import com.polyglot.academy.core.service.AchievementService
import com.polyglot.academy.core.service.ConversationNotice
import com.polyglot.academy.core.service.OfflineReason
import com.polyglot.academy.core.service.WritingCheck
import com.polyglot.academy.core.speech.SpeechErrorKind
import com.polyglot.academy.core.tutor.CorrectionStrategy
import com.polyglot.academy.core.tutor.TeachingStyle
import com.polyglot.academy.core.localai.IncompatibilityReason
import com.polyglot.academy.core.media.ExtractionSupport
import com.polyglot.academy.ui.i18n.K
import com.polyglot.academy.ui.i18n.Labels
import com.polyglot.academy.ui.i18n.Localizer
import com.polyglot.academy.ui.navigation.Route
import com.polyglot.academy.ui.navigation.SettingsSection
import com.polyglot.academy.ui.navigation.Tab
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class LocalizationTest {
    private val en = Localizer(LanguageCode.ENGLISH)
    private val ar = Localizer(LanguageCode.ARABIC)
    private val enTable = com.polyglot.academy.ui.i18n.EnglishStrings.table
    private val arTable = com.polyglot.academy.ui.i18n.ArabicStrings.table

    @Test fun everyKeyIsTranslatedInBothLanguages() {
        K.entries.forEach { k ->
            assertTrue(!enTable[k].isNullOrBlank(), "missing English for $k")
            assertTrue(!arTable[k].isNullOrBlank(), "missing Arabic for $k")
        }
    }

    @Test fun placeholdersMatchBetweenLanguages() {
        K.entries.forEach { k ->
            val e = Localizer.placeholder.findAll(enTable.getValue(k)).map { it.value }.toSet()
            val a = Localizer.placeholder.findAll(arTable.getValue(k)).map { it.value }.toSet()
            // Arabic zero/one/two forms may legitimately omit the number.
            if ('|' !in enTable.getValue(k)) assertEquals(e, a, "placeholder mismatch for $k")
        }
    }

    @Test fun pluralFormsHaveTheRightCount() {
        K.entries.filter { '|' in enTable.getValue(it) }.forEach { k ->
            assertEquals(2, enTable.getValue(k).split('|').size, "English plural forms for $k")
            assertEquals(6, arTable.getValue(k).split('|').size, "Arabic plural forms for $k")
        }
    }

    @Test fun arabicPluralCategories() {
        assertEquals("يومان", ar.plural(K.DAYS, 2))
        assertEquals("5 أيام", ar.plural(K.DAYS, 5))
        assertEquals("11 يومًا", ar.plural(K.DAYS, 11))
        assertEquals("100 يوم", ar.plural(K.DAYS, 100))
        assertEquals("1 day", en.plural(K.DAYS, 1))
        assertEquals("3 days", en.plural(K.DAYS, 3))
    }

    @Test fun arabicIsRtlAndDiffers() {
        assertTrue(ar.isRtl)
        assertTrue(!en.isRtl)
        assertNotEquals(en[K.NAV_HOME], ar[K.NAV_HOME])
    }

    @Test fun everyEnumHasALabel() {
        for (l in listOf(en, ar)) {
            CefrLevel.entries.forEach { Labels.level(l, it) }
            LearningGoal.entries.forEach { Labels.goal(l, it) }
            LearningStyle.entries.forEach { Labels.style(l, it); Labels.styleDesc(l, it) }
            Skill.entries.forEach { Labels.skill(l, it) }
            CorrectionIntensity.entries.forEach { Labels.correction(l, it); Labels.correctionDesc(l, it) }
            ThemeMode.entries.forEach { Labels.theme(l, it) }
            ErrorSeverity.entries.forEach { Labels.severity(l, it) }
            SpeechErrorKind.entries.forEach { Labels.speechError(l, it) }
            OfflineReason.entries.forEach { Labels.offlineReason(l, it) }
            RecommendationReason.entries.forEach { Labels.reason(l, it) }
            TeachingStyle.entries.forEach { Labels.teachingStyle(l, it) }
            CorrectionStrategy.entries.forEach { Labels.correctionStrategy(l, it) }
            DataCategory.entries.forEach { Labels.category(l, it); Labels.categoryDesc(l, it) }
            MaterialType.entries.forEach { Labels.materialType(l, it) }
            MaterialStatus.entries.forEach { Labels.materialStatus(l, it) }
            SessionKind.entries.forEach { Labels.session(l, it) }
            AiCapability.entries.forEach { Labels.capability(l, it) }
            ProviderLocation.entries.forEach { Labels.location(l, it) }
            (1..7).forEach { Labels.day(l, it); Labels.dayInitial(l, it) }
            ConversationNotice.entries.forEach { K.valueOf("CONV_NOTICE_${it.name}") }
            WritingCheck.entries.forEach { K.valueOf("WCHECK_${it.name}") }
            IncompatibilityReason.entries.forEach { K.valueOf("LOCAL_REASON_${it.name}") }
            ExtractionSupport.entries.forEach { K.valueOf("SUPPORT_${it.name}") }
            (1..5).forEach { K.valueOf("TONE_$it") }
            (1..6).forEach { K.valueOf("HELP_Q$it"); K.valueOf("HELP_A$it") }
            com.polyglot.academy.core.model.RecallRating.entries.forEach { K.valueOf("RATING_${it.name}") }
            AchievementService.all.forEach { id -> assertNotEquals(id, Labels.achievement(l, id), "achievement label for $id") }
        }
    }

    @Test fun durationsAreHumanReadable() {
        assertEquals("10 min", Labels.duration(en, 10 * 60_000L))
        assertEquals("4 days", Labels.duration(en, 4 * 86_400_000L))
        assertEquals("2 months", Labels.duration(en, 62 * 86_400_000L))
    }

    @Test fun routesRoundTripThroughSavedState() {
        val routes = listOf(
            Route.TabRoot(Tab.LEARN), Route.Courses("finance"), Route.Courses(), Route.LessonOverview("en_l_cafe"), Route.LessonPlayer("x", true),
            Route.Review, Route.GrammarTopic("g"), Route.ReadingPassage("p"), Route.Material("m"), Route.Assessment(LanguageCode.FRENCH, true),
            Route.Assessment(), Route.TutorDetail("emma"), Route.Conversation("emma", "s", true), Route.Conversation("emma"),
            Route.SettingsPage(SettingsSection.PRIVACY), Route.ProviderDetail(com.polyglot.academy.core.ai.ProviderId.GEMINI), Route.History, Route.Characters, Route.SpeakingPath, Route.Conversation("emma", "mission:b1_story"),
        )
        routes.forEach { assertEquals(it, Route.decode(Route.encode(it))) }
    }
}
