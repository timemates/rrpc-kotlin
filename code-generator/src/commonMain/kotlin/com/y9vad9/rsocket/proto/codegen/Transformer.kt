package com.y9vad9.rsocket.proto.codegen

internal fun interface Transformer<TInput, TResult> {
    fun transform(incoming: TInput): TResult
}

internal fun <TInput, TResult> protoTransformer(
    block: Transformer<TInput, TResult>,
): Transformer<TInput, TResult> {
    return block
}
