package com.polyglot.academy.core.service

import com.polyglot.academy.core.model.ContentIssue
import com.polyglot.academy.core.model.ContentReport
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.repo.ProfileRepository
import com.polyglot.academy.core.util.IdGenerator
import com.polyglot.academy.core.util.TimeSource

/**
 * Reports of errors in built-in content (a wrong translation, a doubtful example, two right answers).
 * They stay on the device; the learner can export them as a table for the content reviewers. Nothing
 * is sent anywhere by the app.
 */
class ContentReportService(private val settings: ProfileRepository, private val clock: TimeSource, private val ids: IdGenerator) {
    suspend fun report(language: LanguageCode, itemId: String, itemText: String, issue: ContentIssue, note: String): ContentReport {
        val r = ContentReport(ids.newId("rep"), language, itemId, itemText.take(MAX_TEXT), issue, note.trim().take(MAX_NOTE), clock.nowMillis())
        settings.updateSettings { it.copy(contentReports = (listOf(r) + it.contentReports).take(MAX_REPORTS)) }
        return r
    }

    suspend fun all(): List<ContentReport> = settings.currentSettings().contentReports

    suspend fun delete(id: String) = settings.updateSettings { s -> s.copy(contentReports = s.contentReports.filterNot { it.id == id }) }

    suspend fun clear() = settings.updateSettings { it.copy(contentReports = emptyList()) }

    /** Tab-separated table for reviewers: one report per line, tabs and line breaks in text replaced by spaces. */
    suspend fun exportTsv(): String = buildString {
        appendLine(listOf("created_at_millis", "language", "item_id", "item_text", "issue", "note").joinToString("\t"))
        all().forEach { r ->
            appendLine(listOf(r.createdAtMillis.toString(), r.language.tag, r.itemId, r.itemText, r.issue.name, r.note).joinToString("\t") { clean(it) })
        }
    }

    private fun clean(s: String) = s.replace(Regex("[\t\r\n]+"), " ")

    companion object {
        const val MAX_REPORTS = 500
        const val MAX_NOTE = 1000
        const val MAX_TEXT = 300
    }
}
