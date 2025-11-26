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
            println("[TimerPlaybackManager] Starting tick loop")
            // Note: This is a local UI update loop
            // The actual timer state is server-authoritative
            // We update the UI every second based on the last known server state
            while (isActive) {
                delay(1000) // Update every second (not every 16ms for performance)

                val state = _uiState.value
                println("[TimerPlaybackManager] Tick - status: ${state.status}, remaining: ${state.remainingMillis}ms")
                
                if (state.status != TimerPlaybackStatus.Running) {
                    println("[TimerPlaybackManager] Timer not running, stopping tick loop. Status: ${state.status}")
                    break
                }

                // Decrement remaining time by 1 second
                val newRemaining = state.remainingMillis - 1000

                if (newRemaining <= 0) {
                    println("[TimerPlaybackManager] Timer completed locally: ${state.currentTimer?.name}")
                    // Note: Server will also detect completion and send update
                    // This is just for immediate UI feedback
                    onEachTimerFinished(state.currentTimer ?: return@launch)
                    moveToNextTimer(onEachTimerFinished)
                    break
                }

                // Update state atomically
                _uiState.update { currentState ->
                    if (currentState.status == TimerPlaybackStatus.Running && currentState.currentTimer?.id == state.currentTimer?.id) {
                        currentState.copy(remainingMillis = newRemaining)
                    } else {
                        // State changed externally, don't update
                        println("[TimerPlaybackManager] State changed externally during update, stopping tick loop")
                        currentState
                    }
                }
                println("[TimerPlaybackManager] Updated remaining to: ${newRemaining}ms")
            }
            println("[TimerPlaybackManager] Tick loop ended")
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
        println("[TimerPlaybackManager] overridePlaybackState called: timer=${timer.name}, state=${timer.state}, timeRemaining=${timeRemaining}ms")
        playbackJob?.cancel()

        val status = when (timer.state) {
            TimerState.RUNNING.toString() -> TimerPlaybackStatus.Running
            TimerState.PAUSED.toString() -> TimerPlaybackStatus.Paused
            TimerState.COMPLETED.toString() -> TimerPlaybackStatus.Stopped
            else -> {
                println("[TimerPlaybackManager] Unknown timer state: ${timer.state}, defaulting to Stopped")
                TimerPlaybackStatus.Stopped
            }
        }

        println("[TimerPlaybackManager] Setting playback state: status=$status, remaining=${timeRemaining}ms")
        _uiState.value = TimerPlaybackState(
            timerList = timerList,
            currentTimerIndex = timerIndex,
            currentTimer = timer,
            remainingMillis = timeRemaining,
            status = status
        )

        if (status == TimerPlaybackStatus.Running) {
            println("[TimerPlaybackManager] Status is Running, starting tick loop")
            startTicking(
                onEachTimerFinished = onEachTimerFinished
            )
        } else {
            println("[TimerPlaybackManager] Status is $status, NOT starting tick loop")
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
