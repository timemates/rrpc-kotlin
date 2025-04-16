package app.timemate.rrpc.generator.kotlin.processors.metadata

import app.timemate.rrpc.generator.GeneratorContext
import app.timemate.rrpc.generator.Processor
import app.timemate.rrpc.generator.kotlin.internal.LibClassNames
import app.timemate.rrpc.generator.kotlin.internal.ext.addDocumentation
import app.timemate.rrpc.generator.kotlin.internal.ext.codeRepresentation
import app.timemate.rrpc.generator.kotlin.internal.ext.newline
import app.timemate.rrpc.generator.plugin.api.result.ProcessResult
import app.timemate.rrpc.generator.plugin.api.result.onFailure
import app.timemate.rrpc.generator.plugin.api.result.onSuccess
import app.timemate.rrpc.proto.schema.RSField
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.withIndent

public object FieldMetadataProcessor : Processor<RSField, CodeBlock> {
    override suspend fun GeneratorContext.process(data: RSField): ProcessResult<CodeBlock> {
        return ProcessResult.Success(
            buildCodeBlock {
                addStatement("%T(", LibClassNames.RS.Field)
                withIndent {
                    addStatement("tag = %L,", data.tag)
                    addStatement("name = %S,", data.name)
                    OptionsMetadataProcessor.process(data.options, this@process)
                        .onSuccess {
                            add(
                                format = "options = %L,",
                                it,
                            )
                        }.onFailure { return it }
                    addDocumentation(data.documentation)
                    newline()
                    add(
                        format = "typeUrl = %T(%S),",
                        LibClassNames.RS.Value.TypeUrl,
                        data.typeUrl.value,
                    )
                    newline()
                    add("label = %T.%L,", LibClassNames.RS.Value.FieldLabel, data.label.toString())
                    newline()
                    add("isExtension = %L,", data.isExtension)
                    newline()
                    add("location = %L,", data.location.codeRepresentation)
                    newline()
                    add(
                        "namespacesList = %L,",
                        data.namespaces
                            ?.packageName
                            ?.value
                            ?.let { listOf(it) }
                            ?.plus(data.namespaces!!.simpleNames)
                            .orEmpty()
                            .filter { it.isNotBlank() }
                            .codeRepresentation,
                    )
                }
                newline()
                add(")")
            }
        )
    }
}