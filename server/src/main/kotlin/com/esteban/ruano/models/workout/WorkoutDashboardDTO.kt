package com.esteban.ruano.models.workout

import com.esteban.ruano.models.workout.day.WorkoutDayDTO

data class WorkoutDashboardDTO(
    val workoutDays: List<WorkoutDayDTO>,
    val totalExercises: Long
)