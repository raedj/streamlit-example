package com.polyglot.academy.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.ThemeMode
import com.polyglot.academy.ui.navigation.Route
import com.polyglot.academy.ui.navigation.SettingsSection
import com.polyglot.academy.ui.navigation.Tab
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class UiFlowTest {
    // A screen that never becomes idle (an endless animation or loop) fails the test instead of hanging the build.
    @get:org.junit.Rule val timeout: org.junit.rules.Timeout = org.junit.rules.Timeout.seconds(180)

    private fun androidx.compose.ui.test.ComposeUiTest.waitForText(text: String, substring: Boolean = false, timeout: Long = 10_000) =
        waitUntil(timeoutMillis = timeout) { onAllNodesWithText(text, substring = substring).fetchSemanticsNodes().isNotEmpty() }

    @Test fun onboardingCanBeCompletedAndSavesChoices() = runDesktopComposeUiTest(800, 1700) {
        val app = TestApp()
        runBlocking { app.store.updateSettings { it.copy(accessibility = it.accessibility.copy(reducedMotion = true)) } }
        setContent { AppUnderTest(app) }
        waitForText("Get started")
        onNodeWithText("Get started").performClick()
        waitForText("Choose the app language")
        onNodeWithText("Continue").performClick() // interface: English
        waitForText("What is your native language?")
        onNodeWithText("Continue").performClick() // native: Arabic
        waitForText("Which language do you want to learn?")
        onNodeWithText("Spanish").performClick()
        onNodeWithText("Continue").performClick()
        waitForText("Preferred variety")
        onNodeWithText("Mexico").performClick()
        onNodeWithText("Continue").performClick()
        waitForText("Choose my level myself")
        onNodeWithText("Choose my level myself").performClick()
        onNodeWithText("A2 · Elementary — everyday situations").performScrollTo().performClick()
        onNodeWithText("Continue").performClick()
        waitForText("Why are you learning?")
        onNodeWithText("Travel").performClick()
        onNodeWithText("Continue").performClick()
        waitForText("How much time per day?")
        onNodeWithText("30 minutes a day").performClick()
        onNodeWithText("Continue").performClick()
        waitForText("How do you like to learn?")
        onNodeWithText("Skip for now").performClick()
        waitForText("Speaking practice")
        onNodeWithText("Continue").performClick()
        waitForText("Your privacy and AI")
        onNodeWithText("Continue").performClick()
        waitForText("A gentle daily reminder?")
        onNodeWithText("Continue").performClick()
        waitForText("Your plan")
        onNodeWithText("Starting level: A2 · Elementary — everyday situations").assertExists()
        onNodeWithText("Start learning").performClick()
        waitUntil(timeoutMillis = 10_000) { runBlocking { app.store.currentProfile().onboardingCompleted } }
        val p = runBlocking { app.store.currentProfile() }
        assertEquals(LanguageCode.SPANISH, p.activeLearningLanguage)
        assertEquals("es-mx", p.enrollment(LanguageCode.SPANISH)?.variantId)
        assertEquals(30, p.dailyMinutes)
        assertTrue("STYLE" in p.skippedOnboardingSteps)
        assertTrue(!runBlocking { app.store.currentSettings() }.privacy.cloudAiEnabled, "cloud AI must stay off by default")
        waitForText("Spaced review", substring = false).also { } // Home loads its plan & dashboard
    }

    @Test fun newLearnerHomeShowsGuidanceInsteadOfStatistics() = runDesktopComposeUiTest(800, 1700) {
        val app = TestApp(); app.profile(level = null)
        setContent { AppUnderTest(app) }
        waitForText("Welcome! Let's find your starting point")
        onNodeWithText("Find your starting level").assertExists()
        assertTrue(onAllNodesWithText("This week").fetchSemanticsNodes().isEmpty(), "no charts for a learner without activity")
    }

    @Test fun lessonFlashcardAnswerIsRecorded() = runDesktopComposeUiTest(800, 1700) {
        val app = TestApp(); app.profile()
        setContent { AppUnderTest(app, listOf(Route.TabRoot(Tab.HOME), Route.LessonPlayer("en_l_greetings", restart = true))) }
        waitForText("Show meaning")
        onNodeWithText("Show meaning").performClick()
        onNodeWithText("I knew it").performClick()
        waitForText("Correct!")
        onNodeWithText("Continue").performClick()
        waitUntil(timeoutMillis = 5_000) { runBlocking { app.store.attempts(LanguageCode.ENGLISH).first().isNotEmpty() } }
        assertTrue(runBlocking { app.store.attempts(LanguageCode.ENGLISH).first().single().correct })
    }

    @Test fun wrongFillInBlankShowsCorrectAnswerAndRecordsMistake() = runDesktopComposeUiTest(800, 1700) {
        val app = TestApp(); app.profile()
        val lesson = com.polyglot.academy.core.content.Curriculum.of(LanguageCode.ENGLISH).lessonsById.getValue("en_l_greetings")
        val idx = lesson.exercises.indexOfFirst { it is com.polyglot.academy.core.model.Exercise.FillInBlank }
        runBlocking { app.services.study.saveResumePoint(lesson, idx) }
        setContent { AppUnderTest(app, listOf(Route.TabRoot(Tab.HOME), Route.LessonPlayer(lesson.id))) }
        waitUntil(timeoutMillis = 10_000) { onAllNodes(hasSetTextAction()).fetchSemanticsNodes().isNotEmpty() }
        onNode(hasSetTextAction()).performTextInput("are")
        onNodeWithText("Check").performClick()
        waitForText("Not quite")
        onNodeWithText("Answer: is").assertExists()
        waitUntil(timeoutMillis = 5_000) { runBlocking { app.store.mistakes(LanguageCode.ENGLISH).first().isNotEmpty() } }
    }

    @Test fun offlineConversationRespondsToTypedReply() = runDesktopComposeUiTest(800, 1700) {
        val app = TestApp(); app.profile()
        setContent { AppUnderTest(app, listOf(Route.TabRoot(Tab.HOME), Route.Conversation("emma", "en_s_cafe"))) }
        waitForText("Hi there! What can I get you today?")
        onNodeWithText("Offline practice partner — scripted, not AI").assertExists()
        onNode(hasSetTextAction()).performTextInput("I'd like a coffee, please")
        onNodeWithText("Send", useUnmergedTree = true).also { }
        onNode(hasText("Send") or androidx.compose.ui.test.hasContentDescription("Send")).performClick()
        waitForText("Sure. Would you like anything to eat?")
    }

    @Test fun enablingCloudAiRequiresConsent() = runDesktopComposeUiTest(800, 1700) {
        val app = TestApp(); app.profile()
        setContent { AppUnderTest(app, listOf(Route.TabRoot(Tab.ME), Route.SettingsPage(SettingsSection.PRIVACY))) }
        waitForText("Allow cloud AI")
        onNodeWithText("Allow cloud AI").performClick()
        waitForText("Before you turn on cloud AI")
        assertTrue(!runBlocking { app.store.currentSettings() }.privacy.cloudAiEnabled)
        onNodeWithText("I understand, turn on").performClick()
        waitUntil(timeoutMillis = 5_000) { runBlocking { app.store.currentSettings() }.privacy.cloudAiEnabled }
    }

    @Test fun arabicInterfaceIsLocalized() = runDesktopComposeUiTest(800, 1700) {
        val app = TestApp(); app.profile(ui = LanguageCode.ARABIC)
        setContent { AppUnderTest(app) }
        waitForText("الرئيسية")
        onNodeWithText("أكاديمية بوليغلوت").assertExists()
        onNodeWithText("المقترح التالي").assertExists()
    }

    @Test fun emptyReviewQueueShowsCaughtUpState() = runDesktopComposeUiTest(800, 1700) {
        val app = TestApp(); app.profile()
        setContent { AppUnderTest(app, listOf(Route.TabRoot(Tab.PRACTICE), Route.Review)) }
        waitForText("All caught up")
    }

    @Test fun providerScreenNeverClaimsConnectionWithoutVerification() = runDesktopComposeUiTest(800, 1700) {
        val app = TestApp(); app.profile()
        runBlocking { app.services.providers.saveConfig(com.polyglot.academy.core.ai.ProviderConfig(com.polyglot.academy.core.ai.ProviderId.OPENAI, enabled = true)) }
        setContent { AppUnderTest(app, listOf(Route.TabRoot(Tab.ME), Route.SettingsPage(SettingsSection.PROVIDERS))) }
        waitForText("Needs an API key")
        assertTrue(onAllNodesWithText("Connection verified").fetchSemanticsNodes().isEmpty())
    }

    @Test fun darkThemeRenders() = runDesktopComposeUiTest(800, 1700) {
        val app = TestApp(); app.profile(theme = ThemeMode.DARK); app.seedActivity()
        setContent { AppUnderTest(app) }
        waitForText("This week")
    }
}
