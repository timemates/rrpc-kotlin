package app.timemate.rrpc.generator.kotlin.processors

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import app.timemate.rrpc.generator.plugin.api.annotation.ExperimentalGeneratorFunctionality
import app.timemate.rrpc.generator.plugin.api.option.ExtensionGenerationStrategy
import app.timemate.rrpc.proto.schema.RSExtend
import app.timemate.rrpc.proto.schema.RSField
import app.timemate.rrpc.proto.schema.RSOptions
import app.timemate.rrpc.proto.schema.kotlinName
import app.timemate.rrpc.proto.schema.sourceOnly
import app.timemate.rrpc.proto.schema.value.RSDeclarationUrl
import app.timemate.rrpc.generator.GeneratorContext
import app.timemate.rrpc.generator.plugin.api.result.ProcessResult
import app.timemate.rrpc.generator.Processor
import app.timemate.rrpc.generator.kotlin.error.ExtendingMessagesAreNotSupportedError
import app.timemate.rrpc.generator.plugin.api.result.flatten
import app.timemate.rrpc.generator.kotlin.internal.LibClassNames
import app.timemate.rrpc.generator.kotlin.internal.ext.asTypeName
import app.timemate.rrpc.generator.plugin.api.option.extensionGenerationStrategy
import app.timemate.rrpc.generator.plugin.api.result.map

public object ExtendProcessor : Processor<RSExtend, List<PropertySpec>> {
    override suspend fun GeneratorContext.process(data: RSExtend): ProcessResult<List<PropertySpec>> {
        val isTopLevel = data.fields.firstOrNull()
            ?.namespaces
            ?.simpleNames
            ?.any()
            ?: return ProcessResult.Success(emptyList())


        return when (data.typeUrl) {
            // we don't support for now options generation for anything except methods, files and services.
            RSOptions.Companion.FIELD_OPTIONS,
            RSOptions.Companion.MESSAGE_OPTIONS,
            RSOptions.Companion.ONEOF_OPTIONS,
            RSOptions.Companion.ENUM_OPTIONS,
            RSOptions.Companion.ENUM_VALUE_OPTIONS,
                -> emptyList()

            RSOptions.Companion.METHOD_OPTIONS,
            RSOptions.Companion.FILE_OPTIONS,
            RSOptions.Companion.SERVICE_OPTIONS,
                -> data.fields.mapNotNull {
                if (it.options.sourceOnly)
                    return@mapNotNull null

                generateOption(
                    field = it,
                    type = getClassNameFromExtendType(data.typeUrl),
                    topLevel = isTopLevel,
                )
            }

            else -> return ProcessResult.Failure(ExtendingMessagesAreNotSupportedError(data))
        }.flatten()
    }

    private fun getClassNameFromExtendType(type: RSDeclarationUrl): ClassName {
        return when (type) {
            RSOptions.Companion.METHOD_OPTIONS -> LibClassNames.Option.RPC
            RSOptions.Companion.SERVICE_OPTIONS -> LibClassNames.Option.Service
            RSOptions.Companion.FILE_OPTIONS -> LibClassNames.Option.File
            else -> error("Should not reach this state.")
        }
    }

    @OptIn(ExperimentalGeneratorFunctionality::class)
    private fun GeneratorContext.generateOption(
        field: RSField,
        type: ClassName,
        topLevel: Boolean,
    ): ProcessResult<PropertySpec> {
        // the decision is dependent on context, please refer to the documentation of strategy
        val strategy = field.options.extensionGenerationStrategy
            ?: if (topLevel) ExtensionGenerationStrategy.REGULAR else ExtensionGenerationStrategy.EXTENSION

        return field.typeUrl.asTypeName(resolver).map { optionType ->
            val fieldName = if (options.adaptNames) field.kotlinName else field.name

            PropertySpec.Companion.builder(fieldName, type.parameterizedBy(optionType))
                .addKdoc(field.documentation?.replace("%", "%%").orEmpty())
                .apply {
                    if (strategy == ExtensionGenerationStrategy.EXTENSION)
                        receiver(type.nestedClass("Companion"))
                }
                .delegate(
                    format = "lazy·{·%T(%S, %L)·}",
                    type,
                    fieldName,
                    field.tag
                )
                .build()
        }
    }
}