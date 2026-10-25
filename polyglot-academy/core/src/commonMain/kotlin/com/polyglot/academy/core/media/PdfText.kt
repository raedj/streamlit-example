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
        val lines = s.split('\n').map { line -> line.replace(Regex("[ \\t\\u00A0]+"), " ").trim() }
        return lines.joinToString("\n")
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

    /** One shown glyph (or ActualText span) with its horizontal extent in device space. */
    private class Glyph(val text: String, val x: Double, val endX: Double, val size: Double)

    /**
     * Collects a page's text line by line. Lines without right-to-left script keep content-stream
     * order (robust when font metrics are approximate). Lines with Arabic are ordered by glyph
     * position, which is the visual order, and then converted to reading order.
     */
    private class LineBuilder(private val out: StringBuilder) {
        private val items = ArrayList<Glyph?>() // null = space hint from a TJ gap

        fun glyph(g: Glyph) {
            // A combining mark belongs to the glyph drawn just before it.
            val last = items.lastOrNull()
            if (last != null && g.text.isNotEmpty() && g.text.all(ArabicForms::isMark)) {
                items[items.size - 1] = Glyph(last.text + g.text, last.x, maxOf(last.endX, g.endX), last.size)
            } else items += g
        }

        fun spaceHint() { if (items.isNotEmpty() && items.last() != null) items += null }

        fun newline() {
            flush()
            if (out.isNotEmpty() && out.last() != '\n') out.append('\n')
        }

        fun flush() {
            if (items.isEmpty()) return
            val glyphs = items.filterNotNull()
            if (glyphs.any { g -> g.text.any(ArabicForms::isRtl) }) {
                val visual = StringBuilder()
                var prevEnd: Double? = null
                for (g in glyphs.sortedBy { it.x }) {
                    val gap = prevEnd?.let { g.x - it } ?: 0.0
                    if (gap > 0.2 * g.size && visual.isNotEmpty() && !visual.last().isWhitespace() && !g.text.startsWith(" ")) visual.append(' ')
                    // Multi-character units (ligatures, ActualText, base + mark) are already in reading
                    // order; pre-reverse them so the line reversal restores them.
                    visual.append(if (g.text.length > 1 && g.text.any(ArabicForms::isRtl)) g.text.reversed() else g.text)
                    prevEnd = maxOf(prevEnd ?: g.endX, g.endX)
                }
                out.append(ArabicForms.visualToLogical(visual.toString()))
            } else {
                items.forEach { out.append(it?.text ?: " ") }
            }
            items.clear()
        }
    }

    private class TextInterpreter(private val doc: PdfDocument, out: StringBuilder) {
        private val lines = LineBuilder(out)
        private var font: FontDecoder? = null
        private val fontCache = HashMap<PDict, FontDecoder>()
        private var fontSize = 0.0
        private var charSpacing = 0.0
        private var wordSpacing = 0.0
        private var hScale = 1.0
        private var leading = 0.0
        private var tm = IDENTITY
        private var lm = IDENTITY
        private var ctm = IDENTITY
        private val ctmStack = ArrayList<DoubleArray>()
        private var lineY: Double? = null
        /** Marked-content stack; a non-null entry is an ActualText replacement in progress. */
        private val marked = ArrayList<ActualText?>()

        private class ActualText(val text: String) { var x: Double? = null; var endX = 0.0 }

        fun run(data: ByteArray, resources: PDict?, depth: Int) {
            val lx = Lexer(data, 0)
            val stack = ArrayList<PObj>()
            val fonts = doc.dict(resources?.get("Font"))
            val xobjects = doc.dict(resources?.get("XObject"))
            while (true) {
                val o = lx.parseObject() ?: break
                if (o !is POp) { stack += o; if (stack.size > 4096) stack.clear(); continue }
                when (o.v) {
                    "q" -> if (ctmStack.size < 64) ctmStack += ctm
                    "Q" -> ctmStack.removeLastOrNull()?.let { ctm = it }
                    "cm" -> if (stack.size >= 6) ctm = multiply(matrix(stack), ctm)
                    "BT" -> { tm = IDENTITY; lm = IDENTITY }
                    // Text blocks on the same baseline (often separate runs of one line) stay together.
                    "ET" -> lines.spaceHint()
                    "Tf" -> {
                        fontSize = num(stack, 0)
                        (stack.getOrNull(stack.size - 2) as? PName)?.let { name ->
                            font = doc.dict(fonts?.get(name.v))?.let { d -> fontCache.getOrPut(d) { FontDecoder.of(doc, d) } }
                        }
                    }
                    "Tc" -> charSpacing = num(stack, 0)
                    "Tw" -> wordSpacing = num(stack, 0)
                    "Tz" -> hScale = num(stack, 0) / 100.0
                    "TL" -> leading = num(stack, 0)
                    "Td" -> moveLine(num(stack, 1), num(stack, 0))
                    "TD" -> { leading = -num(stack, 0); moveLine(num(stack, 1), num(stack, 0)) }
                    "Tm" -> if (stack.size >= 6) { tm = matrix(stack); lm = tm; checkLine() }
                    "T*" -> moveLine(0.0, -leading)
                    "Tj" -> show(stack.lastOrNull())
                    "'" -> { moveLine(0.0, -leading); show(stack.lastOrNull()) }
                    "\"" -> {
                        wordSpacing = num(stack, 2); charSpacing = num(stack, 1)
                        moveLine(0.0, -leading); show(stack.lastOrNull())
                    }
                    "TJ" -> (stack.lastOrNull() as? PArr)?.items?.forEach { item ->
                        when (item) {
                            is PStr -> show(item)
                            is PNum -> {
                                advance(-item.v / 1000.0 * fontSize * hScale)
                                if (item.v < -180) lines.spaceHint()
                            }
                            else -> {}
                        }
                    }
                    "BMC" -> marked += null
                    "BDC" -> marked += actualText(stack, resources)
                    "EMC" -> marked.removeLastOrNull()?.let { span ->
                        if (marked.none { it != null }) lines.glyph(Glyph(span.text, span.x ?: deviceX(), maxOf(span.endX, span.x ?: 0.0), effectiveSize()))
                    }
                    "Do" -> if (depth < MAX_FORM_DEPTH) (stack.lastOrNull() as? PName)?.let { name ->
                        val x = doc.resolve(xobjects?.get(name.v)) as? PStream
                        if (x != null && (x.dict["Subtype"] as? PName)?.v == "Form") {
                            val saved = font
                            val savedCtm = ctm
                            (doc.resolve(x.dict["Matrix"]) as? PArr)?.items?.takeIf { it.size == 6 }?.let { ctm = multiply(matrix(it), ctm) }
                            doc.decode(x)?.let { run(it, doc.dict(x.dict["Resources"]) ?: resources, depth + 1) }
                            font = saved
                            ctm = savedCtm
                        }
                    }
                    "ID" -> lx.skipInlineImage()
                }
                stack.clear()
            }
            lines.flush()
        }

        private fun actualText(stack: List<PObj>, resources: PDict?): ActualText? {
            val props = when (val p = stack.lastOrNull()) {
                is PDict -> p
                is PName -> doc.dict(doc.dict(resources?.get("Properties"))?.get(p.v))
                else -> null
            }
            val t = doc.resolve(props?.get("ActualText")) as? PStr ?: return null
            return ActualText(ArabicForms.normalize(textString(t.bytes)))
        }

        private fun moveLine(tx: Double, ty: Double) {
            lm = doubleArrayOf(lm[0], lm[1], lm[2], lm[3], tx * lm[0] + ty * lm[2] + lm[4], tx * lm[1] + ty * lm[3] + lm[5])
            tm = lm
            checkLine()
        }

        /** Starts a new output line when the baseline moves by more than a third of the text size. */
        private fun checkLine() {
            val y = deviceY()
            val prev = lineY
            if (prev != null && kotlin.math.abs(y - prev) > 0.33 * effectiveSize()) lines.newline()
            lineY = y
        }

        private fun show(o: PObj?) {
            val s = o as? PStr ?: return
            val f = font ?: FontDecoder.fallback
            if (lineY == null) lineY = deviceY()
            f.glyphs(s.bytes) { text, width, isSpace ->
                val x0 = deviceX()
                advance((width / 1000.0 * fontSize + charSpacing + if (isSpace) wordSpacing else 0.0) * hScale)
                val x1 = deviceX()
                val span = marked.lastOrNull { it != null }
                if (span != null) {
                    if (span.x == null) span.x = minOf(x0, x1)
                    span.endX = maxOf(span.endX, x0, x1)
                } else if (text.isNotEmpty()) {
                    lines.glyph(Glyph(ArabicForms.normalize(text), minOf(x0, x1), maxOf(x0, x1), effectiveSize()))
                }
            }
        }

        private fun advance(tx: Double) {
            tm = doubleArrayOf(tm[0], tm[1], tm[2], tm[3], tx * tm[0] + tm[4], tx * tm[1] + tm[5])
        }

        private fun deviceX() = tm[4] * ctm[0] + tm[5] * ctm[2] + ctm[4]
        private fun deviceY() = tm[4] * ctm[1] + tm[5] * ctm[3] + ctm[5]

        private fun effectiveSize(): Double {
            val t = kotlin.math.sqrt(kotlin.math.abs(tm[0] * tm[3] - tm[1] * tm[2]))
            val c = kotlin.math.sqrt(kotlin.math.abs(ctm[0] * ctm[3] - ctm[1] * ctm[2]))
            return (kotlin.math.abs(fontSize) * t * c).takeIf { it > 0.01 } ?: 1.0
        }

        private fun num(stack: List<PObj>, fromEnd: Int) = (stack.getOrNull(stack.size - 1 - fromEnd) as? PNum)?.v ?: 0.0
        private fun matrix(items: List<PObj>) = DoubleArray(6) { i -> (items[items.size - 6 + i] as? PNum)?.v ?: 0.0 }

        companion object {
            val IDENTITY = doubleArrayOf(1.0, 0.0, 0.0, 1.0, 0.0, 0.0)

            /** m × n for PDF's row-vector convention. */
            fun multiply(m: DoubleArray, n: DoubleArray) = doubleArrayOf(
                m[0] * n[0] + m[1] * n[2], m[0] * n[1] + m[1] * n[3],
                m[2] * n[0] + m[3] * n[2], m[2] * n[1] + m[3] * n[3],
                m[4] * n[0] + m[5] * n[2] + n[4], m[4] * n[1] + m[5] * n[3] + n[5],
            )

            /** PDF text strings: UTF-16BE with a byte-order mark, otherwise (approximately) PDFDocEncoding. */
            fun textString(b: ByteArray): String =
                if (b.size >= 2 && (b[0].toInt() and 0xFF) == 0xFE && (b[1].toInt() and 0xFF) == 0xFF) {
                    buildString { var i = 2; while (i + 1 < b.size) { append((((b[i].toInt() and 0xFF) shl 8) or (b[i + 1].toInt() and 0xFF)).toChar()); i += 2 } }
                } else latin1(b, 0, b.size)
        }
    }

    // ---------------------------------------------------------------- fonts

    private class FontDecoder(
        private val cmap: CMap?,
        private val simple: CharArray?,
        private val widths: (Long) -> Double,
        private val twoByte: Boolean,
    ) {
        /** Calls [each] with the text, width (glyph units, 1/1000 em) and word-space flag of every code. */
        fun glyphs(bytes: ByteArray, each: (String, Double, Boolean) -> Unit) {
            if (cmap != null) { cmap.forEachCode(bytes, if (twoByte) 2 else 0) { code, len -> each(cmap.text(code, len, simple), widths(code), len == 1 && code == 32L) }; return }
            if (simple == null) {
                // Composite font without ToUnicode: positions advance, characters are unknown.
                var i = 0
                while (i + 1 < bytes.size) { val code = ((bytes[i].toLong() and 0xFF) shl 8) or (bytes[i + 1].toLong() and 0xFF); each("", widths(code), false); i += 2 }
                return
            }
            bytes.forEach { b ->
                val code = b.toLong() and 0xFF
                val c = simple[code.toInt()]
                each(if (c == '\u0000') "" else c.toString(), widths(code), code == 32L)
            }
        }

        companion object {
            val fallback = FontDecoder(null, Encodings.winAnsi, { code -> Encodings.standardWidth("Helvetica", code) }, false)

            fun of(doc: PdfDocument, font: PDict): FontDecoder {
                val cmap = (doc.resolve(font["ToUnicode"]) as? PStream)?.let { doc.decode(it) }?.let { CMap.parse(it) }
                val subtype = (doc.resolve(font["Subtype"]) as? PName)?.v
                if (subtype == "Type0") {
                    val descendant = (doc.resolve(font["DescendantFonts"]) as? PArr)?.items?.firstOrNull()?.let { doc.dict(it) }
                    val dw = (doc.resolve(descendant?.get("DW")) as? PNum)?.v ?: 1000.0
                    val w = cidWidths(doc, doc.resolve(descendant?.get("W")) as? PArr)
                    return FontDecoder(cmap, null, { code -> w[code] ?: dw }, twoByte = true)
                }
                val first = (doc.resolve(font["FirstChar"]) as? PNum)?.v?.toInt() ?: 0
                val list = (doc.resolve(font["Widths"]) as? PArr)?.items?.map { (doc.resolve(it) as? PNum)?.v ?: 0.0 }
                val missing = (doc.resolve(doc.dict(font["FontDescriptor"])?.get("MissingWidth")) as? PNum)?.v
                val base = (doc.resolve(font["BaseFont"]) as? PName)?.v.orEmpty()
                val widths: (Long) -> Double = if (list != null) { code -> list.getOrNull(code.toInt() - first) ?: missing ?: 500.0 }
                    else { code -> Encodings.standardWidth(base, code) }
                return FontDecoder(cmap, simpleEncoding(doc, font), widths, twoByte = false)
            }

            private fun cidWidths(doc: PdfDocument, w: PArr?): Map<Long, Double> {
                val out = HashMap<Long, Double>()
                val items = w?.items?.map { doc.resolve(it) } ?: return out
                var i = 0
                while (i < items.size) {
                    val start = (items[i] as? PNum)?.v?.toLong() ?: break
                    when (val next = items.getOrNull(i + 1)) {
                        is PArr -> { next.items.forEachIndexed { k, v -> (doc.resolve(v) as? PNum)?.let { out[start + k] = it.v } }; i += 2 }
                        is PNum -> {
                            val end = next.v.toLong()
                            val width = (items.getOrNull(i + 2) as? PNum)?.v ?: break
                            if (end - start in 0..65_535) for (c in start..end) out[c] = width
                            i += 3
                        }
                        else -> break
                    }
                }
                return out
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
        /** Splits [bytes] into codes using the codespace ranges ([forcedLength] > 0 overrides them). */
        fun forEachCode(bytes: ByteArray, forcedLength: Int, each: (Long, Int) -> Unit) {
            var i = 0
            while (i < bytes.size) {
                val len = if (ranges.isEmpty() && forcedLength > 0) minOf(forcedLength, bytes.size - i) else codeLength(bytes, i)
                var code = 0L
                for (k in 0 until len) code = (code shl 8) or (bytes[i + k].toLong() and 0xFF)
                each(code, len)
                i += len
            }
        }

        fun text(code: Long, len: Int, simple: CharArray?): String =
            map[code] ?: if (len == 1 && simple != null) simple[code.toInt()].let { if (it == '\u0000') "" else it.toString() } else ""

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

    // Widths (1/1000 em) of printable ASCII in the standard Helvetica and Times-Roman fonts, used
    // only when a PDF relies on a standard font without a /Widths array.
    private val helvetica = intArrayOf(278, 278, 355, 556, 556, 889, 667, 191, 333, 333, 389, 584, 278, 333, 278, 278,
        556, 556, 556, 556, 556, 556, 556, 556, 556, 556, 278, 278, 584, 584, 584, 556, 1015, 667, 667, 722, 722, 667, 611, 778, 722, 278, 500,
        667, 556, 833, 722, 778, 667, 778, 722, 667, 611, 722, 667, 944, 667, 667, 611, 278, 278, 278, 469, 556, 333, 556, 556, 500, 556, 556,
        278, 556, 556, 222, 222, 500, 222, 833, 556, 556, 556, 556, 333, 500, 278, 556, 500, 722, 500, 500, 500, 334, 260, 334, 584)
    private val times = intArrayOf(250, 333, 408, 500, 500, 833, 778, 180, 333, 333, 500, 564, 250, 333, 250, 278,
        500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 278, 278, 564, 564, 564, 444, 921, 722, 667, 667, 722, 611, 556, 722, 722, 333, 389,
        722, 611, 889, 722, 722, 556, 722, 667, 556, 611, 722, 722, 944, 722, 722, 611, 333, 278, 333, 469, 500, 333, 444, 500, 444, 500, 444,
        333, 500, 500, 278, 278, 500, 278, 778, 500, 500, 500, 500, 333, 389, 278, 500, 500, 722, 500, 500, 444, 480, 200, 480, 541)

    fun standardWidth(baseFont: String, code: Long): Double {
        val name = baseFont.substringAfter('+')
        if (name.startsWith("Courier")) return 600.0
        val table = if (name.startsWith("Times")) times else helvetica
        return (if (code in 32L..126L) table[(code - 32).toInt()] else table[0x61 - 32]).toDouble()
    }

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

    /** Arabic and Hebrew blocks, including presentation forms. */
    fun isRtl(c: Char) = c in '\u0590'..'\u08FF' || c in '\uFB1D'..'\uFDFF' || c in '\uFE70'..'\uFEFF'
    fun isMark(c: Char) = c in '\u064B'..'\u065F' || c == '\u0670' || c in '\u0591'..'\u05C7'
    private val ltrRun = Regex("[A-Za-z0-9\\u00C0-\\u024F](?:[A-Za-z0-9\\u00C0-\\u024F .,:/%-]*[A-Za-z0-9\\u00C0-\\u024F%])?")
    /**
     * Converts a right-to-left line from visual (left-to-right glyph) order to reading order: the
     * line is reversed and Latin/number runs are turned back around. Brackets are not mirrored:
     * ToUnicode maps a mirrored bracket glyph to the character the author typed.
     */
    fun visualToLogical(line: String): String {
        if (line.none(::isRtl)) return line
        val rtlLetters = line.count { isRtl(it) && it.isLetter() }
        val ltrLetters = line.count { !isRtl(it) && it.isLetter() }
        if (ltrLetters > rtlLetters) {
            // Left-to-right line with embedded Arabic: only the Arabic runs are turned around.
            return rtlRun.replace(line) { it.value.reversed() }
        }
        return ltrRun.replace(line.reversed()) { it.value.reversed() }
    }

    private val rtlRun = Regex("[\\u0590-\\u08FF\\uFB1D-\\uFDFF\\uFE70-\\uFEFF](?:[\\u0590-\\u08FF\\uFB1D-\\uFDFF\\uFE70-\\uFEFF \\u060C.:!؟]*[\\u0590-\\u08FF\\uFB1D-\\uFDFF\\uFE70-\\uFEFF])?")

    fun normalize(s: String): String {
        if (s.none { it in '\uFE70'..'\uFEFF' }) return s
        return buildString { s.forEach { c -> append(table[c] ?: c.toString()) } }
    }
}
