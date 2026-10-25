package com.polyglot.academy.core.media

/** zlib (FlateDecode) decompression. Returns the readable output, or null if nothing decodes. */
internal expect fun inflateZlib(data: ByteArray, maxOutput: Int): ByteArray?

sealed interface PdfTextResult {
    data class Text(val text: String, val pages: Int) : PdfTextResult
    /** Password-protected or otherwise encrypted; not decrypted. */
    data object Encrypted : PdfTextResult
    /** Parsed, but no extractable text (typically a scanned document that needs OCR). */
    data object NoText : PdfTextResult
    data object NotPdf : PdfTextResult
}

/**
 * On-device text extraction for PDFs that contain real text. Supports uncompressed, Flate,
 * ASCIIHex and ASCII85 streams, object streams, page trees with inherited resources, form
 * XObjects, ToUnicode CMaps (including two-byte CID fonts) and the standard simple-font
 * encodings. Layout is approximated from text operators, not glyph geometry. Encrypted files and
 * scanned images are reported, not guessed.
 */
object PdfTextExtractor {
    private const val MAX_STREAM = 64 * 1024 * 1024
    private const val MAX_FORM_DEPTH = 6

    fun extract(bytes: ByteArray, maxChars: Int = 2_000_000): PdfTextResult {
        if (!startsWithPdf(bytes)) return PdfTextResult.NotPdf
        val doc = PdfDocument.parse(bytes)
        if (doc.encrypted) return PdfTextResult.Encrypted
        val pages = doc.pages()
        val out = StringBuilder()
        for (page in pages) {
            if (out.length >= maxChars) break
            val sb = StringBuilder()
            val resources = page.resources
            val contents = when (val c = doc.resolve(page.dict["Contents"])) {
                is PStream -> listOf(c)
                is PArr -> c.items.mapNotNull { doc.resolve(it) as? PStream }
                else -> emptyList()
            }
            // Content arrays may split one stream anywhere, so they are interpreted as one.
            val data = contents.mapNotNull { doc.decode(it) }.fold(ByteArray(0)) { acc, b -> acc + b + byteArrayOf(10) }
            TextInterpreter(doc, sb).run(data, resources, 0)
            val pageText = tidy(sb.toString())
            if (pageText.isNotBlank()) {
                if (out.isNotEmpty()) out.append("\n\n")
                out.append(pageText)
            }
        }
        val text = out.toString().take(maxChars).trim()
        return if (text.isEmpty()) PdfTextResult.NoText else PdfTextResult.Text(text, pages.size)
    }

    private fun startsWithPdf(b: ByteArray): Boolean {
        val head = latin1(b, 0, minOf(b.size, 1024))
        return "%PDF-" in head
    }

    private fun tidy(s: String): String {
        val lines = s.split('\n').map { line -> ArabicForms.normalize(line).replace(Regex("[ \\t\\u00A0]+"), " ").trim() }
        val fixed = if (ArabicForms.looksVisualOrder(lines)) lines.map(ArabicForms::visualToLogical) else lines
        return fixed.joinToString("\n")
            .replace(Regex("\n{3,}"), "\n\n")
            .trim()
    }

    // ---------------------------------------------------------------- object model

    internal sealed interface PObj
    internal data class PNum(val v: Double) : PObj
    internal class PStr(val bytes: ByteArray) : PObj
    internal data class PName(val v: String) : PObj
    internal data class PArr(val items: List<PObj>) : PObj
    internal data class PDict(val map: Map<String, PObj>) : PObj { operator fun get(k: String) = map[k] }
    internal data class PRef(val num: Int, val gen: Int) : PObj
    internal data class PBool(val v: Boolean) : PObj
    internal data object PNull : PObj
    internal data class POp(val v: String) : PObj
    internal class PStream(val dict: PDict, val raw: ByteArray) : PObj

    internal class Page(val dict: PDict, val resources: PDict?)

    internal class PdfDocument(private val objects: Map<Int, PObj>, val encrypted: Boolean) {
        private val decoded = HashMap<PStream, ByteArray?>()

        fun resolve(o: PObj?, depth: Int = 0): PObj? = if (o is PRef && depth < 32) resolve(objects[o.num], depth + 1) else o
        fun dict(o: PObj?): PDict? = when (val r = resolve(o)) { is PDict -> r; is PStream -> r.dict; else -> null }

        fun decode(s: PStream): ByteArray? = decoded.getOrPut(s) { decodeStream(s) }

