package org.timemates.rsp.codegen.generators.types.message

import com.squareup.kotlinpoet.*
import org.timemates.rsp.codegen.generators.types.TypeGenerator
import org.timemates.rsp.codegen.typemodel.Types

internal object MessageCompanionObjectGenerator {
    fun generateCompanionObject(
        className: ClassName,
        nested: List<TypeGenerator.Result>,
        oneOfs: List<OneOfGenerator.Result>,
        generateCreateFun: Boolean,
        typeUrl: String,
    ): TypeSpec {
        return TypeSpec.companionObjectBuilder()
            .addSuperinterface(Types.ProtoTypeDefinition(className))
            .addProperty(
                PropertySpec.builder("Default", className)
                    .addModifiers(KModifier.OVERRIDE)
                    .initializer("%T()", className)
                    .build()
            )
            .addProperty(
                PropertySpec.builder("typeUrl", STRING)
                    .addModifiers(KModifier.OVERRIDE)
                    .initializer("%S", typeUrl)
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