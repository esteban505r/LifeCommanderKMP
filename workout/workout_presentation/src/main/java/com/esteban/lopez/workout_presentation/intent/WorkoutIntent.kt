package com.esteban.ruano.workout_presentation.intent

import com.esteban.ruano.core_ui.view_model.Effect
import com.esteban.ruano.core_ui.view_model.UserIntent
import com.esteban.ruano.workout_domain.model.Exercise

sealed class WorkoutIntent : UserIntent {
    data object FetchDashboard : WorkoutIntent()
    data class FetchWorkoutDayById(val id: String) : WorkoutIntent()
    data class CompleteExercise(val id: Int) : WorkoutIntent()
    data class UpdateWorkoutDayExercises(val id: String, val exercises: List<Exercise>) : WorkoutIntent()

    data class DoRep(val id: String) : WorkoutIntent()
    data class UndoRep(val id: String) : WorkoutIntent()

    data object StartTimer : WorkoutIntent()
}
sealed class WorkoutEffect : Effect {

}
