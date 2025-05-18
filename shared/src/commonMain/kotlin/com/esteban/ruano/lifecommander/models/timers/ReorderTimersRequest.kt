package com.esteban.ruano.lifecommander.models.timers

import kotlinx.serialization.Serializable

@Serializable
data class ReorderTimersRequest(
    val timers: List<TimerOrder>
)

@Serializable
data class TimerOrder(
    val timerId: String,
    val order: Int
) 