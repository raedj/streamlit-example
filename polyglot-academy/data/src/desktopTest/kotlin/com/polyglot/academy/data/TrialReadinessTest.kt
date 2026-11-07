package com.polyglot.academy.data

import com.polyglot.academy.core.assessment.PlacementProgress
import com.polyglot.academy.core.content.ContentReview
import com.polyglot.academy.core.model.AssessmentKind
import com.polyglot.academy.core.model.AssessmentResult
import com.polyglot.academy.core.model.CefrLevel
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.service.AppServices
import com.polyglot.academy.core.util.FixedTimeSource
import com.polyglot.academy.core.util.SequentialIdGenerator
import io.ktor.client.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.TimeZone
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** What a first trial with real learners needs (council review, 2026-11-05). */
class TrialReadinessTest {
    private val day = 24L * 60 * 60 * 1000
    private val clock = FixedTimeSource(1_793_000_000_000L)
    private val store = SqlStore(createJvmDriver(), Dispatchers.IO, TimeZone.UTC, clock)
    private val app = AppServices(store, FakePlatform(), HttpClient(), clock, SequentialIdGenerator())

    private fun result(at: Long, level: CefrLevel, correct: Int) = AssessmentResult("a$at", LanguageCode.ENGLISH, AssessmentKind.DIAGNOSTIC, at,
        level, level, level, emptyMap(), 20, correct)

    @Test fun theUsageLogIsOffUntilTurnedOnAndTurningItOffDeletesIt() = runBlocking<Unit> {
        app.usage.record("screen:Review")
        assertTrue(app.usage.current().days.isEmpty(), "nothing is counted while the log is off")

        app.usage.setEnabled(true)
        repeat(2) { app.usage.record("screen:Review") }
        app.usage.record("screen:HOME")
        clock.current += day
        app.usage.record("screen:Review")
        // Kept with the settings, so it survives a restart.
        val saved = store.currentSettings().usageLog
        assertEquals(2, saved.days.size)
        assertEquals(2, saved.days.values.first()["screen:Review"])
        assertEquals(1, saved.days.values.first()["screen:HOME"])

        app.usage.setEnabled(false)
        assertFalse(store.currentSettings().usageLog.enabled)
        assertTrue(store.currentSettings().usageLog.days.isEmpty())
    }

    @Test fun theExportHoldsCountsAndPlacementResultsAndNothingTheLearnerWrote() = runBlocking<Unit> {
        app.usage.setEnabled(true)
        app.usage.record("screen:Conversation")
        app.usage.record("screen:My secret sentence!")
        app.usage.record("screen:id-123/x")
        val json = app.usage.exportJson(listOf(result(clock.current, CefrLevel.A2, 12)))
        assertFalse("secret" in json || "123" in json, "anything that is not a plain event name is dropped")
        val root = Json.parseToJsonElement(json).jsonObject
        assertEquals("polyglot-usage-log/1", root["format"]!!.jsonPrimitive.content)
        val today = root["days"]!!.jsonObject.values.single().jsonObject
        assertEquals(1, today.values.single().jsonPrimitive.int)
        assertFalse(today.keys.single().contains(' '))
        val placement = root["placement_results"]!!.jsonArray.single().jsonObject
        assertEquals("A2", placement["level"]!!.jsonPrimitive.content)
        assertEquals(12, placement["correct"]!!.jsonPrimitive.int)
    }

    @Test fun aRetakenPlacementTestShowsTheChangeAndIsSuggestedAfterTwoWeeks() {
        val first = result(clock.current, CefrLevel.A1, 8)
        assertNull(PlacementProgress.change(listOf(first)), "one test is not a change")
        assertFalse(PlacementProgress.retestDue(listOf(first), clock.current + 13 * day))
        assertTrue(PlacementProgress.retestDue(listOf(first), clock.current + 14 * day))
        assertFalse(PlacementProgress.retestDue(emptyList(), clock.current))

        val later = result(clock.current + 15 * day, CefrLevel.A2, 14)
        val ch = PlacementProgress.change(listOf(later, first))!!
        assertEquals(first, ch.first)
        assertEquals(1, ch.levelSteps)
        assertEquals(0.4, ch.firstShareRight, 1e-9)
        assertEquals(0.7, ch.latestShareRight, 1e-9)
    }

    @Test fun unreviewedContentIsLabelledAndTheStarterCoursesAreAPreview() {
        LanguageCode.entries.forEach { assertTrue(ContentReview.isUnreviewed(it), "$it: the review log is empty") }
        assertEquals(setOf(LanguageCode.TURKISH, LanguageCode.URDU, LanguageCode.MALAY), LanguageCode.entries.filter { ContentReview.isPreview(it) }.toSet())
    }
}
