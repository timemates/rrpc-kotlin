package org.timemates.rsp.codegen.generators.types

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.wire.schema.EnclosingType
import com.squareup.wire.schema.Schema

internal object EnclosingTypeGenerator {

    fun generatorEnclosingType(incoming: EnclosingType, schema: Schema): TypeSpec {
        val nested = incoming.nestedTypes.map { TypeGenerator.generateType(it, schema) }

        return TypeSpec.classBuilder(incoming.name)
            .primaryConstructor(
                FunSpec.constructorBuilder().addModifiers(KModifier.PRIVATE).build()
            )
            .addType(
                TypeSpec.companionObjectBuilder()
                    .addFunctions(nested.mapNotNull(TypeGenerator.Result::constructorFun))
                    .build()
            )
            .addTypes(nested.map(TypeGenerator.Result::typeSpec))
            .build()
    }

}