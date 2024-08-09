package org.timemates.rsp.codegen.generators.types

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.wire.schema.*
import org.timemates.rsp.codegen.generators.types.message.MessageTypeGenerator

internal object TypeGenerator {
    data class Result(val typeSpec: TypeSpec, val constructorFun: FunSpec?)

    fun generateType(incoming: Type, schema: Schema): Result {
        return when (incoming) {
            is MessageType -> MessageTypeGenerator.generateMessage(incoming, schema)
                .let { Result(it.type, it.constructorFun) }
            is EnumType -> Result(EnumTypeGenerator.generateEnum(incoming, schema), null)
            is EnclosingType -> Result(EnclosingTypeGenerator.generatorEnclosingType(incoming, schema), null)
        }
    }

}