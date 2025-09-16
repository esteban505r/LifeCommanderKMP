package com.esteban.ruano.workout_data.datasources

import com.esteban.ruano.lifecommander.models.CreateExerciseSetTrackDTO
import com.esteban.ruano.lifecommander.models.CreateExerciseTrack
import com.esteban.ruano.lifecommander.models.Exercise
import com.esteban.ruano.lifecommander.models.ExerciseDayStatus
import com.esteban.ruano.workout_domain.model.WorkoutDashboard
import com.esteban.ruano.workout_domain.model.Workout

interface WorkoutDataSource {
    suspend fun getWorkoutDays(): List<Workout>

    suspend fun getWorkoutDaysWithExercises(): List<Workout>

    suspend fun getWorkoutDayById(workoutId: String): Workout
    suspend fun getWorkoutDayStatus(workoutDayId: String,dateTime:String): List<ExerciseDayStatus>

    suspend fun getExercisesByWorkoutDay(workoutDayId:String): List<Exercise>

    suspend fun getExercises(): List<Exercise>

    suspend fun saveExercise(exercise: Exercise): Unit

    suspend fun saveWorkoutDay(workout: Workout)

    suspend fun getWorkoutDayByNumber(number: Int): Workout

    suspend fun linkExerciseWithWorkoutDay(workoutDayId: String, exerciseId: String)
    suspend fun unLinkExerciseWithWorkoutDay(workoutDayId: String, exerciseId: String)

    suspend fun updateWorkoutDay(workoutDayId: String, workout: Workout)

    suspend fun getWorkoutDashboard(): WorkoutDashboard

    suspend fun getExerciseById(exerciseId: String): Exercise

    suspend fun addSet(dto: CreateExerciseSetTrackDTO): Unit
    suspend fun removeSet(id:String): Unit

    suspend fun undoExercise(trackId:String)
    suspend fun completeExercise(track:CreateExerciseTrack)
    suspend fun updateExercise(id: String, exercise: Exercise)
    suspend fun deleteExercise(id: String)
}
