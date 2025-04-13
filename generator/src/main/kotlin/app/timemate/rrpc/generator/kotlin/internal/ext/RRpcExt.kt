package app.timemate.rrpc.generator.kotlin.internal.ext

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import app.timemate.rrpc.proto.schema.Language
import app.timemate.rrpc.proto.schema.RSField
import app.timemate.rrpc.generator.plugin.api.RSResolver
import app.timemate.rrpc.proto.schema.StreamableRSTypeUrl
import app.timemate.rrpc.proto.schema.value.RSDeclarationUrl
import app.timemate.rrpc.generator.plugin.api.result.ProcessResult
import app.timemate.rrpc.generator.plugin.api.result.andAccumulate
import app.timemate.rrpc.generator.kotlin.internal.LibClassNames
import app.timemate.rrpc.generator.kotlin.error.UnresolvableDeclarationError
import app.timemate.rrpc.generator.plugin.api.result.map

internal fun RSDeclarationUrl.asTypeName(resolver: RSResolver): ProcessResult<TypeName> {
    if (isMap) {
        return firstTypeArgument!!.asTypeName(resolver)
            .andAccumulate { secondTypeArgument!!.asTypeName(resolver) }
            .map { (keyType, valueType) ->
                MAP.parameterizedBy(keyType, valueType)
            }
    }

    val result = when (this) {
        RSDeclarationUrl.ANY -> ClassName("com.google.protobuf", "ProtoAny")
        RSDeclarationUrl.TIMESTAMP -> ClassName("com.google.protobuf", "ProtoTimestamp")
        RSDeclarationUrl.DURATION -> ClassName("com.google.protobuf", "ProtoDuration")
        RSDeclarationUrl.STRUCT_MAP -> ClassName("com.google.protobuf", "ProtoStruct")
        RSDeclarationUrl.EMPTY -> ClassName("com.google.protobuf", "ProtoEmpty")
        RSDeclarationUrl.STRING_VALUE -> LibClassNames.Wrappers.STRING_VALUE
        RSDeclarationUrl.INT32_VALUE -> LibClassNames.Wrappers.INT32_VALUE
        RSDeclarationUrl.INT64_VALUE -> LibClassNames.Wrappers.INT64_VALUE
        RSDeclarationUrl.FLOAT_VALUE -> LibClassNames.Wrappers.FLOAT_VALUE
        RSDeclarationUrl.DOUBLE_VALUE -> LibClassNames.Wrappers.DOUBLE_VALUE
        RSDeclarationUrl.UINT32_VALUE -> LibClassNames.Wrappers.UINT32_VALUE
        RSDeclarationUrl.UINT64_VALUE -> LibClassNames.Wrappers.UINT64_VALUE
        RSDeclarationUrl.BOOL_VALUE -> LibClassNames.Wrappers.BOOL_VALUE
        RSDeclarationUrl.BYTES_VALUE -> LibClassNames.Wrappers.BYTES_VALUE
        RSDeclarationUrl.STRING -> STRING
        RSDeclarationUrl.BOOL -> BOOLEAN
        RSDeclarationUrl.INT32, RSDeclarationUrl.SINT32, RSDeclarationUrl.FIXED32, RSDeclarationUrl.SFIXED32 -> INT
        RSDeclarationUrl.INT64, RSDeclarationUrl.SINT64, RSDeclarationUrl.FIXED64, RSDeclarationUrl.SFIXED64 -> LONG
        RSDeclarationUrl.BYTES -> BYTE_ARRAY
        RSDeclarationUrl.FLOAT -> FLOAT
        RSDeclarationUrl.UINT32 -> U_INT
        RSDeclarationUrl.UINT64 -> U_LONG
        RSDeclarationUrl.DOUBLE -> DOUBLE
        else -> {
            val file = resolver.resolveFileOf(this)
                ?: return ProcessResult.Failure(UnresolvableDeclarationError(this))

            val packageName = file.platformPackageName(Language.KOTLIN)?.value
            val enclosingName: String = (enclosingTypeOrPackage?.replace(file.packageName?.value.orEmpty(), "") ?: "")
                .replace("..", ".")

            ClassName(packageName ?: "", enclosingName.split(".").filterNot { it.isBlank() } + simpleName)
        }
    }

    return ProcessResult.Success(result)
}

internal fun StreamableRSTypeUrl.asTypeName(resolver: RSResolver): ProcessResult<TypeName> {
    return type.asTypeName(resolver).map {
        if (isStreaming) LibClassNames.Flow(it) else it
    }
}

internal fun RSDeclarationUrl.qualifiedName(resolver: RSResolver): ProcessResult<String> {
    val file = resolver.resolveFileOf(this) ?: return ProcessResult.Failure(UnresolvableDeclarationError(this))
    val packageName = file.packageName?.value?.plus(".") ?: ""

    return ProcessResult.Success(packageName + simpleName)
}

internal val RSField.defaultValue: CodeBlock? get() {
    if (isRepeated)
        return CodeBlock.of("emptyList()")

    if (typeUrl.isMap)
        return CodeBlock.of("emptyMap()")

    return when (typeUrl) {
        RSDeclarationUrl.INT32,
        RSDeclarationUrl.INT64,
        RSDeclarationUrl.DURATION,
        RSDeclarationUrl.FIXED32,
        RSDeclarationUrl.FIXED64,
        RSDeclarationUrl.SFIXED32,
        RSDeclarationUrl.SFIXED64,
        RSDeclarationUrl.SINT32,
        RSDeclarationUrl.SINT64,
            -> "0"

        RSDeclarationUrl.UINT32, RSDeclarationUrl.UINT64 -> "0u"
        RSDeclarationUrl.STRING -> "\"\""
        RSDeclarationUrl.BOOL -> "false"
        RSDeclarationUrl.BYTES -> "byteArrayOf()"
        RSDeclarationUrl.DOUBLE -> "0.0"
        RSDeclarationUrl.FLOAT -> "0.0f"
        else -> return null
    }.let { CodeBlock.of(it) }
}