package org.timemates.rsproto.codegen

internal fun String.capitalized(): String {
    return replaceFirstChar { it.uppercaseChar() }
}

internal fun String.decapitalize(): String {
    return replaceFirstChar { it.lowercaseChar() }
}