package com.polyglot.academy.core.content

import com.polyglot.academy.core.model.Exercise
import com.polyglot.academy.core.model.Gloss
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.Lesson
import com.polyglot.academy.core.model.Skill
import com.polyglot.academy.core.wordlist.WordEntry
import com.polyglot.academy.core.wordlist.WordLevels
import com.polyglot.academy.core.wordlist.WordList
import com.polyglot.academy.core.wordlist.WordLists
import kotlin.random.Random

/**
 * Lessons generated from the frequency word lists: each covers [PER_LESSON] consecutive words of a
 * level (30 lessons per level). Exercises: a flashcard per word, two matching rounds, meaning and
 * reverse-meaning choice, listening choice, and, from the words' example sentences, word order and
 * dictation. Generation is deterministic, so a lesson id always yields the same lesson (needed to
 * resume it and to rebuild its earlier answers).
 */
object WordLessons {
    const val PER_LESSON = 10
    const val PER_LEVEL = WordLevels.SIZE / PER_LESSON
    private const val PREFIX = "wl_"

    fun id(language: LanguageCode, level: Int, number: Int) = "$PREFIX${language.tag}_${level}_$number"

    fun isWordLesson(id: String) = id.startsWith(PREFIX)

    /** (language, level, lesson number 1..[PER_LEVEL]) of a generated lesson id, or null. */
    fun parse(id: String): Triple<LanguageCode, Int, Int>? {
        if (!isWordLesson(id)) return null
        val p = id.removePrefix(PREFIX).split('_')
        if (p.size != 3) return null
        val lang = LanguageCode.entries.firstOrNull { it.tag == p[0] } ?: return null
        val level = p[1].toIntOrNull()?.takeIf { it in 1..WordLevels.COUNT } ?: return null
        val number = p[2].toIntOrNull()?.takeIf { it in 1..PER_LEVEL } ?: return null
        return Triple(lang, level, number)
    }

    /** Ranks covered by a lesson. */
    fun ranks(level: Int, number: Int): IntRange {
        val first = WordLevels.ranks(level).first + (number - 1) * PER_LESSON
        return first until first + PER_LESSON
    }

    /** Builds the lesson for [id], loading the word list off the main thread; null for an unknown id. */
    suspend fun load(id: String): Lesson? {
        val (lang, level, number) = parse(id) ?: return null
        return build(WordLists.load(lang), level, number)
    }

    fun build(list: WordList, level: Int, number: Int): Lesson {
        val lang = list.language
        val id = id(lang, level, number)
        val words = ranks(level, number).mapNotNull { list.byRank[it] }
        val pool = list.level(level)
        val random = Random(id.hashCode())
        val exercises = mutableListOf<Exercise>()

        words.forEach { e -> exercises += Exercise.Flashcard("${id}_fc_${e.id}", Skill.VOCABULARY, Instr.flashcard, e.vocabularyId ?: e.id) }
        words.chunked(5).forEachIndexed { i, chunk ->
            val pairs = chunk.distinctBy { it.term }.distinctBy { it.meaning.en }.map { it.term to it.meaning }
            if (pairs.size >= 3) exercises += Exercise.Matching("${id}_match$i", Skill.VOCABULARY, Instr.matching, pairs)
        }
        words.filterIndexed { i, _ -> i % 3 == 0 }.forEach { e ->
            val options = options(e, pool, random)
            exercises += Exercise.MultipleChoice(
                "${id}_mc_${e.id}", Skill.VOCABULARY, Instr.meaning, prompt = e.term,
                options = options.map { it.meaning.en }, correctIndex = options.indexOf(e), difficulty = 1, optionGlosses = options.map { it.meaning },
            )
        }
        words.filterIndexed { i, _ -> i % 3 == 1 }.forEach { e ->
            val options = options(e, pool, random)
            exercises += Exercise.MultipleChoice(
                "${id}_rev_${e.id}", Skill.VOCABULARY, Instr.whichWord, prompt = e.meaning.en,
                options = options.map { it.term }, correctIndex = options.indexOf(e), difficulty = 2, promptGloss = e.meaning,
            )
        }
        words.filterIndexed { i, _ -> i % 5 == 2 }.forEach { e ->
            val options = options(e, pool, random)
            exercises += Exercise.MultipleChoice(
                "${id}_listen_${e.id}", Skill.LISTENING, Instr.listen, prompt = "🔊",
                options = options.map { it.term }, correctIndex = options.indexOf(e), difficulty = 2, audioText = e.term,
            )
        }
        if (lang != LanguageCode.MANDARIN) {
            words.firstNotNullOfOrNull { e -> e.examples.firstOrNull { it.sentence.split(' ').size in 4..8 }?.let { e to it } }?.let { (e, ex) ->
                exercises += Exercise.SentenceOrder("${id}_order_${e.id}", Skill.GRAMMAR, Instr.order, ex.sentence.split(' '), " ", ex.translation)
            }
        }
        words.flatMap { e -> e.examples.map { e to it } }.minByOrNull { it.second.sentence.length }?.let { (e, ex) ->
            exercises += Exercise.Dictation("${id}_dict_${e.id}", Skill.LISTENING, Instr.dictation, ex.sentence, listOf(ex.sentence), 3)
        }

        val range = ranks(level, number)
        return Lesson(
            id = id, language = lang, level = WordLevels.cefr(level), unitId = "wordlist_${lang.tag}_$level",
            title = Gloss("Level $level · words ${range.first}–${range.last}", "المستوى $level · الكلمات ⁦${range.first}–${range.last}⁩"),
            summary = Gloss(
                words.joinToString(", ") { it.term },
                words.joinToString("، ") { it.term },
            ),
            primarySkill = Skill.VOCABULARY, estimatedMinutes = 10,
            vocabularyIds = words.map { it.reviewId }, grammarTopicId = null, exercises = exercises,
        )
    }

    /** Four choices: the word and three others of the level with a different written form and meaning, same part of speech first. */
    private fun options(e: WordEntry, pool: List<WordEntry>, random: Random): List<WordEntry> {
        fun usable(o: WordEntry) = o.id != e.id && o.term != e.term && o.meaning.en != e.meaning.en && o.meaning.ar != e.meaning.ar
        val same = pool.filter { usable(it) && it.partOfSpeech == e.partOfSpeech }.shuffled(random)
        val other = pool.filter { usable(it) && it.partOfSpeech != e.partOfSpeech }.shuffled(random)
        val distractors = (same + other).distinctBy { it.meaning.en }.distinctBy { it.meaning.ar }.distinctBy { it.term }.take(3)
        return (distractors + e).shuffled(random)
    }
}
