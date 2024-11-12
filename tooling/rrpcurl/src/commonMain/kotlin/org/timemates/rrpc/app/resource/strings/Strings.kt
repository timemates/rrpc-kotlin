package org.timemates.rrpc.app.resource.strings

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf

interface Strings {
    val appName: String get() = "rRPC Inspector"
}

val LocalStrings: ProvidableCompositionLocal<Strings> = compositionLocalOf {
    EnglishStrings
}