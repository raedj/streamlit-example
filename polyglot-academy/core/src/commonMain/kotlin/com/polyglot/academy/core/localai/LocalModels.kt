package com.polyglot.academy.core.localai

import com.polyglot.academy.core.device.DeviceInfo

data class LocalModelSpec(
    val id: String,
    val displayName: String,
    val downloadSizeMb: Long,
    val minRamMb: Long,
    val license: String,
    val sourceUrl: String,
    val runtime: LocalRuntime,
    val languagesNote: String,
    /** The file the app downloads, its exact size and SHA-256 (checked before the model is used). */
    val downloadUrl: String,
    val sizeBytes: Long,
    val sha256: String,
    val fileName: String,
)

enum class LocalRuntime { LLAMA_CPP_GGUF }

sealed interface Compatibility {
    data object Compatible : Compatibility
    data class Incompatible(val reasons: List<IncompatibilityReason>) : Compatibility
}

enum class IncompatibilityReason { RUNTIME_NOT_BUNDLED, NOT_64_BIT, INSUFFICIENT_RAM, INSUFFICIENT_STORAGE, OS_TOO_OLD }

/**
 * Chat models that run on the phone with llama.cpp (Android). Both are Qwen2.5 Instruct, Apache-2.0,
 * 4-bit (Q4_K_M) GGUF files from the Qwen organisation on Hugging Face. They are downloaded only on the
 * learner's request; nothing is bundled. See docs/AI_PROVIDER_MATRIX.md.
 */
object LocalModelCatalog {
    val models = listOf(
        LocalModelSpec("qwen2.5-0.5b-instruct-q4", "Qwen2.5 0.5B Instruct (Q4_K_M)", 491, 2_000, "Apache-2.0",
            "https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF", LocalRuntime.LLAMA_CPP_GGUF,
            "Multilingual incl. zh/en/es/fr/ar; quality limited at this size",
            "https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF/resolve/main/qwen2.5-0.5b-instruct-q4_k_m.gguf",
            491_400_032, "74a4da8c9fdbcd15bd1f6d01d621410d31c6fc00986f5eb687824e7b93d7a9db", "qwen2.5-0.5b-instruct-q4_k_m.gguf"),
        LocalModelSpec("qwen2.5-1.5b-instruct-q4", "Qwen2.5 1.5B Instruct (Q4_K_M)", 1_117, 4_000, "Apache-2.0",
            "https://huggingface.co/Qwen/Qwen2.5-1.5B-Instruct-GGUF", LocalRuntime.LLAMA_CPP_GGUF,
            "Better conversation quality; needs a recent mid/high-end phone",
            "https://huggingface.co/Qwen/Qwen2.5-1.5B-Instruct-GGUF/resolve/main/qwen2.5-1.5b-instruct-q4_k_m.gguf",
            1_117_320_736, "6a1a2eb6d15622bf3c96857206351ba97e1af16c30d7a74ee38970e434e9407e", "qwen2.5-1.5b-instruct-q4_k_m.gguf"),
    )

    fun byId(id: String?): LocalModelSpec? = models.firstOrNull { it.id == id }

    /** [runtimes] are the runtimes this build of the app includes (llama.cpp on Android, none on desktop). */
    fun compatibility(model: LocalModelSpec, device: DeviceInfo, runtimes: Set<LocalRuntime> = emptySet()): Compatibility {
        val reasons = buildList {
            if (model.runtime !in runtimes) add(IncompatibilityReason.RUNTIME_NOT_BUNDLED)
            if (!device.is64Bit) add(IncompatibilityReason.NOT_64_BIT)
            // Model weights plus KV cache and app overhead must fit comfortably.
            if (device.totalRamMb < model.minRamMb) add(IncompatibilityReason.INSUFFICIENT_RAM)
            if (device.availableStorageMb < model.downloadSizeMb * 2) add(IncompatibilityReason.INSUFFICIENT_STORAGE)
            if (device.osApiLevel in 1..27) add(IncompatibilityReason.OS_TOO_OLD)
        }
        return if (reasons.isEmpty()) Compatibility.Compatible else Compatibility.Incompatible(reasons)
    }
}

/** A chat model runtime on the device (llama.cpp on Android). Models are files the learner chose to download. */
interface LocalLlmRuntime {
    val runtime: LocalRuntime
    fun isDownloaded(model: LocalModelSpec): Boolean
    /** Downloads and checks the model (size and SHA-256); [onProgress] gets 0..1. */
    suspend fun download(model: LocalModelSpec, onProgress: (Float) -> Unit): Result<Unit>
    fun delete(model: LocalModelSpec)
    /** [messages] are (role, content) pairs, system first; returns the answer, or throws. */
    suspend fun chat(model: LocalModelSpec, messages: List<Pair<String, String>>, maxTokens: Int, temperature: Float): String
}
