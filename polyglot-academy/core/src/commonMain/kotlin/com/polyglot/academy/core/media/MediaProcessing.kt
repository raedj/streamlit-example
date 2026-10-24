package com.polyglot.academy.core.media

import com.polyglot.academy.core.mandarin.isHanzi
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.model.MaterialType
import com.polyglot.academy.core.util.TextNormalizer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.yield

data class CaptionCue(val startMillis: Long, val endMillis: Long, val text: String)

/** SubRip (.srt) and WebVTT (.vtt) parser. */
object CaptionParser {
    private val time = Regex("(?:(\\d+):)?(\\d{1,2}):(\\d{2})[.,](\\d{1,3})")

    fun parse(content: String): List<CaptionCue> {
        val lines = content.replace("\r\n", "\n").replace('\r', '\n').split('\n')
        val cues = mutableListOf<CaptionCue>()
        var i = 0
        while (i < lines.size) {
            val line = lines[i]
            if ("-->" in line) {
                val parts = line.split("-->")
                val start = parseTime(parts[0]); val end = parseTime(parts[1].trim().split(' ').first())
                i++
                val text = StringBuilder()
                while (i < lines.size && lines[i].isNotBlank()) {
                    if (text.isNotEmpty()) text.append(' ')
                    text.append(lines[i].replace(Regex("<[^>]+>"), "").trim())
                    i++
                }
                if (start != null && end != null && text.isNotBlank()) cues += CaptionCue(start, end, text.toString())
            } else i++
        }
        return cues
    }

    private fun parseTime(s: String): Long? {
        val m = time.find(s.trim()) ?: return null
        val h = m.groupValues[1].toLongOrNull() ?: 0
        val min = m.groupValues[2].toLong(); val sec = m.groupValues[3].toLong()
        val ms = m.groupValues[4].padEnd(3, '0').toLong()
        return ((h * 60 + min) * 60 + sec) * 1000 + ms
    }
}

object SentenceSplitter {
    private val enders = setOf('.', '!', '?', '。', '！', '？', '؟', '…')

    fun split(text: String): List<String> {
        val out = mutableListOf<String>()
        val sb = StringBuilder()
        for (c in text) {
            sb.append(c)
            if (c in enders || c == '\n') {
                val s = sb.toString().trim(); if (s.isNotEmpty()) out += s
                sb.clear()
            }
        }
        sb.toString().trim().takeIf { it.isNotEmpty() }?.let { out += it }
        return out
    }
}

data class VocabularyCandidate(val term: String, val frequency: Int, val knownId: String?, val exampleSentence: String?)

/**
 * Finds study candidates in extracted text. Latin/Arabic scripts are split on whitespace;
 * Mandarin uses forward maximum matching against a dictionary (curriculum words), because
 * Chinese has no spaces. Common function words are skipped.
 */
class VocabularyExtractor(
    private val language: LanguageCode,
    /** Known dictionary: normalised term -> vocabulary id. */
    private val dictionary: Map<String, String>,
    private val stopWords: Set<String> = StopWords.forLanguage(language),
) {
    private val maxWordLen = dictionary.keys.filter { k -> k.any(::isHanzi) }.maxOfOrNull { it.length } ?: 4

    fun tokens(text: String): List<String> =
        if (language == LanguageCode.MANDARIN) segmentChinese(text)
        else TextNormalizer.basic(text).split(' ').filter { it.length > 1 && it.none(Char::isDigit) }

    fun segmentChinese(text: String): List<String> {
        val out = mutableListOf<String>()
        var i = 0
        while (i < text.length) {
            if (!isHanzi(text[i])) { i++; continue }
            var matched: String? = null
            for (len in minOf(maxWordLen, text.length - i) downTo 1) {
                val cand = text.substring(i, i + len)
                if (cand in dictionary) { matched = cand; break }
            }
            val token = matched ?: text.substring(i, i + 1)
            out += token
            i += token.length
        }
        return out
    }

    fun candidates(text: String, limit: Int = 30): List<VocabularyCandidate> {
        val sentences = SentenceSplitter.split(text)
        val counts = mutableMapOf<String, Int>()
        val example = mutableMapOf<String, String>()
        for (s in sentences) for (t in tokens(s)) {
            if (t in stopWords) continue
            if (language == LanguageCode.MANDARIN && t.length == 1 && t !in dictionary) continue
            counts[t] = (counts[t] ?: 0) + 1
            example.getOrPut(t) { s }
        }
        return counts.entries
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenByDescending { it.key.length }.thenBy { it.key })
            .take(limit)
            .map { VocabularyCandidate(it.key, it.value, dictionary[it.key], example[it.key]) }
    }
}

