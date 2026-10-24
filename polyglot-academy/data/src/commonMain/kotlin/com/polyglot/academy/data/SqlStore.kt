package com.polyglot.academy.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import app.cash.sqldelight.db.SqlDriver
import com.polyglot.academy.core.ai.ProviderConfig
import com.polyglot.academy.core.ai.ProviderId
import com.polyglot.academy.core.model.Achievement
import com.polyglot.academy.core.model.AppSettings
import com.polyglot.academy.core.model.AssessmentKind
import com.polyglot.academy.core.model.AssessmentResult
import com.polyglot.academy.core.model.Bookmark
import com.polyglot.academy.core.model.BookmarkKind
import com.polyglot.academy.core.model.CefrLevel
import com.polyglot.academy.core.model.Conversation
import com.polyglot.academy.core.model.ConversationTurn
import com.polyglot.academy.core.model.Correction
import com.polyglot.academy.core.model.ErrorSeverity
import com.polyglot.academy.core.model.ExerciseAttempt
import com.polyglot.academy.core.model.ImportedMaterial
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.LearnerProfile
import com.polyglot.academy.core.model.LessonProgress
import com.polyglot.academy.core.model.MaterialStatus
import com.polyglot.academy.core.model.MaterialType
import com.polyglot.academy.core.model.Mistake
import com.polyglot.academy.core.model.Note
import com.polyglot.academy.core.model.ReviewLog
import com.polyglot.academy.core.model.ReviewPhase
import com.polyglot.academy.core.model.ReviewState
import com.polyglot.academy.core.model.SavedPhrase
import com.polyglot.academy.core.model.SessionKind
import com.polyglot.academy.core.model.Skill
import com.polyglot.academy.core.model.Speaker
import com.polyglot.academy.core.model.StudySession
import com.polyglot.academy.core.model.VocabularyItem
import com.polyglot.academy.core.planner.DailyPlan
import com.polyglot.academy.core.repo.ConversationRepository
import com.polyglot.academy.core.repo.DataLifecycle
import com.polyglot.academy.core.repo.LibraryRepository
import com.polyglot.academy.core.repo.PlanRepository
import com.polyglot.academy.core.repo.ProfileRepository
import com.polyglot.academy.core.repo.ProgressRepository
import com.polyglot.academy.core.repo.ProviderConfigRepository
import com.polyglot.academy.core.repo.ReviewRepository
import com.polyglot.academy.core.repo.VocabularyRepository
import com.polyglot.academy.core.util.toLocalDate
import com.polyglot.academy.data.db.PolyglotDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.put
import kotlin.coroutines.CoroutineContext

private const val KEY_PROFILE = "profile"
private const val KEY_SETTINGS = "settings"
private const val KEY_PREFERRED_PROVIDER = "preferred_provider"

internal val storeJson = Json { ignoreUnknownKeys = true; encodeDefaults = true }

private fun Boolean.toLong() = if (this) 1L else 0L

/**
 * SQLite-backed implementation of every repository. All database work runs on [io] so the UI
 * thread never touches storage.
 */
