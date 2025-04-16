plugins {
    id(libs.plugins.conventions.multiplatform.library.get().pluginId)
    alias(libs.plugins.kotlinx.serialization)
}

group = "app.timemate.rrpc.server.metadata.communicatio "
version = System.getenv("LIB_VERSION") ?: "SNAPSHOT"

dependencies {
    // -- Serialization --
    commonMainApi(libs.kotlinx.serialization.proto)

    // -- RRpc --
    commonMainApi(projects.common.core)
    commonMainApi(libs.timemate.rrpc.schema)
    commonMainApi(projects.common.metadata)

    // -- Coroutines --
    commonMainImplementation(libs.kotlinx.coroutines)

    // -- Test --
    jvmTestImplementation(libs.kotlin.test)
    jvmTestImplementation(libs.mockk)
}


mavenPublishing {
    coordinates(
        groupId = "app.timemate.rrpc",
        artifactId = "common-metadata-communication",
        version = System.getenv("LIB_VERSION") ?: return@mavenPublishing,
    )

    pom {
        name.set("RRpc Common Metadata Communication Library")
        description.set("Multiplatform Kotlin core library for client/server metadata service.")
    }
}