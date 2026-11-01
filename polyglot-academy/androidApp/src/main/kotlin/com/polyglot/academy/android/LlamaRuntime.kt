package com.polyglot.academy.android

import android.content.Context
import com.polyglot.academy.core.localai.LocalLlmRuntime
import com.polyglot.academy.core.localai.LocalModelSpec
import com.polyglot.academy.core.localai.LocalRuntime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

/** llama.cpp through JNI (src/main/cpp/polyglot_llama.cpp), in the same library as whisper.cpp. */
internal object LlamaNative {
    init { System.loadLibrary("polyglot_ml") }
    external fun load(path: String, contextTokens: Int, threads: Int): Long
    external fun free(handle: Long)
    external fun cancel(handle: Long)
    external fun chat(handle: Long, roles: Array<String>, contents: Array<ByteArray>, maxTokens: Int, temperature: Float): ByteArray?
}

/**
 * Runs Qwen2.5 GGUF chat models on the phone. A model stays loaded between turns (loading takes
 * seconds) and is released when another model is chosen or deleted. One answer at a time; leaving
 * the conversation cancels generation. Models live in noBackupFilesDir, never in backups.
 */
class LlamaRuntime(private val context: Context) : LocalLlmRuntime {
    companion object { const val CONTEXT_TOKENS = 2048 }

    override val runtime = LocalRuntime.LLAMA_CPP_GGUF
    private val lock = Mutex()
    private var loaded: Pair<String, Long>? = null

    private fun file(model: LocalModelSpec) = File(context.noBackupFilesDir, "llm/${model.fileName}")

    override fun isDownloaded(model: LocalModelSpec) = file(model).length() == model.sizeBytes

    override suspend fun download(model: LocalModelSpec, onProgress: (Float) -> Unit): Result<Unit> =
        ModelDownload.fetch(model.downloadUrl, model.sizeBytes, model.sha256, file(model), onProgress)

    override fun delete(model: LocalModelSpec) {
        loaded?.takeIf { it.first == model.id }?.let { (_, handle) -> LlamaNative.free(handle); loaded = null }
        file(model).delete()
    }

    override suspend fun chat(model: LocalModelSpec, messages: List<Pair<String, String>>, maxTokens: Int, temperature: Float): String =
        lock.withLock {
            withContext(Dispatchers.Default) {
                val handle = handleFor(model)
                // Leaving the screen cancels the coroutine; the native loop stops at the next token.
                suspendCancellableCoroutine { cont ->
                    cont.invokeOnCancellation { LlamaNative.cancel(handle) }
                    val bytes = LlamaNative.chat(handle, messages.map { it.first }.toTypedArray(), messages.map { it.second.encodeToByteArray() }.toTypedArray(), maxTokens, temperature)
                    when {
                        bytes == null -> cont.resumeWith(Result.failure(IllegalStateException("generation failed")))
                        bytes.isEmpty() -> cont.resumeWith(Result.failure(IllegalStateException("conversation too long for the model")))
                        else -> cont.resumeWith(Result.success(bytes.decodeToString()))
                    }
                }
            }
        }

    private fun handleFor(model: LocalModelSpec): Long {
        loaded?.let { (id, handle) -> if (id == model.id) return handle else { LlamaNative.free(handle); loaded = null } }
        check(isDownloaded(model)) { "model not downloaded" }
        val threads = Runtime.getRuntime().availableProcessors().coerceIn(2, 4)
        val handle = LlamaNative.load(file(model).path, CONTEXT_TOKENS, threads)
        check(handle != 0L) { "model could not be loaded" }
        loaded = model.id to handle
        return handle
    }
}
