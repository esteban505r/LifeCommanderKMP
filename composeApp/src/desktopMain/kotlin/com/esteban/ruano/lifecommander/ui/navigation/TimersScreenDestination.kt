package com.esteban.ruano.lifecommander.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.esteban.ruano.lifecommander.models.UserSettings
import com.esteban.ruano.lifecommander.timer.TimerPlaybackManager
import com.esteban.ruano.lifecommander.ui.screens.TimersScreen
import com.esteban.ruano.lifecommander.ui.viewmodels.TimersViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TimersScreenDestination(
    modifier: Modifier = Modifier,
    timersViewModel: TimersViewModel = koinViewModel(),
    timerPlaybackManager: TimerPlaybackManager,
    onNavigateToDetails: (String) -> Unit,
) {
    val timerLists by timersViewModel.timerLists.collectAsState()
    val userSettings by timersViewModel.userSettings.collectAsState()

    LaunchedEffect(Unit) {
        timersViewModel.loadTimerLists()
    }

    TimersScreen(
        timerLists = timerLists,
        timerPlaybackManager = timerPlaybackManager,

        onUpdateTimerList = { listId, name, loopTimers, pomodoroGrouped ->
            timersViewModel.updateTimerList(listId, name, loopTimers, pomodoroGrouped)
        },
        onDeleteTimerList = { listId ->
            timersViewModel.deleteTimerList(listId)
        },

        onUpdateTimer = { timerId, name, duration, enabled, countsAsPomodoro, order ->
            timersViewModel.updateTimer(timerId, name, duration, enabled, countsAsPomodoro, order)
        },
        onAddTimer = {
            listId, name, duration, enabled, countsAsPomodoro, order ->
            timersViewModel.createTimer(listId, name, duration, enabled, countsAsPomodoro, order)
        },
        onDeleteTimer = { timerId ->
            timersViewModel.deleteTimer(timerId)
        },
        onReorderTimers = {
            listId, fromIndex->

        },
        onAddTimerList = {
            name, loopTimers, pomodoroGrouped ->
            timersViewModel.createTimerList(name, loopTimers, pomodoroGrouped)
        },
        onNavigateToDetail = {
            onNavigateToDetails(it.id)
        }
        )
} 