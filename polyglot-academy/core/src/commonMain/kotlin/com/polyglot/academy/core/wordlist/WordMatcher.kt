package com.polyglot.academy.core.wordlist

import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.PartOfSpeech

/**
 * Finds list words in sentences, per language: normalisation (case, Turkish i/ı, Arabic and Urdu
 * vowel marks), tokenisation (French elision, Chinese longest match) and light inflection rules
 * (English suffixes, Spanish/French plurals and feminine forms, Arabic clitics, Turkish suffixes
 * by stem, Urdu gender/number endings and verb stems, Malay affixes). Irregular forms come from each entry's forms list. The rules are
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
        // Arabic code points typed on some keyboards for Urdu letters.
        if (language == LanguageCode.URDU) t = t.replace('ي', 'ی').replace('ى', 'ی').replace('ك', 'ک').replace("ۓ", "ئے")
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
        if (keys.isNotEmpty() && toks.any { tok -> candidates(tok).any { it in keys } }) return true
        return phrases[entry.id].orEmpty().any { p -> containsSequence(toks, p) }
    }

    /** Rank of the most frequent entry this token belongs to, or null if the list does not know it. */
    fun rankOf(token: String): Int? {
        if (language == LanguageCode.MANDARIN) return zhDictionary[token]
        return candidates(token).mapNotNull { keyRank[it] }.minOrNull()
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
        // Verbs only: the noun "ekmek" (bread) has no stem "ek".
        if (language == LanguageCode.TURKISH && e.partOfSpeech == PartOfSpeech.VERB && (e.term.endsWith("mek") || e.term.endsWith("mak")) && e.term.length > 4) {
            add(e.term.dropLast(3))
        }
        // Broken plurals and the present stem cannot be derived by rule.
        if (language == LanguageCode.ARABIC) listOf("plural", "present").mapNotNull { e.extra[it] }.forEach { add(it) }
        if (language == LanguageCode.URDU) {
            e.extra["plural"]?.let { add(it) }
            // Urdu verbs are listed by infinitive (جانا); the stem (جا) carries all other forms.
            if (e.partOfSpeech == PartOfSpeech.VERB && e.term.endsWith("نا") && e.term.length > 2) add(e.term.dropLast(2))
        }
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
        toks.windowed(phrase.size).any { w -> w.indices.all { i -> candidates(w[i]).contains(phrase[i]) } }

    /**
     * Turkish is agglutinative: a word is a stem followed by suffixes (ev-ler-imiz-de). A stem is accepted
     * only when the rest of the word splits completely into known suffix pieces, so "bulmak" is not taken
     * for "bu" + something. Final p/ç/t/k soften before a vowel (kitabı, gidiyor, çocuğu) and a stem's
     * final e/a narrows before -iyor (bekliyor ← bekle).
     */
    private fun turkishStems(token: String): List<String> = buildList {
        for (len in token.length - 1 downTo 1) {
            val stem = token.substring(0, len)
            val rest = token.substring(len)
            if (!turkishSuffixChain(rest, HashMap())) continue
            add(stem)
            when (stem.last()) {
                'b' -> add(stem.dropLast(1) + "p")
                'c' -> add(stem.dropLast(1) + "ç")
                'd' -> add(stem.dropLast(1) + "t")
                'ğ', 'g' -> add(stem.dropLast(1) + "k")
            }
            if (TR_PRESENT.any { rest.startsWith(it) }) { add(stem + "e"); add(stem + "a") }
        }
    }

    private fun turkishSuffixChain(rest: String, memo: HashMap<String, Boolean>): Boolean {
        if (rest.isEmpty()) return true
        memo[rest]?.let { return it }
        val ok = TR_SUFFIXES.any { rest.startsWith(it) && turkishSuffixChain(rest.substring(it.length), memo) }
        memo[rest] = ok
        return ok
    }

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
                val stripped = linkedSetOf(t)
                // Clitics stack (و + ب + ال), so strip in two passes.
                repeat(2) {
                    for (s in stripped.toList()) for (p in listOf("وال", "بال", "فال", "كال", "لل", "ال", "و", "ف", "ب", "ل", "ك", "س")) {
                        if (s.startsWith(p) && s.length - p.length >= 2) stripped += s.removePrefix(p)
                    }
                }
                // Endings stack too (كتبته = كتب + ت + ه).
                repeat(2) {
                    for (s in stripped.toList()) for (suf in listOf("هما", "كما", "ها", "هم", "هن", "كم", "كن", "نا", "ني", "ات", "ون", "ين", "ان", "وا", "تم", "ه", "ك", "ي", "ت", "ا", "و")) {
                        if (s.endsWith(suf) && s.length - suf.length >= 2) stripped += s.removeSuffix(suf)
                    }
                }
                for (s in stripped.toList()) {
                    if (s.endsWith("ت") && s.length > 2) stripped += s.dropLast(1) + "ة"
                    if (s.endsWith("ة") && s.length > 2) stripped += s.dropLast(1)
                }
                // Present tense: أذهب, تذهب, نذهب match the يـ form stored in the entry's "present" field.
                for (s in stripped.toList()) if (s.length >= 3 && s[0] in "اتن") stripped += "ي" + s.drop(1)
                addAll(stripped.drop(1))
            }
            LanguageCode.TURKISH -> addAll(turkishStems(t))
            LanguageCode.URDU -> {
                // One ending is removed, then the bare stem and the -ا / -ی / -ہ dictionary endings are tried:
                // لڑکے، لڑکوں → لڑکا; لڑکیاں → لڑکی; کمرے → کمرہ; اچھی → اچھا; جاتی، جائیں، کھایا → جا، کھا.
                for (suf in UR_SUFFIXES) if (t.endsWith(suf) && t.length > suf.length) {
                    val stem = t.dropLast(suf.length)
                    add(stem); add(stem + "ا"); add(stem + "ی"); add(stem + "ہ")
                }
            }
            LanguageCode.MALAY -> {
                val bases = mutableListOf(t)
                // -ku / -mu are the possessive clitics (bukuku, rumahmu).
                for (suf in listOf("nya", "lah", "kah", "pun", "kan", "an", "ku", "mu", "i")) if (t.endsWith(suf) && t.length - suf.length >= 3) bases += t.removeSuffix(suf)
                for (b in bases.toList()) {
                    when {
                        b.startsWith("meny") && b.length > 5 -> bases += "s" + b.drop(4)
                        b.startsWith("meng") && b.length > 5 -> { bases += b.drop(4); bases += "k" + b.drop(4) }
                        b.startsWith("mem") && b.length > 4 -> { bases += b.drop(3); bases += "p" + b.drop(3) }
                        b.startsWith("men") && b.length > 4 -> { bases += b.drop(3); bases += "t" + b.drop(3) }
                    }
                    // Plain me- before m, n, ng, ny, l, r, w, y (memasak, menanti, melihat).
                    if (b.startsWith("me") && b.length > 4) bases += b.drop(2)
                    // ber- loses its r before r and some stems (bekerja); belajar is listed as a form.
                    for (p in listOf("ber", "ter", "be", "di", "ke", "se", "pe")) if (b.startsWith(p) && b.length - p.length >= 3) bases += b.removePrefix(p)
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

    private companion object {
        val TR_PRESENT = listOf("iyor", "ıyor", "uyor", "üyor")

        /** Urdu noun, adjective and verb endings (plural, oblique, gender, habitual, perfective, subjunctive, polite). */
        val UR_SUFFIXES = listOf(
            "یاں", "یوں", "ئیں", "ئیے", "یے", "یں", "وں", "ؤں", "تا", "تی", "تے", "نا", "نی", "نے", "یا", "ئی", "ئے", "ے", "ی", "ا", "و", "ؤ",
        )

        /**
         * Turkish suffix pieces in all their vowel-harmony and consonant variants: plural, case, possessive,
         * tense, person, negation, question and common derivations. Single letters cover buffer consonants
         * and one-letter endings (ev-e, oku-r, gel-di-m).
         */
        val TR_SUFFIXES = listOf(
            "iyor", "ıyor", "uyor", "üyor", "yor",
            "acak", "ecek", "acağ", "eceğ", "mış", "miş", "muş", "müş", "malı", "meli", "abil", "ebil",
            "arak", "erek", "ınca", "ince", "unca", "ünce",
            "dan", "den", "tan", "ten", "nın", "nin", "nun", "nün", "lar", "ler", "mız", "miz", "muz", "müz",
            "nız", "niz", "nuz", "nüz", "sın", "sin", "sun", "sün", "dır", "dir", "dur", "dür", "tır", "tir", "tur", "tür",
            "maz", "mez", "mak", "mek", "ken", "lık", "lik", "luk", "lük", "sız", "siz", "suz", "süz",
            "dık", "dik", "duk", "dük", "tık", "tik", "tuk", "tük", "yla", "yle",
            "da", "de", "ta", "te", "ın", "in", "un", "ün", "la", "le", "ki", "ım", "im", "um", "üm",
            "dı", "di", "du", "dü", "tı", "ti", "tu", "tü", "ar", "er", "ır", "ir", "ur", "ür", "ma", "me",
            "sa", "se", "lı", "li", "lu", "lü", "cı", "ci", "cu", "cü", "çı", "çi", "çu", "çü", "ca", "ce", "ça", "çe",
            "ıp", "ip", "up", "üp", "mı", "mi", "mu", "mü",
            "a", "e", "ı", "i", "u", "ü", "m", "n", "k", "y", "s", "r", "z",
        )
    }
}
