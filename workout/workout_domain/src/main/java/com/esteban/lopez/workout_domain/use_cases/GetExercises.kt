package com.esteban.ruano.workout_domain.use_cases

import com.esteban.ruano.lifecommander.models.Exercise
import com.esteban.ruano.workout_domain.repository.WorkoutRepository


class GetExercises(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(): Result<List<Exercise>> {
        return repository.getExercises()
    }
}