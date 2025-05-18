package com.esteban.ruano.lifecommander.timer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class TimerWebSocketClientMessage {
    @Serializable
    @SerialName("StartTimerList")
    data class StartTimerList(val listId: String) : TimerWebSocketClientMessage()
}