package com.esteban.ruano.lifecommander.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.esteban.ruano.lifecommander.ui.screens.TimerListDetailScreen
import com.esteban.ruano.lifecommander.ui.viewmodels.TimersViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TimerListDetailDestination(
    modifier: Modifier = Modifier,
    timerListId: String,
    timersViewModel: TimersViewModel = koinViewModel(),
    onBack: () -> Unit
) {
    val timerLists by timersViewModel.timerLists.collectAsState()
    val timerList = timerLists.find { it.id == timerListId }
    val timerPlaybackState by timersViewModel.timerPlaybackState.collectAsState()
    val listNotifications by timersViewModel.listNotifications.collectAsState()

    LaunchedEffect(Unit) {
        timersViewModel.loadTimerListByID(timerListId)
        timersViewModel.updateListNotifications(timerListId)
    }

    timerList?.let { list ->
        TimerListDetailScreen(
            timerList = list,
            timerPlaybackState = timerPlaybackState,
            listNotifications = listNotifications,
            onBack = onBack,
            onAddTimer = { listId, name, duration, enabled, countsAsPomodoro, order ->
                timersViewModel.createTimer(listId, name, duration, enabled, countsAsPomodoro, order, onSuccess = {
                    timersViewModel.loadTimerListByID(listId)
                })
            },
            onUpdateTimer = { timerId, name, duration, enabled, countsAsPomodoro, order ->
                timersViewModel.updateTimer(timerId, name, duration, enabled, countsAsPomodoro, order, onSuccess = {
                    timersViewModel.loadTimerListByID(timerListId)
                })
            },
            onDeleteTimer = { timerId ->
                timersViewModel.deleteTimer(timerId, onSuccess = {
                    timersViewModel.loadTimerListByID(timerListId)
                })
            },
            onReorderTimers = { timers ->
                // TODO: Implement timer reordering
            },
            onGetTimerNotifications = { timerId ->
                timersViewModel.getTimerNotifications(timerId)
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
}