package com.polyglot.academy.core.stories

import com.polyglot.academy.core.model.CefrLevel
import com.polyglot.academy.core.model.Exercise
import com.polyglot.academy.core.model.Gloss
import com.polyglot.academy.core.model.GlossaryEntry
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.Languages
import com.polyglot.academy.core.model.ReadingPassage
import com.polyglot.academy.core.model.Skill
import com.polyglot.academy.core.model.VocabularyItem
import com.polyglot.academy.core.tutor.TutorResponseParser
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/** A piece of a sentence: a word the learner can tap, or punctuation and spaces. */
data class StoryToken(val text: String, val isWord: Boolean)

/** What a tapped word means, and where the meaning came from. */
data class WordInfo(val term: String, val meaning: Gloss, val reading: String?, val vocabularyId: String?)

object StoryText {
    private const val MAX_CJK_WORD = 6

    /**
     * Splits a sentence into tappable words. Space-separated languages split on letters
     * (keeping apostrophes and hyphens inside words); Mandarin uses the longest known term
     * at each position, falling back to single characters.
     */
    fun tokens(sentence: String, language: LanguageCode, knownTerms: Set<String> = emptySet()): List<StoryToken> =
        if (language == LanguageCode.MANDARIN) cjk(sentence, knownTerms) else mergePhrases(spaced(sentence), knownTerms)

    /** Joins consecutive tokens that spell a known multi-word phrase, so it is tapped (and explained) as one. */
    private fun mergePhrases(tokens: List<StoryToken>, known: Set<String>): List<StoryToken> {
        val phrases = known.filter { ' ' in it }.map(::normalize).toSet()
        if (phrases.isEmpty()) return tokens
        val out = mutableListOf<StoryToken>()
        var i = 0
        while (i < tokens.size) {
            if (tokens[i].isWord) {
                val end = (minOf(tokens.size - 1, i + 12) downTo i + 2).firstOrNull { j ->
                    tokens[j].isWord && normalize(tokens.subList(i, j + 1).joinToString("") { it.text }) in phrases
                }
                if (end != null) { out += StoryToken(tokens.subList(i, end + 1).joinToString("") { it.text }, true); i = end + 1; continue }
            }
            out += tokens[i]; i++
        }
        return out
    }

    private fun isWordChar(c: Char) = c.isLetterOrDigit() || c == 'ً' || (c in 'ً'..'ٟ') || c == 'ٰ'

    private fun spaced(s: String): List<StoryToken> {
        val out = mutableListOf<StoryToken>()
        var i = 0
        while (i < s.length) {
            val start = i
            if (isWordChar(s[i])) {
                while (i < s.length && (isWordChar(s[i]) || ((s[i] == '\'' || s[i] == '’' || s[i] == '-') && i + 1 < s.length && isWordChar(s[i + 1])))) i++
                out += StoryToken(s.substring(start, i), true)
            } else {
                while (i < s.length && !isWordChar(s[i])) i++
                out += StoryToken(s.substring(start, i), false)
            }
        }
        return out
    }

    private fun cjk(s: String, known: Set<String>): List<StoryToken> {
        val out = mutableListOf<StoryToken>()
        var i = 0
        while (i < s.length) {
            val c = s[i]
            if (!c.isLetterOrDigit()) {
                val start = i
                while (i < s.length && !s[i].isLetterOrDigit()) i++
                out += StoryToken(s.substring(start, i), false)
                continue
            }
            val len = (minOf(MAX_CJK_WORD, s.length - i) downTo 2).firstOrNull { s.substring(i, i + it) in known } ?: 1
            out += StoryToken(s.substring(i, i + len), true)
            i += len
        }
        return out
    }

    fun normalize(word: String): String =
        word.lowercase().replace(Regex("[\\u064B-\\u065F\\u0670\\u0640]"), "").replace('’', '\'').trim()

    /** Known terms for a passage: its glossary and the language's vocabulary (single words and phrases). */
    fun knownTerms(passage: ReadingPassage, vocabulary: Collection<VocabularyItem>): Set<String> =
        (passage.glossary.map { it.term } + vocabulary.map { it.term }).toSet()

    /** Looks a tapped word up in the story glossary first, then in the vocabulary. */
    fun lookup(word: String, passage: ReadingPassage, vocabulary: Collection<VocabularyItem>): WordInfo? {
        val n = normalize(word)
        if (n.isEmpty()) return null
        val candidates = buildList {
            add(n)
            // French/English elision: l'aéroport → aéroport.
            if ('\'' in n) add(n.substringAfterLast('\''))
            // Arabic: try without attached conjunctions, prepositions and the article.
            if (passage.language == LanguageCode.ARABIC) listOf("وال", "بال", "فال", "لل", "ال", "و", "ف", "ب").forEach { p -> if (n.startsWith(p) && n.length > p.length + 1) add(n.removePrefix(p)) }
        }.distinct()
        for (cand in candidates) {
            passage.glossary.firstOrNull { normalize(it.term) == cand }?.let { g ->
                return WordInfo(g.term, g.meaning, g.reading, vocabulary.firstOrNull { normalize(it.term) == cand }?.id)
            }
            vocabulary.firstOrNull { normalize(it.term) == cand || (passage.language == LanguageCode.ARABIC && normalize(it.term).removePrefix("ال") == cand) }?.let { v ->
                return WordInfo(v.term, v.meaning, v.pronunciation, v.id)
            }
        }
        return null
    }
}

