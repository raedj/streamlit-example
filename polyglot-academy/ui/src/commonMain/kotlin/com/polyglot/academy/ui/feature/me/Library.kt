package com.polyglot.academy.ui.feature.me

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.polyglot.academy.core.content.Curriculum
import com.polyglot.academy.core.media.ExtractionSupport
import com.polyglot.academy.core.media.ImportPolicy
import com.polyglot.academy.core.model.BookmarkKind
import com.polyglot.academy.core.model.Gloss
import com.polyglot.academy.core.model.ImportedMaterial
import com.polyglot.academy.core.model.MaterialStatus
import com.polyglot.academy.core.model.MaterialType
import com.polyglot.academy.core.model.Note
import com.polyglot.academy.core.model.PartOfSpeech
import com.polyglot.academy.core.model.ReviewPhase
import com.polyglot.academy.core.model.VocabularyItem
import com.polyglot.academy.core.model.VocabularySource
import com.polyglot.academy.core.model.CefrLevel
import com.polyglot.academy.core.service.ImportInput
import com.polyglot.academy.core.service.ImportProgress
import com.polyglot.academy.core.util.TextNormalizer
import com.polyglot.academy.ui.app.LocalApp
import com.polyglot.academy.ui.app.ScrollScreen
import com.polyglot.academy.ui.app.rememberSpeaker
import com.polyglot.academy.ui.app.rememberToast
import com.polyglot.academy.ui.designsystem.AppTextField
import com.polyglot.academy.ui.designsystem.AudioButton
import com.polyglot.academy.ui.designsystem.Banner
import com.polyglot.academy.ui.designsystem.BannerKind
import com.polyglot.academy.ui.designsystem.ChipGroup
import com.polyglot.academy.ui.designsystem.ConfirmDialog
import com.polyglot.academy.ui.designsystem.HeroIllustration
import com.polyglot.academy.ui.designsystem.HeroKind
import com.polyglot.academy.ui.designsystem.LearningText
import com.polyglot.academy.ui.designsystem.Pill
import com.polyglot.academy.ui.designsystem.PolyCard
import com.polyglot.academy.ui.designsystem.PolyglotTheme
import com.polyglot.academy.ui.designsystem.PrimaryButton
import com.polyglot.academy.ui.designsystem.QuietButton
import com.polyglot.academy.ui.designsystem.SecondaryButton
import com.polyglot.academy.ui.designsystem.SectionHeader
import com.polyglot.academy.ui.designsystem.StateMessage
import com.polyglot.academy.ui.designsystem.SwitchRow
import com.polyglot.academy.ui.i18n.K
import com.polyglot.academy.ui.i18n.Labels
import com.polyglot.academy.ui.i18n.LocalStrings
import com.polyglot.academy.ui.navigation.Route
import kotlinx.coroutines.launch

// ---------------- Vocabulary ----------------

