package org.timemates.rsp.codegen.types

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.wire.schema.EnclosingType
import com.squareup.wire.schema.Schema

internal object EnclosingTypeTransformer {

    fun transform(incoming: EnclosingType, schema: Schema): TypeSpec {
        val nested = incoming.nestedTypes.map { TypeTransformer.transform(it, schema) }

        return TypeSpec.classBuilder(incoming.name)
            .primaryConstructor(
                FunSpec.constructorBuilder().addModifiers(KModifier.PRIVATE).build()
            )
            .addType(
                TypeSpec.companionObjectBuilder()
                    .addFunctions(nested.mapNotNull(TypeTransformer.Result::constructorFun))
                    .build()
            )
            .addTypes(nested.map(TypeTransformer.Result::typeSpec))
            .build()
    }

}