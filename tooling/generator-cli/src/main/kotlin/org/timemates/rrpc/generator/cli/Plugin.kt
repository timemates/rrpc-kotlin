package org.timemates.rrpc.generator.cli

import org.timemates.rrpc.codegen.plugin.GeneratorCommunication

data class Plugin(
    val name: String,
    val communication: GeneratorCommunication,
    val process: Process,
)