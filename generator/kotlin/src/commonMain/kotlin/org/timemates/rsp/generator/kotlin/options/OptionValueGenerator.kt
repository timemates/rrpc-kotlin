package org.timemates.rrpc.generator.kotlin.options

import com.squareup.kotlinpoet.CodeBlock
import org.timemates.rrpc.common.metadata.RMOption
import org.timemates.rrpc.common.metadata.RMResolver
import org.timemates.rrpc.common.metadata.RMType
import org.timemates.rrpc.common.metadata.RMTypeMemberUrl
import org.timemates.rrpc.common.metadata.value.RMTypeUrl
import org.timemates.rrpc.generator.kotlin.ext.asClassName
import org.timemates.rrpc.generator.kotlin.ext.protoByteStringToByteArray

public object OptionValueGenerator {
    public fun generate(
        typeUrl: RMTypeUrl,
        value: RMOption.Value?,
        resolver: RMResolver,
    ): String {
        @Suppress("UNCHECKED_CAST")
        return when {
            typeUrl.isScalar || typeUrl == RMTypeUrl.STRING || typeUrl == RMTypeUrl.STRING_VALUE -> generateScalarOrString(
                typeUrl,
                value
            )

            typeUrl.isWrapper -> generateWrapperValue(typeUrl, (value as RMOption.Value.MessageMap).map)
            typeUrl.isMap -> generateMap(typeUrl, (value as RMOption.Value.RawMap).map, resolver)
            else -> generateCustomType(typeUrl, value!!, resolver)
        }
    }

    private fun generateScalarOrString(
        protoType: RMTypeUrl,
        value: Any?,
    ): String {
        return when (protoType) {
            RMTypeUrl.STRING, RMTypeUrl.STRING_VALUE -> "\"${value}\""
            RMTypeUrl.BOOL -> "$value"
            // We don't support special types such as fixed32, sfixed32 and sint32,
            // because it makes no sense if not used in serialization.
            // All they are represented as regular Int.
            RMTypeUrl.INT32, RMTypeUrl.FIXED32, RMTypeUrl.SFIXED32, RMTypeUrl.SINT32 -> "$value"
            // We don't support special types such as fixed64, sfixed64 and sint64,
            // because it makes no sense if not used in serialization.
            // All they are represented as regular Long.
            RMTypeUrl.INT64, RMTypeUrl.FIXED64, RMTypeUrl.SFIXED64, RMTypeUrl.SINT64 -> "${value}L"
            RMTypeUrl.FLOAT -> "${value}f"
            RMTypeUrl.DOUBLE -> "${value}.toDouble()"
            RMTypeUrl.UINT32 -> "${value}.toUInt()"
            RMTypeUrl.UINT64 -> "${value}uL"
            RMTypeUrl.BYTES -> byteArrayToSourceCode((value as String).protoByteStringToByteArray())
            else -> error("Unsupported type")
        }
    }

    private fun generateWrapperValue(
        protoType: RMTypeUrl,
        value: Map<RMTypeMemberUrl, Any>,
    ): String {
        val element = value.values.first().toString()

        return when (protoType) {
            RMTypeUrl.STRING_VALUE -> "\"${element}\""
            RMTypeUrl.BOOL_VALUE -> element
            RMTypeUrl.INT32_VALUE -> element
            RMTypeUrl.INT64_VALUE -> "${element}L"
            RMTypeUrl.FLOAT_VALUE -> "${element}f"
            RMTypeUrl.DOUBLE -> "${element}.toDouble()"
            RMTypeUrl.UINT32_VALUE -> "${element}u"
            RMTypeUrl.UINT64_VALUE -> "${element}uL"
            else -> TODO("Unsupported for now: $protoType")
        }
    }

    private fun generateMap(
        typeUrl: RMTypeUrl,
        value: Map<RMOption.Value, RMOption.Value>,
        schema: RMResolver,
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
        typeUrl: RMTypeUrl,
        value: RMOption.Value?,
        schema: RMResolver,
    ): String {
        require(typeUrl != RMTypeUrl.ANY) { "google.protobuf.Any type is not supported." }
        val type = schema.resolveType(typeUrl)
        val className = typeUrl.asClassName(schema)

        return when (type) {
            is RMType.Message -> {
                return CodeBlock.builder()
                    .add("%T.create {", className)
                    .indent()
                    .apply {
                        @Suppress("UNCHECKED_CAST")
                        (value as Map<RMTypeMemberUrl, RMOption.Value>).entries.forEach { (key, value) ->
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

            is RMType.Enum -> CodeBlock.of("%T.%L", className, value).toString()
            is RMType.Enclosing -> "null"
            null -> error("Unable to resolve custom type for: $typeUrl.")
        }
    }

    private fun byteArrayToSourceCode(byteArray: ByteArray): String {
        return byteArray.joinToString(prefix = "byteArrayOf(", postfix = ")") { byte ->
            "0x${byte.toUByte().toString(16).uppercase().padStart(2, '0')}"
        }
    }
}