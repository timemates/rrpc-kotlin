package app.timemate.rrpc.generator.kotlin.processors

import app.timemate.rrpc.generator.plugin.api.result.ProcessResult
import app.timemate.rrpc.generator.plugin.api.result.flatten
import app.timemate.rrpc.generator.plugin.api.result.getOrElse
import app.timemate.rrpc.generator.plugin.api.result.map
import com.squareup.kotlinpoet.*
import app.timemate.rrpc.proto.schema.RSMessage
import app.timemate.rrpc.proto.schema.RSEnclosingType
import app.timemate.rrpc.proto.schema.RSEnum
import app.timemate.rrpc.proto.schema.RSType
import app.timemate.rrpc.proto.schema.sourceOnly
import app.timemate.rrpc.generator.kotlin.internal.PoetAnnotations
import app.timemate.rrpc.generator.*
import app.timemate.rrpc.generator.kotlin.internal.LibClassNames

public object TypeProcessor : Processor<RSType, Pair<TypeSpec, FunSpec?>?> {
    override suspend fun GeneratorContext.process(data: RSType): ProcessResult<Pair<TypeSpec, FunSpec?>?> {
        if (data.options.sourceOnly)
            return ProcessResult.Success(null)

        return when (data) {
            is RSMessage -> MessageProcessor.process(data, this)
            is RSEnum -> generateEnum(data).map { it to null }
            is RSEnclosingType -> generatorEnclosing(data).map { it to null }
        }
    }

    private suspend fun GeneratorContext.generateEnum(data: RSEnum): ProcessResult<TypeSpec> {
        val nested = data.nestedTypes.map { process(data) }
            .flatten()
            .getOrElse { return it }

        return ProcessResult.Success(
            TypeSpec.enumBuilder(data.name)
                .addAnnotation(PoetAnnotations.OptIn(LibClassNames.ExperimentalSerializationApi))
                .addAnnotation(PoetAnnotations.Serializable)
                .apply {
                    data.constants.forEach { constant ->
                        addEnumConstant(
                            constant.name,
                            TypeSpec.anonymousClassBuilder()
                                .addKdoc(constant.documentation?.replace("%", "%%").orEmpty())
                                .addAnnotation(PoetAnnotations.ProtoNumber(constant.tag))
                                .build()
                        )
                    }
                }
                .addTypes(nested.mapNotNull { it?.first })
                .addType(
                    TypeSpec.companionObjectBuilder()
                        .addProperty(
                            PropertySpec.builder("Default", ClassName("", data.name))
                                .initializer(data.constants.minByOrNull { it.tag }!!.name)
                                .build()
                        )
                        .addFunctions(nested.mapNotNull { it?.second })
                        .build()
                )
                .build()
        )
    }

    private suspend fun GeneratorContext.generatorEnclosing(data: RSEnclosingType): ProcessResult<TypeSpec> {
        val nested = data.nestedTypes.map { process(data) }
            .flatten()
            .getOrElse { return it }


        return ProcessResult.Success(
            TypeSpec.classBuilder(data.name)
                .primaryConstructor(
                    FunSpec.constructorBuilder().addModifiers(KModifier.PRIVATE).build()
                )
                .addType(
                    TypeSpec.companionObjectBuilder()
                        .addFunctions(nested.mapNotNull { it?.second })
                        .build()
                )
                .addTypes(nested.mapNotNull { it?.first })
                .build()
        )
    }
}