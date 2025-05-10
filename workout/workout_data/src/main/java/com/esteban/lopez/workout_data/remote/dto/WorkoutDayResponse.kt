package com.esteban.ruano.workout_data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class WorkoutDayResponse(
    val id: String? = null,
    val day: Int,
    val name:String,
    val time: String,
    val exercises: List<ExerciseResponse>
)