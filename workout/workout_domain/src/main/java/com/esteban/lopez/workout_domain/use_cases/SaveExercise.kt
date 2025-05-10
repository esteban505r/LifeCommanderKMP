package com.esteban.ruano.workout_domain.use_cases

import com.esteban.ruano.core.domain.model.DataException
import com.esteban.ruano.workout_domain.model.Exercise
import com.esteban.ruano.workout_domain.model.WorkoutDay
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