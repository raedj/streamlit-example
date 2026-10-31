package com.polyglot.academy.ui.app

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.polyglot.academy.core.model.AppSettings
import com.polyglot.academy.core.model.LearnerProfile
import com.polyglot.academy.core.service.AppServices
import com.polyglot.academy.ui.navigation.Navigator
import kotlinx.coroutines.CancellationException

/** A file chosen through the platform picker. Text is decoded by the platform when the type is textual. */
/** A picked file: [text] for text formats, [bytes] for PDFs (other binaries carry neither). */
class PickedFile(val name: String, val mimeType: String, val sizeBytes: Long, val text: String?, val bytes: ByteArray? = null)

/** UI-side platform hooks (permission dialogs, file picker, saving exports). */
interface PlatformUi {
    val canPickFiles: Boolean
    suspend fun pickFile(): PickedFile?
    suspend fun requestMicrophonePermission(): Boolean
    suspend fun requestNotificationPermission(): Boolean
    /** Saves an export and returns a human-readable location, or null if the user cancelled. */
    suspend fun saveExport(fileName: String, content: String): String?
    fun openAppSettings()
    val appVersion: String
    /** Nastaliq font for Urdu learning content; null uses the platform default (Naskh-style). */
    val urduFont: androidx.compose.ui.text.font.FontFamily? get() = null
}

object NoopPlatformUi : PlatformUi {
    override val canPickFiles = false
    override suspend fun pickFile(): PickedFile? = null
    override suspend fun requestMicrophonePermission() = false
    override suspend fun requestNotificationPermission() = false
    override suspend fun saveExport(fileName: String, content: String): String? = null
    override fun openAppSettings() {}
    override val appVersion = "dev"
}

@Stable
class AppScope(
    val services: AppServices,
    val nav: Navigator,
    val platformUi: PlatformUi,
    val snackbar: SnackbarHostState,
    val profile: LearnerProfile,
    val settings: AppSettings,
)

val LocalApp = staticCompositionLocalOf<AppScope> { error("AppScope not provided") }

sealed interface Load<out T> {
    data object Loading : Load<Nothing>
    data class Ready<T>(val value: T) : Load<T>
    data class Failed(val error: Throwable) : Load<Nothing>
}

@Stable
class Loader<T>(private val trigger: () -> Unit) {
    var state: Load<T> by mutableStateOf(Load.Loading)
        internal set
    fun reload() = trigger()
}

/**
 * Loads data off the composition, keyed so it reloads when inputs change; failures become a retryable
 * state. The block runs on [kotlinx.coroutines.Dispatchers.Default]: building a language's curriculum or
 * word list the first time is enough work to freeze the screen if done on the main thread.
 */
@Composable
fun <T> rememberLoader(vararg keys: Any?, block: suspend () -> T): Loader<T> {
    var generation by remember { mutableIntStateOf(0) }
    val loader = remember(*keys) { Loader<T> { generation++ } }
    LaunchedEffect(loader, generation) {
        if (loader.state !is Load.Ready) loader.state = Load.Loading
        loader.state = try { Load.Ready(kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) { block() }) }
            catch (e: CancellationException) { throw e } catch (e: Throwable) { Load.Failed(e) }
    }
    return loader
}
