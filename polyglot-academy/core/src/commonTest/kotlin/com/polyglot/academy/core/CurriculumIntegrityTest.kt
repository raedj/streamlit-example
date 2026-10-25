package com.polyglot.academy.core

import com.polyglot.academy.core.content.Curriculum
import com.polyglot.academy.core.learning.AnswerChecker
import com.polyglot.academy.core.model.Exercise
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.Skill
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CurriculumIntegrityTest {
    @Test fun allCurriculaValidate() {
        for ((lang, c) in Curriculum.all) {
            val problems = c.validate()
            assertTrue(problems.isEmpty(), "$lang curriculum problems:\n" + problems.joinToString("\n"))
        }
    }

    @Test fun everyLanguageHasCoreContent() {
        assertEquals(LanguageCode.entries.toSet(), Curriculum.all.keys)
        for ((lang, c) in Curriculum.all) {
            assertTrue(c.vocabulary.size >= 30, "$lang vocabulary ${c.vocabulary.size}")
            assertTrue(c.lessons.size >= 5, "$lang lessons")
            assertTrue(c.grammar.size >= 3, "$lang grammar")
            assertTrue(c.readings.isNotEmpty(), "$lang readings")
            assertTrue(c.scenarios.isNotEmpty(), "$lang scenarios")
            assertTrue(c.assessment.size >= 10, "$lang assessment")
            assertTrue(c.dictations.isNotEmpty(), "$lang dictations")
            assertTrue(c.vocabulary.all { it.id.startsWith(lang.tag + "_") }, "$lang id prefix")
        }
    }

    @Test fun lessonsMixExerciseTypesAndSkills() {
        for (c in Curriculum.all.values) for (l in c.lessons) {
            val types = l.exercises.map { it::class }.toSet()
            assertTrue(types.size >= 3, "${l.id} has only ${types.size} exercise types")
            assertTrue(l.exercises.map { it.skill }.toSet().size >= 2, "${l.id} trains only one skill")
        }
    }

    @Test fun ownAcceptedAnswersAreAcceptedByTheChecker() {
        for (c in Curriculum.all.values) for (l in c.lessons) for (e in l.exercises) {
            when (e) {
                is Exercise.FillInBlank -> e.acceptedAnswers.forEach { assertTrue(AnswerChecker.check(it, e.acceptedAnswers).isCorrect, e.id) }
                is Exercise.Dictation -> assertTrue(AnswerChecker.check(e.audioText, e.acceptedAnswers).isCorrect, e.id)
                is Exercise.SentenceOrder -> assertTrue(AnswerChecker.check(e.tokens.joinToString(e.separator), listOf(e.tokens.joinToString(e.separator))).isCorrect, e.id)
                else -> {}
            }
        }
    }

    @Test fun englishHasOptionalProfessionalTracks() {
        val en = Curriculum.of(LanguageCode.ENGLISH)
        assertEquals(setOf("business", "finance", "credit_risk", "legal", "career"), en.tracks.map { it.id }.toSet())
        en.tracks.forEach { t -> assertTrue(en.unitsForTrack(t.id).isNotEmpty(), "track ${t.id} has no units") }
        // Professional lessons are not part of the general sequence.
        val general = en.sequencer.orderedLessons().map { it.id }
        assertTrue(general.none { it.contains("credit") || it.contains("biz") })
        assertTrue(en.tracks.filter { it.id in setOf("finance", "credit_risk", "legal") }.all { it.disclaimer != null })
    }

    @Test fun spanishFrenchAndArabicHaveAWorkTrackOutsideTheGeneralSequence() {
        for (lang in listOf(LanguageCode.SPANISH, LanguageCode.FRENCH, LanguageCode.ARABIC)) {
            val c = Curriculum.of(lang)
            assertEquals(listOf("business"), c.tracks.map { it.id }, "$lang tracks")
            val trackLessons = c.unitsForTrack("business").flatMap { it.lessonIds }.toSet()
            assertTrue(trackLessons.isNotEmpty(), "$lang business track has no lessons")
            assertTrue(c.sequencer.orderedLessons().none { it.id in trackLessons }, "$lang track lessons leak into the general path")
            assertTrue(c.scenariosForTrack("business").isNotEmpty(), "$lang business track has no role-play")
        }
    }

    @Test fun mandarinHasToneExercises() {
        val zh = Curriculum.of(LanguageCode.MANDARIN)
        assertTrue(zh.lessons.flatMap { it.exercises }.count { it is Exercise.ToneChoice } >= 6)
        assertTrue(zh.lessons.flatMap { it.exercises }.any { it.skill == Skill.PRONUNCIATION })
    }
}
