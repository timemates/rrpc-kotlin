package app.timemate.rrpc.generator.kotlin.processors

import app.timemate.rrpc.generator.plugin.api.result.ProcessResult
import app.timemate.rrpc.generator.plugin.api.result.andAccumulate
import app.timemate.rrpc.generator.plugin.api.result.flatten
import app.timemate.rrpc.generator.plugin.api.result.getOrElse
import app.timemate.rrpc.generator.plugin.api.result.onFailure
import app.timemate.rrpc.generator.plugin.api.result.onSuccess
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import app.timemate.rrpc.proto.schema.RSOptions
import app.timemate.rrpc.proto.schema.RSRpc
import app.timemate.rrpc.proto.schema.RSService
import app.timemate.rrpc.proto.schema.isDeprecated
import app.timemate.rrpc.proto.schema.isFireAndForget
import app.timemate.rrpc.proto.schema.isMetadataPush
import app.timemate.rrpc.proto.schema.isRequestChannel
import app.timemate.rrpc.proto.schema.isRequestResponse
import app.timemate.rrpc.proto.schema.isRequestStream
import app.timemate.rrpc.proto.schema.kotlinName
import app.timemate.rrpc.proto.schema.sourceOnly
import app.timemate.rrpc.proto.schema.value.RSDeclarationUrl
import app.timemate.rrpc.generator.kotlin.internal.PoetAnnotations
import app.timemate.rrpc.generator.*
import app.timemate.rrpc.generator.kotlin.internal.ImportRequirement
import app.timemate.rrpc.generator.kotlin.internal.LibClassNames
import app.timemate.rrpc.generator.kotlin.internal.ext.addImport
import app.timemate.rrpc.generator.kotlin.internal.ext.deprecated
import app.timemate.rrpc.generator.kotlin.internal.ext.newline
import app.timemate.rrpc.generator.kotlin.error.AckTypeCannotBeStreamingError
import app.timemate.rrpc.generator.kotlin.error.ClientOnlyStreamingRpcError
import app.timemate.rrpc.generator.kotlin.error.FileCannotBeResolvedError
import app.timemate.rrpc.generator.kotlin.error.UnresolvableDeclarationMemberError
import app.timemate.rrpc.generator.kotlin.internal.ext.asTypeName
import app.timemate.rrpc.proto.schema.kotlinPackage
import app.timemate.rrpc.proto.schema.option.ExtendGenerationStrategy
import app.timemate.rrpc.proto.schema.option.extendGenerationStrategy
import app.timemate.rrpc.proto.schema.value.RSPackageName

public object ClientProcessor : Processor<RSService, TypeSpec> {
    override suspend fun GeneratorContext.process(data: RSService): ProcessResult<TypeSpec> {
        val className = ClassName("", "${data.name}Client")

        val (functions, optionsProperty) = data.rpcs.map { rpc ->
            processRpc(rpc)
        }.flatten().andAccumulate {
            processOptions(data.rpcs.associate { it.name to it.options })
        }.getOrElse { return it }

        return ProcessResult.Success(
            TypeSpec.classBuilder(className)
                .addAnnotation(
                    PoetAnnotations.OptIn(PoetAnnotations.InternalRRpcAPI)
                )
                .primaryConstructor(
                    FunSpec.constructorBuilder()
                        .addParameter("config", LibClassNames.RRpcClientConfig)
                        .build()
                ).superclass(LibClassNames.RRpcClientService)
                .addSuperclassConstructorParameter("config")
                .addFunctions(functions)
                .addProperty(optionsProperty)
                .addProperty(
                    PropertySpec.builder("serviceName", STRING)
                        .addModifiers(KModifier.PROTECTED, KModifier.OVERRIDE)
                        .initializer("%S", data.name)
                        .build()
                )
                .build()
        )
    }

    private suspend fun GeneratorContext.processOptions(
        optionsMap: Map<String, RSOptions>,
    ): ProcessResult<PropertySpec> {
        val file = get<FileSpec.Builder>()

        val code = buildCodeBlock {
            if (optionsMap.isEmpty()) {
                add("RPCsOptions.EMPTY")
                return@buildCodeBlock
            }
            add("RPCsOptions(")
            indent()

            optionsMap.filter { it.value.list.isNotEmpty() }.forEach { (rpc, options) ->
                add("\n%S to %T(", rpc, LibClassNames.OptionsWithValue)
                newline()
                indent()
                add("mapOf(")
                newline()
                indent()
                options.list.map { option ->
                    return@map ProcessResult.Success(
                        buildCodeBlock {
                            val field = resolver.resolveField(option.fieldUrl)
                                ?: return@map ProcessResult.Failure(UnresolvableDeclarationMemberError(option.fieldUrl))

                            val fieldName = if (this@processOptions.options.adaptNames) field.kotlinName else field.name

                            if (field.options.sourceOnly)
                                return@forEach

                            val isTopLevel = field.namespaces?.simpleNames?.any() ?: false

                            if (!isTopLevel) {
                                file.addImport(
                                    ImportRequirement(
                                        packageName = (
                                            resolver.resolveFileAt(field.location)
                                                ?: return ProcessResult.Failure(FileCannotBeResolvedError(field.location))
                                            ).kotlinPackage ?: RSPackageName.EMPTY,
                                        simpleNames = field.namespaces!!.simpleNames + listOf(fieldName),
                                    )
                                )
                            }

                            val strategy = field.options.extendGenerationStrategy
                                ?: if (field.namespaces!!.simpleNames.any()) {
                                    ExtendGenerationStrategy.REGULAR
                                } else {
                                    ExtendGenerationStrategy.EXTENSION
                                }

                            when (strategy) {
                                ExtendGenerationStrategy.EXTENSION ->
                                    add("%T.${fieldName} to ", LibClassNames.Option.RPC)
                                ExtendGenerationStrategy.REGULAR ->
                                    add("%T.$fieldName to ", ClassName(field.namespaces!!.packageName.value, field.namespaces!!.simpleNames))
                            }

                            add(
                                OptionValueProcessor.process(option, this@processOptions)
                                    .getOrElse { return@map it }
                            )
                            add(",\n")
                        }
                    )
                }.flatten().onSuccess { codeBlocks ->
                    codeBlocks.forEach {
                        add(it)
                    }
                }.onFailure {
                    return it
                }

                unindent()
                add(")")
                unindent()
                newline()
                add("),")
            }
            newline()
            unindent()
            add(")")
        }

        return ProcessResult.Success(
            PropertySpec.builder("rpcsOptions", LibClassNames.RPCsOptions)
                .addModifiers(KModifier.OVERRIDE)
                .initializer(code)
                .build()
        )
    }

