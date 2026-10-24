package com.polyglot.academy.ui.app

import androidx.compose.runtime.Composable

/** The desktop harness has no system back gesture; the top-bar back button is used instead. */
@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {}
