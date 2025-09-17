plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
//    alias(libs.plugins.kotlinAndroid)
}

apply(from = "$rootDir/compose-module.gradle")

android {
    namespace = "com.esteban.ruano.tasks_presentation"
}

dependencies {
    implementation(project(":core"))
    implementation(project(":test-core"))
    implementation(project(":core-ui"))
    implementation(project(":tasks:tasks_domain"))
    implementation(projects.shared)

    implementation(libs.kotlinx.datetime)
    // Calendar
    implementation(libs.calendar.view)
    implementation(libs.calendar.compose)
    implementation(compose.components.resources)

    // Android Instrumentation Testing
    androidTestImplementation(project(":navigation"))
    androidTestImplementation(project(":tasks:tasks_data"))
    androidTestImplementation(project(":core-data"))
}