    private fun GeneratorContext.processRpc(data: RSRpc): ProcessResult<FunSpec> {
        when {
            data.requestType.type == RSDeclarationUrl.ACK && data.requestType.isStreaming ->
                return ProcessResult.Failure(AckTypeCannotBeStreamingError(data))

            data.responseType.type == RSDeclarationUrl.ACK && data.responseType.isStreaming ->
                return ProcessResult.Failure(AckTypeCannotBeStreamingError(data))

            data.requestType.isStreaming && !data.responseType.isStreaming ->
                return ProcessResult.Failure(ClientOnlyStreamingRpcError(data))
        }

        val (rpcRequestType, rpcReturnType) = data.requestType.asTypeName(resolver)
            .andAccumulate { data.responseType.type.asTypeName(resolver) }
            .getOrElse { return it }

        val rpcName = if (options.adaptNames) data.kotlinName else data.name

        val code = when {
            data.isFireAndForget -> {
                CodeBlock.of(
                    FIRE_AND_FORGET_CODE,
                    LibClassNames.ClientMetadata,
                    data.name,
                    LibClassNames.ExtraMetadata,
                    rpcRequestType,
                    LibClassNames.OptionsWithValue,
                )
            }

            data.isMetadataPush -> {
                CodeBlock.of(
                    METADATA_PUSH_CODE,
                    LibClassNames.ClientMetadata,
                    data.name,
                    LibClassNames.ExtraMetadata,
                    LibClassNames.OptionsWithValue,
                )
            }

            else -> {
                CodeBlock.of(
                    format = BASIC_REQUEST_CODE,
                    args = arrayOf(
                        when {
                            data.isRequestResponse -> "requestResponse"
                            data.isRequestStream -> "requestStream"
                            data.isRequestChannel -> "requestChannel"
                            else -> error("Unsupported type.")
                        },
                        LibClassNames.ClientMetadata,
                        data.name,
                        LibClassNames.ExtraMetadata,
                        if (data.requestType.isStreaming) "messages" else "message",
                        rpcRequestType,
                        rpcReturnType,
                        LibClassNames.OptionsWithValue,
                    )
                )
            }
        }

        return FunSpec.builder(rpcName)
            .deprecated(data.options.isDeprecated)
            .apply {
                if (data.requestType.isStreaming) {
                    addParameter(
                        name = "messages",
                        type = LibClassNames.Flow(rpcRequestType)
                    )
                } else {
                    addParameter(
                        name = "message",
                        type = rpcRequestType,
                    )
                }
            }
            .addParameter(
                ParameterSpec.builder("extra", MAP.parameterizedBy(STRING, BYTE_ARRAY))
                    .defaultValue("emptyMap()")
                    .build()
            )
            .apply {
                if (data.isRequestResponse || data.isFireAndForget || data.isMetadataPush) {
                    addModifiers(KModifier.SUSPEND)
                }
            }
            .addCode(code)
            .returns(rpcReturnType.let { if (data.responseType.isStreaming) LibClassNames.Flow(it) else it })
            .build()
            .let { ProcessResult.Success(it) }
    }
}

private val BASIC_REQUEST_CODE = """
        return handler.%1L(
                %2T(
                    serviceName = this.serviceName,
                    procedureName = %3S,
                    extra = %4T(extra),
                ),
                data = %5L,
                options = rpcsOptions[%3S] ?: %8T.EMPTY,
                serializationStrategy = %6T.serializer(),
                deserializationStrategy = %7T.serializer(),
            )
        """.trimIndent()

private val FIRE_AND_FORGET_CODE = """
        return handler.fireAndForget(
                metadata= %1T(
                    serviceName = this.serviceName,
                    procedureName = %2S,
                    extra = %3T(extra),
                ),
                data = message,
                options = rpcsOptions[%2S] ?: %4T.EMPTY,
                serializationStrategy = %5T.serializer(),
            )
        """.trimIndent()

private val METADATA_PUSH_CODE = """
        return handler.metadataPush(
                metadata= %1T(
                    serviceName = this.serviceName,
                    procedureName = %2S,
                    extra = %3T(extra),
                ),
                options = rpcsOptions[%2S] ?: %4T.EMPTY,
            )
        """.trimIndent()