package com.esteban.ruano.lifecommander.websocket

import com.esteban.ruano.lifecommander.utils.TokenStorage
import com.esteban.ruano.lifecommander.timer.TimerNotification
import com.esteban.ruano.lifecommander.utils.appHeaders
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.HttpMethod
import io.ktor.websocket.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import io.ktor.client.plugins.timeout
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class TimerWebSocketClient(
    private val httpClient: HttpClient,
    private val host: String,
    private val port: Int,
    private val path: String,
    private val tokenStorage: TokenStorage
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var session: WebSocketSession? = null
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _timerNotifications = MutableStateFlow<List<TimerNotification>>(emptyList())
    val timerNotifications: StateFlow<List<TimerNotification>> = _timerNotifications.asStateFlow()

    private val _incomingEvents = MutableSharedFlow<String>()
    val incomingEvents: SharedFlow<String> = _incomingEvents

    sealed class ConnectionState {
        object Connected : ConnectionState()
        object Disconnected : ConnectionState()
        object Reconnecting : ConnectionState()
        data class Error(val message: String) : ConnectionState()
    }

    fun connect() {
        scope.launch {
            var attempts = 0
            try {
                val token = tokenStorage.getToken()
                if (token == null) {
                    println("❌ No token found. Cannot connect to WebSocket.")
                    return@launch
                }
                println("Connecting to ws://localhost:8080/api/v1/timers/notifications")
                httpClient.webSocket(
                    host = host,
                    path = "$path/timers/notifications",
                    method = HttpMethod.Get,
                    port = port,
                    request = {
                        appHeaders(
                            token = token,
                        )
                        timeout { requestTimeoutMillis = 10000 }
                    }
                ) {
                    val currentSession = this
                    session = currentSession

                    println("✅ WebSocket connected")
                    _connectionState.value = ConnectionState.Connected

                    try {
                        for (frame in incoming) {
                            when (frame) {
                                is Frame.Text -> {
                                    val text = frame.readText()
                                    handleMessage(text)
                                }
                                is Frame.Close -> {
                                    println("🔌 WebSocket closed by server")
                                    break
                                }
                                else -> {
                                    println("⚠️ Unsupported frame type: ${frame.frameType}")
                                }
                            }
                        }
                    } catch (e: Exception) {
                        println("❌ Error during WebSocket session: ${e.message}")
                        e.printStackTrace()
                        _connectionState.value = ConnectionState.Error(e.message ?: "Unknown error")
                    } finally {
                        try {
                            session?.close(CloseReason(CloseReason.Codes.NORMAL, "Client cleanup"))
                        } catch (e: Exception) {
                            println("⚠️ Failed to close WebSocket cleanly: ${e.message}")
                            e.printStackTrace()
                        }

                        session = null
                        _connectionState.value = ConnectionState.Disconnected

                        if (attempts < 3) {
                            attempts++
                        } else {
                            println("❌ Max reconnection attempts reached. Giving up.")
                            _connectionState.value = ConnectionState.Disconnected
                        }

                        // 🔁 Reconnect after delay
                        println("🔄 Attempting to reconnect in 5 seconds...")
                        _connectionState.value = ConnectionState.Reconnecting
                        delay(5000)
                        connect()
                    }
                }
            } catch (e: Exception) {
                println("🚫 Failed to connect to WebSocket: ${e.message}")
                e.printStackTrace()
                _connectionState.value = ConnectionState.Error(e.message ?: "Failed to connect")

                if(attempts < 3) {
                    attempts++
                } else {
                    _connectionState.value = ConnectionState.Disconnected
                    return@launch
                }

                println("🔄 Retry connection in 5 seconds...")
                _connectionState.value = ConnectionState.Reconnecting
                delay(5000)
                connect()
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
        _incomingEvents.emit(text)
        /*try {
            val notification = Json.decodeFromString<TimerNotification>(text)
            _timerNotifications.value = _timerNotifications.value + notification
            println("Received timer notification: $notification")
        } catch (e: Exception) {
            println("Error parsing WebSocket message: $text")
            println("Error details: ${e.message}")
        }*/
    }

    fun clearNotifications() {
        _timerNotifications.value = emptyList()
        println("Cleared all timer notifications")
    }

    fun sendMessage(message: String) {
        scope.launch {
            session?.send(Frame.Text(message))
            println("Sent message: $message")
        }
    }
} 