plugins {
    alias(libs.plugins.androidLibrary)
//    alias(libs.plugins.kotlinAndroid)
}

apply(from = "$rootDir/base-module.gradle")

android {
    namespace = "com.esteban.ruano.tasks_domain"
}

dependencies {
    implementation(project(":core"))
    implementation(projects.shared)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.datetime)
}
