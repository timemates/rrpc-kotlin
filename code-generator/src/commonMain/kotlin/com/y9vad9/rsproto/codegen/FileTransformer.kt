package com.y9vad9.rsproto.codegen

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.wire.schema.ProtoFile
import com.y9vad9.rsproto.codegen.services.client.ClientServiceApiGenerator
import com.y9vad9.rsproto.codegen.services.server.ServerServiceTransformer
import com.y9vad9.rsproto.codegen.types.TypeTransformer

internal object FileTransformer {
    fun transform(protoFile: ProtoFile, clientGeneration: Boolean, serverGeneration: Boolean): FileSpec {
        val fileName = ClassName(protoFile.javaPackage() ?: protoFile.packageName ?: "", protoFile.name())

        return FileSpec.builder(fileName).apply {
            addFileComment(Constant.GENERATED_COMMENT)

            if(serverGeneration)
                addTypes(protoFile.services.map(ServerServiceTransformer::transform))

            if(clientGeneration) {
                addTypes(protoFile.services.map(ClientServiceApiGenerator::generate))
                addImport(
                    packageName = "kotlinx.serialization.protobuf",
                    names = listOf("decodeFromByteArray", "encodeToByteArray"),
                )
            }

            addTypes(protoFile.types.map(TypeTransformer::transform))
        }.build()
    }
}