package com.esteban.ruano.workout_domain.use_cases

import com.esteban.ruano.lifecommander.models.CreateExerciseSetTrackDTO
import com.esteban.ruano.lifecommander.models.Exercise
import com.esteban.ruano.workout_domain.repository.WorkoutRepository


class AddSet(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(
        exerciseId: String,
        reps: Int,
        dateTime: String,
        workoutDayId: String
    ): Result<Unit> {
        return repository.addSet(
            CreateExerciseSetTrackDTO(
                exerciseId = exerciseId,
                reps = reps,
                doneDateTime = dateTime,
                workoutDayId = workoutDayId
            )
        )
    }
}