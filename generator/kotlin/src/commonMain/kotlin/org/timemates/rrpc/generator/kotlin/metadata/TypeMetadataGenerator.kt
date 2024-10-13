package org.timemates.rrpc.generator.kotlin.metadata

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.withIndent
import org.timemates.rrpc.codegen.typemodel.Types
import org.timemates.rrpc.common.metadata.RMExtend
import org.timemates.rrpc.common.metadata.RMType
import org.timemates.rrpc.generator.kotlin.ext.newline

internal object TypeMetadataGenerator {
    fun generate(type: RMType): CodeBlock {
        return when (type) {
            is RMType.Enclosing -> generateEnclosing(type)
            is RMType.Enum -> generateEnum(type)
            is RMType.Message -> generateMessage(type)
        }
    }

    private fun generateMessage(message: RMType.Message): CodeBlock {
        return buildCodeBlock {
            add("%T(", Types.RM.Enum)
            withIndent {
                addStatement("name = %S", message.name)
                addDocumentation(message.documentation)
                addStatement("fields = listOf(")
                withIndent {
                    message.fields.forEach { field ->
                        newline()
                        add(FieldMetadataGenerator.generate(field))
                        add(",")
                    }
                }
                addStatement("),")
                addStatement("oneOfs = listOf(")
                withIndent {
                    message.oneOfs.forEach { oneOf ->
                        newline()
                        add(OneOfMetadataGenerator.generate(oneOf))
                    }
                }
                addStatement("),")
                addStatement("options = %P", OptionsMetadataGenerator.generate(message.options))
                addStatement("typeUrl = %T(%S)", Types.RM.Value.TypeUrl, message.typeUrl)
                addNestedTypes(message.nestedTypes)
                addNestedExtends(message.nestedExtends)
            }
            addStatement(")")
        }
    }

    private fun generateEnclosing(enclosing: RMType.Enclosing): CodeBlock {
        return buildCodeBlock {
            add("%T(", Types.RM.Enum)
            withIndent {
                addStatement("name = %S,", enclosing.name)
                addDocumentation(enclosing.documentation)
                addStatement("typeUrl = %T(%S),", Types.RM.Value.TypeUrl, enclosing.typeUrl)
                addNestedTypes(enclosing.nestedTypes)
                addNestedExtends(enclosing.nestedExtends)
            }
            add(")")
        }
    }

    private fun generateEnum(enum: RMType.Enum): CodeBlock {
        return buildCodeBlock {
            add("%T(", Types.RM.Enum)
            withIndent {
                addStatement("name = %S", enum.name)
                addStatement("constants = listOf(")
                withIndent {
                    enum.constants.forEach { constant ->
                        newline()
                        add("%T(")
                        withIndent {
                            addStatement("name = %S,", constant.name)
                            addStatement("tag = %L,", constant.tag)
                            addStatement("options = %P,", OptionsMetadataGenerator.generate(constant.options))
                            addStatement(
                                format = "documentation = %L,",
                                if (constant.documentation == null) "null" else "\"${constant.documentation}\""
                            )
                        }
                        add("),")
                    }
                }
                add("),")
                addDocumentation(enum.documentation)
                addStatement("options = %P,", OptionsMetadataGenerator.generate(enum.options))
                addNestedTypes(enum.nestedTypes)
                addNestedExtends(enum.nestedExtends)
                addStatement(
                    format = "typeUrl = %T(%S),",
                    Types.RM.Value.TypeUrl,
                    enum.typeUrl.value
                )
            }
            add(")")
        }
    }

    private fun CodeBlock.Builder.addNestedTypes(nestedTypes: List<RMType>) {
        addStatement("nestedTypes = listOf(")
        withIndent {
            nestedTypes.forEach { nestedType ->
                newline()
                add(generate(nestedType))
                add(",")
            }
        }
        add("),")
    }

    private fun CodeBlock.Builder.addNestedExtends(nestedExtends: List<RMExtend>) {
        addStatement("nestedExtends = listOf(")
        withIndent {
            nestedExtends.forEach { nested ->
                newline()
                add(ExtendMetadataGenerator.generate(nested))
                add(",")
            }
        }
        add("),")
    }
}
