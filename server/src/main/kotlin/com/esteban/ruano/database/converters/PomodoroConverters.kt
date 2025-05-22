package com.esteban.ruano.database.converters

import com.esteban.ruano.utils.formatDateTime
import com.esteban.ruano.database.entities.Pomodoro
import com.esteban.ruano.models.pomodoros.CreatePomodoroDTO
import com.esteban.ruano.models.pomodoros.PomodoroDTO
import com.esteban.ruano.models.pomodoros.UpdatePomodoroDTO
import com.esteban.ruano.utils.DateUIUtils.formatWithSeconds

fun Pomodoro.toDTO(): PomodoroDTO {
    return PomodoroDTO(
        id = this.id.toString(),
        startDateTime = this.startDateTime.formatWithSeconds(),
        endDateTime = this.endDateTime.formatWithSeconds(),
    )
}

fun PomodoroDTO.toCreatePomodoroDTO(): CreatePomodoroDTO {
    return CreatePomodoroDTO(
        startDateTime = this.startDateTime,
        endDateTime = this.endDateTime,
        createdAt = this.createdAt
    )
}

fun PomodoroDTO.toUpdatePomodoroDTO(): UpdatePomodoroDTO {
    return UpdatePomodoroDTO(
        startDateTime = this.startDateTime,
        endDateTime = this.endDateTime,
        updatedAt = this.updatedAt
    )
} 