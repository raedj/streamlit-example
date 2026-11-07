package com.polyglot.academy.core.content

import com.polyglot.academy.core.content.generated.GeneratedReviewStatus
import com.polyglot.academy.core.model.LanguageCode

/**
 * Built-in items that the app leaves out because at least two AI models marked them wrong and the owner
 * chose to hide them (`tools/flag_content.py apply`, recorded in content/review/status.tsv as "ai_disputed").
 * Nothing is hidden on one model's word, and nothing is hidden without the owner running that step.
 *
 * Ids are the ones in content/review/items/<lang>.curriculum.tsv and the word lists: vocabulary
 * ("en_hello"), grammar topics ("en_g_be"), hand-written exercises ("en_x_be_1"), placement questions,
 * readings, word-list words ("en_w_the") and their examples ("en_w_the#ex2").
 */
object ContentDisputes {
    const val STATUS = "ai_disputed"

    private val byLanguage: Map<String, Set<String>> by lazy {
        GeneratedReviewStatus.rows.filter { it[2] == STATUS }.groupBy({ it[0] }, { it[1] }).mapValues { it.value.toSet() }
    }

    fun of(language: LanguageCode): Set<String> = byLanguage[language.tag].orEmpty()
}
