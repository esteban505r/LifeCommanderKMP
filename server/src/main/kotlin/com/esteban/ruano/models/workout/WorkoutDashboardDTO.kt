package com.esteban.ruano.models.workout

import com.esteban.ruano.models.workout.day.WorkoutDayDTO
import kotlinx.serialization.Serializable

@Serializable
data class WorkoutDashboardDTO(
    val workoutDays: List<WorkoutDayDTO>,
    val totalExercises: Long
)