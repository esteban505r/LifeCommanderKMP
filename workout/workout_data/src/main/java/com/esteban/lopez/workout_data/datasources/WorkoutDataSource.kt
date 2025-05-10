package com.esteban.ruano.workout_data.datasources

import com.esteban.ruano.workout_domain.model.Exercise
import com.esteban.ruano.workout_domain.model.WorkoutDashboard
import com.esteban.ruano.workout_domain.model.WorkoutDay

interface WorkoutDataSource {
    suspend fun getWorkoutDays(): List<WorkoutDay>

    suspend fun getWorkoutDaysWithExercises(): List<WorkoutDay>

    suspend fun getWorkoutDayById(workoutId: String): WorkoutDay

    suspend fun getExercisesByWorkoutDay(workoutDayId:String): List<Exercise>

    suspend fun getExercises(): List<Exercise>

    suspend fun saveExercise(exercise: Exercise): Unit

    suspend fun saveWorkoutDay(workoutDay: WorkoutDay)

    suspend fun getWorkoutDayByNumber(number: Int): WorkoutDay

    suspend fun linkExerciseWithWorkoutDay(workoutDayId: String, exerciseId: String)

    suspend fun updateWorkoutDay(workoutDayId: String,workoutDay: WorkoutDay)

    suspend fun getWorkoutDashboard(): WorkoutDashboard

    suspend fun getExerciseById(exerciseId: String): Exercise
}
