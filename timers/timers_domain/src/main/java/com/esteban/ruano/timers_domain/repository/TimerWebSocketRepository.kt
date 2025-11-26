package com.esteban.ruano.timers_domain.repository

import com.esteban.ruano.lifecommander.timer.TimerConnectionState
import com.esteban.ruano.lifecommander.timer.TimerNotification
import com.esteban.ruano.lifecommander.timer.TimerWebSocketServerMessage
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface TimerWebSocketRepository {
    val connectionState: StateFlow<TimerConnectionState>
    val timerNotifications: StateFlow<List<TimerNotification>>
    val incomingMessages: SharedFlow<TimerWebSocketServerMessage>
    
    fun connect()
    fun disconnect()
    fun sendPing(clientTime: Long)
    fun clearNotifications()
}