@Composable
fun VocabularyScreen() {
    val app = LocalApp.current
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val scope = rememberCoroutineScope()
    val lang = app.profile.activeLearningLanguage
    val speaker = rememberSpeaker(s)
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var query by rememberSaveable { mutableStateOf("") }
    var dueOnly by rememberSaveable { mutableStateOf(false) }
    var adding by remember { mutableStateOf(false) }
    val states by remember(lang) { app.services.store.states(lang) }.collectAsState(emptyList())
    val userVocab by remember(lang) { app.services.store.userVocabulary(lang) }.collectAsState(emptyList())
    val phrases by remember(lang) { app.services.store.savedPhrases(lang) }.collectAsState(emptyList())
    val now = app.services.clock.nowMillis()
    ScrollScreen(s[K.VOCAB_TITLE]) {
        TabRow(selectedTabIndex = tab, containerColor = c.background) {
            Tab(tab == 0, { tab = 0 }, text = { Text(s[K.VOCAB_TAB_WORDS]) })
            Tab(tab == 1, { tab = 1 }, text = { Text(s[K.VOCAB_TAB_PHRASES]) })
        }
        AppTextField(query, { query = it }, s[K.VOCAB_SEARCH])
        if (tab == 0) {
            ChipGroup(listOf(false, true), setOf(dueOnly), { if (it) s[K.VOCAB_FILTER_DUE] else s[K.VOCAB_FILTER_ALL] }, { dueOnly = it })
            val builtIn = Curriculum.of(lang).vocabularyById
            val byId = builtIn + userVocab.associateBy { it.id }
            val stateById = states.associateBy { it.itemId }
            val words = states.mapNotNull { st -> byId[st.itemId]?.let { it to st } } + userVocab.filter { it.id !in stateById }.map { it to null }
            val filtered = words.filter { (v, st) ->
                (!dueOnly || (st != null && st.dueAtMillis <= now)) &&
                    (query.isBlank() || TextNormalizer.loose(v.term).contains(TextNormalizer.loose(query)) || TextNormalizer.loose(s.gloss(v.meaning)).contains(TextNormalizer.loose(query)))
            }
            SecondaryButton(s[K.VOCAB_ADD], { adding = true }, Modifier.fillMaxWidth())
            if (filtered.isEmpty()) StateMessage(s[K.VOCAB_EMPTY_TITLE], s[K.VOCAB_EMPTY_BODY])
            filtered.sortedBy { it.second?.dueAtMillis ?: Long.MAX_VALUE }.forEach { (v, st) ->
                PolyCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            LearningText(v.term, lang, style = MaterialTheme.typography.titleMedium, reading = v.pronunciation ?: v.transliteration)
                            Text(s.gloss(v.meaning), style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                        }
                        AudioButton(s[K.PLAY_AUDIO], { speaker.toggle(v.term, lang) }, playing = speaker.playingText == v.term)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Pill(when (st?.phase) { null, ReviewPhase.NEW -> s[K.VOCAB_STATUS_NEW]; ReviewPhase.LEARNING, ReviewPhase.RELEARNING -> s[K.VOCAB_STATUS_LEARNING]; ReviewPhase.REVIEW -> s[K.VOCAB_STATUS_REVIEW] })
                        st?.let { Pill(if (it.dueAtMillis <= now) s[K.VOCAB_DUE_NOW] else s.f(K.VOCAB_NEXT_REVIEW, s.f(K.IN_TIME, Labels.duration(s, it.dueAtMillis - now)))) }
                        if (v.source != VocabularySource.BUILT_IN) IconButton(onClick = { scope.launch { app.services.store.removeVocabulary(v.id) } }) { Icon(Icons.Filled.Delete, s[K.REMOVE]) }
                    }
                }
            }
        } else {
            val list = phrases.filter { query.isBlank() || TextNormalizer.loose(it.text).contains(TextNormalizer.loose(query)) }
            if (list.isEmpty()) StateMessage(s[K.VOCAB_TAB_PHRASES], s[K.PHRASES_EMPTY])
            list.forEach { p ->
                PolyCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            LearningText(p.text, lang, style = MaterialTheme.typography.titleMedium)
                            if (p.meaning.isNotBlank()) Text(p.meaning, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                        }
                        AudioButton(s[K.PLAY_AUDIO], { speaker.toggle(p.text, lang) }, playing = speaker.playingText == p.text)
                        IconButton(onClick = { scope.launch { app.services.store.removePhrase(p.id) } }) { Icon(Icons.Filled.Delete, s[K.REMOVE]) }
                    }
                }
            }
        }
    }
    if (adding) AddWordDialog(onDismiss = { adding = false }) { term, meaning, example ->
        scope.launch {
            val item = VocabularyItem(app.services.ids.newId("uv"), lang, term.trim(), Gloss(meaning.trim(), meaning.trim()), PartOfSpeech.PHRASE, CefrLevel.A2, "mine",
                example = example.trim().ifBlank { null }, source = VocabularySource.USER_ADDED)
            app.services.store.addVocabulary(item)
            app.services.reviews.addToReview(lang, item.id)
            adding = false
        }
    }
}

