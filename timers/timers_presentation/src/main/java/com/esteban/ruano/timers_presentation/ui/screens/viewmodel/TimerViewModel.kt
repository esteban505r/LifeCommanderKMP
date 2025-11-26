package com.esteban.ruano.timers_presentation.ui.screens.viewmodel

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.esteban.lopez.core.utils.SnackbarType
import com.esteban.ruano.core.domain.preferences.Preferences
import com.esteban.ruano.core_ui.view_model.BaseViewModel
import com.esteban.ruano.lifecommander.models.Timer
import com.esteban.ruano.lifecommander.models.TimerList
import com.esteban.ruano.lifecommander.timer.TimerConnectionState
import com.esteban.ruano.lifecommander.timer.TimerNotification
import com.esteban.ruano.lifecommander.timer.TimerPlaybackManager
import com.esteban.ruano.lifecommander.timer.TimerPlaybackState
import com.esteban.ruano.lifecommander.timer.TimerPlaybackStatus
import com.esteban.ruano.lifecommander.timer.TimerWebSocketServerMessage
import com.esteban.ruano.timers_domain.repository.TimerWebSocketRepository
import com.esteban.ruano.timers_domain.use_case.TimerUseCases
import com.esteban.ruano.timers_presentation.service.TimerForegroundService
import com.esteban.ruano.timers_presentation.ui.intent.TimerEffect
import com.esteban.ruano.timers_presentation.ui.intent.TimerIntent
import com.esteban.ruano.timers_presentation.ui.screens.viewmodel.state.TimerState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferences: Preferences,
    private val timerUseCases: TimerUseCases,
    private val webSocketRepository: TimerWebSocketRepository
) : BaseViewModel<TimerIntent, TimerState, TimerEffect>() {

    private val timerPlaybackManager: TimerPlaybackManager by lazy {
        TimerPlaybackManager()
    }

    private var serverTimeOffset: Long = 0L

    // Observing WebSocket state and incoming messages
    init {
        observeWebSocketState()
        observeWebSocketNotifications()
        observeWebSocketMessages()
        observeTimerPlaybackState()
    }

    override fun createInitialState(): TimerState = TimerState()

    // Observing the WebSocket connection state
    private fun observeWebSocketState() {
        viewModelScope.launch {
            webSocketRepository.connectionState.collectLatest { state ->
                emitState { currentState.copy(connectionState = state) }
                if (state is TimerConnectionState.Connected) {
                    syncRunningTimersFromServer()
                }
            }
        }
    }

    // Observing WebSocket notifications
    private fun observeWebSocketNotifications() {
        viewModelScope.launch {
            webSocketRepository.timerNotifications.collectLatest { notifications ->
                emitState { currentState.copy(notifications = notifications) }
            }
        }
    }

    // Handling incoming WebSocket messages
    private fun observeWebSocketMessages() {
        viewModelScope.launch {
            webSocketRepository.incomingMessages.collectLatest { msg ->
                when (msg) {
                    is TimerWebSocketServerMessage.TimerUpdate -> {
                        refreshTimerListFromServer(msg.timer, msg.remainingTime, msg.listId)
                    }
                    is TimerWebSocketServerMessage.Pong -> {
                        val clientTime = msg.clientTime ?: System.currentTimeMillis()
                        serverTimeOffset = msg.serverTime - clientTime
                    }
                    is TimerWebSocketServerMessage.TimerListUpdate -> {
                        fetchTimerLists()
                    }
                    is TimerWebSocketServerMessage.TimerListRefresh -> {
                        if (msg.listId != null) {
                            fetchTimerList(msg.listId!!)
                        } else {
                            fetchTimerLists()
                        }
                    }
                }
            }
        }
    }

    // Observing the playback state changes
    private fun observeTimerPlaybackState() {
        viewModelScope.launch {
            timerPlaybackManager.uiState.collectLatest { playbackState ->
                emitState { currentState.copy(timerPlaybackState = playbackState) }
                updateForegroundServiceWithPlaybackState(playbackState)
            }
        }
    }

    override fun handleIntent(intent: TimerIntent) {
        when (intent) {
            is TimerIntent.FetchTimerLists -> fetchTimerLists()
            is TimerIntent.FetchTimerList -> fetchTimerList(intent.listId)
            is TimerIntent.CreateTimerList -> createTimerList(intent.name, intent.loopTimers, intent.pomodoroGrouped)
            is TimerIntent.UpdateTimerList -> updateTimerList(intent.listId, intent.name, intent.loopTimers, intent.pomodoroGrouped)
            is TimerIntent.DeleteTimerList -> deleteTimerList(intent.listId)
            is TimerIntent.CreateTimer -> createTimer(intent.listId, intent.name, intent.duration, intent.enabled, intent.countsAsPomodoro, intent.sendNotificationOnComplete, intent.order)
            is TimerIntent.UpdateTimer -> updateTimer(intent.timerId, intent.name, intent.duration, intent.enabled, intent.countsAsPomodoro, intent.sendNotificationOnComplete, intent.order)
            is TimerIntent.DeleteTimer -> deleteTimer(intent.timerId)
            is TimerIntent.StartTimer -> startTimer(intent.timerList)
            is TimerIntent.PauseTimer -> pauseTimer()
            is TimerIntent.ResumeTimer -> resumeTimer()
            is TimerIntent.StopTimer -> stopTimer()
            is TimerIntent.ConnectWebSocket -> {
                viewModelScope.launch {
                    if (webSocketRepository.connectionState.value is TimerConnectionState.Connected) {
                        syncRunningTimersFromServer()
                    }
                }
            }
            is TimerIntent.ReconnectWebSocket -> {
                webSocketRepository.disconnect()
                webSocketRepository.connect()
            }
        }
    }

    // Fetch timer lists and update the state
    private fun fetchTimerLists() {
        viewModelScope.launch {
            emitState { currentState.copy(isLoading = true, isError = false) }
            timerUseCases.getTimerLists().fold(
                onSuccess = { lists ->
                    emitState {
                        currentState.copy(
                            timerLists = lists,
                            isLoading = false,
                            timerPlaybackState = timerPlaybackManager.uiState.value
                        )
                    }
                    updateForegroundService(lists)
                },
                onFailure = { e ->
                    emitState {
                        currentState.copy(
                            isError = true,
                            errorMessage = e.message ?: "Failed to load timer lists",
                            isLoading = false
                        )
                    }
                    sendErrorEffect()
                }
            )
        }
    }

    // Fetch a single timer list
    private fun fetchTimerList(listId: String) {
        viewModelScope.launch {
            emitState { currentState.copy(timerDetailLoading = true, timerDetailError = null) }
            timerUseCases.getTimerList(listId).fold(
                onSuccess = { list ->
                    emitState {
                        currentState.copy(
                            timerDetailList = list,
                            timerDetailLoading = false,
                            listNotifications = currentState.notifications.filter { it.listId == listId }
                        )
                    }
                },
                onFailure = { e ->
                    emitState {
                        currentState.copy(
                            timerDetailError = e.message ?: "Failed to load timer list",
                            timerDetailLoading = false
                        )
                    }
                }
            )
        }
    }

    // Handle foreground service updates with the latest timer data
    private fun updateForegroundService(lists: List<TimerList>) {
        val activeTimers = lists.flatMap { it.timers ?: emptyList() }.filter { it.state == "RUNNING" || it.state == "PAUSED" }
        if (activeTimers.isNotEmpty()) {
            TimerForegroundService.startService(context)
            TimerForegroundService.updateTimers(activeTimers, lists)
        } else {
            TimerForegroundService.stopService(context)
        }
    }

    // Handle playback state update to sync with the foreground service
    private fun updateForegroundServiceWithPlaybackState(playbackState: TimerPlaybackState?) {
        if (playbackState == null) {
            TimerForegroundService.stopService(context)
            return
        }
        val updatedTimer = playbackState.currentTimer?.copy(remainingSeconds = playbackState.remainingMillis / 1000)
        val updatedList = playbackState.timerList?.copy(
            timers = playbackState.timerList?.timers?.map { if (it.id == updatedTimer?.id) updatedTimer else it }
        )
        val allLists = currentState.timerLists.toMutableList()
        if (updatedList != null) {
            allLists[allLists.indexOfFirst { it.id == updatedList.id }] = updatedList
        }

        TimerForegroundService.updateTimers(allLists.flatMap { it.timers ?: emptyList() }, allLists)
    }

    // Sync running timers from the server and update the foreground service
    private suspend fun syncRunningTimersFromServer() {
        timerUseCases.getTimerLists().fold(
            onSuccess = { lists ->
                lists.forEach { timerList ->
                    timerList.timers?.forEach { timer ->
                        if (timer.state == "RUNNING") {
                            val remainingSeconds = timer.remainingSeconds
                            if (remainingSeconds > 0) {
                                refreshTimerListFromServer(timer, remainingSeconds, timerList.id)
                            }
                        }
                    }
                }
            },
            onFailure = { }
        )
    }

    // Refresh timer data when updated from the server
    private fun refreshTimerListFromServer(timer: Timer, remainingSeconds: Long, listId: String) {
        viewModelScope.launch {
            val currentLists = currentState.timerLists.toMutableList()
            val listIndex = currentLists.indexOfFirst { it.id == listId }
            if (listIndex == -1) {
                fetchTimerLists()
                return@launch
            }

            var updatedList = currentLists[listIndex]
            val timerIndex = updatedList.timers?.indexOfFirst { it.id == timer.id } ?: -1
            if (timerIndex == -1) {
                fetchTimerList(listId)
                return@launch
            }

            val updatedTimers = updatedList.timers?.toMutableList() ?: mutableListOf()
            updatedTimers[timerIndex] = timer.copy(remainingSeconds = remainingSeconds)
            updatedList = updatedList.copy(timers = updatedTimers)
            currentLists[listIndex] = updatedList
            emitState { currentState.copy(timerLists = currentLists) }

            if (currentState.timerDetailList?.id == listId) {
                emitState { currentState.copy(timerDetailList = updatedList) }
            }

            timerPlaybackManager.overridePlaybackState(
                timerList = updatedList,
                timer = timer.copy(remainingSeconds = remainingSeconds),
                timerIndex = timerIndex,
                timeRemaining = remainingSeconds * 1000,
                onEachTimerFinished = {
                    handleTimerCompletion(updatedList, it)
                }
            )
            
            updateForegroundService(currentLists)
        }
    }

    // Create a new timer list
    private fun createTimerList(name: String, loopTimers: Boolean, pomodoroGrouped: Boolean) {
        viewModelScope.launch {
            timerUseCases.createTimerList(name, loopTimers, pomodoroGrouped).fold(
                onSuccess = {
                    sendSuccessEffect()
                    fetchTimerLists()
                },
                onFailure = {
                    sendErrorEffect()
                }
            )
        }
    }

    // Update an existing timer list
    private fun updateTimerList(listId: String, name: String, loopTimers: Boolean, pomodoroGrouped: Boolean) {
        viewModelScope.launch {
            timerUseCases.updateTimerList(listId, name, loopTimers, pomodoroGrouped).fold(
                onSuccess = {
                    sendSuccessEffect()
                    fetchTimerLists()
                    if (currentState.timerDetailList?.id == listId) {
                        fetchTimerList(listId)
                    }
                },
                onFailure = {
                    sendErrorEffect()
                }
            )
        }
    }

    // Delete a timer list
    private fun deleteTimerList(listId: String) {
        viewModelScope.launch {
            timerUseCases.deleteTimerList(listId).fold(
                onSuccess = {
                    sendSuccessEffect()
                    fetchTimerLists()
                },
                onFailure = {
                    sendErrorEffect()
                }
            )
        }
    }

    // Create a new timer
    private fun createTimer(
        listId: String,
        name: String,
        duration: Long,
        enabled: Boolean,
        countsAsPomodoro: Boolean,
        sendNotificationOnComplete: Boolean,
        order: Int
    ) {
        viewModelScope.launch {
            timerUseCases.createTimer(
                listId, name, duration, enabled,
                countsAsPomodoro, sendNotificationOnComplete, order
            ).fold(
                onSuccess = {
                    sendSuccessEffect()
                    if (currentState.timerDetailList?.id == listId) {
                        fetchTimerList(listId)
                    } else {
                        fetchTimerLists()
                    }
                },
                onFailure = {
                    sendErrorEffect()
                }
            )
        }
    }

    // Update an existing timer
    private fun updateTimer(
        timerId: String,
        name: String,
        duration: Long,
        enabled: Boolean,
        countsAsPomodoro: Boolean,
        sendNotificationOnComplete: Boolean,
        order: Int
    ) {
        viewModelScope.launch {
            timerUseCases.updateTimer(
                timerId = timerId,
                name = name,
                duration = duration,
                enabled = enabled,
                countsAsPomodoro = countsAsPomodoro,
                sendNotificationOnComplete = sendNotificationOnComplete,
                order = order
            ).fold(
                onSuccess = {
                    sendSuccessEffect()
                    val listId = currentState.timerDetailList?.id
                    if (listId != null) {
                        fetchTimerList(listId)
                    } else {
                        fetchTimerLists()
                    }
                },
                onFailure = {
                    sendErrorEffect()
                }
            )
        }
    }

    // Delete a timer
    private fun deleteTimer(timerId: String) {
        viewModelScope.launch {
            timerUseCases.deleteTimer(timerId).fold(
                onSuccess = {
                    sendSuccessEffect()
                    val listId = currentState.timerDetailList?.id
                    if (listId != null) {
                        fetchTimerList(listId)
                    } else {
                        fetchTimerLists()
                    }
                },
                onFailure = {
                    sendErrorEffect()
                }
            )
        }
    }

    // Start a timer
    private fun startTimer(timerList: TimerList) {
        viewModelScope.launch {
            val enabledTimers = timerList.timers?.filter { it.enabled } ?: emptyList()
            if (enabledTimers.isEmpty()) {
                sendEffect {
                    TimerEffect.ShowSnackBar("No enabled timers in this list", SnackbarType.ERROR)
                }
                return@launch
            }

            val firstTimer = enabledTimers.first()
            timerUseCases.startTimer(timerList.id, firstTimer.id).fold(
                onSuccess = {
                    timerPlaybackManager.startTimerList(timerList) {
                        handleTimerCompletion(timerList, it)
                    }
                    emitState {
                        currentState.copy(timerPlaybackState = timerPlaybackManager.uiState.value)
                    }
                    fetchTimerLists()
                },
                onFailure = {
                    sendErrorEffect()
                }
            )
        }
    }

    // Pause the current timer
    private fun pauseTimer() {
        viewModelScope.launch {
            val timerList = currentState.timerPlaybackState?.timerList
            val currentTimer = currentState.timerPlaybackState?.currentTimer
            if (timerList != null && currentTimer != null) {
                timerUseCases.pauseTimer(timerList.id, currentTimer.id).fold(
                    onSuccess = {
                        timerPlaybackManager.pauseTimer()
                        emitState {
                            currentState.copy(timerPlaybackState = timerPlaybackManager.uiState.value)
                        }
                        fetchTimerLists()
                    },
                    onFailure = {
                        sendErrorEffect()
                    }
                )
            }
        }
    }

    // Resume the paused timer
    private fun resumeTimer() {
        viewModelScope.launch {
            val timerList = currentState.timerPlaybackState?.timerList
            if (timerList != null) {
                timerUseCases.resumeTimer(timerList.id).fold(
                    onSuccess = {
                        timerPlaybackManager.resumeTimer()
                        emitState {
                            currentState.copy(timerPlaybackState = timerPlaybackManager.uiState.value)
                        }
                        fetchTimerLists()
                    },
                    onFailure = {
                        sendErrorEffect()
                    }
                )
            }
        }
    }

    // Stop the current timer
    private fun stopTimer() {
        viewModelScope.launch {
            val timerList = currentState.timerPlaybackState?.timerList
            val currentTimer = currentState.timerPlaybackState?.currentTimer
            if (timerList != null && currentTimer != null) {
                timerUseCases.stopTimer(timerList.id, currentTimer.id).fold(
                    onSuccess = {
                        timerPlaybackManager.stopTimer()
                        emitState {
                            currentState.copy(timerPlaybackState = timerPlaybackManager.uiState.value)
                        }
                        fetchTimerLists()
                    },
                    onFailure = {
                        sendErrorEffect()
                    }
                )
            }
        }
    }

    // Handle timer completion
    private fun handleTimerCompletion(timerList: TimerList, timer: Timer) {
        viewModelScope.launch {
            sendEffect {
                TimerEffect.ShowSnackBar("Timer '${timer.name}' completed", SnackbarType.SUCCESS)
            }
        }
    }

    // Helper methods for effects
    private fun sendSuccessEffect() {
        sendEffect {
            TimerEffect.ShowSnackBar("Operation successful", SnackbarType.SUCCESS)
        }
    }

    private fun sendErrorEffect() {
        sendEffect {
            TimerEffect.ShowSnackBar("Operation failed", SnackbarType.ERROR)
        }
    }
}
