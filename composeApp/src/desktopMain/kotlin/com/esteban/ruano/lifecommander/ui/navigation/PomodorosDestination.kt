package com.esteban.ruano.lifecommander.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.esteban.ruano.lifecommander.ui.screens.PomodorosScreen
import com.esteban.ruano.lifecommander.ui.viewmodels.TimersViewModel

@Composable
fun PomodorosScreenDestination(
    modifier: Modifier = Modifier,
    timersViewModel: TimersViewModel,
    onBack: () -> Unit
) {
    val pomodoros by timersViewModel.pomodoros.collectAsState()

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