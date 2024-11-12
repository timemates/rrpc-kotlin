package org.timemates.rrpc.codegen

import com.squareup.wire.schema.Location
import com.squareup.wire.schema.SchemaLoader
import okio.FileSystem
import org.timemates.rrpc.codegen.adapters.SchemaAdapter
import org.timemates.rrpc.codegen.configuration.GenerationOption
import org.timemates.rrpc.codegen.configuration.GenerationOptions
import org.timemates.rrpc.codegen.configuration.isPackageCyclesPermitted
import org.timemates.rrpc.codegen.configuration.protoInputs

public class CodeGenerator(private val fileSystem: FileSystem = FileSystem.SYSTEM) {
    public companion object {
        public val baseOptions: List<GenerationOption> = listOf(
            GenerationOptions.PROTOS_INPUT,
            GenerationOptions.PERMIT_PACKAGE_CYCLES,
        )
    }

    public fun generate(
        options: GenerationOptions,
        adapters: List<SchemaAdapter>,
    ) {
        val schemaLoader = SchemaLoader(fileSystem)
        schemaLoader.permitPackageCycles = options.isPackageCyclesPermitted

        schemaLoader.initRoots(
            options.protoInputs.map { Location.get(it.toString()) }
        )

        val schema = schemaLoader.loadSchema()
        val resolver = schema.asRSResolver()

        return adapters.forEach { adapter ->
            adapter.process(options, resolver)
        }
    }
}