package com.esteban.ruano.workout_domain.use_cases

import com.esteban.ruano.workout_domain.model.Workout
import com.esteban.ruano.workout_domain.repository.WorkoutRepository


class GetWorkoutDayByNumber(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(
        number: Int
    ): Result<Workout> {
        return repository.getWorkoutDayByNumber(
            number = number
        )
    }
}