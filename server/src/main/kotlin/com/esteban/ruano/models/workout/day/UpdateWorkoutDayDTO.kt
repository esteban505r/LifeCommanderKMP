package com.esteban.ruano.models.workout.day

import kotlinx.serialization.Serializable
import com.esteban.ruano.models.workout.exercise.ExerciseDTO

@Serializable
data class UpdateWorkoutDayDTO(
    val id: String? = null,
    val day: Int? = null,
    val time: String? = null,
    val name:String? = null,
    val exercises: List<ExerciseDTO>? = null
)
