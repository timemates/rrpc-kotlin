package io.timemates.rsproto.plugin

import io.timemates.rsproto.codegen.CodeGenerator
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import kotlin.math.log

public class RSocketProtoGeneratorPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.extensions.create<RSProtoExtension>("rsproto")

        val generationTask = target.tasks.create("generateProto") {
            group = "rsproto"

            inputs.dir(extension.protoSourcePath)
            outputs.dir(extension.generationOutputPath)

            doLast {
                val codeGenerator = CodeGenerator(FileSystem.SYSTEM)

                codeGenerator.generate(
                    rootPath = target.file(extension.protoSourcePath).toOkioPath(),
                    outputPath = target.layout.buildDirectory.asFile.get().toOkioPath()
                        .resolve(extension.generationOutputPath),
                    clientGeneration = extension.clientGeneration,
                    serverGeneration = extension.serverGeneration,
                )
            }
        }

        target.tasks.withType<KotlinCompile> {
            dependsOn(generationTask)
        }
    }
}