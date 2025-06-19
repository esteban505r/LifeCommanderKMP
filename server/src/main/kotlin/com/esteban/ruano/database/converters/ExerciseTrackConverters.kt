package com.esteban.ruano.database.converters

import com.esteban.ruano.database.entities.ExerciseTrack
import com.esteban.ruano.models.workout.ExerciseTrackDTO
import com.esteban.ruano.utils.formatDateTime

fun ExerciseTrack.toDTO(): ExerciseTrackDTO {
    return ExerciseTrackDTO(
        id = this.id.value.toString(),
        exerciseId = this.exercise.id.value.toString(),
        workoutDayId = this.workoutDay.id.value.toString(),
        doneDateTime = formatDateTime(this.doneDateTime),
        status = this.status.name
    )
} 