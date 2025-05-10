package com.esteban.ruano.workout_data.local.model

import androidx.room.Entity

@Entity(primaryKeys = ["exerciseId", "workoutDayId"], tableName = "exercises_with_workout_days")
data class ExercisesWithWorkoutDays(
    val exerciseId: Int,
    val workoutDayId: Int
)