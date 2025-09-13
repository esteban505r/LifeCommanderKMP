package com.esteban.ruano.workout_data.datasources

import com.esteban.ruano.lifecommander.models.Exercise
import com.esteban.ruano.workout_domain.model.WorkoutDashboard
import com.esteban.ruano.workout_domain.model.Workout

interface WorkoutDataSource {
    suspend fun getWorkoutDays(): List<Workout>

    suspend fun getWorkoutDaysWithExercises(): List<Workout>

    suspend fun getWorkoutDayById(workoutId: String): Workout

    suspend fun getExercisesByWorkoutDay(workoutDayId:String): List<Exercise>

    suspend fun getExercises(): List<Exercise>

    suspend fun saveExercise(exercise: Exercise): Unit

    suspend fun saveWorkoutDay(workout: Workout)

    suspend fun getWorkoutDayByNumber(number: Int): Workout

    suspend fun linkExerciseWithWorkoutDay(workoutDayId: String, exerciseId: String)

    suspend fun updateWorkoutDay(workoutDayId: String, workout: Workout)

    suspend fun getWorkoutDashboard(): WorkoutDashboard

    suspend fun getExerciseById(exerciseId: String): Exercise
}
