package org.timemates.rsp.codegen.exception

internal class GenerationException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)