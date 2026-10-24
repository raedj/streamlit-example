package com.polyglot.academy.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.polyglot.academy.core.content.Curriculum
import com.polyglot.academy.core.model.CefrLevel
import com.polyglot.academy.core.model.Conversation
import com.polyglot.academy.core.model.ConversationTurn
import com.polyglot.academy.core.model.Correction
import com.polyglot.academy.core.model.ErrorSeverity
import com.polyglot.academy.core.model.Exercise
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.LanguageEnrollment
import com.polyglot.academy.core.model.LearningGoal
import com.polyglot.academy.core.model.RecallRating
import com.polyglot.academy.core.model.SessionKind
import com.polyglot.academy.core.model.Speaker
import com.polyglot.academy.core.model.ThemeMode
import com.polyglot.academy.core.service.AppServices
import com.polyglot.academy.core.service.ExerciseOutcome
import com.polyglot.academy.core.util.FixedTimeSource
import com.polyglot.academy.core.util.SequentialIdGenerator
import com.polyglot.academy.data.SqlStore
import com.polyglot.academy.data.createJvmDriver
import com.polyglot.academy.ui.app.NoopPlatformUi
import com.polyglot.academy.ui.app.PolyglotApp
import com.polyglot.academy.ui.navigation.Route
import com.polyglot.academy.ui.platform.DesktopPlatformServices
import io.ktor.client.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.TimeZone

/** 2026-10-24 09:00 UTC — a fixed "now" so screenshots and tests are reproducible. */
const val TEST_NOW = 1_792_832_400_000L

class TestApp(val clock: FixedTimeSource = FixedTimeSource(TEST_NOW)) {
    val platform = DesktopPlatformServices()
    val store = SqlStore(createJvmDriver(), Dispatchers.IO, TimeZone.UTC, clock)
    val services = AppServices(store, platform, HttpClient(), clock, SequentialIdGenerator())

    fun profile(
        ui: LanguageCode = LanguageCode.ENGLISH, learn: LanguageCode = LanguageCode.ENGLISH, level: CefrLevel? = CefrLevel.A1,
        theme: ThemeMode = ThemeMode.LIGHT, name: String = "Laila",
    ) = runBlocking {
        store.updateProfile {
            it.copy(
                displayName = name, interfaceLanguage = ui, nativeLanguage = if (learn == LanguageCode.ARABIC) LanguageCode.ENGLISH else LanguageCode.ARABIC,
                explanationLanguage = if (ui == LanguageCode.ARABIC) LanguageCode.ARABIC else if (learn == LanguageCode.ARABIC) LanguageCode.ENGLISH else LanguageCode.ARABIC,
                activeLearningLanguage = learn, onboardingCompleted = true, goals = setOf(LearningGoal.TRAVEL, LearningGoal.WORK), dailyMinutes = 15,
                learningLanguages = if (level != null) listOf(LanguageEnrollment(learn, com.polyglot.academy.core.model.Languages.of(learn).defaultVariantId, level, true)) else emptyList(),
                preferredTutorId = com.polyglot.academy.core.tutor.TutorRoster.defaultFor(learn, level ?: CefrLevel.A1).id,
            )
        }
        // Reduced motion keeps tests deterministic (no infinite animations).
        store.updateSettings { it.copy(theme = theme, accessibility = it.accessibility.copy(reducedMotion = true)) }
    }

    /**
     * Seeds realistic but synthetic activity over the past week so populated screens can be
     * verified. Used only by tests and screenshot generation; never shipped in the app.
     */
    fun seedActivity(lang: LanguageCode = LanguageCode.ENGLISH) = runBlocking {
        val cur = Curriculum.of(lang)
        val lessons = cur.sequencer.orderedLessons()
        clock.current = TEST_NOW - 6 * 86_400_000L
        lessons.take(4).forEachIndexed { day, lesson ->
            val session = services.study.startSession(lang, SessionKind.LESSON, lesson.id)
            lesson.exercises.forEachIndexed { i, ex ->
                val correct = (i + day) % 4 != 0
                services.study.recordExercise(lang, lesson.id, ex, ExerciseOutcome(correct, if (correct) 1.0 else 0.0, "answer"), if (correct) "answer" else "wrong", 6000, 0, (ex as? Exercise.FillInBlank)?.sentence ?: ex.id)
            }
            services.study.completeLesson(lesson, if (day == 2) 0.75 else 0.9)
            clock.advanceMinutes(10 + day * 3L)
            services.study.updateSession(session, lesson.exercises.size, lesson.exercises.size - 2, true, (10 + day * 3) * 60)
            // Review the next day.
            clock.advanceDays(1.0)
            services.reviews.dueQueue(lang).take(6).forEachIndexed { k, card -> services.reviews.rate(card, if (k % 3 == 0) RecallRating.HARD else RecallRating.GOOD, 4000) }
            val rs = services.study.startSession(lang, SessionKind.REVIEW, null)
            clock.advanceMinutes(5)
            services.study.updateSession(rs, 6, 5, true, 300)
        }
        clock.current = TEST_NOW
        store.upsertConversation(Conversation("conv_seed", lang, "emma", "en_s_cafe", TEST_NOW - 3_600_000))
        store.addTurn(ConversationTurn("t1", "conv_seed", Speaker.TUTOR, "Hi there! What can I get you today?", "أهلًا! ماذا أحضر لك اليوم؟", createdAtMillis = TEST_NOW - 3_590_000, producedBy = "offline"))
        store.addTurn(ConversationTurn("t2", "conv_seed", Speaker.LEARNER, "I want a coffee", corrections = listOf(Correction("I want a coffee", "I'd like a coffee, please.", "أكثر تهذيبًا", ErrorSeverity.MINOR, true)), createdAtMillis = TEST_NOW - 3_580_000))
    }
}

/** Phone-sized content at a realistic density for screenshots. */
@Composable
fun PhoneFrame(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalDensity provides Density(2f, 1f)) { content() }
}

@Composable
fun AppUnderTest(app: TestApp, stack: List<Route>? = null) {
    PhoneFrame { PolyglotApp(app.services, NoopPlatformUi, stack) }
}
