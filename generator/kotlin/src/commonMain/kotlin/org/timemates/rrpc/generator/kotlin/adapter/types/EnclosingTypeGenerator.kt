package org.timemates.rrpc.generator.kotlin.adapter.types

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import org.timemates.rrpc.common.schema.RSResolver
import org.timemates.rrpc.common.schema.RSType

internal object EnclosingTypeGenerator {

    fun generatorEnclosingType(incoming: RSType.Enclosing, schema: RSResolver): TypeSpec {
        val nested = incoming.nestedTypes.mapNotNull { TypeGenerator.generateType(it, schema) }

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