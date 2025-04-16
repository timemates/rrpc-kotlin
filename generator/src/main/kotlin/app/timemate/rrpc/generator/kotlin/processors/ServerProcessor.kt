package app.timemate.rrpc.generator.kotlin.processors

import app.timemate.rrpc.generator.GeneratorContext
import app.timemate.rrpc.generator.Processor
import app.timemate.rrpc.generator.kotlin.error.FileCannotBeResolvedError
import app.timemate.rrpc.generator.kotlin.error.UnresolvableDeclarationMemberError
import app.timemate.rrpc.generator.kotlin.internal.ImportRequirement
import app.timemate.rrpc.generator.kotlin.internal.LibClassNames
import app.timemate.rrpc.generator.kotlin.internal.ext.addImport
import app.timemate.rrpc.generator.kotlin.internal.ext.asTypeName
import app.timemate.rrpc.generator.kotlin.internal.ext.deprecated
import app.timemate.rrpc.generator.kotlin.internal.ext.newline
import app.timemate.rrpc.generator.plugin.api.RSResolver
import app.timemate.rrpc.generator.plugin.api.result.*
import app.timemate.rrpc.proto.schema.*
import app.timemate.rrpc.proto.schema.option.ExtendGenerationStrategy
import app.timemate.rrpc.proto.schema.option.extendGenerationStrategy
import app.timemate.rrpc.proto.schema.value.RSDeclarationUrl
import app.timemate.rrpc.proto.schema.value.RSPackageName
import com.squareup.kotlinpoet.*

public object ServerProcessor : Processor<RSService, TypeSpec> {
    override suspend fun GeneratorContext.process(data: RSService): ProcessResult<TypeSpec> {
        val (rpcs, metadata) = data.rpcs.map { generateRpc(it, resolver) }
            .flatten().andAccumulate {
                generateMetadata(data, resolver)
            }.getOrElse { return it }

        return ProcessResult.Success(
            TypeSpec.classBuilder(data.name)
                .addModifiers(KModifier.ABSTRACT)
                .addSuperinterface(LibClassNames.RRpcServerService)
                .addProperty(metadata)
                .addFunctions(rpcs)
                .build()
        )
    }

    private fun generateRpc(
        rpc: RSRpc,
        resolver: RSResolver,
    ): ProcessResult<FunSpec> {
        val (requestType, returnType) = getRpcType(rpc, resolver)
            .getOrElse { return it }

        return ProcessResult.Success(
            FunSpec.builder(rpc.kotlinName)
                .addKdoc(rpc.documentation.replace("%", "%%"))
                .addModifiers(KModifier.ABSTRACT)
                .deprecated(rpc.options.isDeprecated)
                .addParameter("context", LibClassNames.RequestContext)
                .apply {
                    if (rpc.isRequestResponse)
                        addModifiers(KModifier.SUSPEND)

                    if (rpc.requestType.type != RSDeclarationUrl.ACK)
                        addParameter(
                            "request",
                            requestType,
                        )

                    if (rpc.responseType.type != RSDeclarationUrl.ACK)
                        returns(returnType)
                }
                .build()
        )
    }

    private fun getRpcType(rpc: RSRpc, schema: RSResolver): ProcessResult<Pair<TypeName, TypeName>> {
        return rpc.requestType.asTypeName(schema).andAccumulate {
            rpc.responseType.asTypeName(schema)
        }
    }

