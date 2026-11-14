package com.polyglot.academy.core.content

import com.polyglot.academy.core.content.generated.GeneratedReviewStatus
import com.polyglot.academy.core.model.LanguageCode

enum class ContentReviewStatus { REVIEWED, NEEDS_FIX }

data class ContentReviewRecord(val status: ContentReviewStatus, val reviewer: String, val date: String)

/**
 * Which built-in items a qualified speaker has reviewed (content/review/status.tsv, maintained with
 * tools/review_sheets.py). Anything not listed is AI-written and unreviewed, and the app says so.
 */
object ContentReview {
    private val records: Map<Pair<String, String>, ContentReviewRecord> by lazy {
        // Items hidden after the AI check are not a person's review: see ContentDisputes.
        GeneratedReviewStatus.rows.filter { it[2] != ContentDisputes.STATUS }.associate { (lang, id, status, reviewer, date) ->
            (lang to id) to ContentReviewRecord(if (status == "reviewed") ContentReviewStatus.REVIEWED else ContentReviewStatus.NEEDS_FIX, reviewer, date)
        }
    }

    fun of(language: LanguageCode, itemId: String): ContentReviewRecord? = records[language.tag to itemId]

    // Starter courses written entirely by AI after the others (2026-10-25): shown as a preview, not a
    // course, until a native speaker has checked some of them.
    private val starterCourses = setOf(LanguageCode.TURKISH, LanguageCode.URDU, LanguageCode.MALAY)

    /** No item of this language has been checked by a native speaker yet. */
    fun isUnreviewed(language: LanguageCode): Boolean = reviewedCount(language) == 0

    /** An AI-written starter course that nobody has reviewed: offered, but labelled as a preview. */
    fun isPreview(language: LanguageCode): Boolean = language in starterCourses && isUnreviewed(language)

    /** Number of items reviewed and marked right, for the language (0 until the first review). */
    fun reviewedCount(language: LanguageCode): Int = records.count { (k, v) -> k.first == language.tag && v.status == ContentReviewStatus.REVIEWED }
}
