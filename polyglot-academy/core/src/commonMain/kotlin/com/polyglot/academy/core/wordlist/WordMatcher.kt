package com.polyglot.academy.core.wordlist

import com.polyglot.academy.core.model.LanguageCode

/**
 * Finds list words in sentences, per language: normalisation (case, Turkish i/ı, Arabic and Urdu
 * vowel marks), tokenisation (French elision, Chinese longest match) and light inflection rules
 * (English suffixes, Spanish/French plurals and feminine forms, Arabic clitics, Turkish suffixes
 * by stem, Malay affixes). Irregular forms come from each entry's forms list. The rules are
 * deliberately simple: they are used to validate examples and to locate a word for exercises,
 * not to analyse arbitrary text.
 */
class WordMatcher(private val language: LanguageCode, entries: List<WordEntry>) {
    /** Lowest rank of any entry that has this normalised single-word key. */
    private val keyRank = HashMap<String, Int>()
    private val singleKeys = HashMap<String, Set<String>>()
    private val phrases = HashMap<String, List<List<String>>>()
    private val zhDictionary = HashMap<String, Int>()
    private val zhMaxLength: Int

    init {
        for (e in entries) {
            val variants = (listOf(e.term) + e.forms + stems(e)).map { normalize(it) }.filter { it.isNotBlank() }
            if (language == LanguageCode.MANDARIN) {
                variants.forEach { v -> zhDictionary[v] = minOf(zhDictionary[v] ?: Int.MAX_VALUE, e.rank) }
                continue
            }
            val singles = mutableSetOf<String>()
            val multi = mutableListOf<List<String>>()
            for (v in variants) {
                val toks = rawTokens(v)
                if (toks.size == 1) singles += toks[0] else if (toks.size > 1) multi += toks
                // Every word of a multi-word term counts as known from this rank on.
                toks.forEach { t -> keyRank[t] = minOf(keyRank[t] ?: Int.MAX_VALUE, e.rank) }
            }
            singleKeys[e.id] = singles
            phrases[e.id] = multi
        }
        zhMaxLength = zhDictionary.keys.maxOfOrNull { it.length } ?: 1
    }

    fun normalize(text: String): String {
        var t = text.replace('’', '\'').replace('‘', '\'')
        t = if (language == LanguageCode.TURKISH) t.replace('I', 'ı').replace('İ', 'i').lowercase() else t.lowercase()
        if (language == LanguageCode.ARABIC || language == LanguageCode.URDU) t = t.replace(Regex("[\\u064B-\\u065F\\u0670\\u0640]"), "")
        if (language == LanguageCode.ARABIC) t = t.replace(Regex("[أإآٱ]"), "ا").replace('ى', 'ي')
        return t
    }

    /** Word tokens of a sentence with proper names (and a following Turkish suffix such as 'da) removed. */
    fun tokens(sentence: String, names: List<String> = emptyList()): List<String> {
        if (language == LanguageCode.MANDARIN) return segment(sentence, names).map { it.first }
        return rawTokens(stripNames(normalize(sentence), names))
    }

    /** True when the example contains the entry's lemma or one of its registered forms. */
    fun contains(entry: WordEntry, sentence: String, names: List<String> = emptyList()): Boolean {
        if (language == LanguageCode.MANDARIN) {
            val s = stripNames(normalize(sentence), names)
            return (listOf(entry.term) + entry.forms).any { normalize(it) in s }
        }
        val toks = tokens(sentence, names)
        val keys = singleKeys[entry.id].orEmpty()
        if (keys.isNotEmpty() && toks.any { tok -> candidates(tok).any { it in keys } || prefixMatch(tok, keys) }) return true
        return phrases[entry.id].orEmpty().any { p -> containsSequence(toks, p) }
    }

    /** Rank of the most frequent entry this token belongs to, or null if the list does not know it. */
    fun rankOf(token: String): Int? {
        if (language == LanguageCode.MANDARIN) return zhDictionary[token]
        candidates(token).mapNotNull { keyRank[it] }.minOrNull()?.let { return it }
        if (language == LanguageCode.TURKISH) {
            for (len in token.length - 1 downTo 2) keyRank[token.substring(0, len)]?.let { return it }
        }
        return null
    }