    private suspend fun GeneratorContext.generateMetadata(
        service: RSService,
        resolver: RSResolver,
    ): ProcessResult<PropertySpec> {
        return ProcessResult.Success(
            PropertySpec.builder("descriptor", LibClassNames.ServiceDescriptor)
                .addModifiers(KModifier.OVERRIDE)
                .initializer(
                    buildCodeBlock {
                        add("%T(", LibClassNames.ServiceDescriptor)
                        newline()
                        indent()
                        add("name = %S,", service.name)
                        newline()
                        add("procedures = listOf(")
                        indent()

                        service.rpcs.map { rpc ->
                            val (requestType, responseType) = rpc.requestType.type.asTypeName(resolver)
                                .andAccumulate { rpc.responseType.type.asTypeName(resolver) }
                                .getOrElse { return@map it }

                            val type = when {
                                rpc.isRequestResponse -> LibClassNames.ProcedureDescriptor.requestResponse
                                rpc.isRequestStream -> LibClassNames.ProcedureDescriptor.requestStream
                                rpc.isRequestChannel -> LibClassNames.ProcedureDescriptor.requestChannel
                                rpc.isFireAndForget -> LibClassNames.ProcedureDescriptor.fireAndForget
                                rpc.isMetadataPush -> LibClassNames.ProcedureDescriptor.metadataPush
                                else -> error("Unsupported type of request for ${service.name}#${rpc.name}")
                            }

                            newline()
                            add("%T(", type)
                            newline()
                            indent()
                            add("name = %S", rpc.name)
                            newline(",")
                            if (!rpc.isMetadataPush) {
                                add("inputSerializer = %T.serializer()", requestType)
                                newline(",")
                            }

                            if (!rpc.isMetadataPush || !rpc.isFireAndForget) {
                                add("outputSerializer = %T.serializer()", responseType)
                                newline(",")
                            }

                            if (!rpc.isMetadataPush || !rpc.isFireAndForget) {
                                add("procedure = { context, data -> %L(context, data) }", rpc.kotlinName)
                            } else {
                                add("procedure = { context -> %L(context) }", rpc.kotlinName)
                            }
                            newline(",")

                            val rawOptions =
                                generateRawOptions(rpc.options, resolver, RSOptions.METHOD_OPTIONS)
                                    .getOrElse { return@map it }

                            add("options = ")
                            add(rawOptions)
                            newline(before = ",")
                            unindent()
                            add("),")
                            ProcessResult.Success(Unit)
                        }.flatten().andAccumulate {
                            generateRawOptions(service.options, resolver, RSOptions.SERVICE_OPTIONS)
                        }.onSuccess { (_, serviceOptionsCode) ->
                            unindent()
                            newline()
                            add("),")
                            newline()
                            add("options = ")
                            add(serviceOptionsCode)
                            newline(before = ",")
                            unindent()
                            add(")")
                        }.onFailure { return it }
                    }
                )
                .build()
        )
    }

    private suspend fun GeneratorContext.generateRawOptions(
        options: RSOptions,
        resolver: RSResolver,
        optionsType: RSDeclarationUrl,
    ): ProcessResult<CodeBlock> {
        val file = get<FileSpec.Builder>()

        return ProcessResult.Success(
            buildCodeBlock {
                val options = options.list.mapNotNull { option ->
                    val field = resolver.resolveField(option.fieldUrl)
                        ?: return@mapNotNull ProcessResult.Failure(UnresolvableDeclarationMemberError(option.fieldUrl))

                    if (field.options.sourceOnly)
                        return@mapNotNull null

                    ProcessResult.Success(option to field)
                }.flatten().getOrElse { return it }.associate { it }

                add("%T(", LibClassNames.OptionsWithValue)

                if (options.isEmpty()) {
                    add("emptyMap())")
                    return@buildCodeBlock
                }

                newline()
                indent()

                options.map { (option, field) ->
                    val fieldName = when (this@generateRawOptions.options.adaptNames) {
                        true -> field.kotlinName
                        false -> field.name
                    }

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
                        ExtendGenerationStrategy.REGULAR -> {
                            add(
                                "%T.$fieldName to ",
                                ClassName(field.namespaces!!.packageName.value, field.namespaces!!.simpleNames)
                            )
                        }

                        ExtendGenerationStrategy.EXTENSION ->
                            add(
                                format = "%T.${fieldName} to ",
                                when (optionsType) {
                                    RSOptions.METHOD_OPTIONS -> LibClassNames.Option.RPC
                                    RSOptions.SERVICE_OPTIONS -> LibClassNames.Option.Service
                                    // shouldn't reach this point
                                    else -> error("Unsupported type of option: ${field.typeUrl}")
                                }
                            )
                    }
                    OptionValueProcessor.process(option, this@generateRawOptions)
                        .onSuccess {
                            add(it)
                            newline(before = ",")
                        }
                }.flatten().getOrElse { return it }

                unindent()
                add(")")
            }
        )
    }
}