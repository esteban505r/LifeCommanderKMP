plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
//    alias(libs.plugins.kotlinAndroid)
}

apply(from = "$rootDir/compose-module.gradle")

android {
    namespace = "com.esteban.ruano.navigation"
}

dependencies {
    implementation(project(":core"))
    implementation(project(":test-core"))
    implementation(project(":core-ui"))
    implementation(compose.components.resources)
    implementation(project(":home:home_presentation"))
    implementation(project(":habits:habits_presentation"))
    implementation(project(":habits:habits_domain"))
    implementation(project(":finance:finance_presentation"))
    implementation(project(":nutrition:nutrition_presentation"))
    implementation(project(":tasks:tasks_presentation"))
    implementation(project(":tasks:tasks_domain"))
    implementation(projects.shared)
    implementation(project(":onboarding:onboarding_presentation"))
    implementation(project(":workout:workout_presentation"))
    implementation(project(":workout:workout_domain"))
}
