package org.timemates.rsp.codegen.generators.options

import com.squareup.kotlinpoet.*
import com.squareup.wire.schema.Options
import com.squareup.wire.schema.Schema
import org.timemates.rsp.codegen.ext.newline
import org.timemates.rsp.codegen.ext.smartPackage
import org.timemates.rsp.codegen.typemodel.ImportRequirement
import org.timemates.rsp.codegen.typemodel.Types

public object ClientOptionsPropertyGenerator {
    private val RPCS_OPTIONS_TYPE = ClassName("org.timemates.rsp.client.options", "RPCsOptions")

    public data class Result(
        val property: PropertySpec,
        val imports: List<ImportRequirement>,
    )

    public fun generate(
        optionsMap: Map<String, Options>,
        schema: Schema,
    ): Result {
        val imports = mutableListOf<ImportRequirement>()

        val code = buildCodeBlock {
            add("%T(", RPCS_OPTIONS_TYPE)
            if (optionsMap.isEmpty()) {
                add("emptyMap()")
                add(")")
                return@buildCodeBlock
            }
            indent()

            optionsMap.forEach { rpc, options ->
                add("\n%S to %T(", rpc, Types.Options)

                if (options.map.isEmpty()) {
                    add("emptyMap()),")
                    return@forEach
                }

                newline()
                indent()
                add("mapOf(")
                newline()
                indent()
                options.map.forEach { (key, value) ->
                    val field = schema.getField(key)!!
                    val type = field.type!!

                    schema.protoFile(type)
                        ?.smartPackage()
                        ?.let {
                            imports.add(ImportRequirement(it, listOf(field.name)))
                        }

                    add("%T.${key.simpleName} to ", Types.Option.RPC)
                    add(OptionValueGenerator.generate(type, value, schema))
                    add(",\n")
                }
                unindent()
                add(")")
                unindent()
                newline("),")
            }
            newline()
            unindent()
            add(")")
        }

        return Result(
            property = PropertySpec.builder("rpcsOptions", RPCS_OPTIONS_TYPE)
                .addModifiers(KModifier.OVERRIDE)
                .initializer(code)
                .build(),
            imports = imports,
        )
    }
}