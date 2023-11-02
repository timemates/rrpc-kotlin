package com.y9vad9.rsocket.proto.codegen

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.wire.schema.ProtoFile
import com.y9vad9.rsocket.proto.codegen.services.ServiceTransformer
import com.y9vad9.rsocket.proto.codegen.types.TypeTransformer

internal object FileTransformer : Transformer<ProtoFile, FileSpec> by protoTransformer({ protoFile ->
    val fileName = ClassName(protoFile.javaPackage() ?: protoFile.packageName ?: "", protoFile.name())

    FileSpec.builder(fileName)
        .addFileComment(Constant.GENERATED_COMMENT)
        .addTypes(protoFile.services.map(ServiceTransformer::transform))
        .addTypes(protoFile.types.map(TypeTransformer::transform))
        .build()
})