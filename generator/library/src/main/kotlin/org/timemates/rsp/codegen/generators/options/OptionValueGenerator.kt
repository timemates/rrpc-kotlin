package org.timemates.rsp.codegen.generators.options

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.wire.schema.*
import org.timemates.rsp.codegen.ext.asClassName
import org.timemates.rsp.codegen.ext.protoByteStringToByteArray

public object OptionValueGenerator {
    public fun generate(
        protoType: ProtoType,
        value: Any?,
        schema: Schema,
    ): String {
        @Suppress("UNCHECKED_CAST")
        return when {
            protoType.isScalar || protoType == ProtoType.STRING || protoType == ProtoType.STRING_VALUE -> generateScalarOrString(protoType, value)
            protoType.isWrapper -> generateWrapperValue(protoType, value as Map<ProtoMember, Any>)
            protoType.isMap -> generateMap(protoType, value as Map<*, *>, schema)
            else -> generateCustomType(protoType, value!!, schema)
        }
    }

    private fun generateScalarOrString(
        protoType: ProtoType,
        value: Any?,
    ): String {
        return when (protoType) {
            ProtoType.STRING, ProtoType.STRING_VALUE -> "\"${value}\""
            ProtoType.BOOL -> "$value"
            // We don't support special types such as fixed32, sfixed32 and sint32,
            // because it makes no sense if not used in serialization.
            // All they are represented as regular Int.
            ProtoType.INT32, ProtoType.FIXED32, ProtoType.SFIXED32, ProtoType.SINT32 -> "$value"
            // We don't support special types such as fixed64, sfixed64 and sint64,
            // because it makes no sense if not used in serialization.
            // All they are represented as regular Long.
            ProtoType.INT64, ProtoType.FIXED64, ProtoType.SFIXED64, ProtoType.SINT64 -> "${value}L"
            ProtoType.FLOAT -> "${value}f"
            ProtoType.DOUBLE -> "${value}.toDouble()"
            ProtoType.UINT32 -> "${value}.toUInt()"
            ProtoType.UINT64 -> "${value}uL"
            ProtoType.BYTES -> byteArrayToSourceCode((value as String).protoByteStringToByteArray())
            else -> error("Unsupported type")
        }
    }

    private fun generateWrapperValue(
        protoType: ProtoType,
        value: Map<ProtoMember, Any>,
    ): String {
        val element = value.values.first().toString()

        return when(protoType) {
            ProtoType.STRING_VALUE -> "\"${element}\""
            ProtoType.BOOL_VALUE -> element
            ProtoType.INT32_VALUE -> element
            ProtoType.INT64_VALUE -> "${element}L"
            ProtoType.FLOAT_VALUE -> "${element}f"
            ProtoType.DOUBLE -> "${element}.toDouble()"
            ProtoType.UINT32_VALUE -> "${element}u"
            ProtoType.UINT64_VALUE -> "${element}uL"
            else -> TODO("Unsupported for now: $protoType")
        }
    }

    private fun generateMap(
        protoType: ProtoType,
        value: Map<*, *>,
        schema: Schema,
    ): String {
        requireNotNull(protoType.keyType)
        requireNotNull(protoType.valueType)

        return CodeBlock.builder()
            .add("mapOf(")
            .indent()
            .apply {
                value.entries.forEach { (key, value) ->
                    add(
                        "\n" + generate(protoType.keyType!!, key, schema) +
                            " to " +
                            generate(protoType.valueType!!, value, schema) + ","
                    )
                }
            }
            .unindent()
            .add(")")
            .toString()
    }

    private fun generateCustomType(
        protoType: ProtoType,
        protoValue: Any?,
        schema: Schema,
    ): String {
        require(protoType != ProtoType.ANY) { "google.protobuf.Any type is not supported." }
        val type = schema.getType(protoType)
        val className = protoType.asClassName(schema)

        return when(type) {
            is MessageType -> {
                return CodeBlock.builder()
                    .add("%T.create {", className)
                    .indent()
                    .apply {
                        @Suppress("UNCHECKED_CAST")
                        (protoValue as Map<ProtoMember, Any>).entries.forEach { (key, value) ->
                            val field = type.field(key.member)!!
                            add("\n${field.name} = ${generate(field.type!!, value, schema)}")
                        }
                    }
                    .unindent()
                    .add("\n")
                    .add("}")
                    .build()
                    .toString()
            }
            is EnumType -> CodeBlock.of("%T.%L", className, protoValue).toString()
            is EnclosingType -> "null"
            null -> error("Unable to resolve custom type for: $protoType.")
        }
    }

    private fun byteArrayToSourceCode(byteArray: ByteArray): String {
        return byteArray.joinToString(prefix = "byteArrayOf(", postfix = ")") { byte ->
            "0x${byte.toUByte().toString(16).toUpperCase().padStart(2, '0')}"
        }
    }
}