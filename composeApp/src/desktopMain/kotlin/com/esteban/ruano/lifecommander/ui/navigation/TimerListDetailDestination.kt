package com.esteban.ruano.lifecommander.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.esteban.ruano.lifecommander.timer.TimerPlaybackManager
import com.esteban.ruano.lifecommander.ui.screens.TimerListDetailScreen
import com.esteban.ruano.lifecommander.ui.viewmodels.TimersViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TimerListDetailDestination(
    modifier: Modifier = Modifier,
    timerListId: String,
    timersViewModel: TimersViewModel = koinViewModel(),
    timerPlaybackManager: TimerPlaybackManager,
    onBack: () -> Unit
) {
    val timerLists by timersViewModel.timerLists.collectAsState()
    val timerList = timerLists.find { it.id == timerListId }

    LaunchedEffect(Unit) {
        timersViewModel.loadTimerLists()
    }

    timerList?.let { list ->
        TimerListDetailScreen(
            timerList = list,
            timerPlaybackManager = timerPlaybackManager,
            onBack = onBack,
            onAddTimer = { listId, name, duration, enabled, countsAsPomodoro, order ->
                timersViewModel.createTimer(listId, name, duration, enabled, countsAsPomodoro, order)
            },
            onUpdateTimer = { timerId, name, duration, enabled, countsAsPomodoro, order ->
                timersViewModel.updateTimer(timerId, name, duration, enabled, countsAsPomodoro, order)
            },
            onDeleteTimer = { timerId ->
                timersViewModel.deleteTimer(timerId)
            },
            onReorderTimers = { timers ->
                // TODO: Implement timer reordering
            }
        )
    }
} 