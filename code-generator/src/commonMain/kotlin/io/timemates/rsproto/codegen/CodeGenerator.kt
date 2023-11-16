package io.timemates.rsproto.codegen

import com.squareup.wire.schema.Location
import com.squareup.wire.schema.SchemaLoader
import okio.FileSystem
import okio.Path

public class CodeGenerator(
    private val fileSystem: FileSystem,
) {
    public fun generate(
        rootPath: Path,
        outputPath: Path,
        clientGeneration: Boolean,
        serverGeneration: Boolean,
    ) {
        val files = fileSystem.listRecursively(rootPath)
            .filter { fileSystem.metadata(it).isRegularFile }
            .toList()

        fileSystem.createDirectories(outputPath)

        val sourcePath = files.map { path ->
            Location(rootPath.name, path = path.name)
        }

        val schemaLoader = SchemaLoader(fileSystem)

        schemaLoader.initRoots(sourcePath)

        schemaLoader.loadSchema()
            .protoFiles
            .map { file ->
                FileTransformer.transform(file, clientGeneration, serverGeneration)
            }.forEach { file ->
                val path = outputPath.resolve(file.packageName.replace('.', '/'))
                    .toNioPath()

                file.writeTo(path)
            }
    }
}