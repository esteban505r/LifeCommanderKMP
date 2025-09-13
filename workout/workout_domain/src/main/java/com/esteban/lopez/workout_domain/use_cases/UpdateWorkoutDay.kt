package com.esteban.ruano.workout_domain.use_cases

import com.esteban.ruano.workout_domain.model.Workout
import com.esteban.ruano.workout_domain.repository.WorkoutRepository


class UpdateWorkoutDay(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(
        id: String,
        workout: Workout
    ): Result<Unit> {
        return repository.updateWorkoutDay(
            id,
            workout
        )
    }
}