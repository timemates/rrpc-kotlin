package org.timemates.rrpc.generator.kotlin.adapter.metadata

import com.squareup.kotlinpoet.CodeBlock
import org.timemates.rrpc.generator.kotlin.adapter.internal.ext.newline

internal fun CodeBlock.Builder.addDocumentation(doc: String?) {
    if (doc.isNullOrBlank())
        return

    newline()
    add(
        format = "documentation = %S,",
        doc.replace("\"", "\\\""),
    )
}