        private fun decodeStream(s: PStream): ByteArray? {
            val filters = when (val f = resolve(s.dict["Filter"])) {
                is PName -> listOf(f.v)
                is PArr -> f.items.mapNotNull { (resolve(it) as? PName)?.v }
                else -> emptyList()
            }
            var data = s.raw
            for (f in filters) {
                data = when (f) {
                    "FlateDecode", "Fl" -> inflateZlib(data, MAX_STREAM) ?: return null
                    "ASCIIHexDecode", "AHx" -> asciiHex(data)
                    "ASCII85Decode", "A85" -> ascii85(data) ?: return null
                    else -> return null // image codecs, LZW, crypt: not text content
                }
            }
            val parms = dict(s.dict["DecodeParms"]) ?: (resolve(s.dict["DecodeParms"]) as? PArr)?.items?.lastOrNull()?.let { dict(it) }
            val predictor = (resolve(parms?.get("Predictor")) as? PNum)?.v?.toInt() ?: 1
            if (predictor >= 10) {
                val columns = (resolve(parms?.get("Columns")) as? PNum)?.v?.toInt() ?: 1
                data = pngUnpredict(data, columns)
            }
            return data
        }

        fun pages(): List<Page> {
            val root = objects.values.firstNotNullOfOrNull { o -> (o as? PDict)?.takeIf { (it["Type"] as? PName)?.v == "Catalog" } }
            val result = mutableListOf<Page>()
            val seen = HashSet<PDict>()
            fun walk(node: PDict, inherited: PDict?, depth: Int) {
                if (depth > 64 || !seen.add(node)) return
                val res = dict(node["Resources"]) ?: inherited
                when ((resolve(node["Type"]) as? PName)?.v) {
                    "Pages" -> (resolve(node["Kids"]) as? PArr)?.items?.forEach { k -> dict(k)?.let { walk(it, res, depth + 1) } }
                    else -> if (node["Contents"] != null || node["Type"] == PName("Page")) result += Page(node, res)
                }
            }
            dict(root?.get("Pages"))?.let { walk(it, null, 0) }
            if (result.isEmpty()) {
                objects.toSortedMap().values.forEach { o ->
                    val d = o as? PDict ?: return@forEach
                    if ((d["Type"] as? PName)?.v == "Page") result += Page(d, dict(d["Resources"]))
                }
            }
            return result
        }

        companion object {
            fun parse(b: ByteArray): PdfDocument {
                val text = latin1(b, 0, b.size)
                val objects = HashMap<Int, PObj>()
                val header = Regex("(\\d+)\\s+(\\d+)\\s+obj\\b")
                var skipUntil = 0
                for (m in header.findAll(text)) {
                    if (m.range.first < skipUntil) continue
                    val lx = Lexer(b, m.range.last + 1)
                    val obj = runCatching { lx.parseObject() }.getOrNull() ?: continue
                    lx.skipWs()
                    if (obj is PDict && lx.peekKeyword("stream")) {
                        lx.pos += 6
                        if (lx.pos < b.size && b[lx.pos] == '\r'.code.toByte()) lx.pos++
                        if (lx.pos < b.size && b[lx.pos] == '\n'.code.toByte()) lx.pos++
                        val start = lx.pos
                        val declared = (obj["Length"] as? PNum)?.v?.toInt()
                        val end = if (declared != null && declared >= 0 && start + declared <= b.size &&
                            text.indexOf("endstream", start + declared).let { it >= 0 && it - (start + declared) <= 4 }
                        ) start + declared else text.indexOf("endstream", start).let { if (it < 0) b.size else trimEol(b, start, it) }
                        objects[m.groupValues[1].toInt()] = PStream(obj, b.copyOfRange(start, end))
                        skipUntil = end
                    } else {
                        objects[m.groupValues[1].toInt()] = obj
                        skipUntil = lx.pos
                    }
                }
                val encrypted = Regex("/Encrypt\\s*(\\d+\\s+\\d+\\s+R|<<)").containsMatchIn(text)
                val doc = PdfDocument(objects, encrypted)
                // Objects packed in object streams (PDF 1.5+). Direct objects take precedence.
                objects.values.filterIsInstance<PStream>().filter { (it.dict["Type"] as? PName)?.v == "ObjStm" }.forEach { os ->
                    val data = doc.decode(os) ?: return@forEach
                    val n = (doc.resolve(os.dict["N"]) as? PNum)?.v?.toInt() ?: return@forEach
                    val first = (doc.resolve(os.dict["First"]) as? PNum)?.v?.toInt() ?: return@forEach
                    val lx = Lexer(data, 0)
                    val entries = (0 until n).mapNotNull {
                        val num = lx.parseObject() as? PNum ?: return@mapNotNull null
                        val off = lx.parseObject() as? PNum ?: return@mapNotNull null
                        num.v.toInt() to off.v.toInt()
                    }
                    entries.forEach { (num, off) ->
                        if (num !in objects && first + off < data.size) {
                            runCatching { Lexer(data, first + off).parseObject() }.getOrNull()?.let { objects[num] = it }
                        }
                    }
                }
                return doc
            }

            private fun trimEol(b: ByteArray, start: Int, end: Int): Int {
                var e = end
                if (e > start && b[e - 1] == '\n'.code.toByte()) e--
                if (e > start && b[e - 1] == '\r'.code.toByte()) e--
                return e
            }
        }
    }

