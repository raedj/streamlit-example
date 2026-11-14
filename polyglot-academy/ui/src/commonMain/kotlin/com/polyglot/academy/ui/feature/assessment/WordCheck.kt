package com.polyglot.academy.ui.feature.assessment

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.key
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.polyglot.academy.core.assessment.WordCheck
import com.polyglot.academy.core.learning.AnswerChecker
import com.polyglot.academy.core.learning.AnswerVerdict
import com.polyglot.academy.core.learning.Recall
import com.polyglot.academy.core.model.AssessmentKind
import com.polyglot.academy.core.model.AssessmentResult
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.util.toLocalDate
import com.polyglot.academy.core.wordlist.WordEntry
import com.polyglot.academy.core.wordlist.WordLists
import com.polyglot.academy.ui.app.LocalApp
import com.polyglot.academy.ui.app.ScrollScreen
import com.polyglot.academy.ui.designsystem.Banner
import com.polyglot.academy.ui.designsystem.BannerKind
import com.polyglot.academy.ui.designsystem.LoadingPlaceholder
import com.polyglot.academy.ui.designsystem.PolyCard
import com.polyglot.academy.ui.designsystem.PolyglotTheme
import com.polyglot.academy.ui.designsystem.PrimaryButton
import com.polyglot.academy.ui.designsystem.SecondaryButton
import com.polyglot.academy.ui.feature.lesson.RecallInput
import com.polyglot.academy.ui.feature.practice.explainFor
import com.polyglot.academy.ui.i18n.K
import com.polyglot.academy.ui.i18n.LocalStrings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Words from across the list, produced from their meaning. Nothing here is shown as right or wrong until the end. */
@Composable
fun WordCheckScreen(language: LanguageCode) {
    val app = LocalApp.current
    val s = LocalStrings.current
    val scope = rememberCoroutineScope()
    val clock = app.services.clock
    // The day decides the sample, so a retake on another day meets different words.
    val words = produceState<List<WordEntry>?>(null, language) {
        value = WordCheck.sample(WordLists.load(language), (clock.nowMillis() / 86_400_000L).toInt())
    }.value
    var started by remember { mutableStateOf(false) }
    var index by remember { mutableIntStateOf(0) }
    val answers = remember { mutableListOf<Pair<WordEntry, Boolean>>() }
    var shownAt by remember { mutableLongStateOf(0L) }
    var result by remember { mutableStateOf<AssessmentResult?>(null) }
    var first by remember { mutableStateOf<AssessmentResult?>(null) }

    fun next(entry: WordEntry, right: Boolean, list: List<WordEntry>) {
        answers += entry to right
        if (index < list.lastIndex) { index++; shownAt = clock.nowMillis(); return }
        val finished = WordCheck.result(app.services.ids.newId("wordcheck"), language, clock.nowMillis(), answers)
        scope.launch {
            val earlier = app.services.store.assessments(language).first().filter { it.kind == AssessmentKind.PROGRESS_CHECK }
            first = earlier.minByOrNull { it.takenAtMillis }
            app.services.store.saveAssessment(finished)
            result = finished
        }
    }

    ScrollScreen(s[K.WORDCHECK_TITLE]) {
        val done = result
        when {
            words == null -> LoadingPlaceholder(s[K.LOADING])
            done != null -> {
                PolyCard {
                    Text(s.f(K.WORDCHECK_RESULT, done.correctAnswers, done.questionsAnswered), style = MaterialTheme.typography.titleLarge)
                    Text(s.f(K.WORDCHECK_ESTIMATE, WordCheck.estimatedWords(done.correctAnswers)), style = MaterialTheme.typography.bodyMedium, color = PolyglotTheme.colors.textSecondary)
                    first?.let { f ->
                        Text(s.f(K.WORDCHECK_CHANGE, f.takenAtMillis.toLocalDate().toString(), f.correctAnswers, f.questionsAnswered), style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Banner(BannerKind.INFO, s[K.WORDCHECK_REAL])
                PrimaryButton(s[K.DONE], { app.nav.pop() }, Modifier.fillMaxWidth())
            }
            !started -> {
                Text(s.f(K.WORDCHECK_INTRO, WordCheck.SIZE, WordCheck.SECONDS_PER_WORD), style = MaterialTheme.typography.bodyLarge)
                PrimaryButton(s[K.WORDCHECK_START], { started = true; shownAt = clock.nowMillis() }, Modifier.fillMaxWidth())
            }
            else -> key(index) {
                val entry = words[index]
                var text by remember { mutableStateOf("") }
                var problem by remember { mutableStateOf<K?>(null) }
                val item = remember { entry.toVocabularyItem() }
                Text(s.f(K.WORDCHECK_QUESTION, index + 1, words.size), style = MaterialTheme.typography.labelLarge, color = PolyglotTheme.colors.textSecondary)
                PolyCard { Text(item.meaning.forLanguage(explainFor(language, app.profile.explanationLanguage)), style = MaterialTheme.typography.headlineSmall) }
                RecallInput(text, { text = it; problem = null }, language, item.term, emptyList(), entry.id,
                    supporting = problem?.let { s[it] }, isError = problem != null, allowTiles = false)
                PrimaryButton(s[K.CHECK], {
                    val check = AnswerChecker.check(text, Recall.acceptedAnswers(item))
                    when (check.verdict) {
                        AnswerVerdict.EMPTY -> problem = K.FEEDBACK_EMPTY
                        AnswerVerdict.WRONG_KEYBOARD -> problem = K.FEEDBACK_KEYBOARD
                        else -> next(entry, check.isCorrect && clock.nowMillis() - shownAt <= WordCheck.SECONDS_PER_WORD * 1000, words)
                    }
                }, Modifier.fillMaxWidth())
                SecondaryButton(s[K.WORDCHECK_SKIP], { next(entry, false, words) }, Modifier.fillMaxWidth())
            }
        }
    }
}
