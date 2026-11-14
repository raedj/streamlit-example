package com.polyglot.academy.core.util

/**
 * Normalisation used when comparing typed or recognised answers. Works without platform
 * Unicode normalisers so the same rules apply on every target.
 */
object TextNormalizer {
    private val latinFolds: Map<Char, String> = buildMap {
        "áàâäãåā".forEach { put(it, "a") }
        "éèêëē".forEach { put(it, "e") }
        "íìîïī".forEach { put(it, "i") }
        "óòôöõøō".forEach { put(it, "o") }
        "úùûüū".forEach { put(it, "u") }
        put('ñ', "n"); put('ç', "c"); put('ÿ', "y"); put('ý', "y")
        put('œ', "oe"); put('æ', "ae"); put('ß', "ss")
        // Pinyin tone vowels
        "ǎ".forEach { put(it, "a") }; "ě".forEach { put(it, "e") }; "ǐ".forEach { put(it, "i") }
        "ǒ".forEach { put(it, "o") }; "ǔǖǘǚǜ".forEach { put(it, "u") }
    }

    private fun isArabicDiacritic(c: Char) = c in 'ً'..'ْ' || c == 'ٰ' || c == 'ـ'

    private val punctuation = setOf(
        '.', ',', '!', '?', ';', ':', '"', '\'', '(', ')', '[', ']', '«', '»', '¿', '¡', '-', '–', '—', '…',
        '。', '，', '？', '！', '；', '：', '、', '“', '”', '‘', '’', '「', '」', '《', '》', '،', '؛', '؟',
    )

    /** Lower-case, unify Arabic letter variants, remove punctuation and collapse whitespace. Keeps accents. */
    fun basic(input: String): String {
        val sb = StringBuilder()
        for (c in input.lowercase()) {
            when {
                c in punctuation -> sb.append(' ')
                c == '’' -> sb.append('\'')
                isArabicDiacritic(c) -> {}
                c == 'أ' || c == 'إ' || c == 'آ' || c == 'ٱ' -> sb.append('ا')
                c == 'ى' -> sb.append('ي')
                else -> sb.append(c)
            }
        }
        return sb.toString().split(Regex("\\s+")).filter { it.isNotEmpty() }.joinToString(" ")
    }

    /** [basic] plus removal of Latin accents and pinyin tone marks. */
    fun loose(input: String): String {
        val b = basic(input)
        val sb = StringBuilder()
        for (c in b) sb.append(latinFolds[c] ?: c.toString())
        return sb.toString()
    }

    fun levenshtein(a: String, b: String): Int {
        if (a == b) return 0
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length
        var prev = IntArray(b.length + 1) { it }
        var cur = IntArray(b.length + 1)
        for (i in 1..a.length) {
            cur[0] = i
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                cur[j] = minOf(cur[j - 1] + 1, prev[j] + 1, prev[j - 1] + cost)
            }
            val t = prev; prev = cur; cur = t
        }
        return prev[b.length]
    }

    /** 1.0 = identical, 0.0 = completely different, on loose-normalised text. */
    fun similarity(a: String, b: String): Double {
        val x = loose(a); val y = loose(b)
        val max = maxOf(x.length, y.length)
        if (max == 0) return 1.0
        return 1.0 - levenshtein(x, y).toDouble() / max
    }
}
