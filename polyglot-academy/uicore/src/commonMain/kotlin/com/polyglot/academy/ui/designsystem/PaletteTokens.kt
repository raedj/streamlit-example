package com.polyglot.academy.ui.designsystem

import kotlin.math.pow

/**
 * Platform-neutral colour tokens (ARGB). The Compose layer maps them to Color. Keeping the values
 * here lets contrast requirements be unit-tested without a UI runtime.
 */
data class PaletteSpec(
    val primary: Long,
    val onPrimary: Long,
    val primaryContainer: Long,
    val onPrimaryContainer: Long,
    val secondary: Long,
    val onSecondary: Long,
    val secondaryContainer: Long,
    val onSecondaryContainer: Long,
    val accent: Long,
    val onAccent: Long,
    val accentContainer: Long,
    val background: Long,
    val surface: Long,
    val surfaceElevated: Long,
    val card: Long,
    val textPrimary: Long,
    val textSecondary: Long,
    val textMuted: Long,
    val border: Long,
    val divider: Long,
    val success: Long,
    val successContainer: Long,
    val warning: Long,
    val warningContainer: Long,
    val error: Long,
    val errorContainer: Long,
    val info: Long,
    val infoContainer: Long,
    val disabled: Long,
    val onDisabled: Long,
    val focus: Long,
    val selection: Long,
    val progressTrack: Long,
    val chartSeries: List<Long>,
    val isDark: Boolean,
)

object PaletteTokens {
    val Light = PaletteSpec(
        primary = 0xFF0B6E6A, onPrimary = 0xFFFFFFFF,
        primaryContainer = 0xFFD3F1EE, onPrimaryContainer = 0xFF00302E,
        secondary = 0xFF4F4FC2, onSecondary = 0xFFFFFFFF,
        secondaryContainer = 0xFFE3E3FB, onSecondaryContainer = 0xFF15156B,
        accent = 0xFFB45309, onAccent = 0xFFFFFFFF, accentContainer = 0xFFFDEBD2,
        background = 0xFFF6F7F9, surface = 0xFFFFFFFF, surfaceElevated = 0xFFEEF2F4, card = 0xFFFFFFFF,
        textPrimary = 0xFF151A21, textSecondary = 0xFF424B57, textMuted = 0xFF5F6874,
        border = 0xFFD9DEE4, divider = 0xFFE7EAEE,
        success = 0xFF116A31, successContainer = 0xFFDDF5E5,
        warning = 0xFF9A4A00, warningContainer = 0xFFFFEFD6,
        error = 0xFFB42318, errorContainer = 0xFFFDE4E1,
        info = 0xFF1D4ED8, infoContainer = 0xFFE0E9FF,
        disabled = 0xFFE3E6EA, onDisabled = 0xFF6E7781,
        focus = 0xFF4F4FC2, selection = 0xFFD3F1EE, progressTrack = 0xFFE3E8EC,
        chartSeries = listOf(0xFF0B6E6A, 0xFF4F4FC2, 0xFFB45309, 0xFFC2185B, 0xFF2E7D32, 0xFF5D4037),
        isDark = false,
    )

    val Dark = PaletteSpec(
        primary = 0xFF5ED6CC, onPrimary = 0xFF00302E,
        primaryContainer = 0xFF0E4A47, onPrimaryContainer = 0xFFC9F3EF,
        secondary = 0xFFB2B2FF, onSecondary = 0xFF1C1C66,
        secondaryContainer = 0xFF34348A, onSecondaryContainer = 0xFFE3E3FB,
        accent = 0xFFFFB960, onAccent = 0xFF3D2200, accentContainer = 0xFF5A3510,
        background = 0xFF0E1316, surface = 0xFF151B1F, surfaceElevated = 0xFF1C2428, card = 0xFF1A2125,
        textPrimary = 0xFFE9EEF1, textSecondary = 0xFFBCC6CC, textMuted = 0xFF97A2A9,
        border = 0xFF334047, divider = 0xFF263136,
        success = 0xFF6EE7A0, successContainer = 0xFF123D25,
        warning = 0xFFFFC266, warningContainer = 0xFF4A3110,
        error = 0xFFFF9C92, errorContainer = 0xFF4F1A16,
        info = 0xFF93B4FF, infoContainer = 0xFF1B2D58,
        disabled = 0xFF283136, onDisabled = 0xFF8C979E,
        focus = 0xFFB2B2FF, selection = 0xFF0E4A47, progressTrack = 0xFF2A353B,
        chartSeries = listOf(0xFF5ED6CC, 0xFFB2B2FF, 0xFFFFB960, 0xFFF48FB1, 0xFF81C784, 0xFFBCAAA4),
        isDark = true,
    )

    /** High-contrast variant: stronger text and borders, used when the learner enables it. */
    fun highContrast(base: PaletteSpec) = if (base.isDark) base.copy(
        textSecondary = 0xFFE2E8EC, textMuted = 0xFFCAD3D8, border = 0xFF8A979E, background = 0xFF000000, surface = 0xFF0B0F11,
    ) else base.copy(
        textSecondary = 0xFF242B33, textMuted = 0xFF39424C, border = 0xFF6B7682, background = 0xFFFFFFFF,
    )
}

/** WCAG 2.1 relative luminance and contrast ratio. */
object Contrast {
    private fun channel(c: Long, shift: Int): Double {
        val v = ((c shr shift) and 0xFF) / 255.0
        return if (v <= 0.03928) v / 12.92 else ((v + 0.055) / 1.055).pow(2.4)
    }
    fun luminance(argb: Long) = 0.2126 * channel(argb, 16) + 0.7152 * channel(argb, 8) + 0.0722 * channel(argb, 0)
    fun ratio(a: Long, b: Long): Double {
        val l1 = luminance(a) + 0.05; val l2 = luminance(b) + 0.05
        return maxOf(l1, l2) / minOf(l1, l2)
    }
}
