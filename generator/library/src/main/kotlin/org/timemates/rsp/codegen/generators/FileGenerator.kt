package org.timemates.rsp.codegen.generators

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.wire.Syntax
import com.squareup.wire.schema.ProtoFile
import com.squareup.wire.schema.Schema
import org.timemates.rsp.codegen.Constant
import org.timemates.rsp.codegen.ext.addImports
import org.timemates.rsp.codegen.generators.client.ClientServiceGenerator
import org.timemates.rsp.codegen.generators.server.ServerServiceGenerator
import org.timemates.rsp.codegen.generators.types.TypeGenerator
import org.timemates.rsp.codegen.typemodel.Annotations
import org.timemates.rsp.codegen.typemodel.Types

internal object FileGenerator {
    fun generateFile(
        schema: Schema,
        protoFile: ProtoFile,
        clientGeneration: Boolean,
        serverGeneration: Boolean,
    ): FileSpec {
        require(protoFile.syntax == Syntax.PROTO_3) {
            "RSP does not support ProtoBuf syntax under the version of 3."
        }
        val fileName = ClassName(protoFile.javaPackage() ?: protoFile.packageName ?: "", protoFile.name())

        return FileSpec.builder(fileName).apply {
            addAnnotation(Annotations.Suppress("UNUSED", "RedundantVisibilityModifier"))
            addAnnotation(Annotations.OptIn(Types.ExperimentalSerializationApi))
            addFileComment(Constant.GENERATED_COMMENT)

            protoFile.extendList.forEach {
                ExtendGenerator.generateExtend(it, schema).forEach(::addProperty)
            }

            if(serverGeneration && protoFile.services.isNotEmpty()) {
                addImport(Types.ServiceDescriptor.packageName, Types.ServiceDescriptor.simpleName)
                addImport(Types.ServiceDescriptor.packageName, Types.ProcedureDescriptor.base.simpleName)
                addTypes(protoFile.services.map { ServerServiceGenerator.generateService(it, schema) })
            }

            if(clientGeneration && protoFile.services.isNotEmpty()) {
                val genResult = protoFile.services.map { ClientServiceGenerator.generateService(it, schema) }
                addTypes(genResult.map(ClientServiceGenerator.Result::typeSpec))
                addImports(genResult.flatMap { it.imports })
            }

            val types = protoFile.types.map { TypeGenerator.generateType(it, schema) }

            if (types.isNotEmpty()) {
                addImport(Types.ProtoType.packageName, Types.ProtoType.simpleName)
            }

            types.mapNotNull(TypeGenerator.Result::constructorFun)
                .forEach(::addFunction)
            addTypes(types.map { it.typeSpec })
        }.build()
    }
}