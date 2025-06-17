package com.esteban.ruano.database.converters

import com.esteban.ruano.database.entities.WorkoutTrack
import com.esteban.ruano.models.workout.CreateWorkoutTrackDTO
import com.esteban.ruano.models.workout.WorkoutTrackDTO
import com.esteban.ruano.utils.formatDateTime

fun WorkoutTrack.toDTO(): WorkoutTrackDTO {
    return WorkoutTrackDTO(
        id = this.id.value.toString(),
        workoutDayId = this.workoutDay.id.value.toString(),
        doneDateTime = formatDateTime(this.doneDateTime),
        status = this.status.name
    )
}

