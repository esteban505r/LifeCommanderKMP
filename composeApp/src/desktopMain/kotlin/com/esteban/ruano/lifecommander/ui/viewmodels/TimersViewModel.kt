package com.esteban.ruano.lifecommander.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esteban.ruano.lifecommander.models.TimerList
import com.esteban.ruano.lifecommander.models.UserSettings
import com.esteban.ruano.lifecommander.services.timers.TimerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import services.auth.TokenStorage

class TimersViewModel(
    private val tokenStorage: TokenStorage,
    private val timerService: TimerService
) : ViewModel() {
    private val _timerLists = MutableStateFlow<List<TimerList>>(emptyList())
    val timerLists: StateFlow<List<TimerList>> = _timerLists.asStateFlow()

    private val _userSettings = MutableStateFlow<UserSettings?>(null)
    val userSettings: StateFlow<UserSettings?> = _userSettings.asStateFlow()

    var isLoading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    fun createTimerList(name: String, loopTimers: Boolean, pomodoroGrouped: Boolean) {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = timerService.createTimerList(
                    token = tokenStorage.getToken() ?: "",
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
                    token = tokenStorage.getToken() ?: "",
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
                    token = tokenStorage.getToken() ?: "",
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
        order: Int
    ) {
        viewModelScope.launch {
            isLoading = true
            try {
                timerService.createTimer(
                    token = tokenStorage.getToken() ?: "",
                    listId = listId,
                    name = name,
                    duration = duration,
                    enabled = enabled,
                    countsAsPomodoro = countsAsPomodoro,
                    order = order
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

    fun updateTimer(
        timerId: String,
        name: String,
        duration: Int,
        enabled: Boolean,
        countsAsPomodoro: Boolean,
        order: Int
    ) {
        viewModelScope.launch {
            isLoading = true
            try {
                timerService.updateTimer(
                    token = tokenStorage.getToken() ?: "",
                    timerId = timerId,
                    name = name,
                    duration = duration,
                    enabled = enabled,
                    countsAsPomodoro = countsAsPomodoro,
                    order = order
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

    fun deleteTimer(timerId: String) {
        viewModelScope.launch {
            isLoading = true
            try {
                timerService.deleteTimer(
                    token = tokenStorage.getToken() ?: "",
                    timerId = timerId
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

    fun updateUserSettings(
        defaultTimerListId: String?,
        dailyPomodoroGoal: Int,
        notificationsEnabled: Boolean
    ) {
        viewModelScope.launch {
            isLoading = true
            try {
                timerService.updateUserSettings(
                    token = tokenStorage.getToken() ?: "",
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

    fun loadTimerLists() {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = timerService.getTimerLists(
                    token = tokenStorage.getToken() ?: ""
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
                    token = tokenStorage.getToken() ?: ""
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
} 