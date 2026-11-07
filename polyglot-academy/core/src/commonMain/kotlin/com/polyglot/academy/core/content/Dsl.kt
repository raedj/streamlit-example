package com.polyglot.academy.core.content

import com.polyglot.academy.core.learning.Recall
import com.polyglot.academy.core.mandarin.Pinyin
import com.polyglot.academy.core.model.CefrLevel
import com.polyglot.academy.core.model.Exercise
import com.polyglot.academy.core.model.Gender
import com.polyglot.academy.core.model.Gloss
import com.polyglot.academy.core.model.GrammarExample
import com.polyglot.academy.core.model.GrammarTopic
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.Lesson
import com.polyglot.academy.core.model.PartOfSpeech
import com.polyglot.academy.core.model.Skill
import com.polyglot.academy.core.model.VocabularyItem
import com.polyglot.academy.core.model.VocabularySource
import com.polyglot.academy.core.model.WORD_RECALL_MARK

/** Small builders that keep curriculum files readable. */
internal fun g(en: String, ar: String) = Gloss(en, ar)

internal class VocabBuilder(private val lang: LanguageCode) {
    val items = mutableListOf<VocabularyItem>()
    fun v(
        id: String, term: String, en: String, ar: String, pos: PartOfSpeech, level: CefrLevel, topic: String,
        pron: String? = null, translit: String? = null, ex: String? = null, exEn: String? = null, exAr: String? = null,
        gender: Gender? = null, trad: String? = null, tags: Set<String> = emptySet(),
        source: VocabularySource = VocabularySource.BUILT_IN,
    ) {
        items += VocabularyItem(
            id = "${lang.tag}_$id", language = lang, term = term, meaning = Gloss(en, ar), partOfSpeech = pos, level = level,
            topic = topic, pronunciation = pron, transliteration = translit, example = ex,
            exampleTranslation = if (exEn != null && exAr != null) Gloss(exEn, exAr) else null,
            gender = gender, traditional = trad, tags = tags, source = source,
        )
    }
}

internal fun grammar(
    id: String, lang: LanguageCode, level: CefrLevel, title: Gloss, rule: Gloss,
    examples: List<GrammarExample>, mistakes: List<Gloss>,
) = GrammarTopic("${lang.tag}_g_$id", lang, level, title, rule, examples, mistakes)

internal fun ex(sentence: String, en: String, ar: String, wrong: Boolean = false) = GrammarExample(sentence, Gloss(en, ar), wrong)

internal object Instr {
    val recall = g("How do you say this?", "كيف تقول هذا؟")
    val grammarNote = g("Read this rule. The next exercises use it.", "اقرأ هذه القاعدة، فالتمارين التالية تستخدمها.")
    val flashcard = g("A new word: listen to it and read its meaning and example.", "كلمة جديدة: استمع إليها واقرأ معناها ومثالها.")
    val matching = g("Match each word with its meaning.", "طابِق كل كلمة مع معناها.")
    val meaning = g("Choose the correct meaning.", "اختر المعنى الصحيح.")
    val whichWord = g("Which word matches this meaning?", "أي كلمة تطابق هذا المعنى؟")
    val listen = g("Listen and choose what you heard.", "استمع واختر ما سمعته.")
    val speak = g("Listen, then say the sentence aloud.", "استمع ثم قل الجملة بصوت عالٍ.")
    val fill = g("Fill in the blank.", "املأ الفراغ.")
    val order = g("Put the words in the correct order.", "رتّب الكلمات بالترتيب الصحيح.")
    val translate = g("Translate into the language you are learning.", "ترجم إلى اللغة التي تتعلمها.")
    val dictation = g("Listen and type what you hear.", "استمع واكتب ما تسمعه.")
    val tone = g("Which tone does this syllable have?", "ما نغمة هذا المقطع؟")
    val choose = g("Choose the best answer.", "اختر الإجابة الأنسب.")
    val write = g("Write a short text.", "اكتب نصًا قصيرًا.")
}

/**
 * Generates a lesson from its vocabulary, moving from meeting a word to producing it: new-word cards,
 * matching, a little meaning choice, word recall, listening, putting an example sentence in order, then
 * the grammar rule with the hand-written exercises that use it, and last saying a sentence aloud. Nothing
 * here is new content: words, meanings and examples all come from the vocabulary. Distractors are
 * chosen deterministically.
 */
internal class LessonFactory(private val lang: LanguageCode, private val allVocab: List<VocabularyItem>) {
    private val byId = allVocab.associateBy { it.id }