@Composable
private fun AddWordDialog(onDismiss: () -> Unit, onSave: (String, String, String) -> Unit) {
    val s = LocalStrings.current
    var term by remember { mutableStateOf("") }
    var meaning by remember { mutableStateOf("") }
    var example by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(s[K.VOCAB_ADD]) },
        text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AppTextField(term, { term = it }, s[K.VOCAB_TERM]); AppTextField(meaning, { meaning = it }, s[K.VOCAB_MEANING]); AppTextField(example, { example = it }, s[K.VOCAB_EXAMPLE])
        } },
        confirmButton = { QuietButton(s[K.SAVE], { onSave(term, meaning, example) }, enabled = term.isNotBlank() && meaning.isNotBlank()) },
        dismissButton = { QuietButton(s[K.CANCEL], onDismiss) })
}

// ---------------- Library ----------------

@Composable
fun LibraryScreen() {
    val app = LocalApp.current
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val scope = rememberCoroutineScope()
    val toast = rememberToast()
    val lang = app.profile.activeLearningLanguage
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var pasting by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf<Float?>(null) }
    val materials by remember(lang) { app.services.store.materials(lang) }.collectAsState(emptyList())
    val notes by remember { app.services.store.notes() }.collectAsState(emptyList())
    val bookmarks by remember { app.services.store.bookmarks() }.collectAsState(emptyList())

    fun runImport(input: ImportInput, cloudAllowed: Boolean) = scope.launch {
        app.services.materials.import(lang, input, cloudAllowed).collect { p ->
            when (p) {
                is ImportProgress.Processing -> progress = p.fraction
                is ImportProgress.Done -> { progress = null; app.nav.push(Route.Material(p.material.id)) }
                is ImportProgress.Rejected -> {
                    progress = null
                    toast(when (val r = p.reason) {
                        is ImportPolicy.Validation.TooLarge -> s.f(K.MATERIAL_TOO_LARGE, r.maxBytes / (1024 * 1024))
                        ImportPolicy.Validation.Empty -> s[K.MATERIAL_EMPTY_FILE]
                        else -> s[K.MATERIAL_UNSUPPORTED_TYPE]
                    })
                }
            }
        }
    }

    ScrollScreen(s[K.LIBRARY_TITLE]) {
        TabRow(selectedTabIndex = tab, containerColor = c.background) {
            Tab(tab == 0, { tab = 0 }, text = { Text(s[K.LIBRARY_TAB_MATERIALS]) })
            Tab(tab == 1, { tab = 1 }, text = { Text(s[K.LIBRARY_TAB_NOTES]) })
            Tab(tab == 2, { tab = 2 }, text = { Text(s[K.LIBRARY_TAB_BOOKMARKS]) })
        }
        when (tab) {
            0 -> {
                Banner(BannerKind.INFO, s[K.LIBRARY_RIGHTS])
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SecondaryButton(s[K.LIBRARY_IMPORT_FILE], {
                        if (!app.platformUi.canPickFiles) toast(s[K.MATERIAL_PICKER_UNAVAILABLE])
                        else scope.launch { app.platformUi.pickFile()?.let { f -> runImport(ImportInput(f.name, f.mimeType, f.name, f.sizeBytes, f.text, f.bytes), false) } }
                    }, Modifier.weight(1f), icon = Icons.Filled.UploadFile)
                    PrimaryButton(s[K.LIBRARY_PASTE], { pasting = true }, Modifier.weight(1f), icon = Icons.Filled.ContentPaste)
                }
                progress?.let { f ->
                    Text(s.f(K.MATERIAL_PROCESSING, (f * 100).toInt()))
                    LinearProgressIndicator(progress = { f }, modifier = Modifier.fillMaxWidth(), drawStopIndicator = {})
                }
                if (materials.isEmpty()) StateMessage(s[K.LIBRARY_EMPTY_TITLE], s[K.LIBRARY_EMPTY_BODY], illustration = { HeroIllustration(HeroKind.EMPTY_LIBRARY, s[K.ILLUSTRATION], size = 130.dp) })
                materials.forEach { m ->
                    PolyCard(onClick = { app.nav.push(Route.Material(m.id)) }) {
                        Text(m.title, style = MaterialTheme.typography.titleMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { Pill(Labels.materialType(s, m.type)); Pill(Labels.materialStatus(s, m.status)) }
                    }
                }
                SectionHeader(s[K.LIBRARY_SUPPORT])
                PolyCard {
                    MaterialType.entries.forEach { t ->
                        val sup = app.services.materials.support(t)
                        Row { Text(Labels.materialType(s, t), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                            Text(s[K.valueOf("SUPPORT_${sup.name}")], style = MaterialTheme.typography.labelMedium, color = if (sup == ExtractionSupport.SUPPORTED) c.success else c.textSecondary) }
                    }
                }
            }
            1 -> NotesTab(notes)
            else -> {
                if (bookmarks.isEmpty()) StateMessage(s[K.LIBRARY_TAB_BOOKMARKS], s[K.BOOKMARKS_EMPTY])
                bookmarks.forEach { b ->
                    val lesson = Curriculum.all.values.firstNotNullOfOrNull { it.lessonsById[b.referenceId] }
                    PolyCard(onClick = { if (b.kind == BookmarkKind.LESSON) app.nav.push(Route.LessonOverview(b.referenceId)) }) {
                        Text(lesson?.let { s.gloss(it.title) } ?: b.referenceId, style = MaterialTheme.typography.titleSmall)
                    }
                }
            }
        }
    }
    if (pasting) PasteDialog(onDismiss = { pasting = false }) { title, text, cloud ->
        pasting = false
        runImport(ImportInput(title, "text/plain", "$title.txt", text.encodeToByteArray().size.toLong(), text), cloud)
    }
}

