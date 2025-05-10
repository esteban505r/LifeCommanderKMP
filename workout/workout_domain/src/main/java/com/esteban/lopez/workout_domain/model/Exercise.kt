package com.esteban.ruano.workout_domain.model

data class Exercise(
    val id: String? = null,
    val name: String,
    val description: String,
    val restSecs: Int,
    val baseSets: Int,
    val baseReps: Int,
    val muscleGroup: MuscleGroup,
    val equipment: List<Equipment>,
    val resource: Resource? = null
)
