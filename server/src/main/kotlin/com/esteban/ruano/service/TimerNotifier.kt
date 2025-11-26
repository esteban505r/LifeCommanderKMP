package com.esteban.ruano.service

import com.esteban.ruano.lifecommander.models.Timer
import com.esteban.ruano.lifecommander.models.TimerList
import com.esteban.ruano.lifecommander.timer.TimerWebSocketServerMessage
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.seconds

// Json instance configured for polymorphic serialization of WebSocket messages
val webSocketJson = Json {
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

object TimerNotifier {
    // Map of user ID to their WebSocket sessions
    private val userSessions = ConcurrentHashMap<Int, MutableSet<DefaultWebSocketSession>>()

    /**
     * Register a new WebSocket session for a user
     */
    fun registerSession(userId: Int, session: DefaultWebSocketSession) {
        userSessions.computeIfAbsent(userId) { mutableSetOf() }.add(session)
        println("User $userId connected to timer notifications")

        // Start heartbeat for this session
        launchHeartbeat(userId, session)
    }

    /**
     * Unregister a WebSocket session for a user
     */
    fun unregisterSession(userId: Int, session: DefaultWebSocketSession) {
        userSessions[userId]?.remove(session)
        if (userSessions[userId]?.isEmpty() == true) {
            userSessions.remove(userId)
        }
        println("User $userId disconnected from timer notifications")
    }

    /**
     * Broadcast a timer update to the timer's owner
     */
    suspend fun broadcastUpdate(timerWebSocketServerMessage: TimerWebSocketServerMessage, userId: Int) {
        val sessions = userSessions[userId] ?: return

        val message = webSocketJson.encodeToString(
            TimerWebSocketServerMessage.serializer(),
            timerWebSocketServerMessage
        )
        val deadSessions = mutableSetOf<DefaultWebSocketSession>()

        sessions.forEach { session ->
            try {
                session.send(Frame.Text(message))
            } catch (e: ClosedSendChannelException) {
                deadSessions.add(session)
                println("Failed to send timer update to user $userId: ${e.message}")
            } catch (e: Exception) {
                deadSessions.add(session)
                println("Error sending timer update to user $userId: ${e.message}")
            }
        }

        // Clean up dead sessions
        deadSessions.forEach { session ->
            unregisterSession(userId, session)
        }
    }

    /**
     * Broadcast a timer update to all connected users
     * Useful for system-wide notifications or admin features
     */
    suspend fun broadcastToAll(timer: Timer) {
        val message = Json.encodeToString(timer)
        val deadSessions = mutableSetOf<Pair<Int, DefaultWebSocketSession>>()

        userSessions.forEach { (userId, sessions) ->
            sessions.forEach { session ->
                try {
                    session.send(Frame.Text(message))
                } catch (e: ClosedSendChannelException) {
                    deadSessions.add(userId to session)
                    println("Failed to send timer update to user $userId: ${e.message}")
                } catch (e: Exception) {
                    deadSessions.add(userId to session)
                    println("Error sending timer update to user $userId: ${e.message}")
                }
            }
        }

        // Clean up dead sessions
        deadSessions.forEach { (userId, session) ->
            unregisterSession(userId, session)
        }
    }

    /**
     * Get the number of connected users
     */
    fun getConnectedUserCount(): Int = userSessions.size

    /**
     * Get the number of active sessions for a specific user
     */
    fun getUserSessionCount(userId: Int): Int = userSessions[userId]?.size ?: 0

    /**
     * Start a heartbeat for a WebSocket session
     * Note: Heartbeat is now handled via ping/pong messages from client
     * This method is kept for backward compatibility but may be deprecated
     */
    private fun launchHeartbeat(userId: Int, session: DefaultWebSocketSession) {
        // Deprecated: Heartbeat is now handled via ping/pong messages
        // Keeping for backward compatibility but not actively used
    }
} 