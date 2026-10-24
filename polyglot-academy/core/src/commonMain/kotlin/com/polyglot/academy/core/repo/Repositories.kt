package com.polyglot.academy.core.repo

import com.polyglot.academy.core.ai.ProviderConfig
import com.polyglot.academy.core.ai.ProviderId
import com.polyglot.academy.core.model.Achievement
import com.polyglot.academy.core.model.AppSettings
import com.polyglot.academy.core.model.AssessmentResult
import com.polyglot.academy.core.model.Bookmark
import com.polyglot.academy.core.model.Conversation
import com.polyglot.academy.core.model.ConversationTurn
import com.polyglot.academy.core.model.ExerciseAttempt
import com.polyglot.academy.core.model.ImportedMaterial
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.LearnerProfile
import com.polyglot.academy.core.model.LessonProgress
import com.polyglot.academy.core.model.Mistake
import com.polyglot.academy.core.model.Note
import com.polyglot.academy.core.model.ReviewLog
import com.polyglot.academy.core.model.ReviewState
import com.polyglot.academy.core.model.SavedPhrase
import com.polyglot.academy.core.model.StudySession
import com.polyglot.academy.core.model.VocabularyItem
import com.polyglot.academy.core.planner.DailyPlan
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface ProfileRepository {
    val profile: Flow<LearnerProfile>
    val settings: Flow<AppSettings>
    suspend fun currentProfile(): LearnerProfile
    suspend fun currentSettings(): AppSettings
    suspend fun updateProfile(transform: (LearnerProfile) -> LearnerProfile)
    suspend fun updateSettings(transform: (AppSettings) -> AppSettings)
}

interface ProgressRepository {
    fun attempts(language: LanguageCode): Flow<List<ExerciseAttempt>>
    suspend fun recentAttempts(language: LanguageCode, limit: Int): List<ExerciseAttempt>
    suspend fun recordAttempt(attempt: ExerciseAttempt)

    fun sessions(language: LanguageCode): Flow<List<StudySession>>
    suspend fun upsertSession(session: StudySession)

    fun lessonProgress(language: LanguageCode): Flow<Map<String, LessonProgress>>
    suspend fun saveLessonProgress(progress: LessonProgress)

    fun mistakes(language: LanguageCode): Flow<List<Mistake>>
    suspend fun recordMistake(mistake: Mistake)
    suspend fun resolveMistake(id: String)

    fun assessments(language: LanguageCode): Flow<List<AssessmentResult>>
    suspend fun saveAssessment(result: AssessmentResult)

    val achievements: Flow<List<Achievement>>
    suspend fun unlock(achievement: Achievement): Boolean
}

interface ReviewRepository {
    fun states(language: LanguageCode): Flow<List<ReviewState>>
    suspend fun state(itemId: String): ReviewState?
    suspend fun save(state: ReviewState, log: ReviewLog?)
    suspend fun reviewCount(language: LanguageCode): Int
}

interface VocabularyRepository {
    /** User-created or imported vocabulary (built-in entries come from the curriculum). */
    fun userVocabulary(language: LanguageCode): Flow<List<VocabularyItem>>
    suspend fun addVocabulary(item: VocabularyItem)
    suspend fun removeVocabulary(id: String)
    fun savedPhrases(language: LanguageCode): Flow<List<SavedPhrase>>
    suspend fun savePhrase(phrase: SavedPhrase)
    suspend fun removePhrase(id: String)
}

interface ConversationRepository {
    fun conversations(language: LanguageCode): Flow<List<Conversation>>
    fun turns(conversationId: String): Flow<List<ConversationTurn>>
    suspend fun upsertConversation(conversation: Conversation)
    suspend fun addTurn(turn: ConversationTurn)
    suspend fun learnerTurnCount(language: LanguageCode): Int
}

interface LibraryRepository {
    fun materials(language: LanguageCode?): Flow<List<ImportedMaterial>>
    suspend fun upsertMaterial(material: ImportedMaterial)
    suspend fun deleteMaterial(id: String)
    fun notes(): Flow<List<Note>>
    suspend fun upsertNote(note: Note)
    suspend fun deleteNote(id: String)
    fun bookmarks(): Flow<List<Bookmark>>
    suspend fun toggleBookmark(bookmark: Bookmark): Boolean
}

interface ProviderConfigRepository {
    val configs: Flow<Map<ProviderId, ProviderConfig>>
    suspend fun current(): Map<ProviderId, ProviderConfig>
    suspend fun save(config: ProviderConfig)
    val preferredProvider: Flow<ProviderId?>
    suspend fun setPreferred(id: ProviderId?)
}

interface PlanRepository {
    fun plan(language: LanguageCode, date: LocalDate): Flow<DailyPlan?>
    suspend fun savePlan(language: LanguageCode, plan: DailyPlan)
    suspend fun activeDates(language: LanguageCode): Set<LocalDate>
}

/** Secrets (API keys). Implementations must use platform protection (Android Keystore). */
interface SecretStore {
    suspend fun get(key: String): String?
    suspend fun put(key: String, value: String)
    suspend fun remove(key: String)
    suspend fun keys(): Set<String>
    /** Human-readable description of how secrets are protected on this platform. */
    val protectionDescription: String
}

/** Erases all user data across stores. Implemented by the data layer. */
interface DataLifecycle {
    suspend fun exportJson(includeConversations: Boolean, includeNotes: Boolean): String
    suspend fun deleteAllData()
    suspend fun deleteConversations()
    suspend fun deleteImportedMaterials()
}

/** Every repository in one place; implemented by the data layer. */
interface LearningStore : ProfileRepository, ProgressRepository, ReviewRepository, VocabularyRepository, ConversationRepository,
    LibraryRepository, ProviderConfigRepository, PlanRepository, DataLifecycle

/** Volatile secret store for tests and the desktop harness. Never use for real credentials on a device. */
class InMemorySecretStore : SecretStore {
    private val map = mutableMapOf<String, String>()
    override suspend fun get(key: String) = map[key]
    override suspend fun put(key: String, value: String) { map[key] = value }
    override suspend fun remove(key: String) { map.remove(key) }
    override suspend fun keys() = map.keys.toSet()
    override val protectionDescription = "In memory only (not persisted)"
}
