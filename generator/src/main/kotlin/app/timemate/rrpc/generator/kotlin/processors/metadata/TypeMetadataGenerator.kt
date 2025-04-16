package app.timemate.rrpc.generator.kotlin.processors.metadata

import app.timemate.rrpc.generator.GeneratorContext
import app.timemate.rrpc.generator.Processor
import app.timemate.rrpc.generator.kotlin.internal.LibClassNames
import app.timemate.rrpc.generator.kotlin.internal.ext.addDocumentation
import app.timemate.rrpc.generator.kotlin.internal.ext.newline
import app.timemate.rrpc.generator.plugin.api.RSResolver
import app.timemate.rrpc.generator.plugin.api.result.ProcessResult
import app.timemate.rrpc.generator.plugin.api.result.onFailure
import app.timemate.rrpc.generator.plugin.api.result.onSuccess
import app.timemate.rrpc.proto.schema.*
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.withIndent
import com.sun.tools.javac.code.TypeMetadata

internal object TypeMetadataGenerator : Processor<RSType, CodeBlock> {
    override suspend fun GeneratorContext.process(data: RSType): ProcessResult<CodeBlock> {
        return when (data) {
            is RSEnclosingType -> generateEnclosing(data)
            is RSEnum -> generateEnum(data)
            is RSMessage -> generateMessage(data)
        }
    }

    private suspend fun GeneratorContext.generateMessage(message: RSMessage): ProcessResult<CodeBlock> {
        return ProcessResult.Success(
            buildCodeBlock {
                add("%T(", LibClassNames.RS.Enum)
                withIndent {
                    addStatement("name = %S", message.name)
                    addDocumentation(message.documentation)
                    add("fields = listOf(")
                    withIndent {
                        message.fields.forEach { field ->
                            newline()
                            FieldMetadataProcessor.process(field, this@generateMessage).onSuccess {
                                add(it)
                            }.onFailure {
                                return it
                            }
                            add(",")
                        }
                    }
                    addStatement("),")
                    addStatement("oneOfs = listOf(")
                    withIndent {
                        message.oneOfs.forEach { oneOf ->
                            newline()
                            OneOfMetadataProcessor.process(oneOf, this@generateMessage).onSuccess {
                                add(it)
                            }.onFailure {
                                return it
                            }
                        }
                    }
                    addStatement("),")
                    OptionsMetadataProcessor.process(message.options, this@generateMessage).onSuccess {
                        addStatement("options = %L", it)
                    }.onFailure {
                        return it
                    }
                    addStatement("typeUrl = %T(%S)", LibClassNames.RS.Value.TypeUrl, message.typeUrl)
                    addNestedTypes(message.nestedTypes, resolver, this@generateMessage).onFailure {
                        return it
                    }
                    addNestedExtends(message.nestedExtends, this@generateMessage).onFailure {
                        return it
                    }
                    add("location = %L,", message.location.codeRepresentation)
                    newline()
                }
                addStatement(")")
            }
        )
    }

    private suspend fun GeneratorContext.generateEnclosing(enclosing: RSEnclosingType): ProcessResult<CodeBlock> {
        return ProcessResult.Success(
            buildCodeBlock {
                add("%T(", LibClassNames.RS.Enum)
                withIndent {
                    addStatement("name = %S,", enclosing.name)
                    addDocumentation(enclosing.documentation)
                    addStatement("typeUrl = %T(%S),", LibClassNames.RS.Value.TypeUrl, enclosing.typeUrl)
                    addNestedTypes(enclosing.nestedTypes, resolver, this@generateEnclosing).onFailure {
                        return it
                    }
                    addNestedExtends(enclosing.nestedExtends, this@generateEnclosing).onFailure {
                        return it
                    }
                }
                add(")")
            }
        )
    }

    private suspend fun GeneratorContext.generateEnum(enum: RSEnum): ProcessResult<CodeBlock> {
        return ProcessResult.Success(
            buildCodeBlock {
                add("%T(", LibClassNames.RS.Enum)
                withIndent {
                    newline()
                    add("name = %S", enum.name)
                    newline()
                    add("constants = listOf(")
                    withIndent {
                        enum.constants.forEach { constant ->
                            newline()
                            add("%T(")
                            withIndent {
                                newline()
                                add("name = %S,", constant.name)
                                newline()
                                add("tag = %L,", constant.tag)
                                newline()
                                OptionsMetadataProcessor.process(enum.options, this@generateEnum).onSuccess {
                                    add("options = %L", it)
                                }.onFailure {
                                    return it
                                }
                                addDocumentation(constant.documentation)
                            }
                            newline()
                            add("),")
                        }
                    }
                    newline()
                    add("),")
                    addDocumentation(enum.documentation)
                    OptionsMetadataProcessor.process(enum.options, this@generateEnum).onSuccess {
                        addStatement("options = %L", it)
                    }.onFailure {
                        return it
                    }
                    addNestedTypes(enum.nestedTypes, resolver, this@generateEnum).onFailure {
                        return it
                    }
                    addNestedExtends(enum.nestedExtends, this@generateEnum).onFailure {
                        return it
                    }
                    addStatement(
                        format = "typeUrl = %T(%S),",
                        LibClassNames.RS.Value.TypeUrl,
                        enum.typeUrl.value
                    )
                }
                add(")")
            }
        )
    }

    private suspend fun CodeBlock.Builder.addNestedTypes(nestedTypes: List<RSType>, resolver: RSResolver, context: GeneratorContext): ProcessResult<Unit> {
        addStatement("nestedTypes = listOf(")
        withIndent {
            nestedTypes.forEach { nestedType ->
                newline()
                process(nestedType, context).onSuccess {
                    add(it)
                }.onFailure {
                    return it
                }
                add(",")
            }
        }
        add("),")
        return ProcessResult.Success(Unit)
    }

    private suspend fun CodeBlock.Builder.addNestedExtends(nestedExtends: List<RSExtend>, context: GeneratorContext): ProcessResult<Unit> {
        addStatement("nestedExtends = listOf(")
        withIndent {
            nestedExtends.forEach { nested ->
                newline()
                ExtendMetadataProcessor.process(nested, context).onSuccess {
                    add(it)
                }.onFailure {
                    return it
                }
                add(",")
            }
        }
        add("),")
        return ProcessResult.Success(Unit)
    }
}