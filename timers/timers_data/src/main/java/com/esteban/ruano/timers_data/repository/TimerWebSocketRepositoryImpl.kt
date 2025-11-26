package com.esteban.ruano.timers_data.repository

import com.esteban.ruano.core.domain.preferences.Preferences
import com.esteban.ruano.lifecommander.timer.TimerConnectionState
import com.esteban.ruano.lifecommander.timer.TimerNotification
import com.esteban.ruano.lifecommander.timer.TimerWebSocketClientMessage
import com.esteban.ruano.lifecommander.timer.TimerWebSocketServerMessage
import com.esteban.ruano.lifecommander.utils.SOCKETS_HOST
import com.esteban.ruano.lifecommander.utils.SOCKETS_PATH
import com.esteban.ruano.lifecommander.utils.SOCKETS_PORT
import com.esteban.ruano.lifecommander.utils.TokenStorage
import com.esteban.ruano.lifecommander.websocket.TimerWebSocketClient
import com.esteban.ruano.timers_domain.repository.TimerWebSocketRepository
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

class TimerWebSocketRepositoryImpl(
    private val httpClient: HttpClient,
    private val preferences: Preferences
) : TimerWebSocketRepository {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val tokenStorage: TokenStorage = object : TokenStorage {
        override suspend fun saveToken(token: String) {
            preferences.saveAuthToken(token)
        }

        override suspend fun getToken(): String? {
            return preferences.loadAuthToken().first().takeIf { it.isNotEmpty() }
        }

        override suspend fun clearToken() {
            preferences.clearAuthToken()
        }
    }

    private val webSocketClient: TimerWebSocketClient by lazy {
        TimerWebSocketClient(
            httpClient = httpClient,
            host = SOCKETS_HOST,
            port = SOCKETS_PORT,
            path = SOCKETS_PATH,
            tokenStorage = tokenStorage
        )
    }

    private val _incomingMessages = MutableSharedFlow<TimerWebSocketServerMessage>()
    override val incomingMessages: SharedFlow<TimerWebSocketServerMessage> = _incomingMessages.asSharedFlow()

    private val webSocketJson = Json {
        ignoreUnknownKeys = true
        classDiscriminator = "type"
        serializersModule = SerializersModule {
            polymorphic(TimerWebSocketServerMessage::class) {
                subclass(
                    TimerWebSocketServerMessage.TimerUpdate::class,
                    TimerWebSocketServerMessage.TimerUpdate.serializer()
                )
                subclass(
                    TimerWebSocketServerMessage.Pong::class,
                    TimerWebSocketServerMessage.Pong.serializer()
                )
                subclass(
                    TimerWebSocketServerMessage.TimerListUpdate::class,
                    TimerWebSocketServerMessage.TimerListUpdate.serializer()
                )
                subclass(
                    TimerWebSocketServerMessage.TimerListRefresh::class,
                    TimerWebSocketServerMessage.TimerListRefresh.serializer()
                )
            }
        }
    }

    init {
        // Parse incoming raw events into domain messages
        scope.launch {
            webSocketClient.incomingEvents.collect { event ->
                try {
                    if (event == "heartbeat") return@collect
                    val message = webSocketJson.decodeFromString<TimerWebSocketServerMessage>(event)
                    _incomingMessages.emit(message)
                } catch (e: Exception) {
                    // Silently ignore parsing errors
                }
            }
        }
    }

    override val connectionState: StateFlow<TimerConnectionState>
        get() = webSocketClient.connectionState

    override val timerNotifications: StateFlow<List<TimerNotification>>
        get() = webSocketClient.timerNotifications

    override fun connect() {
        webSocketClient.connect()
    }

    override fun disconnect() {
        webSocketClient.disconnect()
    }

    override fun sendPing(clientTime: Long) {
        val ping = TimerWebSocketClientMessage.Ping(clientTime = clientTime)
        val message = Json.encodeToString(TimerWebSocketClientMessage.serializer(), ping)
        webSocketClient.sendMessage(message)
    }

    override fun clearNotifications() {
        webSocketClient.clearNotifications()
    }
}

