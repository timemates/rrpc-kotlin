plugins {
    id(libs.plugins.conventions.multiplatform.library.get().pluginId)
    alias(libs.plugins.kotlinx.serialization)
}

group = "app.timemate.rrpc.client"
version = System.getenv("LIB_VERSION") ?: "SNAPSHOT"

dependencies {
    dependencies {
        // -- Project --
        commonMainApi(projects.common.core)

        // -- RSocket --
        commonMainApi(libs.rsocket.client)

        // -- Test --
        jvmTestImplementation(libs.kotlin.test)
        jvmTestImplementation(libs.mockk)
    }

}

mavenPublishing {
    coordinates(
        groupId = "app.timemate.rrpc",
        artifactId = "client-core",
        version = System.getenv("LIB_VERSION") ?: return@mavenPublishing,
    )

    pom {
        name.set("RRpc Client Core")
        description.set("Multiplatform Kotlin core library for RRpc clients.")
    }
}
