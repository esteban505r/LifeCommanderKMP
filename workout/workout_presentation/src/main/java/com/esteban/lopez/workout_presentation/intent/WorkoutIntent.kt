package com.esteban.ruano.workout_presentation.intent

import com.esteban.ruano.core_ui.view_model.Effect
import com.esteban.ruano.core_ui.view_model.UserIntent
import com.esteban.ruano.lifecommander.models.Exercise

sealed class WorkoutIntent : UserIntent {
    data object FetchDashboard : WorkoutIntent()
    data class FetchWorkoutByDay(val id: String) : WorkoutIntent()
    data class CompleteExercise(val id: Int) : WorkoutIntent()
    data class UpdateWorkoutDayExercises(val id: String, val exercises: List<Exercise>) : WorkoutIntent()

    data class DoRep(val id: String) : WorkoutIntent()
    data class UndoRep(val id: String) : WorkoutIntent()

    data object StartTimer : WorkoutIntent()
    
    // New intents for enhanced functionality
    data class CompleteExerciseById(val exerciseId: String, val workoutDayId: String) : WorkoutIntent()
    data class UnCompleteExercise(val trackId: String) : WorkoutIntent()
    data class CompleteWorkout(val dayId: Int) : WorkoutIntent()
    data class UnCompleteWorkout(val trackId: String) : WorkoutIntent()
    data class GetCompletedExercisesForDay(val workoutDayId: String) : WorkoutIntent()
    data object GetWorkoutsCompletedPerDayThisWeek : WorkoutIntent()
    data class DeleteExercise(val exerciseId: String) : WorkoutIntent()
    data class AddSet(val exerciseId: String, val reps:Int,val workoutDayId: String) : WorkoutIntent()
    data class RemoveSet(val setId: String,val onSuccess:()->Unit) : WorkoutIntent()
    data class UpdateExercise(val exercise: Exercise) : WorkoutIntent()
}
sealed class WorkoutEffect : Effect {

}
