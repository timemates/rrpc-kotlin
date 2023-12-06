plugins {
    id(libs.plugins.conventions.multiplatform.library.get().pluginId)
    alias(libs.plugins.kotlinx.serialization)
}

group = "io.timemates.rsproto"
version = System.getenv("LIB_VERSION") ?: "SNAPSHOT"

dependencies {
    commonMainApi(libs.rsocket.server)
    commonMainApi(libs.kotlinx.serialization.proto)
    commonMainApi(projects.commonCore)

    commonMainImplementation(libs.ktor.server.websockets)

    commonMainImplementation(libs.ktor.server.core)
}

mavenPublishing {
    coordinates(
        groupId = "io.timemates.rsproto",
        artifactId = "server-core",
        version = System.getenv("LIB_VERSION") ?: return@mavenPublishing,
    )

    pom {
        name.set("RSProto Client Core")
        description.set("Multiplatform Kotlin core library for RSProto servers.")
    }
}