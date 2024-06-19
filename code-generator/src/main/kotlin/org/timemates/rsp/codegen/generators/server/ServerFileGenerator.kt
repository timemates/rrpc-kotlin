package org.timemates.rsp.codegen.generators.server

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.wire.Syntax
import com.squareup.wire.schema.ProtoFile
import com.squareup.wire.schema.Schema
import org.timemates.rsp.codegen.addTypes

public object ServerFileGenerator {
    public fun generateFile(schema: Schema, protoFile: ProtoFile): FileSpec {
        require(protoFile.syntax == Syntax.PROTO_3) {
            "RSProto does not support ProtoBuf syntax under the version of 3."
        }
        val fileName = ClassName(
            packageName = protoFile.javaPackage() ?: protoFile.packageName ?: "",
            protoFile.name()
        )


        return FileSpec.builder(fileName)
            .addTypes(protoFile.types)
    }
}