package com.esteban.ruano.lifecommander.timer


import com.esteban.ruano.lifecommander.models.Timer
import com.esteban.ruano.lifecommander.models.TimerList


import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class TimerPlaybackManager(
) {
    private val _uiState = MutableStateFlow(TimerPlaybackState())
    val uiState: StateFlow<TimerPlaybackState> = _uiState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private var playbackJob: Job? = null

    fun startTimerList(timerList: TimerList) {
        stopTimer() // Cancel any ongoing playback

        val enabledTimers = timerList.timers?.filter { it.enabled }
        if (enabledTimers?.isEmpty() == true) {
            _uiState.value = TimerPlaybackState(status = TimerPlaybackStatus.Stopped)
            return
        }

        println("Starting timer list: ${timerList.name}")
        println("Enabled timers: ${enabledTimers?.size}")
        println("First timer: ${enabledTimers?.first()?.name}")
        println("First timer duration: ${enabledTimers?.first()?.duration}")
        _uiState.value = TimerPlaybackState(
            timerList = timerList,
            currentTimerIndex = 0,
            currentTimer = enabledTimers?.first(),
            remainingTime = enabledTimers?.first()?.duration ?:0,
            status = TimerPlaybackStatus.Running
        )

        startTicking()
    }

    fun pauseTimer() {
        if (_uiState.value.status == TimerPlaybackStatus.Running) {
            _uiState.update { it.copy(status = TimerPlaybackStatus.Paused) }
        }
    }

    fun resumeTimer() {
        if (_uiState.value.status == TimerPlaybackStatus.Paused) {
            _uiState.update { it.copy(status = TimerPlaybackStatus.Running) }
            startTicking()
        }
    }

    fun stopTimer() {
        playbackJob?.cancel()
        playbackJob = null
        _uiState.value = TimerPlaybackState()
    }

    private fun startTicking() {
        println("Starting timer ticking")
        playbackJob?.cancel()
        playbackJob = scope.launch {
            val isActive = coroutineContext.isActive
            println("Coroutine is active: $isActive")
            while (isActive) {
                val state = _uiState.value
                println("Current state: $state")
                if (state.status != TimerPlaybackStatus.Running) break

                if (state.remainingTime <= 0) {
                    println("Timer completed: ${state.currentTimer?.name}")
                    moveToNextTimer()
                    break
                }

                println("Ticking: ${state.remainingTime} seconds remaining")
                delay(1000)
                _uiState.update {
                    it.copy(
                        remainingTime = it.remainingTime - 1
                    )
                }
            }
        }
    }

    private fun moveToNextTimer() {
        val state = _uiState.value
        val list = state.timerList ?: return
        val enabledTimers = list.timers?.filter { it.enabled }
        val nextIndex = state.currentTimerIndex + 1

        val shouldStop = nextIndex >= (enabledTimers?.size?:0) && !list.loopTimers

        if (shouldStop || enabledTimers?.isEmpty() == true) {
            stopTimer()
        } else {
            val newIndex = nextIndex % (enabledTimers?.size?:0)
            val nextTimer = enabledTimers?.get(newIndex)

            _uiState.value = state.copy(
                currentTimerIndex = newIndex,
                currentTimer = nextTimer,
                remainingTime = nextTimer?.duration?:0,
                status = TimerPlaybackStatus.Running
            )

            startTicking()
        }
    }
}

data class TimerPlaybackState(
    val timerList: TimerList? = null,
    val currentTimerIndex: Int = 0,
    val currentTimer: Timer? = null,
    val remainingTime: Int = 0,
    val status: TimerPlaybackStatus = TimerPlaybackStatus.Stopped
)

enum class TimerPlaybackStatus {
    Running,
    Paused,
    Completed,
    Stopped
}
