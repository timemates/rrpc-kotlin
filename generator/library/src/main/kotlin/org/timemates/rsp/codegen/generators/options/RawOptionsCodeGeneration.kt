package org.timemates.rsp.codegen.generators.options

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.wire.schema.Options
import com.squareup.wire.schema.ProtoType
import com.squareup.wire.schema.Schema
import org.timemates.rsp.codegen.ext.newline
import org.timemates.rsp.codegen.ext.smartPackage
import org.timemates.rsp.codegen.typemodel.ImportRequirement
import org.timemates.rsp.codegen.typemodel.Types

internal object RawOptionsCodeGeneration {
    fun generate(
        options: Options,
        schema: Schema,
        optionsType: ProtoType,
    ): CodeBlock {
        val imports = mutableListOf<ImportRequirement>()

        return buildCodeBlock {
            add("%T(", Types.Options)

            if (options.map.isEmpty()) {
                add("emptyMap())")
                return@buildCodeBlock
            }

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

                add(
                    format = "%T.${key.simpleName} to ",
                    when (optionsType) {
                        Options.METHOD_OPTIONS -> Types.Option.RPC
                        Options.SERVICE_OPTIONS -> Types.Option.Service
                        else -> error("Unsupported type of option: ${field.type}")
                    }
                )
                add(OptionValueGenerator.generate(type, value, schema))
                newline(before = ",")
            }

            unindent()
            add(")")
        }
    }
}