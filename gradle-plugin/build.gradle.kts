plugins {
    `kotlin-dsl`
    alias(libs.plugins.gradle.publish)
}

group = "org.timemates.rsp"
version = System.getenv("LIB_VERSION") ?: "SNAPSHOT"

kotlin {
    explicitApi()
}

dependencies {
    constraints {
        api("org.timemates.rsp:code-generator:$version")
    }
    api(projects.codeGenerator)

    implementation(libs.kotlin.plugin)
    implementation(libs.squareup.okio)
}

gradlePlugin {
    website = "https://github.com/timemates/rsproto"
    vcsUrl = "https://github.com/timemates/rsproto"

    plugins {
        create("rsproto-plugin") {
            id = "org.timemates.rsp"
            displayName = "RSProto Code Generator"
            description = "Code Generator from .proto files to Kotlin code."
            tags = listOf("kotlin", "rsocket", "protobuf", "proto")

            implementationClass = "org.timemates.rsp.plugin.RSocketProtoGeneratorPlugin"
        }
    }
}