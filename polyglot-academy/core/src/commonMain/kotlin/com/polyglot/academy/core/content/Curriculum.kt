package com.polyglot.academy.core.content

import com.polyglot.academy.core.assessment.AssessmentItem
import com.polyglot.academy.core.learning.LessonSequencer
import com.polyglot.academy.core.model.CourseUnit
import com.polyglot.academy.core.model.Exercise
import com.polyglot.academy.core.model.GrammarTopic
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.LearningTrack
import com.polyglot.academy.core.model.Lesson
import com.polyglot.academy.core.model.ReadingPassage
import com.polyglot.academy.core.model.RolePlayScenario
import com.polyglot.academy.core.model.VocabularyItem

/** All built-in learning content for one language. Content is original and authored for this app. */
class LanguageCurriculum(
    val language: LanguageCode,
    val vocabulary: List<VocabularyItem>,
    val grammar: List<GrammarTopic>,
    val units: List<CourseUnit>,
    val lessons: List<Lesson>,
    val readings: List<ReadingPassage>,
    val scenarios: List<RolePlayScenario>,
    val assessment: List<AssessmentItem>,
    val tracks: List<LearningTrack> = emptyList(),
    val dictations: List<Exercise.Dictation> = emptyList(),
    val writingPrompts: List<Exercise.FreeWriting> = emptyList(),
) {
    val vocabularyById: Map<String, VocabularyItem> = vocabulary.associateBy { it.id }
    val lessonsById: Map<String, Lesson> = lessons.associateBy { it.id }
    val grammarById: Map<String, GrammarTopic> = grammar.associateBy { it.id }
    val sequencer: LessonSequencer by lazy { LessonSequencer(units.filter { it.trackId == CourseUnit.GENERAL_TRACK }, lessonsById) }

    fun unitsForTrack(trackId: String) = units.filter { it.trackId == trackId }
    fun scenariosForTrack(trackId: String) = scenarios.filter { it.trackId == trackId }

    /** Integrity problems (dangling ids, invalid answers). Must be empty; enforced by tests. */
    fun validate(): List<String> = buildList {
        val vocabIds = vocabularyById.keys
        vocabulary.groupBy { it.id }.filter { it.value.size > 1 }.keys.forEach { add("duplicate vocabulary id $it") }
        lessons.groupBy { it.id }.filter { it.value.size > 1 }.keys.forEach { add("duplicate lesson id $it") }
        units.forEach { u -> u.lessonIds.filter { it !in lessonsById }.forEach { add("unit ${u.id} references missing lesson $it") } }
        lessons.forEach { l ->
            if (units.none { l.id in it.lessonIds }) add("lesson ${l.id} is not in any unit")
            l.vocabularyIds.filter { it !in vocabIds }.forEach { add("lesson ${l.id} references missing vocab $it") }
            l.grammarTopicId?.let { if (it !in grammarById) add("lesson ${l.id} references missing grammar $it") }
            l.prerequisites.filter { it !in lessonsById }.forEach { add("lesson ${l.id} missing prerequisite $it") }
            if (l.exercises.isEmpty()) add("lesson ${l.id} has no exercises")
            l.exercises.forEach { e -> validateExercise(e)?.let { add("${l.id}/${e.id}: $it") } }
        }
        readings.forEach { r ->
            if (r.sentences.size != r.sentenceTranslations.size) add("reading ${r.id} sentence/translation count mismatch")
            r.highlightedVocabulary.filter { it !in vocabIds }.forEach { add("reading ${r.id} missing vocab $it") }
            r.questions.forEach { q -> validateExercise(q)?.let { add("${r.id}/${q.id}: $it") } }
            val text = com.polyglot.academy.core.stories.StoryText.normalize(r.sentences.joinToString(" "))
            r.glossary.filter { com.polyglot.academy.core.stories.StoryText.normalize(it.term) !in text }.forEach { add("reading ${r.id} glossary term not in text: ${it.term}") }
            r.openQuestions.forEach { o ->
                if (o.keyPoints.isEmpty() || o.keyPoints.any { p -> p.split('|').any { it.isBlank() } }) add("reading ${r.id}/${o.id}: empty key point")
                if (o.prompt.isBlank() || o.modelAnswer.isBlank()) add("reading ${r.id}/${o.id}: empty question or model answer")
            }
            if (r.openQuestions.map { it.id }.toSet().size != r.openQuestions.size) add("reading ${r.id}: duplicate open question ids")
        }
        scenarios.forEach { s -> if (s.beats.isEmpty()) add("scenario ${s.id} has no beats") }
        val trackIds = tracks.map { it.id }.toSet() + CourseUnit.GENERAL_TRACK
        units.filter { it.trackId !in trackIds }.forEach { add("unit ${it.id} references unknown track ${it.trackId}") }
        scenarios.filter { it.trackId !in trackIds }.forEach { add("scenario ${it.id} references unknown track ${it.trackId}") }
        tracks.filter { unitsForTrack(it.id).isEmpty() }.forEach { add("track ${it.id} has no units") }
        assessment.forEach { a -> validateExercise(a.question)?.let { add("assessment ${a.id}: $it") } }
        if (assessment.map { it.level }.distinct().size < 3) add("assessment bank should span at least 3 levels")
    }

    private fun validateExercise(e: Exercise): String? = when (e) {
        is Exercise.MultipleChoice -> when {
            e.options.size < 2 -> "fewer than 2 options"
            e.correctIndex !in e.options.indices -> "correct index out of range"
            e.options.distinct().size != e.options.size -> "duplicate options"
            e.optionGlosses != null && e.optionGlosses.size != e.options.size -> "option glosses size mismatch"
            else -> null
        }
        is Exercise.FillInBlank -> if ("___" !in e.sentence) "missing blank" else if (e.acceptedAnswers.isEmpty()) "no answers" else null
        is Exercise.SentenceOrder -> if (e.tokens.size < 2) "too few tokens" else null
        is Exercise.Matching -> if (e.pairs.size < 2) "too few pairs" else null
        is Exercise.Translate -> if (e.acceptedAnswers.isEmpty()) "no answers" else null
        is Exercise.Dictation -> if (e.acceptedAnswers.isEmpty()) "no answers" else null
        is Exercise.Flashcard -> if (e.vocabularyId !in vocabularyById) "flashcard references missing vocab" else null
        is Exercise.ToneChoice -> if (e.correctTone !in 1..5) "invalid tone" else null
        is Exercise.GrammarNote -> if (e.topicId !in grammarById) "grammar note references missing topic" else null
        is Exercise.WriteCharacter -> when {
            e.character.length != 1 -> "write exercise needs exactly one character"
            com.polyglot.academy.core.mandarin.StrokeOrder.of(e.character[0]) == null -> "no stroke data for ${e.character}"
            e.character !in e.word -> "the word does not contain the character"
            else -> null
        }
        is Exercise.SpeakRepeat, is Exercise.FreeWriting -> null
    }
}

