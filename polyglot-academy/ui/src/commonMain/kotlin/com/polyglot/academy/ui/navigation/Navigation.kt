package com.polyglot.academy.ui.navigation

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.Saver

/** Simple back-stack navigator. Tabs reset the stack to their root; back pops. */
@Stable
class Navigator(initial: List<Route> = listOf(Route.TabRoot(Tab.HOME))) {
    val stack = mutableStateListOf<Route>().apply { addAll(initial.ifEmpty { listOf(Route.TabRoot(Tab.HOME)) }) }
    val current: Route get() = stack.last()
    val currentTab: Tab get() = stack.firstNotNullOfOrNull { (it as? Route.TabRoot)?.tab } ?: Tab.HOME
    val canGoBack: Boolean get() = stack.size > 1

    fun push(route: Route) { if (stack.lastOrNull() != route) stack.add(route) }
    fun replace(route: Route) { if (stack.size > 1) stack.removeAt(stack.lastIndex); push(route) }
    fun pop(): Boolean = if (stack.size > 1) { stack.removeAt(stack.lastIndex); true } else false
    fun popTo(predicate: (Route) -> Boolean) { while (stack.size > 1 && !predicate(stack.last())) stack.removeAt(stack.lastIndex) }
    fun switchTab(tab: Tab) { stack.clear(); stack.add(Route.TabRoot(tab)) }
    fun home() = switchTab(Tab.HOME)

    companion object {
        val saver: Saver<Navigator, List<String>> = Saver(
            save = { nav -> nav.stack.map(Route::encode) },
            restore = { saved -> Navigator(saved.mapNotNull(Route::decode)) },
        )
    }
}
