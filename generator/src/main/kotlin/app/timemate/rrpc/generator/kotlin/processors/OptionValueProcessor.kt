package app.timemate.rrpc.generator.kotlin.processors

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.buildCodeBlock
import app.timemate.rrpc.proto.schema.RSMessage
import app.timemate.rrpc.proto.schema.RSEnclosingType
import app.timemate.rrpc.proto.schema.RSEnum
import app.timemate.rrpc.proto.schema.RSFile
import app.timemate.rrpc.proto.schema.RSOption
import app.timemate.rrpc.generator.plugin.api.RSResolver
import app.timemate.rrpc.proto.schema.RSTypeMemberUrl
import app.timemate.rrpc.proto.schema.kotlinName
import app.timemate.rrpc.proto.schema.value.RSDeclarationUrl
import app.timemate.rrpc.generator.GeneratorContext
import app.timemate.rrpc.generator.plugin.api.result.ProcessResult
import app.timemate.rrpc.generator.Processor
import app.timemate.rrpc.generator.plugin.api.result.andAccumulate
import app.timemate.rrpc.generator.plugin.api.result.flatten
import app.timemate.rrpc.generator.plugin.api.result.getOrElse
import app.timemate.rrpc.generator.plugin.api.result.getOrThrow
import app.timemate.rrpc.generator.kotlin.internal.ext.asTypeName
import app.timemate.rrpc.generator.kotlin.internal.ext.protoByteStringToByteArray
import app.timemate.rrpc.generator.kotlin.error.ProtoAnyCannotBeOptionError
import app.timemate.rrpc.generator.kotlin.error.UnresolvableDeclarationError
import app.timemate.rrpc.generator.kotlin.error.UnresolvableDeclarationMemberError
import app.timemate.rrpc.generator.kotlin.error.UnsupportedTypeError
import app.timemate.rrpc.generator.plugin.api.result.onFailure
import app.timemate.rrpc.generator.plugin.api.result.onSuccess

public object OptionValueProcessor : Processor<RSOption, CodeBlock> {
    override suspend fun GeneratorContext.process(data: RSOption): ProcessResult<CodeBlock> = with(data) {
        val field = resolver.resolveField(fieldUrl)
            ?: return@with ProcessResult.Failure(UnresolvableDeclarationMemberError(fieldUrl))

        generate(field.typeUrl, data.value!!)
    }

    private suspend fun GeneratorContext.generate(
        typeUrl: RSDeclarationUrl,
        value: RSOption.Value,
    ): ProcessResult<CodeBlock> {
        return when {
            typeUrl.isScalar -> generateScalarOrString(
                typeUrl,
                value as RSOption.Value.Raw,
            )

            typeUrl.isWrapper -> generateWrapperValue(typeUrl, (value as RSOption.Value.MessageMap).map, resolver)
            typeUrl.isMap -> generateMap(typeUrl, (value as RSOption.Value.RawMap).map)
            else -> generateCustomType(typeUrl, value)
        }
    }

    private fun generateScalarOrString(
        protoType: RSDeclarationUrl,
        value: RSOption.Value.Raw,
    ): ProcessResult<CodeBlock> {
        val value = value.string

        return CodeBlock.Companion.of(
            when (protoType) {
                RSDeclarationUrl.Companion.STRING, RSDeclarationUrl.Companion.STRING_VALUE -> "\"${value}\""
                RSDeclarationUrl.Companion.BOOL -> value
                // We don't support special types such as fixed32, sfixed32 and sint32,
                // because it makes no sense if not used in serialization.
                // All they are represented as regular Int.
                RSDeclarationUrl.Companion.INT32, RSDeclarationUrl.Companion.FIXED32, RSDeclarationUrl.Companion.SFIXED32, RSDeclarationUrl.Companion.SINT32 -> value
                // We don't support special types such as fixed64, sfixed64 and sint64,
                // because it makes no sense if not used in serialization.
                // All they are represented as regular Long.
                RSDeclarationUrl.Companion.INT64, RSDeclarationUrl.Companion.FIXED64, RSDeclarationUrl.Companion.SFIXED64, RSDeclarationUrl.Companion.SINT64 -> "${value}L"
                RSDeclarationUrl.Companion.FLOAT -> "${value}f"
                RSDeclarationUrl.Companion.DOUBLE -> "${value}.toDouble()"
                RSDeclarationUrl.Companion.UINT32 -> "${value}.toUInt()"
                RSDeclarationUrl.Companion.UINT64 -> "${value}uL"
                RSDeclarationUrl.Companion.BYTES -> byteArrayToSourceCode(value.protoByteStringToByteArray())
                else -> error("Unsupported scalar type: $protoType")
            }
        ).let { ProcessResult.Success(it) }
    }

