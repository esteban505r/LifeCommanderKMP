package com.esteban.ruano.lifecommander.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.esteban.ruano.lifecommander.models.UserSettings
import com.esteban.ruano.lifecommander.ui.components.ErrorScreen
import com.esteban.ruano.lifecommander.ui.components.LoadingScreen
import com.esteban.ruano.lifecommander.ui.screens.SettingsScreen
import com.esteban.ruano.lifecommander.ui.viewmodels.TimersViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreenDestination(
    modifier: Modifier = Modifier,
    timersViewModel: TimersViewModel = koinViewModel(),
    onNavigateToTimers: () -> Unit
) {
    val timerLists by timersViewModel.timerLists.collectAsState()
    val userSettings by timersViewModel.userSettings.collectAsState()
    val settingsLoading by timersViewModel.settingsLoading.collectAsState()
    val settingsError by timersViewModel.settingsError.collectAsState()

    when {
        settingsLoading -> {
            LoadingScreen(
                message = "Loading settings...",
                modifier = modifier
            )
        }
        settingsError != null -> {
            ErrorScreen(
                message = settingsError ?: "Failed to load settings",
                onRetry = { timersViewModel.loadSettings() },
                modifier = modifier
            )
        }
        else -> {
            SettingsScreen(
                userSettings = userSettings ?: UserSettings(
                    dailyPomodoroGoal = 0,
                    notificationsEnabled = false
                ),
                timerLists = timerLists,
                onUpdateSettings = { settings ->
                    timersViewModel.updateUserSettings(
                        defaultTimerListId = settings.defaultTimerListId,
                        dailyPomodoroGoal = settings.dailyPomodoroGoal,
                        notificationsEnabled = settings.notificationsEnabled
                    )
                },
                onNavigateToTimers = onNavigateToTimers
            )
        }
    }
} 