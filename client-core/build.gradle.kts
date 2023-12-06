plugins {
    id(libs.plugins.conventions.multiplatform.library.get().pluginId)
    alias(libs.plugins.kotlinx.serialization)
}

group = "io.timemates.rsproto"
version = System.getenv("LIB_VERSION") ?: "SNAPSHOT"

dependencies {
    commonMainApi(libs.rsocket.client)
    commonMainApi(libs.kotlinx.serialization.proto)
    commonMainApi(projects.commonCore)
}

kotlin {
    js(IR) {
        browser()
        nodejs()
    }
    iosArm64()
    iosX64()
    iosSimulatorArm64()
}

mavenPublishing {
    coordinates(
        groupId = "io.timemates.rsproto",
        artifactId = "client-core",
        version = System.getenv("LIB_VERSION") ?: return@mavenPublishing,
    )

    pom {
        name.set("RSProto Client Core")
        description.set("Multiplatform Kotlin core library for RSProto clients.")
    }
}