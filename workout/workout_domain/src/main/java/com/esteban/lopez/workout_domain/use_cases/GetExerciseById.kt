package com.esteban.ruano.workout_domain.use_cases

import com.esteban.ruano.lifecommander.models.Exercise
import com.esteban.ruano.workout_domain.repository.WorkoutRepository


class GetExerciseById(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(
        id: String
    ): Result<Exercise> {
        return repository.getExerciseById(
            exerciseId = id
        )
    }
}