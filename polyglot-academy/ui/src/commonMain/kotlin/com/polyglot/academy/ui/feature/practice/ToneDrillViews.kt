package com.polyglot.academy.ui.feature.practice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.polyglot.academy.core.mandarin.ListeningPair
import com.polyglot.academy.core.mandarin.SyllableQuestion
import com.polyglot.academy.core.mandarin.ToneDrillKind
import com.polyglot.academy.core.mandarin.ToneDrills
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.Languages
import com.polyglot.academy.core.model.SessionKind
import com.polyglot.academy.core.wordlist.WordLists
import com.polyglot.academy.ui.app.LocalApp
import com.polyglot.academy.ui.app.Speaker
import com.polyglot.academy.ui.designsystem.AudioButton
import com.polyglot.academy.ui.designsystem.Banner
import com.polyglot.academy.ui.designsystem.BannerKind
import com.polyglot.academy.ui.designsystem.ChoiceCard
import com.polyglot.academy.ui.designsystem.FeedbackPanel
import com.polyglot.academy.ui.designsystem.LoadingPlaceholder
import com.polyglot.academy.ui.designsystem.PolyCard
import com.polyglot.academy.ui.designsystem.PolyglotTheme
import com.polyglot.academy.ui.designsystem.PrimaryButton
import com.polyglot.academy.ui.i18n.K
import com.polyglot.academy.ui.i18n.Labels
import com.polyglot.academy.ui.i18n.LocalStrings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** The tones screen's drill modes besides the four-tone exercise. */
internal enum class ToneMode { TONES, SANDHI, NEUTRAL, PAIRS }

/**
 * Tone changes, the neutral tone and hearing the difference between close words, built from the
 * word list. Without a Chinese voice the questions cannot be heard, so answers are not recorded.
 */
@Composable
internal fun ToneDrillSection(mode: ToneMode, speaker: Speaker) {
    val app = LocalApp.current
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val scope = rememberCoroutineScope()
    val practice = remember(mode) { app.services.study.practice(LanguageCode.MANDARIN, SessionKind.LISTENING, "tones_${mode.name.lowercase()}") }
    val voice by produceState<Boolean?>(null) {
        value = app.services.platform.synthesizer.isLanguageAvailable(Languages.of(LanguageCode.MANDARIN).defaultVariant.localeTag)
    }
    // Shuffled once per visit; the word list is parsed off the main thread.
    val questions by produceState<List<Any>?>(null, mode) {
        value = withContext(Dispatchers.Default) {
            val list = WordLists.load(LanguageCode.MANDARIN)
            when (mode) {
                ToneMode.SANDHI -> ToneDrills.sandhi(list)
                ToneMode.NEUTRAL -> ToneDrills.neutral(list)
                ToneMode.PAIRS -> ToneDrills.listeningPairs(list)
                ToneMode.TONES -> emptyList()
            }.shuffled()
        }
    }
    var index by remember(mode) { mutableIntStateOf(0) }
    var chosen by remember(mode, index) { mutableStateOf<Int?>(null) }
    var correct by remember(mode) { mutableIntStateOf(0) }
    var total by remember(mode) { mutableIntStateOf(0) }

    Text(s[K.TONES_DRILL_SOURCE], style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
    if (voice == false) Banner(BannerKind.WARNING, s.f(K.LISTEN_NO_VOICE, Labels.language(s, LanguageCode.MANDARIN)))
    val list = questions
    if (list == null) { LoadingPlaceholder(s[K.LOADING]); return }
    if (list.isEmpty()) return
    if (total > 0) Text(s.f(K.TONES_SCORE, correct, total), style = MaterialTheme.typography.titleSmall)
    val q = list[index % list.size]

    fun answer(i: Int, right: Boolean) {
        if (chosen != null) return
        chosen = i
        // Without a voice the learner could not hear the word, so the answer is not counted.
        if (voice != false) { total++; if (right) correct++; scope.launch { practice.answer(right) } }
    }

    when (q) {
        is SyllableQuestion -> {
            PolyCard {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(s[K.TONES_DRILL_ASK], style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center)
                    // The asked character in the accent colour; its syllable stays hidden until answered.
                    Text(buildAnnotatedString {
                        q.word.forEachIndexed { i, ch -> if (i == q.index) withStyle(SpanStyle(color = c.primary, fontWeight = FontWeight.Bold)) { append(ch) } else append(ch) }
                    }, style = PolyglotTheme.learning.hanzi)
                    // Pinyin reads left to right even in the Arabic interface (otherwise "?" lands on the wrong side).
                    Text(q.spoken.mapIndexed { i, syl -> if (i == q.index && chosen == null) "?" else syl.marked }.joinToString(" "),
                        style = PolyglotTheme.learning.pinyin.copy(textDirection = androidx.compose.ui.text.style.TextDirection.Ltr))
                    Text(s.gloss(q.meaning), style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
                    AudioButton(s[K.PLAY_AUDIO], { speaker.toggle(q.word, LanguageCode.MANDARIN) }, playing = speaker.playingText == q.word)
                }
            }
            q.options.forEachIndexed { i, opt ->
                ChoiceCard(opt, chosen == i || (chosen != null && i == q.answer), onClick = { answer(i, i == q.answer) })
            }
            chosen?.let { ch ->
                val rule = when (q.kind) {
                    ToneDrillKind.BU -> K.TONES_RULE_BU; ToneDrillKind.YI -> K.TONES_RULE_YI
                    ToneDrillKind.THIRD_TONES -> K.TONES_RULE_THIRD; ToneDrillKind.NEUTRAL -> K.TONES_RULE_NEUTRAL
                }
                FeedbackPanel(ch == q.answer, s[if (ch == q.answer) K.FEEDBACK_CORRECT else K.FEEDBACK_INCORRECT], s[rule])
            }
        }
        is ListeningPair -> {
            // The heard word is listed first or second at random, but fixed for this question.
            val options = remember(mode, index) { listOf(q.heard, q.other).shuffled() }
            PolyCard {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(s[K.TONES_PAIRS_ASK], style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center)
                    AudioButton(s[K.PLAY_AUDIO], { speaker.toggle(q.heard.term, LanguageCode.MANDARIN) }, playing = speaker.playingText == q.heard.term)
                }
            }
            options.forEachIndexed { i, e ->
                val label = if (chosen == null) e.pronunciation.orEmpty() else "${e.pronunciation} · ${e.term} · ${s.gloss(e.meaning)}"
                ChoiceCard(label, chosen == i || (chosen != null && e == q.heard), onClick = { answer(i, e == q.heard) })
            }
            chosen?.let { ch ->
                val right = options[ch] == q.heard
                FeedbackPanel(right, s[if (right) K.FEEDBACK_CORRECT else K.FEEDBACK_INCORRECT],
                    s[if (q.contrast == ListeningPair.Contrast.TONE) K.TONES_PAIRS_TONE else K.TONES_PAIRS_SOUND])
            }
        }
    }
    if (chosen != null) PrimaryButton(s[K.NEXT], { index++ }, Modifier.fillMaxWidth())
}
