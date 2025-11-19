plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
}

apply(from = "$rootDir/compose-module.gradle")

android {
    namespace = "com.esteban.ruano.journal_presentation"
}

dependencies {
    implementation(project(":core"))
    implementation(project(":test-core"))
    implementation(project(":core-ui"))
    implementation(project(":journal:journal_domain"))
    implementation(projects.shared)

    implementation(libs.kotlinx.datetime)
    implementation(compose.components.resources)
}

