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
    api(projects.generator.library)

    implementation(libs.kotlin.plugin)
    implementation(libs.squareup.okio)
}

gradlePlugin {
    website = "https://github.com/rsproto"
    vcsUrl = "https://github.com/rsproto"

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

publishing {
    repositories {
        maven {
            val isDev = version.toString().contains("dev")

            name = if (isDev) "timeMatesDev" else "timeMatesReleases"
            url = if (isDev) uri("https://maven.timemates.org/dev") else uri("https://maven.timemates.org/releases")

            credentials {
                username = System.getenv("REPOSILITE_USER")
                password = System.getenv("REPOSILITE_SECRET")
            }
        }
    }
}