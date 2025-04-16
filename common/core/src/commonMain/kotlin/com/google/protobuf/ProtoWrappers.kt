package com.google.protobuf

import kotlinx.serialization.Serializable
import app.timemate.rrpc.RSProtoType

/**
 * Wrapper for a 32-bit integer value based on the standard Protobuf `Int32Value` type.
 * For more information, see [Google's Int32Value documentation](https://protobuf.dev/reference/protobuf/google.protobuf/#int32-value).
 *
 * @property value The encapsulated integer value, defaulting to 0.
 */
@Serializable
public class ProtoInt32Wrapper(
    public val value: Int = 0,
) : RSProtoType {
    public companion object Definition : RSProtoType.Definition<ProtoInt32Wrapper> {
        override val url: String = "type.googleapis.com/google.protobuf.Int32Value"
        override val Default: ProtoInt32Wrapper = ProtoInt32Wrapper()
    }

    override val definition: RSProtoType.Definition<*>
        get() = Definition
}

/**
 * Wrapper for a 64-bit integer value based on the standard Protobuf `Int64Value` type.
 * For more information, see [Google's Int64Value documentation](https://protobuf.dev/reference/protobuf/google.protobuf/#int64-value).
 *
 * @property value The encapsulated long integer value, defaulting to 0L.
 */
@Serializable
public class ProtoInt64Wrapper(
    public val value: Long = 0L,
) : RSProtoType {
    public companion object Definition : RSProtoType.Definition<ProtoInt64Wrapper> {
        override val url: String = "type.googleapis.com/google.protobuf.Int64Value"
        override val Default: ProtoInt64Wrapper = ProtoInt64Wrapper()
    }

    override val definition: RSProtoType.Definition<*>
        get() = Definition
}

/**
 * Wrapper for a 32-bit floating-point value based on the standard Protobuf `FloatValue` type.
 * For more information, see [Google's FloatValue documentation](https://developers.google.com/protocol-buffers/docs/reference/google.protobuf#floatvalue).
 *
 * @property value The encapsulated float value, defaulting to 0.0F.
 */
@Serializable
public class ProtoFloatWrapper(
    public val value: Float = 0.0F,
) : RSProtoType {
    public companion object Definition : RSProtoType.Definition<ProtoFloatWrapper> {
        override val url: String = "type.googleapis.com/google.protobuf.FloatValue"
        override val Default: ProtoFloatWrapper = ProtoFloatWrapper()
    }

    override val definition: RSProtoType.Definition<*>
        get() = Definition
}

/**
 * Wrapper for a 64-bit floating-point value based on the standard Protobuf `DoubleValue` type.
 * For more information, see [Google's DoubleValue documentation](https://developers.google.com/protocol-buffers/docs/reference/google.protobuf#doublevalue).
 *
 * @property value The encapsulated double value, defaulting to 0.0.
 */
@Serializable
public class ProtoDoubleWrapper(
    public val value: Double = 0.0,
) : RSProtoType {
    public companion object Definition : RSProtoType.Definition<ProtoDoubleWrapper> {
        override val url: String = "type.googleapis.com/google.protobuf.DoubleValue"
        override val Default: ProtoDoubleWrapper = ProtoDoubleWrapper()
    }

    override val definition: RSProtoType.Definition<*>
        get() = Definition
}

/**
 * Wrapper for a boolean value based on the standard Protobuf `BoolValue` type.
 * For more information, see [Google's BoolValue documentation](https://developers.google.com/protocol-buffers/docs/reference/google.protobuf#boolvalue).
 *
 * @property value The encapsulated boolean value, defaulting to `false`.
 */
@Serializable
public class ProtoBoolWrapper(
    public val value: Boolean = false,
) : RSProtoType {
    public companion object Definition : RSProtoType.Definition<ProtoBoolWrapper> {
        override val url: String = "type.googleapis.com/google.protobuf.BoolValue"
        override val Default: ProtoBoolWrapper = ProtoBoolWrapper()
    }

    override val definition: RSProtoType.Definition<*>
        get() = Definition
}

/**
 * Wrapper for a string value based on the standard Protobuf `StringValue` type.
 * For more information, see [Google's StringValue documentation](https://developers.google.com/protocol-buffers/docs/reference/google.protobuf#stringvalue).
 *
 * @property value The encapsulated string value, defaulting to an empty string.
 */
@Serializable
public class ProtoStringWrapper(
    public val value: String = "",
) : RSProtoType {

    public companion object Definition : RSProtoType.Definition<ProtoStringWrapper> {
        override val url: String = "type.googleapis.com/google.protobuf.StringValue"
        override val Default: ProtoStringWrapper = ProtoStringWrapper()
    }

    override val definition: RSProtoType.Definition<*>
        get() = Definition
}

/**
 * Wrapper for a byte array value based on the standard Protobuf `BytesValue` type.
 * For more information, see [Google's BytesValue documentation](https://developers.google.com/protocol-buffers/docs/reference/google.protobuf#bytesvalue).
 *
 * @property value The encapsulated byte array value, defaulting to an empty byte array.
 */
@Serializable
public class ProtoBytesWrapper(
    public val value: ByteArray = ByteArray(0),
) : RSProtoType {
    public companion object Definition : RSProtoType.Definition<ProtoBytesWrapper> {
        override val url: String = "type.googleapis.com/google.protobuf.BytesValue"
        override val Default: ProtoBytesWrapper = ProtoBytesWrapper()
    }

    override val definition: RSProtoType.Definition<*>
        get() = Definition
}

/**
 * Wrapper for a 32-bit unsigned integer value based on the standard Protobuf `UInt32Value` type.
 * For more information, see [Google's UInt32Value documentation](https://protobuf.dev/reference/protobuf/google.protobuf/#uint32-value).
 *
 * @property value The encapsulated unsigned integer value, defaulting to 0.
 */
@Serializable
public class ProtoUInt32Wrapper(
    public val value: UInt = 0u,
) : RSProtoType {
    public companion object Definition : RSProtoType.Definition<ProtoUInt32Wrapper> {
        override val url: String = "type.googleapis.com/google.protobuf.UInt32Value"
        override val Default: ProtoUInt32Wrapper = ProtoUInt32Wrapper()
    }

    override val definition: RSProtoType.Definition<*>
        get() = Definition
}

/**
 * Wrapper for a 64-bit unsigned integer value based on the standard Protobuf `UInt64Value` type.
 * For more information, see [Google's UInt64Value documentation](https://protobuf.dev/reference/protobuf/google.protobuf/#uint64-value).
 *
 * @property value The encapsulated unsigned long integer value, defaulting to 0u.
 */
@Serializable
public class ProtoUInt64Wrapper(
    public val value: ULong = 0uL,
) : RSProtoType {
    public companion object Definition : RSProtoType.Definition<ProtoUInt64Wrapper> {
        override val url: String = "type.googleapis.com/google.protobuf.UInt64Value"
        override val Default: ProtoUInt64Wrapper = ProtoUInt64Wrapper()
    }

    override val definition: RSProtoType.Definition<*>
        get() = Definition
}