/** Prompt and validation for stories an AI provider writes about the learner's interest. */
object StoryGenerator {
    fun cleanInterest(raw: String): String? = raw.replace(Regex("[\\r\\n\\t\"|{}]+"), " ").replace(Regex(" {2,}"), " ").trim().take(80).ifBlank { null }

    fun sentenceRange(level: CefrLevel): IntRange = when (level) {
        CefrLevel.A1 -> 5..7
        CefrLevel.A2 -> 6..9
        CefrLevel.B1 -> 8..11
        else -> 10..14
    }

    fun systemPrompt(language: LanguageCode, explanation: LanguageCode, level: CefrLevel, interest: String, knownWords: List<String>): String {
        val target = Languages.of(language).englishName
        val explain = Languages.of(explanation).englishName
        val range = sentenceRange(level)
        return buildString {
            appendLine("You write short graded reading stories for a language-learning app.")
            appendLine("Write an original story in $target for a learner at CEFR level ${level.label} about: \"$interest\".")
            appendLine("Use ${range.first}–${range.last} short sentences, vocabulary and grammar suitable for ${level.label}, and a clear beginning, middle and end.")
            if (knownWords.isNotEmpty()) appendLine("Reuse some words the learner already knows: ${knownWords.take(15).joinToString()}.")
            if (language == LanguageCode.MANDARIN) appendLine("Write in simplified characters. Give pinyin with tone marks as \"reading\" in the glossary.")
            if (language == LanguageCode.ARABIC) appendLine("Write in Modern Standard Arabic with full vowel marks (tashkeel) on uncommon words.")
            appendLine("Keep it suitable for a general audience. Do not copy existing published stories.")
            appendLine("Translate each sentence into English and into Arabic.")
            appendLine("Give a glossary of 8–14 useful words or phrases exactly as they appear in the story, with English and Arabic meanings.")
            appendLine("Write 3 multiple-choice comprehension questions in $target with 3 options each, and the index of the correct option.")
            appendLine("Explanations for the learner are in $explain.")
            appendLine("Respond ONLY with JSON of this shape:")
            append("""{"title":"","title_en":"","title_ar":"","sentences":[{"text":"","en":"","ar":""}],"glossary":[{"term":"","en":"","ar":"","reading":""}],"questions":[{"question":"","question_en":"","question_ar":"","options":["","",""],"answer":0}]}""")
        }
    }

    sealed interface Parsed {
        data class Ok(val story: ReadingPassage) : Parsed
        data class Invalid(val reason: String) : Parsed
    }

    /** Parses and checks a generated story; anything incomplete is rejected rather than shown half-broken. */
    fun parse(raw: String, id: String, language: LanguageCode, level: CefrLevel, interest: String, now: Long): Parsed {
        val obj = TutorResponseParser.extractJsonObject(raw) ?: return Parsed.Invalid("no_json")
        fun JsonObject.s(k: String) = (this[k] as? JsonPrimitive)?.takeIf { it.isString }?.content?.trim().orEmpty()
        val title = obj.s("title").ifEmpty { return Parsed.Invalid("no_title") }
        val sentences = (obj["sentences"] as? JsonArray).orEmpty().mapNotNull { it as? JsonObject }
            .map { Triple(it.s("text"), it.s("en"), it.s("ar")) }.filter { it.first.isNotEmpty() }
        if (sentences.size < 3) return Parsed.Invalid("too_short")
        if (sentences.size > 20) return Parsed.Invalid("too_long")
        val text = sentences.joinToString(" ") { it.first }
        val glossary = (obj["glossary"] as? JsonArray).orEmpty().mapNotNull { it as? JsonObject }.mapNotNull { g ->
            val term = g.s("term")
            if (term.isEmpty() || g.s("en").isEmpty() || StoryText.normalize(term) !in StoryText.normalize(text)) null
            else GlossaryEntry(term, Gloss(g.s("en"), g.s("ar").ifEmpty { g.s("en") }), g.s("reading").ifEmpty { null })
        }.distinctBy { StoryText.normalize(it.term) }
        val questions = (obj["questions"] as? JsonArray).orEmpty().mapNotNull { it as? JsonObject }.mapIndexedNotNull { i, q ->
            val options = (q["options"] as? JsonArray).orEmpty().mapNotNull { (it as? JsonPrimitive)?.content?.trim() }.filter { it.isNotEmpty() }
            val answer = (q["answer"] as? JsonPrimitive)?.content?.toIntOrNull()
            val question = q.s("question")
            if (question.isEmpty() || options.size < 2 || options.distinct().size != options.size || answer == null || answer !in options.indices) null
            else Exercise.MultipleChoice("${id}_q${i + 1}", Skill.READING, Gloss("Choose the correct answer.", "اختر الإجابة الصحيحة."), question, options, answer)
        }
        return Parsed.Ok(ReadingPassage(
            id = id, language = language, level = level, title = title,
            titleTranslation = Gloss(obj.s("title_en").ifEmpty { title }, obj.s("title_ar").ifEmpty { obj.s("title_en").ifEmpty { title } }),
            sentences = sentences.map { it.first },
            sentenceTranslations = sentences.map { (_, en, ar) -> Gloss(en.ifEmpty { "—" }, ar.ifEmpty { en.ifEmpty { "—" } }) },
            highlightedVocabulary = emptyList(), questions = questions, glossary = glossary,
            generated = true, interest = interest, createdAtMillis = now,
        ))
    }
}
