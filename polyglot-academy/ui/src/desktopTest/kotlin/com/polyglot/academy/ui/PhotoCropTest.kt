package com.polyglot.academy.ui

import com.polyglot.academy.ui.feature.me.CropRect
import com.polyglot.academy.ui.platform.SkiaImages
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PhotoCropTest {
    @Test fun cornersAndFrameAreFoundByTouch() {
        val r = CropRect(0.2f, 0.2f, 0.8f, 0.8f)
        assertEquals(CropRect.Handle.TOP_LEFT, r.handleAt(0.21f, 0.19f, 0.05f, 0.05f))
        assertEquals(CropRect.Handle.BOTTOM_RIGHT, r.handleAt(0.83f, 0.8f, 0.05f, 0.05f))
        assertEquals(CropRect.Handle.MOVE, r.handleAt(0.5f, 0.5f, 0.05f, 0.05f))
        assertNull(r.handleAt(0.05f, 0.5f, 0.05f, 0.05f))
    }

    @Test fun dragsStayInsideThePhotoAndKeepAMinimumSize() {
        val r = CropRect(0.2f, 0.2f, 0.8f, 0.8f)
        assertEquals(CropRect(0f, 0f, 0.8f, 0.8f), r.dragged(CropRect.Handle.TOP_LEFT, -0.5f, -0.5f))
        val squeezed = r.dragged(CropRect.Handle.BOTTOM_RIGHT, -0.9f, -0.9f)
        assertEquals(0.2f + CropRect.MIN, squeezed.right, 1e-6f)
        assertEquals(0.2f + CropRect.MIN, squeezed.bottom, 1e-6f)
        // Moving keeps the size and stops at the edge.
        assertEquals(CropRect(0.4f, 0f, 1f, 0.6f), r.dragged(CropRect.Handle.MOVE, 0.5f, -0.5f).let { CropRect(it.left.r(), it.top.r(), it.right.r(), it.bottom.r()) })
    }

    @Test fun pixelBoundsAreNeverEmpty() {
        assertEquals(listOf(0, 0, 200, 100), CropRect.FULL.pixels(200, 100).toList())
        assertEquals(listOf(50, 25, 100, 50), CropRect(0.25f, 0.25f, 0.75f, 0.75f).pixels(200, 100).toList())
        assertEquals(listOf(1, 1, 1, 1), CropRect(1f, 1f, 1f, 1f).pixels(2, 2).toList())
    }

    @Test fun imagesAreTurnedThenCropped() {
        val photo = PhotoTestImages.jpeg(200, 100)
        assertEquals(100 to 200, PhotoTestImages.size(SkiaImages.edit(photo, CropRect.FULL, 1)!!))
        assertEquals(100 to 50, PhotoTestImages.size(SkiaImages.edit(photo, CropRect(0f, 0f, 0.5f, 0.5f), 0)!!))
        assertEquals(50 to 100, PhotoTestImages.size(SkiaImages.edit(photo, CropRect(0f, 0f, 0.5f, 0.5f), 3)!!))
        // The black square is in the top-left quarter; after a quarter turn clockwise it is top-right.
        val turned = org.jetbrains.skia.Image.makeFromEncoded(SkiaImages.edit(photo, CropRect.FULL, 1)!!)
        val bitmap = org.jetbrains.skia.Bitmap.makeFromImage(turned)
        assertTrue(brightness(bitmap.getColor(75, 25)) < 60, "top-right is dark")
        assertTrue(brightness(bitmap.getColor(25, 25)) > 200, "top-left is light")
    }

    private fun brightness(argb: Int) = ((argb shr 16 and 0xFF) + (argb shr 8 and 0xFF) + (argb and 0xFF)) / 3
    private fun Float.r() = kotlin.math.round(this * 1000) / 1000
}
