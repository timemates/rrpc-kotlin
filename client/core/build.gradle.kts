plugins {
    id(libs.plugins.conventions.multiplatform.library.get().pluginId)
    alias(libs.plugins.kotlinx.serialization)
}

group = "org.timemates.rsp"
version = System.getenv("LIB_VERSION") ?: "SNAPSHOT"

dependencies {
    commonMainImplementation(libs.rsocket.client)
    commonMainImplementation(projects.common.core)

    jvmTestImplementation(libs.kotlin.test)
    jvmTestImplementation(libs.mockk)
}

kotlin {
//    js(IR) {
//        browser()
//        nodejs()
//    }
//    iosArm64()
//    iosX64()
//    iosSimulatorArm64()
}

mavenPublishing {
    coordinates(
        groupId = "org.timemates.rsp",
        artifactId = "client-core",
        version = System.getenv("LIB_VERSION") ?: return@mavenPublishing,
    )

    pom {
        name.set("RSProto Client Core")
        description.set("Multiplatform Kotlin core library for RSProto clients.")
    }
}
