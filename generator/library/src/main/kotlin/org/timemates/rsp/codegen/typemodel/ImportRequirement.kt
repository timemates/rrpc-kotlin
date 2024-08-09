package org.timemates.rsp.codegen.typemodel

public data class ImportRequirement(
    public val packageName: String,
    public val simpleNames: List<String> = emptyList(),
)