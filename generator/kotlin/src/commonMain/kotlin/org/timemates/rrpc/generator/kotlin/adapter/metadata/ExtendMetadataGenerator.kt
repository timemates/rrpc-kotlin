package org.timemates.rrpc.generator.kotlin.adapter.metadata

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.withIndent
import org.timemates.rrpc.codegen.typemodel.LibClassNames
import org.timemates.rrpc.common.schema.RSExtend
import org.timemates.rrpc.common.schema.RSResolver
import org.timemates.rrpc.generator.kotlin.adapter.internal.ext.newline

internal object ExtendMetadataGenerator {
    fun generate(extend: RSExtend, resolver: RSResolver): CodeBlock {
        return buildCodeBlock {
            addStatement("%T(", LibClassNames.RS.Extend)
            withIndent {
                addStatement(
                    format = "typeUrl = %1T(%2S),",
                    LibClassNames.RS.Value.TypeUrl,
                    extend.typeUrl.value,
                )
                addStatement("name = %S,", extend.name)
                add("fields = listOf(")
                withIndent {
                    extend.fields.forEach { field ->
                        newline()
                        add(FieldMetadataGenerator.generate(field, resolver))
                        add(",")
                    }
                }
                newline()
                add("),")
                newline()
                add(
                    format = "documentation = %L,",
                    if (extend.documentation != null) "null" else "\"${extend.documentation}\""
                )
            }
            newline()
            add(")")
        }
    }
}