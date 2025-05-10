package com.esteban.ruano.workout_domain.use_cases

import com.esteban.ruano.workout_domain.model.WorkoutDashboard
import com.esteban.ruano.workout_domain.repository.WorkoutRepository


class GetWorkoutDashboard(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(
    ): Result<WorkoutDashboard> {
        return repository.getWorkoutDashboard()
    }
}