    // ---------------------------------------------------------------- lexer

    internal class Lexer(private val b: ByteArray, var pos: Int) {
        private fun at(i: Int) = b[i].toInt() and 0xFF
        val atEnd get() = pos >= b.size

        fun skipWs() {
            while (pos < b.size) {
                val c = at(pos)
                if (c == '%'.code) { while (pos < b.size && at(pos) != 10 && at(pos) != 13) pos++ }
                else if (isWs(c)) pos++ else break
            }
        }

        fun peekKeyword(k: String): Boolean {
            if (pos + k.length > b.size) return false
            for (i in k.indices) if (at(pos + i) != k[i].code) return false
            return pos + k.length == b.size || isDelimOrWs(at(pos + k.length))
        }

        fun parseObject(): PObj? {
            skipWs()
            if (atEnd) return null
            val c = at(pos)
            return when {
                c == '/'.code -> PName(readName())
                c == '('.code -> PStr(readLiteral())
                c == '<'.code && pos + 1 < b.size && at(pos + 1) == '<'.code -> readDict()
                c == '<'.code -> PStr(readHex())
                c == '['.code -> { pos++; val items = mutableListOf<PObj>(); while (true) { skipWs(); if (atEnd) break; if (at(pos) == ']'.code) { pos++; break }; items += parseObject() ?: break }; PArr(items) }
                c == '+'.code || c == '-'.code || c == '.'.code || c in '0'.code..'9'.code -> readNumberOrRef()
                c == ']'.code || c == '>'.code || c == ')'.code || c == '}'.code || c == '{'.code -> { pos++; POp(c.toChar().toString()) }
                else -> {
                    val w = readWord()
                    when (w) { "true" -> PBool(true); "false" -> PBool(false); "null" -> PNull; else -> POp(w) }
                }
            }
        }

        private fun readDict(): PDict {
            pos += 2
            val map = LinkedHashMap<String, PObj>()
            while (true) {
                skipWs()
                if (atEnd) break
                if (at(pos) == '>'.code && pos + 1 < b.size && at(pos + 1) == '>'.code) { pos += 2; break }
                val k = parseObject()
                if (k !is PName) { if (k == null) break else continue }
                val v = parseObject() ?: break
                map[k.v] = v
            }
            return PDict(map)
        }

        private fun readNumberOrRef(): PObj {
            val n = readNumber()
            val save = pos
            if (n.v >= 0 && n.v == kotlin.math.floor(n.v)) {
                skipWs()
                if (!atEnd && at(pos) in '0'.code..'9'.code) {
                    val g = readNumber()
                    skipWs()
                    if (!atEnd && at(pos) == 'R'.code && (pos + 1 == b.size || isDelimOrWs(at(pos + 1)))) { pos++; return PRef(n.v.toInt(), g.v.toInt()) }
                }
            }
            pos = save
            return n
        }

        private fun readNumber(): PNum {
            val start = pos
            pos++
            while (pos < b.size && (at(pos) in '0'.code..'9'.code || at(pos) == '.'.code)) pos++
            return PNum(latin1(b, start, pos).toDoubleOrNull() ?: 0.0)
        }

        private fun readWord(): String {
            val start = pos
            while (pos < b.size && !isDelimOrWs(at(pos))) pos++
            if (pos == start) pos++
            return latin1(b, start, pos)
        }

        private fun readName(): String {
            pos++
            val sb = StringBuilder()
            while (pos < b.size && !isDelimOrWs(at(pos))) {
                val c = at(pos)
                if (c == '#'.code && pos + 2 < b.size) {
                    val h = latin1(b, pos + 1, pos + 3).toIntOrNull(16)
                    if (h != null) { sb.append(h.toChar()); pos += 3; continue }
                }
                sb.append(c.toChar()); pos++
            }
            return sb.toString()
        }

