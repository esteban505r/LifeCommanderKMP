package com.esteban.ruano.lifecommander.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esteban.ruano.lifecommander.models.Timer
import com.esteban.ruano.lifecommander.models.TimerList
import com.esteban.ruano.lifecommander.models.UserSettings
import com.esteban.ruano.lifecommander.services.timers.TimerService
import com.esteban.ruano.lifecommander.timer.*
import com.esteban.ruano.lifecommander.websocket.TimerWebSocketClient
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import com.esteban.ruano.utils.DateUIUtils.formatWithSeconds
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import services.auth.TokenStorageImpl
import services.dailyjournal.PomodoroService
import services.dailyjournal.models.PomodoroResponse
import ui.services.dailyjournal.models.CreatePomodoroRequest
import utils.DateUtils.parseDateTime
import utils.StatusBarService
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.seconds

class TimersViewModel(
    private val tokenStorageImpl: TokenStorageImpl,
    private val timerService: TimerService,
    private val timerPlaybackManager: TimerPlaybackManager,
    private val webSocketClient: TimerWebSocketClient,
    private val pomodoroService: PomodoroService,
    private val statusBarService: StatusBarService
) : ViewModel() {
    private val _timerLists = MutableStateFlow<List<TimerList>>(emptyList())
    val timerLists: StateFlow<List<TimerList>> = _timerLists.asStateFlow()

    private val _timerDetailList = MutableStateFlow<TimerList?>(null)
    val timerDetailList: StateFlow<TimerList?> = _timerDetailList.asStateFlow()

    private val _userSettings = MutableStateFlow<UserSettings?>(null)
    val userSettings: StateFlow<UserSettings?> = _userSettings.asStateFlow()

    val timerPlaybackState: StateFlow<TimerPlaybackState> = timerPlaybackManager.uiState

    private val _notifications = MutableStateFlow<List<TimerNotification>>(emptyList())
    val notifications: StateFlow<List<TimerNotification>> = _notifications.asStateFlow()

    private val _listNotifications = MutableStateFlow<List<TimerNotification>>(emptyList())
    val listNotifications: StateFlow<List<TimerNotification>> = _listNotifications.asStateFlow()

    private val _pomodoros = MutableStateFlow<List<PomodoroResponse>>(emptyList())
    val pomodoros: StateFlow<List<PomodoroResponse>> = _pomodoros.asStateFlow()

    private val _connectionState =
        MutableStateFlow<TimerWebSocketClient.ConnectionState>(TimerWebSocketClient.ConnectionState.Disconnected)
    val connectionState: StateFlow<TimerWebSocketClient.ConnectionState> = _connectionState.asStateFlow()

    var isLoading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    fun connectWebSocket() {
        webSocketClient.connect()
        viewModelScope.launch {
            webSocketClient.connectionState.collectLatest { state ->
                _connectionState.value = state
            }
        }
        viewModelScope.launch {
            webSocketClient.timerNotifications.collectLatest { notifications ->
                _notifications.value = notifications
            }
        }
        viewModelScope.launch {
            webSocketClient.incomingEvents.collectLatest { event ->
                try {
                    if(event == "heartbeat"){
                        println("Heartbeat received")
                        return@collectLatest
                    }
                    println("Converting msg to TimerWebSocketServerMessage")
                    val msg = Json.decodeFromString<TimerWebSocketServerMessage>(event)
                    println("Converted msg to TimerWebSocketServerMessage: $msg")

                    when (msg) {
                        is TimerWebSocketServerMessage.TimerUpdate -> {
                            println("Timer update received: ${msg.timer.name} - ${msg.remainingTime}s")
                            refreshTimerListFromServer(msg.timer, msg.remainingTime, msg.listId)
                        }
                    }
                } catch (e: Exception) {
                    println("Error parsing WebSocket event: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }

    fun refreshTimerListFromServer(timer: Timer, secondsRemaining: Long, listId: String) {
        println("[Client] Received update for listId=$listId, timer=${timer.name}")

        viewModelScope.launch {
            val updatedList = getTimerListByID(listId)

            if (updatedList == null) {
                println("[Client] Timer list not found for ID: $listId")
                error = "Unable to refresh timer list"
                return@launch
            }

            val timerIndex = updatedList.timers?.indexOfFirst { it.id == timer.id } ?: -1

            if (timerIndex == -1) {
                println("[Client] Timer ${timer.id} not found in list. Maybe out of sync?")
                error = "Timer not found in list"
                return@launch
            }

            println("[Client] Starting playback at timer index $timerIndex with $secondsRemaining seconds")

            timerPlaybackManager.overridePlaybackState(
                timerList = updatedList,
                timer = timer,
                timerIndex = timerIndex,
                timeRemaining = secondsRemaining,
                onEachTimerFinished = {
                    println("[Client] Timer finished: ${it.name}")
                    sendTimerUpdate(updatedList.id, it, 0)
                }
            )
        }
    }

    private fun sendTimerUpdate(listId: String, timer: Timer, secondsRemaining: Long) {
        webSocketClient.sendMessage(
            Json.encodeToString(
                TimerWebSocketClientMessage.serializer(),
                TimerWebSocketClientMessage.TimerUpdate(
                    listId = listId,
                    timer = timer,
                    remainingSeconds = secondsRemaining.toLong()
                )
            )
        )
    }

    fun startTimer(timerList: TimerList) {
        viewModelScope.launch {
            timerPlaybackManager.startTimerList(timerList, onEachTimerFinished = { timer ->
                sendTimerUpdate(timerList.id, timer, 0)
                handleTimerCompletion(timerList, timer)
            })
            if (timerList.timers.isNullOrEmpty()) return@launch
            sendTimerUpdate(timerList.id, timerList.timers!!.first(),
                timerList.timers?.firstOrNull()?.duration ?: 0)
        }
    }

    fun pauseTimer() {
        viewModelScope.launch {
            val currentList = timerPlaybackManager.uiState.value.timerList ?: return@launch
            val currentTimer = timerPlaybackManager.uiState.value.currentTimer ?: return@launch
            timerPlaybackManager.pauseTimer()
            sendTimerUpdate(currentList.id, currentTimer,
                timerPlaybackManager.uiState.value.remainingMillis)
        }
    }

    fun resumeTimer() {
        viewModelScope.launch {
            val currentList = timerPlaybackManager.uiState.value.timerList ?: return@launch
            val currentTimer = timerPlaybackManager.uiState.value.currentTimer ?: return@launch
            timerPlaybackManager.resumeTimer(onEachTimerFinished = { timer ->
                sendTimerUpdate(currentList.id, timer, 0)
                handleTimerCompletion(currentList, timer)
            })
            sendTimerUpdate(currentList.id, currentTimer,
                timerPlaybackManager.uiState.value.remainingMillis)
        }
    }

    fun stopTimer() {
        viewModelScope.launch {
            val currentList = timerPlaybackManager.uiState.value.timerList ?: return@launch
            val currentTimer = timerPlaybackManager.uiState.value.currentTimer ?: return@launch
            timerPlaybackManager.stopTimer()
            sendTimerUpdate(currentList.id, currentTimer, 0)
        }
    }

    fun getTimerNotifications(timerId: String): List<TimerNotification> {
        return _notifications.value.filter { it.timerId == timerId }
    }

    fun updateListNotifications(listId: String) {
        _listNotifications.value = _notifications.value.filter { it.listId == listId }
    }

    fun disconnectWebSocket() {
        webSocketClient.disconnect()
    }

    fun createTimerList(name: String, loopTimers: Boolean, pomodoroGrouped: Boolean) {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = timerService.createTimerList(
                    token = tokenStorageImpl.getToken() ?: "",
                    name = name,
                    loopTimers = loopTimers,
                    pomodoroGrouped = pomodoroGrouped
                )
                loadTimerLists()
            } catch (e: Exception) {
                error = e.message
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    fun updateTimerList(
        listId: String,
        name: String,
        loopTimers: Boolean,
        pomodoroGrouped: Boolean
    ) {
        viewModelScope.launch {
            isLoading = true
            try {
                timerService.updateTimerList(
                    token = tokenStorageImpl.getToken() ?: "",
                    listId = listId,
                    name = name,
                    loopTimers = loopTimers,
                    pomodoroGrouped = pomodoroGrouped
                )
                loadTimerLists()
            } catch (e: Exception) {
                error = e.message
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    fun deleteTimerList(listId: String) {
        viewModelScope.launch {
            isLoading = true
            try {
                timerService.deleteTimerList(
                    token = tokenStorageImpl.getToken() ?: "",
                    listId = listId
                )
                loadTimerLists()
            } catch (e: Exception) {
                error = e.message
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    fun createTimer(
        listId: String,
        name: String,
        duration: Long,
        enabled: Boolean,
        countsAsPomodoro: Boolean,
        order: Int,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            isLoading = true
            try {
                timerService.createTimer(
                    token = tokenStorageImpl.getToken() ?: "",
                    listId = listId,
                    name = name,
                    duration = duration,
                    enabled = enabled,
                    countsAsPomodoro = countsAsPomodoro,
                    order = order
                )
                onSuccess()
            } catch (e: Exception) {
                error = e.message
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    fun updateTimer(
        timerId: String,
        name: String,
        duration: Long,
        enabled: Boolean,
        countsAsPomodoro: Boolean,
        order: Int,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            isLoading = true
            try {
                timerService.updateTimer(
                    token = tokenStorageImpl.getToken() ?: "",
                    timerId = timerId,
                    name = name,
                    duration = duration,
                    enabled = enabled,
                    countsAsPomodoro = countsAsPomodoro,
                    order = order
                )
                onSuccess()
            } catch (e: Exception) {
                error = e.message
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    fun deleteTimer(timerId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            isLoading = true
            try {
                timerService.deleteTimer(
                    token = tokenStorageImpl.getToken() ?: "",
                    timerId = timerId
                )
                onSuccess()
            } catch (e: Exception) {
                error = e.message
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    fun updateUserSettings(
        defaultTimerListId: String?,
        dailyPomodoroGoal: Int,
        notificationsEnabled: Boolean
    ) {
        viewModelScope.launch {
            isLoading = true
            try {
                timerService.updateUserSettings(
                    token = tokenStorageImpl.getToken() ?: "",
                    defaultTimerListId = defaultTimerListId,
                    dailyPomodoroGoal = dailyPomodoroGoal,
                    notificationsEnabled = notificationsEnabled
                )
                loadUserSettings()
            } catch (e: Exception) {
                error = e.message
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    fun loadTimerListByID(listId: String) {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = timerService.getTimerList(
                    token = tokenStorageImpl.getToken() ?: "",
                    listId = listId
                )
                _timerDetailList.value = response
            } catch (e: Exception) {
                error = e.message
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    suspend fun getTimerListByID(listId: String): TimerList? {
        return timerService.getTimerList(
            token = tokenStorageImpl.getToken() ?: "",
            listId
        )
    }

    fun loadTimerLists() {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = timerService.getTimerLists(
                    token = tokenStorageImpl.getToken() ?: ""
                )
                _timerLists.value = response
            } catch (e: Exception) {
                error = e.message
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    fun loadUserSettings() {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = timerService.getUserSettings(
                    token = tokenStorageImpl.getToken() ?: ""
                )
                _userSettings.value = response
            } catch (e: Exception) {
                error = e.message
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    private fun handleTimerCompletion(timerList: TimerList, completedTimer: Timer) {
        viewModelScope.launch {
            try {
                // Get all enabled timers
                val enabledTimers = timerList.timers?.filter { it.enabled } ?: emptyList()
                
                // Check if this is the last timer in the list
                val isLastTimer = enabledTimers.lastOrNull()?.id == completedTimer.id
                
                // Determine if we should create a pomodoro
                val shouldCreatePomodoro = when {
                    // If list is pomodoro grouped, only create on last timer
                    timerList.pomodoroGrouped -> isLastTimer
                    // If list is not pomodoro grouped, create for each pomodoro timer
                    else -> completedTimer.countsAsPomodoro
                }

                if (shouldCreatePomodoro) {
                    val now = Clock.System.now()
                    val past = now - 5.seconds
                    val endDate = now.toLocalDateTime(TimeZone.currentSystemDefault())
                    val startDate = past.toLocalDateTime(TimeZone.currentSystemDefault())
                    
                    pomodoroService.createPomodoro(
                        CreatePomodoroRequest(
                            startDateTime = startDate.formatWithSeconds(),
                            endDateTime = endDate.formatWithSeconds()
                        )
                    )
                    loadPomodoros()
                }
            } catch (e: Exception) {
                error = "Failed to create pomodoro: ${e.message}"
                e.printStackTrace()
            }
        }
    }


    fun loadPomodoros() {
        viewModelScope.launch {
            try {
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                val result = pomodoroService.getPomodoros(
                    startDate = LocalDate.now().format(formatter),
                    endDate = LocalDate.now().format(formatter),
                    limit = 30
                )
                _pomodoros.value = result
                statusBarService.updatePomodoroCount(result.size)
            } catch (e: Exception) {
                error = e.message
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    fun addSamplePomodoro(){
        viewModelScope.launch {
            try {
                val pomodoro = CreatePomodoroRequest(
                    startDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).formatWithSeconds(),
                    endDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).formatWithSeconds()
                )
                pomodoroService.createPomodoro(pomodoro)
                loadPomodoros()
            } catch (e: Exception) {
                error = e.message
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    fun removeLastPomodoro() {
        viewModelScope.launch {
            isLoading = true
            try {
                if (_pomodoros.value.isNotEmpty()) {
                    val lastPomodoro = _pomodoros.value.last()
                    pomodoroService.removePomodoro(lastPomodoro.id)
                    loadPomodoros()
                } else {
                    error = "No pomodoros to remove"
                }
            } catch (e: Exception) {
                error = e.message
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    fun removePomodoro(pomodoroId: String) {
        viewModelScope.launch {
            isLoading = true
            try {
                pomodoroService.removePomodoro(pomodoroId)
                loadPomodoros()
            } catch (e: Exception) {
                error = e.message
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        disconnectWebSocket()
    }
}