@Composable
private fun PasteDialog(onDismiss: () -> Unit, onImport: (String, String, Boolean) -> Unit) {
    val s = LocalStrings.current
    var title by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }
    var cloud by remember { mutableStateOf(false) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(s[K.LIBRARY_PASTE]) },
        text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AppTextField(title, { title = it }, s[K.MATERIAL_TITLE_LABEL])
            AppTextField(text, { text = it }, s[K.MATERIAL_TEXT_LABEL], singleLine = false, minLines = 5)
            SwitchRow(s[K.MATERIAL_CLOUD_ALLOW], cloud, { cloud = it })
        } },
        confirmButton = { QuietButton(s[K.MATERIAL_IMPORT], { onImport(title.ifBlank { text.take(30) }, text, cloud) }, enabled = text.isNotBlank()) },
        dismissButton = { QuietButton(s[K.CANCEL], onDismiss) })
}

@Composable
private fun NotesTab(notes: List<Note>) {
    val app = LocalApp.current
    val s = LocalStrings.current
    val scope = rememberCoroutineScope()
    var editing by remember { mutableStateOf<Note?>(null) }
    SecondaryButton(s[K.NOTE_NEW], {
        val now = app.services.clock.nowMillis()
        editing = Note(app.services.ids.newId("note"), app.profile.activeLearningLanguage, "", "", now, now)
    }, Modifier.fillMaxWidth())
    if (notes.isEmpty()) StateMessage(s[K.LIBRARY_TAB_NOTES], s[K.NOTES_EMPTY])
    notes.forEach { n ->
        PolyCard(onClick = { editing = n }) {
            Text(n.title.ifBlank { "—" }, style = MaterialTheme.typography.titleSmall)
            Text(n.body, style = MaterialTheme.typography.bodySmall, color = PolyglotTheme.colors.textSecondary, maxLines = 3)
        }
    }
    editing?.let { n ->
        var title by remember(n.id) { mutableStateOf(n.title) }
        var body by remember(n.id) { mutableStateOf(n.body) }
        AlertDialog(onDismissRequest = { editing = null }, title = { Text(s[K.NOTE_NEW]) },
            text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppTextField(title, { title = it }, s[K.NOTE_TITLE]); AppTextField(body, { body = it }, s[K.NOTE_BODY], singleLine = false, minLines = 4)
            } },
            confirmButton = { QuietButton(s[K.SAVE], {
                scope.launch { app.services.store.upsertNote(n.copy(title = title, body = body, updatedAtMillis = app.services.clock.nowMillis())); editing = null }
            }, enabled = title.isNotBlank() || body.isNotBlank()) },
            dismissButton = { QuietButton(s[K.DELETE], { scope.launch { app.services.store.deleteNote(n.id); editing = null } }, color = PolyglotTheme.colors.error) })
    }
}

