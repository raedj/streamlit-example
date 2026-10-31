package com.polyglot.academy.ui.feature.me

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Rotate90DegreesCw
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.polyglot.academy.core.service.ImportInput
import com.polyglot.academy.core.service.ImportProgress
import com.polyglot.academy.ui.app.LocalApp
import com.polyglot.academy.ui.app.ScrollScreen
import com.polyglot.academy.ui.app.rememberToast
import com.polyglot.academy.ui.designsystem.PolyglotTheme
import com.polyglot.academy.ui.designsystem.PrimaryButton
import com.polyglot.academy.ui.designsystem.SecondaryButton
import com.polyglot.academy.ui.designsystem.StateMessage
import com.polyglot.academy.ui.i18n.K
import com.polyglot.academy.ui.i18n.LocalStrings
import com.polyglot.academy.ui.navigation.Route
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.math.abs
import kotlin.math.roundToInt

/** Part of a photo to keep, as fractions of its width and height. */
data class CropRect(val left: Float = 0f, val top: Float = 0f, val right: Float = 1f, val bottom: Float = 1f) {
    enum class Handle { TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, MOVE }

    val isFull: Boolean get() = left <= 0f && top <= 0f && right >= 1f && bottom >= 1f

    /** The frame after dragging [handle] by ([dx], [dy]), kept inside the photo and at least [MIN] wide and high. */
    fun dragged(handle: Handle, dx: Float, dy: Float): CropRect = when (handle) {
        Handle.MOVE -> {
            val mx = dx.coerceIn(-left, 1f - right); val my = dy.coerceIn(-top, 1f - bottom)
            CropRect(left + mx, top + my, right + mx, bottom + my)
        }
        Handle.TOP_LEFT -> copy(left = (left + dx).coerceIn(0f, right - MIN), top = (top + dy).coerceIn(0f, bottom - MIN))
        Handle.TOP_RIGHT -> copy(right = (right + dx).coerceIn(left + MIN, 1f), top = (top + dy).coerceIn(0f, bottom - MIN))
        Handle.BOTTOM_LEFT -> copy(left = (left + dx).coerceIn(0f, right - MIN), bottom = (bottom + dy).coerceIn(top + MIN, 1f))
        Handle.BOTTOM_RIGHT -> copy(right = (right + dx).coerceIn(left + MIN, 1f), bottom = (bottom + dy).coerceIn(top + MIN, 1f))
    }

    /** The handle under a touch at ([x], [y]): the nearest corner within reach, else the frame itself, else null. */
    fun handleAt(x: Float, y: Float, reachX: Float, reachY: Float): Handle? {
        val corners = listOf(Handle.TOP_LEFT to (left to top), Handle.TOP_RIGHT to (right to top),
            Handle.BOTTOM_LEFT to (left to bottom), Handle.BOTTOM_RIGHT to (right to bottom))
        corners.filter { (_, p) -> abs(x - p.first) <= reachX && abs(y - p.second) <= reachY }
            .minByOrNull { (_, p) -> abs(x - p.first) / reachX + abs(y - p.second) / reachY }?.let { return it.first }
        return if (x in left..right && y in top..bottom) Handle.MOVE else null
    }

    /** Pixel bounds in a [width] x [height] image: x, y, width, height, never empty. */
    fun pixels(width: Int, height: Int): IntArray {
        val l = (left * width).roundToInt().coerceIn(0, width - 1)
        val t = (top * height).roundToInt().coerceIn(0, height - 1)
        val r = (right * width).roundToInt().coerceIn(l + 1, width)
        val b = (bottom * height).roundToInt().coerceIn(t + 1, height)
        return intArrayOf(l, t, r - l, b - t)
    }

    companion object {
        const val MIN = 0.1f
        val FULL = CropRect()
    }
}

/** The photo between the camera and the crop screen; kept in memory only. */
object PendingPhoto {
    var bytes: ByteArray? = null
}

@OptIn(kotlin.time.ExperimentalTime::class)
private fun photoTime(): String {
    val t = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    fun two(n: Int) = n.toString().padStart(2, '0')
    // Isolated left to right, so the date is not reordered inside an Arabic title.
    return "⁦${t.year}-${two(t.month.number)}-${two(t.day)} ${two(t.hour)}:${two(t.minute)}⁩"
}

