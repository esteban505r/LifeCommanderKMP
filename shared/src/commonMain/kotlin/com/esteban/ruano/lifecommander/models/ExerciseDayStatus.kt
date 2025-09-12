package com.esteban.ruano.lifecommander.models

import kotlinx.serialization.Serializable

@Serializable

data class ExerciseDayStatus(
    val exerciseTrackId: String,
    val exerciseId: String,
    val setsCountDone: Int,
    val totalRepsDone: Int,
    val exerciseDone: Boolean,
    val setsDone: List<ExerciseSet>
)