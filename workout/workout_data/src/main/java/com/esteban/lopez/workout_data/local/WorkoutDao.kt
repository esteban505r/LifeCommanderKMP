package com.esteban.ruano.workout_data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.esteban.ruano.workout_data.local.model.Exercise
import com.esteban.ruano.workout_data.local.model.ExercisesWithWorkoutDays
import com.esteban.ruano.workout_data.local.model.WorkoutDay
import com.esteban.ruano.workout_data.local.model.WorkoutDayWithExercises

@Dao
interface WorkoutDao{

    @Query("SELECT * FROM workout_days")
    suspend fun getWorkoutDays(): List<WorkoutDay>

    @Query("SELECT * FROM workout_days WHERE id = :workoutId")
    suspend fun getWorkoutDayById(workoutId:String): WorkoutDayWithExercises

    @Insert
    suspend fun saveExercise(exercise: Exercise): Long

    @Insert
    suspend fun saveWorkoutDay(workoutDay: WorkoutDay)

    @Query("SELECT * FROM workout_days WHERE day = :number")
    suspend fun getWorkoutDayByNumber(number: Int): WorkoutDay

    @Transaction
    @Query("SELECT * FROM workout_days WHERE id = :workoutDayId")
    suspend fun getExercisesByWorkoutDay(workoutDayId: String): WorkoutDayWithExercises?

    @Query("SELECT * FROM exercises")
    suspend fun getExercises(): List<Exercise>

    @Transaction
    @Query("SELECT * FROM workout_days")
    suspend fun getWorkoutDaysWithExercises(): List<WorkoutDayWithExercises>

    @Query("INSERT INTO exercises_with_workout_days VALUES (:exerciseId,:workoutDayId)")
    suspend fun linkExerciseWithWorkoutDay(workoutDayId: String, exerciseId: String)



}