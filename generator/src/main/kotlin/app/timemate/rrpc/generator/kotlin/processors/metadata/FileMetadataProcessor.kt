package app.timemate.rrpc.generator.kotlin.processors.metadata

import app.timemate.rrpc.generator.GeneratorContext
import app.timemate.rrpc.generator.Processor
import app.timemate.rrpc.generator.kotlin.internal.LibClassNames
import app.timemate.rrpc.generator.kotlin.internal.PoetAnnotations
import app.timemate.rrpc.generator.kotlin.internal.ext.newline
import app.timemate.rrpc.generator.kotlin.internal.ext.toUpperCamelCase
import app.timemate.rrpc.generator.plugin.api.result.ProcessResult
import app.timemate.rrpc.generator.plugin.api.result.flatten
import app.timemate.rrpc.generator.plugin.api.result.onFailure
import app.timemate.rrpc.generator.plugin.api.result.onSuccess
import app.timemate.rrpc.proto.schema.RSFile
import com.squareup.kotlinpoet.*

public object FileMetadataProcessor : Processor<RSFile, FileSpec> {
    override suspend fun GeneratorContext.process(data: RSFile): ProcessResult<FileSpec> {
        val packageName = buildString {
            append("app.timemate.rrpc.metadata.generated")
            if (!data.packageName?.value.isNullOrEmpty()) {
                append(".")
                append(data.packageName!!.value)
            }
        }
        val fileName = "${data.name.toUpperCamelCase()}FileMetadata"

        return ProcessResult.Success(
            FileSpec.builder(ClassName(packageName, fileName))
            .addProperty(
                PropertySpec.builder(fileName, LibClassNames.RS.File)
                    .addAnnotation(PoetAnnotations.InternalMetadataApi)
                    .initializer(
                        buildCodeBlock {
                            addStatement("%T(", LibClassNames.RS.File).withIndent {
                                addStatement("name = %S,", data.name)
                                if (data.packageName.value.isNotBlank()) {
                                    addStatement(
                                        "packageName = %T(%S),",
                                        LibClassNames.RS.Value.PackageName,
                                        data.packageName.value
                                    )
                                }
                                OptionsMetadataProcessor.process(data.options, this@process).onSuccess {
                                    addStatement("options = %L,", it)
                                }.onFailure {
                                    return it
                                }
                                if (data.services.isNotEmpty()) {
                                    add("services = listOf(")
                                    withIndent {
                                        data.services.map { service ->
                                            newline()
                                            ServiceMetadataProcessor.process(service, this@process).onSuccess {
                                                add(it)
                                            }
                                        }.flatten().onFailure { return it }
                                    }
                                    newline()
                                    add("),")
                                } else {
                                    add("services = emptyList(),")
                                }
                                newline()
                                if (data.services.isNotEmpty()) {
                                    add("extends = listOf(")
                                    withIndent {
                                        data.extends.map { extend ->
                                            newline()
                                            ExtendMetadataProcessor.process(extend, this@process).onSuccess {
                                                add(it)
                                                add(",")
                                            }
                                        }.flatten().onFailure { return it }
                                    }
                                    newline()
                                    add("),")
                                } else {
                                    add("extends = emptyList(),")
                                }
                                newline()
                                add("location = %L,", data.location.codeRepresentation)
                            }
                            newline()
                            add(")")
                        }
                    )
                    .build()
            ).build()
        )
    }
}