package org.timemates.rsp.codegen

internal class GenerationException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)