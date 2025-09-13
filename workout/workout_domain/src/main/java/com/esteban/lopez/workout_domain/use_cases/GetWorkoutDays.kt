package com.esteban.ruano.workout_domain.use_cases

import com.esteban.ruano.workout_domain.model.Workout
import com.esteban.ruano.workout_domain.repository.WorkoutRepository


class GetWorkoutDays(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(
    ): Result<List<Workout>> {
        return repository.getWorkoutDays()
    }
}