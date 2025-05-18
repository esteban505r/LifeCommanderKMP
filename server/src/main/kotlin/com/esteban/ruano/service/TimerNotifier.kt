package com.esteban.ruano.service

import com.esteban.ruano.lifecommander.models.Timer
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
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.seconds


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

        val message = Json.encodeToString(timerWebSocketServerMessage)
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
     */
    private fun launchHeartbeat(userId: Int, session: DefaultWebSocketSession) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                while (true) {
                    delay(30.seconds)
                    try {
                        session.send(Frame.Text("heartbeat"))
                    } catch (e: ClosedSendChannelException) {
                        println("Heartbeat failed for user $userId: ${e.message}")
                        unregisterSession(userId, session)
                        break
                    } catch (e: Exception) {
                        println("Error during heartbeat for user $userId: ${e.message}")
                        unregisterSession(userId, session)
                        break
                    }
                }
            } catch (e: Exception) {
                println("Error in heartbeat coroutine for user $userId: ${e.message}")
                unregisterSession(userId, session)
            }
        }
    }
} 