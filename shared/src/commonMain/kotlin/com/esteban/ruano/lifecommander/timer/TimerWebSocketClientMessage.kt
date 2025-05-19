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
}