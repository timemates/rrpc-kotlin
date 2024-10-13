package org.timemates.rrpc.codegen.exception

class GenerationException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)