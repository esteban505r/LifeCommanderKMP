package com.esteban.ruano.lifecommander.service

import com.esteban.ruano.lifecommander.models.Timer
import com.esteban.ruano.lifecommander.models.TimerList
import com.esteban.ruano.lifecommander.timer.TimerNotification
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.HttpMethod
import io.ktor.websocket.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class TimerService(
    private val httpClient: HttpClient,
    private val baseUrl: String,
    private val authToken: String
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var session: WebSocketSession? = null

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _timerNotifications = MutableStateFlow<List<TimerNotification>>(emptyList())
    val timerNotifications: StateFlow<List<TimerNotification>> = _timerNotifications.asStateFlow()

    private val _activeTimer = MutableStateFlow<ActiveTimer?>(null)
    val activeTimer: StateFlow<ActiveTimer?> = _activeTimer.asStateFlow()

    sealed class ConnectionState {
        object Connected : ConnectionState()
        object Disconnected : ConnectionState()
        data class Error(val message: String) : ConnectionState()
    }

    data class ActiveTimer(
        val timer: Timer,
        val listId: String,
        val remainingTime: Int,
        val status: TimerStatus
    )

    enum class TimerStatus {
        RUNNING, PAUSED, STOPPED, COMPLETED
    }

    fun connect() {
        scope.launch {
            try {
                _connectionState.value = ConnectionState.Connected
                httpClient.webSocket(
                    method = HttpMethod.Get,
                    host = baseUrl,
                    path = "/timer/notifications"
                ) {
                    session = this
                    println("WebSocket connected")

                    try {
                        for (frame in incoming) {
                            when (frame) {
                                is Frame.Text -> {
                                    val text = frame.readText()
                                    handleMessage(text)
                                }
                                is Frame.Close -> {
                                    println("WebSocket closed")
                                    break
                                }
                                else -> {
                                    println("Received unsupported frame type: ${frame.frameType}")
                                }
                            }
                        }
                    } catch (e: Exception) {
                        println("Error in WebSocket session: ${e.message}")
                        _connectionState.value = ConnectionState.Error(e.message ?: "Unknown error")
                    } finally {
                        session = null
                        _connectionState.value = ConnectionState.Disconnected
                    }
                }
            } catch (e: Exception) {
                println("Failed to connect to WebSocket: ${e.message}")
                _connectionState.value = ConnectionState.Error(e.message ?: "Failed to connect")
            }
        }
    }

    fun disconnect() {
        scope.launch {
            try {
                session?.close()
                session = null
                _connectionState.value = ConnectionState.Disconnected
                println("WebSocket disconnected")
            } catch (e: Exception) {
                println("Error disconnecting WebSocket: ${e.message}")
            }
        }
    }

    private suspend fun handleMessage(text: String) {
        try {
            val notification = Json.decodeFromString<TimerNotification>(text)
            _timerNotifications.value = _timerNotifications.value + notification
            
            // Update active timer state based on notification
            when (notification.type) {
                "TIMER_STARTED" -> {
                    _activeTimer.value?.let { currentTimer ->
                        if (currentTimer.timer.id == notification.timerId) {
                            _activeTimer.value = currentTimer.copy(
                                status = TimerStatus.RUNNING,
                                remainingTime = notification.remainingTime ?: currentTimer.remainingTime
                            )
                        }
                    }
                }
                "TIMER_PAUSED" -> {
                    _activeTimer.value?.let { currentTimer ->
                        if (currentTimer.timer.id == notification.timerId) {
                            _activeTimer.value = currentTimer.copy(
                                status = TimerStatus.PAUSED,
                                remainingTime = notification.remainingTime ?: currentTimer.remainingTime
                            )
                        }
                    }
                }
                "TIMER_STOPPED" -> {
                    _activeTimer.value?.let { currentTimer ->
                        if (currentTimer.timer.id == notification.timerId) {
                            _activeTimer.value = currentTimer.copy(
                                status = TimerStatus.STOPPED,
                                remainingTime = currentTimer.timer.duration
                            )
                        }
                    }
                }
                "TIMER_COMPLETED" -> {
                    _activeTimer.value?.let { currentTimer ->
                        if (currentTimer.timer.id == notification.timerId) {
                            _activeTimer.value = currentTimer.copy(
                                status = TimerStatus.COMPLETED,
                                remainingTime = 0
                            )
                        }
                    }
                }
            }
            
            println("Received timer notification: $notification")
        } catch (e: Exception) {
            println("Error parsing WebSocket message: $text")
            println("Error details: ${e.message}")
        }
    }

    fun startTimer(timer: Timer, listId: String) {
        scope.launch {
            _activeTimer.value = ActiveTimer(
                timer = timer,
                listId = listId,
                remainingTime = timer.duration,
                status = TimerStatus.RUNNING
            )
            println("Started timer: ${timer.name}")
            // TODO: Send start command to server
        }
    }

    fun pauseTimer() {
        scope.launch {
            _activeTimer.value?.let { currentTimer ->
                _activeTimer.value = currentTimer.copy(status = TimerStatus.PAUSED)
                println("Paused timer: ${currentTimer.timer.name}")
                // TODO: Send pause command to server
            }
        }
    }

    fun resumeTimer() {
        scope.launch {
            _activeTimer.value?.let { currentTimer ->
                _activeTimer.value = currentTimer.copy(status = TimerStatus.RUNNING)
                println("Resumed timer: ${currentTimer.timer.name}")
                // TODO: Send resume command to server
            }
        }
    }

    fun stopTimer() {
        scope.launch {
            _activeTimer.value?.let { currentTimer ->
                _activeTimer.value = currentTimer.copy(
                    status = TimerStatus.STOPPED,
                    remainingTime = currentTimer.timer.duration
                )
                println("Stopped timer: ${currentTimer.timer.name}")
                // TODO: Send stop command to server
            }
        }
    }

    fun updateRemainingTime(remainingTime: Int) {
        scope.launch {
            _activeTimer.value?.let { currentTimer ->
                if (currentTimer.status == TimerStatus.RUNNING) {
                    _activeTimer.value = currentTimer.copy(remainingTime = remainingTime)
                    println("Updated remaining time for ${currentTimer.timer.name}: $remainingTime seconds")
                }
            }
        }
    }

    fun clearNotifications() {
        _timerNotifications.value = emptyList()
        println("Cleared all timer notifications")
    }
} 