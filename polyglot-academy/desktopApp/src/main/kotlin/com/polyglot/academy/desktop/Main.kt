package com.polyglot.academy.desktop
import androidx.compose.ui.window.application
import androidx.compose.ui.window.Window
import com.polyglot.academy.ui.ProbeUi
fun main() = application { Window(onCloseRequest = ::exitApplication) { ProbeUi() } }