        private fun readLiteral(): ByteArray {
            pos++
            val out = ByteBuf()
            var depth = 1
            while (pos < b.size) {
                val c = at(pos++)
                when (c) {
                    '('.code -> { depth++; out.add(c) }
                    ')'.code -> { depth--; if (depth == 0) return out.toArray(); out.add(c) }
                    '\\'.code -> {
                        if (pos >= b.size) break
                        when (val e = at(pos++)) {
                            'n'.code -> out.add(10); 'r'.code -> out.add(13); 't'.code -> out.add(9)
                            'b'.code -> out.add(8); 'f'.code -> out.add(12)
                            13 -> { if (pos < b.size && at(pos) == 10) pos++ }
                            10 -> {}
                            in '0'.code..'7'.code -> {
                                var v = e - '0'.code
                                repeat(2) { if (pos < b.size && at(pos) in '0'.code..'7'.code) v = v * 8 + (at(pos++) - '0'.code) }
                                out.add(v and 0xFF)
                            }
                            else -> out.add(e)
                        }
                    }
                    else -> out.add(c)
                }
            }
            return out.toArray()
        }

        private fun readHex(): ByteArray {
            pos++
            val sb = StringBuilder()
            while (pos < b.size && at(pos) != '>'.code) { val c = at(pos++); if (!isWs(c)) sb.append(c.toChar()) }
            pos++
            if (sb.length % 2 == 1) sb.append('0')
            return ByteArray(sb.length / 2) { i -> (sb.substring(i * 2, i * 2 + 2).toIntOrNull(16) ?: 0).toByte() }
        }

        /** Skips inline image data after the ID operator up to EI. */
        fun skipInlineImage() {
            pos++
            while (pos + 2 < b.size) {
                if (at(pos) == 'E'.code && at(pos + 1) == 'I'.code && isWs(at(pos - 1)) && (pos + 2 == b.size || isDelimOrWs(at(pos + 2)))) { pos += 2; return }
                pos++
            }
            pos = b.size
        }
    }

    // ---------------------------------------------------------------- content interpretation

    private class TextInterpreter(private val doc: PdfDocument, private val out: StringBuilder) {
        private var font: FontDecoder? = null
        private val fontCache = HashMap<PDict, FontDecoder>()
        private var lastY: Double? = null

        fun run(data: ByteArray, resources: PDict?, depth: Int) {
            val lx = Lexer(data, 0)
            val stack = ArrayList<PObj>()
            val fonts = doc.dict(resources?.get("Font"))
            val xobjects = doc.dict(resources?.get("XObject"))
            while (true) {
                val o = lx.parseObject() ?: break
                if (o !is POp) { stack += o; if (stack.size > 4096) stack.clear(); continue }
                when (o.v) {
                    "BT" -> lastY = null
                    "ET" -> newline()
                    "Tf" -> (stack.getOrNull(stack.size - 2) as? PName)?.let { name ->
                        font = doc.dict(fonts?.get(name.v))?.let { d -> fontCache.getOrPut(d) { FontDecoder.of(doc, d) } }
                    }
                    "Td", "TD" -> { val ty = num(stack, 0); if (kotlin.math.abs(ty) > 0.01) newline() }
                    "Tm" -> { val y = num(stack, 5); lastY?.let { if (kotlin.math.abs(it - y) > 0.01) newline() }; lastY = y }
                    "T*" -> newline()
                    "Tj" -> show(stack.lastOrNull())
                    "'" -> { newline(); show(stack.lastOrNull()) }
                    "\"" -> { newline(); show(stack.lastOrNull()) }
                    "TJ" -> (stack.lastOrNull() as? PArr)?.items?.forEach { item ->
                        when (item) {
                            is PStr -> show(item)
                            is PNum -> if (item.v < -180 && out.isNotEmpty() && !out.last().isWhitespace()) out.append(' ')
                            else -> {}
                        }
                    }
                    "Do" -> if (depth < MAX_FORM_DEPTH) (stack.lastOrNull() as? PName)?.let { name ->
                        val x = doc.resolve(xobjects?.get(name.v)) as? PStream
                        if (x != null && (x.dict["Subtype"] as? PName)?.v == "Form") {
                            val saved = font
                            doc.decode(x)?.let { run(it, doc.dict(x.dict["Resources"]) ?: resources, depth + 1) }
                            font = saved
                        }
                    }
                    "ID" -> lx.skipInlineImage()
                }
                stack.clear()
            }
        }

        private fun num(stack: List<PObj>, fromEnd: Int) = (stack.getOrNull(stack.size - 1 - fromEnd) as? PNum)?.v ?: 0.0

