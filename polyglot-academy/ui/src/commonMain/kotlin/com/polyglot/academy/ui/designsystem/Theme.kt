package com.polyglot.academy.ui.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.polyglot.academy.core.model.ThemeMode

private fun PolyglotColors.toMaterial(): ColorScheme {
    val base = if (isDark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = primary, onPrimary = onPrimary, primaryContainer = primaryContainer, onPrimaryContainer = onPrimaryContainer,
        secondary = secondary, onSecondary = onSecondary, secondaryContainer = secondaryContainer, onSecondaryContainer = onSecondaryContainer,
        tertiary = accent, onTertiary = onAccent, tertiaryContainer = accentContainer,
        background = background, onBackground = textPrimary, surface = surface, onSurface = textPrimary,
        surfaceVariant = surfaceElevated, onSurfaceVariant = textSecondary, surfaceContainer = surfaceElevated,
        surfaceContainerLow = card, surfaceContainerHigh = surfaceElevated, surfaceContainerHighest = surfaceElevated,
        surfaceContainerLowest = surface,
        outline = border, outlineVariant = divider, error = error, onError = if (isDark) errorContainer else surface, errorContainer = errorContainer,
        onErrorContainer = error,
    )
}

object PolyglotTheme {
    val colors: PolyglotColors @Composable @ReadOnlyComposable get() = LocalPolyglotColors.current
    val spacing: Spacing @Composable @ReadOnlyComposable get() = LocalSpacing.current
    val radii: Radii @Composable @ReadOnlyComposable get() = LocalRadii.current
    val learning: LearningTypography @Composable @ReadOnlyComposable get() = LocalLearningTypography.current
    val motion: MotionPrefs @Composable @ReadOnlyComposable get() = LocalMotion.current
}

@Composable
fun PolyglotTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    rtl: Boolean = false,
    highContrast: Boolean = false,
    reducedMotion: Boolean = false,
    textScale: Float = 1f,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val base = if (dark) PolyglotPalettes.Dark else PolyglotPalettes.Light
    val colors = if (highContrast) PolyglotPalettes.highContrast(base) else base
    val radii = Radii()
    CompositionLocalProvider(
        LocalPolyglotColors provides colors,
        LocalSpacing provides Spacing(),
        LocalRadii provides radii,
        LocalMotion provides MotionPrefs(reducedMotion),
        LocalLearningTypography provides learningTypography(textScale),
        LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr,
    ) {
        MaterialTheme(
            colorScheme = colors.toMaterial(),
            typography = polyglotTypography(textScale),
            shapes = Shapes(
                extraSmall = RoundedCornerShape(radii.sm / 2), small = RoundedCornerShape(radii.sm), medium = RoundedCornerShape(radii.md),
                large = RoundedCornerShape(radii.lg), extraLarge = RoundedCornerShape(radii.xl),
            ),
            content = content,
        )
    }
}
