package com.y9vad9.rsocket.proto.codegen

import com.squareup.wire.schema.Rpc

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