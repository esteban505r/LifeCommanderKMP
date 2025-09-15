package com.esteban.ruano.workout_domain.use_cases

import com.esteban.ruano.lifecommander.models.ExerciseDayStatus
import com.esteban.ruano.workout_domain.model.Workout
import com.esteban.ruano.workout_domain.repository.WorkoutRepository


class GetWorkoutDayStatus(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(
        workoutDayId:String,
        dateTime:String
    ): Result<List<ExerciseDayStatus>> {
        return repository.getWorkoutDayStatus(
            workoutDayId = workoutDayId,
            dateTime = dateTime
        )
    }
}