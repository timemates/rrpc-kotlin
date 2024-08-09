package org.timemates.rsp.codegen.ext

internal fun String.capitalized(): String {
    return replaceFirstChar { it.uppercaseChar() }
}

internal fun String.decapitalized(): String {
    return replaceFirstChar { it.lowercaseChar() }
}