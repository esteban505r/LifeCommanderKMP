rootProject.name = "oter"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.google.dagger.hilt.android") version "2.56.2" apply false
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

// Include Android modules only when requested
val includeAndroid = (System.getenv("INCLUDE_ANDROID")
    ?: System.getProperty("includeAndroid")
    ?: "true").toBoolean()

if (includeAndroid) {
    include(":composeApp")
    include(":core")
    include(":core-ui")
    include(":core-data")
    include(":onboarding")
    include(":onboarding:onboarding_presentation")
    include(":onboarding:onboarding_domain")
    include(":nutrition")
    include(":nutrition:nutrition_presentation")
    include(":nutrition:nutrition_domain")
    include(":nutrition:nutrition_data")
    include(":habits")
    include(":habits:habits_presentation")
    include(":habits:habits_domain")
    include(":habits:habits_data")

    include(":workout")
    include(":workout:workout_data")
    include(":workout:workout_presentation")
    include(":workout:workout_domain")

    include(":tasks")
    include(":tasks:tasks_presentation")
    include(":tasks:tasks_domain")
    include(":tasks:tasks_data")

    include(":finance")
    include(":finance:finance_presentation")
    include(":finance:finance_domain")
    include(":finance:finance_data")

    include(":test-core")
    include(":navigation")
    include(":home")
    include(":home:home_presentation")
    include(":onboarding:onboarding_data")
}


include(":server")
include(":shared")

