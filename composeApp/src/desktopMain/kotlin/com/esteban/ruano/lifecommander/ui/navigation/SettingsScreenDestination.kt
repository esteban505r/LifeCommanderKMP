package com.esteban.ruano.lifecommander.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.esteban.ruano.lifecommander.models.UserSettings
import com.esteban.ruano.lifecommander.ui.components.ErrorScreen
import com.esteban.ruano.lifecommander.ui.components.LoadingScreen
import com.esteban.ruano.lifecommander.ui.screens.SettingsScreen
import com.esteban.ruano.lifecommander.ui.viewmodels.SettingsViewModel
import com.esteban.ruano.lifecommander.ui.viewmodels.TimersViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreenDestination(
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = koinViewModel(),
    timersViewModel: TimersViewModel = koinViewModel(),
    onNavigateToTimers: () -> Unit
) {
    val settings by settingsViewModel.settings.collectAsState()
    val settingsLoading by settingsViewModel.loading.collectAsState()
    val settingsError by settingsViewModel.error.collectAsState()
    
    val timerLists by timersViewModel.timerLists.collectAsState()

    // Load settings when the screen is first displayed
    LaunchedEffect(Unit) {
        settingsViewModel.loadSettings()
    }

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
                onRetry = { settingsViewModel.loadSettings() },
                modifier = modifier
            )
        }
        else -> {
            SettingsScreen(
                userSettings = settings ?: UserSettings(
                    dailyPomodoroGoal = 0,
                    notificationsEnabled = false
                ),
                timerLists = timerLists,
                onUpdateSettings = { updatedSettings ->
                    settingsViewModel.updateSettings(updatedSettings)
                },
                onNavigateToTimers = onNavigateToTimers
            )
        }
    }
} 