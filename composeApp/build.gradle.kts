import org.gradle.kotlin.dsl.implementation
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.hilt)
    kotlin("kapt")
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_18)
        }
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    jvm("desktop")
    
    /*@OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "composeApp"
        browser {
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(rootDirPath)
                        add(projectDirPath)
                    }
                }
            }
        }
        binaries.executable()
    }*/



    sourceSets {
        val desktopMain by getting

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            dependencies {
//                implementation(platform(libs.compose.bom))
                implementation(libs.compose.compiler)
                implementation(libs.compose.ui)
                implementation(libs.compose.ui.tooling.preview)
                implementation(libs.compose.hilt.navigation)
                implementation(libs.compose.material)
//                implementation(libs.compose.runtime)
                implementation(libs.compose.navigation)
                implementation(libs.androidx.lifecycle.viewmodel.compose)
                implementation(libs.androidx.activity.compose)
                implementation(libs.compose.google.fonts)

                implementation(libs.material3)

                implementation(libs.hilt.android)
                "kapt"(libs.hilt.android.compiler)

                implementation(project(":navigation"))
                implementation(project(":test-core"))
                implementation(project(":core"))
                implementation(project(":core-ui"))
                implementation(project(":core-data"))
                implementation(project(":onboarding:onboarding_presentation"))
                implementation(project(":onboarding:onboarding_domain"))
                implementation(project(":onboarding:onboarding_data"))
                implementation(project(":nutrition:nutrition_presentation"))
                implementation(project(":nutrition:nutrition_domain"))
                implementation(project(":nutrition:nutrition_data"))
                implementation(project(":habits:habits_presentation"))
                implementation(project(":habits:habits_domain"))
                implementation(project(":habits:habits_data"))
                implementation(project(":tasks:tasks_presentation"))
                implementation(project(":tasks:tasks_domain"))
                implementation(project(":tasks:tasks_data"))
                implementation(project(":workout:workout_presentation"))
                implementation(project(":workout:workout_domain"))
                implementation(project(":workout:workout_data"))

                implementation(libs.androidx.core.ktx)
                implementation(libs.androidx.appcompat)

                implementation(libs.google.material)

                implementation(libs.okhttp)
                implementation(libs.retrofit)
                implementation(libs.okhttp.logging.interceptor)
                implementation(libs.retrofit.gson)

                "kapt"(libs.room.compiler)
                implementation(libs.room.ktx)
                implementation(libs.room.runtime)

                // Testing
                testImplementation(project(":test-core"))
                androidTestImplementation(project(":test-core"))
//                kaptAndroidTest(libs.hilt.android.compiler)

                androidTestImplementation(libs.junit.android.ext)
                androidTestImplementation(libs.ui.test.manifest)
                androidTestImplementation(libs.compose.ui.test)
                androidTestImplementation(libs.truth)
//                androidTestImplementation(libs.kotlinx.coroutines.test)
                androidTestImplementation(libs.turbine)
                androidTestImplementation(libs.compose.ui.test)
//                androidTestImplementation(libs.espresso)
//                androidTestImplementation(libs.hilt.testing)
                androidTestImplementation(libs.test.runner)

                implementation("androidx.media3:media3-exoplayer:1.3.1")
            }

        }
        commonMain.dependencies {
//            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
//            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(projects.shared)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(projects.shared)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.cio)
            implementation(libs.ktor.client.json)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.serialization.gson.jvm)

            implementation(libs.navigation.compose)
            implementation(libs.lifecycle.viewmodel.compose)

            implementation(libs.coil.compose)
            implementation(libs.coil.network.okhttp)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.coroutines.swing)

            implementation(libs.datastore)
            implementation(libs.datastore.preferences)

            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            implementation(libs.compose.material.icons.extended)

            implementation(libs.compose.native.tray)
            implementation(libs.compose.native.notification)
            implementation(libs.calendar.compose.multiplatform)
        }
    }
}

android {
    namespace = "com.esteban.ruano.lifecommander"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.esteban.ruano.lifecommander"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_18
        targetCompatibility = JavaVersion.VERSION_18
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}



compose.desktop {

    application {
        mainClass = "com.esteban.ruano.lifecommander.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.AppImage,)
            packageName = "LifeCommander"
            packageVersion = "1.0.0"
            modules("jdk.unsupported")
        }

        buildTypes.release.proguard {
            version.set("7.4.0")
            isEnabled = false
            configurationFiles.from("proguard-rules.pro")
        }
    }
}
