package app.timemate.rrpc.generator.kotlin.processors.metadata

import app.timemate.rrpc.generator.GeneratorContext
import app.timemate.rrpc.generator.Processor
import app.timemate.rrpc.generator.kotlin.internal.LibClassNames
import app.timemate.rrpc.generator.kotlin.internal.ext.addDocumentation
import app.timemate.rrpc.generator.kotlin.internal.ext.newline
import app.timemate.rrpc.generator.plugin.api.result.ProcessResult
import app.timemate.rrpc.generator.plugin.api.result.onFailure
import app.timemate.rrpc.generator.plugin.api.result.onSuccess
import app.timemate.rrpc.proto.schema.RSOneOf
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.withIndent

public object OneOfMetadataProcessor : Processor<RSOneOf, CodeBlock> {
    override suspend fun GeneratorContext.process(data: RSOneOf): ProcessResult<CodeBlock> {
        return ProcessResult.Success(
            buildCodeBlock {
                add("%T(", LibClassNames.RS.OneOf)
                withIndent {
                    addStatement("name = %S", data.name)
                    addDocumentation(data.documentation)
                    addStatement("fields = listOf(")
                    withIndent {
                        data.fields.forEach { field ->
                            newline()
                            FieldMetadataProcessor.process(field, this@process).onSuccess {
                                add(it)
                            }.onFailure {
                                return it
                            }
                            add(",")
                        }
                    }
                    addStatement("),")
                    OptionsMetadataProcessor.process(data.options, this@process).onSuccess {
                        addStatement("options = %L,", it)
                    }.onFailure {
                        return it
                    }
                }
                addStatement(")")
            }
        )
    }
}