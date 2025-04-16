package app.timemate.rrpc.generator.kotlin.processors.metadata

import app.timemate.rrpc.generator.GeneratorContext
import app.timemate.rrpc.generator.Processor
import app.timemate.rrpc.generator.kotlin.internal.LibClassNames
import app.timemate.rrpc.generator.kotlin.internal.ext.newline
import app.timemate.rrpc.generator.plugin.api.result.ProcessResult
import app.timemate.rrpc.generator.plugin.api.result.onFailure
import app.timemate.rrpc.generator.plugin.api.result.onSuccess
import app.timemate.rrpc.proto.schema.RSRpc
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.withIndent

public object RpcMetadataProcessor : Processor<RSRpc, CodeBlock> {

    override suspend fun GeneratorContext.process(data: RSRpc): ProcessResult<CodeBlock> {
        return ProcessResult.Success(
            buildCodeBlock {
                add("%T(", LibClassNames.RS.Rpc)
                withIndent {
                    newline()
                    addStatement("name = %S,", data.name)
                    addStatement(
                        format = "requestType = %1T(isStreaming = %2L, %3T(%4S)),",
                        LibClassNames.RS.StreamableTypeUrl,
                        data.requestType.isStreaming,
                        LibClassNames.RS.Value.TypeUrl,
                        data.requestType.type.value
                    )
                    addStatement(
                        format = "responseType = %1T(isStreaming = %2L, %3T(%4S)),",
                        LibClassNames.RS.StreamableTypeUrl,
                        data.responseType.isStreaming,
                        LibClassNames.RS.Value.TypeUrl,
                        data.responseType.type.value
                    )
                    OptionsMetadataProcessor.process(data.options, this@process).onSuccess {
                        addStatement("options = %L,", it)
                    }.onFailure {
                        return it
                    }
                    if (data.documentation.isNotBlank()) {
                        addStatement(
                            "documentation = %S,",
                            data.documentation
                        )
                    }
                    addStatement("location = %L,", data.location.codeRepresentation)
                }
                add(")")
            }
        )
    }
}