        private fun newline() { if (out.isNotEmpty() && out.last() != '\n') out.append('\n') }

        private fun show(o: PObj?) {
            val s = o as? PStr ?: return
            out.append((font ?: FontDecoder.fallback).decode(s.bytes))
        }
    }

    // ---------------------------------------------------------------- fonts

    private class FontDecoder(private val cmap: CMap?, private val simple: CharArray?) {
        fun decode(bytes: ByteArray): String = when {
            cmap != null -> cmap.decode(bytes, simple)
            simple != null -> buildString { bytes.forEach { b -> simple[b.toInt() and 0xFF].let { if (it != '\u0000') append(it) } } }
            else -> "" // composite font without ToUnicode: codes cannot be mapped to characters
        }

        companion object {
            val fallback = FontDecoder(null, Encodings.winAnsi)

            fun of(doc: PdfDocument, font: PDict): FontDecoder {
                val cmap = (doc.resolve(font["ToUnicode"]) as? PStream)?.let { doc.decode(it) }?.let { CMap.parse(it) }
                val subtype = (doc.resolve(font["Subtype"]) as? PName)?.v
                if (subtype == "Type0") return FontDecoder(cmap, null)
                return FontDecoder(cmap, simpleEncoding(doc, font))
            }

            private fun simpleEncoding(doc: PdfDocument, font: PDict): CharArray {
                val enc = doc.resolve(font["Encoding"])
                val baseName = when (enc) { is PName -> enc.v; is PDict -> (doc.resolve(enc["BaseEncoding"]) as? PName)?.v; else -> null }
                val table = when (baseName) { "MacRomanEncoding" -> Encodings.macRoman; else -> Encodings.winAnsi }.copyOf()
                val diffs = (enc as? PDict)?.let { doc.resolve(it["Differences"]) as? PArr }
                var code = 0
                diffs?.items?.forEach { item ->
                    when (val r = doc.resolve(item)) {
                        is PNum -> code = r.v.toInt()
                        is PName -> { if (code in 0..255) Encodings.glyph(r.v)?.let { table[code] = it }; code++ }
                        else -> {}
                    }
                }
                return table
            }
        }
    }

    private class CMap(private val ranges: List<Pair<ByteArray, ByteArray>>, private val map: Map<Long, String>) {
        fun decode(bytes: ByteArray, simple: CharArray?): String = buildString {
            var i = 0
            while (i < bytes.size) {
                val len = codeLength(bytes, i)
                var code = 0L
                for (k in 0 until len) code = (code shl 8) or (bytes[i + k].toLong() and 0xFF)
                val mapped = map[code]
                when {
                    mapped != null -> append(mapped)
                    len == 1 && simple != null -> simple[code.toInt()].let { if (it != '\u0000') append(it) }
                }
                i += len
            }
        }

        private fun codeLength(b: ByteArray, i: Int): Int {
            for ((lo, hi) in ranges) {
                if (i + lo.size > b.size) continue
                var ok = true
                for (k in lo.indices) {
                    val v = b[i + k].toInt() and 0xFF
                    if (v < (lo[k].toInt() and 0xFF) || v > (hi[k].toInt() and 0xFF)) { ok = false; break }
                }
                if (ok) return lo.size
            }
            val fallbackLen = ranges.firstOrNull()?.first?.size ?: if (map.keys.any { it > 0xFF }) 2 else 1
            return minOf(fallbackLen, b.size - i).coerceAtLeast(1)
        }

