package com.esteban.ruano.lifecommander.models.timers

import kotlinx.serialization.Serializable

@Serializable
data class CreateTimerRequest(
    val name: String,
    val timerListId: String,
    val duration: Long,
    val enabled: Boolean,
    val countsAsPomodoro: Boolean,
    val sendNotificationOnComplete: Boolean = true,
    val order: Int
)