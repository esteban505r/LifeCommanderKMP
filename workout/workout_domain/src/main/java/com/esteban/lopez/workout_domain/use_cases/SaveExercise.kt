package com.esteban.ruano.workout_domain.use_cases

import com.esteban.ruano.lifecommander.models.Exercise
import com.esteban.ruano.workout_domain.repository.WorkoutRepository


class SaveExercise(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(
        exercise: Exercise
    ): Result<Unit> {
        return repository.saveExercise(
            exercise,
        )
    }
}