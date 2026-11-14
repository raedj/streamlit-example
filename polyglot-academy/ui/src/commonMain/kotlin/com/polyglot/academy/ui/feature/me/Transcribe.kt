package com.polyglot.academy.ui.feature.me

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.polyglot.academy.core.media.DeviceTranscriber
import com.polyglot.academy.core.media.TranscriptionResult
import com.polyglot.academy.core.media.TranscriptionStage
import com.polyglot.academy.core.model.ImportedMaterial
import com.polyglot.academy.ui.app.LocalApp
import com.polyglot.academy.ui.app.rememberToast
import com.polyglot.academy.ui.designsystem.ConfirmDialog
import com.polyglot.academy.ui.designsystem.PolyCard
import com.polyglot.academy.ui.designsystem.PolyglotTheme
import com.polyglot.academy.ui.designsystem.PrimaryButton
import com.polyglot.academy.ui.designsystem.SecondaryButton
import com.polyglot.academy.ui.i18n.K
import com.polyglot.academy.ui.i18n.Labels
import com.polyglot.academy.ui.i18n.LocalStrings
import kotlinx.coroutines.launch

/** Private copies of picked audio and video, by material id, until they are transcribed. */
object PendingMediaFiles {
    private val paths = LinkedHashMap<String, String>()
    fun put(materialId: String, path: String) { paths[materialId] = path }
    fun get(materialId: String): String? = paths[materialId]
    fun remove(materialId: String): String? = paths.remove(materialId)
}

/** Transcribes speech on the device: a one-time model download with consent, then transcription. */
@Composable
internal fun TranscribeCard(material: ImportedMaterial) {
    val app = LocalApp.current
    val s = LocalStrings.current
    val c = PolyglotTheme.colors
    val scope = rememberCoroutineScope()
    val toast = rememberToast()
    val transcriber = app.services.materials.transcription ?: return
    val source = PendingMediaFiles.get(material.id)
    var ready by remember { mutableStateOf(transcriber.isModelReady()) }
    var download by remember { mutableStateOf<Float?>(null) }
    var confirm by remember { mutableStateOf(false) }
    var stage by remember { mutableStateOf<TranscriptionStage?>(null) }
    val megabytes = (transcriber.modelSizeBytes / 1_000_000).toInt()

    PolyCard {
        Text(s[K.TRANSCRIBE_TITLE], style = MaterialTheme.typography.titleSmall)
        Text(s.f(K.TRANSCRIBE_BODY, Labels.language(s, material.language), DeviceTranscriber.MAX_MINUTES), style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
        when {
            source == null -> Text(s[K.TRANSCRIBE_REIMPORT], style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
            !ready -> {
                Text(s.f(K.TRANSCRIBE_MODEL_NEEDED, megabytes), style = MaterialTheme.typography.bodySmall)
                download?.let { f ->
                    Text(s.f(K.TRANSCRIBE_DOWNLOADING, (f * 100).toInt()))
                    LinearProgressIndicator(progress = { f }, modifier = Modifier.fillMaxWidth(), drawStopIndicator = {})
                } ?: SecondaryButton(s.f(K.TRANSCRIBE_DOWNLOAD, megabytes), { confirm = true }, Modifier.fillMaxWidth())
            }
            stage != null -> {
                Text(s[if (stage == TranscriptionStage.DECODING) K.TRANSCRIBE_DECODING else K.TRANSCRIBE_RECOGNISING], style = MaterialTheme.typography.bodyMedium)
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            else -> PrimaryButton(s[K.TRANSCRIBE_START], {
                scope.launch {
                    stage = TranscriptionStage.DECODING
                    val (_, result) = app.services.materials.transcribe(material, source) { stage = it }
                    stage = null
                    if (result is TranscriptionResult.Text || result == TranscriptionResult.NoSpeech || result == TranscriptionResult.Unreadable) {
                        // The private copy is no longer needed.
                        PendingMediaFiles.remove(material.id)?.let(app.platformUi::deleteLocalFile)
                    }
                    toast(when (result) {
                        is TranscriptionResult.Text -> if (result.truncated) s.f(K.TRANSCRIBE_DONE_CUT, DeviceTranscriber.MAX_MINUTES) else s[K.TRANSCRIBE_DONE]
                        TranscriptionResult.NoSpeech -> s[K.TRANSCRIBE_NO_SPEECH]
                        TranscriptionResult.Unreadable -> s[K.TRANSCRIBE_UNREADABLE]
                        TranscriptionResult.ModelMissing -> { ready = false; s[K.TRANSCRIBE_MODEL_MISSING] }
                        is TranscriptionResult.Failed -> s[K.TRANSCRIBE_FAILED]
                    })
                }
            }, Modifier.fillMaxWidth())
        }
    }
    if (confirm) ConfirmDialog(s[K.TRANSCRIBE_CONFIRM_TITLE], s.f(K.TRANSCRIBE_CONFIRM_BODY, megabytes), s.f(K.TRANSCRIBE_DOWNLOAD, megabytes), s[K.CANCEL],
        onConfirm = {
            confirm = false
            scope.launch {
                download = 0f
                val r = transcriber.downloadModel { download = it }
                download = null
                ready = transcriber.isModelReady()
                if (r.isFailure) toast(s[K.TRANSCRIBE_DOWNLOAD_FAILED])
            }
        }, onDismiss = { confirm = false })
}
