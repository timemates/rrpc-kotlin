package org.timemates.rrpc.generator.kotlin.adapter.options

import com.squareup.kotlinpoet.CodeBlock
import org.timemates.rrpc.common.schema.RSOption
import org.timemates.rrpc.common.schema.RSResolver
import org.timemates.rrpc.common.schema.RSType
import org.timemates.rrpc.common.schema.RSTypeMemberUrl
import org.timemates.rrpc.common.schema.value.RMDeclarationUrl
import org.timemates.rrpc.generator.kotlin.adapter.internal.ext.asClassName
import org.timemates.rrpc.generator.kotlin.adapter.internal.ext.protoByteStringToByteArray

public object OptionValueGenerator {
    public fun generate(
        typeUrl: RMDeclarationUrl,
        value: RSOption.Value?,
        resolver: RSResolver,
    ): String {
        @Suppress("UNCHECKED_CAST")
        return when {
            typeUrl.isScalar || typeUrl == RMDeclarationUrl.STRING || typeUrl == RMDeclarationUrl.STRING_VALUE -> generateScalarOrString(
                typeUrl,
                value
            )

            typeUrl.isWrapper -> generateWrapperValue(typeUrl, (value as RSOption.Value.MessageMap).map)
            typeUrl.isMap -> generateMap(typeUrl, (value as RSOption.Value.RawMap).map, resolver)
            else -> generateCustomType(typeUrl, value!!, resolver)
        }
    }

    private fun generateScalarOrString(
        protoType: RMDeclarationUrl,
        value: Any?,
    ): String {
        return when (protoType) {
            RMDeclarationUrl.STRING, RMDeclarationUrl.STRING_VALUE -> "\"${value}\""
            RMDeclarationUrl.BOOL -> "$value"
            // We don't support special types such as fixed32, sfixed32 and sint32,
            // because it makes no sense if not used in serialization.
            // All they are represented as regular Int.
            RMDeclarationUrl.INT32, RMDeclarationUrl.FIXED32, RMDeclarationUrl.SFIXED32, RMDeclarationUrl.SINT32 -> "$value"
            // We don't support special types such as fixed64, sfixed64 and sint64,
            // because it makes no sense if not used in serialization.
            // All they are represented as regular Long.
            RMDeclarationUrl.INT64, RMDeclarationUrl.FIXED64, RMDeclarationUrl.SFIXED64, RMDeclarationUrl.SINT64 -> "${value}L"
            RMDeclarationUrl.FLOAT -> "${value}f"
            RMDeclarationUrl.DOUBLE -> "${value}.toDouble()"
            RMDeclarationUrl.UINT32 -> "${value}.toUInt()"
            RMDeclarationUrl.UINT64 -> "${value}uL"
            RMDeclarationUrl.BYTES -> byteArrayToSourceCode((value as String).protoByteStringToByteArray())
            else -> error("Unsupported type")
        }
    }

    private fun generateWrapperValue(
        protoType: RMDeclarationUrl,
        value: Map<RSTypeMemberUrl, Any>,
    ): String {
        val element = value.values.first().toString()

        return when (protoType) {
            RMDeclarationUrl.STRING_VALUE -> "\"${element}\""
            RMDeclarationUrl.BOOL_VALUE -> element
            RMDeclarationUrl.INT32_VALUE -> element
            RMDeclarationUrl.INT64_VALUE -> "${element}L"
            RMDeclarationUrl.FLOAT_VALUE -> "${element}f"
            RMDeclarationUrl.DOUBLE -> "${element}.toDouble()"
            RMDeclarationUrl.UINT32_VALUE -> "${element}u"
            RMDeclarationUrl.UINT64_VALUE -> "${element}uL"
            else -> TODO("Unsupported for now: $protoType")
        }
    }

    private fun generateMap(
        typeUrl: RMDeclarationUrl,
        value: Map<RSOption.Value.Raw, RSOption.Value.Raw>,
        schema: RSResolver,
    ): String {
        requireNotNull(typeUrl.firstTypeArgument)
        requireNotNull(typeUrl.secondTypeArgument)

        return CodeBlock.builder()
            .add("mapOf(")
            .indent()
            .apply {
                value.entries.forEach { (key, value) ->
                    add(
                        "\n" + generate(typeUrl.firstTypeArgument!!, key, schema) +
                            " to " +
                            generate(typeUrl.secondTypeArgument!!, value, schema) + ","
                    )
                }
            }
            .unindent()
            .add(")")
            .toString()
    }

    private fun generateCustomType(
        typeUrl: RMDeclarationUrl,
        value: RSOption.Value?,
        schema: RSResolver,
    ): String {
        require(typeUrl != RMDeclarationUrl.ANY) { "google.protobuf.Any type is not supported." }
        val type = schema.resolveType(typeUrl)
        val className = typeUrl.asClassName(schema)

        return when (type) {
            is RSType.Message -> {
                return CodeBlock.builder()
                    .add("%T.create {", className)
                    .indent()
                    .apply {
                        @Suppress("UNCHECKED_CAST")
                        (value as Map<RSTypeMemberUrl, RSOption.Value>).entries.forEach { (key, value) ->
                            val field = type.field(key.memberName)!!
                            add("\n${field.name} = ${generate(field.typeUrl, value, schema)}")
                        }
                    }
                    .unindent()
                    .add("\n")
                    .add("}")
                    .build()
                    .toString()
            }

            is RSType.Enum -> CodeBlock.of("%T.%L", className, value).toString()
            is RSType.Enclosing -> "null"
            null -> error("Unable to resolve custom type for: $typeUrl.")
        }
    }

    private fun byteArrayToSourceCode(byteArray: ByteArray): String {
        return byteArray.joinToString(prefix = "byteArrayOf(", postfix = ")") { byte ->
            "0x${byte.toUByte().toString(16).uppercase().padStart(2, '0')}"
        }
    }
}