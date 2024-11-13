package org.timemates.rrpc.generator.kotlin.adapter.metadata

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.withIndent
import org.timemates.rrpc.codegen.typemodel.LibClassNames
import org.timemates.rrpc.common.schema.RSOneOf
import org.timemates.rrpc.common.schema.RSResolver
import org.timemates.rrpc.generator.kotlin.adapter.internal.ext.newline

internal object OneOfMetadataGenerator {
    fun generate(oneOf: RSOneOf, resolver: RSResolver): CodeBlock {
        return buildCodeBlock {
            add("%T(", LibClassNames.RS.OneOf)
            withIndent {
                addStatement("name = %S", oneOf.name)
                addDocumentation(oneOf.documentation)
                addStatement("fields = listOf(")
                withIndent {
                    oneOf.fields.forEach { field ->
                        newline()
                        add(FieldMetadataGenerator.generate(field, resolver))
                        add(",")
                    }
                }
                addStatement("),")
                addStatement("options = %P", OptionsMetadataGenerator.generate(oneOf.options, resolver))
            }
            addStatement(")")
        }
    }
}