plugins {
    id(libs.plugins.conventions.multiplatform.library.get().pluginId)
    alias(libs.plugins.kotlinx.serialization)
}

group = "org.timemates.rrpc"
version = System.getenv("LIB_VERSION") ?: "SNAPSHOT"

dependencies {
    commonMainImplementation(libs.kotlinx.serialization.proto)
    commonMainApi(projects.server.core)

    commonMainImplementation(projects.common.metadata)

    commonMainImplementation(libs.ktor.server.websockets) 

    commonMainImplementation(libs.ktor.server.core) 
}

mavenPublishing {
    coordinates(
        groupId = "org.timemates.rrpc",
        artifactId = "server-metadata",
        version = System.getenv("LIB_VERSION") ?: return@mavenPublishing,
    )

    pom {
        name.set("RRpcroto Server Metadata")
        description.set("Multiplatform Kotlin metadata library for RRpcroto servers.")
    }
}
