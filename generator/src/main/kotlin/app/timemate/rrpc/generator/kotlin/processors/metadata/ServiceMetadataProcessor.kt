package app.timemate.rrpc.generator.kotlin.processors.metadata

import app.timemate.rrpc.generator.GeneratorContext
import app.timemate.rrpc.generator.Processor
import app.timemate.rrpc.generator.kotlin.internal.LibClassNames
import app.timemate.rrpc.generator.kotlin.internal.ext.newline
import app.timemate.rrpc.generator.plugin.api.result.ProcessResult
import app.timemate.rrpc.generator.plugin.api.result.onFailure
import app.timemate.rrpc.generator.plugin.api.result.onSuccess
import app.timemate.rrpc.proto.schema.RSService
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.withIndent

public object ServiceMetadataProcessor : Processor<RSService, CodeBlock> {
    override suspend fun GeneratorContext.process(data: RSService): ProcessResult<CodeBlock> {
        return ProcessResult.Success(
            buildCodeBlock {
                addStatement("%T(", LibClassNames.RS.Service)
                withIndent {
                    addStatement("name = %S,", data.name)
                    add("rpcs = listOf(").withIndent {
                        data.rpcs.forEach { rpc ->
                            newline()
                            RpcMetadataProcessor.process(rpc, this@process).onSuccess {
                                add(it)
                            }.onFailure {
                                return it
                            }
                            add(",")
                        }
                    }
                    newline()
                    add("),")
                    newline()
                    OptionsMetadataProcessor.process(data.options, this@process).onSuccess {
                        add("options = %L,", it)
                    }.onFailure {
                        return it
                    }

                    newline()
                    add(
                        format = "typeUrl = %T(%S),",
                        LibClassNames.RS.Value.TypeUrl,
                        data.typeUrl.value,
                    )
                    newline()
                    add("location = %L,", data.location.codeRepresentation)
                }
                newline()
                add(")")
            }
        )
    }
}