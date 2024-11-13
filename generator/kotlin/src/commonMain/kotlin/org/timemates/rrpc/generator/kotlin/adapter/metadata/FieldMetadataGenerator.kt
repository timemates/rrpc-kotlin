package org.timemates.rrpc.generator.kotlin.adapter.metadata

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.withIndent
import org.timemates.rrpc.codegen.typemodel.LibClassNames
import org.timemates.rrpc.common.schema.RSField
import org.timemates.rrpc.common.schema.RSResolver
import org.timemates.rrpc.generator.kotlin.adapter.internal.ext.newline

internal object FieldMetadataGenerator {
    fun generate(field: RSField, resolver: RSResolver): CodeBlock {
        return buildCodeBlock {
            addStatement("%T(", LibClassNames.RS.Field)
            withIndent {
                addStatement("tag = %L,", field.tag)
                addStatement("name = %S,", field.name)
                add(
                    format = "options = %L,",
                    OptionsMetadataGenerator.generate(field.options, resolver)
                )
                addDocumentation(field.documentation)
                newline()
                add(
                    format = "typeUrl = %T(%S),",
                    LibClassNames.RS.Value.TypeUrl,
                    field.typeUrl.value,
                )
                newline()
                add("isRepeated = %L,", field.isRepeated)
                newline()
                add("isInOneOf = %L,", field.isInOneOf)
                newline()
                add("isExtension = %L,", field.isExtension)
            }
            newline()
            add(")")
        }
    }
}