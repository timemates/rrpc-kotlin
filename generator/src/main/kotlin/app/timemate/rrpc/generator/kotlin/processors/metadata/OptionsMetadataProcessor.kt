package app.timemate.rrpc.generator.kotlin.processors.metadata

import app.timemate.rrpc.generator.GeneratorContext
import app.timemate.rrpc.generator.Processor
import app.timemate.rrpc.generator.kotlin.error.UnresolvableDeclarationMemberError
import app.timemate.rrpc.generator.kotlin.internal.LibClassNames
import app.timemate.rrpc.generator.kotlin.internal.ext.newline
import app.timemate.rrpc.generator.plugin.api.result.ProcessResult
import app.timemate.rrpc.proto.schema.RSOption
import app.timemate.rrpc.proto.schema.RSOptions
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.withIndent

public object OptionsMetadataProcessor : Processor<RSOptions, CodeBlock> {
    override suspend fun GeneratorContext.process(data: RSOptions): ProcessResult<CodeBlock> {
        return ProcessResult.Success(
            buildCodeBlock {
                if (data.list.isEmpty()) {
                    add("%T.EMPTY", LibClassNames.RS.Options)
                    return@buildCodeBlock
                }

                add("%T(", LibClassNames.RS.Options)
                withIndent {
                    newline()
                    add("listOf(")
                    data.list.forEach { option ->
                        withIndent {
                            newline()
                            add("%T(", LibClassNames.RS.Option)
                            withIndent {
                                newline()
                                add("name = %S,", option.name)
                                newline()
                                add("fieldUrl = %T(", LibClassNames.RS.TypeMemberUrl)
                                withIndent {
                                    newline()
                                    add("typeUrl = %1T(%2S),", LibClassNames.RS.Value.TypeUrl, option.fieldUrl.typeUrl)
                                    newline()
                                    add("memberName = %S,", option.fieldUrl.memberName)
                                }
                                newline()
                                add("),")
                                newline()
                                option.value?.let { value ->
                                    add("value = %L,", generateValue(value))
                                }
                            }
                            newline()
                            add("),")
                        }
                    }
                    newline()
                    add(")")
                }
                newline()
                add(")")
            }
        )
    }

    private fun generateValue(value: RSOption.Value): CodeBlock {
        return buildCodeBlock {
            when (value) {
                is RSOption.Value.Raw -> add("%T(%S)", LibClassNames.RS.OptionValueRaw, value.string)
                is RSOption.Value.RawMap -> {
                    add("%T(", LibClassNames.RS.Option)
                    newline()
                    withIndent {
                        add("map = mapOf(")
                        withIndent {
                            value.map.forEach { key, value ->
                                newline()
                                add(
                                    format = "%1T(%2S) to %1T(%3S),",
                                    LibClassNames.RS.OptionValueRaw,
                                    key.string,
                                    value.string,
                                )
                            }
                        }
                        add("),")
                    }
                    add("),")
                }

                is RSOption.Value.MessageMap -> {
                    add("%T(", LibClassNames.RS.OptionValueMessageMap)
                    newline()
                    withIndent {
                        add("map = mapOf(")
                        withIndent {
                            value.map.forEach { key, value ->
                                newline()
                                add(
                                    format = "%1T(%2S, %3S) to %P",
                                    LibClassNames.RS.TypeMemberUrl,
                                    key.typeUrl,
                                    key.memberName,
                                    generateValue(value)
                                )
                                add(")")
                            }
                        }
                        add("),")
                    }
                }
            }
        }
    }
}