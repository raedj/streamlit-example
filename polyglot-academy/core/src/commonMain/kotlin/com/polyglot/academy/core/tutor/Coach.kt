package com.polyglot.academy.core.tutor

import com.polyglot.academy.core.content.Curriculum
import com.polyglot.academy.core.model.CoachTrack
import com.polyglot.academy.core.model.ConversationTurn
import com.polyglot.academy.core.model.Correction
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.Mistake
import com.polyglot.academy.core.model.ReviewState
import com.polyglot.academy.core.model.Skill
import com.polyglot.academy.core.model.VocabularyItem

/** What the coach should know about earlier sessions, computed from the learner's own data. */
data class CoachMemory(
    /** Items to quiz at the start of the session, e.g. "la cuenta — the bill". */
    val warmUp: List<String> = emptyList(),
    /** Recurring error types with an example, e.g. "tense (4×): I went → I have gone". */
    val recurringErrors: List<String> = emptyList(),
)

/**
 * The AI language-coach protocol: session structure, Socratic corrections, tracks, error
 * tracking and motivation. The model gets these as instructions; the app supplies the memory
 * (spaced-repetition items and the error log) and records every correction itself.
 */
object CoachProtocol {
    /** Error types the model must use, so the error log can group recurring mistakes. */
    val errorCategories = listOf("tense", "agreement", "word_order", "spelling", "vocabulary", "preposition", "article", "register", "other")

    fun rules(target: String, explain: String): String = buildString {
        appendLine("Coaching protocol (always):")
        appendLine("- Micro-learning: introduce at most one new language point or three new words in a reply.")
        appendLine("- When it fits, put the learner into a short realistic role-play so they use what they have just learned.")
        appendLine("- For each error give the original, the corrected form, a very short reason in $explain, and the register of the corrected form (formal, informal or neutral).")
        appendLine("- Classify each error as one of: ${errorCategories.joinToString()}.")
        appendLine("- Socratic step: for a minor error, do not give the answer in your reply. Ask a short guiding question that helps the learner find it, and put that question in the correction's \"hint\" field.")
        appendLine("- Always end your reply with exactly one specific question or one short practice task in $target.")
        appendLine("- Be encouraging, positive and patient, like a good private tutor.")
    }

    fun track(track: CoachTrack, language: LanguageCode): String = when (track) {
        CoachTrack.FLUENCY -> "Track: everyday fluency. Help the learner speak without hesitating, use common idioms and natural phrases, and practise everyday situations."
        CoachTrack.BUSINESS -> "Track: business and professional emails. Practise formal emails, short reports, meetings and negotiation, and the corporate register."
        CoachTrack.TRAVEL -> "Track: travel, tourism and hospitality. Practise quick practical phrases for airports, hotels, restaurants, emergencies and solving problems."
        CoachTrack.EXAM -> "Track: preparation for ${examName(language)}. Practise the four skills and academic vocabulary with exam-style tasks you write yourself. " +
            "They are not official exam material; any score you give is an estimate and must be called one."
    }

    fun examName(language: LanguageCode): String = when (language) {
        LanguageCode.ENGLISH -> "IELTS or TOEFL"
        LanguageCode.SPANISH -> "DELE or SIELE"
        LanguageCode.FRENCH -> "DELF or DALF"
        LanguageCode.MANDARIN -> "HSK"
        LanguageCode.ARABIC -> "an Arabic proficiency exam"
        LanguageCode.TURKISH -> "a Turkish proficiency exam such as TYS"
        LanguageCode.URDU -> "an Urdu proficiency exam"
        LanguageCode.MALAY -> "a Malay proficiency exam"
    }

    fun trackQuestion(language: LanguageCode): String =
        "This is the learner's first coaching session. Welcome them warmly and ask which track they want: everyday fluency, " +
            "business and professional emails, travel and hospitality, or preparation for ${examName(language)}. " +
            "When they choose, set \"track\" in your JSON to fluency, business, travel or exam (otherwise leave it empty)."

    fun warmUp(items: List<String>): String =
        "Start this session with one or two short quiz questions, one at a time, about items the learner studied before: ${items.joinToString("; ")}."

    fun recurring(errors: List<String>): String =
        "Error log (recurring mistakes): ${errors.joinToString("; ")}. From time to time, check one of them with a short task."

    const val STRUGGLING =
        "The learner seems to be struggling or frustrated. In this reply stop quizzing and add nothing new: encourage them warmly, " +
            "simplify what you said, and use an example from everyday life."

