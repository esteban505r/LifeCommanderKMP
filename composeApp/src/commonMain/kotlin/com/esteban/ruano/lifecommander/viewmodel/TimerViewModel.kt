package com.esteban.ruano.lifecommander.viewmodel

import com.esteban.ruano.lifecommander.models.Timer
import com.esteban.ruano.lifecommander.models.TimerList
import com.esteban.ruano.lifecommander.service.TimerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class TimerViewModel : KoinComponent {
    private val timerService: TimerService by inject()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _timerLists = MutableStateFlow<List<TimerList>>(emptyList())
    val timerLists: StateFlow<List<TimerList>> = _timerLists.asStateFlow()

    val connectionState = timerService.connectionState
    val timerNotifications = timerService.timerNotifications
    val activeTimer = timerService.activeTimer

    init {
        connect()
    }

    private fun connect() {
        timerService.connect()
    }

    fun disconnect() {
        timerService.disconnect()
    }

    fun startTimer(timer: Timer, listId: String) {
        scope.launch {
            timerService.startTimer(timer, listId)
            // Start local timer countdown
            startLocalTimerCountdown(timer.duration)
        }
    }

    fun pauseTimer() {
        scope.launch {
            timerService.pauseTimer()
        }
    }

    fun resumeTimer() {
        scope.launch {
            timerService.resumeTimer()
            // Resume local timer countdown
            activeTimer.value?.let { timer ->
                startLocalTimerCountdown(timer.remainingTime)
            }
        }
    }

    fun stopTimer() {
        scope.launch {
            timerService.stopTimer()
        }
    }

    private fun startLocalTimerCountdown(initialTime: Int) {
        scope.launch {
            var remainingTime = initialTime
            while (remainingTime > 0 && activeTimer.value?.status == TimerService.TimerStatus.RUNNING) {
                withContext(Dispatchers.IO) {
                    kotlinx.coroutines.delay(1000) // 1 second delay
                    remainingTime--
                    timerService.updateRemainingTime(remainingTime)
                }
            }
            if (remainingTime == 0) {
                // Timer completed
                activeTimer.value?.let { timer ->
                    // TODO: Handle timer completion
                }
            }
        }
    }

    fun addTimerList(name: String, loopTimers: Boolean, pomodoroGrouped: Boolean) {
        // TODO: Implement timer list creation
    }

    fun updateTimerList(id: String, name: String, loopTimers: Boolean, pomodoroGrouped: Boolean) {
        // TODO: Implement timer list update
    }

    fun deleteTimerList(id: String) {
        // TODO: Implement timer list deletion
    }

    fun addTimer(listId: String, name: String, duration: Int, enabled: Boolean, countsAsPomodoro: Boolean, order: Int) {
        // TODO: Implement timer creation
    }

    fun updateTimer(id: String, name: String, duration: Int, enabled: Boolean, countsAsPomodoro: Boolean, order: Int) {
        // TODO: Implement timer update
    }

    fun deleteTimer(id: String) {
        // TODO: Implement timer deletion
    }

    fun reorderTimers(listId: String, timers: List<Timer>) {
        // TODO: Implement timer reordering
    }
} 