package org.timemates.rrpc.generator.kotlin.types

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import org.timemates.rrpc.common.metadata.RMResolver
import org.timemates.rrpc.common.metadata.RMType
import org.timemates.rrpc.common.metadata.value.RMTypeUrl
import org.timemates.rrpc.generator.kotlin.annotation.ExperimentalSpecModifierApi
import org.timemates.rrpc.generator.kotlin.modifier.ModifiersRegistry
import org.timemates.rrpc.generator.kotlin.modifier.TypeModifier
import org.timemates.rrpc.generator.kotlin.types.message.MessageTypeGenerator

internal object TypeGenerator {
    data class Result(val typeSpec: TypeSpec, val constructorFun: FunSpec?)

    @OptIn(ExperimentalSpecModifierApi::class)
    fun generateType(
        incoming: RMType,
        resolver: RMResolver,
        modifiersRegistry: ModifiersRegistry,
    ): Result? {
        if (incoming.typeUrl == RMTypeUrl.ACK)
            // ignore timemates.rrpc.Ack – it's type-marker
            return null

        return when (incoming) {
            is RMType.Message -> MessageTypeGenerator.generateMessage(incoming, resolver)
                .let { Result(it.type, it.constructorFun) }
            is RMType.Enum -> Result(EnumTypeGenerator.generateEnum(incoming, resolver), null)
            is RMType.Enclosing -> Result(EnclosingTypeGenerator.generatorEnclosingType(incoming, resolver), null)
        }.let {
            it.copy(typeSpec = modifiersRegistry.modified(TypeModifier, it.typeSpec, incoming, resolver))
        }
    }

}