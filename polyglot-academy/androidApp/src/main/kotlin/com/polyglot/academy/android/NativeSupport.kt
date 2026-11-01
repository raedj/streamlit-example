package com.polyglot.academy.android

import android.os.Build
import java.io.File

/**
 * Whether libpolyglot_ml.so can run here. Its x86_64 build uses AVX2, FMA and F16C (much faster on
 * emulators and Chromebooks); on an x86_64 CPU without them, loading it would crash, so the on-device
 * engines (transcription, chat) are offered only when the CPU reports those features. ARM builds use
 * the portable baseline and always run.
 */
internal object NativeSupport {
    val available: Boolean by lazy {
        if (Build.SUPPORTED_ABIS.firstOrNull() != "x86_64") return@lazy true
        val flags = runCatching { File("/proc/cpuinfo").readLines().firstOrNull { it.startsWith("flags") }.orEmpty() }.getOrDefault("")
            .substringAfter(':').trim().split(' ').toSet()
        flags.containsAll(listOf("avx2", "fma", "f16c"))
    }
}
