plugins {
    alias(libs.plugins.androidLibrary)
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22"
}

apply(from = "$rootDir/base-module.gradle")

android {
    namespace = "com.esteban.ruano.journal_data"
}

dependencies {
    implementation(project(":core"))
    implementation(project(":test-core"))
    implementation(project(":core-data"))
    implementation(project(":journal:journal_domain"))
    implementation(projects.shared)
    implementation(libs.kotlinx.coroutines.core)
}

