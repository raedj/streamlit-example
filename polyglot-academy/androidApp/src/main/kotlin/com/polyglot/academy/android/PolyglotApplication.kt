package com.polyglot.academy.android

import android.app.Application
import com.polyglot.academy.core.ai.defaultHttpClient
import com.polyglot.academy.core.service.AppEdition
import com.polyglot.academy.core.service.AppServices
import com.polyglot.academy.data.SqlStore
import com.polyglot.academy.data.createAndroidDriver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/** Owns the single dependency graph for the process (ADR-004: manual DI). */
class PolyglotApplication : Application() {
    lateinit var services: AppServices
        private set
    lateinit var platform: AndroidPlatformServices
        private set
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        platform = AndroidPlatformServices(this)
        // Copies of audio and video kept for transcription do not outlive the process that picked them.
        java.io.File(cacheDir, "media").deleteRecursively()
        val store = SqlStore(createAndroidDriver(this), Dispatchers.IO)
        services = AppServices(store, platform, defaultHttpClient(), edition = if (BuildConfig.TRIAL) AppEdition.TRIAL else AppEdition.FULL)
        // Keep the optional, secret-free backup file in sync with the learner's choice.
        appScope.launch {
            store.settings.map { it.privacy.includeInBackup }.distinctUntilChanged().collect { enabled ->
                BackupFiles.update(this@PolyglotApplication, if (enabled) store.exportJson(includeConversations = false, includeNotes = false) else null)
            }
        }
        // Background sync follows the setting; turning it off or disconnecting cancels the job.
        appScope.launch {
            store.settings.map { it.sync.autoSync to it.sync.autoSyncUnmeteredOnly }.distinctUntilChanged().collect { (on, unmetered) ->
                AutoSyncJob.schedule(this@PolyglotApplication, on, unmetered)
            }
        }
    }
}

object BackupFiles {
    fun update(context: android.content.Context, json: String?) {
        val dir = java.io.File(context.filesDir, "backup")
        val f = java.io.File(dir, "learning-data.json")
        if (json == null) { f.delete(); return }
        dir.mkdirs(); f.writeText(json)
    }
}
