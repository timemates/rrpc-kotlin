package org.timemates.rsp.codegen.ext

import com.squareup.kotlinpoet.*
import org.timemates.rsp.codegen.typemodel.Annotations
import org.timemates.rsp.codegen.typemodel.ImportRequirement

/**
 * Adds a list of TypeSpec to the FileSpec.
 *
 * @param types the list of TypeSpec to add.
 * @return the modified FileSpec.Builder.
 */
internal fun FileSpec.Builder.addTypes(types: List<TypeSpec>): FileSpec.Builder = apply {
    types.forEach {
        addType(it)
    }
}

internal fun FileSpec.Builder.addImports(imports: List<ImportRequirement>): FileSpec.Builder = apply {
    imports.forEach {
        addImport(it.packageName, it.simpleNames)
    }
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

internal fun CodeBlock.Builder.addAllSeparated(codeBlocks: Iterable<CodeBlock>, separator: String = ",\n"): CodeBlock.Builder = apply {
    codeBlocks.forEach {
        add(it)
        add(separator)
    }
}

internal fun FunSpec.Builder.deprecated(deprecated: Boolean = true): FunSpec.Builder = apply {
    if (deprecated)
        addAnnotation(Annotations.Deprecated)
}

internal fun ClassName.asImportRequirement(): ImportRequirement = ImportRequirement(
    packageName,
    simpleNames,
)

internal fun CodeBlock.Builder.newline(before: String = ""): CodeBlock.Builder = apply {
    add("$before\n")
}