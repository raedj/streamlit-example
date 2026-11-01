package com.polyglot.academy.core.content

import com.polyglot.academy.core.model.CefrLevel
import com.polyglot.academy.core.model.CourseUnit
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.speaking.SpeakingPath
import com.polyglot.academy.core.wordlist.WordLevels

/** What the app offers at one CEFR level for one language. */
data class LevelOffer(
    val level: CefrLevel,
    /** Built-in lessons (general course and professional tracks); generated word-list lessons are counted in [wordLevels]. */
    val lessons: Int,
    /** Of [lessons], those in the general course. */
    val courseLessons: Int,
    /** Reading texts and graded stories. */
    val texts: Int,
    val grammarTopics: Int,
    val speakingMissions: Int,
    /** Word-list levels whose approximate band is this level. */
    val wordLevels: List<Int>,
) {
    val isEmpty: Boolean get() = lessons == 0 && texts == 0 && grammarTopics == 0 && speakingMissions == 0 && wordLevels.isEmpty()
}

/**
 * The level coverage of the built-in content, counted from the content itself so the app never claims
 * more than it has. Built-in lessons are concentrated at A1–A2 and thin out from B1; there is no C1 or
 * C2 course (see docs/PRODUCT_REQUIREMENTS.md, PR-26).
 */
object LevelCoverage {
    val levels = listOf(CefrLevel.A1, CefrLevel.A2, CefrLevel.B1, CefrLevel.B2, CefrLevel.C1, CefrLevel.C2)

    /** A level counts as having a course when the general course has at least this many lessons there. */
    const val COURSE_MINIMUM = 2

    fun of(language: LanguageCode): List<LevelOffer> {
        val c = Curriculum.of(language)
        val course = c.units.filter { it.trackId == CourseUnit.GENERAL_TRACK }.flatMap { it.lessonIds }.mapNotNull { c.lessonsById[it] }
        return levels.map { level ->
            LevelOffer(
                level = level,
                lessons = c.lessons.count { it.level == level },
                courseLessons = course.count { it.level == level },
                texts = c.readings.count { it.level == level },
                grammarTopics = c.grammar.count { it.level == level },
                speakingMissions = SpeakingPath.missions.count { it.level == level },
                wordLevels = (1..WordLevels.COUNT).filter { WordLevels.cefr(it) == level },
            )
        }
    }

    /** The highest level with a general course of at least [COURSE_MINIMUM] lessons. */
    fun courseCeiling(language: LanguageCode): CefrLevel =
        of(language).lastOrNull { it.courseLessons >= COURSE_MINIMUM }?.level ?: CefrLevel.A1

    /** True when a learner at [level] is above the structured course for [language]. */
    fun isAboveCourse(language: LanguageCode, level: CefrLevel): Boolean = level > courseCeiling(language)
}
