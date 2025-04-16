package app.timemate.rrpc.generator.kotlin.processors

import app.timemate.rrpc.generator.GeneratorContext
import app.timemate.rrpc.generator.Processor
import app.timemate.rrpc.generator.kotlin.internal.LibClassNames
import app.timemate.rrpc.generator.kotlin.internal.PoetAnnotations
import app.timemate.rrpc.generator.kotlin.internal.ext.asTypeName
import app.timemate.rrpc.generator.kotlin.internal.ext.capitalized
import app.timemate.rrpc.generator.kotlin.internal.ext.defaultValue
import app.timemate.rrpc.generator.kotlin.internal.ext.newline
import app.timemate.rrpc.generator.plugin.api.RSResolver
import app.timemate.rrpc.generator.plugin.api.result.ProcessResult
import app.timemate.rrpc.generator.plugin.api.result.flatten
import app.timemate.rrpc.generator.plugin.api.result.getOrElse
import app.timemate.rrpc.generator.plugin.api.result.map
import app.timemate.rrpc.proto.schema.RSField
import app.timemate.rrpc.proto.schema.RSMessage
import app.timemate.rrpc.proto.schema.RSOneOf
import app.timemate.rrpc.proto.schema.kotlinName
import app.timemate.rrpc.proto.schema.value.RSDeclarationUrl
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import kotlin.math.log

public object MessageProcessor : Processor<RSMessage, Pair<TypeSpec, FunSpec?>> {
    override suspend fun GeneratorContext.process(data: RSMessage): ProcessResult<Pair<TypeSpec, FunSpec?>> {
        val parameterTypes = data.fields.map { field ->
            field.typeUrl.asTypeName(resolver).map {
                when {
                    field.isRepeated -> LIST.parameterizedBy(it)
                    field.typeUrl.isMap -> it.copy(nullable = false)
                    !field.typeUrl.isScalar -> it.copy(nullable = true)
                    else -> it
                }
            }
        }.flatten().getOrElse { return it }

        val oneOfs = data.oneOfs.map {
            generateOneOf(it, resolver)
        }.flatten().getOrElse { return it }

        val properties = generateProperties(data, parameterTypes)

        val oneOfProperties = oneOfs.map { it.second }
        val className = data.typeUrl.asTypeName(resolver).getOrElse { return it } as ClassName
        val nested = data.nestedTypes.map { TypeProcessor.process(it, this) }
            .flatten().getOrElse { return it }.filterNotNull()

        val generateCreateFun = data.fields.isNotEmpty() || data.oneOfs.isNotEmpty()

        val typeSpec = TypeSpec.classBuilder(className)
            .addSuperinterface(LibClassNames.ProtoType)
            .addAnnotation(PoetAnnotations.OptIn(LibClassNames.ExperimentalSerializationApi))
            .addKdoc(data.documentation.replace("%", "%%"))
            .addAnnotation(PoetAnnotations.Serializable)
            .primaryConstructor(
                generatePrimaryConstructor(
                    data,
                    parameterTypes,
                    oneOfs
                )
            )
            .addType(
                generateCompanionObject(
                    data = data,
                    className = className,
                    nested = nested,
                    options = data.nestedExtends.map {
                        ExtendProcessor.process(it, this)
                    }.flatten().getOrElse { return it }.flatten(),
                    generateCreateFun = generateCreateFun,
                    typeUrl = data.typeUrl,
                )
            )
            .addTypes(nested.map { it.first })
            .apply {
                if (generateCreateFun) {
                    addType(
                        generateMessageBuilder(
                            data.name,
                            properties.mapIndexed { index, it -> it to data.fields[index] },
                            oneOfProperties
                        )
                    )
                }
            }
            .addProperties(properties)
            .addProperties(oneOfProperties)
            .addProperty(
                PropertySpec.builder("definition", LibClassNames.ProtoTypeDefinition(ClassName("", data.name)))
                    .addModifiers(KModifier.OVERRIDE)
                    .getter(
                        FunSpec.getterBuilder()
                            .addCode("return Companion")
                            .build()
                    )
                    .build()
            )
            .addTypes(oneOfs.map { it.first })
            .build()

        val constructorFun = if (generateCreateFun) {
            FunSpec.builder(typeSpec.name!!)
                .addParameter(
                    "builder",
                    LambdaTypeName.get(
                        receiver = className.nestedClass("DSLBuilder"),
                        returnType = UNIT
                    )
                )
                .addCode("return ${typeSpec.name}.create(builder)")
                .returns(className)
                .build()
        } else null

        return ProcessResult.Success(typeSpec to constructorFun)
    }

