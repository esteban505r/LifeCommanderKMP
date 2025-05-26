plugins {
    alias(libs.plugins.androidLibrary)
//    alias(libs.plugins.kotlinAndroid)
}

apply(from = "$rootDir/base-module.gradle")

android {
    namespace = "com.esteban.ruano.habits_domain"
}

dependencies {
    implementation(project(":core"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(projects.shared)
}
