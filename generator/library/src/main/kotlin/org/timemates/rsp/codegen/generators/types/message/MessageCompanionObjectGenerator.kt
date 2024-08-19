package org.timemates.rsp.codegen.generators.types.message

import com.squareup.kotlinpoet.*
import org.timemates.rsp.codegen.generators.types.TypeGenerator

internal object MessageCompanionObjectGenerator {
    fun generateCompanionObject(
        className: ClassName,
        nested: List<TypeGenerator.Result>,
        oneOfs: List<OneOfGenerator.Result>,
        generateCreateFun: Boolean,
    ): TypeSpec {
        return TypeSpec.companionObjectBuilder()
            .addProperty(
                PropertySpec.builder("Default", className)
                    .initializer("%T()", className)
                    .build()
            )
            .addFunctions(nested.mapNotNull(TypeGenerator.Result::constructorFun))
            .apply {
                if (generateCreateFun) {
                    addFunction(
                        FunSpec.builder("create")
                            .addModifiers(KModifier.INLINE)
                            .addParameter(
                                name = "block",
                                type = LambdaTypeName.get(ClassName("", "DSLBuilder"), returnType = UNIT)
                            )
                            .addCode("return DSLBuilder().apply(block).build()", className)
                            .returns(className)
                            .build()

                    )
                }
            }
            .build()
    }
}