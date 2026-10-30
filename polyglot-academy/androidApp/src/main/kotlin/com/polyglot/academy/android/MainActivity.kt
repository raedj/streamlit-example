package com.polyglot.academy.android

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.polyglot.academy.ui.app.PickedFile
import com.polyglot.academy.ui.app.PlatformUi
import com.polyglot.academy.ui.app.PolyglotApp
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity(), PlatformUi {
    private var permissionResult: CompletableDeferred<Boolean>? = null
    private var pickResult: CompletableDeferred<Uri?>? = null
    private var createResult: CompletableDeferred<Uri?>? = null

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { permissionResult?.complete(it) }
    // The system picker grants access to the chosen file only; no storage permission is needed.
    private val pickLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { pickResult?.complete(it) }
    private val createLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { createResult?.complete(it) }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val app = application as PolyglotApplication
        setContent { PolyglotApp(app.services, this) }
    }

    private suspend fun request(permission: String): Boolean {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) return true
        val d = CompletableDeferred<Boolean>().also { permissionResult = it }
        permissionLauncher.launch(permission)
        return d.await()
    }

    override suspend fun requestMicrophonePermission() = request(Manifest.permission.RECORD_AUDIO)
    override suspend fun requestNotificationPermission() = if (Build.VERSION.SDK_INT >= 33) request(Manifest.permission.POST_NOTIFICATIONS) else true

    override val canPickFiles = true
    override suspend fun pickFile(): PickedFile? {
        val d = CompletableDeferred<Uri?>().also { pickResult = it }
        pickLauncher.launch(arrayOf("text/plain", "text/markdown", "application/json", "application/x-subrip", "text/vtt", "application/pdf", "image/*", "audio/*", "video/*", "application/octet-stream"))
        val uri = d.await() ?: return null
        return withContext(Dispatchers.IO) {
            var name = "file"; var size = 0L
            contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)?.use { c ->
                if (c.moveToFirst()) { name = c.getString(0) ?: name; size = c.getLong(1) }
            }
            val mime = contentResolver.getType(uri) ?: "application/octet-stream"
            val lower = name.lowercase()
            val json = mime == "application/json" || lower.endsWith(".json")
            val textual = json || mime.startsWith("text/") || lower.endsWith(".srt") || lower.endsWith(".vtt") || lower.endsWith(".txt") || lower.endsWith(".md")
            // Read text in a bounded way; binary formats are not loaded into memory.
            val limit = if (json) 50L * 1024 * 1024 else 5L * 1024 * 1024
            val text = if (textual && size in 1..limit) contentResolver.openInputStream(uri)?.use { it.readBytes().decodeToString() } else null
            val pdf = mime == "application/pdf" || lower.endsWith(".pdf")
            // PDFs and photos are kept as bytes: PDF text is extracted on device, photos can be read with AI on request.
            val binaryLimit = if (pdf) 50L * 1024 * 1024 else if (mime.startsWith("image/")) 20L * 1024 * 1024 else 0L
            val bytes = if (size in 1..binaryLimit) contentResolver.openInputStream(uri)?.use { it.readBytes() } else null
            PickedFile(name, mime, size, text, bytes)
        }
    }

    override suspend fun saveExport(fileName: String, content: String): String? {
        val d = CompletableDeferred<Uri?>().also { createResult = it }
        createLauncher.launch(fileName)
        val uri = d.await() ?: return null
        withContext(Dispatchers.IO) { contentResolver.openOutputStream(uri)?.use { it.write(content.toByteArray()) } }
        return uri.lastPathSegment ?: fileName
    }

    override fun openAppSettings() {
        startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null)))
    }

    override val appVersion: String get() = packageManager.getPackageInfo(packageName, 0).versionName ?: "?"
    // Noto Nastaliq Urdu, SIL Open Font License 1.1 (docs/licenses/OFL-NotoNastaliqUrdu.txt).
    override val urduFont = androidx.compose.ui.text.font.FontFamily(androidx.compose.ui.text.font.Font(R.font.noto_nastaliq_urdu))
}
