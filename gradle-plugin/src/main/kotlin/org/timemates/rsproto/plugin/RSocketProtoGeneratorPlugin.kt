package org.timemates.rsproto.plugin

import org.timemates.rsproto.codegen.CodeGenerator
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File
import kotlin.math.log

public class RSocketProtoGeneratorPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.extensions.create<RSProtoExtension>("rsproto", target.objects)

        val generationTask = target.tasks.create("generateProto") {
            group = "rsproto"

            inputs.dir(extension.protoSourcePath)
            outputs.dir(extension.generationOutputPath)

            doLast {
                val codeGenerator = CodeGenerator(FileSystem.SYSTEM)

                codeGenerator.generate(
                    rootPath = target.file(extension.protoSourcePath.get()).toOkioPath(),
                    outputPath = target.file(extension.generationOutputPath.get()).toOkioPath(),
                    clientGeneration = extension.clientGeneration.get(),
                    serverGeneration = extension.serverGeneration.get(),
                )
            }
        }

        target.tasks.withType<KotlinCompile> {
            dependsOn(generationTask)
        }
    }
}