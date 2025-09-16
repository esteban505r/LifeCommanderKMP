package com.esteban.ruano.workout_domain.use_cases

import com.esteban.ruano.lifecommander.models.Exercise
import com.esteban.ruano.workout_domain.repository.WorkoutRepository


class DeleteExercise(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(
        id:String
    ): Result<Unit> {
        return repository.deleteExercise(
            id,
        )
    }
}