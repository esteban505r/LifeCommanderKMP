package com.esteban.ruano.models.workout

import com.esteban.ruano.lifecommander.models.ExerciseSet
import kotlinx.serialization.Serializable

@Serializable
data class ExerciseTrackDTO(
    val id: String,
    val exerciseId: String,
    val workoutDayId: String,
    val doneDateTime: String? = null,
    val dateTime: String? = null,
    val status: String = "ACTIVE"
)

@Serializable
data class CreateExerciseTrackDTO(
    val exerciseId: String,
    val workoutDayId: String,
    val doneDateTime: String
)

@Serializable
data class CreateExerciseSetTrackDTO(
    val exerciseId: String,
    val workoutDayId: String,
    val reps: Int,
    val doneDateTime: String
)

@Serializable
data class ExerciseDayStatusDTO(
    val exerciseTrackId: String,
    val exerciseId: String,
    val setsCountDone: Int,
    val totalRepsDone: Int,
    val exerciseDone: Boolean,
    val setsDone: List<ExerciseSet>
)
