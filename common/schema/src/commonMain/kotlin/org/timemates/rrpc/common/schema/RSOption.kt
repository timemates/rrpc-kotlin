package org.timemates.rrpc.common.schema

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kotlinx.serialization.protobuf.ProtoOneOf
import kotlin.jvm.JvmInline


@Serializable
public class RSOption(
    @ProtoNumber(1)
    public val name: String,
    @ProtoNumber(2)
    public val fieldUrl: RSTypeMemberUrl,
    /**
     * Value is present only if RPSOption is present in position where a specific value is
     * possible, it can also be default value if supported.
     */
    @ProtoOneOf
    public val value: Value?,
) {
    public companion object {
        public val DEPRECATED: RSTypeMemberUrl = RSTypeMemberUrl(RSOptions.METHOD_OPTIONS, "deprecated")
    }

    @Serializable
    public sealed interface Value {
        @JvmInline
        public value class Raw(@ProtoNumber(3) public val string: String) : Value
        @JvmInline
        public value class RawMap(@ProtoNumber(4) public val map: Map<Raw, Raw>) : Value
        @JvmInline
        public value class MessageMap(@ProtoNumber(5) public val map: Map<RSTypeMemberUrl, Value>) : Value
    }
}