    fun parseTrack(value: String?): CoachTrack? = when (value?.trim()?.lowercase()) {
        "fluency" -> CoachTrack.FLUENCY
        "business" -> CoachTrack.BUSINESS
        "travel" -> CoachTrack.TRAVEL
        "exam" -> CoachTrack.EXAM
        else -> null
    }

    private val frustration = listOf(
        "i give up", "too hard", "so hard", "i can't", "i don't get it", "frustrat", "confus",
        "صعب", "لا أستطيع", "لا افهم شيئا", "لا أفهم شيئًا", "محبط", "استسلم",
        "difícil", "no puedo", "difficile", "je n'y arrive pas", "太难", "我不会",
    )

    /**
     * True when the learner's last message sounds frustrated, or when each of their last three
     * turns had at least two real (non-stylistic) errors.
     */
    fun isStruggling(learnerTurns: List<ConversationTurn>): Boolean {
        val last = learnerTurns.lastOrNull() ?: return false
        if (frustration.any { it in last.text.lowercase() }) return true
        val recent = learnerTurns.takeLast(3)
        return recent.size == 3 && recent.all { t -> t.corrections.count { !it.isStylistic } >= 2 }
    }

    /** Warm-up items: words due for review first, then the weakest recently reviewed ones. */
    fun warmUpItems(language: LanguageCode, states: List<ReviewState>, extraVocabulary: List<VocabularyItem>, now: Long, limit: Int = 3): List<String> {
        val vocab = Curriculum.of(language).vocabularyById + extraVocabulary.associateBy { it.id }
        return states.filter { it.lastReviewedAtMillis != null && it.itemId in vocab }
            .sortedWith(compareBy<ReviewState>({ it.dueAtMillis > now }, { it.ease }))
            .take(limit)
            .map { s -> vocab.getValue(s.itemId).let { "${it.term} — ${it.meaning.en}" } }
    }

    /** The most frequent unresolved error types from the error log. */
    fun recurringErrors(mistakes: List<Mistake>, limit: Int = 3): List<String> =
        mistakes.filter { !it.resolved && it.occurrences >= 2 }
            .sortedByDescending { it.occurrences }
            .take(limit)
            .map { "${it.category} (${it.occurrences}×): ${it.userAnswer} → ${it.correctAnswer}" }

    /** Error-log entry for a conversation correction; repeated errors of one type accumulate. */
    fun mistakeFor(correction: Correction, language: LanguageCode, id: String, now: Long): Mistake? {
        if (correction.isStylistic) return null
        val category = correction.category?.takeIf { it in errorCategories } ?: "other"
        return Mistake(id, language, Skill.SPEAKING, "conversation:$category", correction.original, correction.original, correction.corrected,
            correction.explanation.ifBlank { null }, correction.severity, category, now)
    }
}

/** Encouraging titles earned from real progress (not from time spent or logins). */
enum class CoachTitle { FIRST_STEPS, WORD_EXPLORER, CONVERSATION_STARTER, PHRASING_PRO, MISSION_MASTER, FLUENT_VOICE }

data class CoachStats(
    val wordsInReview: Int,
    val conversations: Int,
    val recentCorrectionsPerTurn: Double?,
    val missionsPassed: Int,
    val medianWordsPerTurn: Int?,
)

object CoachTitles {
    /** The highest title reached, and the next one with what it still needs (null at the top). */
    fun current(st: CoachStats): CoachTitle = CoachTitle.entries.last { reached(it, st) }

    fun next(st: CoachStats): CoachTitle? = CoachTitle.entries.firstOrNull { !reached(it, st) }

    /** Titles are earned in order: each one also needs every title before it. */
    fun reached(t: CoachTitle, st: CoachStats): Boolean = CoachTitle.entries.take(t.ordinal + 1).all { own(it, st) }

    private fun own(t: CoachTitle, st: CoachStats): Boolean = when (t) {
        CoachTitle.FIRST_STEPS -> true
        CoachTitle.WORD_EXPLORER -> st.wordsInReview >= 25
        CoachTitle.CONVERSATION_STARTER -> st.wordsInReview >= 25 && st.conversations >= 5
        CoachTitle.PHRASING_PRO -> st.conversations >= 10 && (st.recentCorrectionsPerTurn ?: 1.0) <= 0.3
        CoachTitle.MISSION_MASTER -> st.conversations >= 10 && st.missionsPassed >= 10
        CoachTitle.FLUENT_VOICE -> st.missionsPassed >= 20 && (st.medianWordsPerTurn ?: 0) >= 12
    }
}
