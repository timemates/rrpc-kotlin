plugins {
    id(libs.plugins.conventions.multiplatform.library.get().pluginId)
    alias(libs.plugins.kotlinx.serialization)
}

group = "app.timemate.rrpc.metadata.common"
version = System.getenv("LIB_VERSION") ?: "SNAPSHOT"

dependencies {
    // -- Serialization --
    commonMainApi(libs.kotlinx.serialization.proto)

    // -- RRpc --
    commonMainApi(projects.common.core)
    commonMainApi(libs.timemate.rrpc.schema)

    // -- Coroutines --
    commonMainImplementation(libs.kotlinx.coroutines)

    // -- Test --
    jvmTestImplementation(libs.kotlin.test)
    jvmTestImplementation(libs.mockk)
}


mavenPublishing {
    coordinates(
        groupId = "app.timemate.rrpc",
        artifactId = "common-metadata",
        version = System.getenv("LIB_VERSION") ?: return@mavenPublishing,
    )

    pom {
        name.set("RRpc Common Core")
        description.set("Multiplatform Kotlin core library for RRpc servers and clients.")
    }
}