        companion object {
            fun parse(data: ByteArray): CMap {
                val lx = Lexer(data, 0)
                val ranges = mutableListOf<Pair<ByteArray, ByteArray>>()
                val map = HashMap<Long, String>()
                val operands = ArrayList<PObj>()
                var mode = ""
                while (true) {
                    val o = lx.parseObject() ?: break
                    if (o is POp) {
                        when (o.v) {
                            "begincodespacerange", "beginbfchar", "beginbfrange" -> { mode = o.v; operands.clear() }
                            "endcodespacerange" -> { operands.filterIsInstance<PStr>().chunked(2).forEach { if (it.size == 2) ranges += it[0].bytes to it[1].bytes }; mode = "" }
                            "endbfchar" -> {
                                operands.chunked(2).forEach { p -> val src = p.getOrNull(0) as? PStr; val dst = p.getOrNull(1); if (src != null && dst != null) dest(dst)?.let { map[code(src.bytes)] = it } }
                                mode = ""
                            }
                            "endbfrange" -> {
                                operands.chunked(3).forEach { t ->
                                    val lo = (t.getOrNull(0) as? PStr)?.let { code(it.bytes) } ?: return@forEach
                                    val hi = (t.getOrNull(1) as? PStr)?.let { code(it.bytes) } ?: return@forEach
                                    if (hi < lo || hi - lo > 65_535) return@forEach
                                    when (val d = t.getOrNull(2)) {
                                        is PStr -> {
                                            val base = d.bytes
                                            for (c in lo..hi) {
                                                val bytes = base.copyOf()
                                                // Increment the last byte of the destination (the PDF rule).
                                                val last = (bytes.last().toInt() and 0xFF) + (c - lo).toInt()
                                                bytes[bytes.size - 1] = (last and 0xFF).toByte()
                                                if (bytes.size >= 2 && last > 0xFF) bytes[bytes.size - 2] = (bytes[bytes.size - 2] + (last shr 8)).toByte()
                                                map[c] = utf16(bytes)
                                            }
                                        }
                                        is PArr -> d.items.forEachIndexed { k, item -> if (lo + k <= hi) dest(item)?.let { map[lo + k] = it } }
                                        else -> {}
                                    }
                                }
                                mode = ""
                            }
                        }
                    } else if (mode.isNotEmpty()) operands += o
                }
                ranges.sortByDescending { it.first.size }
                return CMap(ranges, map)
            }

            private fun dest(o: PObj): String? = when (o) {
                is PStr -> utf16(o.bytes)
                is PName -> Encodings.glyph(o.v)?.toString()
                else -> null
            }

            private fun code(b: ByteArray): Long = b.fold(0L) { acc, x -> (acc shl 8) or (x.toLong() and 0xFF) }

            private fun utf16(b: ByteArray): String {
                val sb = StringBuilder()
                var i = 0
                while (i + 1 < b.size) { sb.append((((b[i].toInt() and 0xFF) shl 8) or (b[i + 1].toInt() and 0xFF)).toChar()); i += 2 }
                if (b.size == 1) sb.append((b[0].toInt() and 0xFF).toChar())
                return sb.toString()
            }
        }
    }

    // ---------------------------------------------------------------- helpers

    private fun isWs(c: Int) = c == 0 || c == 9 || c == 10 || c == 12 || c == 13 || c == 32
    private fun isDelimOrWs(c: Int) = isWs(c) || c == '('.code || c == ')'.code || c == '<'.code || c == '>'.code ||
        c == '['.code || c == ']'.code || c == '{'.code || c == '}'.code || c == '/'.code || c == '%'.code

    private fun latin1(b: ByteArray, from: Int, to: Int): String {
        val chars = CharArray(to - from) { (b[from + it].toInt() and 0xFF).toChar() }
        return chars.concatToString()
    }

    private class ByteBuf {
        private var a = ByteArray(64)
        private var n = 0
        fun add(v: Int) { if (n == a.size) a = a.copyOf(a.size * 2); a[n++] = v.toByte() }
        fun toArray() = a.copyOf(n)
    }

    private fun asciiHex(d: ByteArray): ByteArray {
        val digits = StringBuilder()
        for (x in d) { val c = (x.toInt() and 0xFF).toChar(); if (c == '>') break; if (c.isLetterOrDigit()) digits.append(c) }
        if (digits.length % 2 == 1) digits.append('0')
        return ByteArray(digits.length / 2) { (digits.substring(it * 2, it * 2 + 2).toIntOrNull(16) ?: 0).toByte() }
    }

    private fun ascii85(d: ByteArray): ByteArray? {
        val out = ByteBuf()
        val group = IntArray(5)
        var n = 0
        var i = 0
        val s = latin1(d, 0, d.size).removePrefix("<~")
        while (i < s.length) {
            val c = s[i++]
            when {
                c == '~' -> break
                c.isWhitespace() -> {}
                c == 'z' && n == 0 -> repeat(4) { out.add(0) }
                c in '!'..'u' -> {
                    group[n++] = c - '!'
                    if (n == 5) { var v = 0L; for (k in 0 until 5) v = v * 85 + group[k]; for (k in 3 downTo 0) out.add(((v shr (8 * k)) and 0xFF).toInt()); n = 0 }
                }
                else -> return null
            }
        }
        if (n > 0) {
            for (k in n until 5) group[k] = 84
            var v = 0L; for (k in 0 until 5) v = v * 85 + group[k]
            for (k in 0 until n - 1) out.add(((v shr (8 * (3 - k))) and 0xFF).toInt())
        }
        return out.toArray()
    }

