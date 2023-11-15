package com.y9vad9.rsproto.codegen

import com.squareup.kotlinpoet.ClassName
import com.squareup.wire.schema.ProtoType
import com.squareup.wire.schema.Rpc

/**
 * Converts the given [ProtoType] to a [ClassName].
 *
 * @return The converted [ClassName].
 */
internal fun ProtoType.asClassName(): ClassName {
    val type = toString()

    val packageName = type.substringBeforeLast('.')
    val className = type.substringAfterLast('.')
    return ClassName(packageName, className)
}

/**
 * Determines if the RPC is a request-response type.
 *
 * @return `true` if the RPC is a request-response type, `false` otherwise.
 */
internal val Rpc.isRequestResponse get() = !requestStreaming && !responseStreaming

/**
 * Determines if the Rpc is a request stream.
 *
 * @return true if the Rpc is a request stream, false otherwise.
 */
internal val Rpc.isRequestStream get() = !requestStreaming && responseStreaming

/**
 * Determines if the RPC is a request channel.
 *
 * @return `true` if the RPC is a request channel, `false` otherwise.
 */
internal val Rpc.isRequestChannel get() = requestStreaming && responseStreaming