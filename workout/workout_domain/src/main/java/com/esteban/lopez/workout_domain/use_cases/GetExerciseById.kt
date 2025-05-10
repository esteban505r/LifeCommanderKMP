package com.esteban.ruano.workout_domain.use_cases

import com.esteban.ruano.core.domain.model.DataException
import com.esteban.ruano.workout_domain.model.Exercise
import com.esteban.ruano.workout_domain.model.WorkoutDay
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