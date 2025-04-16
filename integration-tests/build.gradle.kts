import org.gradle.kotlin.dsl.rrpc

plugins {
    id(libs.plugins.conventions.jvm.core.get().pluginId)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.timemate.rrpc)
}

group = "app.timemate.rrpc"
version = System.getenv("LIB_VERSION") ?: "SNAPSHOT"

dependencies {
    // -- Project --
    implementation(projects.server.core)
    implementation(projects.client.core)
    implementation(libs.timemate.rrpc.schema)
    implementation(projects.common.metadata)
    implementation(projects.server.metadata)
    implementation(projects.client.metadata)

    // -- Serialization --
    implementation(libs.kotlinx.serialization.proto)

    // -- RSocket --
    implementation(libs.rsocket.client)
    implementation(libs.rsocket.server)

    // -- Ktor --
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.websockets)

    // -- MockK --
    implementation(libs.mockk)

    // -- Coroutines
    implementation(libs.kotlinx.coroutines.test)

    // -- Tests --
    implementation(libs.kotlin.test.junit)
}

val rrpcOutputFolder = layout.buildDirectory.dir("generated/rrpc").get().asFile

rrpc {
    inputs {
        source {
            artifact(projects.common.core)
            directory(file("src/test/resources"))
        }
    }

    outputFolder = rrpcOutputFolder
    permitPackageCycles = true

    plugins {
        add(projects.generator) {
            option("server_generation", true)
            option("metadata_generation", true)
            option("metadata_scope_name", "Test23")
        }
    }
}

kotlin {
    sourceSets.test {
        kotlin.srcDir(rrpcOutputFolder.resolve("rrpc-kotlin-gen"))
    }
}