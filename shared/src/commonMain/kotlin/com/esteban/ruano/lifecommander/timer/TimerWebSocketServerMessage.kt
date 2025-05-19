package com.esteban.ruano.lifecommander.timer

import com.esteban.ruano.lifecommander.models.Timer
import kotlinx.serialization.Serializable

@Serializable
sealed class TimerWebSocketServerMessage {
    @Serializable
    data class TimerUpdate(
        val listId: String,
        val timer: Timer,
        val remainingTime: Long,
    ) : TimerWebSocketServerMessage()



}