@Composable
fun MaterialScreen(materialId: String) {
    val app = LocalApp.current
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val scope = rememberCoroutineScope()
    val speaker = rememberSpeaker(s)
    val materials by remember { app.services.store.materials(null) }.collectAsState(null)
    val material = materials?.firstOrNull { it.id == materialId }
    var editText by remember(material?.id) { mutableStateOf(material?.extractedText.orEmpty()) }
    val chosen = remember(material?.id) { mutableStateMapOf<String, String>() }
    var confirmDelete by remember { mutableStateOf(false) }
    var added by remember { mutableStateOf(false) }
    ScrollScreen(material?.title ?: s[K.LIBRARY_TITLE], actions = {
        if (material != null) IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Filled.Delete, s[K.DELETE]) }
    }) {
        if (materials == null) return@ScrollScreen
        if (material == null) { StateMessage(s[K.ERROR_TITLE], s[K.LESSON_NOT_FOUND]); return@ScrollScreen }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { Pill(Labels.materialType(s, material.type)); Pill(Labels.materialStatus(s, material.status)) }
        when (material.status) {
            MaterialStatus.UNSUPPORTED, MaterialStatus.FAILED -> Banner(BannerKind.WARNING, s[when (material.errorMessage) {
                "pdf_encrypted" -> K.MATERIAL_PDF_ENCRYPTED
                "pdf_no_text" -> K.MATERIAL_PDF_NO_TEXT
                "pdf_invalid" -> K.MATERIAL_PDF_INVALID
                "no_text_found" -> K.MATERIAL_NO_TEXT
                else -> K.MATERIAL_UNSUPPORTED_BODY
            }])
            else -> {
                if (material.type == MaterialType.PDF) Banner(BannerKind.INFO, s[K.MATERIAL_PDF_CHECK])
                SectionHeader(s[K.MATERIAL_TEXT_REVIEW])
                Text(s[K.MATERIAL_TEXT_EDIT_NOTE], style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                AppTextField(editText, { editText = it }, s[K.MATERIAL_TEXT_LABEL], singleLine = false, minLines = 6,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(localeList = com.polyglot.academy.ui.designsystem.localeFor(com.polyglot.academy.core.model.Languages.of(material.language).defaultVariant.localeTag)))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (editText != material.extractedText) SecondaryButton(s[K.SAVE], { scope.launch { app.services.materials.updateExtractedText(material, editText) } }, Modifier.weight(1f))
                    SecondaryButton(s[K.MATERIAL_READ], { speaker.toggle(editText.take(1500), material.language) }, Modifier.weight(1f))
                }
                val candidates = remember(material.extractedText) { app.services.materials.candidates(material) }
                SectionHeader(s[K.MATERIAL_CANDIDATES])
                candidates.forEach { cand ->
                    val known = cand.knownId?.let { Curriculum.of(material.language).vocabularyById[it] }
                    PolyCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = cand.term in chosen, onCheckedChange = { on -> if (on) chosen[cand.term] = known?.let { s.gloss(it.meaning) } ?: "" else chosen.remove(cand.term) })
                            Column(Modifier.weight(1f)) {
                                LearningText(cand.term, material.language, style = MaterialTheme.typography.titleMedium)
                                Text(listOfNotNull(s.f(K.MATERIAL_CANDIDATE_INFO, cand.frequency), known?.let { s[K.MATERIAL_IN_COURSE] + ": " + s.gloss(it.meaning) }).joinToString(" · "),
                                    style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                            }
                        }
                        if (cand.term in chosen && known == null) AppTextField(chosen[cand.term].orEmpty(), { chosen[cand.term] = it }, s[K.MATERIAL_ADD_MEANING])
                    }
                }
                val ready = chosen.isNotEmpty() && chosen.all { (term, meaning) -> meaning.isNotBlank() || candidates.firstOrNull { it.term == term }?.knownId != null }
                if (added) Banner(BannerKind.SUCCESS, s[K.MATERIAL_ADDED])
                PrimaryButton(s.plural(K.MATERIAL_ADD_SELECTED, chosen.size), {
                    scope.launch {
                        val pairs = candidates.filter { it.term in chosen }.map { it to chosen.getValue(it.term) }
                        app.services.materials.addToDeck(material, pairs)
                        chosen.clear(); added = true
                    }
                }, Modifier.fillMaxWidth(), enabled = ready)
            }
        }
    }
    if (confirmDelete && material != null) ConfirmDialog(s[K.MATERIAL_DELETE_TITLE], s[K.MATERIAL_DELETE_BODY], s[K.DELETE], s[K.CANCEL], destructive = true,
        onConfirm = { confirmDelete = false; scope.launch { app.services.materials.delete(material); app.nav.pop() } }, onDismiss = { confirmDelete = false })
}

