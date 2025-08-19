package com.esteban.ruano.lifecommander.ui.state

import com.esteban.ruano.lifecommander.models.Exercise
import com.esteban.ruano.lifecommander.models.WorkoutTrack
import com.esteban.ruano.lifecommander.models.ExerciseTrack
import com.esteban.ruano.lifecommander.models.workout.day.WorkoutDay

data class WorkoutState(
    val exercises: List<Exercise> = emptyList(),
    val workoutDays: List<WorkoutDay> = emptyList(),
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String = "",
    val daySelected: Int = 0,
    val allExerciseMode: Boolean = false,
    val weeklyWorkoutsCompleted: List<Int> = emptyList(),
    val workoutTracks: List<WorkoutTrack> = emptyList(),
    val exerciseTracks: List<ExerciseTrack> = emptyList(),
    val completedExercises: Set<String> = emptySet(),
    val exerciseDayMap: Map<String, Set<Int>> = emptyMap(),
    val isCompleted: Boolean = false,
)