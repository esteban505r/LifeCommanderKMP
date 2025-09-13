package com.esteban.ruano.workout_domain.use_cases

import com.esteban.ruano.workout_domain.model.Workout
import com.esteban.ruano.workout_domain.repository.WorkoutRepository


class SaveWorkoutDay(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(
        workout: Workout
    ): Result<Unit> {
        return repository.saveWorkoutDay(
            workout
        )
    }
}