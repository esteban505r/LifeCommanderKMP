package com.esteban.ruano.repository

import com.esteban.ruano.models.tasks.TaskDTO
import com.esteban.ruano.models.workout.WorkoutDashboardDTO
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

    fun getByDay(userId: Int, day: Int): List<WorkoutDayDTO> {
        return workoutService.getWorkoutDaysByDay(
            userId,
            day
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


}