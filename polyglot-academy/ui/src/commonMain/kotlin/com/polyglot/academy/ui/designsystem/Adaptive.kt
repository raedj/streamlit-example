package com.polyglot.academy.ui.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * How wide the window is, decided once at the root.
 * - COMPACT: phones held upright. Bottom navigation bar, one column.
 * - MEDIUM: small tablets, phones on their side, narrow desktop windows. Side navigation rail, one column.
 * - EXPANDED: tablets and desktop windows. Side navigation rail, and two panes on the screens that use them.
 */
enum class WidthClass {
    COMPACT, MEDIUM, EXPANDED;

    val hasRail: Boolean get() = this != COMPACT
    val twoPanes: Boolean get() = this == EXPANDED

    companion object {
        /** Two panes need room for two readable columns of about 420 dp each beside the rail. */
        fun of(width: Dp): WidthClass = when {
            width < 600.dp -> COMPACT
            width < 1000.dp -> MEDIUM
            else -> EXPANDED
        }
    }
}

val LocalWidthClass = staticCompositionLocalOf { WidthClass.COMPACT }

/** The widest a two-pane screen grows, so each pane keeps a readable line length. */
val MaxTwoPaneWidth: Dp = 1200.dp

/**
 * Two columns side by side on wide windows, one after the other otherwise. On a phone this
 * adds nothing: [first] and [second] are laid out in the surrounding column as before.
 */
@Composable
fun ColumnScope.TwoPane(
    first: @Composable ColumnScope.() -> Unit,
    second: @Composable ColumnScope.() -> Unit,
) {
    val sp = PolyglotTheme.spacing
    if (LocalWidthClass.current.twoPanes) {
        Row(Modifier.fillMaxWidth().testTag("two-pane"), horizontalArrangement = Arrangement.spacedBy(sp.xl)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(sp.lg), content = first)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(sp.lg), content = second)
        }
    } else {
        first()
        second()
    }
}
