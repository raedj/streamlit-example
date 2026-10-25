package com.polyglot.academy.core.sync

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readRawBytes
import io.ktor.http.HttpMethod
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.appendPathSegments
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

sealed class SyncError(message: String) : Exception(message) {
    object NotConfigured : SyncError("not_configured")
    object InsecureUrl : SyncError("insecure_url")
    object Authentication : SyncError("authentication")
    object WrongPassphrase : SyncError("wrong_passphrase")
    object NotASyncFile : SyncError("not_a_sync_file")
    object NewerFormat : SyncError("newer_format")
    /** The server copy changed between reading and writing (another device synced). */
    object Changed : SyncError("changed")
    class Network(detail: String) : SyncError("network: $detail")
    class Server(val status: Int) : SyncError("server: HTTP $status")
}

/**
 * The encrypted file stored on the server. Only the format, KDF parameters and salt are readable;
 * the learning data is AES-256-GCM encrypted with a key derived from the learner's passphrase.
 */
@OptIn(ExperimentalEncodingApi::class)
object SyncEnvelope {
    const val FORMAT = "polyglot-academy-sync"
    const val VERSION = 1
    const val ITERATIONS = 210_000
    private val aad = "$FORMAT/v$VERSION".encodeToByteArray()
    private val json = Json { ignoreUnknownKeys = true }

    fun seal(plaintext: String, passphrase: String, iterations: Int = ITERATIONS): ByteArray {
        val salt = SyncCrypto.randomBytes(16)
        val key = SyncCrypto.deriveKey(passphrase, salt, iterations)
        val data = SyncCrypto.seal(key, plaintext.encodeToByteArray(), aad)
        key.fill(0)
        return buildJsonObject {
            put("format", FORMAT); put("version", VERSION); put("kdf", "PBKDF2-HMAC-SHA256"); put("iterations", iterations)
            put("salt", Base64.encode(salt)); put("cipher", "AES-256-GCM"); put("data", Base64.encode(data))
        }.toString().encodeToByteArray()
    }

    fun open(file: ByteArray, passphrase: String): String {
        val o = runCatching { json.parseToJsonElement(file.decodeToString()).jsonObject }.getOrNull() ?: throw SyncError.NotASyncFile
        fun s(k: String) = (o[k] as? JsonPrimitive)?.content
        if (s("format") != FORMAT) throw SyncError.NotASyncFile
        if ((s("version")?.toIntOrNull() ?: 0) > VERSION) throw SyncError.NewerFormat
        val iterations = s("iterations")?.toIntOrNull()?.takeIf { it in 10_000..5_000_000 } ?: throw SyncError.NotASyncFile
        val salt = s("salt")?.let { runCatching { Base64.decode(it) }.getOrNull() } ?: throw SyncError.NotASyncFile
        val data = s("data")?.let { runCatching { Base64.decode(it) }.getOrNull() } ?: throw SyncError.NotASyncFile
        val key = SyncCrypto.deriveKey(passphrase, salt, iterations)
        try {
            return SyncCrypto.open(key, data, aad).decodeToString()
        } catch (e: Exception) {
            throw SyncError.WrongPassphrase
        } finally {
            key.fill(0)
        }
    }

    /** Fingerprint of the learning data, ignoring the export time and device settings. */
    fun fingerprint(exportJson: String): String {
        val o = runCatching { json.parseToJsonElement(exportJson).jsonObject }.getOrNull() ?: return ""
        val stable = JsonObject(o.filterKeys { it != "exportedAt" && it != "settings" })
        return SyncCrypto.sha256(stable.toString().encodeToByteArray()).joinToString("") { (it.toInt() and 0xFF).toString(16).padStart(2, '0') }
    }
}

data class RemoteFile(val bytes: ByteArray, val etag: String?)

/** Where the encrypted file lives. */
interface SyncRemote {
    suspend fun read(): RemoteFile?
    /** Writes the file; [ifMatch] = expected current ETag (null = must not exist yet). Returns the new ETag if the server reports one. */
    suspend fun write(bytes: ByteArray, ifMatch: String?, exists: Boolean): String?
}

/** WebDAV (Nextcloud, ownCloud, many NAS devices and hosting providers) with Basic authentication. */
class WebDavRemote(
    private val http: HttpClient,
    serverUrl: String,
    private val username: String,
    private val password: String,
    private val timeoutMillis: Long = 60_000,
) : SyncRemote {
    private val folder: Url
    private val file: Url

    init {
        val base = runCatching { Url(serverUrl.trim()) }.getOrNull() ?: throw SyncError.NotConfigured
        if (!isAllowed(base)) throw SyncError.InsecureUrl
        folder = URLBuilder(base).appendPathSegments(FOLDER, "").build()
        file = URLBuilder(base).appendPathSegments(FOLDER, FILE).build()
    }

    @OptIn(ExperimentalEncodingApi::class)
    private val auth = "Basic " + Base64.encode("$username:$password".encodeToByteArray())

    override suspend fun read(): RemoteFile? {
        val r = call(HttpMethod.Get, file)
        return when (r.status.value) {
            404 -> null
            in 200..299 -> RemoteFile(r.readRawBytes(), r.headers["ETag"])
            else -> throw statusError(r.status.value)
        }
    }

    override suspend fun write(bytes: ByteArray, ifMatch: String?, exists: Boolean): String? {
        if (!exists) {
            val m = call(HttpMethod("MKCOL"), folder)
            // 405 = folder already exists.
            if (m.status.value !in 200..299 && m.status.value != 405) throw statusError(m.status.value)
        }
        val r = call(HttpMethod.Put, file) {
            when {
                ifMatch != null -> header("If-Match", ifMatch)
                !exists -> header("If-None-Match", "*")
            }
            header("Content-Type", "application/json")
            setBody(bytes)
        }
        if (r.status.value == 412) throw SyncError.Changed
        if (r.status.value !in 200..299) throw statusError(r.status.value)
        return r.headers["ETag"]
    }

    private fun statusError(status: Int) = if (status == 401 || status == 403) SyncError.Authentication else SyncError.Server(status)

    private suspend fun call(method: HttpMethod, url: Url, extra: io.ktor.client.request.HttpRequestBuilder.() -> Unit = {}): HttpResponse = try {
        withTimeout(timeoutMillis) {
            http.request(url) { this.method = method; header("Authorization", auth); extra() }
        }
    } catch (e: TimeoutCancellationException) {
        throw SyncError.Network("timeout")
    } catch (e: CancellationException) {
        throw e
    } catch (e: SyncError) {
        throw e
    } catch (e: Exception) {
        throw SyncError.Network(e::class.simpleName ?: "IO")
    }

    companion object {
        const val FOLDER = "polyglot-academy"
        const val FILE = "sync.json"

        /** HTTPS everywhere; plain HTTP only for this device or the local network. */
        fun isAllowed(url: Url): Boolean = when (url.protocol.name) {
            "https" -> true
            "http" -> url.host == "localhost" || url.host.startsWith("127.") || url.host.startsWith("10.") || url.host.startsWith("192.168.") ||
                Regex("^172\\.(1[6-9]|2[0-9]|3[01])\\.").containsMatchIn(url.host) || url.host.endsWith(".local")
            else -> false
        }
    }
}
