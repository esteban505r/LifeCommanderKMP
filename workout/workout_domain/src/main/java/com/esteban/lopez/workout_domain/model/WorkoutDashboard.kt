package com.esteban.ruano.workout_domain.model

data class WorkoutDashboard(
    val workouts: List<Workout> = emptyList(),
    val totalExercises: Long = 0
)