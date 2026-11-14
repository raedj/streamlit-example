package com.polyglot.academy.android

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import kotlin.coroutines.coroutineContext

/**
 * Downloads a model file over HTTPS into [target], checking its size and SHA-256 before it replaces
 * anything. Large models (up to 1.1 GB) are often interrupted on phones, so a dropped connection is
 * resumed where it stopped (HTTP Range) up to [ATTEMPTS] times; the partial file is kept for that and
 * deleted only when the finished file fails the check. Only the file is requested.
 */
internal object ModelDownload {
    private const val ATTEMPTS = 5

    suspend fun fetch(url: String, size: Long, sha256: String, target: File, onProgress: (Float) -> Unit): Result<Unit> = withContext(Dispatchers.IO) {
        val part = File(target.parentFile, target.name + ".part")
        target.parentFile?.mkdirs()
        var lastError: Throwable? = null
        repeat(ATTEMPTS) { attempt ->
            coroutineContext.ensureActive()
            try {
                if (part.length() > size) part.delete()
                if (part.length() < size) receive(url, size, part, onProgress)
                // The whole file is checked, including what earlier attempts wrote.
                val sha = MessageDigest.getInstance("SHA-256").let { d ->
                    part.inputStream().use { input -> val b = ByteArray(1 shl 20); while (true) { val n = input.read(b); if (n < 0) break; d.update(b, 0, n) } }
                    d.digest().joinToString("") { "%02x".format(it) }
                }
                if (part.length() != size || sha != sha256) { part.delete(); return@withContext Result.failure(IllegalStateException("checksum")) }
                if (!part.renameTo(target)) return@withContext Result.failure(IllegalStateException("rename"))
                return@withContext Result.success(Unit)
            } catch (e: IOException) {
                lastError = e
                delay(2_000L * (attempt + 1))
            }
        }
        Result.failure(lastError ?: IllegalStateException("download failed"))
    }

    /** Appends to [part] from where it ends; restarts from zero when the server ignores the range. */
    private suspend fun receive(url: String, size: Long, part: File, onProgress: (Float) -> Unit) {
        val start = part.length()
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 20_000; readTimeout = 60_000
            if (start > 0) setRequestProperty("Range", "bytes=$start-")
        }
        try {
            val append = when (connection.responseCode) {
                206 -> true
                200 -> false
                else -> throw IOException("HTTP ${connection.responseCode}")
            }
            var done = if (append) start else 0L
            connection.inputStream.use { input ->
                FileOutputStream(part, append).use { out ->
                    val buffer = ByteArray(256 * 1024)
                    while (true) {
                        coroutineContext.ensureActive()
                        val n = input.read(buffer)
                        if (n < 0) break
                        out.write(buffer, 0, n)
                        done += n
                        onProgress((done.toFloat() / size).coerceAtMost(1f))
                    }
                }
            }
        } finally {
            connection.disconnect()
        }
    }
}