    fun lesson(
        id: String, level: CefrLevel, unitId: String, title: Gloss, summary: Gloss, skill: Skill,
        vocab: List<String>, grammarId: String? = null, extra: List<Exercise> = emptyList(),
        minutes: Int = 8, prerequisites: List<String> = emptyList(),
    ): Lesson {
        val fullId = "${lang.tag}_l_$id"
        val items = vocab.map { v -> byId["${lang.tag}_$v"] ?: error("Unknown vocabulary ${lang.tag}_$v in lesson $fullId") }
        val exercises = mutableListOf<Exercise>()
        items.take(6).forEach { exercises += Exercise.Flashcard("${fullId}_fc_${it.id}", Skill.VOCABULARY, Instr.flashcard, it.id) }
        if (items.size >= 3) {
            exercises += Exercise.Matching("${fullId}_match", Skill.VOCABULARY, Instr.matching, items.take(4).map { it.term to it.meaning })
        }
        items.take(2).forEachIndexed { i, item ->
            val distractors = distractors(item, 3)
            val options = rotate(listOf(item) + distractors, i)
            exercises += Exercise.MultipleChoice(
                "${fullId}_mc_${item.id}", Skill.VOCABULARY, Instr.meaning, prompt = item.term,
                options = options.map { it.meaning.en }, correctIndex = options.indexOf(item), difficulty = 1,
                optionGlosses = options.map { it.meaning },
            )
        }
        // Producing the word strengthens memory more than picking it from a list.
        items.take(4).forEach { item ->
            exercises += Exercise.Translate("${fullId}${WORD_RECALL_MARK}${item.id}", Skill.VOCABULARY, Instr.recall, item.meaning, Recall.acceptedAnswers(item), difficulty = 2)
        }
        items.drop(2).take(if (lang == LanguageCode.MANDARIN) 2 else 1).forEachIndexed { i, item ->
            val options = rotate(listOf(item) + distractors(item, 2), i + 2)
            exercises += Exercise.MultipleChoice(
                "${fullId}_listen_${item.id}", Skill.LISTENING, Instr.listen, prompt = "🔊",
                options = options.map { it.term }, correctIndex = options.indexOf(item), difficulty = 2, audioText = item.term,
            )
        }
        if (lang == LanguageCode.MANDARIN) {
            items.filter { it.term.length == 1 && it.pronunciation != null }.take(2).forEach { item ->
                val syl = Pinyin.parseSyllable(item.pronunciation!!)
                exercises += Exercise.ToneChoice("${fullId}_tone_${item.id}", Skill.PRONUNCIATION, Instr.tone, syl.base, syl.tone, item.term, item.meaning)
            }
        }
        // Mandarin is written without spaces, so its sentences cannot be split here; its hand-written exercises cover this.
        val sentences = if (lang == LanguageCode.MANDARIN) emptyList() else items.filter { it.example != null && it.exampleTranslation != null && it.example!!.split(' ').size in 3..9 }
        sentences.firstOrNull()?.let { item ->
            exercises += Exercise.Dictation("${fullId}_dict_${item.id}", Skill.LISTENING, Instr.dictation, item.example!!, listOf(item.example!!), 3)
        }
        sentences.drop(1).firstOrNull()?.let { item ->
            exercises += Exercise.SentenceOrder("${fullId}_order_${item.id}", Skill.GRAMMAR, Instr.order, item.example!!.split(' '), " ", item.exampleTranslation!!)
        }
        grammarId?.let { exercises += Exercise.GrammarNote("${fullId}_rule", Skill.GRAMMAR, Instr.grammarNote, "${lang.tag}_g_$it") }
        exercises += extra
        items.firstOrNull { it.example != null && it.exampleTranslation != null }?.let { item ->
            exercises += Exercise.SpeakRepeat("${fullId}_speak", Skill.SPEAKING, Instr.speak, item.example!!, item.pronunciation, item.exampleTranslation!!)
        }
        // Typing takes longer than tapping, so the time shown follows the exercises (an estimate, not a measurement).
        return Lesson(fullId, lang, level, "${lang.tag}_u_$unitId", title, summary, skill, maxOf(minutes, exercises.size / 2), items.map { it.id },
            grammarId?.let { "${lang.tag}_g_$it" }, exercises, prerequisites.map { "${lang.tag}_l_$it" })
    }

    private fun distractors(item: VocabularyItem, n: Int): List<VocabularyItem> {
        val pool = allVocab.filter { it.id != item.id && it.meaning.en != item.meaning.en && it.term != item.term }
        val sameTopic = pool.filter { it.topic == item.topic }
        val ordered = (sameTopic + pool.filter { it.topic != item.topic })
        val start = (item.id.hashCode() and 0x7fffffff) % maxOf(1, sameTopic.size.coerceAtLeast(1))
        return (ordered.drop(start) + ordered.take(start)).distinctBy { it.meaning.en }.take(n)
    }

    private fun <T> rotate(list: List<T>, k: Int): List<T> {
        if (list.isEmpty()) return list
        val r = k % list.size
        return list.drop(r) + list.take(r)
    }
}

internal fun fill(id: String, lang: LanguageCode, sentence: String, answers: List<String>, hint: Gloss? = null, explanation: Gloss? = null, difficulty: Int = 2, skill: Skill = Skill.GRAMMAR) =
    Exercise.FillInBlank("${lang.tag}_x_$id", skill, Instr.fill, sentence, answers, hint, difficulty, explanation)

internal fun order(id: String, lang: LanguageCode, tokens: List<String>, translation: Gloss, separator: String = " ", difficulty: Int = 2) =
    Exercise.SentenceOrder("${lang.tag}_x_$id", Skill.GRAMMAR, Instr.order, tokens, separator, translation, difficulty)

internal fun translate(id: String, lang: LanguageCode, source: Gloss, answers: List<String>, difficulty: Int = 3) =
    Exercise.Translate("${lang.tag}_x_$id", Skill.WRITING, Instr.translate, source, answers, difficulty)

internal fun dictation(id: String, lang: LanguageCode, text: String, alts: List<String> = emptyList(), difficulty: Int = 3) =
    Exercise.Dictation("${lang.tag}_x_$id", Skill.LISTENING, Instr.dictation, text, listOf(text) + alts, difficulty)

internal fun choice(id: String, lang: LanguageCode, prompt: String, options: List<String>, correct: Int, skill: Skill = Skill.GRAMMAR, explanation: Gloss? = null, difficulty: Int = 2) =
    Exercise.MultipleChoice("${lang.tag}_x_$id", skill, Instr.choose, prompt, options, correct, difficulty, explanation)

internal fun writing(id: String, lang: LanguageCode, prompt: Gloss, minWords: Int, suggested: List<String>) =
    Exercise.FreeWriting("${lang.tag}_x_$id", Skill.WRITING, Instr.write, prompt, minWords, suggested)
