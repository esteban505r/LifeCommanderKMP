package com.esteban.ruano.workout_data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class WorkoutDashboardResponse(
    val workoutDays: List<WorkoutDayResponse>,
    val totalExercises: Long,
)