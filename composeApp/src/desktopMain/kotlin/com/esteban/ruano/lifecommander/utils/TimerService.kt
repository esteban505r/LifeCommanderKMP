package utils

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import models.TimerModel
import utils.DateUtils.parseTime
import java.io.File
import kotlin.coroutines.coroutineContext
import services.AppPreferencesService

class TimerService(private val statusBarService: StatusBarService) {
    private val _timerFlow = MutableStateFlow<TimerModel?>(null)
    val timerFlow: StateFlow<TimerModel?> get() = _timerFlow.asStateFlow()
    private val _paused = MutableStateFlow(false)
    val paused: StateFlow<Boolean> get() = _paused.asStateFlow()
    val timerEndingListenerChannel = Channel<TimerModel>(Channel.UNLIMITED)

    fun pauseTimer() {
        _paused.value = true
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun resumeTimer(
        onFinish: (TimerModel) -> Unit,
        onResume: () -> Unit
    ) {
        println("Resuming timer: Child's parent: ${coroutineContext[Job]?.parent}")
        _paused.value = false
        _timerFlow.value?.let { timerModel ->
           try {
               getFlow(
                   timerName = timerModel.name,
                   id = timerModel.id,
                   delayTimeMilliseconds = timerModel.step,
                   startValue = timerModel.timeRemaining,
                   endValue = timerModel.endValue
               )
                   .collect { value ->
                       when (value) {
                           0L -> {
                               onFinish(timerModel)
                               _timerFlow.value = null
                               handleTimerCompletion(timerModel)
                           }
                           timerModel.startValue -> {
                               onResume()
                           }
                           else -> {
                               _timerFlow.value = _timerFlow.value?.copy(timeRemaining = value)
                           }
                       }
                   }
           }
            catch (e: Exception) {
                println("Error: $e")
                withContext(Dispatchers.IO) {
                    val file = File(System.getProperty("user.home"), ".config/LifeCommanderDesktop/timer")
                    if(paused.value) {
                        file.writeText("echo \"Timer paused\"")
                    } else {
                        file.writeText("echo \"Timer stopped\"")
                    }
                }
                throw e
            }
        }
    }

    suspend fun startTimer(
        timerModel: TimerModel,
        onStart: () -> Unit,
        onFinish: () -> Unit
    ) {
        println("Starting timer: Child's parent: ${coroutineContext[Job]?.parent}")
        _timerFlow.value = timerModel
        try{
            getFlow(
                timerName = timerModel.name,
                id = timerModel.id,
                delayTimeMilliseconds = timerModel.step,
                startValue = timerModel.startValue,
                endValue = timerModel.endValue
            )
                .collect { value ->
                    when (value) {
                        0L -> {
                            onFinish()
                            _timerFlow.value = null
                            handleTimerCompletion(timerModel)
                        }

                        timerModel.startValue -> onStart()
                        else -> {
                            _timerFlow.value = _timerFlow.value?.copy(timeRemaining = value)
                        }
                    }
                }
        }
        catch (e: Exception) {
            println("Error: $e")
            withContext(Dispatchers.IO) {
                val file = File(System.getProperty("user.home"), ".config/LifeCommanderDesktop/timer")
                file.writeText("echo \"Timer stopped\"")
            }
            throw e
        }
    }

    fun stopTimer() {
        _timerFlow.value = null
    }


    private fun getFlow(
        id: String,
        timerName: String = "Timer",
        delayTimeMilliseconds: Long,
        startValue: Long,
        stepValue: Long = 1,
        endValue: Long = 0
    ): Flow<Long> = (startValue downTo endValue step stepValue).asFlow()
        .onEach { delay(delayTimeMilliseconds) }
        .onEach {
            println(
                coroutineContext[CoroutineName]?.name ?: "No name"
            )
        }
        .onEach {
            withContext(Dispatchers.IO) {
                statusBarService.updateTimerStatus(timerName, it)
            }
        }
        .conflate()
        .transform { remainingValue: Long ->
            print("timer: $id}")
            println("remainingValue: $remainingValue")
            emit(if (remainingValue < 0) 0 else remainingValue)
        }.cancellable()

    private suspend fun handleTimerCompletion(timerModel: TimerModel? = null) {
       timerModel?.let { timerEndingListenerChannel.send(it) }
    }


}
