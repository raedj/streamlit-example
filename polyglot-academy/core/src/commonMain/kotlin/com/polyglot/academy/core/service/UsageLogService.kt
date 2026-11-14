package com.polyglot.academy.core.service

import com.polyglot.academy.core.model.AssessmentResult
import com.polyglot.academy.core.model.UsageLog
import com.polyglot.academy.core.repo.ProfileRepository
import com.polyglot.academy.core.util.TimeSource
import com.polyglot.academy.core.util.toLocalDate
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import kotlinx.serialization.json.addJsonObject

/**
 * An opt-in record for a trial with testers: how many times each screen was opened, per day. The app
 * has no server and no analytics, so this is the only way to see what testers use; it stays on the
 * device, is off by default, holds no text the learner wrote or read, and goes nowhere unless the
 * tester exports the file and sends it themselves. Turning it off deletes it.
 */
class UsageLogService(private val settings: ProfileRepository, private val clock: TimeSource) {
    suspend fun current(): UsageLog = settings.currentSettings().usageLog

    suspend fun setEnabled(on: Boolean) = settings.updateSettings {
        it.copy(usageLog = if (on) UsageLog(enabled = true, startedAtMillis = clock.nowMillis()) else UsageLog())
    }

    /** Counts one event ("screen:Review", "app_open") for today; nothing happens while the log is off. */
    suspend fun record(event: String) {
        if (!current().enabled) return
        // Only names like "screen:Review": anything else (a sentence, an id) is not stored at all.
        val name = event.takeIf { it.length <= MAX_EVENT && EVENT.matches(it) } ?: return
        val day = clock.nowMillis().toLocalDate().toString()
        settings.updateSettings { s ->
            if (!s.usageLog.enabled) return@updateSettings s
            val today = s.usageLog.days[day].orEmpty()
            val days = (s.usageLog.days + (day to (today + (name to (today[name] ?: 0) + 1)))).entries.sortedBy { it.key }.takeLast(MAX_DAYS)
            s.copy(usageLog = s.usageLog.copy(days = days.associate { it.key to it.value }))
        }
    }

    /**
     * The file a tester sends: event counts per day and the placement results (level and share right),
     * which show whether the learner moved between the first and the last test. No names, no content.
     */
    suspend fun exportJson(assessments: List<AssessmentResult>): String {
        val log = current()
        val json = buildJsonObject {
            put("format", "polyglot-usage-log/1")
            put("exported_at_millis", clock.nowMillis())
            log.startedAtMillis?.let { put("started_at_millis", it) }
            putJsonObject("days") {
                log.days.entries.sortedBy { it.key }.forEach { (day, counts) -> putJsonObject(day) { counts.entries.sortedBy { it.key }.forEach { (k, v) -> put(k, v) } } }
            }
            putJsonArray("placement_results") {
                assessments.sortedBy { it.takenAtMillis }.forEach { a ->
                    addJsonObject {
                        put("language", a.language.tag)
                        put("taken_on", a.takenAtMillis.toLocalDate().toString())
                        put("level", a.estimatedLevel.label)
                        put("range", "${a.levelLow.label}-${a.levelHigh.label}")
                        put("answered", a.questionsAnswered)
                        put("correct", a.correctAnswers)
                    }
                }
            }
        }
        return Json { prettyPrint = true }.encodeToString(kotlinx.serialization.json.JsonObject.serializer(), json)
    }

    companion object {
        const val MAX_DAYS = 120
        const val MAX_EVENT = 60
        private val EVENT = Regex("^[a-z_]+(:[A-Za-z0-9_]+)?$")
    }
}
