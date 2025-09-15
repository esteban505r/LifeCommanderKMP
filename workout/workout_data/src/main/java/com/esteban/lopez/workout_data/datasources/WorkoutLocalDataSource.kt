package com.esteban.ruano.workout_data.datasources

import com.esteban.ruano.lifecommander.models.CreateExerciseSetTrackDTO
import com.esteban.ruano.lifecommander.models.Exercise
import com.esteban.ruano.lifecommander.models.ExerciseDayStatus
import com.esteban.ruano.workout_data.local.WorkoutDao
import com.esteban.ruano.workout_data.mappers.toExercise
import com.esteban.ruano.workout_data.mappers.toLocalExercise
import com.esteban.ruano.workout_data.mappers.toLocalWorkoutDay
import com.esteban.ruano.workout_data.mappers.toDomainModel
import com.esteban.ruano.workout_domain.model.WorkoutDashboard
import com.esteban.ruano.workout_domain.model.Workout

class WorkoutLocalDataSource(
    private val workoutDao: WorkoutDao
): WorkoutDataSource {
    override suspend fun getWorkoutDays(): List<Workout> {
        return workoutDao.getWorkoutDays().map { it.toDomainModel() }
    }

    override suspend fun getWorkoutDaysWithExercises(): List<Workout> {
        return workoutDao.getWorkoutDaysWithExercises().map { it.toDomainModel() }
    }

    override suspend fun getWorkoutDayById(workoutId: String): Workout {
        return workoutDao.getWorkoutDayById(workoutId).toDomainModel()
    }

    override suspend fun getWorkoutDayStatus(workoutDayId: String,dateTime:String): List<ExerciseDayStatus> {
        TODO("Not yet implemented")
    }

    override suspend fun getExercisesByWorkoutDay(
        workoutDayId: String
    ): List<Exercise> {
        return workoutDao.getExercisesByWorkoutDay(
            workoutDayId
        )?.exercises?.map { it.toExercise() } ?: emptyList()
    }

    override suspend fun getExercises(): List<Exercise> {
        return workoutDao.getExercises().map { it.toExercise() }
    }

    override suspend fun saveExercise(exercise: Exercise):Unit {
        workoutDao.saveExercise(
            exercise.toLocalExercise()
        )
    }

    override suspend fun saveWorkoutDay(workout: Workout) {
        workoutDao.saveWorkoutDay(
            workout.toLocalWorkoutDay()
        )
    }

    override suspend fun getWorkoutDayByNumber(number: Int): Workout {
        return workoutDao.getWorkoutDayByNumber(number).toDomainModel()
    }

    override suspend fun linkExerciseWithWorkoutDay(workoutDayId: String, exerciseId: String) {
        workoutDao.linkExerciseWithWorkoutDay(workoutDayId, exerciseId)
    }

    override suspend fun updateWorkoutDay(workoutDayId: String, workout: Workout) {
        throw UnsupportedOperationException("Local data source does not support this operation")
    }

    override suspend fun getWorkoutDashboard(): WorkoutDashboard {
        throw UnsupportedOperationException("Local data source does not support this operation")
    }

    override suspend fun getExerciseById(exerciseId: String): Exercise {
        throw UnsupportedOperationException("Local data source does not support this operation")
    }

    override suspend fun addSet(dto: CreateExerciseSetTrackDTO,) {
        TODO("Not yet implemented")
    }

    override suspend fun removeSet(id: String) {
        TODO("Not yet implemented")
    }


}