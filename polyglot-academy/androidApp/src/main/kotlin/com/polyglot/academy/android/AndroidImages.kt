package com.polyglot.academy.android

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import com.polyglot.academy.ui.feature.me.CropRect
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File

/** Photo handling for import: upright, scaled for text recognition, cropped, saved as JPEG. */
internal object AndroidImages {
    /** Longest side kept from a camera photo; enough for printed text and far smaller than a full sensor image. */
    private const val MAX_SIDE = 2400
    private const val QUALITY = 92

    /** A camera file as upright JPEG bytes no larger than [MAX_SIDE], or null if it cannot be read. */
    fun fromCamera(file: File): ByteArray? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.path, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_SIDE) sample *= 2
        val decoded = BitmapFactory.decodeFile(file.path, BitmapFactory.Options().apply { inSampleSize = sample }) ?: return null
        val turns = when (ExifInterface(file.path).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 1
            ExifInterface.ORIENTATION_ROTATE_180 -> 2
            ExifInterface.ORIENTATION_ROTATE_270 -> 3
            else -> 0
        }
        val longest = maxOf(decoded.width, decoded.height)
        val scale = if (longest > MAX_SIDE) MAX_SIDE.toFloat() / longest else 1f
        return jpeg(transform(decoded, turns, scale))
    }

    fun edit(bytes: ByteArray, crop: CropRect, quarterTurns: Int): ByteArray? {
        val decoded = BitmapFactory.decodeStream(ByteArrayInputStream(bytes)) ?: return null
        val turned = transform(decoded, quarterTurns, 1f)
        val (x, y, w, h) = crop.pixels(turned.width, turned.height).toList()
        val cropped = if (x == 0 && y == 0 && w == turned.width && h == turned.height) turned else Bitmap.createBitmap(turned, x, y, w, h)
        return jpeg(cropped)
    }

    private fun transform(bitmap: Bitmap, quarterTurns: Int, scale: Float): Bitmap {
        val turns = ((quarterTurns % 4) + 4) % 4
        if (turns == 0 && scale == 1f) return bitmap
        val m = Matrix().apply { postScale(scale, scale); postRotate(90f * turns) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
    }

    private fun jpeg(bitmap: Bitmap): ByteArray = ByteArrayOutputStream().use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, QUALITY, out)
        out.toByteArray()
    }
}
