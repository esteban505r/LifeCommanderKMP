package com.esteban.ruano.database.converters

import com.esteban.ruano.database.entities.Timer
import com.esteban.ruano.database.entities.TimerList


fun Timer.toDomainModel(): com.esteban.ruano.lifecommander.models.Timer {
    return com.esteban.ruano.lifecommander.models.Timer(
        id = this.id.value.toString(),
        name = this.name,
        duration = this.duration,
        enabled = this.enabled,
        countsAsPomodoro = this.countsAsPomodoro,
        order = this.order,
        state = this.state.toString()
    )
}

fun TimerList.toDomainModel(): com.esteban.ruano.lifecommander.models.TimerList {
    return com.esteban.ruano.lifecommander.models.TimerList(
        id = this.id.value.toString(),
        name = this.name,
        loopTimers = this.loopTimers,
        pomodoroGrouped = this.pomodoroGrouped,
        status = this.status.toString()
    )
}

