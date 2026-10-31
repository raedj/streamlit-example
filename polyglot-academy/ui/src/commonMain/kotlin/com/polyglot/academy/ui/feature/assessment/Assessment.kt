package com.polyglot.academy.ui.feature.assessment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.polyglot.academy.core.assessment.DiagnosticSession
import com.polyglot.academy.core.model.AssessmentResult
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.Languages
import com.polyglot.academy.core.model.SessionKind
import com.polyglot.academy.core.model.StudySession
import com.polyglot.academy.ui.app.LocalApp
import com.polyglot.academy.ui.app.ScrollScreen
import com.polyglot.academy.ui.app.rememberSpeaker
import com.polyglot.academy.ui.designsystem.Banner
import com.polyglot.academy.ui.designsystem.BannerKind
import com.polyglot.academy.ui.designsystem.ChoiceCard
import com.polyglot.academy.ui.designsystem.HeroIllustration
import com.polyglot.academy.ui.designsystem.HeroKind
import com.polyglot.academy.ui.designsystem.LabeledProgress
import com.polyglot.academy.ui.designsystem.LevelBadge
import com.polyglot.academy.ui.designsystem.PolyCard
import com.polyglot.academy.ui.designsystem.PolyglotTheme
import com.polyglot.academy.ui.designsystem.PrimaryButton
import com.polyglot.academy.ui.designsystem.QuietButton
import com.polyglot.academy.ui.designsystem.SecondaryButton
import com.polyglot.academy.ui.designsystem.SectionHeader
import com.polyglot.academy.ui.feature.lesson.ExerciseContext
import com.polyglot.academy.ui.feature.lesson.ExerciseView
import com.polyglot.academy.ui.i18n.K
import com.polyglot.academy.ui.i18n.Labels
import com.polyglot.academy.ui.i18n.LocalStrings
import kotlinx.coroutines.launch

