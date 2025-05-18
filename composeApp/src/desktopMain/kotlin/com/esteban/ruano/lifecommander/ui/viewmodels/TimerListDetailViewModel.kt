package com.esteban.ruano.lifecommander.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esteban.ruano.lifecommander.models.Timer
import com.esteban.ruano.lifecommander.models.TimerList
import com.esteban.ruano.lifecommander.timer.TimerNotification
import com.esteban.ruano.lifecommander.timer.TimerPlaybackManager
import com.esteban.ruano.lifecommander.timer.TimerPlaybackState
import com.esteban.ruano.lifecommander.websocket.TimerWebSocketClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class TimerListDetailViewModel(
    private val timerList: TimerList,
    private val webSocketClient: TimerWebSocketClient,
    private val timerPlaybackManager: TimerPlaybackManager
) : ViewModel() {
    private val _timerPlaybackState = MutableStateFlow<TimerPlaybackState>(TimerPlaybackState())
    val timerPlaybackState: StateFlow<TimerPlaybackState> = _timerPlaybackState.asStateFlow()

    private val _notifications = MutableStateFlow<List<TimerNotification>>(emptyList())
    val notifications: StateFlow<List<TimerNotification>> = _notifications.asStateFlow()

    private val _listNotifications = MutableStateFlow<List<TimerNotification>>(emptyList())
    val listNotifications: StateFlow<List<TimerNotification>> = _listNotifications.asStateFlow()

    var showAddTimerDialog by mutableStateOf(false)
        private set

    init {
        connectWebSocket()
    }

    private fun connectWebSocket() {
        viewModelScope.launch{
            webSocketClient.connect()
            webSocketClient.timerNotifications.collectLatest { notifications ->
                _notifications.value = notifications
                _listNotifications.value = notifications.filter { it.listId == timerList.id }
            }
        }    }

    fun disconnectWebSocket() {
        webSocketClient.disconnect()
    }

    fun showAddTimerDialog() {
        showAddTimerDialog = true
    }

    fun hideAddTimerDialog() {
        showAddTimerDialog = false
    }

    fun getTimerNotifications(timerId: String): List<TimerNotification> {
        return _notifications.value.filter { it.timerId == timerId }
    }

    fun updateTimerPlaybackState(state: TimerPlaybackState) {
        _timerPlaybackState.value = state
    }
} 