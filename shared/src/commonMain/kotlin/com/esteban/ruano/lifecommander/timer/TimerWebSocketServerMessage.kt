package com.esteban.ruano.lifecommander.timer

import com.esteban.ruano.lifecommander.models.Timer
import com.esteban.ruano.lifecommander.models.TimerList
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class TimerWebSocketServerMessage {
    @Serializable
    @SerialName("TimerUpdate")
    data class TimerUpdate(
        val listId: String,
        val timer: Timer,
        val remainingTime: Long,
    ) : TimerWebSocketServerMessage()

    @Serializable
    @SerialName("Pong")
    data class Pong(
        val serverTime: Long, // Server epoch time in milliseconds
        val clientTime: Long? = null // Echo back client time for round-trip calculation
    ) : TimerWebSocketServerMessage()
    
    @Serializable
    @SerialName("TimerListUpdate")
    data class TimerListUpdate(
        val timerList: TimerList,
        val action: String // "created", "updated", "deleted"
    ) : TimerWebSocketServerMessage()
    
    @Serializable
    @SerialName("TimerListRefresh")
    data class TimerListRefresh(
        val listId: String? = null // If null, refresh all lists
    ) : TimerWebSocketServerMessage()
}