package com.esteban.ruano.repository

import com.esteban.ruano.models.tasks.TaskDTO
import com.esteban.ruano.models.workout.ExerciseDayStatusDTO
import com.esteban.ruano.models.workout.WorkoutDashboardDTO
import com.esteban.ruano.models.workout.WorkoutTrackDTO
import com.esteban.ruano.models.workout.ExerciseTrackDTO
import com.esteban.ruano.models.workout.day.UpdateWorkoutDayDTO
import com.esteban.ruano.models.workout.day.WorkoutDayDTO
import com.esteban.ruano.models.workout.exercise.ExerciseDTO
import com.esteban.ruano.service.WorkoutService
import com.esteban.ruano.utils.parseDate
import java.util.UUID

class WorkoutRepository(private val workoutService: WorkoutService) {

    fun getAll(userId: Int): List<WorkoutDayDTO> {
        return workoutService.getWorkoutDaysWithExercises(
            userId,
        )
    }

    fun getByDay(userId: Int, day: Int, dateTime:String): List<WorkoutDayDTO> {
        return workoutService.getWorkoutDaysByDay(
            userId,
            day,
            dateTime
        )
    }

    fun getById(userId: Int,workoutDayId: Int): WorkoutDayDTO {
        return workoutService.getWorkoutDayById(
            userId,workoutDayId
        )
    }

    fun getWorkoutDashboard(userId: Int): WorkoutDashboardDTO {
        return workoutService.getWorkoutDashboard(
            userId
        )
    }

    fun getExercises(userId: Int,filter: String, limit: Int, offset: Long): List<ExerciseDTO> {
        return workoutService.getExercises(
            userId,
            filter,
            limit,
            offset
        )
    }

    fun updateExercise(userId: Int, id: UUID, exercise: ExerciseDTO):Boolean{
        return workoutService.updateExercise(
            userId,id,exercise
        )
    }

    fun deleteExercise(userId: Int, id: UUID):Boolean{
        return workoutService.deleteExercise(
            userId,id
        )
    }

    fun createExercise(userId: Int, exercise: ExerciseDTO): UUID? {
        return workoutService.createExercise(
            userId,
            exercise
        )
    }

    fun updateWorkoutDay(userId: Int, workoutDayId: String, workoutDay: UpdateWorkoutDayDTO):Boolean {
        return workoutService.updateWorkoutDay(
            userId,
            workoutDayId,
            workoutDay
        )
    }

    // Workout Tracking Methods
    fun completeWorkout(userId: Int, workoutDayId: Int, doneDateTime: String): Boolean {
        return workoutService.completeWorkout(userId, workoutDayId, doneDateTime)
    }

    fun unCompleteWorkout(userId: Int, trackId: String): Boolean {
        return workoutService.unCompleteWorkout(userId, trackId)
    }

    fun getWorkoutsCompletedPerDayThisWeek(userId: Int): List<Int> {
        return workoutService.getWorkoutsCompletedPerDayThisWeek(userId)
    }

    fun getWorkoutTracksByDateRange(userId: Int, startDate: String, endDate: String): List<WorkoutTrackDTO> {
        return workoutService.getWorkoutTracksByDateRange(userId, startDate, endDate)
    }

    fun deleteWorkoutTrack(userId: Int, trackId: String): Boolean {
        return workoutService.deleteWorkoutTrack(userId, trackId)
    }

    fun unbindExerciseFromDay(userId: Int, exerciseId: String, workoutDayDay: Int): Boolean {
        return workoutService.unbindExerciseFromDay(userId, exerciseId, workoutDayDay)
    }

    fun bindExerciseToDay(userId: Int, exerciseId: String, workoutDayDay: Int): Boolean {
        return workoutService.bindExerciseToDay(userId, exerciseId, workoutDayDay)
    }

    // Exercise Tracking Methods
    fun completeExercise(userId: Int, exerciseId: String, workoutDayId: String, doneDateTime: String): Boolean {
        return workoutService.completeExercise(userId, exerciseId, workoutDayId, doneDateTime)
    }

    fun unCompleteExercise(userId: Int, trackId: String): Boolean {
        return workoutService.unCompleteExercise(userId, trackId)
    }




    fun getExerciseTracksByDateRange(userId: Int, startDate: String, endDate: String): List<ExerciseTrackDTO> {
        return workoutService.getExerciseTracksByDateRange(userId, startDate, endDate)
    }

    fun getCompletedExercisesForDay(userId: Int, workoutDayId: String, dateTime: String,
    ): List<ExerciseDayStatusDTO> {
        return workoutService.getExerciseSetStatusForDay(userId, workoutDayId,dateTime)
    }

    fun completeExerciseSet(userId: Int, exerciseId: String, workoutDayId: String, reps: Int, doneDateTime: String): Boolean {
        return workoutService.completeExerciseSet(
            userId, exerciseId, workoutDayId, reps, doneDateTime
        )
    }

    fun unCompleteExerciseSet(userId: Int, trackId: String): Boolean {
        return workoutService.unCompleteExerciseSet(
            userId,trackId
        )
    }

    fun getExercise(userId: Int, id: String): ExerciseDTO {
        return workoutService.getExercise(
            userId,id
        )
    }
}