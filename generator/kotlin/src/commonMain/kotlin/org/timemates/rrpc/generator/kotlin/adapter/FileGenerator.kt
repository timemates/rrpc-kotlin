package org.timemates.rrpc.generator.kotlin.adapter

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import org.timemates.rrpc.codegen.typemodel.PoetAnnotations
import org.timemates.rrpc.codegen.typemodel.LibClassNames
import org.timemates.rrpc.common.schema.RSFile
import org.timemates.rrpc.common.schema.RSResolver
import org.timemates.rrpc.common.schema.kotlinPackage
import org.timemates.rrpc.generator.kotlin.adapter.client.ClientServiceGenerator
import org.timemates.rrpc.generator.kotlin.adapter.internal.ext.addImports
import org.timemates.rrpc.generator.kotlin.adapter.server.ServerServiceGenerator
import org.timemates.rrpc.generator.kotlin.adapter.types.TypeGenerator

internal object FileGenerator {
    fun generateFile(
        resolver: RSResolver,
        file: RSFile,
        clientGeneration: Boolean,
        serverGeneration: Boolean,
    ): FileSpec {
        val fileName = ClassName(file.kotlinPackage().value, file.name)

        return FileSpec.builder(fileName).apply {
            addAnnotation(PoetAnnotations.Suppress("UNUSED", "RedundantVisibilityModifier"))
            addAnnotation(PoetAnnotations.OptIn(LibClassNames.ExperimentalSerializationApi))
            addFileComment(Constant.GENERATED_COMMENT)

            file.extends.forEach {
                ExtendGenerator.generateExtend(it, resolver).forEach(::addProperty)
            }

            if (serverGeneration && file.services.isNotEmpty()) {
                addImport(LibClassNames.ServiceDescriptor.packageName, LibClassNames.ServiceDescriptor.simpleName)
                addImport(LibClassNames.ServiceDescriptor.packageName, LibClassNames.ProcedureDescriptor.base.simpleName)
                addTypes(file.services.map { ServerServiceGenerator.generateService(it, resolver) })
            }

            if (clientGeneration && file.services.isNotEmpty()) {
                val genResult = file.services.map { ClientServiceGenerator.generateService(it, resolver) }
                addTypes(genResult.map(ClientServiceGenerator.Result::typeSpec))
                addImports(genResult.flatMap { it.imports })
            }

            val types = file.types.mapNotNull { TypeGenerator.generateType(it, resolver) }

            if (types.isNotEmpty()) {
                addImport(LibClassNames.ProtoType.packageName, LibClassNames.ProtoType.simpleName)
            }

            types.mapNotNull(TypeGenerator.Result::constructorFun)
                .forEach(::addFunction)
            addTypes(types.map { it.typeSpec })
        }.build()
    }
}