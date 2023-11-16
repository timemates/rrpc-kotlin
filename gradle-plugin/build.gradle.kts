plugins {
    `kotlin-dsl`
    alias(libs.plugins.gradle.publish)
}

group = "io.timemates.rsproto"
version = System.getenv("LIB_VERSION") ?: "SNAPSHOT"

kotlin {
    explicitApi()
}

dependencies {
    implementation(projects.codeGenerator)

    implementation(libs.kotlin.plugin)
    implementation(libs.squareup.okio)
}

gradlePlugin {
    website = "https://github.com/timemates/rsproto"
    vcsUrl = "https://github.com/timemates/rsproto"

    plugins {
        create("rsproto-generator-plugin") {
            id = "io.timemates.rsproto.plugin"
            displayName = "RSProto Code Generator"
            description = "Code Generator from .proto files to Kotlin code."
            tags = listOf("kotlin", "rsocket", "protobuf", "proto")

            implementationClass = "io.timemates.rsproto.plugin.RSocketProtoGeneratorPlugin"
        }
    }
}