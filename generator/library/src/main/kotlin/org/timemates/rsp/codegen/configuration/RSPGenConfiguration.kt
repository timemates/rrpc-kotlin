package org.timemates.rsp.codegen.configuration

import okio.Path

public data class RSPGenConfiguration(
    public val rootPath: Path,
    public val outputPath: Path,
    public val clientGeneration: Boolean,
    public val serverGeneration: Boolean,
    public val builderTypes: Set<MessageBuilderType>,
    public val permitPackageCycles: Boolean,
)