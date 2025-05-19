package com.esteban.ruano.lifecommander.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.esteban.ruano.lifecommander.ui.screens.TimersScreen
import com.esteban.ruano.lifecommander.ui.viewmodels.TimersViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TimersScreenDestination(
    modifier: Modifier = Modifier,
    timersViewModel: TimersViewModel,
    onNavigateToDetail: (String) -> Unit
) {
    val timerLists by timersViewModel.timerLists.collectAsState()
    val timerPlaybackState by timersViewModel.timerPlaybackState.collectAsState()
    val connectionState by timersViewModel.connectionState.collectAsState()
    val notifications by timersViewModel.notifications.collectAsState()

    LaunchedEffect(Unit){
        timersViewModel.loadTimerLists()
    }

    TimersScreen(
        timerLists = timerLists,
        timerPlaybackState = timerPlaybackState,
        connectionState = connectionState,
        onReconnectToSocket = {
            timersViewModel.connectWebSocket()
        },
        notifications = notifications,
        onAddTimerList = { name, loopTimers, pomodoroGrouped ->
            timersViewModel.createTimerList(name, loopTimers, pomodoroGrouped)
        },
        onUpdateTimerList = { listId, name, loopTimers, pomodoroGrouped ->
            timersViewModel.updateTimerList(listId, name, loopTimers, pomodoroGrouped)
        },
        onDeleteTimerList = { listId ->
            timersViewModel.deleteTimerList(listId)
        },
        onAddTimer = { listId, name, duration, enabled, countsAsPomodoro, order ->
            timersViewModel.createTimer(listId, name, duration, enabled, countsAsPomodoro, order)
        },
        onUpdateTimer = { timerId, name, duration, enabled, countsAsPomodoro, order ->
            timersViewModel.updateTimer(timerId, name, duration, enabled, countsAsPomodoro, order)
        },
        onDeleteTimer = { timerId ->
            timersViewModel.deleteTimer(timerId)
        },
        onReorderTimers = { listId, timers ->
            // TODO: Implement timer reordering
        },
        onNavigateToDetail = { timerList ->
            onNavigateToDetail(timerList.id)
        },
        onStartTimer = { timerList ->
            timersViewModel.startTimer(timerList)
        },
        onPauseTimer = {
            timersViewModel.pauseTimer()
        },
        onResumeTimer = {
            timersViewModel.resumeTimer()
        },
        onStopTimer = {
            timersViewModel.stopTimer()
        }
    )
} 