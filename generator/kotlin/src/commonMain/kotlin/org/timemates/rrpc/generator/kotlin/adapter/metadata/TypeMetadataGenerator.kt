package org.timemates.rrpc.generator.kotlin.adapter.metadata

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.withIndent
import org.timemates.rrpc.codegen.typemodel.LibClassNames
import org.timemates.rrpc.common.schema.RSExtend
import org.timemates.rrpc.common.schema.RSResolver
import org.timemates.rrpc.common.schema.RSType
import org.timemates.rrpc.generator.kotlin.adapter.internal.ext.newline

internal object TypeMetadataGenerator {
    fun generate(type: RSType, resolver: RSResolver): CodeBlock {
        return when (type) {
            is RSType.Enclosing -> generateEnclosing(type, resolver)
            is RSType.Enum -> generateEnum(type, resolver)
            is RSType.Message -> generateMessage(type, resolver)
        }
    }

    private fun generateMessage(message: RSType.Message, resolver: RSResolver): CodeBlock {
        return buildCodeBlock {
            add("%T(", LibClassNames.RS.Enum)
            withIndent {
                addStatement("name = %S", message.name)
                addDocumentation(message.documentation)
                add("fields = listOf(")
                withIndent {
                    message.fields.forEach { field ->
                        newline()
                        add(FieldMetadataGenerator.generate(field, resolver))
                        add(",")
                    }
                }
                addStatement("),")
                addStatement("oneOfs = listOf(")
                withIndent {
                    message.oneOfs.forEach { oneOf ->
                        newline()
                        add(OneOfMetadataGenerator.generate(oneOf, resolver))
                    }
                }
                addStatement("),")
                addStatement("options = %P", OptionsMetadataGenerator.generate(message.options, resolver))
                addStatement("typeUrl = %T(%S)", LibClassNames.RS.Value.TypeUrl, message.typeUrl)
                addNestedTypes(message.nestedTypes, resolver)
                addNestedExtends(message.nestedExtends, resolver)
            }
            addStatement(")")
        }
    }

    private fun generateEnclosing(enclosing: RSType.Enclosing, resolver: RSResolver): CodeBlock {
        return buildCodeBlock {
            add("%T(", LibClassNames.RS.Enum)
            withIndent {
                addStatement("name = %S,", enclosing.name)
                addDocumentation(enclosing.documentation)
                addStatement("typeUrl = %T(%S),", LibClassNames.RS.Value.TypeUrl, enclosing.typeUrl)
                addNestedTypes(enclosing.nestedTypes, resolver)
                addNestedExtends(enclosing.nestedExtends, resolver)
            }
            add(")")
        }
    }

    private fun generateEnum(enum: RSType.Enum, resolver: RSResolver): CodeBlock {
        return buildCodeBlock {
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
                            add("options = %P,", OptionsMetadataGenerator.generate(constant.options, resolver))
                            addDocumentation(constant.documentation)
                        }
                        newline()
                        add("),")
                    }
                }
                newline()
                add("),")
                addDocumentation(enum.documentation)
                addStatement("options = %P,", OptionsMetadataGenerator.generate(enum.options, resolver))
                addNestedTypes(enum.nestedTypes, resolver)
                addNestedExtends(enum.nestedExtends, resolver)
                addStatement(
                    format = "typeUrl = %T(%S),",
                    LibClassNames.RS.Value.TypeUrl,
                    enum.typeUrl.value
                )
            }
            add(")")
        }
    }

    private fun CodeBlock.Builder.addNestedTypes(nestedTypes: List<RSType>, resolver: RSResolver) {
        addStatement("nestedTypes = listOf(")
        withIndent {
            nestedTypes.forEach { nestedType ->
                newline()
                add(generate(nestedType, resolver))
                add(",")
            }
        }
        add("),")
    }

    private fun CodeBlock.Builder.addNestedExtends(nestedExtends: List<RSExtend>, resolver: RSResolver) {
        addStatement("nestedExtends = listOf(")
        withIndent {
            nestedExtends.forEach { nested ->
                newline()
                add(ExtendMetadataGenerator.generate(nested, resolver))
                add(",")
            }
        }
        add("),")
    }
}
