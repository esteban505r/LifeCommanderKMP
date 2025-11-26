plugins {
    alias(libs.plugins.androidLibrary)
//    alias(libs.plugins.kotlinAndroid)
}

apply(from = "$rootDir/base-module.gradle")

android {
    namespace = "com.esteban.ruano.timers_data"
}

dependencies {
    implementation(project(":core"))
    implementation(project(":core-data"))
    implementation(projects.shared)
    implementation(project(":timers:timers_domain"))

    implementation(libs.kotlinx.coroutines.core)
}

