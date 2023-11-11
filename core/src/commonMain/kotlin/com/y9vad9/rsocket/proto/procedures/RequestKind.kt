package com.y9vad9.rsocket.proto.procedures

public enum class RequestKind {
    /**
     * Default kind of any generated method if method is not involved in data
     * streaming.
     */
    REQUEST_RESPONSE,

    /**
     * This kind of request denotes that it's [REQUEST_RESPONSE], but has
     * `google.protobuf.Empty` return type.
     */
    FIRE_AND_FORGET,

    /**
     * This kind of request denotes that request has server-side streaming.
     */
    REQUEST_STREAM,

    /**
     * This kind of request denotes that request has bidirectional streaming.
     */
    REQUEST_CHANNEL,
}