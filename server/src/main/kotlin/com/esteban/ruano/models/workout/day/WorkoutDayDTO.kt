package com.esteban.ruano.models.workout.day

import kotlinx.serialization.Serializable
import com.esteban.ruano.models.workout.exercise.ExerciseDTO

@Serializable
data class WorkoutDayDTO(
    val id: String,
    val day: Int,
    val time: String,
    val name:String,
    val exercises: List<ExerciseDTO>
)
