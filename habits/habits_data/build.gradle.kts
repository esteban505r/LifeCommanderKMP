plugins {
    alias(libs.plugins.androidLibrary)
//    alias(libs.plugins.kotlinAndroid)
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22"
}

apply(from = "$rootDir/base-module.gradle")

android {
    namespace = "com.esteban.ruano.habits_data"
}

dependencies {
    implementation(project(":core"))
    implementation(project(":core-data"))
    implementation(project(":habits:habits_domain"))

    implementation(libs.kotlinx.coroutines.core)
}
