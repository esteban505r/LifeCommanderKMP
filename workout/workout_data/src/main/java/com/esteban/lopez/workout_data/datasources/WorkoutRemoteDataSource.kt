package com.esteban.ruano.workout_data.datasources

import com.esteban.ruano.lifecommander.models.BindExercise
import com.esteban.ruano.lifecommander.models.CreateExerciseSetTrackDTO
import com.esteban.ruano.lifecommander.models.CreateExerciseTrack
import com.esteban.ruano.lifecommander.models.Exercise
import com.esteban.ruano.lifecommander.models.ExerciseDayStatus
import com.esteban.ruano.lifecommander.models.UnBindExercise
import com.esteban.ruano.workout_data.mappers.toExercise
import com.esteban.ruano.workout_data.mappers.toExerciseResponse
import com.esteban.ruano.workout_data.mappers.toDomainModel
import com.esteban.ruano.workout_data.mappers.toResponseModel
import com.esteban.ruano.workout_data.remote.WorkoutApi
import com.esteban.ruano.workout_domain.model.WorkoutDashboard
import com.esteban.ruano.workout_domain.model.Workout

class WorkoutRemoteDataSource(
    private val api:WorkoutApi
): WorkoutDataSource {
    override suspend fun getWorkoutDays(): List<Workout> = api.getWorkoutDays().map { it.toDomainModel() }
    override suspend fun getWorkoutDaysWithExercises(): List<Workout> {
        return api.getWorkoutDays().map { it.toDomainModel() }
    }

    override suspend fun getWorkoutDayById(workoutId: String): Workout {
        return api.getWorkoutDayById(workoutId).toDomainModel()
    }

    override suspend fun getWorkoutDayStatus(workoutDayId: String,dateTime:String): List<ExerciseDayStatus> {
        return api.getWorkoutDayStatus(workoutDayId,dateTime)
    }

    override suspend fun getExercisesByWorkoutDay(workoutDayId: String): List<Exercise> {
        return api.getExercisesByWorkoutDay(workoutDayId).map { it.toExercise() }
    }

    override suspend fun getExercises(): List<Exercise> {
        return api.getExercises().map { it.toExercise() }
    }

    override suspend fun saveExercise(exercise: Exercise) = api.saveExercise(exercise.toExerciseResponse())


    override suspend fun saveWorkoutDay(workout: Workout) {
        api.saveWorkoutDay(workout.toResponseModel())
    }

    override suspend fun getWorkoutDayByNumber(number: Int): Workout {
        return api.getWorkoutDayByNumber(number).toDomainModel()
    }

    override suspend fun linkExerciseWithWorkoutDay(workoutDayId: String, exerciseId: String) {
        api.linkExerciseWithWorkoutDay(
            BindExercise(
                workoutDayId = workoutDayId,
                exerciseId = exerciseId
            )
        )
    }

    override suspend fun unLinkExerciseWithWorkoutDay(
        workoutDayId: String,
        exerciseId: String
    ) {
        api.unLinkExerciseWithWorkoutDay(
            UnBindExercise(
                workoutDayId = workoutDayId,
                exerciseId = exerciseId
            )
        )
    }

    override suspend fun updateWorkoutDay(workoutDayId:String, workout: Workout) {
        api.updateWorkoutDay(workoutDayId,workout.toResponseModel())
    }

    override suspend fun getWorkoutDashboard(): WorkoutDashboard {
        return api.getWorkoutDashboard().toDomainModel()
    }

    override suspend fun getExerciseById(exerciseId: String): Exercise {
        return api.getExerciseById(exerciseId)
    }

    override suspend fun addSet(dto: CreateExerciseSetTrackDTO,): Unit {
        return api.addSet(dto)
    }

    override suspend fun removeSet(id: String) {
        return api.removeSet(id)
    }

    override suspend fun undoExercise(trackId: String) {
        return api.undoExercise(trackId)
    }

    override suspend fun completeExercise(track: CreateExerciseTrack) {
        return api.completeExercise(track)
    }

    override suspend fun updateExercise(
        id: String,
        exercise: Exercise
    ) {
        return api.updateExercise(id,exercise)
    }

    override suspend fun deleteExercise(
        id: String,
    ) {
        return api.deleteExercise(id,)
    }

}