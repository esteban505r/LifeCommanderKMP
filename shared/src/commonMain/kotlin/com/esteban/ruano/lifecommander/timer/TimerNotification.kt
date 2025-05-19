package com.esteban.ruano.lifecommander.timer

import kotlinx.serialization.Serializable

@Serializable
data class TimerNotification(
    val type: String,
    val timerId: String,
    val listId: String,
    val status: String,
    val remainingTime: Long? = null
)