package com.esteban.ruano.workout_domain.model

data class WorkoutDashboard(
    val workoutDays: List<WorkoutDay> = emptyList(),
    val totalExercises: Long = 0
)