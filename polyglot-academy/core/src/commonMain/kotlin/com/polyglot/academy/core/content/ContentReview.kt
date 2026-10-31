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
        GeneratedReviewStatus.rows.associate { (lang, id, status, reviewer, date) ->
            (lang to id) to ContentReviewRecord(if (status == "reviewed") ContentReviewStatus.REVIEWED else ContentReviewStatus.NEEDS_FIX, reviewer, date)
        }
    }

    fun of(language: LanguageCode, itemId: String): ContentReviewRecord? = records[language.tag to itemId]

    /** Number of items reviewed and marked right, for the language (0 until the first review). */
    fun reviewedCount(language: LanguageCode): Int = records.count { (k, v) -> k.first == language.tag && v.status == ContentReviewStatus.REVIEWED }
}
