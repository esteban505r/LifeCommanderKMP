package com.esteban.ruano.workout_domain.use_cases

import com.esteban.ruano.core.domain.model.DataException
import com.esteban.ruano.workout_domain.model.WorkoutDay
import com.esteban.ruano.workout_domain.repository.WorkoutRepository


class GetWorkoutDayByNumber(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(
        number: Int
    ): Result<WorkoutDay> {
        return repository.getWorkoutDayByNumber(
            number = number
        )
    }
}