    private fun GeneratorContext.generateProperties(
        incoming: RSMessage,
        parameterTypes: List<TypeName>,
    ): List<PropertySpec> {
        return incoming.fields.mapIndexed { index, field ->
            val fieldName = if (options.adaptNames) field.kotlinName else field.name

            PropertySpec.builder(fieldName, parameterTypes[index])
                .initializer(fieldName)
                .addKdoc(field.documentation.replace("%", "%%"))
                .addAnnotation(PoetAnnotations.ProtoNumber(field.tag))
                .apply {
                    if (field.isRepeated && field.typeUrl.isScalar) {
                        addAnnotation(PoetAnnotations.ProtoPacked)
                    }

                    if (field.typeUrl == RSDeclarationUrl.FIXED32 || field.typeUrl == RSDeclarationUrl.FIXED64) {
                        addAnnotation(PoetAnnotations.ProtoType("FIXED"))
                    }

                    if (field.typeUrl == RSDeclarationUrl.SINT32 || field.typeUrl == RSDeclarationUrl.SINT64) {
                        addAnnotation(PoetAnnotations.ProtoType("SIGNED"))
                    }


                }
                .build()
        }
    }

    private fun GeneratorContext.generateCompanionObject(
        data: RSMessage,
        className: ClassName,
        nested: List<Pair<TypeSpec, FunSpec?>>,
        options: List<PropertySpec>,
        generateCreateFun: Boolean,
        typeUrl: RSDeclarationUrl,
    ): TypeSpec {
        return TypeSpec.companionObjectBuilder()
            .addSuperinterface(LibClassNames.ProtoTypeDefinition(className))
            .addProperty(
                PropertySpec.builder("Default", className)
                    .addModifiers(KModifier.OVERRIDE)
                    .initializer(
                        buildCodeBlock {
                            val withNoDefaultValues = data.fields.any { it.defaultValue == null } ||
                                data.oneOfs.any()

                            if (withNoDefaultValues) {
                                add("%T(", className)
                                indent()
                                data.fields.filter { it.defaultValue == null }.forEach {
                                    val fieldName = if (this@generateCompanionObject.options.adaptNames)
                                        it.kotlinName
                                    else it.name

                                    newline()
                                    add("$fieldName = null,")
                                }
                                data.oneOfs.forEach {
                                    val fieldName = if (this@generateCompanionObject.options.adaptNames)
                                        it.kotlinName
                                    else it.name

                                    newline()
                                    add("$fieldName = null,")
                                }
                                unindent()
                                newline()
                                add(")")
                            } else {
                                add("%T()", className)
                            }
                        }
                    )
                    .build()
            )
            .addProperty(
                PropertySpec.builder("url", STRING)
                    .addModifiers(KModifier.OVERRIDE)
                    .initializer("%S", typeUrl.value)
                    .build()
            )
            .addProperties(options)
            .addFunctions(nested.mapNotNull { it.second })
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

    private fun GeneratorContext.generatePrimaryConstructor(
        incoming: RSMessage,
        parameterTypes: List<TypeName>,
        oneOfs: List<Pair<TypeSpec, PropertySpec>>,
    ): FunSpec {
        return FunSpec.constructorBuilder()
            .addModifiers(KModifier.PRIVATE)
            .addParameters(incoming.fields.mapIndexed { index, field ->
                val fieldName = if (options.adaptNames) field.kotlinName else field.name

                val type = parameterTypes[index]
                ParameterSpec.builder(fieldName, type)
                    .apply {
                        if (!type.isNullable)
                            defaultValue(
                                field.defaultValue ?: return@apply
                            )
                    }
                    .build()
            })
            .addParameters(oneOfs.map { (_, property) ->
                ParameterSpec.builder(property.name, property.type).build()
            })
            .build()
    }

    private fun generateMessageBuilder(
        name: String,
        declaredFields: List<Pair<PropertySpec, RSField>>,
        oneOfs: List<PropertySpec>,
    ): TypeSpec {
        val returnParametersSet = (declaredFields.map { MemberName("",it.first.name) } + oneOfs.map { MemberName("", it.name) })
            .toTypedArray()
        val returnParametersFormat = returnParametersSet.mapIndexed { index, _ -> "%${index + 1}N" }
            .joinToString(", ")

        return TypeSpec.classBuilder("DSLBuilder")
            .addProperties(declaredFields.map { (spec, type) ->
                spec.toBuilder().initializer(type.defaultValue).mutable(true).also {
                    it.annotations.clear()
                    it.kdoc.clear()

                    if (type.typeUrl != RSDeclarationUrl.UINT32 && type.typeUrl != RSDeclarationUrl.UINT64)
                        it.addAnnotation(JvmField::class)
                }.initializer(type.defaultValue.toString()).build()
            })
            .addProperties(oneOfs.map {
                it.toBuilder().apply {
                    annotations.clear()
                }.mutable(true).initializer("null").build()
            })
            .addFunction(
                FunSpec.builder("build")
                    .addCode("return ${name}(${returnParametersFormat})", args = returnParametersSet)
                    .returns(ClassName("", name))
                    .build()
            )
            .build()
    }

    private fun GeneratorContext.generateOneOf(
        oneOf: RSOneOf,
        schema: RSResolver,
    ): ProcessResult<Pair<TypeSpec, PropertySpec>> {
        val oneOfName = "${oneOf.kotlinName}OneOf".capitalized()
        val oneOfClassName = ClassName("", oneOfName)

        val oneOfClass = TypeSpec.interfaceBuilder(oneOfName)
            .addAnnotation(PoetAnnotations.Serializable)
            .addModifiers(KModifier.SEALED)
            .addTypes(oneOf.fields.map { field ->
                val defaultValue = field.defaultValue.toString()
                // safe to assume it's a class name; oneof cannot contain maps
                val typeName = field.typeUrl.asTypeName(schema)
                    .getOrElse { return it } as ClassName
                val builder = typeName.nestedClass("DSLBuilder")

                // we do it here without check anyway to avoid names like Foo_barOneOf
                val fieldName = field.kotlinName.capitalized()

                TypeSpec.classBuilder(fieldName)
                    .addModifiers(KModifier.VALUE)
                    .addAnnotation(PoetAnnotations.Serializable)
                    .addAnnotation(JvmInline::class)
                    .primaryConstructor(
                        FunSpec.constructorBuilder()
                            .addParameter(
                                ParameterSpec.builder("value", typeName)
                                    .defaultValue(
                                        defaultValue.takeUnless { it == "null" } ?: "%T.Default", typeName,
                                    )
                                    .build()
                            )
                            .build()
                    ).apply {
                        val type = schema.resolveType(field.typeUrl)
                        if (type is RSMessage && type.fields.isNotEmpty())
                            addFunction(
                                FunSpec.constructorBuilder()
                                    .addParameter(
                                        name = "builder",
                                        type = LambdaTypeName.get(builder, returnType = UNIT),
                                    )
                                    .callThisConstructor(CodeBlock.of("%T().also(builder).build()", builder))
                                    .build()
                            )
                    }
                    .addProperty(
                        PropertySpec.builder("value", typeName).initializer("value")
                            .addAnnotation(PoetAnnotations.ProtoNumber(field.tag))
                            .build()
                    )
                    .addType(
                        TypeSpec.companionObjectBuilder()
                            .addProperty(
                                PropertySpec.builder("Default", ClassName("", fieldName))
                                    .initializer("$fieldName()")
                                    .build()
                            )
                            .build()
                    )
                    .addSuperinterface(oneOfClassName)
                    .build()
            })
            .build()

        val propertyName = if (options.adaptNames) oneOf.kotlinName else oneOf.name

        val property = PropertySpec.builder(propertyName, oneOfClassName.copy(nullable = true))
            .addAnnotation(PoetAnnotations.ProtoOneOf)
            .addKdoc(oneOf.documentation.replace("%", "%%"))
            .initializer(propertyName)
            .build()

        return ProcessResult.Success(oneOfClass to property)
    }
}