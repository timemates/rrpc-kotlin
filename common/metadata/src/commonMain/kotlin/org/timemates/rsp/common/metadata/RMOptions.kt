package org.timemates.rrpc.common.metadata

import kotlinx.serialization.Serializable
import org.timemates.rrpc.common.metadata.value.RMTypeUrl
import kotlin.jvm.JvmInline


@Serializable
@JvmInline
public value class RMOptions(
    public val list: List<RMOption>
) {
    public companion object {
        public val EMPTY: RMOptions = RMOptions(emptyList())

        public val FILE_OPTIONS: RMTypeUrl = RMTypeUrl("google.protobuf.FileOptions")
        public val MESSAGE_OPTIONS: RMTypeUrl = RMTypeUrl("google.protobuf.MessageOptions")
        public val SERVICE_OPTIONS: RMTypeUrl = RMTypeUrl("google.protobuf.ServiceOptions")
        public val FIELD_OPTIONS: RMTypeUrl = RMTypeUrl("google.protobuf.FieldOptions")
        public val ONEOF_OPTIONS: RMTypeUrl = RMTypeUrl("google.protobuf.OneofOptions")
        public val ENUM_OPTIONS: RMTypeUrl = RMTypeUrl("google.protobuf.EnumOptions")
        public val ENUM_VALUE_OPTIONS: RMTypeUrl = RMTypeUrl("google.protobuf.EnumValueOptions")
        public val METHOD_OPTIONS: RMTypeUrl = RMTypeUrl("google.protobuf.MethodOptions")
        public val EXTENSION_RANGE_OPTIONS: RMTypeUrl = RMTypeUrl("google.protobuf.ExtensionRangeOptions")
    }

    public operator fun get(fieldUrl: RMTypeMemberUrl): RMOption? {
        return list.firstOrNull { it.fieldUrl == fieldUrl }
    }

    public operator fun contains(fieldUrl: RMTypeMemberUrl): Boolean {
        return get(fieldUrl) != null
    }
}

public val RMOptions.isDeprecated: Boolean get() = TODO()