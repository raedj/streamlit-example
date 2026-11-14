package com.polyglot.academy.ui.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Semantic colour tokens. Screens use these names, never raw hex values, so light/dark themes and
 * high-contrast mode stay consistent. Contrast pairs are verified in ContrastTest.
 */
@Immutable
data class PolyglotColors(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val accent: Color,
    val onAccent: Color,
    val accentContainer: Color,
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val card: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val border: Color,
    val divider: Color,
    val success: Color,
    val successContainer: Color,
    val warning: Color,
    val warningContainer: Color,
    val error: Color,
    val errorContainer: Color,
    val info: Color,
    val infoContainer: Color,
    val disabled: Color,
    val onDisabled: Color,
    val focus: Color,
    val selection: Color,
    val progressTrack: Color,
    val chartSeries: List<Color>,
    val isDark: Boolean,
)

object PolyglotPalettes {
    private fun c(v: Long) = Color(v)
    fun fromSpec(p: PaletteSpec) = PolyglotColors(
        primary = c(p.primary),
        onPrimary = c(p.onPrimary),
        primaryContainer = c(p.primaryContainer),
        onPrimaryContainer = c(p.onPrimaryContainer),
        secondary = c(p.secondary),
        onSecondary = c(p.onSecondary),
        secondaryContainer = c(p.secondaryContainer),
        onSecondaryContainer = c(p.onSecondaryContainer),
        accent = c(p.accent),
        onAccent = c(p.onAccent),
        accentContainer = c(p.accentContainer),
        background = c(p.background),
        surface = c(p.surface),
        surfaceElevated = c(p.surfaceElevated),
        card = c(p.card),
        textPrimary = c(p.textPrimary),
        textSecondary = c(p.textSecondary),
        textMuted = c(p.textMuted),
        border = c(p.border),
        divider = c(p.divider),
        success = c(p.success),
        successContainer = c(p.successContainer),
        warning = c(p.warning),
        warningContainer = c(p.warningContainer),
        error = c(p.error),
        errorContainer = c(p.errorContainer),
        info = c(p.info),
        infoContainer = c(p.infoContainer),
        disabled = c(p.disabled),
        onDisabled = c(p.onDisabled),
        focus = c(p.focus),
        selection = c(p.selection),
        progressTrack = c(p.progressTrack),
        chartSeries = p.chartSeries.map(::c),
        isDark = p.isDark,
    )

    val Light = fromSpec(PaletteTokens.Light)
    val Dark = fromSpec(PaletteTokens.Dark)

    /** High-contrast variant: stronger text and borders, used when the learner enables it. */
    fun highContrast(base: PolyglotColors) = fromSpec(PaletteTokens.highContrast(if (base.isDark) PaletteTokens.Dark else PaletteTokens.Light))
}

@Immutable
data class Spacing(
    val xxs: Dp = 2.dp, val xs: Dp = 4.dp, val sm: Dp = 8.dp, val md: Dp = 12.dp, val lg: Dp = 16.dp,
    val xl: Dp = 24.dp, val xxl: Dp = 32.dp, val xxxl: Dp = 48.dp,
    /** Horizontal screen gutter. */
    val gutter: Dp = 16.dp,
    /** Minimum touch target (Material/Android accessibility guidance). */
    val touchTarget: Dp = 48.dp,
    /** Max content width on tablets/large windows so lines stay readable. */
    val maxContentWidth: Dp = 720.dp,
)

@Immutable
data class Radii(val sm: Dp = 8.dp, val md: Dp = 12.dp, val lg: Dp = 16.dp, val xl: Dp = 24.dp, val pill: Dp = 999.dp)

@Immutable
data class Elevations(val none: Dp = 0.dp, val low: Dp = 1.dp, val card: Dp = 2.dp, val raised: Dp = 6.dp)

/** Motion preferences resolved from settings and the device. */
@Immutable
data class MotionPrefs(val reduced: Boolean) {
    fun duration(ms: Int) = if (reduced) 0 else ms
}

val LocalPolyglotColors = staticCompositionLocalOf { PolyglotPalettes.Light }
val LocalSpacing = staticCompositionLocalOf { Spacing() }
val LocalRadii = staticCompositionLocalOf { Radii() }
val LocalElevations = staticCompositionLocalOf { Elevations() }
val LocalMotion = staticCompositionLocalOf { MotionPrefs(reduced = false) }
