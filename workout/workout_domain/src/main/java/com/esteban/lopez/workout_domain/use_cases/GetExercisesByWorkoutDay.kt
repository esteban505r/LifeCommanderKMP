package com.esteban.ruano.workout_domain.use_cases

import com.esteban.ruano.lifecommander.models.Exercise
import com.esteban.ruano.workout_domain.repository.WorkoutRepository


class GetExercisesByWorkoutDay(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(
        workoutDayId: Int
    ): Result<List<Exercise>> {
        return repository.getExercisesByWorkoutDay(
            workoutDayId
        )
    }
}