package com.esteban.ruano.workout_domain.model

enum class MuscleGroup(val value: String) {
    UPPER_BODY("upper_body"),
    LOWER_BODY("lower_body"),
    FULL_BODY("full_body"),
    CORE("core"),
    CARDIO("cardio"),
    STRETCH("stretch"),
    LEGS("legs"),
    PULL("pull"),
    PUSH("push"),
    NONE("none");

    companion object{
        fun fromValue(value: String): MuscleGroup {
            return when (value) {
                "upper_body" -> MuscleGroup.UPPER_BODY
                "lower_body" -> MuscleGroup.LOWER_BODY
                "full_body" -> MuscleGroup.FULL_BODY
                "core" -> MuscleGroup.CORE
                "cardio" -> MuscleGroup.CARDIO
                "stretch" -> MuscleGroup.STRETCH
                "legs" -> MuscleGroup.LEGS
                "pull" -> MuscleGroup.PULL
                "push" -> MuscleGroup.PUSH
                else -> MuscleGroup.NONE
            }
        }
    }
}


fun MuscleGroup.toMuscleGroupString(): String {
    return when (this) {
        MuscleGroup.UPPER_BODY -> "upper_body"
        MuscleGroup.LOWER_BODY -> "lower_body"
        MuscleGroup.FULL_BODY -> "full_body"
        MuscleGroup.CORE -> "core"
        MuscleGroup.CARDIO -> "cardio"
        MuscleGroup.STRETCH -> "stretch"
        MuscleGroup.LEGS -> "legs"
        MuscleGroup.PULL -> "pull"
        MuscleGroup.PUSH -> "push"
        MuscleGroup.NONE -> "none"
    }
}