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
)

enum class LocalRuntime { LLAMA_CPP_GGUF, MEDIAPIPE_LLM }

sealed interface Compatibility {
    data object Compatible : Compatibility
    data class Incompatible(val reasons: List<IncompatibilityReason>) : Compatibility
}

enum class IncompatibilityReason { RUNTIME_NOT_BUNDLED, NOT_64_BIT, INSUFFICIENT_RAM, INSUFFICIENT_STORAGE, OS_TOO_OLD }

/**
 * Catalog of candidate on-device models. This build does NOT bundle an inference runtime, so
 * every model is reported as incompatible with [IncompatibilityReason.RUNTIME_NOT_BUNDLED] and
 * nothing is downloaded. The compatibility logic is real so it can be enabled when a runtime is
 * added (see docs/AI_PROVIDER_MATRIX.md).
 */
object LocalModelCatalog {
    val models = listOf(
        LocalModelSpec("qwen2.5-0.5b-instruct-q4", "Qwen2.5 0.5B Instruct (Q4 GGUF)", 400, 2_000, "Apache-2.0",
            "https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF", LocalRuntime.LLAMA_CPP_GGUF, "Multilingual incl. zh/en/es/fr/ar; quality limited at this size"),
        LocalModelSpec("qwen2.5-1.5b-instruct-q4", "Qwen2.5 1.5B Instruct (Q4 GGUF)", 1_100, 4_000, "Apache-2.0",
            "https://huggingface.co/Qwen/Qwen2.5-1.5B-Instruct-GGUF", LocalRuntime.LLAMA_CPP_GGUF, "Better conversation quality; needs a recent mid/high-end phone"),
        LocalModelSpec("gemma-3-1b-it-int4", "Gemma 3 1B IT (int4)", 550, 3_000, "Gemma Terms of Use",
            "https://huggingface.co/litert-community/Gemma3-1B-IT", LocalRuntime.MEDIAPIPE_LLM, "Licence terms must be accepted by the user before download"),
    )

    val bundledRuntimes: Set<LocalRuntime> = emptySet()

    fun compatibility(model: LocalModelSpec, device: DeviceInfo): Compatibility {
        val reasons = buildList {
            if (model.runtime !in bundledRuntimes) add(IncompatibilityReason.RUNTIME_NOT_BUNDLED)
            if (!device.is64Bit) add(IncompatibilityReason.NOT_64_BIT)
            // Model weights plus KV cache and app overhead must fit comfortably.
            if (device.totalRamMb < model.minRamMb) add(IncompatibilityReason.INSUFFICIENT_RAM)
            if (device.availableStorageMb < model.downloadSizeMb * 2) add(IncompatibilityReason.INSUFFICIENT_STORAGE)
            if (device.osApiLevel in 1..27) add(IncompatibilityReason.OS_TOO_OLD)
        }
        return if (reasons.isEmpty()) Compatibility.Compatible else Compatibility.Incompatible(reasons)
    }
}
