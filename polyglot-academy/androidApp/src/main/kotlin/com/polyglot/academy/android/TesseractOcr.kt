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
 * from tesseract-ocr/tessdata_fast (Apache-2.0) bundled in assets/tessdata. Only the languages whose
 * model is bundled are offered. Nothing is sent anywhere.
 */
class TesseractOcr(private val context: Context) : DeviceOcr {
    private val models = mapOf(LanguageCode.ARABIC to "ara", LanguageCode.URDU to "urd")
    // TessBaseAPI is not thread-safe and needs a few MB per instance, so pages are read one at a time.
    private val lock = Mutex()

    override val languages: Set<LanguageCode> =
        models.filterValues { m -> runCatching { context.assets.list("tessdata")?.contains("$m.traineddata") == true }.getOrDefault(false) }.keys

    override suspend fun recognize(page: OcrPage, language: LanguageCode): String = withContext(Dispatchers.Default) {
        val model = models[language] ?: return@withContext ""
        lock.withLock {
            val bitmap = BitmapFactory.decodeByteArray(page.bytes, 0, page.bytes.size) ?: return@withLock ""
            val api = TessBaseAPI()
            try {
                if (!api.init(dataDir(model).absolutePath, model)) return@withLock ""
                // One block of text: on test photos this kept lines that automatic layout analysis dropped.
                api.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_BLOCK)
                api.setImage(bitmap)
                api.getUTF8Text().orEmpty()
            } finally {
                api.recycle()
                bitmap.recycle()
            }
        }
    }

    /** Tesseract reads models from a real directory, so the asset is copied once into private storage. */
    private fun dataDir(model: String): File {
        val root = File(context.noBackupFilesDir, "tesseract")
        val target = File(root, "tessdata/$model.traineddata")
        if (!target.exists()) {
            target.parentFile!!.mkdirs()
            val tmp = File(target.parentFile, "$model.part")
            context.assets.open("tessdata/$model.traineddata").use { input -> tmp.outputStream().use { input.copyTo(it) } }
            tmp.renameTo(target)
        }
        return root
    }
}
