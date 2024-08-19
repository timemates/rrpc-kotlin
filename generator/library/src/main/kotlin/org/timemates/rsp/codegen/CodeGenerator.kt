package org.timemates.rsp.codegen

import com.squareup.wire.schema.Location
import com.squareup.wire.schema.SchemaLoader
import okio.FileSystem
import okio.Path
import org.timemates.rsp.codegen.configuration.RSPGenConfiguration
import org.timemates.rsp.codegen.generators.FileGenerator
import kotlin.io.path.absolutePathString

public class CodeGenerator(
    private val fileSystem: FileSystem,
) {
    public fun generate(
        configuration: RSPGenConfiguration
    ): Unit = with(configuration) {
        fileSystem.createDirectories(outputPath)

        val schemaLoader = SchemaLoader(fileSystem)

        schemaLoader.initRoots(listOf(Location.get(rootPath.toNioPath().absolutePathString())))

        val schema = schemaLoader.loadSchema()

        schema.protoFiles
            .filter {
                it.packageName?.startsWith("wire") != true &&
                    it.location.toString() != "google/protobuf/descriptor.proto" &&
                    // wrappers are ignored, because they're generated as usual kotlin types, but nullable
                    it.location.toString() != "google/protobuf/wrappers.proto"
                    // this type already contains in the common-core
                    && it.location.toString() != "google/protobuf/timestamp.proto"
            }
            .map { file ->
                FileGenerator.generateFile(schema, file, clientGeneration, serverGeneration)
            }.forEach { file ->
                file.writeTo(outputPath.toNioPath())
            }
    }
}