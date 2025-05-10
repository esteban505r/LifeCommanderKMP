plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeCompiler)
//    alias(libs.plugins.kotlinAndroid)
}

apply(from = "$rootDir/compose-module.gradle")

android {
    namespace = "com.esteban.ruano.home_presentation"
}

dependencies {
    implementation(project(":core"))
    implementation(project(":core-ui"))
    implementation(project(":habits:habits_domain"))
    implementation(project(":habits:habits_presentation"))
    implementation(project(":workout:workout_presentation"))
    implementation(project(":workout:workout_domain"))
    implementation(project(":tasks:tasks_presentation"))
    implementation(project(":tasks:tasks_domain"))
    implementation(project(":test-core"))

    androidTestImplementation(project(":navigation"))
    androidTestImplementation(project(":habits:habits_data"))
    androidTestImplementation(project(":tasks:tasks_data"))
    androidTestImplementation(project(":workout:workout_data"))
    androidTestImplementation(project(":core-data"))
}
