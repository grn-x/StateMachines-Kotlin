package machine_reflectable.desktopMain.kotlin

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import machine_reflectable.shared.AutomatonApp

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        AutomatonApp()
    }
}
