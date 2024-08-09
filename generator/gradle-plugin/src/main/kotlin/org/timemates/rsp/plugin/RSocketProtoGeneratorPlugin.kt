package org.timemates.rsp.plugin

import okio.FileSystem
import okio.Path.Companion.toOkioPath
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.timemates.rsp.codegen.CodeGenerator
import org.timemates.rsp.codegen.configuration.RSPGenConfiguration
import java.io.File

public class RSocketProtoGeneratorPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.extensions.create<RSProtoExtension>("rsp", target.objects)
        val generationOutputPath = target.layout.buildDirectory.file(extension.paths.generationOutput)

        val generateProto = target.tasks.create("generateCode") {
            group = "rsp"

            inputs.dir(extension.paths.protoSources)
            outputs.dir(generationOutputPath)

            doLast {
                val codeGenerator = CodeGenerator(FileSystem.SYSTEM)

                try {
                    generationOutputPath.get()
                        .asFile
                        .listFiles()
                        ?.forEach(File::deleteRecursively)
                } catch (e: Exception) {
                    if(logger.isDebugEnabled)
                        logger.error(e.stackTraceToString())
                }

                codeGenerator.generate(
                    configuration = RSPGenConfiguration(
                        rootPath = target.file(extension.paths.protoSources.get()).toOkioPath(),
                        outputPath = target.file(generationOutputPath).toOkioPath(),
                        clientGeneration = extension.profile.client.get(),
                        serverGeneration = extension.profile.server.get(),
                        builderTypes = extension.options.builderTypes.get(),
                    )
                )
            }
        }

        target.afterEvaluate {
            val allSourceSets = target.extensions.findByType<KotlinMultiplatformExtension>()?.sourceSets
                ?: target.extensions.findByType<KotlinJvmProjectExtension>()?.sourceSets
                ?: target.extensions.findByType<KotlinAndroidProjectExtension>()?.sourceSets
                ?: error("Does Kotlin plugin apply to the buildscript?")

            val commonSourceSet = allSourceSets
                .findByName(KotlinSourceSet.COMMON_MAIN_SOURCE_SET_NAME)
            val mainSourceSet = allSourceSets.findByName("main")

            val sourceSet = if (extension.targetSourceSet.getOrNull() != null)
                allSourceSets.getByName(extension.targetSourceSet.get())
            else commonSourceSet ?: mainSourceSet

            sourceSet
                ?.kotlin
                ?.srcDirs(generateProto.outputs)
                ?: error(SOURCE_SET_NOT_FOUND)
        }
    }
}

private const val SOURCE_SET_NOT_FOUND =
    "Unable to obtain source set: you should have commonMain/main or custom one that is set up in the [rsp.targetSourceSet]"