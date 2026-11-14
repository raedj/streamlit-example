package com.polyglot.academy.ui

import com.polyglot.academy.ui.designsystem.Contrast
import com.polyglot.academy.ui.designsystem.PaletteSpec
import com.polyglot.academy.ui.designsystem.PaletteTokens
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** WCAG 2.1 AA contrast checks for the semantic colour pairs used for text and icons. */
class ContrastTest {
    private fun check(p: PaletteSpec, name: String) {
        val text = listOf("textPrimary" to p.textPrimary, "textSecondary" to p.textSecondary, "textMuted" to p.textMuted)
        val surfaces = listOf("background" to p.background, "card" to p.card, "surface" to p.surface, "surfaceElevated" to p.surfaceElevated)
        for ((tn, t) in text) for ((sn, s) in surfaces) {
            val r = Contrast.ratio(t, s)
            assertTrue(r >= 4.5, "$name $tn on $sn = $r (needs 4.5)")
        }
        val pairs = listOf(
            "onPrimary/primary" to (p.onPrimary to p.primary), "onSecondary/secondary" to (p.onSecondary to p.secondary),
            "onAccent/accent" to (p.onAccent to p.accent), "onPrimaryContainer/primaryContainer" to (p.onPrimaryContainer to p.primaryContainer),
            "onSecondaryContainer/secondaryContainer" to (p.onSecondaryContainer to p.secondaryContainer),
            "primary/card" to (p.primary to p.card), "primary/background" to (p.primary to p.background),
            "error/errorContainer" to (p.error to p.errorContainer), "success/successContainer" to (p.success to p.successContainer),
            "warning/warningContainer" to (p.warning to p.warningContainer), "info/infoContainer" to (p.info to p.infoContainer),
            "textPrimary/errorContainer" to (p.textPrimary to p.errorContainer), "textPrimary/successContainer" to (p.textPrimary to p.successContainer),
            "textPrimary/primaryContainer" to (p.textPrimary to p.primaryContainer), "error/card" to (p.error to p.card), "success/card" to (p.success to p.card),
            "onDisabled/disabled" to (p.onDisabled to p.disabled),
        )
        for ((n, pr) in pairs) {
            val r = Contrast.ratio(pr.first, pr.second)
            val min = if (n == "onDisabled/disabled") 3.0 else 4.5
            assertTrue(r >= min, "$name $n = $r (needs $min)")
        }
    }

    @Test fun formulaMatchesKnownValues() {
        assertEquals(21.0, Contrast.ratio(0xFF000000, 0xFFFFFFFF), 0.01)
        assertEquals(1.0, Contrast.ratio(0xFF777777, 0xFF777777), 0.001)
    }
    @Test fun lightPaletteMeetsWcagAA() = check(PaletteTokens.Light, "light")
    @Test fun darkPaletteMeetsWcagAA() = check(PaletteTokens.Dark, "dark")
    @Test fun highContrastPalettes() {
        check(PaletteTokens.highContrast(PaletteTokens.Light), "hc-light"); check(PaletteTokens.highContrast(PaletteTokens.Dark), "hc-dark")
        assertTrue(Contrast.ratio(PaletteTokens.highContrast(PaletteTokens.Light).textMuted, 0xFFFFFFFF) > Contrast.ratio(PaletteTokens.Light.textMuted, 0xFFFFFFFF))
    }
}
