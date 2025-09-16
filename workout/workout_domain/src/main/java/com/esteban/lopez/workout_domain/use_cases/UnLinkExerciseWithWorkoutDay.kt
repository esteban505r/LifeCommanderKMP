package com.esteban.ruano.workout_domain.use_cases

import com.esteban.ruano.workout_domain.repository.WorkoutRepository


class UnLinkExerciseWithWorkoutDay(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(
        workoutDayId: Int,
        exerciseId: String
    ): Result<Unit> {
        return repository.unlinkExerciseWithWorkoutDay(
            workoutDayId,
            exerciseId
        )
    }
}