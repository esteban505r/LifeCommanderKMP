package com.esteban.ruano.workout_domain.use_cases

import com.esteban.ruano.workout_domain.repository.WorkoutRepository


class UndoExercise(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(
        trackId: String,
    ): Result<Unit> {
        return repository.undoExercise(
            trackId
        )
    }
}