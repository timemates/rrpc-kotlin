plugins {
    id(libs.plugins.conventions.multiplatform.library.get().pluginId)
    alias(libs.plugins.kotlinx.serialization)
}

group = "app.timemate.rrpc"
version = System.getenv("LIB_VERSION") ?: "SNAPSHOT"

dependencies {
    // -- Project --
    commonMainImplementation(projects.common.core)

    // -- Ktor --
    commonMainImplementation(libs.ktor.server.core)
    commonMainImplementation(libs.ktor.server.websockets)

    // -- RSocket --
    commonMainApi(libs.rsocket.server)

    // -- Serialization --
    commonMainImplementation(libs.kotlinx.serialization.proto)
}


mavenPublishing {
    coordinates(
        groupId = "app.timemate.rrpc",
        artifactId = "server-core",
        version = System.getenv("LIB_VERSION") ?: return@mavenPublishing,
    )

    pom {
        name.set("RRpc Server Core")
        description.set("Multiplatform Kotlin core library for RRpc servers.")
    }
}
