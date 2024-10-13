plugins {
    `kotlin-dsl`
    alias(libs.plugins.gradle.publish)
}

group = "org.timemates.rrpc"
version = System.getenv("LIB_VERSION") ?: "SNAPSHOT"

kotlin {
    explicitApi()
}

dependencies {
    constraints {
        api("org.timemates.rrpc.generator:kotlin:$version")
    }
    api(projects.generator.core)
    api(projects.generator.kotlin)

    implementation(libs.kotlin.plugin)
    implementation(libs.squareup.okio)
}

gradlePlugin {
    website = "https://github.com/rrpcroto"
    vcsUrl = "https://github.com/rrpcroto"

    plugins {
        create("rrpcroto-plugin") {
            id = "org.timemates.rrpc"
            displayName = "RRpcroto Code Generator"
            description = "Code Generator from .proto files to Kotlin code."
            tags = listOf("kotlin", "rsocket", "protobuf", "proto")

            implementationClass = "org.timemates.rrpc.plugin.RSocketProtoGeneratorPlugin"
        }
    }
}

publishing {
    repositories {
        if (project.hasProperty("publish-reposilite")) {
            maven {
                val isDev = version.toString().contains("dev")

                name = if (isDev) "timeMatesDev" else "timeMatesReleases"
                url = if (isDev) uri("https://maven.timemates.org/dev") else uri("https://maven.timemates.org/releases")

                credentials {
                    username = System.getenv("REPOSILITE_USER")
                    password = System.getenv("REPOSILITE_SECRET")
                }
            }
        } else {
            logger.log(
                LogLevel.INFO,
                "Custom plugin publishing is disabled: publish-locally or publish-reposilite parameter should be used to specify publication destination."
            )
        }
    }
}