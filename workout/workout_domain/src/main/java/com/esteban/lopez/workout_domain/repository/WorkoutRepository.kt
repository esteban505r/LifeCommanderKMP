package com.esteban.ruano.workout_domain.repository

import com.esteban.ruano.lifecommander.models.CreateExerciseSetTrackDTO
import com.esteban.ruano.lifecommander.models.Exercise
import com.esteban.ruano.lifecommander.models.ExerciseDayStatus
import com.esteban.ruano.workout_domain.model.WorkoutDashboard
import com.esteban.ruano.workout_domain.model.Workout


interface WorkoutRepository {
    suspend fun getWorkoutDays(

    ): Result<List<Workout>>

    suspend fun getWorkoutDayById(
        workoutId: Int
    ): Result<Workout>

    suspend fun getWorkoutDayStatus(
        workoutDayId: String,
        dateTime:String,
    ): Result<List<ExerciseDayStatus>>

    suspend fun getWorkoutDayByNumber(
        number: Int
    ): Result<Workout>

    suspend fun getExercisesByWorkoutDay(
        workoutDayId:Int
    ): Result<List<Exercise>>

    suspend fun getExerciseById(
        exerciseId: String
    ): Result<Exercise>

    suspend fun getExercises(
    ): Result<List<Exercise>>

    suspend fun getWorkoutDaysWithExercises(
    ): Result<List<Workout>>

    suspend fun saveExercise(
        exercise: Exercise
    ): Result<Unit>

    suspend fun saveWorkoutDay(
        workout: Workout
    ): Result<Unit>

    suspend fun updateWorkoutDay(
        id: String,
        workout: Workout
    ): Result<Unit>

    suspend fun linkExerciseWithWorkoutDay(
        workoutDayId: Int,
        exerciseId: Int
    ): Result<Unit>

    suspend fun getWorkoutDashboard(): Result<WorkoutDashboard>

    suspend fun addSet(
        dto: CreateExerciseSetTrackDTO,
    ):Result<Unit>

    suspend fun removeSet(id: String): Result<Unit>
}