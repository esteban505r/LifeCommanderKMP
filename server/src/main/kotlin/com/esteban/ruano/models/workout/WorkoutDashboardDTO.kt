package com.esteban.ruano.models.workoutimport com.esteban.ruano.models.workout.day.WorkoutDayDTO

data class WorkoutDashboardDTO(
    val workoutDays: List<WorkoutDayDTO>,
    val totalExercises: Long
)