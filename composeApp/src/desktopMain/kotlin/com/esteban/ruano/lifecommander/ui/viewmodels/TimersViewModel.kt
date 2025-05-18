package com.esteban.ruano.lifecommander.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    private val _connectionState = MutableStateFlow<TimerWebSocketClient.ConnectionState>(TimerWebSocketClient.ConnectionState.Disconnected)
    val connectionState: StateFlow<TimerWebSocketClient.ConnectionState> = _connectionState.asStateFlow()

    private val _waitingForServer = MutableStateFlow<String?>(null)
    val waitingForServer: StateFlow<String?> = _waitingForServer.asStateFlow()

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
                    println("Converting msg to TimerWebSocketServerMessage")
                    val msg = Json.decodeFromString<TimerWebSocketServerMessage>(event)
                    println("Converted msg to TimerWebSocketServerMessage: $msg")
                    when (msg) {
                        is TimerWebSocketServerMessage.TimerListStarted -> {
                            if (_waitingForServer.value == msg.listId) {
                                println("Timer list started: ${msg.listId}")
                                val timerList = getTimerListByID(msg.listId)
                                if (timerList != null) {
                                    println("Starting timer list: $timerList")
                                    startTimerList(timerList)
                                    _waitingForServer.value = null
                                } else {
                                    error = "Timer list not found for id ${msg.listId}"
                                }
                            }
                        }

                        is TimerWebSocketServerMessage.TimerListCompleted -> TODO()
                        is TimerWebSocketServerMessage.TimerListPaused -> TODO()
                        is TimerWebSocketServerMessage.TimerListResumed -> TODO()
                        is TimerWebSocketServerMessage.TimerListStopped -> TODO()
                    }
                } catch (e: Exception) {
                    // Ignore or log parse errors
                }
            }
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
        duration: Int,
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
        duration: Int,
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
            listId)
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

    fun sendStartTimerListSignal(timerList: TimerList) {
        _waitingForServer.value = timerList.id
        webSocketClient.sendMessage(
            Json.encodeToString(
                TimerWebSocketClientMessage.serializer(
                ),
                TimerWebSocketClientMessage.StartTimerList(
                    listId = timerList.id
                )
            )
        )
    }

    fun startTimerList(timerList: TimerList) {
        viewModelScope.launch {
            timerPlaybackManager.startTimerList(timerList)
        }
    }

    fun pauseTimer() {
        viewModelScope.launch {
            timerPlaybackManager.pauseTimer()
        }
    }

    fun resumeTimer() {
        viewModelScope.launch {
            timerPlaybackManager.resumeTimer()
        }
    }

    fun stopTimer() {
        viewModelScope.launch {
            timerPlaybackManager.stopTimer()
        }
    }

    override fun onCleared() {
        super.onCleared()
        disconnectWebSocket()
    }
}