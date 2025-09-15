package com.esteban.ruano.workout_domain.use_cases

import com.esteban.ruano.lifecommander.models.CreateExerciseSetTrackDTO
import com.esteban.ruano.lifecommander.models.Exercise
import com.esteban.ruano.workout_domain.repository.WorkoutRepository


class RemoveSet(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(
        setId: String,
    ): Result<Unit> {
        return repository.removeSet(
            setId
        )
    }
}