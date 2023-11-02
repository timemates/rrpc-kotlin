package com.y9vad9.rsocket.proto.codegen

import com.squareup.kotlinpoet.TypeSpec

internal fun TypeSpec.Builder.addEnumConstants(vararg names: String) {
    names.forEach {
        addEnumConstant(it)
    }
}