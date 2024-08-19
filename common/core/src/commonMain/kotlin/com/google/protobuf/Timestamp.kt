@file:OptIn(ExperimentalSerializationApi::class)

package com.google.protobuf

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import org.timemates.rsp.annotations.ExperimentalRSProtoAPI
import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds

public fun Timestamp(builder: Timestamp.Builder.() -> Unit): Timestamp =
    Timestamp.create(builder)

/**
 * Corresponding type from ProtoBuf definition.
 * Refer to the [official documentation](https://protobuf.dev/reference/protobuf/google.protobuf/#timestamp) regarding usage of Timestamp.
 */
@Serializable
public class Timestamp private constructor(
    @ProtoNumber(1)
    public val seconds: Long,
    @ProtoNumber(2)
    public val nanos: Int,
) {
    public companion object {
        public fun ofSeconds(seconds: Long): Timestamp {
            return Timestamp(seconds, 0)
        }

        public fun create(builder: Builder.() -> Unit): Timestamp {
            return Builder().apply(builder).build()
        }
    }

    public class Builder {
        public var seconds: Long = 0
        public var nanos: Int = 0

        public fun build(): Timestamp {
            return Timestamp(seconds, nanos)
        }
    }
}

@ExperimentalRSProtoAPI
public fun Timestamp.toDuration(): Duration {
    return seconds.seconds + nanos.nanoseconds
}