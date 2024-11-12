package org.timemates.rrpc.generator.kotlin.adapter.metadata

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.withIndent
import org.timemates.rrpc.codegen.typemodel.LibClassNames
import org.timemates.rrpc.common.schema.RSField
import org.timemates.rrpc.common.schema.RSResolver

internal object FieldMetadataGenerator {
    fun generate(field: RSField, resolver: RSResolver): CodeBlock {
        return buildCodeBlock {
            addStatement("%T(", LibClassNames.RM.Field)
            withIndent {
                addStatement("tag = %L,", field.tag)
                addStatement("name = %S,", field.name)
                addStatement(
                    format = "options = %L,",
                    OptionsMetadataGenerator.generate(field.options, resolver)
                )
                addStatement(
                    "documentation = %L,",
                    if (field.documentation == null) "null" else "\"${field.documentation}\""
                )
                addStatement(
                    format = "typeUrl = %T(%S),",
                    LibClassNames.RM.Value.TypeUrl,
                    field.typeUrl.value,
                )
                addStatement("isRepeated = %L,", field.isRepeated)
                addStatement("isInOneOf = %L,", field.isInOneOf)
                addStatement("isExtension = %L,", field.isExtension)
            }
            add(")")
        }
    }
}