package app.timemate.rrpc.generator.kotlin.internal.ext

import com.squareup.kotlinpoet.*
import app.timemate.rrpc.proto.schema.value.RSPackageName
import app.timemate.rrpc.generator.kotlin.internal.PoetAnnotations
import app.timemate.rrpc.generator.kotlin.internal.ImportRequirement

internal fun FileSpec.Builder.addImports(imports: List<ImportRequirement>): FileSpec.Builder = apply {
    imports.forEach {
        addImport(it.packageName.value, it.simpleNames)
    }
}

internal fun FileSpec.Builder.addImport(import: ImportRequirement): FileSpec.Builder = apply {
    addImport(import.packageName.value, import.simpleNames)
}

/**
 * Adds enum constants to the TypeSpec.Builder.
 *
 * @param names The names of the enum constants to be added.
 */
internal fun TypeSpec.Builder.addEnumConstants(vararg names: String) {
    names.forEach {
        addEnumConstant(it)
    }
}

internal fun CodeBlock.Builder.addAllSeparated(
    codeBlocks: Iterable<CodeBlock>,
    separator: String = ",\n",
): CodeBlock.Builder = apply {
    codeBlocks.forEach {
        add(it)
        add(separator)
    }
}

internal fun FunSpec.Builder.deprecated(deprecated: Boolean = true): FunSpec.Builder = apply {
    if (deprecated)
        addAnnotation(PoetAnnotations.Deprecated)
}

internal fun ClassName.asImportRequirement(): ImportRequirement = ImportRequirement(
    RSPackageName(packageName),
    simpleNames,
)

internal fun CodeBlock.Builder.newline(before: String = ""): CodeBlock.Builder = apply {
    add("$before\n")
}

internal fun CodeBlock.Builder.addDocumentation(doc: String?) {
    if (doc.isNullOrBlank())
        return

    newline()
    add(
        format = "documentation = %S,",
        doc.replace("\"", "\\\""),
    )
}

internal val List<String>.codeRepresentation: CodeBlock get() {
    if (isEmpty()) return CodeBlock.of("emptyList()")
    return buildCodeBlock {
        add("listOf(")
        indent()
        forEach {
            newline()
            add("%S,", it)
            newline()
        }
        unindent()
        add(")")
    }
}