package com.esteban.ruano.lifecommander.models.timers

import kotlinx.serialization.Serializable

@Serializable
data class CreateTimerListRequest(
    val name: String,
    val loopTimers: Boolean,
    val pomodoroGrouped: Boolean
)
