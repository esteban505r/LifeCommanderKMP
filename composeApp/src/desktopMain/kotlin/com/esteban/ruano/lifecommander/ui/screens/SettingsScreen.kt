package com.esteban.ruano.lifecommander.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.esteban.ruano.lifecommander.models.TimerList
import com.esteban.ruano.lifecommander.models.UserSettings
import com.esteban.ruano.lifecommander.models.settings.UnbudgetedPeriodType
import kotlinx.coroutines.delay

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
    
    // Budget settings state
    var unbudgetedPeriodType by remember { mutableStateOf(userSettings.unbudgetedPeriodType) }
    var unbudgetedPeriodStartDay by remember { mutableStateOf(userSettings.unbudgetedPeriodStartDay.toString()) }
    var unbudgetedPeriodEndDay by remember { mutableStateOf(userSettings.unbudgetedPeriodEndDay.toString()) }
    var showBudgetSettings by remember { mutableStateOf(false) }
    var showSaveSuccess by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.h5,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Timer Settings Card
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
                                id = userSettings.id,
                                defaultTimerListId = selectedTimerListId,
                                dailyPomodoroGoal = dailyPomodoroGoal.toIntOrNull() ?: 8,
                                notificationsEnabled = notificationsEnabled,
                                unbudgetedPeriodType = unbudgetedPeriodType,
                                unbudgetedPeriodStartDay = unbudgetedPeriodStartDay.toIntOrNull() ?: 1,
                                unbudgetedPeriodEndDay = unbudgetedPeriodEndDay.toIntOrNull() ?: 31
                            )
                        )
                        showSaveSuccess = true
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Save Settings")
                }
                
                if (showSaveSuccess) {
                    LaunchedEffect(showSaveSuccess) {
                        delay(3000)
                        showSaveSuccess = false
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Settings saved successfully!",
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.primary,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Budget Settings Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Budget Settings",
                        style = MaterialTheme.typography.h6
                    )
                    IconButton(
                        onClick = { showBudgetSettings = !showBudgetSettings }
                    ) {
                        Icon(
                            imageVector = if (showBudgetSettings) 
                                androidx.compose.material.icons.Icons.Default.ExpandLess 
                            else 
                                androidx.compose.material.icons.Icons.Default.ExpandMore,
                            contentDescription = if (showBudgetSettings) "Hide" else "Show"
                        )
                    }
                }

                if (showBudgetSettings) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Unbudgeted Transaction Period",
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Configure how unbudgeted transactions are grouped for tracking and analysis.",
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Period Type Selection
                    Text(
                        text = "Period Type",
                        style = MaterialTheme.typography.subtitle2,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Column {
                        UnbudgetedPeriodType.values().forEach { periodType ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                RadioButton(
                                    selected = unbudgetedPeriodType == periodType,
                                    onClick = { 
                                        unbudgetedPeriodType = periodType
                                        // When switching to monthly, set endDay to 31 (will be ignored by backend)
                                        if (periodType == UnbudgetedPeriodType.MONTHLY) {
                                            unbudgetedPeriodEndDay = "31"
                                        }
                                    }
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = when (periodType) {
                                            UnbudgetedPeriodType.MONTHLY -> "Monthly"
                                            UnbudgetedPeriodType.WEEKLY -> "Weekly"
                                            UnbudgetedPeriodType.CUSTOM -> "Custom"
                                        },
                                        style = MaterialTheme.typography.body1
                                    )
                                    Text(
                                        text = when (periodType) {
                                            UnbudgetedPeriodType.MONTHLY -> "Standard monthly periods (1st to last day)"
                                            UnbudgetedPeriodType.WEEKLY -> "Weekly periods (Monday to Sunday)"
                                            UnbudgetedPeriodType.CUSTOM -> "Custom start and end days"
                                        },
                                        style = MaterialTheme.typography.caption,
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Start and End Day Inputs
                    when (unbudgetedPeriodType) {
                        UnbudgetedPeriodType.MONTHLY -> {
                            // For monthly, only show start day
                            OutlinedTextField(
                                value = unbudgetedPeriodStartDay,
                                onValueChange = { 
                                    val value = it.filter { char -> char.isDigit() }
                                    if (value.isNotEmpty()) {
                                        val day = value.toIntOrNull() ?: 1
                                        unbudgetedPeriodStartDay = day.coerceIn(1, 31).toString()
                                    } else {
                                        unbudgetedPeriodStartDay = value
                                    }
                                },
                                label = { Text("Start Day of Month") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                ),
                                singleLine = true
                            )
                        }
                        UnbudgetedPeriodType.WEEKLY, UnbudgetedPeriodType.CUSTOM -> {
                            // For weekly and custom, show both start and end day
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                OutlinedTextField(
                                    value = unbudgetedPeriodStartDay,
                                    onValueChange = { 
                                        val value = it.filter { char -> char.isDigit() }
                                        if (value.isNotEmpty()) {
                                            val day = value.toIntOrNull() ?: 1
                                            unbudgetedPeriodStartDay = day.coerceIn(1, 31).toString()
                                        } else {
                                            unbudgetedPeriodStartDay = value
                                        }
                                    },
                                    label = { Text("Start Day") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                    ),
                                    singleLine = true
                                )
                                
                                OutlinedTextField(
                                    value = unbudgetedPeriodEndDay,
                                    onValueChange = { 
                                        val value = it.filter { char -> char.isDigit() }
                                        if (value.isNotEmpty()) {
                                            val day = value.toIntOrNull() ?: 31
                                            unbudgetedPeriodEndDay = day.coerceIn(1, 31).toString()
                                        } else {
                                            unbudgetedPeriodEndDay = value
                                        }
                                    },
                                    label = { Text("End Day") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                    ),
                                    singleLine = true
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = when (unbudgetedPeriodType) {
                            UnbudgetedPeriodType.MONTHLY -> "Monthly periods starting from day ${unbudgetedPeriodStartDay} of each month"
                            UnbudgetedPeriodType.WEEKLY -> "Weekly periods from day ${unbudgetedPeriodStartDay} to ${unbudgetedPeriodEndDay} of each week"
                            UnbudgetedPeriodType.CUSTOM -> "Custom periods from day ${unbudgetedPeriodStartDay} to ${unbudgetedPeriodEndDay}"
                        },
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.primary
                    )
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