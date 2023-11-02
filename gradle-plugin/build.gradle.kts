plugins {
    `kotlin-dsl`
}

kotlin {
    explicitApi()
}

dependencies {
    implementation(projects.codeGenerator)
    implementation(libs.squareup.okio)
}