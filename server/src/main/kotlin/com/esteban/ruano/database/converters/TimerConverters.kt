package com.esteban.ruano.database.converters

import com.esteban.ruano.database.entities.Timer
import com.esteban.ruano.database.entities.TimerList
import com.esteban.ruano.service.TimerTimeCalculator
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun Timer.toDomainModel(): com.esteban.ruano.lifecommander.models.Timer {
    // Calculate remaining seconds using server-authoritative time calculation
    val remainingSeconds = TimerTimeCalculator.calculateRemainingSeconds(this)
    
    return com.esteban.ruano.lifecommander.models.Timer(
        id = this.id.value.toString(),
        name = this.name,
        duration = this.duration,
        enabled = this.enabled,
        countsAsPomodoro = this.countsAsPomodoro,
        sendNotificationOnComplete = this.sendNotificationOnComplete,
        order = this.order,
        state = this.state.toString(),
        remainingSeconds = remainingSeconds
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

