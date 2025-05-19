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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import services.auth.TokenStorageImpl
import kotlinx.serialization.json.Json

class TimersViewModel(
    private val tokenStorageImpl: TokenStorageImpl,
    private val timerService: TimerService,
    private val timerPlaybackManager: TimerPlaybackManager,
    private val webSocketClient: TimerWebSocketClient
) : ViewModel() {
    private val _timerLists = MutableStateFlow<List<TimerList>>(emptyList())
    val timerLists: StateFlow<List<TimerList>> = _timerLists.asStateFlow()

    private val _userSettings = MutableStateFlow<UserSettings?>(null)
    val userSettings: StateFlow<UserSettings?> = _userSettings.asStateFlow()

    val timerPlaybackState: StateFlow<TimerPlaybackState> = timerPlaybackManager.uiState

    private val _notifications = MutableStateFlow<List<TimerNotification>>(emptyList())
    val notifications: StateFlow<List<TimerNotification>> = _notifications.asStateFlow()

    private val _listNotifications = MutableStateFlow<List<TimerNotification>>(emptyList())
    val listNotifications: StateFlow<List<TimerNotification>> = _listNotifications.asStateFlow()

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
            timerPlaybackManager.startTimerList(timerList) { timer ->
                sendTimerUpdate(timerList.id, timer, 0)
            }
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
            timerPlaybackManager.resumeTimer()
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
                _timerLists.value = listOf(response)
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

    override fun onCleared() {
        super.onCleared()
        disconnectWebSocket()
    }
}