    /** Words of the example that are unknown or less frequent than rank + slack (for messages and checks). */
    fun outOfRange(entry: WordEntry, example: WordExample): List<String> {
        val limit = entry.rank + WordLevels.EXAMPLE_RANK_SLACK
        return tokens(example.sentence, example.names).filter { (rankOf(it) ?: Int.MAX_VALUE) > limit }
    }

    /** Share of the example's words within rank + slack; 1.0 for an example without countable words. */
    fun coverage(entry: WordEntry, example: WordExample): Double {
        val toks = tokens(example.sentence, example.names)
        if (toks.isEmpty()) return 1.0
        return 1.0 - outOfRange(entry, example).size.toDouble() / toks.size
    }

    private fun stems(e: WordEntry): List<String> = buildList {
        e.extra["stem"]?.let { add(it) }
        if (language == LanguageCode.TURKISH && (e.term.endsWith("mek") || e.term.endsWith("mak")) && e.term.length > 4) add(e.term.dropLast(3))
    }

    private fun rawTokens(text: String): List<String> = when (language) {
        LanguageCode.ENGLISH -> Regex("[\\p{L}\\p{M}']+").findAll(text).map { it.value.trim('\'') }.filter { it.isNotEmpty() }.toList()
        LanguageCode.FRENCH -> Regex("[\\p{L}\\p{M}]+'?").findAll(text).map { it.value }.toList()
        else -> Regex("[\\p{L}\\p{M}]+").findAll(text).map { it.value }.toList()
    }

    private fun stripNames(normalizedSentence: String, names: List<String>): String {
        var s = normalizedSentence
        for (n in names.map { normalize(it) }.filter { it.isNotBlank() }.sortedByDescending { it.length }) {
            // Whole words only: removing "Ana" must not cut it out of "hermana". Chinese has no spaces, so a name
            // (王 in 王先生) is removed wherever it occurs.
            s = if (language == LanguageCode.MANDARIN) s.replace(n, " ")
            else s.replace(Regex("(?<![\\p{L}\\p{M}])" + Regex.escape(n) + "(['’][\\p{L}]+)?(?![\\p{L}\\p{M}])"), " ")
        }
        return s
    }

    private fun containsSequence(toks: List<String>, phrase: List<String>): Boolean =
        toks.windowed(phrase.size).any { w -> w.indices.all { i -> candidates(w[i]).contains(phrase[i]) || prefixMatch(w[i], setOf(phrase[i])) } }

    /** Turkish is agglutinative: a key of two or more letters matches the start of a longer word (ev → evde). */
    private fun prefixMatch(token: String, keys: Set<String>): Boolean =
        language == LanguageCode.TURKISH && keys.any { it.length >= 2 && token.length > it.length && token.startsWith(it) }

