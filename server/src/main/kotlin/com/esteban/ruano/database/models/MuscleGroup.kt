package com.esteban.ruano.database.models

enum class MuscleGroup(val value: String) {
    UPPER_BODY("upper_body"),
    LOWER_BODY("lower_body"),
    FULL_BODY("full_body"),
    BACK("back"),
    CORE("core"),
    CARDIO("cardio"),
    STRETCH("stretch"),
    LEGS("legs"),
    PULL("pull"),
    PUSH("push"),
}

fun MuscleGroup.toMuscleGroupString(): String {
    return when (this) {
        MuscleGroup.UPPER_BODY -> "upper_body"
        MuscleGroup.LOWER_BODY -> "lower_body"
        MuscleGroup.FULL_BODY -> "full_body"
        MuscleGroup.CORE -> "core"
        MuscleGroup.BACK -> "back"
        MuscleGroup.CARDIO -> "cardio"
        MuscleGroup.STRETCH -> "stretch"
        MuscleGroup.LEGS -> "legs"
        MuscleGroup.PULL -> "pull"
        MuscleGroup.PUSH -> "push"
    }
}