package org.timemates.rrpc.generator.kotlin.adapter.types

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import org.timemates.rrpc.common.schema.RSResolver
import org.timemates.rrpc.common.schema.RSType
import org.timemates.rrpc.common.schema.value.RMDeclarationUrl
import org.timemates.rrpc.generator.kotlin.adapter.types.message.MessageTypeGenerator

internal object TypeGenerator {
    data class Result(val typeSpec: TypeSpec, val constructorFun: FunSpec?)

    fun generateType(
        incoming: RSType,
        resolver: RSResolver,
    ): Result? {
        if (incoming.typeUrl == RMDeclarationUrl.ACK)
        // ignore timemates.rrpc.Ack – it's type-marker
            return null

        return when (incoming) {
            is RSType.Message -> MessageTypeGenerator.generateMessage(incoming, resolver)
                .let { Result(it.type, it.constructorFun) }

            is RSType.Enum -> Result(EnumTypeGenerator.generateEnum(incoming, resolver), null)
            is RSType.Enclosing -> Result(EnclosingTypeGenerator.generatorEnclosingType(incoming, resolver), null)
        }
    }

}