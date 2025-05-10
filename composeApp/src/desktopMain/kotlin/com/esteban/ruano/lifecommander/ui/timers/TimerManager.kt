package ui.timers

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import models.TimerModel
import utils.TimerService
import utils.Timers

class TimerManager(
    private val coroutineScope: CoroutineScope,
    private val timerJobParent: Job,
    private val timerService: TimerService,
    private val onTimerFinish: (TimerModel, Int) -> Unit,
    private val onTimerStart: (TimerModel) -> Unit,
    private val onTimerPause: (TimerModel) -> Unit,
    private val onTimerResume: (TimerModel) -> Unit,
    private val onTimerStop: (TimerModel) -> Unit,
    private val isLoopEnabled: Boolean,
) {
    private var isRunning = false

    fun playTimer(
        startAt: Int = 0,
    ) {
        if (timerService.paused.value && startAt == 0) {
            resumeTimer()
        } else {
            startTimer(
                startAt = startAt
            )
        }
    }

    fun pauseTimer() {
        timerService.timerFlow.value?.let { onTimerPause(it) }
        timerService.pauseTimer()
        timerJobParent.cancelChildren()
    }

    fun stopTimer() {
        timerService.timerFlow.value?.let { onTimerStop(it) }
        timerService.stopTimer()
        timerJobParent.cancelChildren()
    }

    private fun resumeTimer() {
        coroutineScope.launch(timerJobParent) {
            timerService.timerFlow.value?.let { onTimerResume(it) }
            timerService.resumeTimer(
                onFinish = { timer ->
                    onTimerFinish(timer, Timers.timers.value.indexOf(timer))
                },
                onResume = {}
            )
        }
    }

    private fun startTimer(startAt:Int = 0) {
        isRunning = true
        coroutineScope.launch(timerJobParent) {
            try {
                do {
                    for (i in startAt until Timers.timers.value.size) {
                        val timer = Timers.timers.value[i]
                        timerService.startTimer(
                            onFinish = { onTimerFinish(timer,i) },
                            onStart = { onTimerStart(timer) },
                            timerModel = timer
                        )
                    }
                } while (isLoopEnabled && Timers.timers.value.isNotEmpty())
            } catch (e: Exception) {
                println("Timer error: ${e.message}")
                throw e
            } finally {
                isRunning = false
            }
        }
    }
} 