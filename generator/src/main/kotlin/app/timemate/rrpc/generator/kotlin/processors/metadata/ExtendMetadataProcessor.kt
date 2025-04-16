package app.timemate.rrpc.generator.kotlin.processors.metadata

import app.timemate.rrpc.generator.GeneratorContext
import app.timemate.rrpc.generator.Processor
import app.timemate.rrpc.generator.kotlin.internal.LibClassNames
import app.timemate.rrpc.generator.kotlin.internal.ext.newline
import app.timemate.rrpc.generator.plugin.api.result.ProcessResult
import app.timemate.rrpc.generator.plugin.api.result.onFailure
import app.timemate.rrpc.generator.plugin.api.result.onSuccess
import app.timemate.rrpc.proto.schema.RSExtend
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.withIndent

public object ExtendMetadataProcessor : Processor<RSExtend, CodeBlock> {
    override suspend fun GeneratorContext.process(data: RSExtend): ProcessResult<CodeBlock> {
        return ProcessResult.Success(
            buildCodeBlock {
                addStatement("%T(", LibClassNames.RS.Extend)
                withIndent {
                    addStatement(
                        format = "typeUrl = %1T(%2S),",
                        LibClassNames.RS.Value.TypeUrl,
                        data.typeUrl.value,
                    )
                    addStatement("name = %S,", data.name)
                    add("fields = listOf(")
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
                    newline()
                    add("),")
                    if (data.documentation.isNotBlank()) {
                        newline()
                        add(
                            format = "documentation = %S,",
                            data.documentation,
                        )
                    }
                }
                newline()
                add(")")
            }
        )
    }
}