package app.timemate.rrpc.generator.kotlin.processors.metadata

import app.timemate.rrpc.generator.GeneratorContext
import app.timemate.rrpc.generator.Processor
import app.timemate.rrpc.generator.kotlin.internal.LibClassNames
import app.timemate.rrpc.generator.kotlin.internal.PoetAnnotations
import app.timemate.rrpc.generator.kotlin.internal.ext.newline
import app.timemate.rrpc.generator.kotlin.internal.ext.nextString
import app.timemate.rrpc.generator.plugin.api.result.ProcessResult
import app.timemate.rrpc.generator.plugin.api.result.flatten
import app.timemate.rrpc.generator.plugin.api.result.getOrElse
import app.timemate.rrpc.proto.schema.RSFile
import com.squareup.kotlinpoet.*
import kotlin.random.Random

public object CompoundFilesMetadataProcessor : Processor<List<RSFile>, List<FileSpec>> {

    override suspend fun GeneratorContext.process(data: List<RSFile>): ProcessResult<List<FileSpec>> {
        val isPrivate = options.metadataScopeName?.takeIf { it.isNotBlank() } == null
        // assign random value if name wasn't provided
        val name = options.metadataScopeName?.takeIf { it.isNotBlank() }?.plus("MetadataModule")
            ?: "SchemaMetadataModule${Random.nextString(16)}"

        val files = data.map { file ->
            FileMetadataProcessor.process(file, this@process)
        }.flatten().getOrElse { return it }

        val typeBuilder = if (isPrivate) TypeSpec.objectBuilder(name) else TypeSpec.classBuilder(name)

        val metadataFile = FileSpec.builder(ClassName("app.timemate.rrpc.metadata.generated", name))
            .addType(
                typeBuilder
                    .addAnnotation(PoetAnnotations.OptIn(PoetAnnotations.InternalMetadataApi.typeName))
                    .addSuperinterface(
                        superinterface = LibClassNames.SchemaMetadataModule,
                        delegate = buildCodeBlock {
                            add("SchemaMetadataModule(")
                            withIndent {
                                files.forEach { file ->
                                    newline()
                                    add("%M", MemberName(file.packageName, file.name))
                                    add(",")
                                }
                            }
                            newline()
                            add(")")
                        }
                    )
                    .apply {
                        if (isPrivate) {
                            addInitializerBlock(buildCodeBlock {
                                add("%T.register(this)", LibClassNames.GlobalSchemaMetadataModule)
                                newline()
                            })
                        }

                        if (isPrivate)
                            addModifiers(KModifier.PRIVATE)
                    }
                    .build()
            )
            .build()

        return ProcessResult.Success(
            files + metadataFile
        )
    }
}