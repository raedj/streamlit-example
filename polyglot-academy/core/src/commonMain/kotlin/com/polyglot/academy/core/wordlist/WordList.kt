package com.polyglot.academy.core.wordlist

import com.polyglot.academy.core.model.CefrLevel
import com.polyglot.academy.core.model.Gender
import com.polyglot.academy.core.model.Gloss
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.PartOfSpeech
import com.polyglot.academy.core.model.VocabularyItem
import com.polyglot.academy.core.wordlist.generated.GeneratedWordLists

/**
 * The frequency word lists: up to 3000 lemmas per language in 10 levels of 300, most frequent first.
 * Data lives in content/wordlists/<lang>.{words,examples}.tsv and is compiled into Kotlin by
 * tools/gen_wordlists.py; see docs/ARCHITECTURE.md ("Word lists").
 */
object WordLevels {
    const val SIZE = 300
    const val COUNT = 10
    const val TARGET = SIZE * COUNT

    /** Example words may be at most this many ranks less frequent than the word they illustrate. */
    const val EXAMPLE_RANK_SLACK = 300
    /** Share of an example's words (names and numbers excluded) that must fall within the slack. */
    const val EXAMPLE_COVERAGE = 0.85

    fun levelOf(rank: Int): Int = (rank - 1) / SIZE + 1
    fun ranks(level: Int): IntRange = ((level - 1) * SIZE + 1)..(level * SIZE)

    /** Approximate CEFR band, not a certified equivalence: 1–2 A1, 3–4 A2, 5–6 B1, 7–8 B2, 9–10 B2+ to C1. */
    fun cefr(level: Int): CefrLevel = when (level) {
        1, 2 -> CefrLevel.A1
        3, 4 -> CefrLevel.A2
        5, 6 -> CefrLevel.B1
        7, 8 -> CefrLevel.B2
        else -> CefrLevel.C1
    }
}

data class WordExample(val sentence: String, val translation: Gloss, val names: List<String> = emptyList())

data class WordEntry(
    val language: LanguageCode,
    val rank: Int,
    val id: String,
    /** Lemma (dictionary form). Arabic with full vowel marks; Mandarin in simplified characters. */
    val term: String,
    val partOfSpeech: PartOfSpeech,
    val topic: String,
    val meaning: Gloss,
    /** IPA, pinyin with tone marks, or (Arabic, Urdu) a Latin transliteration. */
    val pronunciation: String?,
    /** Inflected and irregular forms (went, gone; fue; l'), also used to find the word in a sentence. */
    val forms: List<String>,
    /** Language-specific fields: gender, article, plural, root, masdar, present, trad, hsk, measure, stem, irregular. */
    val extra: Map<String, String>,
    /** Id of the same word in the hand-written curriculum, if it exists there. */
    val vocabularyId: String?,
    val examples: List<WordExample>,
) {
    val level: Int get() = WordLevels.levelOf(rank)

    /** Key of the review state: the existing curriculum id when linked, so earlier review progress is kept. */
    val reviewId: String get() = vocabularyId ?: id

    fun toVocabularyItem(): VocabularyItem {
        val transliterated = language == LanguageCode.ARABIC || language == LanguageCode.URDU
        val first = examples.firstOrNull()
        return VocabularyItem(
            id = reviewId, language = language, term = term, meaning = meaning, partOfSpeech = partOfSpeech,
            level = WordLevels.cefr(level), topic = topic,
            pronunciation = pronunciation.takeUnless { transliterated },
            transliteration = pronunciation.takeIf { transliterated },
            example = first?.sentence, exampleTranslation = first?.translation,
            gender = when (extra["gender"]) { "m" -> Gender.MASCULINE; "f" -> Gender.FEMININE; else -> null },
            traditional = extra["trad"],
            tags = setOf("wordlist", "wordlevel_$level"),
        )
    }
}

class WordList(val language: LanguageCode, val entries: List<WordEntry>) {
    val byRank: Map<Int, WordEntry> = entries.associateBy { it.rank }
    val byId: Map<String, WordEntry> = entries.associateBy { it.id }
    val levelCount: Int get() = entries.size / WordLevels.SIZE
    val matcher: WordMatcher by lazy { WordMatcher(language, entries) }

    fun level(level: Int): List<WordEntry> = WordLevels.ranks(level).mapNotNull { byRank[it] }
}

object WordLists {
    private val lists: Map<LanguageCode, Lazy<WordList>> = LanguageCode.entries.associateWith { code -> lazy { load(code) } }

    /** The language's list; loaded on first use only, so other languages cost nothing at start-up. */
    fun of(language: LanguageCode): WordList = lists.getValue(language).value

    private fun load(language: LanguageCode): WordList {
        val chunks = GeneratedWordLists.levels(language.tag).map { it() }
        return parse(language, chunks.flatMap { it.first }, chunks.flatMap { it.second })
    }

    private val posCodes = mapOf(
        "noun" to PartOfSpeech.NOUN, "verb" to PartOfSpeech.VERB, "adj" to PartOfSpeech.ADJECTIVE, "adv" to PartOfSpeech.ADVERB,
        "pron" to PartOfSpeech.PRONOUN, "prep" to PartOfSpeech.PREPOSITION, "conj" to PartOfSpeech.CONJUNCTION,
        "interj" to PartOfSpeech.INTERJECTION, "num" to PartOfSpeech.NUMERAL, "phrase" to PartOfSpeech.PHRASE,
        "part" to PartOfSpeech.PARTICLE, "measure" to PartOfSpeech.MEASURE_WORD, "det" to PartOfSpeech.DETERMINER,
    )

    /** Parses the generated rows (same columns as the TSV files, without headers). */
    fun parse(language: LanguageCode, wordRows: List<String>, exampleRows: List<String>): WordList {
        val examples = exampleRows.map { it.split('\t') }.groupBy({ it[0].toInt() }) { c ->
            WordExample(c[1], Gloss(c[2], c[3]), c[4].split(',').map { it.trim() }.filter { it.isNotEmpty() })
        }
        val entries = wordRows.map { row ->
            val c = row.split('\t')
            val rank = c[0].toInt()
            WordEntry(
                language = language, rank = rank, id = c[1], term = c[2],
                partOfSpeech = posCodes[c[3]] ?: error("unknown part of speech ${c[3]} at ${language.tag} rank $rank"),
                topic = c[4], meaning = Gloss(c[5], c[6]), pronunciation = c[7].ifEmpty { null },
                forms = c[8].split(',').map { it.trim() }.filter { it.isNotEmpty() },
                extra = c[9].split(';').mapNotNull { kv -> kv.split('=', limit = 2).takeIf { it.size == 2 }?.let { it[0].trim() to it[1].trim() } }.toMap(),
                vocabularyId = c[10].ifEmpty { null },
                examples = examples[rank].orEmpty(),
            )
        }
        return WordList(language, entries)
    }
}
