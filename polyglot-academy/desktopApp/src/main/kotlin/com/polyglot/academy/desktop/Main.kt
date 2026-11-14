package com.polyglot.academy.desktop

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.polyglot.academy.core.ai.defaultHttpClient
import com.polyglot.academy.core.service.AppServices
import com.polyglot.academy.data.SqlStore
import com.polyglot.academy.data.createJvmDriver
import com.polyglot.academy.ui.app.PolyglotApp
import com.polyglot.academy.ui.platform.DesktopPlatformServices
import com.polyglot.academy.ui.platform.DesktopPlatformUi
import kotlinx.coroutines.Dispatchers
import java.io.File

/**
 * Developer verification harness: runs the shared Compose UI on the JVM with a real SQLite
 * database so screens can be exercised without an Android device. Not a shipping product.
 */
fun main() {
    val dir = File(System.getProperty("user.home"), ".polyglot-academy").apply { mkdirs() }
    val store = SqlStore(createJvmDriver("jdbc:sqlite:${File(dir, "polyglot.db").absolutePath}"), Dispatchers.IO)
    val services = AppServices(store, DesktopPlatformServices(), defaultHttpClient())
    application {
        Window(onCloseRequest = ::exitApplication, title = "Polyglot Academy (desktop harness)", state = rememberWindowState(width = 420.dp, height = 880.dp)) {
            PolyglotApp(services, DesktopPlatformUi())
        }
    }
}
