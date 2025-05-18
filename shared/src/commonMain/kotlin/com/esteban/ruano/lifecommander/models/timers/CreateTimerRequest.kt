package com.esteban.ruano.lifecommander.models.timers

import kotlinx.serialization.Serializable

@Serializable
data class CreateTimerRequest(
    val name: String,
    val timerListId: String,
    val duration: Int,
    val enabled: Boolean,
    val countsAsPomodoro: Boolean,
    val order: Int
)