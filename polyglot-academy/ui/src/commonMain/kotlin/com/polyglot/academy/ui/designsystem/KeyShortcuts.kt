package com.polyglot.academy.ui.designsystem

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type

/**
 * Keyboard shortcuts for a practice screen (desktop, or a phone or tablet with a keyboard): Enter checks
 * or continues, and 1–9 pick a choice. The handlers return true when they used the key; otherwise it goes
 * on to the focused field as usual, so typing is never blocked. Focus returns to the screen whenever
 * [focusKey] changes (a new question, or an answer checked), so the keys work without clicking first.
 */
@Composable
fun KeyShortcuts(
    focusKey: Any?,
    onEnter: () -> Boolean,
    modifier: Modifier = Modifier,
    onNumber: (Int) -> Boolean = { false },
    content: @Composable () -> Unit,
) {
    val focus = remember { FocusRequester() }
    val enter by rememberUpdatedState(onEnter)
    val number by rememberUpdatedState(onNumber)
    Box(modifier.focusRequester(focus).focusable().onPreviewKeyEvent { e ->
        if (e.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
        when (e.key) {
            Key.Enter, Key.NumPadEnter -> enter()
            else -> KeyNumbers.of(e.key)?.let { number(it) } ?: false
        }
    }) { content() }
    LaunchedEffect(focusKey) { runCatching { focus.requestFocus() } }
}

object KeyNumbers {
    private val keys = mapOf(
        Key.One to 1, Key.Two to 2, Key.Three to 3, Key.Four to 4, Key.Five to 5, Key.Six to 6, Key.Seven to 7, Key.Eight to 8, Key.Nine to 9,
        Key.NumPad1 to 1, Key.NumPad2 to 2, Key.NumPad3 to 3, Key.NumPad4 to 4, Key.NumPad5 to 5,
        Key.NumPad6 to 6, Key.NumPad7 to 7, Key.NumPad8 to 8, Key.NumPad9 to 9,
    )

    fun of(key: Key): Int? = keys[key]
}
