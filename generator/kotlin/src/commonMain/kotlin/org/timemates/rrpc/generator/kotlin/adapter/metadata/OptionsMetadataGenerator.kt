package org.timemates.rrpc.generator.kotlin.adapter.metadata

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.withIndent
import org.timemates.rrpc.codegen.typemodel.LibClassNames
import org.timemates.rrpc.common.schema.RSOption
import org.timemates.rrpc.common.schema.RSOptions
import org.timemates.rrpc.common.schema.RSResolver
import org.timemates.rrpc.generator.kotlin.adapter.internal.ext.newline

internal object OptionsMetadataGenerator {
    fun generate(options: RSOptions, resolver: RSResolver): CodeBlock {
        return buildCodeBlock {
            if (options.list.isEmpty()) {
                add("%T.EMPTY", LibClassNames.RM.Options)
                return@buildCodeBlock
            }
            add("%T(", LibClassNames.RM.Options)
            withIndent {
                newline()
                add("listOf(")
                options.list.forEach { option ->
                    val field = resolver.resolveField(option.fieldUrl) ?: return@forEach
                    withIndent {
                        add("%T(", LibClassNames.RM.Option)
                        withIndent {
                            add("name = %S,", option.name)
                            newline()
                            add("tag = %L,", field.tag)
                            newline()
                            add("fieldUrl = %1T(", LibClassNames.RM.TypeMemberUrl, option.fieldUrl.typeUrl)
                            newline()
                            withIndent {
                                add("typeUrl = %S,", option.fieldUrl.typeUrl)
                                add("memberName = %S,", option.fieldUrl.memberName)
                            }
                            add("),")
                            option.value?.let { value ->
                                add("value = %P,", generateValue(value))
                            }
                        }
                        add("),")
                    }
                }
                add(")")
            }
            newline()
            add(")")
        }
    }

    private fun generateValue(value: RSOption.Value): CodeBlock {
        return buildCodeBlock {
            when (value) {
                is RSOption.Value.Raw -> add("%T(%S)", LibClassNames.RM.OptionValueRaw)
                is RSOption.Value.RawMap -> {
                    add("%T(", LibClassNames.RM.Option)
                    newline()
                    withIndent {
                        add("map = mapOf(")
                        withIndent {
                            value.map.forEach { key, value ->
                                newline()
                                add(
                                    format = "%1T(%2S) to %1T(%3S),",
                                    LibClassNames.RM.OptionValueRaw,
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
                    add("%T(", LibClassNames.RM.OptionValueMessageMap)
                    newline()
                    withIndent {
                        add("map = mapOf(")
                        withIndent {
                            value.map.forEach { key, value ->
                                newline()
                                add(
                                    format = "%1T(%2S, %3S) to %P",
                                    LibClassNames.RM.TypeMemberUrl,
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