object StopWords {
    fun forLanguage(code: LanguageCode): Set<String> = when (code) {
        LanguageCode.ENGLISH -> setOf("the", "a", "an", "and", "or", "but", "of", "to", "in", "on", "at", "for", "is", "are", "was", "were", "be", "been",
            "it", "this", "that", "with", "as", "by", "from", "i", "you", "he", "she", "we", "they", "my", "your", "his", "her", "our", "their", "not",
            "do", "does", "did", "have", "has", "had", "will", "would", "can", "could", "so", "if", "there", "what", "which", "who", "me", "him", "them", "its")
        LanguageCode.SPANISH -> setOf("el", "la", "los", "las", "un", "una", "unos", "unas", "y", "o", "de", "del", "a", "al", "en", "por", "para", "con",
            "es", "son", "que", "se", "no", "su", "sus", "lo", "le", "me", "te", "mi", "tu", "yo", "muy", "más", "pero", "como")
        LanguageCode.FRENCH -> setOf("le", "la", "les", "un", "une", "des", "et", "ou", "de", "du", "à", "au", "aux", "en", "dans", "pour", "par", "avec",
            "est", "sont", "que", "qui", "se", "ne", "pas", "son", "sa", "ses", "je", "tu", "il", "elle", "nous", "vous", "ils", "elles", "mais", "ce", "l", "d")
        LanguageCode.ARABIC -> setOf("في", "من", "على", "الى", "إلى", "عن", "مع", "و", "او", "أو", "ثم", "هذا", "هذه", "ذلك", "التي", "الذي", "هو", "هي", "ان", "أن", "لا", "ما", "كان")
        LanguageCode.MANDARIN -> setOf("的", "了", "是", "在", "和", "也", "就", "都", "吗", "呢", "吧", "啊", "我", "你", "他", "她", "们", "这", "那", "有", "不")
    }
}

data class ImportLimits(val maxBytes: Long, val supportedMimePrefixes: List<String>)

object ImportPolicy {
    val limits: Map<MaterialType, ImportLimits> = mapOf(
        MaterialType.TEXT to ImportLimits(5L * 1024 * 1024, listOf("text/plain", "text/markdown")),
        MaterialType.CAPTIONS to ImportLimits(5L * 1024 * 1024, listOf("application/x-subrip", "text/vtt", "text/srt", "application/octet-stream")),
        MaterialType.PDF to ImportLimits(50L * 1024 * 1024, listOf("application/pdf")),
        MaterialType.IMAGE to ImportLimits(20L * 1024 * 1024, listOf("image/")),
        MaterialType.AUDIO to ImportLimits(200L * 1024 * 1024, listOf("audio/")),
        MaterialType.VIDEO to ImportLimits(1024L * 1024 * 1024, listOf("video/")),
        MaterialType.DOCUMENT to ImportLimits(20L * 1024 * 1024, listOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document")),
    )

    fun typeFor(mimeType: String, fileName: String): MaterialType? {
        val name = fileName.lowercase()
        return when {
            name.endsWith(".srt") || name.endsWith(".vtt") -> MaterialType.CAPTIONS
            mimeType.startsWith("text/") || name.endsWith(".txt") || name.endsWith(".md") -> MaterialType.TEXT
            mimeType == "application/pdf" || name.endsWith(".pdf") -> MaterialType.PDF
            mimeType.startsWith("image/") -> MaterialType.IMAGE
            mimeType.startsWith("audio/") -> MaterialType.AUDIO
            mimeType.startsWith("video/") -> MaterialType.VIDEO
            name.endsWith(".docx") -> MaterialType.DOCUMENT
            else -> null
        }
    }

    sealed interface Validation {
        data object Ok : Validation
        data class TooLarge(val maxBytes: Long) : Validation
        data object UnsupportedType : Validation
        data object Empty : Validation
    }

    fun validate(type: MaterialType?, sizeBytes: Long): Validation = when {
        type == null -> Validation.UnsupportedType
        sizeBytes <= 0 -> Validation.Empty
        sizeBytes > (limits[type]?.maxBytes ?: 0) -> Validation.TooLarge(limits[type]?.maxBytes ?: 0)
        else -> Validation.Ok
    }
}

/** What extraction is implemented for each type in this build (honest capability report). */
enum class ExtractionSupport { SUPPORTED, PLATFORM_DEPENDENT, NOT_IMPLEMENTED }

data class ProcessingProgress(val processedChunks: Int, val totalChunks: Int, val partialText: String?, val done: Boolean) {
    val fraction: Float get() = if (totalChunks == 0) 1f else processedChunks.toFloat() / totalChunks
}

/** Processes text in bounded chunks so large inputs never need to be held twice and can be cancelled. */
object ChunkedTextProcessor {
    const val CHUNK_CHARS = 4_000

    fun chunks(text: String, size: Int = CHUNK_CHARS): List<String> {
        if (text.length <= size) return listOf(text)
        val out = mutableListOf<String>()
        var start = 0
        while (start < text.length) {
            var end = minOf(text.length, start + size)
            if (end < text.length) {
                val brk = text.lastIndexOfAny(charArrayOf('\n', '.', '。', '!', '?', ' '), end)
                if (brk > start + size / 2) end = brk + 1
            }
            out += text.substring(start, end)
            start = end
        }
        return out
    }

    /** Cleans each chunk (collapse whitespace, drop control chars) and reports progress. Cancellable between chunks. */
    fun process(chunks: List<String>): Flow<ProcessingProgress> = flow {
        val sb = StringBuilder()
        chunks.forEachIndexed { i, c ->
            yield()
            sb.append(c.filter { it == '\n' || !it.isISOControl() }.replace(Regex("[ \\t]+"), " "))
            emit(ProcessingProgress(i + 1, chunks.size, null, done = false))
        }
        emit(ProcessingProgress(chunks.size, chunks.size, sb.toString().trim(), done = true))
    }
}
