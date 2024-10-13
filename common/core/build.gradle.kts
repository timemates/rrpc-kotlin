plugins {
    id(libs.plugins.conventions.multiplatform.library.get().pluginId)
    alias(libs.plugins.kotlinx.serialization)
}

dependencies {
    commonMainApi(libs.kotlinx.serialization.proto) 
    commonMainApi(libs.rsocket.core)

    jvmTestImplementation(libs.kotlin.test)
    jvmTestImplementation(libs.mockk)
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
        groupId = "org.timemates.rrpc",
        artifactId = "common-core",
        version = System.getenv("LIB_VERSION") ?: return@mavenPublishing,
    )

    pom {
        name.set("RRpcroto Common Core")
        description.set("Multiplatform Kotlin core library for RRpcroto servers and clients.")
    }
}