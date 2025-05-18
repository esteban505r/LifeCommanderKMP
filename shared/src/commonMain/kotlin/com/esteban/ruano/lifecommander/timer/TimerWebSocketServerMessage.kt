package com.esteban.ruano.lifecommander.timer

import kotlinx.serialization.Serializable

@Serializable
sealed class TimerWebSocketServerMessage {
    @Serializable
    data class TimerListStarted(
        val timerStartedId : String?,
        val listId: String,
    ) : TimerWebSocketServerMessage()

    @Serializable
    data class TimerListStopped(
        val timerIdStopped: String,
        val secondsRemaining: Int,
        val listId: String,
    ) : TimerWebSocketServerMessage()

    @Serializable
    data class TimerListCompleted(
        val listId: String,
    ) : TimerWebSocketServerMessage()

    @Serializable
    data class TimerListPaused(
        val timerIdPaused: String,
        val secondsRemaining: Int,
        val listId: String,
    ) : TimerWebSocketServerMessage()

    @Serializable
    data class TimerListResumed(
        val timerIdResumed: String,
        val secondsRemaining: Int,
        val listId: String,
    ) : TimerWebSocketServerMessage()

    @Serializable
    data class TimerListRunningUpdate(
        val timerIdRunning: String,
        val secondsRemaining: Int,
        val listId: String,
    )


}