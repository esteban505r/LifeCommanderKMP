package ui.viewmodels

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.kdroid.composenotification.builder.Notification
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import models.TimerModel
import services.NightBlockService
import services.auth.TokenStorage
import services.habits.HabitService
import services.tasks.TaskService
import ui.habits.HabitReminderManager
import ui.state.AppState
import ui.tasks.TaskReminderManager
import ui.timers.TimerManager
import utils.TimerService
import utils.timersKey
import utils.timersLoopEnabledKey

class AppViewModel(
    private val nightBlockService: NightBlockService,
    private val dataStore: DataStore<Preferences>,
    private val timerService: TimerService,
    private val habitsService: HabitService,
    private val taskService: TaskService,
    private val tokenStorage: TokenStorage
) : ViewModel() {
    
    private val _timers = MutableStateFlow(listOf<TimerModel>())
    val timers: StateFlow<List<TimerModel>> = _timers.asStateFlow()

    val _timerIndex = MutableStateFlow(0)
    val timerIndex: StateFlow<Int> = _timerIndex.asStateFlow()

    val timer = timerService.timerFlow
    val paused = timerService.paused
    val timerEndingListenerChannel = timerService.timerEndingListenerChannel

    private val _isTimersLoopEnabled = MutableStateFlow(false)
    val isTimersLoopEnabled: StateFlow<Boolean> = _isTimersLoopEnabled.asStateFlow()

    private val _timerEvents = Channel<TimerEvent>()
    val timerEvents = _timerEvents.receiveAsFlow()

    private val _appState = MutableStateFlow(AppState())
    val appState: StateFlow<AppState> = _appState.asStateFlow()

    private val habitReminderManager = HabitReminderManager(
        habitsService = habitsService,
        coroutineScope = viewModelScope,
        onReminder = { title, message ->
            showNotification(title, message)
        },
        tokenStorage = tokenStorage
    )

    private val taskReminderManager = TaskReminderManager(
        taskService = taskService,
        coroutineScope = viewModelScope,
        onReminder = { title, message ->
            showNotification(title, message)
        },
        tokenStorage = tokenStorage
    )

    private val timerManager = TimerManager(
        coroutineScope = viewModelScope,
        timerJobParent = viewModelScope.coroutineContext[Job] ?: Job(),
        timerService = timerService,
        onTimerFinish = { timer, index ->
            viewModelScope.launch {
                _timerIndex.value = index
                _timerEvents.send(TimerEvent.TimerFinished(timer))
                showNotification(timer.name, "Time up!")
            }
        },
        onTimerStart = { timer ->
            viewModelScope.launch {
                _timerEvents.send(TimerEvent.TimerStarted(timer))
                showNotification(timer.name, "Timer started!")
            }
        },
        isLoopEnabled = _isTimersLoopEnabled.value,
        onTimerResume = {},
        onTimerPause = {},
        onTimerStop = { _ ->
            _timerIndex.value = 0
        },
    )

    init {
        viewModelScope.launch {
            dataStore.data.collect { preferences ->
                _isTimersLoopEnabled.value = preferences[timersLoopEnabledKey] ?: false
            }
        }
        startReminders()
    }

    private fun startReminders() {
        viewModelScope.launch {
            habitReminderManager.start()
            taskReminderManager.start()
        }
    }

    private fun showNotification(title: String, message: String) {
        Notification(
            title = title,
            message = message,
            onActivated = { /* Handle notification click */ },
            onDismissed = { /* Handle notification dismissal */ },
            onFailed = { /* Handle notification failure */ }
        )
    }

    fun minimize() {
        _appState.value = _appState.value.copy(isMinimized = true)
    }

    fun restore() {
        _appState.value = _appState.value.copy(isMinimized = false)
    }


    override fun onCleared() {
        super.onCleared()
        habitReminderManager.stop()
        taskReminderManager.stop()
    }

    fun addTimer(timer: TimerModel) {
        viewModelScope.launch {
            val newList = _timers.value.toMutableList()
            newList.add(timer)
            _timers.value = newList
            saveTimers()
        }
    }

    fun addAllTimers(timers: List<TimerModel>) {
        viewModelScope.launch {
            val newList = _timers.value.toMutableList()
            newList.addAll(timers)
            _timers.value = newList
            saveTimers()
        }
    }

    fun removeTimer(timer: TimerModel) {
        viewModelScope.launch {
            val newList = _timers.value.toMutableList()
            newList.removeIf { it.id == timer.id }
            _timers.value = newList
            saveTimers()
        }
    }

    fun replaceTimers(timers: List<TimerModel>) {
        viewModelScope.launch {
            _timers.value = timers
            saveTimers()
        }
    }

    private suspend fun saveTimers() {
        dataStore.edit {
            it[timersKey] = Gson().toJson(_timers.value)
        }
    }

    fun loadTimers() {
        viewModelScope.launch {
            dataStore.data.collect { preferences ->
                val timersJson = preferences[timersKey]
                if (timersJson != null) {
                    val type = object : com.google.gson.reflect.TypeToken<List<TimerModel>>() {}.type
                    val loadedTimers = Gson().fromJson<List<TimerModel>>(timersJson, type)
                    _timers.value = loadedTimers
                }
            }
        }
    }

    fun playTimer() {
        timerManager.playTimer()
        _appState.value = appState.value.copy(timersPaused = false)
    }

    fun pauseTimer() {
        timerManager.pauseTimer()
        _appState.value = appState.value.copy(timersPaused = true)
    }

    fun stopTimer() {
        timerManager.stopTimer()
        _appState.value = appState.value.copy(timerStopped = true)
    }

    fun previousTimer() {
        timerManager.stopTimer()
        viewModelScope.launch {
            val currentIndex = _timerIndex.value
            if (currentIndex > 0) {
                _timerIndex.value = currentIndex - 1
                timerManager.playTimer(_timerIndex.value)
            } else {
                _timerIndex.value = 0
                timerManager.playTimer()
            }
        }
    }

    fun nextTimer() {
        timerManager.stopTimer()
        viewModelScope.launch {
            val currentIndex = _timerIndex.value
            if (currentIndex < _timers.value.size - 1) {
                _timerIndex.value = currentIndex + 1
                timerManager.playTimer(_timerIndex.value)
            } else {
                _timerIndex.value = 0
                timerManager.playTimer()
            }
        }
    }

    fun setTimersLoopEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit {
                it[timersLoopEnabledKey] = enabled
            }
            _isTimersLoopEnabled.value = enabled
        }
    }

    fun showTimersDialog() {
        _appState.value = _appState.value.copy(showTimersDialog = true)
    }

    fun hideTimersDialog() {
        _appState.value = _appState.value.copy(showTimersDialog = false)
    }

    fun hideDialog() {
        _appState.value = _appState.value.copy(isDialogOpen = false)
    }

    fun triggerTimerEndActions() {
        viewModelScope.launch {
            dataStore.data.first{
                val enabled = it[timersLoopEnabledKey] ?: false
                if(enabled){
                    timerManager.playTimer()
                }
                enabled
            }
        }
    }

    fun checkNightBlock() {
        viewModelScope.launch {
            nightBlockService.checkAndActivateNightBlock()
        }
    }
} 