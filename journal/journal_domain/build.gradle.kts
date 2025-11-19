plugins {
    alias(libs.plugins.androidLibrary)
}

apply(from = "$rootDir/base-module.gradle")

android {
    namespace = "com.esteban.ruano.journal_domain"
}

dependencies {
    implementation(project(":core"))
    implementation(projects.shared)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.datetime)
}

