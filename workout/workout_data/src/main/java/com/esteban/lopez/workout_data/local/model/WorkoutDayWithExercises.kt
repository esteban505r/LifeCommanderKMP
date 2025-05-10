package com.esteban.ruano.workout_data.local.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class WorkoutDayWithExercises(
    @Embedded val workoutDay: WorkoutDay,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            ExercisesWithWorkoutDays::class,
            parentColumn = "workoutDayId",
            entityColumn = "exerciseId"
        )
    )
    val exercises: List<Exercise>
)
