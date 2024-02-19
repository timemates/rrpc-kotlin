package org.timemates.rsproto.codegen.types

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.wire.schema.*

internal object TypeTransformer {
    data class Result(val typeSpec: TypeSpec, val constructorFun: FunSpec?)

    fun transform(incoming: Type, schema: Schema): Result {
        return when (incoming) {
            is MessageType -> MessageTypeTransformer.transform(incoming, schema)
                .let { Result(it.type, it.constructorFun) }
            is EnumType -> Result(EnumTypeTransformer.transform(incoming, schema), null)
            is EnclosingType -> Result(EnclosingTypeTransformer.transform(incoming, schema), null)
        }
    }

}