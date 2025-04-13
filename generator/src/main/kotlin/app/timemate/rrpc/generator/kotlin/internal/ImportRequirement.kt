package app.timemate.rrpc.generator.kotlin.internal

import app.timemate.rrpc.proto.schema.value.RSPackageName

public data class ImportRequirement(
    public val packageName: RSPackageName,
    public val simpleNames: List<String> = emptyList(),
)