    private fun generateWrapperValue(
        protoType: RSDeclarationUrl,
        value: Map<RSTypeMemberUrl, Any>,
        resolver: RSResolver,
    ): ProcessResult<CodeBlock> {
        val element = value.values.first().toString()

        val innerValue = when (protoType) {
            RSDeclarationUrl.Companion.STRING_VALUE -> "\"${element}\""
            RSDeclarationUrl.Companion.BOOL_VALUE -> element
            RSDeclarationUrl.Companion.INT32_VALUE -> element
            RSDeclarationUrl.Companion.INT64_VALUE -> "${element}L"
            RSDeclarationUrl.Companion.FLOAT_VALUE -> "${element}f"
            RSDeclarationUrl.Companion.DOUBLE_VALUE -> "${element}.toDouble()"
            RSDeclarationUrl.Companion.UINT32_VALUE -> "${element}u"
            RSDeclarationUrl.Companion.UINT64_VALUE -> "${element}uL"
            RSDeclarationUrl.Companion.BYTES_VALUE -> byteArrayToSourceCode(element.protoByteStringToByteArray())
            else -> return ProcessResult.Failure(UnsupportedTypeError(protoType))
        }

        return CodeBlock.Companion.of(
            format = "%1T(%2L)",
            args = arrayOf(
                // safe to do, it's a builtin type
                protoType.asTypeName(resolver).getOrThrow(),
                innerValue,
            )
        ).let { ProcessResult.Success(it) }
    }

    private suspend fun GeneratorContext.generateMap(
        typeUrl: RSDeclarationUrl,
        value: Map<RSOption.Value.Raw, RSOption.Value.Raw>,
    ): ProcessResult<CodeBlock> {
        requireNotNull(typeUrl.firstTypeArgument)
        requireNotNull(typeUrl.secondTypeArgument)

        return CodeBlock.Companion.builder()
            .add("mapOf(")
            .indent()
            .apply {
                value.entries.map { (key, value) ->
                    generate(typeUrl.firstTypeArgument!!, key)
                        .andAccumulate { generate(typeUrl.secondTypeArgument!!, value) }
                        .onSuccess { (keyCode, valueCode) ->
                            add("%1L to %2L,\n", keyCode, valueCode)
                        }
                }.flatten().onFailure {
                    return it
                }
            }
            .unindent()
            .add(")")
            .build()
            .let { ProcessResult.Success(it) }
    }

    private suspend fun GeneratorContext.generateCustomType(
        typeUrl: RSDeclarationUrl,
        value: RSOption.Value?,
    ): ProcessResult<CodeBlock> {
        println("custom type: $typeUrl")
        if (typeUrl == RSDeclarationUrl.Companion.ANY) {
            return ProcessResult.Failure(
                ProtoAnyCannotBeOptionError(
                    file = this.get<RSFile>()
                )
            )
        }

        val (className, type) = typeUrl.asTypeName(resolver)
            .andAccumulate {
                ProcessResult.Success(
                    data = resolver.resolveType(typeUrl)
                        ?: return@andAccumulate ProcessResult.Failure(UnresolvableDeclarationError(typeUrl))
                )
            }
            .getOrElse { return it }

        println("resolved: $className, $type")

        return ProcessResult.Success(
            buildCodeBlock {
                when (type) {
                    is RSMessage -> {
                        add("%T.create {", className)
                        indent()
                        @Suppress("UNCHECKED_CAST")
                        (value as RSOption.Value.MessageMap).map.entries.map { (key, value) ->
                            val field = type.field(key.memberName)!!

                            val fieldName = if (options.adaptNames) field.kotlinName else field.name

                            generate(field.typeUrl, value).onSuccess {
                                add("\n${fieldName} = %L", it)
                            }
                        }.flatten().onFailure {
                            return it
                        }
                        unindent()
                        add("\n")
                        add("}")
                    }

                    is RSEnum -> add("%T.%L", className, value)
                    is RSEnclosingType -> add("null")
                }
            }
        )
    }

    private fun byteArrayToSourceCode(byteArray: ByteArray): String {
        return byteArray.joinToString(prefix = "byteArrayOf(", postfix = ")") { byte ->
            "0x${byte.toUByte().toString(16).uppercase().padStart(2, '0')}"
        }
    }
}