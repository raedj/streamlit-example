package com.polyglot.academy.core.model

import kotlinx.serialization.Serializable

/**
 * Text authored for explanations, available in each interface/explanation language.
 * English is mandatory; Arabic is provided for all built-in content.
 */
@Serializable
data class Gloss(val en: String, val ar: String) {
    fun forLanguage(code: LanguageCode): String = when (code) {
        LanguageCode.ARABIC -> ar
        else -> en
    }
}

@Serializable
enum class PartOfSpeech { NOUN, VERB, ADJECTIVE, ADVERB, PRONOUN, PREPOSITION, CONJUNCTION, INTERJECTION, NUMERAL, PHRASE, PARTICLE, MEASURE_WORD }

@Serializable
enum class Gender { MASCULINE, FEMININE, NEUTER }

/** A vocabulary entry. Built-in entries have ids prefixed with the language tag. */
@Serializable
data class VocabularyItem(
    val id: String,
    val language: LanguageCode,
    /** Written form. For Mandarin, simplified characters. */
    val term: String,
    val meaning: Gloss,
    val partOfSpeech: PartOfSpeech,
    val level: CefrLevel,
    val topic: String,
    /** IPA or pinyin (with tone marks) or Arabic transliteration. */
    val pronunciation: String? = null,
    val transliteration: String? = null,
    val example: String? = null,
    val exampleTranslation: Gloss? = null,
    val gender: Gender? = null,
    /** Mandarin only. */
    val traditional: String? = null,
    val tags: Set<String> = emptySet(),
    val related: List<String> = emptyList(),
    val source: VocabularySource = VocabularySource.BUILT_IN,
)

@Serializable
enum class VocabularySource { BUILT_IN, CONVERSATION, IMPORTED_MATERIAL, USER_ADDED, PROFESSIONAL_TRACK }

@Serializable
data class GrammarExample(val sentence: String, val translation: Gloss, val isIncorrect: Boolean = false)

@Serializable
data class GrammarTopic(
    val id: String,
    val language: LanguageCode,
    val level: CefrLevel,
    val title: Gloss,
    val rule: Gloss,
    val examples: List<GrammarExample>,
    val commonMistakes: List<Gloss>,
)

@Serializable
data class ReadingPassage(
    val id: String,
    val language: LanguageCode,
    val level: CefrLevel,
    val title: String,
    val titleTranslation: Gloss,
    /** Sentences in order; each can be explained or played individually. */
    val sentences: List<String>,
    val sentenceTranslations: List<Gloss>,
    /** Built-in vocabulary ids highlighted in this passage. */
    val highlightedVocabulary: List<String>,
    val questions: List<Exercise.MultipleChoice>,
)

/** One learning activity. Every exercise has an instruction, an answer mechanism and feedback. */
@Serializable
sealed interface Exercise {
    val id: String
    val skill: Skill
    val instruction: Gloss
    /** 1 (easiest) .. 5 (hardest) within the lesson level. */
    val difficulty: Int
    val explanation: Gloss?

    @Serializable
    data class MultipleChoice(
        override val id: String,
        override val skill: Skill,
        override val instruction: Gloss,
        val prompt: String,
        val options: List<String>,
        val correctIndex: Int,
        override val difficulty: Int = 1,
        override val explanation: Gloss? = null,
        /** Optional text to synthesize before showing options (listening comprehension). */
        val audioText: String? = null,
    ) : Exercise

    @Serializable
    data class FillInBlank(
        override val id: String,
        override val skill: Skill,
        override val instruction: Gloss,
        /** Sentence containing "___" where the answer goes. */
        val sentence: String,
        val acceptedAnswers: List<String>,
        val hint: Gloss? = null,
        override val difficulty: Int = 2,
        override val explanation: Gloss? = null,
    ) : Exercise

    @Serializable
    data class SentenceOrder(
        override val id: String,
        override val skill: Skill,
        override val instruction: Gloss,
        /** Correct order. Shuffled by the player. Joined with [separator]. */
        val tokens: List<String>,
        val separator: String = " ",
        val translation: Gloss,
        override val difficulty: Int = 2,
        override val explanation: Gloss? = null,
    ) : Exercise

