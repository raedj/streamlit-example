package com.polyglot.academy.core.speaking

import com.polyglot.academy.core.model.CefrLevel
import com.polyglot.academy.core.model.Conversation
import com.polyglot.academy.core.model.ConversationTurn
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.util.toLocalDate
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

/** Counting words in learner speech. Mandarin has no spaces, so words are estimated from characters. */
object SpeechUnits {
    private val word = Regex("\\p{L}+(?:['’-]\\p{L}+)*")
    /** Average Mandarin word length in characters, used only to estimate word counts. */
    private const val HANZI_PER_WORD = 1.6

    fun tokens(text: String, language: LanguageCode): List<String> =
        if (language == LanguageCode.MANDARIN) text.filter { it in '一'..'鿿' }.map { it.toString() }
        else word.findAll(text).map { it.value.lowercase() }.toList()

    fun words(text: String, language: LanguageCode): Int {
        val t = tokens(text, language).size
        return if (language == LanguageCode.MANDARIN && t > 0) maxOf(1, kotlin.math.round(t / HANZI_PER_WORD).toInt()) else t
    }
}

enum class MissionGap { TOO_FEW_TURNS, ANSWERS_TOO_SHORT, TASKS_NOT_CONFIRMED }

data class MissionResult(
    val learnerTurns: Int,
    val medianWords: Int,
    val tasksConfirmed: Boolean,
    val passed: Boolean,
    val gaps: List<MissionGap>,
)

object MissionEvaluator {
    /**
     * A mission passes when the learner spoke enough, in long enough turns, and the tasks were
     * confirmed: by the AI tutor ("mission_complete") or by finishing the built-in role-play.
     */
    fun evaluate(mission: SpeakingMission, language: LanguageCode, learnerTurns: List<ConversationTurn>, tasksConfirmed: Boolean): MissionResult {
        val counts = learnerTurns.map { SpeechUnits.words(it.text, language) }
        val median = median(counts)
        val gaps = buildList {
            if (counts.size < mission.minTurns) add(MissionGap.TOO_FEW_TURNS)
            if (median < mission.minWordsPerTurn) add(MissionGap.ANSWERS_TOO_SHORT)
            if (!tasksConfirmed) add(MissionGap.TASKS_NOT_CONFIRMED)
        }
        return MissionResult(counts.size, median, tasksConfirmed, gaps.isEmpty(), gaps)
    }

    const val PASSED_MARK = "mission_passed=true"

    internal fun median(values: List<Int>): Int {
        if (values.isEmpty()) return 0
        val s = values.sorted()
        return if (s.size % 2 == 1) s[s.size / 2] else (s[s.size / 2 - 1] + s[s.size / 2]) / 2
    }
}

data class WeeklySpeaking(val weekStart: LocalDate, val learnerTurns: Int, val medianWords: Int)

/** Speaking measurements computed only from the learner's own recorded turns. Null means not enough data. */
data class FluencyReport(
    val learnerTurns: Int,
    val spokenShare: Double?,
    val medianWordsPerTurn: Int?,
    val longestTurnWords: Int?,
    /** Moving-average type/token ratio over 50-word windows (0..1); null below 50 words. */
    val lexicalVariety: Double?,
    val correctionsPerTurn: Double?,
    /** Median words per minute over spoken turns with timing; approximate (recognizer timing). */
    val wordsPerMinute: Int?,
    val weekly: List<WeeklySpeaking>,
)

object FluencyCalculator {
    private const val WINDOW = 50

    fun report(language: LanguageCode, turns: List<ConversationTurn>, today: LocalDate, weeks: Int = 8): FluencyReport {
        if (turns.isEmpty()) return FluencyReport(0, null, null, null, null, null, null, emptyList())
        val words = turns.map { SpeechUnits.words(it.text, language) }
        val tokens = turns.flatMap { SpeechUnits.tokens(it.text, language) }
        val variety = if (tokens.size < WINDOW) null else (0..tokens.size - WINDOW).map { i ->
            tokens.subList(i, i + WINDOW).toSet().size.toDouble() / WINDOW
        }.average()
        val rates = turns.zip(words).mapNotNull { (t, w) ->
            val ms = t.speakingMillis ?: return@mapNotNull null
            if (!t.wasSpoken || ms < 1_500 || w < 3) null else (w * 60_000.0 / ms).toInt()
        }
        val weekStarts = (0 until weeks).map { startOfWeek(today).minus(DatePeriod(days = 7 * it)) }.reversed()
        val weekly = weekStarts.map { start ->
            val inWeek = turns.zip(words).filter { (t, _) -> startOfWeek(t.createdAtMillis.toLocalDate()) == start }
            WeeklySpeaking(start, inWeek.size, MissionEvaluator.median(inWeek.map { it.second }))
        }
        return FluencyReport(
            learnerTurns = turns.size,
            spokenShare = turns.count { it.wasSpoken }.toDouble() / turns.size,
            medianWordsPerTurn = MissionEvaluator.median(words),
            longestTurnWords = words.max(),
            lexicalVariety = variety,
            correctionsPerTurn = turns.sumOf { it.corrections.count { c -> !c.isStylistic } }.toDouble() / turns.size,
            wordsPerMinute = rates.takeIf { it.size >= 3 }?.let { MissionEvaluator.median(it) },
            weekly = weekly,
        )
    }

    private fun startOfWeek(d: LocalDate): LocalDate = d.minus(DatePeriod(days = d.dayOfWeek.ordinal - DayOfWeek.MONDAY.ordinal))
}

data class MissionStatus(val mission: SpeakingMission, val attempts: Int, val passed: Boolean)

data class LevelProgress(val level: CefrLevel, val missions: List<MissionStatus>) {
    val passedCount: Int get() = missions.count { it.passed }
    val complete: Boolean get() = missions.all { it.passed }
}

object SpeakingProgressCalculator {
    fun levels(conversations: List<Conversation>): List<LevelProgress> {
        val byKey = conversations.groupBy { it.scenarioId }
        return SpeakingPath.levels.map { level ->
            LevelProgress(level, SpeakingPath.missions.filter { it.level == level }.map { m ->
                val list = byKey[m.conversationKey].orEmpty()
                MissionStatus(m, list.size, list.any { it.summary?.contains(MissionEvaluator.PASSED_MARK) == true })
            })
        }
    }

    /** The first mission not yet passed, in path order. */
    fun next(levels: List<LevelProgress>): SpeakingMission? = levels.flatMap { it.missions }.firstOrNull { !it.passed }?.mission
}
