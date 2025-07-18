package com.esteban.ruano.workout_data.datasources

import com.esteban.ruano.workout_data.mappers.toExercise
import com.esteban.ruano.workout_data.mappers.toExerciseResponse
import com.esteban.ruano.workout_data.mappers.toDomainModel
import com.esteban.ruano.workout_data.mappers.toResponseModel
import com.esteban.ruano.workout_data.remote.WorkoutApi
import com.esteban.ruano.workout_domain.model.Exercise
import com.esteban.ruano.workout_domain.model.WorkoutDashboard
import com.esteban.ruano.workout_domain.model.WorkoutDay

class WorkoutRemoteDataSource(
    private val api:WorkoutApi
): WorkoutDataSource {
    override suspend fun getWorkoutDays(): List<WorkoutDay> = api.getWorkoutDays().map { it.toDomainModel() }
    override suspend fun getWorkoutDaysWithExercises(): List<WorkoutDay> {
        return api.getWorkoutDays().map { it.toDomainModel() }
    }

    override suspend fun getWorkoutDayById(workoutId: String): WorkoutDay {
        return api.getWorkoutDayById(workoutId).toDomainModel()
    }

    override suspend fun getExercisesByWorkoutDay(workoutDayId: String): List<Exercise> {
        return api.getExercisesByWorkoutDay(workoutDayId).map { it.toExercise() }
    }

    override suspend fun getExercises(): List<Exercise> {
        return api.getExercises().map { it.toExercise() }
    }

    override suspend fun saveExercise(exercise: Exercise) = api.saveExercise(exercise.toExerciseResponse())


    override suspend fun saveWorkoutDay(workoutDay: WorkoutDay) {
        api.saveWorkoutDay(workoutDay.toResponseModel())
    }

    override suspend fun getWorkoutDayByNumber(number: Int): WorkoutDay {
        return api.getWorkoutDayByNumber(number).toDomainModel()
    }

    override suspend fun linkExerciseWithWorkoutDay(workoutDayId: String, exerciseId: String) {
        api.linkExerciseWithWorkoutDay(workoutDayId, exerciseId)
    }

    override suspend fun updateWorkoutDay(workoutDayId:String,workoutDay: WorkoutDay) {
        api.updateWorkoutDay(workoutDayId,workoutDay.toResponseModel())
    }

    override suspend fun getWorkoutDashboard(): WorkoutDashboard {
        return api.getWorkoutDashboard().toDomainModel()
    }

    override suspend fun getExerciseById(exerciseId: String): Exercise {
        return api.getExerciseById(exerciseId).toExercise()
    }

}