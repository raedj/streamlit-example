package com.polyglot.academy.core.mandarin

/** A pinyin syllable split into its toneless base and tone (1-4, 5 = neutral). */
data class PinyinSyllable(val base: String, val tone: Int) {
    val marked: String get() = Pinyin.applyTone(base, tone)
    val numbered: String get() = "$base$tone"
}

object Pinyin {
    private val marks = mapOf(
        'a' to "āáǎà", 'e' to "ēéěè", 'i' to "īíǐì", 'o' to "ōóǒò", 'u' to "ūúǔù", 'ü' to "ǖǘǚǜ",
    )
    private val reverse: Map<Char, Pair<Char, Int>> = buildMap {
        marks.forEach { (base, s) -> s.forEachIndexed { i, c -> put(c, base to (i + 1)) } }
    }

    val toneNames = mapOf(1 to "high level", 2 to "rising", 3 to "dipping", 4 to "falling", 5 to "neutral")
    val toneNamesAr = mapOf(1 to "مستوية عالية", 2 to "صاعدة", 3 to "هابطة صاعدة", 4 to "هابطة", 5 to "محايدة")

    /** Puts the tone mark on the correct vowel following standard pinyin rules. */
    fun applyTone(base: String, tone: Int): String {
        val s = base.replace("u:", "ü").replace("v", "ü")
        if (tone !in 1..4) return s
        val lower = s.lowercase()
        val idx = when {
            'a' in lower -> lower.indexOf('a')
            'e' in lower -> lower.indexOf('e')
            "ou" in lower -> lower.indexOf('o')
            else -> lower.indexOfLast { it in "iouü" }
        }
        if (idx < 0) return s
        val ch = lower[idx]
        val marked = marks[ch]?.get(tone - 1) ?: return s
        val out = if (s[idx].isUpperCase()) marked.uppercaseChar() else marked
        return s.substring(0, idx) + out + s.substring(idx + 1)
    }

    /** "ni3 hao3" -> "nǐ hǎo". Unnumbered syllables are kept as-is. */
    fun numberedToMarked(text: String): String = text.split(' ').joinToString(" ") { word ->
        Regex("([A-Za-züÜv:]+)([1-5])").replace(word) { m -> applyTone(m.groupValues[1], m.groupValues[2].toInt()) }
    }

    /** Parses one marked syllable ("hǎo") into base and tone. */
    fun parseSyllable(syllable: String): PinyinSyllable {
        var tone = 5
        val sb = StringBuilder()
        for (c in syllable) {
            val r = reverse[c] ?: reverse[c.lowercaseChar()]
            if (r != null) { tone = r.second; sb.append(if (c.isUpperCase()) r.first.uppercaseChar() else r.first) } else sb.append(c)
        }
        return PinyinSyllable(sb.toString(), tone)
    }

    private val initials = listOf("zh", "ch", "sh", "b", "p", "m", "f", "d", "t", "n", "l", "g", "k", "h", "j", "q", "x", "r", "z", "c", "s", "y", "w")

    /**
     * Splits a marked pinyin word such as "xièxie" or "Zhōngguó" into syllables. Uses the
     * tone-mark positions plus a greedy initial/final scan; adequate for curriculum words.
     */
    fun syllables(word: String): List<PinyinSyllable> {
        val cleaned = word.replace("'", " ").replace("’", " ").replace("-", " ")
        return cleaned.split(' ').filter { it.isNotBlank() }.flatMap { splitWord(it) }.map { parseSyllable(it) }
    }

    private fun isVowel(c: Char): Boolean {
        val b = reverse[c.lowercaseChar()]?.first ?: c.lowercaseChar()
        return b in "aeiouü"
    }

    private fun splitWord(w: String): List<String> {
        val out = mutableListOf<String>()
        var i = 0
        val lower = w.lowercase()
        while (i < w.length) {
            var j = i
            val ini = initials.firstOrNull { lower.startsWith(it, i) }
            if (ini != null) j += ini.length
            while (j < w.length && isVowel(w[j])) j++
            // Optional nasal final: n / ng, only if not the initial of the next syllable.
            if (j < w.length && lower[j] == 'n') {
                val ng = j + 1 < w.length && lower[j + 1] == 'g'
                val afterNg = j + 2
                if (ng && (afterNg >= w.length || !isVowel(w[afterNg]))) j += 2
                else if (!ng && (j + 1 >= w.length || !isVowel(w[j + 1]))) j += 1
            } else if (j < w.length && lower[j] == 'r' && (j + 1 >= w.length || !isVowel(w[j + 1])) && j > i) {
                j += 1 // erhua
            }
            if (j == i) j++ // safety: consume one char
            out += w.substring(i, j)
            i = j
        }
        return out
    }

    fun tones(word: String): List<Int> = syllables(word).map { it.tone }

    /** Third-tone sandhi: a 3rd tone before another 3rd tone is pronounced as a 2nd tone. */
    fun spokenTones(word: String): List<Int> {
        val t = tones(word).toMutableList()
        for (i in 0 until t.size - 1) if (t[i] == 3 && t[i + 1] == 3) t[i] = 2
        return t
    }
}

/** Tone-pair category for two-syllable drills, e.g. "3-4". */
data class TonePair(val first: Int, val second: Int) {
    override fun toString() = "$first-$second"
    companion object {
        val all: List<TonePair> = (1..4).flatMap { a -> (1..5).map { b -> TonePair(a, b) } }
    }
}

fun isHanzi(c: Char): Boolean = c.code in 0x4E00..0x9FFF || c.code in 0x3400..0x4DBF
fun containsHanzi(s: String) = s.any(::isHanzi)