// ---------------- Search ----------------

@Composable
fun SearchScreen() {
    val app = LocalApp.current
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val lang = app.profile.activeLearningLanguage
    val notes by remember { app.services.store.notes() }.collectAsState(emptyList())
    var query by rememberSaveable { mutableStateOf("") }
    ScrollScreen(s[K.SEARCH_TITLE]) {
        AppTextField(query, { query = it }, s[K.SEARCH_HINT])
        val q = TextNormalizer.loose(query)
        if (q.length < 2) { Text(s[K.SEARCH_START], color = c.textSecondary); return@ScrollScreen }
        val cur = Curriculum.of(lang)
        fun m(t: String) = TextNormalizer.loose(t).contains(q)
        val words = cur.vocabulary.filter { m(it.term) || m(it.meaning.en) || m(it.meaning.ar) || (it.pronunciation?.let(::m) ?: false) }.take(20)
        val lessons = cur.lessons.filter { m(it.title.en) || m(it.title.ar) || m(it.summary.en) || m(it.summary.ar) }
        val grammar = cur.grammar.filter { m(it.title.en) || m(it.title.ar) || m(it.rule.en) || m(it.rule.ar) }
        val readings = cur.readings.filter { r -> m(r.title) || r.sentences.any(::m) }
        val noteHits = notes.filter { m(it.title) || m(it.body) }
        if (words.isEmpty() && lessons.isEmpty() && grammar.isEmpty() && readings.isEmpty() && noteHits.isEmpty()) {
            StateMessage(s.f(K.SEARCH_EMPTY, query), ""); return@ScrollScreen
        }
        if (words.isNotEmpty()) { SectionHeader(s[K.SEARCH_WORDS]); words.forEach { v -> PolyCard { LearningText(v.term, lang, style = MaterialTheme.typography.titleMedium, reading = v.pronunciation); Text(s.gloss(v.meaning), color = c.textSecondary) } } }
        if (lessons.isNotEmpty()) { SectionHeader(s[K.SEARCH_LESSONS]); lessons.forEach { l -> PolyCard(onClick = { app.nav.push(Route.LessonOverview(l.id)) }) { Text(s.gloss(l.title), style = MaterialTheme.typography.titleSmall) } } }
        if (grammar.isNotEmpty()) { SectionHeader(s[K.SEARCH_GRAMMAR]); grammar.forEach { g -> PolyCard(onClick = { app.nav.push(Route.GrammarTopic(g.id)) }) { Text(s.gloss(g.title), style = MaterialTheme.typography.titleSmall) } } }
        if (readings.isNotEmpty()) { SectionHeader(s[K.SEARCH_READINGS]); readings.forEach { r -> PolyCard(onClick = { app.nav.push(Route.ReadingPassage(r.id)) }) { LearningText(r.title, lang, style = MaterialTheme.typography.titleSmall) } } }
        if (noteHits.isNotEmpty()) { SectionHeader(s[K.SEARCH_NOTES]); noteHits.forEach { n -> PolyCard { Text(n.title, style = MaterialTheme.typography.titleSmall); Text(n.body, maxLines = 2, color = c.textSecondary) } } }
    }
}

