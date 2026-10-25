package com.polyglot.academy.core

import com.polyglot.academy.core.media.ArabicForms
import com.polyglot.academy.core.media.Encodings
import com.polyglot.academy.core.media.PdfTextExtractor
import com.polyglot.academy.core.media.PdfTextResult
import java.io.ByteArrayOutputStream
import java.util.zip.Deflater
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PdfTextTest {
    private fun fixture(name: String) = javaClass.getResourceAsStream("/pdf/$name")!!.readBytes()
    private fun text(bytes: ByteArray) = assertIs<PdfTextResult.Text>(PdfTextExtractor.extract(bytes))

    // Fixtures were produced with reportlab 5.0.1 and LibreOffice (writer_pdf_Export); `pdftotext` gives the same words.
    @Test fun reportlabStandardFontsWithCompressedPages() {
        val r = text(fixture("reportlab_winansi.pdf"))
        assertEquals(2, r.pages)
        assertEquals("Café crème à côté de la gare.\nWhere is the station? It's straight on.\n\nPage two: ¿Dónde está la estación?", r.text)
    }

    @Test fun libreOfficeEmbeddedFontsInFourScripts() {
        val t = text(fixture("libreoffice_multilingual.pdf")).text
        assertTrue("Hello, my name is Laila. I study Spanish every morning." in t, t)
        assertTrue("Quisiera un café con leche, por favor." in t, t)
        assertTrue("我要一杯茶。谢谢！" in t, t)
        // Stored in visual order by the writer; words come back in reading order.
        assertTrue("مرحبًا اسمي عمر أنا طالب في مدرسة جديدة" in t, t)
    }

    @Test fun objectStreamsCidFontsAndKerningSpaces() {
        val cmap = """
            /CIDInit /ProcSet findresource begin 12 dict begin begincmap
            1 begincodespacerange <0000> <FFFF> endcodespacerange
            2 beginbfchar <0001> <0048> <0002> <0069> endbfchar
            1 beginbfrange <0010> <0012> <4F60> endbfrange
            1 beginbfrange <0020> <0021> [<6211> <0021>] endbfrange
            endcmap end end
        """.trimIndent()
        // "Hi" then a large TJ gap (word space), then 你 佡 佢 我 !
        val content = "BT /F1 12 Tf 72 700 Td [<00010002> -300 <0010>] TJ 0 -14 Td <001100120020> Tj <0021> Tj ET"
        val pdf = Builder()
            .obj(1, "<< /Type /Catalog /Pages 2 0 R >>")
            .objectStream(10, listOf(2 to "<< /Type /Pages /Kids [3 0 R] /Count 1 /Resources << /Font << /F1 5 0 R >> >> >>",
                3 to "<< /Type /Page /Parent 2 0 R /Contents 4 0 R >>"))
            .stream(4, content, deflate = true)
            .obj(5, "<< /Type /Font /Subtype /Type0 /BaseFont /X /Encoding /Identity-H /ToUnicode 6 0 R >>")
            .stream(6, cmap, deflate = true)
            .build()
        assertEquals("Hi 你\n佡佢我!", text(pdf).text)
    }

    @Test fun simpleFontDifferencesAndFormXObjects() {
        val pdf = Builder()
            .obj(1, "<< /Type /Catalog /Pages 2 0 R >>")
            .obj(2, "<< /Type /Pages /Kids [3 0 R] /Count 1 >>")
            .obj(3, "<< /Type /Page /Parent 2 0 R /Contents 4 0 R /Resources << /XObject << /X1 6 0 R >> /Font << /F1 5 0 R >> >> >>")
            .stream(4, "q /X1 Do Q BT /F1 10 Tf (Fin) Tj ET")
            .obj(5, "<< /Type /Font /Subtype /Type1 /Encoding << /BaseEncoding /WinAnsiEncoding /Differences [65 /eacute /uni0645] >> >>")
            .stream(6, "BT /F1 10 Tf (Caf) Tj (A) Tj (B) Tj ET", extraDict = "/Type /XObject /Subtype /Form /Resources << /Font << /F1 5 0 R >> >>")
            .build()
        assertEquals("Caféم\nFin", text(pdf).text)
    }

    @Test fun encryptedScannedAndNonPdfFilesAreReported() {
        val encrypted = Builder().obj(1, "<< /Type /Catalog /Pages 2 0 R >>").obj(2, "<< /Type /Pages /Kids [] /Count 0 >>").build("/Encrypt 9 0 R")
        assertEquals(PdfTextResult.Encrypted, PdfTextExtractor.extract(encrypted))
        val scanned = Builder()
            .obj(1, "<< /Type /Catalog /Pages 2 0 R >>")
            .obj(2, "<< /Type /Pages /Kids [3 0 R] /Count 1 >>")
            .obj(3, "<< /Type /Page /Parent 2 0 R /Contents 4 0 R >>")
            .stream(4, "q 612 0 0 792 0 0 cm /Im1 Do Q")
            .build()
        assertEquals(PdfTextResult.NoText, PdfTextExtractor.extract(scanned))
        assertEquals(PdfTextResult.NotPdf, PdfTextExtractor.extract("hello".encodeToByteArray()))
    }

    @Test fun truncatedFilesDoNotCrash() {
        val bytes = fixture("libreoffice_multilingual.pdf")
        for (cut in listOf(10, 500, bytes.size / 3, bytes.size / 2, bytes.size - 100)) {
            PdfTextExtractor.extract(bytes.copyOf(cut))
        }
    }

    @Test fun glyphNamesAndArabicPresentationForms() {
        assertEquals('é', Encodings.glyph("eacute"))
        assertEquals('Ü', Encodings.glyph("Udieresis"))
        assertEquals('ñ', Encodings.glyph("ntilde"))
        assertEquals('م', Encodings.glyph("uni0645"))
        assertEquals("مرحبا", ArabicForms.normalize("ﻣﺮﺣﺒﺎ"))
        assertEquals("لا", ArabicForms.normalize("ﻻ"))
    }

    /** Writes minimal PDFs; the extractor scans objects, so no cross-reference table is needed. */
    private class Builder {
        private val out = ByteArrayOutputStream().apply { write("%PDF-1.7\n%âã\n".toByteArray(Charsets.ISO_8859_1)) }

        fun obj(n: Int, body: String) = apply { out.write("$n 0 obj\n$body\nendobj\n".toByteArray(Charsets.ISO_8859_1)) }

        fun stream(n: Int, data: String, deflate: Boolean = false, extraDict: String = "") = apply {
            val raw = data.toByteArray(Charsets.UTF_8)
            val bytes = if (deflate) zlib(raw) else raw
            val filter = if (deflate) "/Filter /FlateDecode " else ""
            out.write("$n 0 obj\n<< $extraDict $filter/Length ${bytes.size} >>\nstream\n".toByteArray(Charsets.ISO_8859_1))
            out.write(bytes)
            out.write("\nendstream\nendobj\n".toByteArray(Charsets.ISO_8859_1))
        }

        fun objectStream(n: Int, objects: List<Pair<Int, String>>) = apply {
            val bodies = StringBuilder()
            val header = StringBuilder()
            objects.forEach { (num, body) -> header.append("$num ${bodies.length} "); bodies.append(body).append('\n') }
            val data = zlib((header.toString() + bodies).toByteArray(Charsets.ISO_8859_1))
            out.write("$n 0 obj\n<< /Type /ObjStm /N ${objects.size} /First ${header.length} /Filter /FlateDecode /Length ${data.size} >>\nstream\n".toByteArray(Charsets.ISO_8859_1))
            out.write(data)
            out.write("\nendstream\nendobj\n".toByteArray(Charsets.ISO_8859_1))
        }

        fun build(trailerExtra: String = ""): ByteArray {
            out.write("trailer\n<< /Root 1 0 R $trailerExtra >>\n%%EOF\n".toByteArray(Charsets.ISO_8859_1))
            return out.toByteArray()
        }

        private fun zlib(b: ByteArray): ByteArray {
            val d = Deflater().apply { setInput(b); finish() }
            val buf = ByteArray(b.size + 64)
            val n = d.deflate(buf)
            d.end()
            return buf.copyOf(n)
        }
    }
}
