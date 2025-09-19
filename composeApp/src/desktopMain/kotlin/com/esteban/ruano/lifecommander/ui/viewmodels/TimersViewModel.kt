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
import com.esteban.ruano.lifecommander.timer.TimerNotification
import com.esteban.ruano.lifecommander.timer.TimerPlaybackManager
import com.esteban.ruano.lifecommander.timer.TimerPlaybackState
import com.esteban.ruano.lifecommander.timer.TimerWebSocketClientMessage
import com.esteban.ruano.lifecommander.timer.TimerWebSocketServerMessage
import com.esteban.ruano.lifecommander.websocket.TimerWebSocketClient
import com.esteban.ruano.utils.DateUIUtils.formatWithSeconds
import com.esteban.ruano.utils.DateUIUtils.getCurrentDateTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import services.auth.TokenStorageImpl
import kotlinx.serialization.json.Json
import services.dailyjournal.PomodoroService
import services.dailyjournal.models.PomodoroResponse
import ui.services.dailyjournal.models.CreatePomodoroRequest
import utils.StatusBarService
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

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

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Additional loading and error states for specific features
    private val _timersLoading = MutableStateFlow(false)
    val timersLoading: StateFlow<Boolean> = _timersLoading.asStateFlow()

    private val _timersError = MutableStateFlow<String?>(null)
    val timersError: StateFlow<String?> = _timersError.asStateFlow()

    private val _pomodorosLoading = MutableStateFlow(false)
    val pomodorosLoading: StateFlow<Boolean> = _pomodorosLoading.asStateFlow()

    private val _pomodorosError = MutableStateFlow<String?>(null)
    val pomodorosError: StateFlow<String?> = _pomodorosError.asStateFlow()

    private val _settingsLoading = MutableStateFlow(false)
    val settingsLoading: StateFlow<Boolean> = _settingsLoading.asStateFlow()

    private val _settingsError = MutableStateFlow<String?>(null)
    val settingsError: StateFlow<String?> = _settingsError.asStateFlow()

    private val _timerDetailLoading = MutableStateFlow(false)
    val timerDetailLoading: StateFlow<Boolean> = _timerDetailLoading.asStateFlow()

    private val _timerDetailError = MutableStateFlow<String?>(null)
    val timerDetailError: StateFlow<String?> = _timerDetailError.asStateFlow()

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
                _error.value = "Unable to refresh timer list"
                return@launch
            }

            val timerIndex = updatedList.timers?.indexOfFirst { it.id == timer.id } ?: -1

            if (timerIndex == -1) {
                println("[Client] Timer ${timer.id} not found in list. Maybe out of sync?")
                _error.value = "Timer not found in list"
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
            _isLoading.value = true
            _error.value = null
            try {
                val response = timerService.createTimerList(
                    token = tokenStorageImpl.getToken() ?: "",
                    name = name,
                    loopTimers = loopTimers,
                    pomodoroGrouped = pomodoroGrouped
                )
                loadTimerLists()
            } catch (e: Exception) {
                _error.value = e.message
                e.printStackTrace()
            } finally {
                _isLoading.value = false
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
            _isLoading.value = true
            _error.value = null
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
                _error.value = e.message
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteTimerList(listId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                timerService.deleteTimerList(
                    token = tokenStorageImpl.getToken() ?: "",
                    listId = listId
                )
                loadTimerLists()
            } catch (e: Exception) {
                _error.value = e.message
                e.printStackTrace()
            } finally {
                _isLoading.value = false
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
            _isLoading.value = true
            _error.value = null
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
                _error.value = e.message
                e.printStackTrace()
            } finally {
                _isLoading.value = false
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
            _isLoading.value = true
            _error.value = null
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
                _error.value = e.message
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteTimer(timerId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                timerService.deleteTimer(
                    token = tokenStorageImpl.getToken() ?: "",
                    timerId = timerId
                )
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateUserSettings(
        defaultTimerListId: String?,
        dailyPomodoroGoal: Int,
        notificationsEnabled: Boolean
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                timerService.updateUserSettings(
                    token = tokenStorageImpl.getToken() ?: "",
                    defaultTimerListId = defaultTimerListId,
                    dailyPomodoroGoal = dailyPomodoroGoal,
                    notificationsEnabled = notificationsEnabled
                )
                loadUserSettings()
            } catch (e: Exception) {
                _error.value = e.message
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadTimerListByID(listId: String) {
        viewModelScope.launch {
            _timerDetailLoading.value = true
            _timerDetailError.value = null
            try {
                val response = timerService.getTimerList(
                    token = tokenStorageImpl.getToken() ?: "",
                    listId = listId
                )
                _timerDetailList.value = response
            } catch (e: Exception) {
                _timerDetailError.value = e.message ?: "Failed to load timer list details"
                e.printStackTrace()
            } finally {
                _timerDetailLoading.value = false
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
            _timersLoading.value = true
            _timersError.value = null
            try {
                val response = timerService.getTimerLists(
                    token = tokenStorageImpl.getToken() ?: ""
                )
                _timerLists.value = response
            } catch (e: Exception) {
                _timersError.value = e.message ?: "Failed to load timer lists"
                e.printStackTrace()
            } finally {
                _timersLoading.value = false
            }
        }
    }

    fun loadUserSettings() {
        viewModelScope.launch {
            _settingsLoading.value = true
            _settingsError.value = null
            try {
                val response = timerService.getUserSettings(
                    token = tokenStorageImpl.getToken() ?: ""
                )
                _userSettings.value = response
            } catch (e: Exception) {
                _settingsError.value = e.message ?: "Failed to load user settings"
                e.printStackTrace()
            } finally {
                _settingsLoading.value = false
            }
        }
    }

    @OptIn(ExperimentalTime::class)
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
                    val now = getCurrentDateTime(TimeZone.currentSystemDefault())
                    val past = now.toInstant(TimeZone.currentSystemDefault()).minus(5.seconds).toLocalDateTime(TimeZone.currentSystemDefault())
                    val endDate = now
                    val startDate = past

                    pomodoroService.createPomodoro(
                        CreatePomodoroRequest(
                            startDateTime = startDate.formatWithSeconds(),
                            endDateTime = endDate.formatWithSeconds()
                        )
                    )
                    loadPomodoros()
                }
            } catch (e: Exception) {
                _error.value = "Failed to create pomodoro: ${e.message}"
                e.printStackTrace()
            }
        }
    }

    fun loadPomodorosByDateRange(
        startDate: LocalDate,
        endDate: LocalDate,
        limit: Int = 100
    ) {
        viewModelScope.launch {
            _pomodorosLoading.value = true
            _pomodorosError.value = null
            try {
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                val result = pomodoroService.getPomodoros(
                    startDate = startDate.format(formatter),
                    endDate = endDate.format(formatter),
                    limit = limit
                )
                _pomodoros.value = result
                statusBarService.updatePomodoroCount(result.size)
            } catch (e: Exception) {
                _pomodorosError.value = e.message ?: "Failed to load pomodoros"
                e.printStackTrace()
            } finally {
                _pomodorosLoading.value = false
            }
        }
    }

    fun loadPomodoros() {
        viewModelScope.launch {
            _pomodorosLoading.value = true
            _pomodorosError.value = null
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
                _pomodorosError.value = e.message ?: "Failed to load pomodoros"
                e.printStackTrace()
            } finally {
                _pomodorosLoading.value = false
            }
        }
    }

    fun addSamplePomodoro(){
        viewModelScope.launch {
            try {
                val pomodoro = CreatePomodoroRequest(
                    startDateTime =getCurrentDateTime(TimeZone.currentSystemDefault()).formatWithSeconds(),
                    endDateTime = getCurrentDateTime(TimeZone.currentSystemDefault()).formatWithSeconds()
                )
                pomodoroService.createPomodoro(pomodoro)
                loadPomodoros()
            } catch (e: Exception) {
                _error.value = e.message
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun removeLastPomodoro() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                if (_pomodoros.value.isNotEmpty()) {
                    val lastPomodoro = _pomodoros.value.last()
                    pomodoroService.removePomodoro(lastPomodoro.id)
                    loadPomodoros()
                } else {
                    _error.value = "No pomodoros to remove"
                }
            } catch (e: Exception) {
                _error.value = e.message
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun removePomodoro(pomodoroId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                pomodoroService.removePomodoro(pomodoroId)
                loadPomodoros()
            } catch (e: Exception) {
                _error.value = e.message
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadSettings() {
        viewModelScope.launch {
            _settingsLoading.value = true
            _settingsError.value = null
            try {
                loadTimerLists()
                loadUserSettings()
            } catch (e: Exception) {
                _settingsError.value = e.message ?: "Failed to load settings"
                e.printStackTrace()
            } finally {
                _settingsLoading.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        disconnectWebSocket()
    }
}