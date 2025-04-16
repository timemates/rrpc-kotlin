package app.timemate.rrpc.generator.kotlin.internal.ext

import kotlin.random.Random

internal fun String.capitalized(): String {
    return replaceFirstChar { it.uppercaseChar() }
}

internal fun String.decapitalized(): String {
    return replaceFirstChar { it.lowercaseChar() }
}

internal fun String.protoByteStringToByteArray(): ByteArray {
    val result = mutableListOf<Byte>()
    var i = 0

    while (i < length) {
        when {
            startsWith("\\x", i) && i + 3 < length -> {
                val hexValue = substring(i + 2, i + 4)
                val byteValue = hexValue.toInt(16).toByte()
                result.add(byteValue)
                i += 4
            }
            else -> {
                result.add(get(i).code.toByte())
                i++
            }
        }
    }
    return result.toByteArray()
}

internal fun Random.nextString(length: Int): String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')

    return String(CharArray(length) { allowedChars.random() })
}

internal fun String.toLowerCamelCase(): String = split("_")
    .mapIndexed { index, word ->
        if (index == 0) word.lowercase()
        else word.replaceFirstChar { it.uppercase() }
    }
    .joinToString("")

internal fun String.toUpperCamelCase(): String = split("_")
    .mapIndexed { index, word ->
        if (index == 0) word.lowercase().replaceFirstChar { it.uppercase() }
        else word.replaceFirstChar { it.uppercase() }
    }
    .joinToString("")