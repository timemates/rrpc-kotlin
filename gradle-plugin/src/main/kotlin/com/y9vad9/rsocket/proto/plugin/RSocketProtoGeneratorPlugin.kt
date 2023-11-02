package com.y9vad9.rsocket.proto.plugin

import com.y9vad9.rsocket.proto.codegen.CodeGenerator
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

public class RSocketProtoGeneratorPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.extensions.create<ProtoGeneratorExtension>("protoGenerator")

        target.afterEvaluate {
            val codeGenerator = CodeGenerator(FileSystem.SYSTEM)

            codeGenerator.generate(
                target.file(extension.protoSourcePath).toOkioPath(),
                target.buildDir.toOkioPath().resolve(extension.generationOutputPath),
            )
        }
    }
}