/** Crop, turn or retake a photo, then import it; the material screen then offers to read it on the device. */
@Composable
fun PhotoImportScreen() {
    val app = LocalApp.current
    val s = LocalStrings.current
    val scope = rememberCoroutineScope()
    val toast = rememberToast()
    val lang = app.profile.activeLearningLanguage
    var photo by remember { mutableStateOf(PendingPhoto.bytes) }
    var crop by remember(photo) { mutableStateOf(CropRect.FULL) }
    var busy by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf<Float?>(null) }
    val image = remember(photo) { photo?.let { app.platformUi.decodeImage(it) } }
    // Leaving the screen, by importing or going back, drops the photo from memory.
    DisposableEffect(Unit) { onDispose { PendingPhoto.bytes = null } }

    fun retake() = scope.launch { app.platformUi.takePhoto()?.let { PendingPhoto.bytes = it; photo = it } }

    fun edit(frame: CropRect, turns: Int, then: suspend (ByteArray) -> Unit) = scope.launch {
        val p = photo ?: return@launch
        busy = true
        val out = if (frame.isFull && turns == 0) p else app.platformUi.editImage(p, frame, turns)
        busy = false
        if (out == null) toast(s[K.PHOTO_EDIT_FAILED]) else then(out)
    }

    suspend fun import(bytes: ByteArray) {
        val name = s.f(K.PHOTO_NAME, photoTime())
        app.services.materials.import(lang, ImportInput(name, "image/jpeg", "$name.jpg", bytes.size.toLong(), null, bytes), false).collect { p ->
            when (p) {
                is ImportProgress.Processing -> progress = p.fraction
                is ImportProgress.Done -> {
                    progress = null
                    if (app.services.materials.canTryOcr(p.material)) PendingOcrFiles.put(p.material.id, "image/jpeg", bytes)
                    PendingPhoto.bytes = null
                    app.nav.replace(Route.Material(p.material.id))
                }
                is ImportProgress.Rejected -> { progress = null; toast(s[K.MATERIAL_UNSUPPORTED_TYPE]) }
            }
        }
    }

    ScrollScreen(s[K.PHOTO_TITLE]) {
        val bitmap = image
        if (photo == null || bitmap == null) {
            StateMessage(s[K.PHOTO_LOST_TITLE], s[K.PHOTO_LOST_BODY])
            if (app.platformUi.canTakePhoto) PrimaryButton(s[K.PHOTO_TAKE], { retake() }, Modifier.fillMaxWidth(), icon = Icons.Filled.CameraAlt)
            return@ScrollScreen
        }
        Text(s[K.PHOTO_CROP_HINT], style = MaterialTheme.typography.bodyMedium)
        CropArea(bitmap, crop, { crop = it }, s[K.PHOTO_CROP_AREA])
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SecondaryButton(s[K.PHOTO_ROTATE], { edit(CropRect.FULL, 1) { PendingPhoto.bytes = it; photo = it } }, Modifier.weight(1f), enabled = !busy, icon = Icons.Filled.Rotate90DegreesCw)
            SecondaryButton(s[K.PHOTO_WHOLE], { crop = CropRect.FULL }, Modifier.weight(1f), enabled = !crop.isFull, icon = Icons.Filled.Crop)
        }
        if (app.platformUi.canTakePhoto) SecondaryButton(s[K.PHOTO_RETAKE], { retake() }, Modifier.fillMaxWidth(), enabled = !busy, icon = Icons.Filled.CameraAlt)
        PrimaryButton(s[K.PHOTO_USE], { edit(crop, 0) { import(it) } }, Modifier.fillMaxWidth(), enabled = !busy && progress == null)
        progress?.let { f ->
            Text(s.f(K.MATERIAL_PROCESSING, (f * 100).toInt()))
            LinearProgressIndicator(progress = { f }, modifier = Modifier.fillMaxWidth(), drawStopIndicator = {})
        }
    }
}

/** The photo with a frame whose corners (or middle) can be dragged. Touch coordinates are not mirrored in RTL. */
@Composable
private fun CropArea(image: ImageBitmap, crop: CropRect, onChange: (CropRect) -> Unit, description: String) {
    val current by rememberUpdatedState(crop)
    val change by rememberUpdatedState(onChange)
    val ratio = image.width.toFloat() / image.height
    val accent = PolyglotTheme.colors.primary
    // The touch area reaches past the photo so corners at its edges can be grabbed all round.
    val margin = 24.dp
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Box(Modifier.semantics { contentDescription = description }.pointerInput(Unit) {
            val m = margin.toPx()
            var handle: CropRect.Handle? = null
            detectDragGestures(
                onDragStart = { o ->
                    val w = size.width - 2 * m; val h = size.height - 2 * m
                    val reach = 32.dp.toPx()
                    handle = current.handleAt((o.x - m) / w, (o.y - m) / h, reach / w, reach / h)
                },
                onDragEnd = { handle = null },
                onDragCancel = { handle = null },
            ) { pointer, d ->
                val h = handle ?: return@detectDragGestures
                pointer.consume()
                change(current.dragged(h, d.x / (size.width - 2 * m), d.y / (size.height - 2 * m)))
            }
        }.padding(margin)) {
        // Tall photos are limited in height so the buttons stay in reach.
        Box(if (ratio < 1f) Modifier.height(420.dp).aspectRatio(ratio, matchHeightConstraintsFirst = true) else Modifier.fillMaxWidth().aspectRatio(ratio)) {
            Image(image, null, Modifier.fillMaxSize(), contentScale = ContentScale.FillBounds)
            Canvas(Modifier.fillMaxSize()) {
                val l = crop.left * size.width; val t = crop.top * size.height
                val r = crop.right * size.width; val b = crop.bottom * size.height
                val shade = Color.Black.copy(alpha = 0.5f)
                drawRect(shade, Offset.Zero, Size(size.width, t))
                drawRect(shade, Offset(0f, b), Size(size.width, size.height - b))
                drawRect(shade, Offset(0f, t), Size(l, b - t))
                drawRect(shade, Offset(r, t), Size(size.width - r, b - t))
                drawRect(Color.White, Offset(l, t), Size(r - l, b - t), style = Stroke(2.dp.toPx()))
                listOf(Offset(l, t), Offset(r, t), Offset(l, b), Offset(r, b)).forEach {
                    drawCircle(Color.White, 10.dp.toPx(), it)
                    drawCircle(accent, 7.dp.toPx(), it)
                }
            }
        }
        }
    }
}
