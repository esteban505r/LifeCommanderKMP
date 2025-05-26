plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeCompiler)
//    alias(libs.plugins.kotlinAndroid)
}

apply(from = "$rootDir/compose-module.gradle")

android {
    namespace = "com.esteban.ruano.finance_presentation"
}

dependencies {
    implementation(project(":core"))
    implementation(project(":core-ui"))
    implementation(projects.shared)
    implementation(libs.kotlinx.datetime)
    implementation(project(":finance:finance_domain"))
    testImplementation(project(":test-core"))
} 