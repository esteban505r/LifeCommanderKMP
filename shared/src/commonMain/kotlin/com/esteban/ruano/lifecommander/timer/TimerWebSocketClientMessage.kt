package com.esteban.ruano.lifecommander.timer

import kotlinx.serialization.Serializable

@Serializable
sealed class TimerWebSocketClientMessage {
    @Serializable
    data class TimerUpdate(
        val listId: String,
        val timer: com.esteban.ruano.lifecommander.models.Timer,
        val remainingSeconds: Long,
    ) : TimerWebSocketClientMessage()
    
    @Serializable
    data class Ping(
        val clientTime: Long // Client epoch time in milliseconds
    ) : TimerWebSocketClientMessage()
    
    @Serializable
    data class SubscribeTimers(
        val scope: String = "user", // "user", "list", etc.
        val userId: String? = null,
        val listId: String? = null
    ) : TimerWebSocketClientMessage()
}