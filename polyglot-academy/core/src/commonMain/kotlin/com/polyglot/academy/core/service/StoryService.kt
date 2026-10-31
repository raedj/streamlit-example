package com.polyglot.academy.core.service

import com.polyglot.academy.core.ai.AiCapability
import com.polyglot.academy.core.ai.AiError
import com.polyglot.academy.core.ai.AiOutcome
import com.polyglot.academy.core.ai.ChatMessage
import com.polyglot.academy.core.ai.ChatRequest
import com.polyglot.academy.core.ai.ChatRole
import com.polyglot.academy.core.content.Curriculum
import com.polyglot.academy.core.model.CefrLevel
import com.polyglot.academy.core.model.DataCategory
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.PartOfSpeech
import com.polyglot.academy.core.model.ReadingPassage
import com.polyglot.academy.core.model.VocabularyItem
import com.polyglot.academy.core.model.VocabularySource
import com.polyglot.academy.core.repo.ReviewRepository
import com.polyglot.academy.core.repo.StoryRepository
import com.polyglot.academy.core.repo.VocabularyRepository
import com.polyglot.academy.core.stories.StoryGenerator
import com.polyglot.academy.core.stories.WordInfo
import com.polyglot.academy.core.util.IdGenerator
import com.polyglot.academy.core.util.TimeSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/** Built-in graded stories, AI-written stories about the learner's interests, and tap-to-save words. */
class StoryService(
    private val stories: StoryRepository,
    private val vocabulary: VocabularyRepository,
    private val reviewStates: ReviewRepository,
    private val reviews: ReviewService,
    private val providers: ProviderService,
    private val clock: TimeSource,
    private val ids: IdGenerator,
) {
    fun all(language: LanguageCode): Flow<List<ReadingPassage>> =
        stories.stories(language).map { generated -> Curriculum.of(language).readings.sortedBy { it.level } + generated }

    suspend fun find(id: String): ReadingPassage? =
        Curriculum.reading(id)
            ?: LanguageCode.entries.firstNotNullOfOrNull { l -> stories.stories(l).first().firstOrNull { it.id == id } }

    /** All vocabulary a story's words can be looked up in: built-in plus the learner's own. */
    suspend fun lookupVocabulary(language: LanguageCode): List<VocabularyItem> =
        Curriculum.of(language).vocabulary + vocabulary.userVocabulary(language).first()

    suspend fun generate(language: LanguageCode, explanation: LanguageCode, level: CefrLevel, rawInterest: String): Result<ReadingPassage> {
        val interest = StoryGenerator.cleanInterest(rawInterest) ?: return Result.failure(IllegalArgumentException("empty_interest"))
        val known = reviewStates.states(language).first().filter { it.lastReviewedAtMillis != null }.mapNotNull { Curriculum.of(language).vocabularyById[it.itemId]?.term }
        val request = ChatRequest(
            StoryGenerator.systemPrompt(language, explanation, level, interest, known),
            listOf(ChatMessage(ChatRole.USER, "Write the story now.")),
            setOf(DataCategory.LESSON_CONTENT), maxOutputTokens = 2_500, temperature = 0.8, expectJson = true,
        )
        return when (val outcome = providers.execute(AiCapability.STRUCTURED_FEEDBACK, request)) {
            is AiOutcome.Failure -> Result.failure(outcome.error)
            is AiOutcome.Success -> when (val parsed = StoryGenerator.parse(outcome.response.text, ids.newId("story"), language, level, interest, clock.nowMillis())) {
                is StoryGenerator.Parsed.Invalid -> Result.failure(AiError.InvalidResponse(outcome.response.providerId, parsed.reason))
                is StoryGenerator.Parsed.Ok -> { stories.saveStory(parsed.story); Result.success(parsed.story) }
            }
        }
    }

    suspend fun delete(story: ReadingPassage) { if (story.generated) stories.deleteStory(story.id) }

    /** Adds a tapped word to the review queue, creating the learner's own vocabulary item when it is not built in. */
    suspend fun saveWord(story: ReadingPassage, word: WordInfo, example: String?): String {
        val language = story.language
        val id = word.vocabularyId ?: vocabulary.userVocabulary(language).first().firstOrNull { it.term.equals(word.term, ignoreCase = true) }?.id ?: run {
            val item = VocabularyItem(
                id = ids.newId("uv"), language = language, term = word.term, meaning = word.meaning, partOfSpeech = PartOfSpeech.PHRASE,
                level = story.level, topic = "story", pronunciation = word.reading, example = example,
                source = VocabularySource.USER_ADDED, tags = setOf("story:${story.id}"),
            )
            vocabulary.addVocabulary(item)
            item.id
        }
        reviews.addToReview(language, id)
        return id
    }
}
