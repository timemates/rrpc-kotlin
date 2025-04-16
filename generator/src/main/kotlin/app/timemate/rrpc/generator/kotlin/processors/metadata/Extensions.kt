package app.timemate.rrpc.generator.kotlin.processors.metadata

import app.timemate.rrpc.generator.kotlin.internal.LibClassNames
import app.timemate.rrpc.generator.kotlin.internal.ext.newline
import app.timemate.rrpc.proto.schema.RSElementLocation
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.buildCodeBlock

public val RSElementLocation.codeRepresentation: CodeBlock get() {
    if (this == RSElementLocation.UNKNOWN)
        return CodeBlock.of("%T.UNKNOWN", LibClassNames.RS.ElementLocation)

    return buildCodeBlock {
        add("%T(", LibClassNames.RS.ElementLocation)
        indent()
        newline()
        add("basePath = %T.EMPTY,", LibClassNames.RS.Value.LocationPath)
        newline()
        add("relativePath = %1T(%2S),", LibClassNames.RS.Value.LocationPath, relativePath)
        newline()
        add("line = %L,", line)
        newline()
        add("column = %L,", column)
        unindent()
        newline()
        add(")")
    }
}