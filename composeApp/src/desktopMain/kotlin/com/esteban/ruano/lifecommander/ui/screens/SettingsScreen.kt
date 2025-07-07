package com.esteban.ruano.lifecommander.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Repeat
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
    onNavigateToTimers: () -> Unit,
    onTestNotification: () -> Unit = {},
    onTestDueTasksNotification: () -> Unit = {},
    onTestDueHabitsNotification: () -> Unit = {}
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
    
    // Notification frequency settings
    var dueTasksNotificationFrequency by remember { mutableStateOf(userSettings.dueTasksNotificationFrequency.toString()) }
    var dueHabitsNotificationFrequency by remember { mutableStateOf(userSettings.dueHabitsNotificationFrequency.toString()) }
    var showNotificationSettings by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.h4,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Timer Settings Section
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            elevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Timer Settings",
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Daily Pomodoro Goal",
                    style = MaterialTheme.typography.subtitle1
                )
                TextField(
                    value = dailyPomodoroGoal,
                    onValueChange = { dailyPomodoroGoal = it },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    label = { Text("Number of pomodoros") }
                )

                Text(
                    text = "Default Timer List",
                    style = MaterialTheme.typography.subtitle1,
                    modifier = Modifier.padding(top = 16.dp)
                )
                var expanded by remember { mutableStateOf(false) }
                Box {
                    Button(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        Text(selectedTimerListId ?: "Select a timer list")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            onClick = { 
                                selectedTimerListId = null
                                expanded = false
                            }
                        ) {
                            Text("None")
                        }
                        timerLists.forEach { list ->
                            DropdownMenuItem(
                                onClick = { 
                                    selectedTimerListId = list.id
                                    expanded = false
                                }
                            ) {
                                Text(list.name)
                            }
                        }
                    }
                }
            }
        }

        // Notification Settings Section
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
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
                        text = "Notification Settings",
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { showNotificationSettings = !showNotificationSettings }) {
                        Icon(
                            imageVector = if (showNotificationSettings) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (showNotificationSettings) "Collapse" else "Expand"
                        )
                    }
                }

                if (showNotificationSettings) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it }
                        )
                        Text(
                            text = "Enable Notifications",
                            style = MaterialTheme.typography.subtitle1
                        )
                    }

                    if (notificationsEnabled) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Due Tasks Notification Frequency (minutes)",
                            style = MaterialTheme.typography.subtitle1
                        )
                        TextField(
                            value = dueTasksNotificationFrequency,
                            onValueChange = { dueTasksNotificationFrequency = it },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            label = { Text("Minutes") }
                        )

                        Text(
                            text = "Due Habits Notification Frequency (minutes)",
                            style = MaterialTheme.typography.subtitle1,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                        TextField(
                            value = dueHabitsNotificationFrequency,
                            onValueChange = { dueHabitsNotificationFrequency = it },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            label = { Text("Minutes") }
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Test notification buttons in a row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onTestNotification,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = MaterialTheme.colors.secondary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                                Text("Test General")
                            }
                            
                            Button(
                                onClick = onTestDueTasksNotification,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = MaterialTheme.colors.primary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Assignment,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                                Text("Test Tasks")
                            }
                            
                            Button(
                                onClick = onTestDueHabitsNotification,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = MaterialTheme.colors.primary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Repeat,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                                Text("Test Habits")
                            }
                        }
                    }
                }
            }
        }

        // Budget Settings Section
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
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
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { showBudgetSettings = !showBudgetSettings }) {
                        Icon(
                            imageVector = if (showBudgetSettings) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (showBudgetSettings) "Collapse" else "Expand"
                        )
                    }
                }

                if (showBudgetSettings) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Unbudgeted Period Type",
                        style = MaterialTheme.typography.subtitle1
                    )
                    var budgetExpanded by remember { mutableStateOf(false) }
                    Box {
                        Button(
                            onClick = { budgetExpanded = true },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        ) {
                            Text(unbudgetedPeriodType.name)
                        }
                        DropdownMenu(
                            expanded = budgetExpanded,
                            onDismissRequest = { budgetExpanded = false }
                        ) {
                            UnbudgetedPeriodType.values().forEach { type ->
                                DropdownMenuItem(
                                    onClick = { 
                                        unbudgetedPeriodType = type
                                        budgetExpanded = false
                                    }
                                ) {
                                    Text(type.name)
                                }
                            }
                        }
                    }

                    Text(
                        text = "Start Day",
                        style = MaterialTheme.typography.subtitle1,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    TextField(
                        value = unbudgetedPeriodStartDay,
                        onValueChange = { unbudgetedPeriodStartDay = it },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        label = { Text("Day of month") }
                    )

                    Text(
                        text = "End Day",
                        style = MaterialTheme.typography.subtitle1,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    TextField(
                        value = unbudgetedPeriodEndDay,
                        onValueChange = { unbudgetedPeriodEndDay = it },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        label = { Text("Day of month") }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

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
                        unbudgetedPeriodEndDay = unbudgetedPeriodEndDay.toIntOrNull() ?: 31,
                        dueTasksNotificationFrequency = dueTasksNotificationFrequency.toIntOrNull() ?: 30,
                        dueHabitsNotificationFrequency = dueHabitsNotificationFrequency.toIntOrNull() ?: 60
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