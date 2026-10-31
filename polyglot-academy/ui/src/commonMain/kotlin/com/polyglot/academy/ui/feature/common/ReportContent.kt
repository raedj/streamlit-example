package com.polyglot.academy.ui.feature.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.polyglot.academy.core.model.ContentIssue
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.ui.app.LocalApp
import com.polyglot.academy.ui.app.rememberToast
import com.polyglot.academy.ui.designsystem.AppTextField
import com.polyglot.academy.ui.designsystem.ChipGroup
import com.polyglot.academy.ui.designsystem.PolyglotTheme
import com.polyglot.academy.ui.designsystem.QuietButton
import com.polyglot.academy.ui.i18n.K
import com.polyglot.academy.ui.i18n.LocalStrings
import com.polyglot.academy.ui.i18n.Localizer
import kotlinx.coroutines.launch

internal fun issueLabel(s: Localizer, issue: ContentIssue): String = when (issue) {
    ContentIssue.TRANSLATION -> s[K.REPORT_ISSUE_TRANSLATION]
    ContentIssue.EXAMPLE -> s[K.REPORT_ISSUE_EXAMPLE]
    ContentIssue.PRONUNCIATION -> s[K.REPORT_ISSUE_PRONUNCIATION]
    ContentIssue.AMBIGUOUS_ANSWER -> s[K.REPORT_ISSUE_AMBIGUOUS]
    ContentIssue.LEVEL -> s[K.REPORT_ISSUE_LEVEL]
    ContentIssue.OTHER -> s[K.REPORT_ISSUE_OTHER]
}

/**
 * "Report a content error": the learner picks what is wrong and may add a note. The report is kept on
 * the device (Settings → Your data → Content reports) and can be exported for reviewers.
 */
@Composable
fun ReportContentButton(language: LanguageCode, itemId: String, itemText: String) {
    val app = LocalApp.current
    val s = LocalStrings.current
    val scope = rememberCoroutineScope()
    val toast = rememberToast()
    var open by remember { mutableStateOf(false) }
    var issue by remember { mutableStateOf<ContentIssue?>(null) }
    var note by remember { mutableStateOf("") }
    QuietButton(s[K.REPORT_BUTTON], { open = true }, color = PolyglotTheme.colors.textSecondary)
    if (open) AlertDialog(
        onDismissRequest = { open = false },
        title = { Text(s[K.REPORT_TITLE]) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(s[K.REPORT_BODY], style = MaterialTheme.typography.bodySmall)
                ChipGroup(ContentIssue.entries, setOfNotNull(issue), { issueLabel(s, it) }, { issue = it })
                AppTextField(note, { note = it }, s[K.REPORT_NOTE], singleLine = false, minLines = 2)
            }
        },
        confirmButton = {
            QuietButton(s[K.REPORT_SAVE], {
                val chosen = issue ?: return@QuietButton
                open = false
                scope.launch {
                    app.services.contentReports.report(language, itemId, itemText, chosen, note)
                    issue = null; note = ""
                    toast(s[K.REPORT_SAVED])
                }
            }, enabled = issue != null)
        },
        dismissButton = { QuietButton(s[K.CANCEL], { open = false }) },
        containerColor = PolyglotTheme.colors.surface,
    )
}
