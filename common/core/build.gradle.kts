plugins {
    id(libs.plugins.conventions.multiplatform.library.get().pluginId)
    alias(libs.plugins.kotlinx.serialization)
}

group = "app.timemate.rrpc.server"
version = System.getenv("LIB_VERSION") ?: "SNAPSHOT"

dependencies {
    // -- Serialization --
    commonMainApi(libs.kotlinx.serialization.proto)

    // -- Coroutines --
    commonMainImplementation(libs.kotlinx.coroutines)

    // -- Test --
    jvmTestImplementation(libs.kotlin.test)
    jvmTestImplementation(libs.mockk)
}

mavenPublishing {
    coordinates(
        groupId = "app.timemate.rrpc",
        artifactId = "common-core",
        version = System.getenv("LIB_VERSION") ?: return@mavenPublishing,
    )

    pom {
        name.set("RRpc Common Core")
        description.set("Multiplatform Kotlin core library for RRpc servers and clients.")
    }
}