    private fun pngUnpredict(d: ByteArray, columns: Int): ByteArray {
        val row = columns + 1
        if (columns <= 0 || d.size % row != 0) return d
        val out = ByteArray(d.size / row * columns)
        val prev = IntArray(columns)
        for (r in 0 until d.size / row) {
            val type = d[r * row].toInt() and 0xFF
            for (c in 0 until columns) {
                val raw = d[r * row + 1 + c].toInt() and 0xFF
                val left = if (c > 0) out[r * columns + c - 1].toInt() and 0xFF else 0
                val up = prev[c]
                val upLeft = if (c > 0 && r > 0) out[(r - 1) * columns + c - 1].toInt() and 0xFF else 0
                val v = when (type) {
                    1 -> raw + left
                    2 -> raw + up
                    3 -> raw + (left + up) / 2
                    4 -> { val p = left + up - upLeft; val pa = kotlin.math.abs(p - left); val pb = kotlin.math.abs(p - up); val pc = kotlin.math.abs(p - upLeft)
                        raw + if (pa <= pb && pa <= pc) left else if (pb <= pc) up else upLeft }
                    else -> raw
                } and 0xFF
                out[r * columns + c] = v.toByte()
            }
            for (c in 0 until columns) prev[c] = out[r * columns + c].toInt() and 0xFF
        }
        return out
    }
}

/** Single-byte encodings for simple fonts and a small glyph-name table for /Differences. */
internal object Encodings {
    val winAnsi: CharArray = CharArray(256) { i -> if (i in 0x20..0x7E || i >= 0xA0) i.toChar() else '\u0000' }.also { t ->
        val hi = "€\u0000‚ƒ„…†‡ˆ‰Š‹Œ\u0000Ž\u0000\u0000‘’“”•–—˜™š›œ\u0000žŸ"
        hi.forEachIndexed { k, c -> t[0x80 + k] = c }
        t[9] = ' '; t[10] = '\n'; t[13] = '\n'
    }

    val macRoman: CharArray = CharArray(256) { i -> if (i in 0x20..0x7E) i.toChar() else '\u0000' }.also { t ->
        val hi = "ÄÅÇÉÑÖÜáàâäãåçéèêëíìîïñóòôöõúùûü†°¢£§•¶ß®©™´¨≠ÆØ∞±≤≥¥µ∂∑∏π∫ªºΩæø¿¡¬√ƒ≈∆«»…\u00A0ÀÃÕŒœ–—“”‘’÷◊ÿŸ⁄€‹›ﬁﬂ‡·‚„‰ÂÊÁËÈÍÎÏÌÓÔ\uF8FFÒÚÛÙıˆ˜¯˘˙˚¸˝˛ˇ"
        hi.forEachIndexed { k, c -> t[0x80 + k] = c }
    }

    private val names = mapOf(
        "space" to ' ', "exclam" to '!', "quotedbl" to '"', "numbersign" to '#', "dollar" to '$', "percent" to '%', "ampersand" to '&',
        "quotesingle" to '\'', "quoteright" to '’', "quoteleft" to '‘', "parenleft" to '(', "parenright" to ')', "asterisk" to '*', "plus" to '+',
        "comma" to ',', "hyphen" to '-', "minus" to '−', "period" to '.', "slash" to '/', "colon" to ':', "semicolon" to ';', "less" to '<',
        "equal" to '=', "greater" to '>', "question" to '?', "at" to '@', "bracketleft" to '[', "backslash" to '\\', "bracketright" to ']',
        "underscore" to '_', "braceleft" to '{', "bar" to '|', "braceright" to '}', "asciitilde" to '~', "endash" to '–', "emdash" to '—',
        "quotedblleft" to '“', "quotedblright" to '”', "bullet" to '•', "ellipsis" to '…', "Euro" to '€', "degree" to '°', "fi" to 'ﬁ', "fl" to 'ﬂ',
        "zero" to '0', "one" to '1', "two" to '2', "three" to '3', "four" to '4', "five" to '5', "six" to '6', "seven" to '7', "eight" to '8', "nine" to '9',
        "germandbls" to 'ß', "ae" to 'æ', "AE" to 'Æ', "oe" to 'œ', "OE" to 'Œ', "oslash" to 'ø', "Oslash" to 'Ø', "exclamdown" to '¡', "questiondown" to '¿',
        "guillemotleft" to '«', "guillemotright" to '»', "ccedilla" to 'ç', "Ccedilla" to 'Ç', "ntilde" to 'ñ', "Ntilde" to 'Ñ',
    )
    private val accents = mapOf("acute" to '́', "grave" to '̀', "circumflex" to '̂', "dieresis" to '̈', "tilde" to '̃')
    private val precomposed = "áàâäãéèêëíìîïóòôöõúùûüÁÀÂÄÃÉÈÊËÍÌÎÏÓÒÔÖÕÚÙÛÜýÿÝ"
    private val decomposed = "aaaaaeeeeiiiiooooouuuuAAAAAEEEEIIIIOOOOOUUUUyyY"
    private val marks = "́̀̂̈̃́̀̂̈́̀̂̈́̀̂̈̃́̀̂̈" +
        "́̀̂̈̃́̀̂̈́̀̂̈́̀̂̈̃́̀̂̈́̈́"

