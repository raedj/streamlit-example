package com.polyglot.academy.core.learning

import com.polyglot.academy.core.mandarin.isHanzi
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.util.TextNormalizer

/** One word of the sentence (one character in Chinese) and whether the recognizer heard it. */
data class ReadWord(val text: String, val heard: Boolean)

data class ReadAloudResult(val words: List<ReadWord>) {
    val missed: List<String> get() = words.filterNot { it.heard }.map { it.text }
    /** Share of the sentence's words that were heard, 0.0..1.0. */
    val score: Double get() = if (words.isEmpty()) 0.0 else words.count { it.heard }.toDouble() / words.size
}

/**
 * Compares a sentence read aloud with what the speech recognizer heard, word by word, without any AI service.
 * The words are aligned in order (longest common subsequence), so a skipped word is marked even when it
 * appears again later in the sentence. A recognizer can mishear good speech, so this is a guide, not a grade.
 */
object ReadAloud {
    fun compare(expected: String, heard: String, language: LanguageCode): ReadAloudResult {
        val want = units(expected, language)
        val got = units(heard, language).map { it.second }
        if (want.isEmpty()) return ReadAloudResult(emptyList())
        // dp[i][j] = longest common subsequence of want[i..] and got[j..]
        val dp = Array(want.size + 1) { IntArray(got.size + 1) }
        for (i in want.indices.reversed()) for (j in got.indices.reversed()) {
            dp[i][j] = if (same(want[i].second, got[j])) dp[i + 1][j + 1] + 1 else maxOf(dp[i + 1][j], dp[i][j + 1])
        }
        val heardAt = BooleanArray(want.size)
        var i = 0; var j = 0
        while (i < want.size && j < got.size) {
            when {
                same(want[i].second, got[j]) && dp[i][j] == dp[i + 1][j + 1] + 1 -> { heardAt[i] = true; i++; j++ }
                dp[i + 1][j] >= dp[i][j + 1] -> i++
                else -> j++
            }
        }
        return ReadAloudResult(want.mapIndexed { k, (text, _) -> ReadWord(text, heardAt[k]) })
    }

    /** (as written, comparison key) for each word; each Chinese character is its own unit. */
    private fun units(text: String, language: LanguageCode): List<Pair<String, String>> =
        if (language == LanguageCode.MANDARIN) text.filter(::isHanzi).map { it.toString() to it.toString() }
        else text.split(Regex("\\s+")).mapNotNull { raw ->
            val key = TextNormalizer.loose(raw).replace(" ", "")
            if (key.isEmpty()) null else raw.trim { !it.isLetterOrDigit() } to key
        }

    /** Equal, or one letter apart in a word of five letters or more (recognizers spell some words differently). */
    private fun same(a: String, b: String): Boolean =
        a == b || (a.length >= 5 && b.length >= 5 && TextNormalizer.levenshtein(a, b) <= 1)
}
