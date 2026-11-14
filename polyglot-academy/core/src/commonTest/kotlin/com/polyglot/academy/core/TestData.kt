package com.polyglot.academy.core

import com.polyglot.academy.core.model.ExerciseAttempt
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.Skill

fun attempt(
    i: Int, correct: Boolean, skill: Skill = Skill.VOCABULARY, difficulty: Int = 2,
    responseMillis: Long = 5_000, hints: Int = 0, at: Long = 1_000_000L + i * 60_000L,
) = ExerciseAttempt(
    id = "a$i", lessonId = "l", exerciseId = "e$i", language = LanguageCode.ENGLISH, skill = skill, difficulty = difficulty,
    correct = correct, score = if (correct) 1.0 else 0.0, responseMillis = responseMillis, hintsUsed = hints,
    answeredAtMillis = at, userAnswer = "x",
)
