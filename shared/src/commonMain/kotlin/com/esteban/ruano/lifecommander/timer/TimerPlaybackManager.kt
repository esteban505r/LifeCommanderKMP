package com.esteban.ruano.lifecommander.timer


import com.esteban.ruano.lifecommander.models.Timer
import com.esteban.ruano.lifecommander.models.TimerList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlin.time.Duration.Companion.seconds

class TimerPlaybackManager {
    private var currentTimerList: TimerList? = null
    private var currentTimerIndex: Int = 0
    private var isPaused: Boolean = false
    private var remainingTime: Int = 0

    fun startTimerList(timerList: TimerList) {
        currentTimerList = timerList
        currentTimerIndex = 0
        isPaused = false
        startCurrentTimer()
    }

    fun pauseTimer() {
        isPaused = true
    }

    fun resumeTimer() {
        isPaused = false
    }

    fun stopTimer() {
        currentTimerList = null
        currentTimerIndex = 0
        isPaused = false
        remainingTime = 0
    }

    private fun startCurrentTimer() {
        currentTimerList?.let { list ->
            val enabledTimers = list.timers.filter { it.enabled }
            if (enabledTimers.isNotEmpty()) {
                val timer = enabledTimers[currentTimerIndex]
                remainingTime = timer.duration
            }
        }
    }

    fun getTimerFlow(): Flow<TimerPlaybackState> = flow {
        while (currentTimerList != null) {
            if (!isPaused && remainingTime > 0) {
                emit(TimerPlaybackState.Running(remainingTime))
                remainingTime--
                kotlinx.coroutines.delay(1000) // 1 second delay
            } else if (!isPaused && remainingTime == 0) {
                emit(TimerPlaybackState.Completed)
                moveToNextTimer()
            } else {
                emit(TimerPlaybackState.Paused(remainingTime))
                kotlinx.coroutines.delay(100) // Small delay when paused
            }
        }
        emit(TimerPlaybackState.Stopped)
    }

    private fun moveToNextTimer() {
        currentTimerList?.let { list ->
            val enabledTimers = list.timers.filter { it.enabled }
            if (enabledTimers.isNotEmpty()) {
                currentTimerIndex = (currentTimerIndex + 1) % enabledTimers.size
                if (currentTimerIndex == 0 && !list.loopTimers) {
                    stopTimer()
                } else {
                    startCurrentTimer()
                }
            }
        }
    }

    fun getCurrentTimer(): Timer? {
        return currentTimerList?.let { list ->
            list.timers.filter { it.enabled }.getOrNull(currentTimerIndex)
        }
    }

    fun getCurrentTimerList(): TimerList? = currentTimerList
}

sealed class TimerPlaybackState {
    data class Running(val remainingSeconds: Int) : TimerPlaybackState()
    data class Paused(val remainingSeconds: Int) : TimerPlaybackState()
    object Completed : TimerPlaybackState()
    object Stopped : TimerPlaybackState()
} 