    fun glyph(name: String): Char? {
        names[name]?.let { return it }
        if (name.length == 1) return name[0]
        if (name.startsWith("uni") && name.length == 7) name.substring(3).toIntOrNull(16)?.let { return it.toChar() }
        if (name.startsWith("u") && name.length == 5) name.substring(1).toIntOrNull(16)?.let { return it.toChar() }
        // Latin letters with accents, e.g. "eacute", "Udieresis".
        for ((suffix, mark) in accents) if (name.length == suffix.length + 1 && name.endsWith(suffix)) {
            val base = name[0]
            for (k in precomposed.indices) if (decomposed[k] == base && marks[k] == mark) return precomposed[k]
        }
        return null
    }
}

/** Maps Arabic presentation forms (common in PDF ToUnicode tables) back to base letters. */
internal object ArabicForms {
    private val table: Map<Char, String> = buildMap {
        val letters = listOf('ء' to 1, 'آ' to 2, 'أ' to 2, 'ؤ' to 2, 'إ' to 2, 'ئ' to 4, 'ا' to 2, 'ب' to 4, 'ة' to 2, 'ت' to 4, 'ث' to 4,
            'ج' to 4, 'ح' to 4, 'خ' to 4, 'د' to 2, 'ذ' to 2, 'ر' to 2, 'ز' to 2, 'س' to 4, 'ش' to 4, 'ص' to 4, 'ض' to 4, 'ط' to 4, 'ظ' to 4,
            'ع' to 4, 'غ' to 4, 'ف' to 4, 'ق' to 4, 'ك' to 4, 'ل' to 4, 'م' to 4, 'ن' to 4, 'ه' to 4, 'و' to 2, 'ى' to 2, 'ي' to 4)
        var cp = 0xFE80
        for ((ch, forms) in letters) repeat(forms) { put((cp++).toChar(), ch.toString()) }
        listOf("لآ", "لأ", "لإ", "لا").forEach { lig -> repeat(2) { put((cp++).toChar(), lig) } }
        check(cp == 0xFEFD)
    }

    private fun isArabic(c: Char) = c in '\u0600'..'\u06FF'
    private fun isMark(c: Char) = c in '\u064B'..'\u065F' || c == '\u0670'
    private val ltrRun = Regex("[A-Za-z0-9\\u00C0-\\u024F](?:[A-Za-z0-9\\u00C0-\\u024F .,:/%-]*[A-Za-z0-9\\u00C0-\\u024F%])?")

    /**
     * Some writers store Arabic glyphs in visual (left-to-right) order. Letters that can only
     * begin (ال) or end (ة) a word reveal the direction: in reversed text they appear at the other end.
     */
    fun looksVisualOrder(lines: List<String>): Boolean {
        var logical = 0
        var visual = 0
        lines.forEach { line ->
            line.split(' ').map { w -> w.filter { isArabic(it) && !isMark(it) } }.filter { it.length >= 3 }.forEach { w ->
                if (w.startsWith("ال")) logical++
                if (w.endsWith("ة")) logical++
                if (w.endsWith("لا")) visual++
                if (w.startsWith("ة")) visual++
            }
        }
        return visual > logical
    }

    /** Reverses an Arabic line into logical order, keeping embedded Latin/number runs readable. */
    fun visualToLogical(line: String): String {
        if (line.none(::isArabic)) return line
        val withRuns = ltrRun.replace(line.reversed()) { it.value.reversed() }
        // Combining marks belong after their base letter.
        val chars = withRuns.toCharArray()
        var i = 0
        while (i < chars.size - 1) {
            if (isMark(chars[i]) && !isMark(chars[i + 1])) { val t = chars[i]; chars[i] = chars[i + 1]; chars[i + 1] = t; i += 2 } else i++
        }
        return chars.concatToString()
    }

    fun normalize(s: String): String {
        if (s.none { it in '\uFE70'..'\uFEFF' }) return s
        return buildString { s.forEach { c -> append(table[c] ?: c.toString()) } }
    }
}
