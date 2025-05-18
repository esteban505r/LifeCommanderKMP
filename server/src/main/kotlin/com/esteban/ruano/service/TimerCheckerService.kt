package com.esteban.ruano.service

import com.esteban.ruano.database.converters.toDomainModel
import com.esteban.ruano.database.entities.Timer
import com.esteban.ruano.database.entities.Timers
import com.esteban.ruano.lifecommander.models.timers.TimerState
import com.esteban.ruano.lifecommander.timer.TimerWebSocketServerMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.*
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.time.Duration.Companion.seconds


class TimerCheckerService(
    private val timerService: TimerService
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun start() {
        scope.launch {
            while (true) {
                try {
                    checkCompletedTimers()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(30_000L) // 30 seconds
            }
        }
    }

    private suspend fun checkCompletedTimers() {
        val now = Clock.System.now()
        val currentTime = now.toLocalDateTime(TimeZone.UTC)

        // Fetch completed timers inside transaction, but keep it short
        val completedTimers = transaction {
            Timer.find {
                (Timers.state eq TimerState.RUNNING) and
                        (Timers.startTime.isNotNull()) // Exposed needs this in SQL
            }
                .limit(1000)
                .filter { timer ->
                    val timezone = TimeZone.UTC
                    val endTime = timer.startTime!!
                        .toInstant(timezone)
                        .plus(timer.duration.seconds)
                        .toLocalDateTime(timezone)
                    endTime <= currentTime
                }
                .toList()
        }

        if (completedTimers.isEmpty()) {
            println("No completed timers found.")
            return
        }

        println("Found ${completedTimers.size} completed timers.")

        // Process each timer outside transaction
        completedTimers.forEach { timer ->
            try {
                val startInstant = timer.startTime!!.toInstant(TimeZone.UTC)
                val remainingSeconds = maxOf(0, timer.duration - (now - startInstant).inWholeSeconds)

                // Update state inside transaction
                transaction {
                    timer.state = TimerState.COMPLETED
                }

                // Build domain model
                val domainTimer = timer.toDomainModel().copy(
                    remainingSeconds = remainingSeconds,
                )

                TimerNotifier.broadcastUpdate(
                    TimerWebSocketServerMessage.TimerListCompleted(
                        listId = timer.list.id.toString(),
                    ), timer.list.userId.id.value
                )

                CoroutineScope(Dispatchers.Default).launch {
                    timerService.sendPushNotificationToUser(
                        userId = timer.list.userId.id.value,
                        title = "Timer Completed",
                        body = "Your timer '${timer.name}' has completed.",
                        data = mapOf(
                            "type" to "TIMER_COMPLETED",
                            "timerId" to timer.id.value.toString()
                        )
                    )
                }

                val nexTimer = timerService.getNextTimerToStart(
                    timer.list.id.value,
                    timer
                )

                timerService.startTimer(
                    listId = timer.list.id.value,
                    timerId = nexTimer?.id?.value,
                )

                if (nexTimer != null) {
                    TimerNotifier.broadcastUpdate(
                        TimerWebSocketServerMessage.TimerListStarted(
                            listId = timer.list.id.toString(),
                            timerStartedId = nexTimer.id.value.toString(),
                        ), timer.list.userId.id.value
                    )
                }


                println("Processed completed timer: ${timer.id}")
            } catch (e: Exception) {
                println("Error processing completed timer: ${timer.id}, error: ${e.stackTraceToString()}")
            }
        }
    }
} 