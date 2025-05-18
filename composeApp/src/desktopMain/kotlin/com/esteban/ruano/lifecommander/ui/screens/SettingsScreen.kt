package com.esteban.ruano.lifecommander.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.esteban.ruano.lifecommander.models.TimerList
import com.esteban.ruano.lifecommander.models.UserSettings

@Composable
fun SettingsScreen(
    userSettings: UserSettings,
    timerLists: List<TimerList>,
    onUpdateSettings: (UserSettings) -> Unit,
    onNavigateToTimers: () -> Unit
) {
    var dailyPomodoroGoal by remember { mutableStateOf(userSettings.dailyPomodoroGoal.toString()) }
    var notificationsEnabled by remember { mutableStateOf(userSettings.notificationsEnabled) }
    var selectedTimerListId by remember { mutableStateOf(userSettings.defaultTimerListId) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.h5,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Timer Settings",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = dailyPomodoroGoal,
                    onValueChange = { dailyPomodoroGoal = it },
                    label = { Text("Daily Pomodoro Goal") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it }
                    )
                    Text("Enable Notifications")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("Default Timer List", style = MaterialTheme.typography.subtitle1)
                DropdownMenu(
                    expanded = false,
                    onDismissRequest = { },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    timerLists.forEach { list ->
                        DropdownMenuItem(
                            onClick = { selectedTimerListId = list.id }
                        ) {
                            Text(list.name)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        onUpdateSettings(
                            UserSettings(
                                defaultTimerListId = selectedTimerListId,
                                dailyPomodoroGoal = dailyPomodoroGoal.toIntOrNull() ?: 8,
                                notificationsEnabled = notificationsEnabled
                            )
                        )
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Save Settings")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onNavigateToTimers,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Manage Timer Lists")
        }
    }
} 