object Curriculum {
    /** Topics of the everyday and travel lessons, shown together as a phrasebook. */
    val phrasebookTopics = listOf("essentials", "emergency", "restaurant", "hotel", "transport", "sightseeing")

    fun phrasebook(code: LanguageCode, topic: String) = of(code).vocabulary.filter { it.topic == topic && it.id.removePrefix(code.tag + "_").startsWith("ev_") }

    // Each language is built on first use: building all eight at once took about 7 s on a slow phone.
    private val built: Map<LanguageCode, Lazy<LanguageCurriculum>> = LanguageCode.entries.associateWith { code -> lazy { build(code) } }

    // Items hidden after the AI content check are left out here, so no screen shows them.
    private fun build(code: LanguageCode): LanguageCurriculum = ContentCheck.without(unfilteredOf(code), ContentDisputes.of(code))

    /** The curriculum with nothing hidden: what the AI content check reads (content/review/items). Built anew on each call. */
    fun unfilteredOf(code: LanguageCode): LanguageCurriculum {
        val c = when (code) {
            LanguageCode.ENGLISH -> EnglishCurriculum.build()
            LanguageCode.MANDARIN -> MandarinCurriculum.build()
            LanguageCode.SPANISH -> SpanishCurriculum.build()
            LanguageCode.FRENCH -> FrenchCurriculum.build()
            LanguageCode.ARABIC -> ArabicCurriculum.build()
            LanguageCode.TURKISH -> TurkishCurriculum.build()
            LanguageCode.URDU -> UrduCurriculum.build()
            LanguageCode.MALAY -> MalayCurriculum.build()
        }
        check(c.language == code)
        return LanguageCurriculum(c.language, c.vocabulary, c.grammar, c.units, c.lessons, (c.readings + GradedStories.of(c.language) + ReadingLibrary.of(c.language) + AdvancedLessons.readings(c.language)).map(ReadingLibrary::withOpenQuestions), c.scenarios,
            c.assessment, c.tracks, c.dictations, c.writingPrompts)
    }

    /** Every language's curriculum. This builds all eight, so use [of], [lesson], [grammar] or [reading] where possible. */
    val all: Map<LanguageCode, LanguageCurriculum> get() = built.mapValues { it.value.value }

    fun of(code: LanguageCode): LanguageCurriculum = built.getValue(code).value

    /** Content ids start with the language tag ("zh_l_greetings"), so only that language is built. */
    private fun <T> find(id: String, pick: (LanguageCurriculum) -> T?): T? =
        LanguageCode.fromTag(id.substringBefore('_'))?.let { pick(of(it)) } ?: all.values.firstNotNullOfOrNull(pick)

    fun lesson(id: String): Lesson? = find(id) { it.lessonsById[id] }
    fun grammar(id: String): GrammarTopic? = find(id) { it.grammarById[id] }
    fun reading(id: String): ReadingPassage? = find(id) { c -> c.readings.firstOrNull { it.id == id } }
}
