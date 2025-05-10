package com.esteban.ruano.workout_domain.model

data class WorkoutDay(
    val id: String? = null,
    val day: Int,
    val name:String,
    val time: String,
    val exercises: List<Exercise>,
    val remoteId: Int? = null
)