package com.esteban.ruano.workout_domain.repository

import com.esteban.ruano.workout_domain.model.Exercise
import com.esteban.ruano.workout_domain.model.WorkoutDashboard
import com.esteban.ruano.workout_domain.model.WorkoutDay


interface WorkoutRepository {
    suspend fun getWorkoutDays(

    ): Result<List<WorkoutDay>>

    suspend fun getWorkoutDayById(
        workoutId: Int
    ): Result<WorkoutDay>

    suspend fun getWorkoutDayByNumber(
        number: Int
    ): Result<WorkoutDay>

    suspend fun getExercisesByWorkoutDay(
        workoutDayId:Int
    ): Result<List<Exercise>>

    suspend fun getExerciseById(
        exerciseId: String
    ): Result<Exercise>

    suspend fun getExercises(
    ): Result<List<Exercise>>

    suspend fun getWorkoutDaysWithExercises(
    ): Result<List<WorkoutDay>>

    suspend fun saveExercise(
        exercise: Exercise
    ): Result<Unit>

    suspend fun saveWorkoutDay(
        workoutDay: WorkoutDay
    ): Result<Unit>

    suspend fun updateWorkoutDay(
        id: String,
        workoutDay: WorkoutDay
    ): Result<Unit>

    suspend fun linkExerciseWithWorkoutDay(
        workoutDayId: Int,
        exerciseId: Int
    ): Result<Unit>

    suspend fun getWorkoutDashboard(): Result<WorkoutDashboard>
}