package com.polyglot.academy.core.media

import java.io.ByteArrayOutputStream
import java.util.zip.DataFormatException
import java.util.zip.Inflater

internal actual fun inflateZlib(data: ByteArray, maxOutput: Int): ByteArray? {
    // Try zlib-wrapped first (the PDF norm), then raw deflate, which some writers emit.
    for (nowrap in listOf(false, true)) {
        val inflater = Inflater(nowrap)
        try {
            inflater.setInput(data)
            val out = ByteArrayOutputStream()
            val buf = ByteArray(16 * 1024)
            while (!inflater.finished() && out.size() < maxOutput) {
                val n = try { inflater.inflate(buf) } catch (e: DataFormatException) { break }
                if (n == 0 && (inflater.needsInput() || inflater.needsDictionary())) break
                out.write(buf, 0, n)
            }
            // Damaged streams still yield their readable prefix.
            if (out.size() > 0) return out.toByteArray()
        } finally {
            inflater.end()
        }
    }
    return null
}