    @Serializable
    data class Matching(
        override val id: String,
        override val skill: Skill,
        override val instruction: Gloss,
        /** Pairs of (target-language term, meaning in explanation language). */
        val pairs: List<Pair<String, Gloss>>,
        override val difficulty: Int = 1,
        override val explanation: Gloss? = null,
    ) : Exercise

    @Serializable
    data class Translate(
        override val id: String,
        override val skill: Skill,
        override val instruction: Gloss,
        /** Source sentence in the explanation language. */
        val source: Gloss,
        val acceptedAnswers: List<String>,
        override val difficulty: Int = 3,
        override val explanation: Gloss? = null,
    ) : Exercise

    @Serializable
    data class Dictation(
        override val id: String,
        override val skill: Skill,
        override val instruction: Gloss,
        val audioText: String,
        val acceptedAnswers: List<String>,
        override val difficulty: Int = 3,
        override val explanation: Gloss? = null,
    ) : Exercise

    @Serializable
    data class SpeakRepeat(
        override val id: String,
        override val skill: Skill,
        override val instruction: Gloss,
        val targetText: String,
        val pronunciationGuide: String? = null,
        val translation: Gloss,
        override val difficulty: Int = 2,
        override val explanation: Gloss? = null,
    ) : Exercise

    @Serializable
    data class ToneChoice(
        override val id: String,
        override val skill: Skill,
        override val instruction: Gloss,
        /** Syllable without tone, e.g. "ma". */
        val syllable: String,
        /** Correct tone 1..5 (5 = neutral). */
        val correctTone: Int,
        val character: String,
        val meaning: Gloss,
        override val difficulty: Int = 2,
        override val explanation: Gloss? = null,
    ) : Exercise

    @Serializable
    data class Flashcard(
        override val id: String,
        override val skill: Skill,
        override val instruction: Gloss,
        val vocabularyId: String,
        override val difficulty: Int = 1,
        override val explanation: Gloss? = null,
    ) : Exercise

    @Serializable
    data class FreeWriting(
        override val id: String,
        override val skill: Skill,
        override val instruction: Gloss,
        val prompt: Gloss,
        val minWords: Int,
        /** Words the learner is encouraged to use. */
        val suggestedVocabulary: List<String>,
        override val difficulty: Int = 3,
        override val explanation: Gloss? = null,
    ) : Exercise
}

@Serializable
data class Lesson(
    val id: String,
    val language: LanguageCode,
    val level: CefrLevel,
    val unitId: String,
    val title: Gloss,
    val summary: Gloss,
    val primarySkill: Skill,
    val estimatedMinutes: Int,
    val vocabularyIds: List<String>,
    val grammarTopicId: String?,
    val exercises: List<Exercise>,
    /** Lesson ids that should be mastered first. */
    val prerequisites: List<String> = emptyList(),
)

@Serializable
data class CourseUnit(
    val id: String,
    val language: LanguageCode,
    val level: CefrLevel,
    val title: Gloss,
    val description: Gloss,
    val lessonIds: List<String>,
    val trackId: String = GENERAL_TRACK,
) {
    companion object { const val GENERAL_TRACK = "general" }
}

@Serializable
data class RolePlayScenario(
    val id: String,
    val language: LanguageCode,
    val level: CefrLevel,
    val title: Gloss,
    val setting: Gloss,
    val learnerGoal: Gloss,
    val tutorOpening: String,
    val tutorOpeningTranslation: Gloss,
    /** Ordered beats the offline tutor walks through. */
    val beats: List<ScenarioBeat>,
    val usefulPhrases: List<String>,
    val trackId: String = CourseUnit.GENERAL_TRACK,
)

/**
 * One step of a scripted scenario: the offline tutor waits for a reply that contains any of the
 * [expectedKeywords] (case/diacritic-insensitive) and then says [tutorLine].
 */
@Serializable
data class ScenarioBeat(
    val expectedKeywords: List<String>,
    val hintForLearner: Gloss,
    val modelAnswer: String,
    val tutorLine: String,
    val tutorLineTranslation: Gloss,
)

/** Professional or specialised learning track (optional). */
@Serializable
data class LearningTrack(
    val id: String,
    val language: LanguageCode,
    val title: Gloss,
    val description: Gloss,
    val minimumLevel: CefrLevel,
    val disclaimer: Gloss?,
)
