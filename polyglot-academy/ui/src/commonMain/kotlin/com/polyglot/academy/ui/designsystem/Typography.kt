package com.polyglot.academy.ui.designsystem

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/**
 * Type scale. The platform default family is used deliberately: on Android it falls back to Noto
 * Sans Arabic / Noto Sans CJK, which render Arabic shaping, Chinese characters and pinyin tone
 * marks correctly. Line heights are generous (≈1.45×) so Arabic diacritics and tone marks are
 * not clipped.
 */
private val family = FontFamily.Default
private val trimNone = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None)

private fun style(size: TextUnit, lineHeight: TextUnit, weight: FontWeight, letterSpacing: TextUnit = 0.sp) =
    TextStyle(fontFamily = family, fontSize = size, lineHeight = lineHeight, fontWeight = weight, letterSpacing = letterSpacing, lineHeightStyle = trimNone)

fun polyglotTypography(scale: Float = 1f): Typography {
    fun s(v: Float) = (v * scale).sp
    return Typography(
        displaySmall = style(s(32f), s(44f), FontWeight.SemiBold),
        headlineMedium = style(s(26f), s(36f), FontWeight.SemiBold),
        headlineSmall = style(s(23f), s(32f), FontWeight.SemiBold),
        titleLarge = style(s(21f), s(30f), FontWeight.SemiBold),
        titleMedium = style(s(17f), s(25f), FontWeight.SemiBold),
        titleSmall = style(s(15f), s(22f), FontWeight.Medium),
        bodyLarge = style(s(16f), s(24f), FontWeight.Normal),
        bodyMedium = style(s(14.5f), s(22f), FontWeight.Normal),
        bodySmall = style(s(13f), s(19f), FontWeight.Normal),
        labelLarge = style(s(15f), s(21f), FontWeight.SemiBold, 0.1.sp),
        labelMedium = style(s(13f), s(18f), FontWeight.Medium, 0.2.sp),
        labelSmall = style(s(12f), s(16f), FontWeight.Medium, 0.3.sp),
    )
}

/** Styles for target-language content (terms, characters, pinyin) and statistics. */
@Immutable
data class LearningTypography(
    val term: TextStyle,
    val hanzi: TextStyle,
    val pinyin: TextStyle,
    val sentence: TextStyle,
    val stat: TextStyle,
    val statSmall: TextStyle,
)

fun learningTypography(scale: Float = 1f) = LearningTypography(
    term = style((28 * scale).sp, (40 * scale).sp, FontWeight.SemiBold),
    hanzi = style((36 * scale).sp, (52 * scale).sp, FontWeight.Medium),
    pinyin = style((16 * scale).sp, (24 * scale).sp, FontWeight.Normal, 0.02.em),
    sentence = style((19 * scale).sp, (30 * scale).sp, FontWeight.Normal),
    stat = style((30 * scale).sp, (38 * scale).sp, FontWeight.Bold),
    statSmall = style((20 * scale).sp, (26 * scale).sp, FontWeight.Bold),
)

val LocalLearningTypography = staticCompositionLocalOf { learningTypography() }

/** Locale hint so Han characters use Chinese (not Japanese) glyph variants, and Arabic shapes correctly. */
fun localeFor(tag: String): LocaleList = LocaleList(tag)
