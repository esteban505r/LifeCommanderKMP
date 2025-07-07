package com.esteban.ruano.lifecommander.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    val testNotificationResult by settingsViewModel.testNotificationResult.collectAsState()
    val testDueTasksNotificationResult by settingsViewModel.testDueTasksNotificationResult.collectAsState()
    val testDueHabitsNotificationResult by settingsViewModel.testDueHabitsNotificationResult.collectAsState()
    
    val timerLists by timersViewModel.timerLists.collectAsState()

    // Load settings when the screen is first displayed
    LaunchedEffect(Unit) {
        settingsViewModel.loadSettings()
    }
    
    // Clear test notification results after 5 seconds
    LaunchedEffect(testNotificationResult) {
        if (testNotificationResult != null) {
            kotlinx.coroutines.delay(5000)
            settingsViewModel.clearTestNotificationResult()
        }
    }
    
    LaunchedEffect(testDueTasksNotificationResult) {
        if (testDueTasksNotificationResult != null) {
            kotlinx.coroutines.delay(5000)
            settingsViewModel.clearTestDueTasksNotificationResult()
        }
    }
    
    LaunchedEffect(testDueHabitsNotificationResult) {
        if (testDueHabitsNotificationResult != null) {
            kotlinx.coroutines.delay(5000)
            settingsViewModel.clearTestDueHabitsNotificationResult()
        }
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
                onNavigateToTimers = onNavigateToTimers,
                onTestNotification = {
                    settingsViewModel.testNotification()
                },
                onTestDueTasksNotification = {
                    settingsViewModel.testDueTasksNotification()
                },
                onTestDueHabitsNotification = {
                    settingsViewModel.testDueHabitsNotification()
                }
            )
            
            // Show test notification results
            testNotificationResult?.let { result ->
                androidx.compose.material.Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        androidx.compose.material.TextButton(
                            onClick = { settingsViewModel.clearTestNotificationResult() }
                        ) {
                            androidx.compose.material.Text("Dismiss")
                        }
                    }
                ) {
                    androidx.compose.material.Text(result)
                }
            }
            
            testDueTasksNotificationResult?.let { result ->
                androidx.compose.material.Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        androidx.compose.material.TextButton(
                            onClick = { settingsViewModel.clearTestDueTasksNotificationResult() }
                        ) {
                            androidx.compose.material.Text("Dismiss")
                        }
                    }
                ) {
                    androidx.compose.material.Text(result)
                }
            }
            
            testDueHabitsNotificationResult?.let { result ->
                androidx.compose.material.Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        androidx.compose.material.TextButton(
                            onClick = { settingsViewModel.clearTestDueHabitsNotificationResult() }
                        ) {
                            androidx.compose.material.Text("Dismiss")
                        }
                    }
                ) {
                    androidx.compose.material.Text(result)
                }
            }
        }
    }
} 