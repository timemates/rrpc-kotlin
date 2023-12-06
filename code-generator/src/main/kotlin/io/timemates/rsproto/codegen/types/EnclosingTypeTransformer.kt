package io.timemates.rsproto.codegen.types

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.wire.schema.EnclosingType
import com.squareup.wire.schema.Schema

internal object EnclosingTypeTransformer {
    fun transform(incoming: EnclosingType, schema: Schema): TypeSpec {
        return TypeSpec.classBuilder(incoming.name)
            .primaryConstructor(
                FunSpec.constructorBuilder().addModifiers(KModifier.PRIVATE).build()
            )
            .addTypes(incoming.nestedTypes.map { TypeTransformer.transform(it, schema) })
            .build()
    }

}