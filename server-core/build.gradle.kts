plugins {
    id(libs.plugins.conventions.multiplatform.library.get().pluginId)
    alias(libs.plugins.kotlinx.serialization)
}

dependencies {
    commonMainImplementation(libs.rsocket.server)
    commonMainImplementation(libs.kotlinx.serialization.proto)
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