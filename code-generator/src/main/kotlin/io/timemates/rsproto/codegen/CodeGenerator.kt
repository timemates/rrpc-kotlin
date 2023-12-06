package io.timemates.rsproto.codegen

import com.squareup.wire.schema.Location
import com.squareup.wire.schema.SchemaLoader
import okio.FileSystem
import okio.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.name

public class CodeGenerator(
    private val fileSystem: FileSystem,
) {
    public fun generate(
        rootPath: Path,
        outputPath: Path,
        clientGeneration: Boolean,
        serverGeneration: Boolean,
    ) {
        fileSystem.createDirectories(outputPath)

        val schemaLoader = SchemaLoader(fileSystem)

        schemaLoader.initRoots(listOf(Location.get(rootPath.toNioPath().absolutePathString())))

        val schema = schemaLoader.loadSchema()

        schema.protoFiles
            .filter { it.packageName?.startsWith("wire") != true }
            .map { file ->
                FileTransformer.transform(schema, file, clientGeneration, serverGeneration)
            }.forEach { file ->
                file.writeTo(outputPath.toNioPath())
            }
    }
}