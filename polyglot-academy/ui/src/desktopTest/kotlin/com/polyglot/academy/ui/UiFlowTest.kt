package com.polyglot.academy.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.ThemeMode
import com.polyglot.academy.ui.navigation.Route
import com.polyglot.academy.ui.navigation.SettingsSection
import com.polyglot.academy.ui.navigation.Tab
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
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

    @Test fun aNewInstallStartsInArabicAndCanSwitchToEnglish() = runDesktopComposeUiTest(800, 1700) {
        val app = TestApp()
        runBlocking { app.store.updateSettings { it.copy(accessibility = it.accessibility.copy(reducedMotion = true)) } }
        setContent { AppUnderTest(app) }
        waitForText("لنبدأ")
        onNodeWithText("لنبدأ").performClick()
        waitForText("اختر لغة التطبيق")
        onNodeWithText("English").performClick()
        waitForText("Choose the app language")
        assertEquals(LanguageCode.ENGLISH, runBlocking { app.store.currentProfile() }.interfaceLanguage)
    }

    @Test fun onboardingCanBeCompletedAndSavesChoices() = runDesktopComposeUiTest(800, 1700) {
        val app = TestApp()
        runBlocking {
            app.store.updateSettings { it.copy(accessibility = it.accessibility.copy(reducedMotion = true)) }
            app.store.updateProfile { it.copy(interfaceLanguage = LanguageCode.ENGLISH) }
        }
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
        waitForText("Recommended next", substring = false).also { } // Home loads its plan & dashboard
    }

    @Test fun trialOnboardingAsksFiveQuestionsAndOffersOnlyItsThreeLanguages() = runDesktopComposeUiTest(800, 1700) {
        val app = TestApp(edition = com.polyglot.academy.core.service.AppEdition.TRIAL)
        runBlocking {
            app.store.updateSettings { it.copy(accessibility = it.accessibility.copy(reducedMotion = true)) }
            app.store.updateProfile { it.copy(interfaceLanguage = LanguageCode.ENGLISH) }
        }
        setContent { AppUnderTest(app) }
        waitForText("Short lessons, real conversations and smart reviews for learning English, Mandarin or Malay.")
        onNodeWithText("Get started").performClick()
        waitForText("Step 1 of 6")
        onNodeWithText("Continue").performClick() // interface: English
        waitForText("Which language do you want to learn?")
        onNodeWithText("English").assertExists()
        onNodeWithText("Malay").assertExists()
        assertEquals(0, onAllNodesWithText("Spanish").fetchSemanticsNodes().size)
        onNodeWithText("· Preview", substring = true).assertExists() // Malay's mark, before the choice is made
        onNodeWithText("No language's content has been checked by a native speaker yet.", substring = true).assertExists()
        onNodeWithText("Mandarin Chinese").performClick()
        onNodeWithText("Continue").performClick()
        waitForText("Choose my level myself")
        onNodeWithText("Choose my level myself").performClick()
        onNodeWithText("Continue").performClick()
        waitForText("How much time per day?")
        onNodeWithText("Continue").performClick()
        waitForText("Your privacy and AI")
        onNodeWithText("Continue").performClick()
        waitForText("Your plan")
        onNodeWithText("Step 6 of 6").assertExists()
        onNodeWithText("Your first goal: one short lesson today. A longer estimate is under Learn.").assertExists()
        onNodeWithText("What to expect", substring = true).assertExists()
        onNodeWithText("Start learning").performClick()
        waitUntil(timeoutMillis = 10_000) { runBlocking { app.store.currentProfile().onboardingCompleted } }
        val p = runBlocking { app.store.currentProfile() }
        assertEquals(LanguageCode.MANDARIN, p.activeLearningLanguage)
        assertEquals(LanguageCode.ARABIC, p.nativeLanguage)
        assertEquals(listOf(LanguageCode.MANDARIN), p.learningLanguages.map { it.language })
        waitForText("Recommended next", substring = false)
        onNode(hasText("Beginner", substring = true)).assertExists() // the level by name, not "self-selected"
        // The switcher on Home offers the same three languages.
        onNode(androidx.compose.ui.test.hasContentDescription("Switch language")).performClick()
        waitForText("Malay")
        onNodeWithText("English").assertExists()
        assertEquals(0, onAllNodesWithText("Spanish").fetchSemanticsNodes().size)
    }

    @Test fun newLearnerHomeShowsGuidanceInsteadOfStatistics() = runDesktopComposeUiTest(800, 1700) {
        val app = TestApp(); app.profile(level = null)
        setContent { AppUnderTest(app) }
        waitForText("Welcome! Let's find your starting point")
        onNodeWithText("Find your starting level").assertExists()
        assertTrue(onAllNodesWithText("This week").fetchSemanticsNodes().isEmpty(), "no charts for a learner without activity")
        assertTrue(onAllNodesWithText("Daily goal").fetchSemanticsNodes().isEmpty(), "no goals or counters at zero")
        assertTrue(onAllNodesWithText("Daily quests").fetchSemanticsNodes().isEmpty(), "no quests at zero")
    }

    @Test fun homeShowsOneProgressCardAfterTheFirstActivity() = runDesktopComposeUiTest(800, 1700) {
        val app = TestApp(); app.profile(); app.seedActivity()
        setContent { AppUnderTest(app) }
        waitForText("Daily goal")
        assertTrue(onAllNodesWithText("Welcome! Your first lesson is ready").fetchSemanticsNodes().isEmpty())
        assertEquals(1, onAllNodes(hasText("min today", substring = true)).fetchSemanticsNodes().size)
    }

    @Test fun tutorsWithoutAiOfferScenesInsteadOfAnyTopic() = runDesktopComposeUiTest(800, 1700) {
        val app = TestApp(); app.profile()
        setContent { AppUnderTest(app, listOf(Route.TabRoot(Tab.TUTORS))) }
        waitForText("Practise a ready-made conversation")
        assertTrue(onAllNodesWithText("Talk about anything").fetchSemanticsNodes().isEmpty(), "no promise of any topic without AI")
        assertTrue(onAllNodesWithText("Topic (optional)").fetchSemanticsNodes().isEmpty())
        onNodeWithText("Choose a scene").performClick()
        waitForText("Ordering at a café", substring = true)
    }

    @Test fun practiceShowsTheMainPlacesAndSkillDrillsAndTheRestUnderMore() = runDesktopComposeUiTest(800, 1700) {
        val app = TestApp(); app.profile()
        setContent { AppUnderTest(app, listOf(Route.TabRoot(Tab.PRACTICE))) }
        waitForText("Review what you learned")
        assertTrue(onAllNodesWithText("Grammar").fetchSemanticsNodes().isEmpty())
        listOf("Listen to sentences", "Guided conversation", "Write sentences", "Speed training").forEach { onNodeWithText(it).assertExists() }
        onNodeWithText("More practice").performScrollTo().performClick()
        waitForText("Grammar")
    }

    @Test fun sentenceWritingStartsARound() = runDesktopComposeUiTest(800, 1700) {
        val app = TestApp(); app.profile()
        setContent { AppUnderTest(app, listOf(Route.TabRoot(Tab.PRACTICE), Route.SentenceWriting)) }
        waitForText("Start")
        onNodeWithText("Start").performScrollTo().performClick()
        waitForText("Compare with the model")
    }

    @Test fun guidedConversationAcceptsTheModelAnswer() = runDesktopComposeUiTest(800, 1700) {
        val app = TestApp(); app.profile()
        setContent { AppUnderTest(app, listOf(Route.TabRoot(Tab.PRACTICE), Route.GuidedDialogue)) }
        waitForText("Choose a situation")
        // Explanations are in Arabic for this learner, so the situations are listed by their Arabic titles.
        val first = com.polyglot.academy.core.content.Curriculum.of(LanguageCode.ENGLISH).scenarios.minBy { it.level.ordinal }
        onNodeWithText(first.title.ar).performScrollTo().performClick()
        waitForText("Or type your answer")
        onNode(hasSetTextAction()).performTextInput(first.beats.first().modelAnswer)
        onNodeWithText("Check").performScrollTo().performClick()
        waitForText("Understood", substring = true)
    }

    @Test fun settingsKeepKeysAndModelsUnderAdvanced() = runDesktopComposeUiTest(800, 1700) {
        val app = TestApp(); app.profile()
        setContent { AppUnderTest(app, listOf(Route.TabRoot(Tab.ME), Route.Settings)) }
        waitForText("Advanced")
        assertTrue(onAllNodesWithText("AI providers").fetchSemanticsNodes().isEmpty())
        onNodeWithText("Advanced").performClick()
        waitForText("AI providers")
        onNodeWithText("Voice services").assertExists()
        onNodeWithText("On-device models").assertExists()
    }

    private fun tabLabelsFitAtDoubleText(ui: LanguageCode, labels: List<String>) = runDesktopComposeUiTest(720, 1600) {
        val app = TestApp(); app.profile(ui = ui)
        setContent {
            androidx.compose.runtime.CompositionLocalProvider(androidx.compose.ui.platform.LocalDensity provides androidx.compose.ui.unit.Density(2f, 2f)) {
                com.polyglot.academy.ui.app.PolyglotApp(app.services, com.polyglot.academy.ui.app.NoopPlatformUi, listOf(Route.TabRoot(Tab.PRACTICE)))
            }
        }
        labels.forEach { label ->
            waitForText(label)
            // The tab bar is drawn last, so its label is the last node with this text.
            val node = onAllNodesWithText(label).fetchSemanticsNodes().last()
            val layout = mutableListOf<androidx.compose.ui.text.TextLayoutResult>()
            node.config[androidx.compose.ui.semantics.SemanticsActions.GetTextLayoutResult].action!!(layout)
            val r = layout.first()
            val natural = r.multiParagraph.intrinsics.maxIntrinsicWidth
            assertTrue(natural <= r.layoutInput.constraints.maxWidth, "$label needs $natural but has ${r.layoutInput.constraints.maxWidth}")
        }
    }

    @Test fun tabLabelsFitAtDoubleTextInEnglish() = tabLabelsFitAtDoubleText(LanguageCode.ENGLISH, listOf("Home", "Learn", "Practice", "Tutors", "Me"))

    @Test fun tabLabelsFitAtDoubleTextInArabic() = tabLabelsFitAtDoubleText(LanguageCode.ARABIC, listOf("الرئيسية", "تعلّم", "تدرّب", "المعلّمون", "حسابي"))

    @Test fun reviewAsksForTheWordAndChecksTheAnswer() = runDesktopComposeUiTest(800, 1700) {
        val app = TestApp(); app.profile()
        runBlocking { app.services.study.introduceVocabulary(LanguageCode.ENGLISH, listOf("en_hello")) }
        setContent { AppUnderTest(app, listOf(Route.TabRoot(Tab.PRACTICE), Route.Review)) }
        waitForText("How do you say this?")
        onNodeWithText("مرحبًا").assertExists()
        assertTrue(onAllNodesWithText("hello").fetchSemanticsNodes().isEmpty(), "the word stays hidden until answered")
        onNode(hasSetTextAction()).performTextInput("helo")
        onNodeWithText("Check").performClick()
        waitForText("Almost there")
        onNodeWithText("Continue").performClick()
        waitUntil(timeoutMillis = 5_000) { runBlocking { app.store.states(LanguageCode.ENGLISH).first().single().totalReviews == 1 } }
    }

    @Test fun anotherKeyboardIsToldToSwitchAndNotMarkedWrong() = runDesktopComposeUiTest(800, 1700) {
        val app = TestApp(); app.profile()
        runBlocking { app.services.study.introduceVocabulary(LanguageCode.ENGLISH, listOf("en_hello")) }
        setContent { AppUnderTest(app, listOf(Route.TabRoot(Tab.PRACTICE), Route.Review)) }
        waitForText("How do you say this?")
        onNode(hasSetTextAction()).performTextInput("مرحبا")
        onNodeWithText("Check").performClick()
        waitForText("Your keyboard seems to be in another language", substring = true)
        assertTrue(onAllNodesWithText("Not quite").fetchSemanticsNodes().isEmpty())
        assertEquals(0, runBlocking { app.store.states(LanguageCode.ENGLISH).first().single().totalReviews })
    }

    @Test fun lessonRecallCanBeBuiltFromTiles() = runDesktopComposeUiTest(800, 1700) {
        val app = TestApp(); app.profile()
        val lesson = com.polyglot.academy.core.content.Curriculum.of(LanguageCode.ENGLISH).lessonsById.getValue("en_l_greetings")
        val index = lesson.exercises.indexOfFirst { it.id.contains(com.polyglot.academy.core.model.WORD_RECALL_MARK) }
        runBlocking { app.services.study.saveResumePoint(lesson, index) }
        setContent { AppUnderTest(app, listOf(Route.TabRoot(Tab.HOME), Route.LessonPlayer("en_l_greetings"))) }
        waitForText("Build it by tapping")
        onNodeWithText("Build it by tapping").performClick()
        "hello".forEach { letter -> onNodeWithText(letter.toString()).performClick() }
        onNodeWithText("Check").performClick()
        waitForText("Correct!")
    }

    @Test fun theGrammarRuleComesRightBeforeTheExercisesThatUseIt() = runDesktopComposeUiTest(800, 1700) {
        val app = TestApp(); app.profile()
        val lesson = com.polyglot.academy.core.content.Curriculum.of(LanguageCode.ENGLISH).lessonsById.getValue("en_l_greetings")
        val index = lesson.exercises.indexOfFirst { it is com.polyglot.academy.core.model.Exercise.GrammarNote }
        assertTrue(lesson.exercises[index + 1] is com.polyglot.academy.core.model.Exercise.FillInBlank)
        runBlocking { app.services.study.saveResumePoint(lesson, index) }
        setContent { AppUnderTest(app, listOf(Route.TabRoot(Tab.HOME), Route.LessonPlayer("en_l_greetings"))) }
        waitForText("to be", substring = true)
        onNodeWithText("Continue").performClick()
        waitForText("My name _____ Sara.")
        assertTrue(runBlocking { app.store.attempts(LanguageCode.ENGLISH).first() }.isEmpty())
    }

    @Test fun theWordCheckIsSavedApartFromPlacement() = runDesktopComposeUiTest(800, 1700) {
        val app = TestApp(); app.profile()
        setContent { AppUnderTest(app, listOf(Route.TabRoot(Tab.ME), Route.WordCheck(LanguageCode.ENGLISH))) }
        waitForText("Take the word check", timeout = 30_000)
        onNodeWithText("Take the word check").performClick()
        repeat(com.polyglot.academy.core.assessment.WordCheck.SIZE) { i ->
            waitForText("Word ${i + 1} of ${com.polyglot.academy.core.assessment.WordCheck.SIZE}")
            onNodeWithText("I don't know it").performClick()
        }
        waitForText("You recalled 0 of 20 words.")
        val results = runBlocking { app.store.assessments(LanguageCode.ENGLISH).first() }
        assertEquals(com.polyglot.academy.core.model.AssessmentKind.PROGRESS_CHECK, results.single().kind)
    }

    @Test fun thePlacementTestMovesOnAndShowsTheSentenceWithTheBlank() = runDesktopComposeUiTest(800, 1700) {
        val app = TestApp(); app.profile()
        setContent { AppUnderTest(app, listOf(Route.TabRoot(Tab.ME), Route.Assessment(LanguageCode.MANDARIN))) }
        waitForText("Start test", timeout = 30_000)
        onNodeWithText("Start test").performClick()
        waitForText("Question 1 · up to 14")
        // Every A1 question for Mandarin is a sentence with a blank; it must be on screen, not only its instruction.
        assertTrue(onAllNodesWithText("___", substring = true).fetchSemanticsNodes().isNotEmpty(), "the sentence with the blank is shown")
        onNodeWithText("I don't know").performClick()
        waitForText("Question 2 · up to 14")
    }

    @Test fun aNewWordIsShownAndEarnsNoScore() = runDesktopComposeUiTest(800, 1700) {
        val app = TestApp(); app.profile()
        setContent { AppUnderTest(app, listOf(Route.TabRoot(Tab.HOME), Route.LessonPlayer("en_l_greetings", restart = true))) }
        val total = com.polyglot.academy.core.content.Curriculum.of(LanguageCode.ENGLISH).lessonsById.getValue("en_l_greetings").exercises.size
        waitForText("1/$total")
        onNodeWithText("مرحبًا").assertExists() // the meaning is there from the start
        onNodeWithText("Continue").performClick()
        waitForText("2/$total")
        assertTrue(runBlocking { app.store.attempts(LanguageCode.ENGLISH).first() }.isEmpty())
    }

    @Test fun aPassedLessonSaysPractisedNotMastered() = runDesktopComposeUiTest(800, 1700) {
        val app = TestApp(); app.profile()
        val lesson = com.polyglot.academy.core.content.Curriculum.of(LanguageCode.ENGLISH).lessonsById.getValue("en_l_greetings")
        runBlocking { app.services.study.completeLesson(lesson, 0.9) }
        setContent { AppUnderTest(app, listOf(Route.TabRoot(Tab.LEARN))) }
        waitForText("lessons practised", substring = true)
        onAllNodesWithText("Practised", substring = true).fetchSemanticsNodes().let { assertTrue(it.isNotEmpty()) }
        assertTrue(onAllNodesWithText("astered", substring = true).fetchSemanticsNodes().isEmpty())
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

    @Test fun enterChecksATypedAnswerAndThenGoesOn() = runDesktopComposeUiTest(800, 1700) {
        val app = TestApp(); app.profile()
        val lesson = com.polyglot.academy.core.content.Curriculum.of(LanguageCode.ENGLISH).lessonsById.getValue("en_l_greetings")
        val idx = lesson.exercises.indexOfFirst { it is com.polyglot.academy.core.model.Exercise.FillInBlank }
        runBlocking { app.services.study.saveResumePoint(lesson, idx) }
        setContent { AppUnderTest(app, listOf(Route.TabRoot(Tab.HOME), Route.LessonPlayer(lesson.id))) }
        waitUntil(timeoutMillis = 10_000) { onAllNodes(hasSetTextAction()).fetchSemanticsNodes().isNotEmpty() }
        onNode(hasSetTextAction()).performTextInput("are")
        onNode(hasSetTextAction()).performKeyInput { pressKey(androidx.compose.ui.input.key.Key.Enter) }
        waitForText("Not quite")
        // The verdict takes the focus, so the next Enter continues to the next exercise.
        onNodeWithText("Not quite").performKeyInput { pressKey(androidx.compose.ui.input.key.Key.Enter) }
        waitForText("${idx + 2}/${lesson.exercises.size}")
    }

    @Test fun aWordOfAnExampleCanBeTappedForItsMeaning() = runDesktopComposeUiTest(800, 1700) {
        val app = TestApp(); app.profile()
        val list = com.polyglot.academy.core.wordlist.WordLists.of(LanguageCode.ENGLISH)
        val entry = list.level(1).first { it.examples.isNotEmpty() }
        val sentence = entry.examples.first().sentence
        val tapped = com.polyglot.academy.core.stories.StoryText.tokens(sentence, LanguageCode.ENGLISH, list.tapTerms)
            .first { it.isWord && it.text.length > 2 && !it.text.equals(entry.term, ignoreCase = true) && list.lookup(it.text) != null }.text
        val target = list.lookup(tapped)!!
        setContent { AppUnderTest(app, listOf(Route.TabRoot(Tab.PRACTICE), Route.WordDetail(entry.id))) }
        waitForText("Tap a word to see its meaning.")
        onAllNodesWithText(tapped).onFirst().performScrollTo().performClick()
        waitForText("Add to my review")
        onAllNodesWithText(target.meaning.ar).onFirst().assertExists()
        // By its click action: the "no voice for this language" message of the test machine may cover the button.
        onNode(hasText("Add to my review") and androidx.compose.ui.test.hasClickAction())
            .performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.OnClick)
        waitUntil(timeoutMillis = 5_000) { onAllNodesWithText("your review", substring = true).fetchSemanticsNodes().isNotEmpty() }
        waitUntil(timeoutMillis = 5_000) { runBlocking { app.store.state(target.reviewId) != null } }
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

    @Test fun audioIsTranscribedAfterTheModelDownloadIsConfirmed() = runDesktopComposeUiTest(800, 1700) {
        var ready = false
        var downloads = 0
        val fake = object : com.polyglot.academy.core.media.DeviceTranscriber {
            override val languages = setOf(LanguageCode.FRENCH)
            override val modelSizeBytes = 59_707_625L
            override fun isModelReady() = ready
            override suspend fun downloadModel(onProgress: (Float) -> Unit): Result<Unit> { downloads++; onProgress(1f); ready = true; return Result.success(Unit) }
            override fun deleteModel() { ready = false }
            override suspend fun transcribe(source: String, language: LanguageCode, onStage: (com.polyglot.academy.core.media.TranscriptionStage) -> Unit) =
                com.polyglot.academy.core.media.TranscriptionResult.Text("Bonjour à tous.", 3, truncated = false)
        }
        val app = TestApp(transcriber = fake); app.profile(learn = LanguageCode.FRENCH)
        val done = runBlocking {
            app.services.materials.import(LanguageCode.FRENCH, com.polyglot.academy.core.service.ImportInput("Cours", "audio/mpeg", "cours.mp3", 1000, null), false)
                .toList().last() as com.polyglot.academy.core.service.ImportProgress.Done
        }
        com.polyglot.academy.ui.feature.me.PendingMediaFiles.put(done.material.id, "/tmp/cours.mp3")
        setContent { AppUnderTest(app, listOf(Route.TabRoot(Tab.ME), Route.Material(done.material.id))) }
        waitForText("Download speech model (59 MB)")
        onNodeWithText("Download speech model (59 MB)").performScrollTo().performClick()
        waitForText("Download the speech model?")
        assertEquals(0, downloads, "nothing is downloaded before the learner agrees")
        onAllNodesWithText("Download speech model (59 MB)").fetchSemanticsNodes().size.let { assertTrue(it >= 2) }
        onAllNodesWithText("Download speech model (59 MB)")[1].performClick()
        waitForText("Transcribe")
        onNodeWithText("Transcribe").performScrollTo().performClick()
        waitUntil(timeoutMillis = 10_000) { runBlocking { app.store.materials(LanguageCode.FRENCH).first().single().extractedText == "Bonjour à tous." } }
        assertEquals(1, downloads)
        assertTrue(com.polyglot.academy.ui.feature.me.PendingMediaFiles.get(done.material.id) == null, "the private copy is let go")
    }

    @Test fun contentReportsAreSavedOnlyAfterTheLearnerSeesWhatTheFileHolds() = runDesktopComposeUiTest(800, 1700) {
        val app = TestApp(); app.profile()
        runBlocking { app.services.contentReports.report(LanguageCode.ENGLISH, "en_hello", "hello", com.polyglot.academy.core.model.ContentIssue.TRANSLATION, "odd") }
        val saved = mutableListOf<String>()
        val ui = object : com.polyglot.academy.ui.app.PlatformUi by com.polyglot.academy.ui.app.NoopPlatformUi {
            override suspend fun saveExport(fileName: String, content: String): String? { saved += content; return fileName }
        }
        setContent { AppUnderTest(app, listOf(Route.TabRoot(Tab.ME), Route.SettingsPage(SettingsSection.DATA)), ui) }
        waitForText("Export reports for reviewers")
        onNodeWithText("Export reports for reviewers").performScrollTo().performClick()
        waitForText("not your name", substring = true)
        assertTrue(saved.isEmpty(), "nothing is written before the learner agrees")
        onNodeWithText("Save the file").performClick()
        waitUntil(timeoutMillis = 5_000) { saved.size == 1 }
        assertTrue("en_hello" in saved.single() && "odd" in saved.single())
    }

    // Until 2026-11-07 the "delete everything" dialog also opened on top of this one, and its button wiped all data.
    @Test fun deletingContentReportsDeletesOnlyTheReports() = runDesktopComposeUiTest(800, 1700) {
        val app = TestApp(); app.profile()
        runBlocking { app.services.contentReports.report(LanguageCode.ENGLISH, "en_hello", "hello", com.polyglot.academy.core.model.ContentIssue.TRANSLATION, "odd") }
        setContent { AppUnderTest(app, listOf(Route.TabRoot(Tab.ME), Route.SettingsPage(SettingsSection.DATA))) }
        waitForText("Delete all reports")
        onNodeWithText("Delete all reports").performScrollTo().performClick()
        waitForText("Export them first", substring = true)
        // The page button and this dialog's title; the old second dialog added its own title and body (4 in all).
        assertEquals(2, onAllNodesWithText("Delete all reports").fetchSemanticsNodes().size, "one dialog only")
        onNode(hasText("Delete") and androidx.compose.ui.test.hasClickAction()).performClick()
        waitUntil(timeoutMillis = 5_000) { runBlocking { app.services.contentReports.all().isEmpty() } }
        assertEquals("Laila", runBlocking { app.store.profile.first().displayName })
    }

    @Test fun privacyPageIsReachedFromPrivacySettings() = runDesktopComposeUiTest(800, 1700) {
        val app = TestApp(); app.profile(ui = LanguageCode.ARABIC)
        setContent { AppUnderTest(app, listOf(Route.TabRoot(Tab.ME), Route.SettingsPage(SettingsSection.PRIVACY))) }
        waitForText("كيف تُعامَل بياناتك")
        onNodeWithText("كيف تُعامَل بياناتك").performClick()
        waitForText("ما قد يغادر الجهاز ومتى")
        onNodeWithText("مدة الاحتفاظ").performScrollTo()
        onNodeWithText("حذف بياناتك").performScrollTo()
        onNodeWithText("وليست سياسة الخصوصية القانونية", substring = true).performScrollTo()
    }

    @Test fun arabicInterfaceIsLocalized() = runDesktopComposeUiTest(800, 1700) {
        val app = TestApp(); app.profile(ui = LanguageCode.ARABIC)
        setContent { AppUnderTest(app) }
        waitForText("الرئيسية")
        onNodeWithText("أكاديمية بوليغلوت").assertExists()
        // The tab bar appears before the home data is loaded; wait for it rather than racing it.
        waitForText("المقترح التالي")
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

    @Test fun writingPracticeRemembersTheSetAndPosition() = runDesktopComposeUiTest(800, 1700) {
        val app = TestApp(); app.profile(learn = LanguageCode.MANDARIN)
        setContent { AppUnderTest(app, listOf(Route.TabRoot(Tab.PRACTICE), Route.Characters)) }
        waitForText("Character 1 of", substring = true)
        // No missed characters yet, so that set is not offered.
        assertTrue(onAllNodesWithText("To practise again", substring = true).fetchSemanticsNodes().isEmpty())
        onNodeWithText("Level 3").performClick()
        waitUntil(timeoutMillis = 10_000) { runBlocking { app.store.currentSettings().writingPractice.set == 3 } }
        waitForText("Character 1 of", substring = true)
        onNodeWithText("Next").performScrollTo().performClick()
        waitForText("Character 2 of", substring = true)
        waitUntil(timeoutMillis = 10_000) { runBlocking { app.store.currentSettings().writingPractice.position(3, false) == 1 } }
    }

    @Test fun missedCharactersCanBePractisedAgain() = runDesktopComposeUiTest(800, 1700) {
        val app = TestApp(); app.profile(learn = LanguageCode.MANDARIN)
        runBlocking { app.store.updateSettings { it.copy(writingPractice = it.writingPractice.checked('好', right = false).copy(set = -1)) } }
        setContent { AppUnderTest(app, listOf(Route.TabRoot(Tab.PRACTICE), Route.Characters)) }
        waitForText("To practise again (1)")
        waitForText("Character 1 of 1")
        assertTrue(onAllNodesWithText("你好").fetchSemanticsNodes().isNotEmpty(), "the character is shown with a word it appears in")
    }

    @Test fun arabicPhotoIsReadOnTheDeviceFromTheMaterialScreen() = runDesktopComposeUiTest(800, 1700) {
        val ocr = object : com.polyglot.academy.core.service.DeviceOcr {
            override val languages = setOf(LanguageCode.ARABIC)
            override suspend fun recognize(page: com.polyglot.academy.core.service.OcrPage, language: LanguageCode, traditionalChinese: Boolean) = "محطة القطار"
        }
        val app = TestApp(deviceOcr = ocr); app.profile(learn = LanguageCode.ARABIC)
        val photo = byteArrayOf(1, 2, 3)
        val done = runBlocking {
            app.services.materials.import(LanguageCode.ARABIC, com.polyglot.academy.core.service.ImportInput("Sign", "image/jpeg", "sign.jpg", 3, null, photo), false)
                .toList().last() as com.polyglot.academy.core.service.ImportProgress.Done
        }
        com.polyglot.academy.ui.feature.me.PendingOcrFiles.put(done.material.id, "image/jpeg", photo)
        setContent { AppUnderTest(app, listOf(Route.TabRoot(Tab.ME), Route.Material(done.material.id))) }
        waitForText("Read on this device")
        onNodeWithText("Read on this device").performClick()
        waitUntil(timeoutMillis = 10_000) {
            runBlocking { app.store.materials(LanguageCode.ARABIC).first().single().extractedText == "محطة القطار" }
        }
        waitForText("محطة القطار")
        assertTrue(!runBlocking { app.store.materials(LanguageCode.ARABIC).first().single() }.cloudProcessingAllowed)
    }

    @Test fun aLearnerAboveTheCourseIsToldWhatTheLevelsCover() = runDesktopComposeUiTest(800, 1700) {
        val app = TestApp(); app.profile(learn = LanguageCode.SPANISH, level = com.polyglot.academy.core.model.CefrLevel.C1)
        setContent { AppUnderTest(app, listOf(Route.TabRoot(Tab.LEARN))) }
        waitForText("Your level is above the built-in course")
        waitForText("there is no structured course at C1", substring = true)
        onNodeWithText("What each level covers").performScrollTo()
        onNodeWithText("No built-in material at this level").performScrollTo()
        onNodeWithText("no language has a C2 course", substring = true).performScrollTo()
    }

    @Test fun arabicLettersAreWrittenAndCheckedWithTheirDots() = runDesktopComposeUiTest(800, 1700) {
        val app = TestApp(); app.profile(learn = LanguageCode.ARABIC)
        setContent { AppUnderTest(app, listOf(Route.TabRoot(Tab.PRACTICE), Route.Letters)) }
        waitForText("Letter 1 of 28")
        val box = onNode(androidx.compose.ui.test.hasContentDescription("Writing box for", substring = true))
        // Alif: one stroke from top to bottom.
        box.performScrollTo().performTouchInput { swipe(androidx.compose.ui.geometry.Offset(width * 0.5f, height * 0.12f), androidx.compose.ui.geometry.Offset(width * 0.5f, height * 0.85f), 300) }
        onNodeWithText("Check").performScrollTo().performClick()
        waitForText("Well written", substring = true)
        // ب written without its dot: the dot is named, not just a low score.
        onNodeWithText("Next").performScrollTo().performClick()
        waitForText("Letter 2 of 28")
        box.performScrollTo().performTouchInput { swipe(androidx.compose.ui.geometry.Offset(width * 0.85f, height * 0.45f), androidx.compose.ui.geometry.Offset(width * 0.15f, height * 0.6f), 300) }
        onNodeWithText("Check").performScrollTo().performClick()
        waitForText("This letter has 1 dot under it. You wrote none.", substring = true)
        assertEquals(1, runBlocking { app.store.currentSettings() }.letterPositions[LanguageCode.ARABIC])
    }

    @Test fun cameraPhotoIsTurnedCroppedAndImported() = runDesktopComposeUiTest(800, 1700) {
        val ocr = object : com.polyglot.academy.core.service.DeviceOcr {
            override val languages = setOf(LanguageCode.ARABIC)
            override suspend fun recognize(page: com.polyglot.academy.core.service.OcrPage, language: LanguageCode, traditionalChinese: Boolean) = "مخرج"
        }
        val app = TestApp(deviceOcr = ocr); app.profile(learn = LanguageCode.ARABIC)
        val camera = object : com.polyglot.academy.ui.app.PlatformUi by com.polyglot.academy.ui.platform.DesktopPlatformUi() {
            override val canTakePhoto = true
            override suspend fun takePhoto() = PhotoTestImages.jpeg(200, 100)
        }
        setContent { AppUnderTest(app, listOf(Route.TabRoot(Tab.ME), Route.Library), camera) }
        waitForText("Take a photo")
        onNodeWithText("Take a photo").performScrollTo().performClick()
        waitForText("Crop the photo")
        onNodeWithText("Rotate").performScrollTo().performClick()
        // A quarter turn makes the landscape photo portrait.
        waitUntil(timeoutMillis = 10_000) { com.polyglot.academy.ui.feature.me.PendingPhoto.bytes?.let { PhotoTestImages.size(it) } == (100 to 200) }
        onNodeWithText("Use this photo").performScrollTo().performClick()
        waitForText("Read on this device")
        val material = runBlocking { app.store.materials(LanguageCode.ARABIC).first().single() }
        assertEquals(com.polyglot.academy.core.model.MaterialType.IMAGE, material.type)
        assertEquals(100 to 200, PhotoTestImages.size(com.polyglot.academy.ui.feature.me.PendingOcrFiles.get(material.id)!!.second))
        assertTrue(com.polyglot.academy.ui.feature.me.PendingPhoto.bytes == null, "the photo is not kept after import")
    }
}

object PhotoTestImages {
    fun jpeg(width: Int, height: Int): ByteArray {
        val surface = org.jetbrains.skia.Surface.makeRasterN32Premul(width, height)
        surface.canvas.clear(0xFFFFFFFF.toInt())
        surface.canvas.drawRect(org.jetbrains.skia.Rect.makeXYWH(0f, 0f, width / 2f, height / 2f), org.jetbrains.skia.Paint().apply { color = 0xFF000000.toInt() })
        return surface.makeImageSnapshot().encodeToData(org.jetbrains.skia.EncodedImageFormat.JPEG, 92)!!.bytes
    }

    fun size(bytes: ByteArray): Pair<Int, Int> = org.jetbrains.skia.Image.makeFromEncoded(bytes).let { it.width to it.height }
}
