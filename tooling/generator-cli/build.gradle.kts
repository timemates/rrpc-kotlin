plugins {
    id(libs.plugins.conventions.jvm.core.get().pluginId)
    application
    alias(libs.plugins.graalvm.native)
}

dependencies {
    // -- CLI --
    implementation(libs.clikt.core)

    // -- Okio --
    implementation(libs.squareup.okio)

    // -- Generators --
    implementation(projects.generator.core)
    implementation(projects.generator.kotlin)

    // -- JNA --
    implementation(libs.net.java.jna)

    // -- KotlinX --
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.serialization.proto)
}

application {
    applicationName = "rrgcli"
    mainClass = "org.timemates.rrpc.generator.cli.MainKt"
}

graalvmNative {
    binaries {
        named("main") {
            mainClass = "org.timemates.rrpc.generator.cli.MainKt"
            buildArgs.addAll(
                "--initialize-at-build-time=kotlin.DeprecationLevel",
                "-H:ReflectionConfigurationFiles=${project.layout.projectDirectory.dir("src/main/resources/META-INF/native-image/reflect-config.json")}",
                "-H:ResourceConfigurationFiles=${project.layout.projectDirectory.dir("src/main/resources/META-INF/native-image/resource-config.json")}",
                "-H:Name=rrgcli"
            )
            useFatJar = true
        }
        named("test") {
            buildArgs.addAll(
                "--verbose",
                "-O0",
                "-H:Name=rrgcli",
            )
            useFatJar = true
        }
    }
}
