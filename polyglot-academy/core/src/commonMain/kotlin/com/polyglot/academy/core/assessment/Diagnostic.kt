package com.polyglot.academy.core.assessment

import com.polyglot.academy.core.model.AssessmentKind
import com.polyglot.academy.core.model.AssessmentResult
import com.polyglot.academy.core.model.CefrLevel
import com.polyglot.academy.core.model.Exercise
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.Skill

data class AssessmentItem(
    val id: String,
    val language: LanguageCode,
    val level: CefrLevel,
    val skill: Skill,
    val question: Exercise.MultipleChoice,
)

/**
 * Adaptive staircase placement test.
 *  - starts at the self-reported level (or A1)
 *  - two consecutive correct answers at a level move up one level; one wrong answer moves down
 *  - stops after [maxItems] answers, or earlier once a level boundary has been confirmed twice
 *  - the estimate is the highest level answered with >= 60% accuracy on at least two items
 *  - the result reports a range; it is an estimate, not a certified CEFR result
 */
class DiagnosticSession(
    private val language: LanguageCode,
    items: List<AssessmentItem>,
    startLevel: CefrLevel = CefrLevel.A1,
    private val maxItems: Int = 14,
    private val minItems: Int = 8,
) {
    private val bank = items.filter { it.language == language }.groupBy { it.level }.mapValues { it.value.toMutableList() }
    private val levels = bank.keys.sortedBy { it.ordinal }
    private var level: CefrLevel = levels.minByOrNull { kotlin.math.abs(it.ordinal - startLevel.ordinal) } ?: CefrLevel.A1
    private var streak = 0
    private var boundaryHits = 0
    private val answers = mutableListOf<Pair<AssessmentItem, Boolean>>()
    private val asked = mutableSetOf<String>()
    var current: AssessmentItem? = null
        private set

    init {
        require(levels.isNotEmpty()) { "No assessment items for $language" }
        current = pick()
    }

    val answeredCount: Int get() = answers.size
    val maxQuestions: Int get() = maxItems
    val isFinished: Boolean get() = current == null

    private fun pick(): AssessmentItem? {
        if (answers.size >= maxItems) return null
        if (answers.size >= minItems && boundaryHits >= 2) return null
        val candidatesByDistance = levels.sortedBy { kotlin.math.abs(it.ordinal - level.ordinal) }
        for (l in candidatesByDistance) {
            val pool = bank[l].orEmpty().filter { it.id !in asked }
            if (pool.isNotEmpty()) {
                // Rotate skills so the estimate is not driven by a single skill.
                val usedSkills = answers.groupingBy { it.first.skill }.eachCount()
                return pool.minBy { usedSkills[it.skill] ?: 0 }
            }
        }
        return null
    }

    fun answer(selectedIndex: Int): Boolean {
        val item = current ?: error("Assessment finished")
        val correct = selectedIndex == item.question.correctIndex
        answers += item to correct
        asked += item.id
        if (correct) {
            streak++
            if (streak >= 2) {
                val up = levels.firstOrNull { it.ordinal > level.ordinal }
                if (up != null) level = up else boundaryHits++
                streak = 0
            }
        } else {
            streak = 0
            val down = levels.lastOrNull { it.ordinal < level.ordinal }
            // Falling back from a level we just climbed to confirms the boundary.
            if (answers.size > 1) boundaryHits++
            if (down != null) level = down
        }
        current = pick()
        return correct
    }

    fun result(id: String, nowMillis: Long): AssessmentResult {
        val byLevel = answers.groupBy { it.first.level }
        val passed = byLevel.filter { (_, a) -> a.size >= 2 && a.count { it.second }.toDouble() / a.size >= 0.6 }.keys
        val single = byLevel.filter { (_, a) -> a.size == 1 && a.first().second }.keys
        val estimate = passed.maxByOrNull { it.ordinal }
            ?: single.minByOrNull { it.ordinal }
            ?: CefrLevel.PRE_A1
        val testedAbove = byLevel.keys.any { it.ordinal > estimate.ordinal }
        val evidence = byLevel[estimate]?.size ?: 0
        val low = if (evidence >= 3) estimate else estimate.previous()
        val high = if (testedAbove && byLevel.filterKeys { it.ordinal == estimate.ordinal + 1 }.values.flatten().none { it.second }) estimate else estimate.next()
        val skillScores = answers.groupBy { it.first.skill }.mapValues { (_, a) -> a.count { it.second }.toDouble() / a.size }
        return AssessmentResult(
            id = id, language = language, kind = AssessmentKind.DIAGNOSTIC, takenAtMillis = nowMillis,
            estimatedLevel = estimate, levelLow = low, levelHigh = high, skillScores = skillScores,
            questionsAnswered = answers.size, correctAnswers = answers.count { it.second },
        )
    }
}
