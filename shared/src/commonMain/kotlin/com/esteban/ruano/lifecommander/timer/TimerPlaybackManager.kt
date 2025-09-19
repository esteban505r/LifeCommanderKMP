package com.esteban.ruano.lifecommander.timer


import com.esteban.ruano.lifecommander.models.Timer
import com.esteban.ruano.lifecommander.models.TimerList
import com.esteban.ruano.lifecommander.models.timers.TimerState


import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class TimerPlaybackManager(
) {
    private val _uiState = MutableStateFlow(TimerPlaybackState())
    val uiState: StateFlow<TimerPlaybackState> = _uiState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private var playbackJob: Job? = null

    fun startTimerList(timerList: TimerList, onEachTimerFinished: (Timer) -> Unit) {
        if(timerList.timers.isNullOrEmpty()) {
            println("Timer list is empty")
            return
        }

        stopTimer()

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
            remainingMillis = enabledTimers?.first()?.duration ?: 0,
            status = TimerPlaybackStatus.Running
        )

        startTicking(onEachTimerFinished)
    }

    fun pauseTimer() {
        if (_uiState.value.status == TimerPlaybackStatus.Running) {
            _uiState.update { it.copy(status = TimerPlaybackStatus.Paused) }
        }
    }

    fun resumeTimer(
        onEachTimerFinished: (Timer) -> Unit = {}
    ) {
        if (_uiState.value.status == TimerPlaybackStatus.Paused) {
            _uiState.update { it.copy(status = TimerPlaybackStatus.Running) }
            startTicking(
                onEachTimerFinished = onEachTimerFinished
            )
        }
    }

    fun stopTimer() {
        playbackJob?.cancel()
        playbackJob = null
        _uiState.value = TimerPlaybackState()
    }

    @OptIn(ExperimentalTime::class)
    private fun startTicking(onEachTimerFinished: (Timer) -> Unit = {}) {
        playbackJob?.cancel()
        playbackJob = scope.launch {
            val startInstant = Clock.System.now().toEpochMilliseconds()
            var lastTimestamp = startInstant

            while (isActive) {
                delay(16)

                val state = _uiState.value
                if (state.status != TimerPlaybackStatus.Running) break

                val now = Clock.System.now().toEpochMilliseconds()
                val elapsed = now - lastTimestamp
                lastTimestamp = now

                val newRemaining = _uiState.value.remainingMillis - elapsed
                println("Remaining time: $newRemaining")

                if (newRemaining <= 0) {
                    println("Timer completed: ${state.currentTimer?.name}")
                    onEachTimerFinished(state.currentTimer ?: return@launch)
                    moveToNextTimer(onEachTimerFinished)
                    break
                }

                _uiState.update { it.copy(remainingMillis = newRemaining) }
            }
        }
    }

    private fun moveToNextTimer(
        onEachTimerFinished: (Timer) -> Unit = {}
    ) {
        val state = _uiState.value
        val list = state.timerList ?: return
        val enabledTimers = list.timers?.filter { it.enabled }
        val nextIndex = state.currentTimerIndex + 1

        val shouldStop = nextIndex >= (enabledTimers?.size ?: 0) && !list.loopTimers

        if (shouldStop || enabledTimers?.isEmpty() == true) {
            stopTimer()
        } else {
            val newIndex = nextIndex % (enabledTimers?.size ?: 0)
            val nextTimer = enabledTimers?.get(newIndex)

            _uiState.value = state.copy(
                currentTimerIndex = newIndex,
                currentTimer = nextTimer,
                remainingMillis = nextTimer?.duration ?: 0,
                status = TimerPlaybackStatus.Running
            )

            startTicking(
                onEachTimerFinished
            )
        }
    }

    fun overridePlaybackState(
        timerList: TimerList,
        timer: Timer,
        timerIndex: Int,
        timeRemaining: Long,
        onEachTimerFinished: (Timer) -> Unit = {}
    ) {
        playbackJob?.cancel()

        val status = when (timer.state) {
            TimerState.RUNNING.toString() -> TimerPlaybackStatus.Running
            TimerState.PAUSED.toString() -> TimerPlaybackStatus.Paused
            TimerState.COMPLETED.toString() -> TimerPlaybackStatus.Stopped
            else -> TimerPlaybackStatus.Stopped
        }

        _uiState.value = TimerPlaybackState(
            timerList = timerList,
            currentTimerIndex = timerIndex,
            currentTimer = timer,
            remainingMillis = timeRemaining,
            status = status
        )

        if (status == TimerPlaybackStatus.Running) {
            startTicking(
                onEachTimerFinished = onEachTimerFinished
            )
        }
    }
}

data class TimerPlaybackState(
    val timerList: TimerList? = null,
    val currentTimerIndex: Int = 0,
    val currentTimer: Timer? = null,
    val remainingMillis: Long = 0L,
    val status: TimerPlaybackStatus = TimerPlaybackStatus.Stopped
)

enum class TimerPlaybackStatus {
    Running,
    Paused,
    Completed,
    Stopped
}
