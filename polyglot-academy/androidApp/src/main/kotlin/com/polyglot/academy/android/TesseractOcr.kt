package com.polyglot.academy.android

import android.content.Context
import android.graphics.BitmapFactory
import com.googlecode.tesseract.android.TessBaseAPI
import com.polyglot.academy.core.model.LanguageCode
import com.polyglot.academy.core.service.DeviceOcr
import com.polyglot.academy.core.service.OcrPage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

/**
 * On-device text recognition with Tesseract (Tesseract4Android, Apache-2.0) and the "fast" models
 * from tesseract-ocr/tessdata_fast (Apache-2.0) bundled in assets/tessdata, one or more per learning
 * language. Only languages whose models are all bundled are offered. Nothing is sent anywhere.
 */
class TesseractOcr(private val context: Context) : DeviceOcr {
    // Mandarin: the first model is for simplified text, the second for traditional. Each also reads the
    // other script but writes it in its own (and is the more confident either way), so the learner's
    // choice of script decides which one runs.
    private val models: Map<LanguageCode, List<String>> = mapOf(
        LanguageCode.ARABIC to listOf("ara"), LanguageCode.URDU to listOf("urd"), LanguageCode.ENGLISH to listOf("eng"),
        LanguageCode.MANDARIN to listOf("chi_sim", "chi_tra"), LanguageCode.SPANISH to listOf("spa"), LanguageCode.FRENCH to listOf("fra"),
        LanguageCode.TURKISH to listOf("tur"), LanguageCode.MALAY to listOf("msa"),
    )
    // TessBaseAPI is not thread-safe and needs a few MB per instance, so pages are read one at a time.
    private val lock = Mutex()

    override val languages: Set<LanguageCode> = run {
        val bundled = runCatching { context.assets.list("tessdata")?.toSet() }.getOrNull().orEmpty()
        models.filterValues { parts -> parts.all { "$it.traineddata" in bundled } }.keys
    }

    override suspend fun recognize(page: OcrPage, language: LanguageCode, traditionalChinese: Boolean): String = withContext(Dispatchers.Default) {
        val parts = models[language] ?: return@withContext ""
        val model = if (traditionalChinese && parts.size > 1) parts[1] else parts[0]
        lock.withLock {
            val bitmap = BitmapFactory.decodeByteArray(page.bytes, 0, page.bytes.size) ?: return@withLock ""
            try {
                read(dataDir(listOf(model)).absolutePath, model, bitmap).orEmpty()
            } finally {
                bitmap.recycle()
            }
        }
    }

    /** The text read with one model, or null if Tesseract cannot start. */
    private fun read(root: String, model: String, bitmap: android.graphics.Bitmap): String? {
        val api = TessBaseAPI()
        try {
            if (!api.init(root, model)) return null
            // One block of text: on test photos this kept lines that automatic layout analysis dropped.
            api.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_BLOCK)
            api.setImage(bitmap)
            return api.getUTF8Text().orEmpty()
        } finally {
            api.recycle()
        }
    }

    /** Tesseract reads models from a real directory, so each model is copied once into private storage. */
    private fun dataDir(parts: List<String>): File {
        val root = File(context.noBackupFilesDir, "tesseract")
        for (model in parts) {
            val target = File(root, "tessdata/$model.traineddata")
            if (target.exists()) continue
            target.parentFile!!.mkdirs()
            val tmp = File(target.parentFile, "$model.part")
            context.assets.open("tessdata/$model.traineddata").use { input -> tmp.outputStream().use { input.copyTo(it) } }
            tmp.renameTo(target)
        }
        return root
    }
}
