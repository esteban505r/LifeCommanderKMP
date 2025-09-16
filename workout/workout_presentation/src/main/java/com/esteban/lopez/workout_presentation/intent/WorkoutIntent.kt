package com.esteban.ruano.workout_presentation.intent

import com.esteban.ruano.core_ui.view_model.Effect
import com.esteban.ruano.core_ui.view_model.UserIntent
import com.esteban.ruano.lifecommander.models.Exercise

sealed class WorkoutIntent : UserIntent {
    data object FetchDashboard : WorkoutIntent()
    data class FetchWorkoutByDay(val id: String) : WorkoutIntent()
    data class CompleteExercise(val id: Int) : WorkoutIntent()
    data class UpdateWorkoutDayExercises(val id: String, val exercises: List<Exercise>) :
        WorkoutIntent()

    data class DoRep(val id: String) : WorkoutIntent()
    data class UndoRep(val id: String) : WorkoutIntent()

    data object StartTimer : WorkoutIntent()

    data class CompleteExerciseById(
        val exerciseId: String,
        val workoutDayId: String,
        val doneDateTime: String,
        val onSuccess: () -> Unit
    ) : WorkoutIntent()

    data class UnCompleteExercise(val trackId: String) : WorkoutIntent()
    data class CompleteWorkout(val dayId: Int) : WorkoutIntent()
    data class UnCompleteWorkout(val trackId: String) : WorkoutIntent()
    data class GetCompletedExercisesForDay(val workoutDayId: String) : WorkoutIntent()
    data object GetWorkoutsCompletedPerDayThisWeek : WorkoutIntent()
    data class DeleteExercise(val exerciseId: String,val onSuccess: () -> Unit) : WorkoutIntent()
    data class AddSet(val exerciseId: String, val reps: Int, val workoutDayId: String) :
        WorkoutIntent()

    data class RemoveSet(val setId: String, val onSuccess: () -> Unit) : WorkoutIntent()
    data class UndoExercise(val exerciseId: String, val onSuccess: () -> Unit) : WorkoutIntent()
    data class UpdateExercise(val exercise: Exercise) : WorkoutIntent()
    data class UnlinkExerciseFromDay(val exerciseId:String,val day:String, val onSuccess: () -> Unit) : WorkoutIntent()
}

sealed class WorkoutEffect : Effect {

}
