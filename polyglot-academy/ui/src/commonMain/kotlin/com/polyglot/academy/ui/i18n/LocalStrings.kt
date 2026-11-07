package com.polyglot.academy.ui.i18n

import androidx.compose.runtime.staticCompositionLocalOf
import com.polyglot.academy.core.model.LanguageCode

val LocalStrings = staticCompositionLocalOf { Localizer(LanguageCode.ENGLISH) }
