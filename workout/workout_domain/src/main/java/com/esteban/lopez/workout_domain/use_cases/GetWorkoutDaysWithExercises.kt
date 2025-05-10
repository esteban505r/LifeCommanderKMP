package com.esteban.ruano.workout_domain.use_cases

import com.esteban.ruano.core.domain.model.DataException
import com.esteban.ruano.workout_domain.model.WorkoutDay
import com.esteban.ruano.workout_domain.repository.WorkoutRepository


class GetWorkoutDaysWithExercises(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(
    ): Result<List<WorkoutDay>> {
        return repository.getWorkoutDaysWithExercises()
    }
}