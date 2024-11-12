package org.timemates.rrpc.app

import androidx.compose.ui.Alignment
import androidx.compose.ui.window.*
import org.timemates.rrpc.app.resource.strings.LocalStrings

fun main(): Unit = application {
    val windowState = rememberWindowState(
        placement = WindowPlacement.Floating,
        position = WindowPosition.Aligned(Alignment.Center),
    )

    Window(
        title = LocalStrings.current.appName,
        state = windowState,
        onCloseRequest = ::exitApplication,
    ) {

    }
}