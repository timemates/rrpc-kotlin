package org.timemates.rrpc.codegen

import com.squareup.wire.schema.*
import org.timemates.rrpc.codegen.exception.GenerationException
import org.timemates.rrpc.common.metadata.*
import org.timemates.rrpc.common.metadata.value.RMPackageName
import org.timemates.rrpc.common.metadata.value.RMTypeUrl

internal fun ProtoType.asRMTypeUrl(): RMTypeUrl {
    return when {
        isMap -> RMTypeUrl("map<${keyType!!.asRMTypeUrl()}, ${valueType!!.asRMTypeUrl()}>")
        isScalar -> when (this) {
            ProtoType.BOOL -> RMTypeUrl.BOOL
            ProtoType.BYTES -> RMTypeUrl.BYTES
            ProtoType.DOUBLE -> RMTypeUrl.DOUBLE
            ProtoType.FLOAT -> RMTypeUrl.FLOAT
            ProtoType.FIXED32 -> RMTypeUrl.FIXED32
            ProtoType.FIXED64 -> RMTypeUrl.FIXED64
            ProtoType.INT32 -> RMTypeUrl.INT32
            ProtoType.INT64 -> RMTypeUrl.INT64
            ProtoType.SFIXED32 -> RMTypeUrl.SFIXED32
            ProtoType.SFIXED64 -> RMTypeUrl.SFIXED64
            ProtoType.SINT32 -> RMTypeUrl.SINT32
            ProtoType.SINT64 -> RMTypeUrl.SINT64
            ProtoType.STRING -> RMTypeUrl.STRING
            ProtoType.UINT32 -> RMTypeUrl.UINT32
            ProtoType.UINT64 -> RMTypeUrl.UINT64
            ProtoType.ANY -> RMTypeUrl.ANY
            ProtoType.DURATION -> RMTypeUrl.DURATION
            ProtoType.TIMESTAMP -> RMTypeUrl.TIMESTAMP
            ProtoType.EMPTY -> RMTypeUrl.EMPTY
            ProtoType.STRUCT_MAP -> RMTypeUrl.STRUCT_MAP
            ProtoType.STRUCT_LIST -> RMTypeUrl.STRUCT_LIST
            ProtoType.STRUCT_VALUE -> RMTypeUrl.STRUCT_VALUE
            ProtoType.STRUCT_NULL -> RMTypeUrl.STRUCT_NULL
            ProtoType.DOUBLE_VALUE -> RMTypeUrl.DOUBLE_VALUE
            ProtoType.FLOAT_VALUE -> RMTypeUrl.FLOAT_VALUE
            ProtoType.INT32_VALUE -> RMTypeUrl.INT32_VALUE
            ProtoType.INT64_VALUE -> RMTypeUrl.INT64_VALUE
            ProtoType.UINT32_VALUE -> RMTypeUrl.UINT32_VALUE
            ProtoType.UINT64_VALUE -> RMTypeUrl.UINT64_VALUE
            ProtoType.BOOL_VALUE -> RMTypeUrl.BOOL_VALUE
            ProtoType.BYTES_VALUE -> RMTypeUrl.BYTES_VALUE
            else -> throw GenerationException("Unable to convert scalar type '$this'.")
        }

        else -> RMTypeUrl(typeUrl!!)
    }
}

internal fun Field.asRMField(): RMField {
    return RMField(
        tag = tag,
        name = name,
        options = options.asRMOptions(),
        documentation = documentation,
        typeUrl = type!!.asRMTypeUrl(),
        isRepeated = isRepeated,
        isInOneOf = isOneOf,
        isExtension = isExtension,
    )
}

internal fun OneOf.asRMOneOf(): RMOneOf {
    return RMOneOf(
        name = name,
        fields = fields.map { it.asRMField() },
        documentation = documentation,
        options = options.asRMOptions(),
    )
}

internal fun EnumConstant.asRMConstant(): RMEnumConstant {
    return RMEnumConstant(
        name = name,
        tag = tag,
        options = options.asRMOptions(),
        documentation = documentation,
    )
}

internal fun ProtoMember.asRMOption(value: Any?): RMOption {
    return RMOption(
        name = simpleName,
        fieldUrl = RMTypeMemberUrl(type.asRMTypeUrl(), member),
        value = value?.asRMOptionValue(),
    )
}

private fun Any.asRMOptionValue(): RMOption.Value {
    return if (this is Map<*, *>) {
        val firstKey = keys.firstOrNull()

        if (firstKey is ProtoMember) {
            @Suppress("UNCHECKED_CAST")
            RMOption.Value.MessageMap((this as Map<ProtoMember, Any>).map { (key, mapValue) ->
                RMTypeMemberUrl(key.type.asRMTypeUrl(), key.member) to mapValue.asRMOptionValue()
            }.associate { it })
        } else {
            RMOption.Value.RawMap(
                map { (key, value) ->
                    RMOption.Value.Raw(key.toString()) to RMOption.Value.Raw(value.toString()) }
                    .associate { it }
            )
        }
    } else {
        RMOption.Value.Raw(toString())
    }
}

internal fun Options.asRMOptions(): RMOptions {
    return RMOptions(map.map { (key, value) -> key.asRMOption(value) })
}

internal fun Type.asRMType(): RMType {
    return when (this) {
        is EnclosingType -> RMType.Enclosing(
            name = name,
            documentation = documentation,
            typeUrl = type.asRMTypeUrl(),
            nestedTypes = nestedTypes.map { it.asRMType() },
            nestedExtends = nestedExtendList.map { it.asRMExtend() }
        )

        is EnumType -> RMType.Enum(
            name = name,
            constants = constants.map { it.asRMConstant() },
            documentation = documentation,
            options = options.asRMOptions(),
            nestedTypes = nestedTypes.map { it.asRMType() },
            typeUrl = type.asRMTypeUrl(),
            nestedExtends = nestedExtendList.map { it.asRMExtend() },
        )

        is MessageType -> RMType.Message(
            name = name,
            documentation = documentation,
            fields = fields.map { it.asRMField() },
            oneOfs = oneOfs.map { it.asRMOneOf() },
            options = options.asRMOptions(),
            nestedTypes = nestedTypes.map { it.asRMType() },
            nestedExtends = nestedExtendList.map { it.asRMExtend() },
            typeUrl = type.asRMTypeUrl(),
        )
    }
}

internal fun Extend.asRMExtend(): RMExtend {
    return RMExtend(
        typeUrl = type!!.asRMTypeUrl(),
        name = name,
        fields = fields.map { it.asRMField() },
        documentation = documentation,
    )
}

internal fun Rpc.asRMRpc(): RMRpc {
    return RMRpc(
        name = name,
        requestType = StreamableRMTypeUrl(requestStreaming, requestType!!.asRMTypeUrl()),
        responseType = StreamableRMTypeUrl(responseStreaming, responseType!!.asRMTypeUrl()),
        options = options.asRMOptions(),
        documentation = documentation,
    )
}

internal fun Service.asRMService(): RMService {
    return RMService(
        name = name,
        rpcs = rpcs.map { it.asRMRpc() },
        options = options.asRMOptions(),
        typeUrl = type.asRMTypeUrl(),
    )
}

internal fun ProtoFile.asRMFile(): RMFile {
    return RMFile(
        name = name(),
        packageName = RMPackageName(packageName!!),
        options = options.asRMOptions(),
        services = services.map { it.asRMService() },
        types = types.map { it.asRMType() },
        extends = extendList.map { it.asRMExtend() },
    )
}