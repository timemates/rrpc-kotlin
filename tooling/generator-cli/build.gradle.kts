plugins {
    id(libs.plugins.conventions.jvm.core.get().pluginId)
}

dependencies {
    // -- CLI --
    implementation(libs.clikt.core)

    // -- Okio --
    implementation(libs.squareup.okio)

    // -- Generators --
    implementation(projects.generator.core)
    implementation(projects.generator.kotlin)
}