@Composable
fun AssessmentScreen(language: LanguageCode?, fromOnboarding: Boolean) {
    val app = LocalApp.current
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val scope = rememberCoroutineScope()
    val lang = language ?: app.profile.activeLearningLanguage
    val speaker = rememberSpeaker(s)
    var session by remember(lang) { mutableStateOf<DiagnosticSession?>(null) }
    var study by remember { mutableStateOf<StudySession?>(null) }
    var result by remember(lang) { mutableStateOf<AssessmentResult?>(null) }
    var questionKey by remember { mutableIntStateOf(0) }
    var choosingOther by remember { mutableStateOf(false) }
    val history by remember(lang) { app.services.store.assessments(lang) }.collectAsState(emptyList())
    val explain = app.profile.explanationLanguage.takeIf { it != lang } ?: if (lang == LanguageCode.ENGLISH) LanguageCode.ARABIC else LanguageCode.ENGLISH

    fun finishIfDone(sess: DiagnosticSession) {
        if (!sess.isFinished) return
        scope.launch {
            result = app.services.assessments.finish(sess, lang)
            study?.let { app.services.study.updateSession(it, sess.answeredCount, result!!.correctAnswers, true, sess.answeredCount * 20) }
        }
    }

    ScrollScreen(s[K.ASSESS_TITLE], showBack = !fromOnboarding || result != null) {
        val res = result
        val sess = session
        when {
            res != null -> {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    HeroIllustration(HeroKind.SUCCESS, s[K.ILLUSTRATION], size = 110.dp)
                    Text(s[K.ASSESS_RESULT_TITLE], style = MaterialTheme.typography.titleMedium, color = c.textSecondary)
                    Text("${res.estimatedLevel.label}", style = PolyglotTheme.learning.stat)
                    Text(Labels.level(s, res.estimatedLevel), style = MaterialTheme.typography.titleMedium)
                    Text(s.f(K.ASSESS_RANGE, res.levelLow.label, res.levelHigh.label), style = MaterialTheme.typography.bodyLarge)
                    Text(s.f(K.ASSESS_SCORE, res.correctAnswers, res.questionsAnswered), color = c.textSecondary)
                }
                Banner(BannerKind.INFO, s[K.ASSESS_DISCLAIMER])
                if (res.skillScores.isNotEmpty()) {
                    SectionHeader(s[K.ASSESS_BY_SKILL])
                    PolyCard { res.skillScores.forEach { (sk, v) -> LabeledProgress(Labels.skill(s, sk), s.f(K.PERCENT, (v * 100).toInt()), v.toFloat()) } }
                }
                if (!choosingOther) {
                    PrimaryButton(s.f(K.ASSESS_ACCEPT, res.estimatedLevel.label), {
                        scope.launch {
                            app.services.assessments.applyStartingLevel(lang, res.estimatedLevel, fromAssessment = true)
                            app.nav.home()
                        }
                    }, Modifier.fillMaxWidth())
                    SecondaryButton(s[K.ASSESS_CHOOSE_OTHER], { choosingOther = true }, Modifier.fillMaxWidth())
                } else {
                    Languages.of(lang).supportedLevels.forEach { l ->
                        ChoiceCard("${l.label} · ${Labels.level(s, l)}", l == res.estimatedLevel, onClick = {
                            scope.launch { app.services.assessments.applyStartingLevel(lang, l, fromAssessment = l == res.estimatedLevel); app.nav.home() }
                        })
                    }
                }
            }
            sess != null && !sess.isFinished -> {
                val item = sess.current!!
                Text(s.f(K.ASSESS_QUESTION, sess.answeredCount + 1, sess.maxQuestions), style = MaterialTheme.typography.labelLarge, color = c.textSecondary)
                LinearProgressIndicator(progress = { sess.answeredCount.toFloat() / sess.maxQuestions }, modifier = Modifier.fillMaxWidth(), color = c.primary, trackColor = c.progressTrack, drawStopIndicator = {})
                val ctx = ExerciseContext(lang, explain, speaker, false, true, false)
                PolyCard {
                    // Keyed so each new question gets fresh exercise state.
                    androidx.compose.runtime.key(questionKey) {
                        ExerciseView(item.question, ctx, answered = false) { a ->
                            val idx = item.question.options.indexOf(a.userAnswer).takeIf { it >= 0 } ?: -1
                            sess.answer(idx); questionKey++
                            finishIfDone(sess)
                        }
                    }
                }
                QuietButton(s[K.ASSESS_DONT_KNOW], { sess.answer(-1); questionKey++; finishIfDone(sess) })
            }
            else -> {
                HeroIllustration(HeroKind.PLAN, s[K.ILLUSTRATION], Modifier.align(Alignment.CenterHorizontally), size = 120.dp)
                Text(s.f(K.ASSESS_INTRO_TITLE, Labels.language(s, lang)), style = MaterialTheme.typography.headlineSmall)
                Text(s[K.ASSESS_INTRO_BODY], style = MaterialTheme.typography.bodyLarge, color = c.textSecondary)
                Banner(BannerKind.INFO, s[K.ASSESS_DISCLAIMER])
                PrimaryButton(s[K.ASSESS_START], {
                    session = app.services.assessments.start(lang, app.profile.enrollment(lang)?.startingLevel)
                    scope.launch { study = app.services.study.startSession(lang, SessionKind.ASSESSMENT, null) }
                }, Modifier.fillMaxWidth())
                if (fromOnboarding) QuietButton(s[K.SKIP_FOR_NOW], { app.nav.home() })
                if (history.isNotEmpty()) {
                    SectionHeader(s[K.ASSESS_HISTORY])
                    history.reversed().forEach { h ->
                        PolyCard {
                            LevelBadge(h.estimatedLevel, null)
                            Text(s.f(K.ASSESS_RANGE, h.levelLow.label, h.levelHigh.label), style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                        }
                    }
                }
            }
        }
    }
}
