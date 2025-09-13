package com.esteban.ruano.workout_domain.use_cases

import com.esteban.ruano.workout_domain.model.Workout
import com.esteban.ruano.workout_domain.repository.WorkoutRepository


class GetWorkoutDayById(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(
        id: Int
    ): Result<Workout> {
        return repository.getWorkoutDayById(
            workoutId = id
        )
    }
}