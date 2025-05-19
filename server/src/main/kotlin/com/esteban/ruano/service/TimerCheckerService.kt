package com.esteban.ruano.service

import com.esteban.ruano.lifecommander.models.timers.CompletedTimerInfo
import com.esteban.ruano.lifecommander.timer.TimerWebSocketServerMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.UUID


class TimerCheckerService(
    private val timerService: TimerService
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun start() {
        scope.launch {
            while (true) {
                try {
                    checkTimers()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(30_000L) // 30 seconds
            }
        }
    }

    private suspend fun checkTimers() {
        val now = Clock.System.now()
        val currentTime = now.toLocalDateTime(TimeZone.UTC)
        val timezone = TimeZone.UTC

        // Fetch completed timers and needed metadata
        val completedTimers: List<CompletedTimerInfo> = timerService.checkCompletedTimers(
            timezone,
            currentTime
        )

        if (completedTimers.isEmpty()) {
            println("[Timers] No timers completed at $now")
            return
        }

        println("[Timers] Processing ${completedTimers.size} completed timers")

        for (info in completedTimers) {
            try {
                CoroutineScope(Dispatchers.Default).launch {
                    timerService.sendPushNotificationToUser(
                        userId = info.userId,
                        title = "Timer Completed",
                        body = "Your timer '${info.name}' has completed.",
                        data = mapOf(
                            "type" to "TIMER_COMPLETED",
                            "timerId" to info.domainTimer.id,
                            "listId" to info.listId.toString()
                        )
                    )
                }

                val nextTimer = timerService.getNextTimerToStart(UUID.fromString(info.listId), info.domainTimer)
                val updatedTimer = nextTimer?.let {
                    timerService.startTimer(
                        userId = info.userId,
                        listId = UUID.fromString(info.listId),
                        timerId = it.id.value
                    ).firstOrNull()
                }

                TimerNotifier.broadcastUpdate(
                    TimerWebSocketServerMessage.TimerUpdate(
                        timer = updatedTimer ?: info.domainTimer,
                        listId = info.listId,
                        remainingTime = updatedTimer?.duration ?: info.domainTimer.duration,
                    ),
                    info.userId
                )

                println("[Timers] Timer update broadcasted for list ${info.listId}")

            } catch (e: Exception) {
                println("[Timers] Error processing timer ${info.domainTimer.id}: ${e.message}")
                e.printStackTrace()
            }
        }
    }

} 