package com.esteban.ruano.workout_domain.use_cases

import com.esteban.ruano.lifecommander.models.CreateExerciseTrack
import com.esteban.ruano.workout_domain.repository.WorkoutRepository


class CompleteExercise(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(
        track: CreateExerciseTrack,
    ): Result<Unit> {
        return repository.completeExercise(
            track
        )
    }
}