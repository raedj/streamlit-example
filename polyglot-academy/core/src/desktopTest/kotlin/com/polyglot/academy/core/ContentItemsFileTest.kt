package com.polyglot.academy.core

import com.polyglot.academy.core.content.ContentCheck
import com.polyglot.academy.core.content.Curriculum
import com.polyglot.academy.core.model.LanguageCode
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * content/review/items/<lang>.curriculum.tsv is what tools/flag_content.py sends to the AI models, so it must
 * match the content in the app. Set POLYGLOT_WRITE_CONTENT_ITEMS=1 when running the tests (or delete the file) to rewrite it.
 */
class ContentItemsFileTest {
    private val dir = generateSequence(File("").absoluteFile) { it.parentFile }.map { File(it, "content/review") }.first { it.isDirectory }.resolve("items")

    @Test fun theItemFilesMatchTheContent() {
        val rewrite = System.getenv("POLYGLOT_WRITE_CONTENT_ITEMS") == "1"
        listOf(LanguageCode.ENGLISH, LanguageCode.MANDARIN, LanguageCode.MALAY).forEach { lang ->
            val items = ContentCheck.items(Curriculum.unfilteredOf(lang))
            assertEquals(items.size, items.map { it.id }.toSet().size, "${lang.tag}: duplicate item ids")
            val text = "id\tkind\tlevel\ttext\n" + items.joinToString("") { "${it.id}\t${it.kind}\t${it.level}\t${it.text}\n" }
            val file = dir.resolve("${lang.tag}.curriculum.tsv")
            if (rewrite || !file.exists()) { dir.mkdirs(); file.writeText(text) }
            assertTrue(text == file.readText(), "${file.path} is out of date: run the core tests with POLYGLOT_WRITE_CONTENT_ITEMS=1")
        }
    }
}