    /** The token plus the forms it may come from under the language's regular inflection rules. */
    fun candidates(token: String): List<String> = buildList {
        add(token)
        val t = token
        when (language) {
            LanguageCode.ENGLISH -> {
                if (t.endsWith("'s")) add(t.dropLast(2))
                if (t.endsWith("ies") && t.length > 4) add(t.dropLast(3) + "y")
                if (t.endsWith("es") && t.length > 3) add(t.dropLast(2))
                if (t.endsWith("s") && t.length > 2) add(t.dropLast(1))
                if (t.endsWith("ied") && t.length > 4) add(t.dropLast(3) + "y")
                if (t.endsWith("ed") && t.length > 3) { add(t.dropLast(2)); add(t.dropLast(1)); if (t.length > 4 && t[t.length - 3] == t[t.length - 4]) add(t.dropLast(3)) }
                if (t.endsWith("ing") && t.length > 4) { add(t.dropLast(3)); add(t.dropLast(3) + "e"); if (t.length > 5 && t[t.length - 4] == t[t.length - 5]) add(t.dropLast(4)) }
                if (t.endsWith("er") && t.length > 4) { add(t.dropLast(2)); add(t.dropLast(1)) }
                if (t.endsWith("est") && t.length > 5) { add(t.dropLast(3)); add(t.dropLast(2)) }
            }
            LanguageCode.SPANISH -> {
                if (t.endsWith("es") && t.length > 3) add(t.dropLast(2))
                if (t.endsWith("s") && t.length > 2) add(t.dropLast(1))
                val base = if (t.endsWith("s") && t.length > 2) t.dropLast(1) else t
                if (base.endsWith("a") && base.length > 2) add(base.dropLast(1) + "o")
            }
            LanguageCode.FRENCH -> {
                if ((t.endsWith("s") || t.endsWith("x")) && t.length > 2) add(t.dropLast(1))
                val base = if ((t.endsWith("s") || t.endsWith("x")) && t.length > 2) t.dropLast(1) else t
                if (base.endsWith("e") && base.length > 2) add(base.dropLast(1))
                if (base.endsWith("ne") && base.length > 3) add(base.dropLast(2))
                if (base.endsWith("euse") && base.length > 5) add(base.dropLast(4) + "eux")
                if (base.endsWith("ve") && base.length > 3) add(base.dropLast(2) + "f")
            }
            LanguageCode.ARABIC -> {
                val stripped = mutableListOf(t)
                // Clitics stack (و + ب + ال), so strip in two passes.
                repeat(2) {
                    for (s in stripped.toList()) for (p in listOf("وال", "بال", "فال", "كال", "لل", "ال", "و", "ف", "ب", "ل", "ك", "س")) {
                        if (s.startsWith(p) && s.length - p.length >= 2) stripped += s.removePrefix(p)
                    }
                }
                for (s in stripped.toList()) for (suf in listOf("هما", "كما", "ها", "هم", "هن", "كم", "كن", "نا", "ني", "ات", "ون", "ين", "ان", "ه", "ك", "ي", "ت", "ا", "و")) {
                    if (s.endsWith(suf) && s.length - suf.length >= 2) stripped += s.removeSuffix(suf)
                }
                for (s in stripped.toList()) if (s.endsWith("ت") && s.length > 2) stripped += s.dropLast(1) + "ة"
                addAll(stripped.drop(1))
            }
            LanguageCode.MALAY -> {
                val bases = mutableListOf(t)
                for (suf in listOf("nya", "lah", "kah", "pun", "kan", "an", "i")) if (t.endsWith(suf) && t.length - suf.length >= 3) bases += t.removeSuffix(suf)
                for (b in bases.toList()) {
                    when {
                        b.startsWith("meny") && b.length > 5 -> bases += "s" + b.drop(4)
                        b.startsWith("meng") && b.length > 5 -> { bases += b.drop(4); bases += "k" + b.drop(4) }
                        b.startsWith("mem") && b.length > 4 -> { bases += b.drop(3); bases += "p" + b.drop(3) }
                        b.startsWith("men") && b.length > 4 -> { bases += b.drop(3); bases += "t" + b.drop(3) }
                        b.startsWith("me") && b.length > 4 -> bases += b.drop(2)
                    }
                    for (p in listOf("ber", "ter", "di", "ke", "se", "pe")) if (b.startsWith(p) && b.length - p.length >= 3) bases += b.removePrefix(p)
                }
                addAll(bases.drop(1))
            }
            else -> {}
        }
    }

    /** Longest-match segmentation over the list's terms and forms; unknown Han characters become one-character tokens. */
    private fun segment(sentence: String, names: List<String>): List<Pair<String, Int?>> {
        val s = stripNames(normalize(sentence), names)
        val out = mutableListOf<Pair<String, Int?>>()
        var i = 0
        while (i < s.length) {
            if (s[i] !in '一'..'鿿') { i++; continue }
            var matched = false
            for (len in minOf(zhMaxLength, s.length - i) downTo 1) {
                val piece = s.substring(i, i + len)
                zhDictionary[piece]?.let { r -> out += piece to r; i += len; matched = true }
                if (matched) break
            }
            if (!matched) { out += s[i].toString() to null; i++ }
        }
        return out
    }
}
