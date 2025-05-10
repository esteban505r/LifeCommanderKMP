package com.esteban.ruano.workout_domain.use_cases

import com.esteban.ruano.core.domain.model.DataException
import com.esteban.ruano.workout_domain.model.Exercise
import com.esteban.ruano.workout_domain.model.WorkoutDay
import com.esteban.ruano.workout_domain.repository.WorkoutRepository


class LinkExerciseWithWorkoutDay(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(
        workoutDayId: Int,
        exerciseId: Int
    ): Result<Unit> {
        return repository.linkExerciseWithWorkoutDay(
            workoutDayId,
            exerciseId
        )
    }
}