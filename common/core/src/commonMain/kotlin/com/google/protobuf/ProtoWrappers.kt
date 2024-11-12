package com.google.protobuf

import org.timemates.rrpc.ProtoType

/**
 * Wrapper for a 32-bit integer value based on the standard Protobuf `Int32Value` type.
 * For more information, see [Google's Int32Value documentation](https://protobuf.dev/reference/protobuf/google.protobuf/#int32-value).
 *
 * @property value The encapsulated integer value, defaulting to 0.
 */
public class Int32Wrapper(
    public val value: Int = 0,
) : ProtoType {
    public companion object Definition : ProtoType.Definition<Int32Wrapper> {
        override val url: String = "type.googleapis.com/google.protobuf.Int32Value"
        override val Default: Int32Wrapper = Int32Wrapper()
    }

    override val definition: ProtoType.Definition<*>
        get() = Definition
}

/**
 * Wrapper for a 64-bit integer value based on the standard Protobuf `Int64Value` type.
 * For more information, see [Google's Int64Value documentation](https://protobuf.dev/reference/protobuf/google.protobuf/#int64-value).
 *
 * @property value The encapsulated long integer value, defaulting to 0L.
 */
public class Int64Wrapper(
    public val value: Long = 0L,
) : ProtoType {
    public companion object Definition : ProtoType.Definition<Int64Wrapper> {
        override val url: String = "type.googleapis.com/google.protobuf.Int64Value"
        override val Default: Int64Wrapper = Int64Wrapper()
    }

    override val definition: ProtoType.Definition<*>
        get() = Definition
}

/**
 * Wrapper for a 32-bit floating-point value based on the standard Protobuf `FloatValue` type.
 * For more information, see [Google's FloatValue documentation](https://developers.google.com/protocol-buffers/docs/reference/google.protobuf#floatvalue).
 *
 * @property value The encapsulated float value, defaulting to 0.0F.
 */
public class FloatWrapper(
    public val value: Float = 0.0F,
) : ProtoType {
    public companion object Definition : ProtoType.Definition<FloatWrapper> {
        override val url: String = "type.googleapis.com/google.protobuf.FloatValue"
        override val Default: FloatWrapper = FloatWrapper()
    }

    override val definition: ProtoType.Definition<*>
        get() = Definition
}

/**
 * Wrapper for a 64-bit floating-point value based on the standard Protobuf `DoubleValue` type.
 * For more information, see [Google's DoubleValue documentation](https://developers.google.com/protocol-buffers/docs/reference/google.protobuf#doublevalue).
 *
 * @property value The encapsulated double value, defaulting to 0.0.
 */
public class DoubleWrapper(
    public val value: Double = 0.0,
) : ProtoType {
    public companion object Definition : ProtoType.Definition<DoubleWrapper> {
        override val url: String = "type.googleapis.com/google.protobuf.DoubleValue"
        override val Default: DoubleWrapper = DoubleWrapper()
    }

    override val definition: ProtoType.Definition<*>
        get() = Definition
}

/**
 * Wrapper for a boolean value based on the standard Protobuf `BoolValue` type.
 * For more information, see [Google's BoolValue documentation](https://developers.google.com/protocol-buffers/docs/reference/google.protobuf#boolvalue).
 *
 * @property value The encapsulated boolean value, defaulting to `false`.
 */
public class BoolWrapper(
    public val value: Boolean = false,
) : ProtoType {
    public companion object Definition : ProtoType.Definition<BoolWrapper> {
        override val url: String = "type.googleapis.com/google.protobuf.BoolValue"
        override val Default: BoolWrapper = BoolWrapper()
    }

    override val definition: ProtoType.Definition<*>
        get() = Definition
}

/**
 * Wrapper for a string value based on the standard Protobuf `StringValue` type.
 * For more information, see [Google's StringValue documentation](https://developers.google.com/protocol-buffers/docs/reference/google.protobuf#stringvalue).
 *
 * @property value The encapsulated string value, defaulting to an empty string.
 */
public class StringWrapper(
    public val value: String = "",
) : ProtoType {

    public companion object Definition : ProtoType.Definition<StringWrapper> {
        override val url: String = "type.googleapis.com/google.protobuf.StringValue"
        override val Default: StringWrapper = StringWrapper()
    }

    override val definition: ProtoType.Definition<*>
        get() = Definition
}

/**
 * Wrapper for a byte array value based on the standard Protobuf `BytesValue` type.
 * For more information, see [Google's BytesValue documentation](https://developers.google.com/protocol-buffers/docs/reference/google.protobuf#bytesvalue).
 *
 * @property value The encapsulated byte array value, defaulting to an empty byte array.
 */
public class BytesWrapper(
    public val value: ByteArray = ByteArray(0),
) : ProtoType {
    public companion object Definition : ProtoType.Definition<BytesWrapper> {
        override val url: String = "type.googleapis.com/google.protobuf.BytesValue"
        override val Default: BytesWrapper = BytesWrapper()
    }

    override val definition: ProtoType.Definition<*>
        get() = Definition
}