package com.polyglot.academy.core.content

import com.polyglot.academy.core.model.Exercise
import com.polyglot.academy.core.model.Gloss
import com.polyglot.academy.core.model.WORD_RECALL_MARK

/**
 * One built-in item as the AI content check sees it: what an outside model needs to judge it, in plain text.
 * The list for a language is written to content/review/items/<lang>.curriculum.tsv (kept current by
 * ContentItemsFileTest) and read by tools/flag_content.py. The word lists are read from their own TSV files.
 */
data class CheckItem(val id: String, val kind: String, val level: String, val text: String)

object ContentCheck {
    /** Hand-written items only: lesson exercises built from the vocabulary are checked through the vocabulary. */
    fun items(c: LanguageCurriculum): List<CheckItem> = buildList {
        c.vocabulary.forEach { v ->
            add(CheckItem(v.id, "word", v.level.label, fields(
                "term" to v.term, "reading" to (v.pronunciation ?: v.transliteration), "traditional" to v.traditional,
                "part_of_speech" to v.partOfSpeech.name.lowercase(), "gender" to v.gender?.name?.lowercase(),
                "meaning" to gloss(v.meaning), "example" to v.example, "example_meaning" to v.exampleTranslation?.let(::gloss),
            )))
        }
        c.grammar.forEach { g ->
            add(CheckItem(g.id, "grammar", g.level.label, fields(
                "title" to gloss(g.title), "rule" to gloss(g.rule),
                "examples" to g.examples.joinToString(" / ") { (if (it.isIncorrect) "[marked incorrect] " else "") + it.sentence + " = " + gloss(it.translation) },
                "common_mistakes" to g.commonMistakes.joinToString(" / ") { gloss(it) },
            )))
        }
        val lessonLevel = c.lessons.flatMap { l -> l.exercises.map { it.id to l.level.label } }.toMap()
        c.lessons.flatMap { it.exercises }.filter { "_x_" in it.id }.distinctBy { it.id }.forEach { e ->
            exercise(e)?.let { add(CheckItem(e.id, "exercise", lessonLevel[e.id].orEmpty(), it)) }
        }
        (c.dictations + c.writingPrompts).forEach { e -> exercise(e)?.let { add(CheckItem(e.id, "exercise", "", it)) } }
        c.assessment.forEach { a -> exercise(a.question)?.let { add(CheckItem(a.question.id, "placement", a.level.label, it)) } }
        c.readings.filterNot { it.generated }.forEach { r ->
            add(CheckItem(r.id, "reading", r.level.label, fields(
                "title" to r.title + " = " + gloss(r.titleTranslation),
                "sentences" to r.sentences.indices.joinToString(" / ") { r.sentences[it] + " = " + gloss(r.sentenceTranslations[it]) },
                "glossary" to r.glossary.joinToString(" / ") { it.term + (it.reading?.let { p -> " ($p)" } ?: "") + " = " + gloss(it.meaning) },
                "questions" to r.questions.joinToString(" / ") { exercise(it).orEmpty() },
                "open_questions" to r.openQuestions.joinToString(" / ") { o ->
                    o.prompt + " = " + gloss(o.promptGloss) + " ; model answer: " + o.modelAnswer + " ; key words: " + o.keyPoints.joinToString(", ")
                }.ifEmpty { null },
            )))
        }
        c.scenarios.forEach { s ->
            add(CheckItem(s.id, "role_play", s.level.label, fields(
                "setting" to gloss(s.setting), "opening" to s.tutorOpening + " = " + gloss(s.tutorOpeningTranslation),
                "steps" to s.beats.joinToString(" / ") { "learner: " + it.modelAnswer + " ; tutor: " + it.tutorLine + " = " + gloss(it.tutorLineTranslation) },
                "useful_phrases" to s.usefulPhrases.joinToString(" / "),
            )))
        }
    }

