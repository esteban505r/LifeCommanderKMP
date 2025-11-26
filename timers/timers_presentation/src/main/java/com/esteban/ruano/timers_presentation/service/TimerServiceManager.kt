package com.esteban.ruano.timers_presentation.service

import com.esteban.ruano.core.domain.preferences.Preferences
import com.esteban.ruano.lifecommander.timer.TimerConnectionState
import com.esteban.ruano.timers_domain.repository.TimerWebSocketRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimerServiceManager @Inject constructor(
    private val webSocketRepository: TimerWebSocketRepository,
    private val preferences: Preferences
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isInitialized = false

    fun initialize() {
        if (isInitialized) return
        isInitialized = true

        scope.launch {
            // Wait for authentication token to be available
            val token = preferences.loadAuthToken().first()
            if (token.isNotEmpty()) {
                connectWebSocket()
            }
        }

        // Start periodic ping for time synchronization
        scope.launch {
            while (true) {
                delay(15000) // Every 15 seconds
                if (isConnected()) {
                    val clientTime = System.currentTimeMillis()
                    webSocketRepository.sendPing(clientTime)
                }
            }
        }
    }

    fun connectWebSocket() {
        webSocketRepository.connect()
    }

    fun disconnectWebSocket() {
        webSocketRepository.disconnect()
    }

    private fun isConnected(): Boolean {
        return webSocketRepository.connectionState.value is TimerConnectionState.Connected
    }
}

