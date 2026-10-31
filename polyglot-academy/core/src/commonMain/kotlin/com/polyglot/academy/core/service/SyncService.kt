package com.polyglot.academy.core.service

import com.polyglot.academy.core.model.SyncSettings
import com.polyglot.academy.core.repo.LearningStore
import com.polyglot.academy.core.repo.SecretStore
import com.polyglot.academy.core.sync.SyncEnvelope
import com.polyglot.academy.core.sync.SyncError
import com.polyglot.academy.core.sync.SyncRemote
import com.polyglot.academy.core.util.TimeSource
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonObject

enum class SyncOutcome { UPLOADED, DOWNLOADED, UP_TO_DATE, CONFLICT }

enum class ConflictChoice { KEEP_THIS_DEVICE, USE_SERVER }

/**
 * Keeps devices in step through one end-to-end encrypted file on the learner's WebDAV server.
 * Whole-state sync: the newer side wins; when both sides changed since the last sync the learner
 * chooses (there is no record-by-record merge). Device settings stay per device.
 */
class SyncService(
    private val store: LearningStore,
    private val secrets: SecretStore,
    private val clock: TimeSource,
    private val remoteFactory: (url: String, user: String, password: String) -> SyncRemote,
) {
    suspend fun settings(): SyncSettings = store.currentSettings().sync

    suspend fun isConfigured(): Boolean {
        val s = settings()
        return s.serverUrl.isNotBlank() && !secrets.get(PASSWORD).isNullOrEmpty() && !secrets.get(PASSPHRASE).isNullOrEmpty()
    }

    /** Saves the connection. Blank password/passphrase keep the stored ones. Returns false for an unusable URL. */
    suspend fun configure(url: String, username: String, password: String?, passphrase: String?): Result<Unit> = runCatching {
        val clean = url.trim().removeSuffix("/")
        val parsed = runCatching { io.ktor.http.Url(clean) }.getOrNull()?.takeIf { it.host.isNotBlank() } ?: throw SyncError.NotConfigured
        if (!com.polyglot.academy.core.sync.WebDavRemote.isAllowed(parsed)) throw SyncError.InsecureUrl
        if (passphrase != null && passphrase.isNotEmpty() && passphrase.length < MIN_PASSPHRASE) throw IllegalArgumentException("passphrase_too_short")
        val previous = settings()
        val serverChanged = previous.serverUrl != clean || previous.username != username.trim()
        store.updateSettings { st -> st.copy(sync = st.sync.copy(serverUrl = clean, username = username.trim()).let {
            if (serverChanged) it.copy(lastLocalFingerprint = null, lastRemoteFingerprint = null, lastSyncAtMillis = null) else it }) }
        password?.takeIf { it.isNotEmpty() }?.let { secrets.put(PASSWORD, it) }
        passphrase?.takeIf { it.isNotEmpty() }?.let { secrets.put(PASSPHRASE, it) }
    }

    suspend fun setIncluded(conversations: Boolean, notes: Boolean) =
        store.updateSettings { it.copy(sync = it.sync.copy(includeConversations = conversations, includeNotes = notes)) }

    /** Removes the connection and stored secrets from this device (the server copy is untouched). */
    suspend fun disconnect() {
        secrets.remove(PASSWORD); secrets.remove(PASSPHRASE)
        store.updateSettings { it.copy(sync = SyncSettings()) }
    }

    suspend fun sync(choice: ConflictChoice? = null): Result<SyncOutcome> = runCatching {
        val s = settings()
        val password = secrets.get(PASSWORD); val passphrase = secrets.get(PASSPHRASE)
        if (s.serverUrl.isBlank() || password.isNullOrEmpty() || passphrase.isNullOrEmpty()) throw SyncError.NotConfigured
        val remote = remoteFactory(s.serverUrl, s.username, password)
        val local = store.exportJson(s.includeConversations, s.includeNotes)
        val localFp = SyncEnvelope.fingerprint(local)
        val file = remote.read() ?: return@runCatching upload(remote, local, localFp, null, exists = false)
        val remoteJson = SyncEnvelope.open(file.bytes, passphrase)
        val remoteFp = SyncEnvelope.fingerprint(remoteJson)
        if (remoteFp == localFp) { remember(localFp, remoteFp); return@runCatching SyncOutcome.UP_TO_DATE }
        val localChanged = localFp != s.lastLocalFingerprint && !isEmpty(local)
        val remoteChanged = remoteFp != s.lastRemoteFingerprint
        when {
            localChanged && remoteChanged -> when (choice) {
                ConflictChoice.KEEP_THIS_DEVICE -> upload(remote, local, localFp, file.etag, exists = true)
                ConflictChoice.USE_SERVER -> download(remoteJson, remoteFp)
                null -> SyncOutcome.CONFLICT
            }
            remoteChanged -> download(remoteJson, remoteFp)
            localChanged -> upload(remote, local, localFp, file.etag, exists = true)
            else -> { remember(localFp, remoteFp); SyncOutcome.UP_TO_DATE }
        }
    }

    private suspend fun upload(remote: SyncRemote, local: String, fp: String, etag: String?, exists: Boolean): SyncOutcome {
        remote.write(SyncEnvelope.seal(local, secrets.get(PASSPHRASE)!!), etag, exists)
        remember(fp, fp)
        return SyncOutcome.UPLOADED
    }

    private suspend fun download(remoteJson: String, remoteFp: String): SyncOutcome {
        val deviceSettings = store.currentSettings()
        val preferred = store.preferredProvider.first()
        store.importJson(remoteJson)
        // Keep this device's own settings (theme, privacy choices, voice services, sync state) and preferred AI provider.
        store.updateSettings { deviceSettings }
        store.setPreferred(preferred)
        val s = settings()
        remember(SyncEnvelope.fingerprint(store.exportJson(s.includeConversations, s.includeNotes)), remoteFp)
        return SyncOutcome.DOWNLOADED
    }

    private suspend fun remember(localFp: String, remoteFp: String) = store.updateSettings {
        it.copy(sync = it.sync.copy(lastLocalFingerprint = localFp, lastRemoteFingerprint = remoteFp, lastSyncAtMillis = clock.nowMillis()))
    }

    /** A device with no learning activity yet should simply take the server copy. */
    private fun isEmpty(export: String): Boolean {
        val o = runCatching { Json.parseToJsonElement(export).jsonObject }.getOrNull() ?: return false
        return listOf("attempts", "sessions", "reviewStates", "lessonProgress", "conversations", "userVocabulary").all { (o[it] as? JsonArray).isNullOrEmpty() }
    }

    companion object {
        const val PASSWORD = "sync.webdav.password"
        const val PASSPHRASE = "sync.passphrase"
        const val MIN_PASSPHRASE = 10
    }
}
