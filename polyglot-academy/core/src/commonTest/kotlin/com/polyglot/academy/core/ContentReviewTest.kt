package com.polyglot.academy.core

import com.polyglot.academy.core.content.ContentReview
import com.polyglot.academy.core.content.generated.GeneratedReviewStatus
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.wordlist.WordLists
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ContentReviewTest {
    @Test fun everyReviewRecordPointsAtARealItem() {
        // A typo in content/review/status.tsv must not mark the wrong word (or nothing) as reviewed.
        val unknown = GeneratedReviewStatus.rows.filter { (lang, id) ->
            val code = LanguageCode.fromTag(lang) ?: return@filter true
            val list = WordLists.of(code)
            val wordId = id.substringBefore("#ex")
            val entry = list.byId[wordId] ?: return@filter true
            val ex = id.substringAfter("#ex", "").toIntOrNull()
            ex != null && ex !in 1..entry.examples.size
        }
        assertEquals(emptyList(), unknown)
    }

    @Test fun unlistedItemsAreUnreviewed() {
        assertNull(ContentReview.of(LanguageCode.MANDARIN, "zh_w_de"))
        // Nothing has been reviewed by a person yet; this changes only through tools/review_sheets.py.
        assertEquals(GeneratedReviewStatus.rows.count { it[0] == "zh" && it[2] == "reviewed" }, ContentReview.reviewedCount(LanguageCode.MANDARIN))
    }
}
