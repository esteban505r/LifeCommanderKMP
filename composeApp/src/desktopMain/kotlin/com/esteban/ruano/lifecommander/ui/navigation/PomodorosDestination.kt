package com.esteban.ruano.lifecommander.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.esteban.ruano.lifecommander.ui.components.ErrorScreen
import com.esteban.ruano.lifecommander.ui.components.LoadingScreen
import com.esteban.ruano.lifecommander.ui.screens.PomodorosScreen
import com.esteban.ruano.lifecommander.ui.viewmodels.TimersViewModel

@Composable
fun PomodorosScreenDestination(
    modifier: Modifier = Modifier,
    timersViewModel: TimersViewModel,
    onBack: () -> Unit
) {
    val pomodoros by timersViewModel.pomodoros.collectAsState()
    val pomodorosLoading by timersViewModel.pomodorosLoading.collectAsState()
    val pomodorosError by timersViewModel.pomodorosError.collectAsState()

    when {
        pomodorosLoading -> {
            LoadingScreen(
                message = "Loading pomodoros...",
                modifier = modifier
            )
        }
        pomodorosError != null -> {
            ErrorScreen(
                message = pomodorosError ?: "Failed to load pomodoros",
                onRetry = { timersViewModel.loadPomodoros() },
                modifier = modifier
            )
        }
        else -> {
    PomodorosScreen(
        pomodoros = pomodoros,
        onBack = onBack,
        onRemovePomodoro = { pomodoroId ->
            timersViewModel.removePomodoro(pomodoroId)
        },
        onAddSamplePomodoro = {
            timersViewModel.addSamplePomodoro()
        },
        modifier = modifier
    )
        }
    }
} 