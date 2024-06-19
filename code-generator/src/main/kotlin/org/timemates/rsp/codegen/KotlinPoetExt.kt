package org.timemates.rsp.codegen

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec

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

internal fun FunSpec.Builder.deprecated(isDeprecated: Boolean = true): FunSpec.Builder = apply {
    if (isDeprecated)
        addAnnotation(Annotations.Deprecated)
}