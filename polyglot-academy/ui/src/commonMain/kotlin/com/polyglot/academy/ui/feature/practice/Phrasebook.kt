package com.polyglot.academy.ui.feature.practice

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.polyglot.academy.core.content.Curriculum
import com.polyglot.academy.ui.app.LocalApp
import com.polyglot.academy.ui.app.ScrollScreen
import com.polyglot.academy.ui.app.rememberSpeaker
import com.polyglot.academy.ui.app.rememberToast
import com.polyglot.academy.ui.designsystem.AudioButton
import com.polyglot.academy.ui.designsystem.ChipGroup
import com.polyglot.academy.ui.designsystem.LearningText
import com.polyglot.academy.ui.designsystem.PolyCard
import com.polyglot.academy.ui.designsystem.PolyglotTheme
import com.polyglot.academy.ui.designsystem.QuietButton
import com.polyglot.academy.ui.i18n.K
import com.polyglot.academy.ui.i18n.Labels
import com.polyglot.academy.ui.i18n.LocalStrings
import kotlinx.coroutines.launch

/** The most-used everyday and travel phrases of the active language, by situation, with audio. */
@Composable
fun PhrasebookScreen() {
    val app = LocalApp.current
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val scope = rememberCoroutineScope()
    val toast = rememberToast()
    val speaker = rememberSpeaker(s)
    val lang = app.profile.activeLearningLanguage
    var topic by rememberSaveable { mutableStateOf(Curriculum.phrasebookTopics.first()) }
    ScrollScreen(s[K.PHRASEBOOK_TITLE]) {
        Text(Labels.language(s, lang), style = MaterialTheme.typography.bodyLarge, color = c.textSecondary)
        Text(s[K.PHRASEBOOK_BODY], style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
        ChipGroup(Curriculum.phrasebookTopics, setOf(topic), { s[K.valueOf("PHRASE_TOPIC_" + it.uppercase())] }, { topic = it })
        Curriculum.phrasebook(lang, topic).forEach { item ->
            PolyCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        LearningText(item.term, lang, style = MaterialTheme.typography.titleMedium, reading = item.pronunciation ?: item.transliteration)
                        Text(s.gloss(item.meaning), style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
                    }
                    AudioButton(s[K.PLAY_AUDIO], { speaker.toggle(item.term, lang) }, playing = speaker.playingText == item.term)
                }
                QuietButton(s[K.PHRASEBOOK_ADD], {
                    scope.launch { app.services.reviews.addToReview(lang, item.id); toast(s[K.PHRASEBOOK_ADDED]) }
                })
            }
        }
    }
}