class SqlStore(
    driver: SqlDriver,
    private val io: CoroutineContext,
    private val zone: TimeZone = TimeZone.currentSystemDefault(),
    private val clock: com.polyglot.academy.core.util.TimeSource = com.polyglot.academy.core.util.TimeSource.System,
) : ProfileRepository, ProgressRepository, ReviewRepository, VocabularyRepository, ConversationRepository,
    LibraryRepository, ProviderConfigRepository, PlanRepository, DataLifecycle {

    val db = PolyglotDatabase(driver)
    private val kv = db.storeQueries
    private val pq = db.progressQueries
    private val rq = db.reviewQueries
    private val lq = db.libraryQueries
    private val cq = db.conversationQueries

    // ---------------- Profile & settings ----------------
    override val profile: Flow<LearnerProfile> =
        kv.getValue(KEY_PROFILE).asFlow().mapToOneOrNull(io).map { it?.let { s -> decodeOr(s, LearnerProfile()) } ?: LearnerProfile() }
    override val settings: Flow<AppSettings> =
        kv.getValue(KEY_SETTINGS).asFlow().mapToOneOrNull(io).map { it?.let { s -> decodeOr(s, AppSettings()) } ?: AppSettings() }

    private inline fun <reified T> decodeOr(s: String, fallback: T): T = runCatching { storeJson.decodeFromString<T>(s) }.getOrDefault(fallback)

    override suspend fun currentProfile(): LearnerProfile = withContext(io) {
        kv.getValue(KEY_PROFILE).executeAsOneOrNull()?.let { decodeOr(it, LearnerProfile()) } ?: LearnerProfile()
    }
    override suspend fun currentSettings(): AppSettings = withContext(io) {
        kv.getValue(KEY_SETTINGS).executeAsOneOrNull()?.let { decodeOr(it, AppSettings()) } ?: AppSettings()
    }
    override suspend fun updateProfile(transform: (LearnerProfile) -> LearnerProfile) = withContext(io) {
        db.transaction {
            val cur = kv.getValue(KEY_PROFILE).executeAsOneOrNull()?.let { decodeOr(it, LearnerProfile()) } ?: LearnerProfile()
            kv.putValue(KEY_PROFILE, storeJson.encodeToString(LearnerProfile.serializer(), transform(cur)))
        }
    }
    override suspend fun updateSettings(transform: (AppSettings) -> AppSettings) = withContext(io) {
        db.transaction {
            val cur = kv.getValue(KEY_SETTINGS).executeAsOneOrNull()?.let { decodeOr(it, AppSettings()) } ?: AppSettings()
            kv.putValue(KEY_SETTINGS, storeJson.encodeToString(AppSettings.serializer(), transform(cur)))
        }
    }

    // ---------------- Progress ----------------
    private fun mapAttempt(a: com.polyglot.academy.data.db.Exercise_attempt) = ExerciseAttempt(
        a.id, a.lesson_id, a.exercise_id, LanguageCode.valueOf(a.language), Skill.valueOf(a.skill), a.difficulty.toInt(), a.correct == 1L,
        a.score, a.response_ms, a.hints_used.toInt(), a.answered_at, a.user_answer,
    )

    override fun attempts(language: LanguageCode): Flow<List<ExerciseAttempt>> =
        pq.attemptsForLanguage(language.name).asFlow().mapToList(io).map { l -> l.map(::mapAttempt) }

    override suspend fun recentAttempts(language: LanguageCode, limit: Int) = withContext(io) {
        pq.recentAttempts(language.name, limit.toLong()).executeAsList().map(::mapAttempt)
    }

    override suspend fun recordAttempt(attempt: ExerciseAttempt) = withContext(io) {
        with(attempt) {
            pq.insertAttempt(id, lessonId, exerciseId, language.name, skill.name, difficulty.toLong(), correct.toLong(), score,
                responseMillis, hintsUsed.toLong(), answeredAtMillis, userAnswer)
        }
    }

    private fun mapSession(s: com.polyglot.academy.data.db.Study_session) = StudySession(
        s.id, LanguageCode.valueOf(s.language), SessionKind.valueOf(s.kind), s.reference_id, s.started_at, s.ended_at,
        s.active_seconds.toInt(), s.items_completed.toInt(), s.items_correct.toInt(), s.completed == 1L,
    )

    override fun sessions(language: LanguageCode): Flow<List<StudySession>> =
        pq.sessionsForLanguage(language.name).asFlow().mapToList(io).map { l -> l.map(::mapSession) }

    override suspend fun upsertSession(session: StudySession) = withContext(io) {
        with(session) {
            pq.upsertSession(id, language.name, kind.name, referenceId, startedAtMillis, endedAtMillis, activeSeconds.toLong(),
                itemsCompleted.toLong(), itemsCorrect.toLong(), completed.toLong())
        }
    }

    override fun lessonProgress(language: LanguageCode): Flow<Map<String, LessonProgress>> =
        pq.lessonProgressForLanguage(language.name).asFlow().mapToList(io).map { l ->
            l.associate { it.lesson_id to LessonProgress(it.lesson_id, LanguageCode.valueOf(it.language), it.best_score, it.completions.toInt(), it.last_completed_at, it.resume_index.toInt()) }
        }

    override suspend fun saveLessonProgress(progress: LessonProgress) = withContext(io) {
        with(progress) { pq.upsertLessonProgress(lessonId, language.name, bestScore, completions.toLong(), lastCompletedAtMillis, resumeIndex.toLong()) }
    }

    private fun mapMistake(m: com.polyglot.academy.data.db.Mistake) = Mistake(
        m.id, LanguageCode.valueOf(m.language), Skill.valueOf(m.skill), m.source_id, m.prompt, m.user_answer, m.correct_answer, m.explanation,
        ErrorSeverity.valueOf(m.severity), m.category, m.occurred_at, m.resolved == 1L, m.occurrences.toInt(),
    )

    override fun mistakes(language: LanguageCode): Flow<List<Mistake>> =
        pq.mistakesForLanguage(language.name).asFlow().mapToList(io).map { l -> l.map(::mapMistake) }

    /** Repeated mistakes on the same exercise increase its occurrence count instead of duplicating it. */
    override suspend fun recordMistake(mistake: Mistake) = withContext(io) {
        db.transaction {
            val existing = pq.mistakeBySource(mistake.language.name, mistake.sourceId).executeAsOneOrNull()
            if (existing != null) pq.bumpMistake(mistake.occurredAtMillis, mistake.userAnswer, existing.id)
            else with(mistake) {
                pq.insertMistake(id, language.name, skill.name, sourceId, prompt, userAnswer, correctAnswer, explanation, severity.name, category,
                    occurredAtMillis, resolved.toLong(), occurrences.toLong())
            }
        }
    }

    override suspend fun resolveMistake(id: String) = withContext(io) { pq.resolveMistake(id) }

    private val skillScoreSerializer = MapSerializer(Skill.serializer(), Double.serializer())

    override fun assessments(language: LanguageCode): Flow<List<AssessmentResult>> =
        pq.assessmentsForLanguage(language.name).asFlow().mapToList(io).map { l ->
            l.map {
                AssessmentResult(it.id, LanguageCode.valueOf(it.language), AssessmentKind.valueOf(it.kind), it.taken_at, CefrLevel.valueOf(it.estimated_level),
                    CefrLevel.valueOf(it.level_low), CefrLevel.valueOf(it.level_high), storeJson.decodeFromString(skillScoreSerializer, it.skill_scores_json),
                    it.questions_answered.toInt(), it.correct_answers.toInt())
            }
        }

    override suspend fun saveAssessment(result: AssessmentResult) = withContext(io) {
        with(result) {
            pq.insertAssessment(id, language.name, kind.name, takenAtMillis, estimatedLevel.name, levelLow.name, levelHigh.name,
                storeJson.encodeToString(skillScoreSerializer, skillScores), questionsAnswered.toLong(), correctAnswers.toLong())
        }
    }

    override val achievements: Flow<List<Achievement>> =
        pq.allAchievements().asFlow().mapToList(io).map { l -> l.map { Achievement(it.id, it.unlocked_at) } }

    override suspend fun unlock(achievement: Achievement): Boolean = withContext(io) {
        db.transactionWithResult {
            if (pq.achievementExists(achievement.id).executeAsOne() > 0) false
            else { pq.insertAchievement(achievement.id, achievement.unlockedAtMillis); true }
        }
    }

    // ---------------- Reviews ----------------
    private fun mapState(s: com.polyglot.academy.data.db.Review_state) = ReviewState(
        s.item_id, LanguageCode.valueOf(s.language), ReviewPhase.valueOf(s.phase), s.ease, s.interval_days, s.learning_step.toInt(),
        s.repetitions.toInt(), s.lapses.toInt(), s.last_reviewed_at, s.due_at, s.total_reviews.toInt(), s.correct_reviews.toInt(),
    )

    override fun states(language: LanguageCode): Flow<List<ReviewState>> =
        rq.statesForLanguage(language.name).asFlow().mapToList(io).map { l -> l.map(::mapState) }

    override suspend fun state(itemId: String): ReviewState? = withContext(io) { rq.stateFor(itemId).executeAsOneOrNull()?.let(::mapState) }

    override suspend fun save(state: ReviewState, log: ReviewLog?) = withContext(io) {
        db.transaction {
            with(state) {
                rq.upsertState(itemId, language.name, phase.name, ease, intervalDays, learningStep.toLong(), repetitions.toLong(), lapses.toLong(),
                    lastReviewedAtMillis, dueAtMillis, totalReviews.toLong(), correctReviews.toLong())
            }
            log?.let { rq.insertLog(it.itemId, state.language.name, it.reviewedAtMillis, it.rating.name, it.previousIntervalDays, it.newIntervalDays, it.responseMillis) }
        }
    }

    override suspend fun reviewCount(language: LanguageCode): Int = withContext(io) { rq.logCount(language.name).executeAsOne().toInt() }

    // ---------------- Vocabulary ----------------
    override fun userVocabulary(language: LanguageCode): Flow<List<VocabularyItem>> =
        lq.vocabularyForLanguage(language.name).asFlow().mapToList(io).map { l -> l.mapNotNull { runCatching { storeJson.decodeFromString(VocabularyItem.serializer(), it.item_json) }.getOrNull() } }

    override suspend fun addVocabulary(item: VocabularyItem) = withContext(io) {
        lq.upsertVocabulary(item.id, item.language.name, storeJson.encodeToString(VocabularyItem.serializer(), item), clock.nowMillis())
    }

    override suspend fun removeVocabulary(id: String) = withContext(io) { lq.deleteVocabulary(id) }

    override fun savedPhrases(language: LanguageCode): Flow<List<SavedPhrase>> =
        lq.phrasesForLanguage(language.name).asFlow().mapToList(io).map { l ->
            l.map { SavedPhrase(it.id, LanguageCode.valueOf(it.language), it.text, it.meaning, it.note, it.source_conversation_id, it.created_at) }
        }

    override suspend fun savePhrase(phrase: SavedPhrase) = withContext(io) {
        with(phrase) { lq.upsertPhrase(id, language.name, text, meaning, note, sourceConversationId, createdAtMillis) }
    }

    override suspend fun removePhrase(id: String) = withContext(io) { lq.deletePhrase(id) }

    // ---------------- Conversations ----------------
    private val correctionList = ListSerializer(Correction.serializer())
    private val stringList = ListSerializer(String.serializer())

    override fun conversations(language: LanguageCode): Flow<List<Conversation>> =
        cq.conversationsForLanguage(language.name).asFlow().mapToList(io).map { l -> l.map(::mapConversation) }

    private fun mapConversation(c: com.polyglot.academy.data.db.Conversation) =
        Conversation(c.id, LanguageCode.valueOf(c.language), c.tutor_id, c.scenario_id, c.started_at, c.ended_at, c.summary)

    private fun mapTurn(t: com.polyglot.academy.data.db.Conversation_turn) = ConversationTurn(
        t.id, t.conversation_id, Speaker.valueOf(t.speaker), t.text, t.translation, storeJson.decodeFromString(correctionList, t.corrections_json),
        storeJson.decodeFromString(stringList, t.alternatives_json), storeJson.decodeFromString(stringList, t.vocabulary_json), t.created_at, t.produced_by, t.was_spoken == 1L,
    )

    override fun turns(conversationId: String): Flow<List<ConversationTurn>> =
        cq.turnsFor(conversationId).asFlow().mapToList(io).map { l -> l.map(::mapTurn) }

    override suspend fun upsertConversation(conversation: Conversation) = withContext(io) {
        with(conversation) { cq.upsertConversation(id, language.name, tutorId, scenarioId, startedAtMillis, endedAtMillis, summary) }
    }

    override suspend fun addTurn(turn: ConversationTurn) = withContext(io) {
        with(turn) {
            cq.insertTurn(id, conversationId, speaker.name, text, translation, storeJson.encodeToString(correctionList, corrections),
                storeJson.encodeToString(stringList, alternatives), storeJson.encodeToString(stringList, vocabularyNotes), createdAtMillis, producedBy, wasSpoken.toLong())
        }
    }

    override suspend fun learnerTurnCount(language: LanguageCode): Int = withContext(io) { cq.learnerTurnCount(language.name).executeAsOne().toInt() }

    // ---------------- Library ----------------
    private fun mapMaterial(m: com.polyglot.academy.data.db.Imported_material) = ImportedMaterial(
        m.id, LanguageCode.valueOf(m.language), m.title, MaterialType.valueOf(m.type), m.mime_type, m.size_bytes, m.imported_at,
        MaterialStatus.valueOf(m.status), m.extracted_text, m.error_message, storeJson.decodeFromString(stringList, m.generated_vocabulary_json), m.cloud_processing_allowed == 1L,
    )

    override fun materials(language: LanguageCode?): Flow<List<ImportedMaterial>> =
        (if (language == null) lq.allMaterials() else lq.materialsForLanguage(language.name)).asFlow().mapToList(io).map { l -> l.map(::mapMaterial) }

    override suspend fun upsertMaterial(material: ImportedMaterial) = withContext(io) {
        with(material) {
            lq.upsertMaterial(id, language.name, title, type.name, mimeType, sizeBytes, importedAtMillis, status.name, extractedText, errorMessage,
                storeJson.encodeToString(stringList, generatedVocabularyIds), cloudProcessingAllowed.toLong())
        }
    }

    override suspend fun deleteMaterial(id: String) = withContext(io) { lq.deleteMaterial(id) }

    override fun notes(): Flow<List<Note>> = lq.allNotes().asFlow().mapToList(io).map { l ->
        l.map { Note(it.id, it.language?.let(LanguageCode::valueOf), it.title, it.body, it.created_at, it.updated_at) }
    }

    override suspend fun upsertNote(note: Note) = withContext(io) {
        with(note) { lq.upsertNote(id, language?.name, title, body, createdAtMillis, updatedAtMillis) }
    }

    override suspend fun deleteNote(id: String) = withContext(io) { lq.deleteNote(id) }

    override fun bookmarks(): Flow<List<Bookmark>> = lq.allBookmarks().asFlow().mapToList(io).map { l ->
        l.map { Bookmark(it.id, BookmarkKind.valueOf(it.kind), it.reference_id, it.created_at) }
    }

    override suspend fun toggleBookmark(bookmark: Bookmark): Boolean = withContext(io) {
        db.transactionWithResult {
            if (lq.bookmarkExists(bookmark.kind.name, bookmark.referenceId).executeAsOne() > 0) {
                lq.deleteBookmark(bookmark.kind.name, bookmark.referenceId); false
            } else {
                lq.insertBookmark(bookmark.id, bookmark.kind.name, bookmark.referenceId, bookmark.createdAtMillis, null); true
            }
        }
    }

    // ---------------- Providers ----------------
    override val configs: Flow<Map<ProviderId, ProviderConfig>> = lq.allProviderConfigs().asFlow().mapToList(io).map(::mapConfigs)

    private fun mapConfigs(rows: List<com.polyglot.academy.data.db.Provider_config>): Map<ProviderId, ProviderConfig> {
        val stored = rows.mapNotNull { r -> runCatching { storeJson.decodeFromString(ProviderConfig.serializer(), r.config_json) }.getOrNull() }.associateBy { it.id }
        return ProviderId.entries.associateWith { stored[it] ?: ProviderConfig(it) }
    }

    override suspend fun current(): Map<ProviderId, ProviderConfig> = withContext(io) { mapConfigs(lq.allProviderConfigs().executeAsList()) }

    override suspend fun save(config: ProviderConfig) = withContext(io) {
        lq.upsertProviderConfig(config.id.name, storeJson.encodeToString(ProviderConfig.serializer(), config))
    }

    override val preferredProvider: Flow<ProviderId?> =
        kv.getValue(KEY_PREFERRED_PROVIDER).asFlow().mapToOneOrNull(io).map { v -> v?.let { runCatching { ProviderId.valueOf(it) }.getOrNull() } }

    override suspend fun setPreferred(id: ProviderId?) = withContext(io) { kv.putValue(KEY_PREFERRED_PROVIDER, id?.name ?: "") }

    // ---------------- Plans ----------------
    override fun plan(language: LanguageCode, date: LocalDate): Flow<DailyPlan?> =
        pq.planFor(language.name, date.toString()).asFlow().mapToOneOrNull(io).map { it?.let { s -> runCatching { storeJson.decodeFromString(DailyPlan.serializer(), s) }.getOrNull() } }

    override suspend fun savePlan(language: LanguageCode, plan: DailyPlan) = withContext(io) {
        pq.upsertPlan(language.name, plan.date.toString(), storeJson.encodeToString(DailyPlan.serializer(), plan))
    }

    override suspend fun activeDates(language: LanguageCode): Set<LocalDate> = withContext(io) {
        pq.activeSessionDays(language.name).executeAsList().map { it.toLocalDate(zone) }.toSet()
    }

    // ---------------- Lifecycle ----------------
    override suspend fun exportJson(includeConversations: Boolean, includeNotes: Boolean): String = withContext(io) {
        val obj: JsonObject = buildJsonObject {
            put("format", "polyglot-academy-export")
            put("version", 1)
            put("profile", storeJson.encodeToJsonElement(LearnerProfile.serializer(), currentProfileSync()))
            put("settings", storeJson.encodeToJsonElement(AppSettings.serializer(), currentSettingsSync()))
            put("lessonProgress", JsonArray(pq.lessonProgressForLanguageAll().map { storeJson.encodeToJsonElement(LessonProgress.serializer(), it) }))
            put("assessments", JsonArray(LanguageCode.entries.flatMap { l -> pq.assessmentsForLanguage(l.name).executeAsList() }.map {
                buildJsonObject { put("language", it.language); put("takenAt", it.taken_at); put("estimatedLevel", it.estimated_level); put("low", it.level_low); put("high", it.level_high) }
            }))
            put("reviewStates", JsonArray(LanguageCode.entries.flatMap { l -> rq.statesForLanguage(l.name).executeAsList().map(::mapState) }
                .map { storeJson.encodeToJsonElement(ReviewState.serializer(), it) }))
            put("userVocabulary", JsonArray(LanguageCode.entries.flatMap { l -> lq.vocabularyForLanguage(l.name).executeAsList() }
                .map { storeJson.parseToJsonElement(it.item_json) }))
            put("savedPhrases", JsonArray(LanguageCode.entries.flatMap { l -> lq.phrasesForLanguage(l.name).executeAsList() }.map {
                buildJsonObject { put("language", it.language); put("text", it.text); put("meaning", it.meaning) }
            }))
            put("mistakes", JsonArray(LanguageCode.entries.flatMap { l -> pq.mistakesForLanguage(l.name).executeAsList().map(::mapMistake) }
                .map { storeJson.encodeToJsonElement(Mistake.serializer(), it) }))
            if (includeConversations) {
                put("conversations", JsonArray(cq.allConversations().executeAsList().map { storeJson.encodeToJsonElement(Conversation.serializer(), mapConversation(it)) }))
                put("conversationTurns", JsonArray(cq.allTurns().executeAsList().map { storeJson.encodeToJsonElement(ConversationTurn.serializer(), mapTurn(it)) }))
            }
            if (includeNotes) {
                put("notes", JsonArray(lq.allNotes().executeAsList().map { buildJsonObject { put("title", it.title); put("body", it.body); put("updatedAt", it.updated_at) } }))
            }
            put("note", "API keys and other secrets are never included in exports.")
        }
        storeJson.encodeToString(JsonObject.serializer(), obj)
    }

    private fun currentProfileSync() = kv.getValue(KEY_PROFILE).executeAsOneOrNull()?.let { decodeOr(it, LearnerProfile()) } ?: LearnerProfile()
    private fun currentSettingsSync() = kv.getValue(KEY_SETTINGS).executeAsOneOrNull()?.let { decodeOr(it, AppSettings()) } ?: AppSettings()

    private fun com.polyglot.academy.data.db.ProgressQueries.lessonProgressForLanguageAll(): List<LessonProgress> =
        LanguageCode.entries.flatMap { l -> lessonProgressForLanguage(l.name).executeAsList() }.map {
            LessonProgress(it.lesson_id, LanguageCode.valueOf(it.language), it.best_score, it.completions.toInt(), it.last_completed_at, it.resume_index.toInt())
        }

    override suspend fun deleteAllData() = withContext(io) {
        db.transaction {
            pq.deleteAllProgress()
            rq.deleteAllReviews()
            lq.deleteAllLibrary()
            cq.deleteAllConversations()
            kv.deleteAllValues()
        }
    }

    override suspend fun deleteConversations() = withContext(io) { cq.deleteAllConversations() }
    override suspend fun deleteImportedMaterials() = withContext(io) { lq.deleteAllMaterials() }
}