    private fun exercise(e: Exercise): String? = when (e) {
        is Exercise.MultipleChoice -> fields(
            "question" to (e.promptGloss?.let(::gloss) ?: e.prompt), "sentence" to e.prompt.takeIf { e.promptGloss != null && "___" in it },
            "audio" to e.audioText,
            "options" to e.options.indices.joinToString(" / ") { (e.optionGlosses?.get(it)?.let(::gloss) ?: e.options[it]) },
            "correct" to (e.optionGlosses?.get(e.correctIndex)?.let(::gloss) ?: e.options[e.correctIndex]),
            "explanation" to e.explanation?.let(::gloss),
        )
        is Exercise.FillInBlank -> fields("sentence" to e.sentence, "accepted" to e.acceptedAnswers.joinToString(" / "),
            "hint" to e.hint?.let(::gloss), "explanation" to e.explanation?.let(::gloss))
        is Exercise.SentenceOrder -> fields("sentence" to e.tokens.joinToString(e.separator), "meaning" to gloss(e.translation))
        is Exercise.Translate -> fields("source" to gloss(e.source), "accepted" to e.acceptedAnswers.joinToString(" / "))
        is Exercise.Dictation -> fields("audio" to e.audioText, "accepted" to e.acceptedAnswers.joinToString(" / "))
        is Exercise.Matching -> fields("pairs" to e.pairs.joinToString(" / ") { it.first + " = " + gloss(it.second) })
        is Exercise.SpeakRepeat -> fields("sentence" to e.targetText, "reading" to e.pronunciationGuide, "meaning" to gloss(e.translation))
        is Exercise.ToneChoice -> fields("character" to e.character, "syllable" to e.syllable, "tone" to e.correctTone.toString(), "meaning" to gloss(e.meaning))
        is Exercise.WriteCharacter -> fields("character" to e.character, "word" to e.word, "reading" to e.reading, "meaning" to gloss(e.meaning), "explanation" to e.explanation?.let(::gloss))
        is Exercise.FreeWriting -> fields("prompt" to gloss(e.prompt), "suggested_words" to e.suggestedVocabulary.joinToString(" / "))
        is Exercise.Flashcard, is Exercise.GrammarNote -> null
    }

    private fun gloss(g: Gloss) = "en: ${g.en} | ar: ${g.ar}"

    private fun fields(vararg f: Pair<String, String?>) =
        f.filter { !it.second.isNullOrBlank() }.joinToString(" ¦ ") { "${it.first}: ${it.second}" }.replace(Regex("[\t\r\n]+"), " ")

    /**
     * The curriculum without the items in [disputed], and without what was built from them: a hidden word
     * takes its card, choice, recall, listening, tone, dictation and ordering exercises with it, a hidden
     * grammar topic takes its rule note. Lessons stay; if one is left without exercises, validate() says so.
     */
    fun without(c: LanguageCurriculum, disputed: Set<String>): LanguageCurriculum {
        if (disputed.isEmpty()) return c
        val goneWords = c.vocabulary.filter { it.id in disputed }
        val goneIds = goneWords.map { it.id }.toSet()
        val goneTerms = goneWords.map { it.term }.toSet()
        val goneExamples = goneWords.mapNotNull { it.example }.toSet()
        val goneGrammar = c.grammar.filter { it.id in disputed }.map { it.id }.toSet()
        fun keep(e: Exercise): Boolean = when {
            e.id in disputed -> false
            e is Exercise.Flashcard -> e.vocabularyId !in goneIds
            e is Exercise.GrammarNote -> e.topicId !in goneGrammar
            e is Exercise.SpeakRepeat -> e.targetText !in goneExamples
            e is Exercise.Matching -> e.pairs.count { it.first !in goneTerms } >= 2
            "_x_" in e.id -> true
            else -> goneIds.none { e.id.endsWith("_$it") || e.id.endsWith("$WORD_RECALL_MARK$it") }
        }
        fun trim(e: Exercise): Exercise = if (e is Exercise.Matching) e.copy(pairs = e.pairs.filter { it.first !in goneTerms }) else e
        val lessons = c.lessons.map { l ->
            l.copy(vocabularyIds = l.vocabularyIds - goneIds, grammarTopicId = l.grammarTopicId?.takeIf { it !in goneGrammar },
                exercises = l.exercises.filter(::keep).map(::trim))
        }
        val readings = c.readings.filter { it.id !in disputed }.map { r ->
            r.copy(highlightedVocabulary = r.highlightedVocabulary - goneIds, questions = r.questions.filter { it.id !in disputed })
        }
        return LanguageCurriculum(c.language, c.vocabulary.filter { it.id !in goneIds }, c.grammar.filter { it.id !in goneGrammar }, c.units, lessons,
            readings, c.scenarios.filter { it.id !in disputed }, c.assessment.filter { it.id !in disputed && it.question.id !in disputed }, c.tracks,
            c.dictations.filter { it.id !in disputed }, c.writingPrompts.filter { it.id !in disputed })
    }
}
