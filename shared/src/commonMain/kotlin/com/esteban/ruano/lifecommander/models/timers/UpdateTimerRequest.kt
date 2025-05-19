package com.esteban.ruano.lifecommander.models.timers

import kotlinx.serialization.Serializable

@Serializable
data class UpdateTimerRequest(
    val name: String? = null,
    val timerListId: String ? = null,
    val duration: Long ? = null,
    val enabled: Boolean ? = null,
    val countsAsPomodoro: Boolean ? = null,
    val order: Int ? = null
)