package com.polyglot.academy.ui.feature.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.stories.StoryText
import com.polyglot.academy.core.wordlist.WordEntry
import com.polyglot.academy.core.wordlist.WordList
import com.polyglot.academy.core.wordlist.WordLists
import com.polyglot.academy.ui.app.LocalApp
import com.polyglot.academy.ui.app.Speaker
import com.polyglot.academy.ui.designsystem.LearningText
import com.polyglot.academy.ui.designsystem.Pill
import com.polyglot.academy.ui.designsystem.PolyCard
import com.polyglot.academy.ui.designsystem.PolyglotTheme
import com.polyglot.academy.ui.designsystem.QuietButton
import com.polyglot.academy.ui.i18n.K
import com.polyglot.academy.ui.i18n.LocalStrings
import kotlinx.coroutines.launch

/**
 * A sentence in the language being learned, in which every word can be tapped: the word is spoken and a
 * card under the sentence gives its dictionary form, reading, meaning and level, with a button that adds it
 * to the review. Words are found in the language's frequency list, inflected forms included (WordList.lookup);
 * a word the list lacks says so. Until the list has loaded, the sentence shows as plain text.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TappableSentence(
    sentence: String,
    language: LanguageCode,
    speaker: Speaker,
    modifier: Modifier = Modifier,
    style: TextStyle = PolyglotTheme.learning.sentence,
    names: List<String> = emptyList(),
    center: Boolean = false,
) {
    val list by produceState<WordList?>(null, language) { value = WordLists.load(language) }
    val l = list
    if (l == null) { LearningText(sentence, language, modifier, style = style); return }
    val c = PolyglotTheme.colors
    var selected by remember(sentence) { mutableStateOf<Int?>(null) }
    val tokens = remember(sentence, l) { StoryText.tokens(sentence, language, l.tapTerms) }
    val rtl = language == LanguageCode.ARABIC || language == LanguageCode.URDU
    Column(modifier.fillMaxWidth()) {
        // The sentence keeps its own direction whatever the interface language is.
        CompositionLocalProvider(LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr) {
            FlowRow(horizontalArrangement = if (center) Arrangement.Center else Arrangement.Start, verticalArrangement = Arrangement.Center) {
                tokens.forEachIndexed { i, t ->
                    val tappable = t.isWord && t.text !in names && t.text.any(Char::isLetter)
                    if (!tappable) LearningText(t.text, language, style = style)
                    else {
                        val isSel = selected == i
                        LearningText(t.text, language, style = style, color = if (isSel) c.primary else c.textPrimary,
                            modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(if (isSel) c.selection else c.card)
                                .clickable(role = Role.Button) { selected = if (isSel) null else i; if (!isSel) speaker.play(t.text, language) })
                    }
                }
            }
        }
        selected?.let { i -> tokens.getOrNull(i)?.let { t -> WordCard(t.text, l.lookup(t.text), language, speaker) { selected = null } } }
    }
}

@Composable
private fun WordCard(tapped: String, entry: WordEntry?, language: LanguageCode, speaker: Speaker, onClose: () -> Unit) {
    val app = LocalApp.current
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val scope = rememberCoroutineScope()
    var added by remember(entry?.id) { mutableStateOf<Boolean?>(null) }
    val explain = app.profile.explanationLanguage.takeIf { it != language } ?: if (language == LanguageCode.ENGLISH) LanguageCode.ARABIC else LanguageCode.ENGLISH
    PolyCard(containerColor = c.primaryContainer.copy(alpha = if (c.isDark) 0.35f else 0.5f), modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                LearningText(entry?.term ?: tapped, language, style = MaterialTheme.typography.titleLarge, reading = entry?.pronunciation)
            }
            IconButton(onClick = { speaker.play(entry?.term ?: tapped, language) }) { Icon(Icons.AutoMirrored.Filled.VolumeUp, s[K.PLAY_AUDIO]) }
            IconButton(onClick = onClose) { Icon(Icons.Filled.Close, s[K.CLOSE]) }
        }
        if (entry == null) Text(s[K.LOOKUP_NOT_IN_LIST], style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
        else {
            Text(entry.meaning.forLanguage(explain), style = MaterialTheme.typography.bodyLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Pill(s.f(K.LOOKUP_LEVEL, entry.level))
                if (entry.term != tapped && entry.term.lowercase() != tapped.lowercase()) Pill(s.f(K.LOOKUP_FORM_OF, tapped, entry.term))
            }
            when (added) {
                true -> Text(s[K.LOOKUP_ADDED], color = c.success, style = MaterialTheme.typography.labelLarge)
                false -> Text(s[K.LOOKUP_ALREADY], color = c.textSecondary, style = MaterialTheme.typography.labelLarge)
                null -> QuietButton(s[K.LOOKUP_ADD], { scope.launch { added = app.services.wordLevels.add(entry) } })
            }
        }
    }
}
