package com.esteban.ruano.lifecommander.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.esteban.ruano.lifecommander.models.Timer
import com.esteban.ruano.lifecommander.models.TimerList
import com.esteban.ruano.lifecommander.timer.TimerNotification
import com.esteban.ruano.lifecommander.timer.TimerPlaybackState
import com.esteban.ruano.lifecommander.timer.TimerPlaybackStatus
import com.esteban.ruano.lifecommander.ui.components.TimerControls

@Composable
fun TimersScreen(
    timerLists: List<TimerList>,
    timerPlaybackState: TimerPlaybackState,
    notifications: List<TimerNotification>,
    onAddTimerList: (String, Boolean, Boolean) -> Unit,
    onUpdateTimerList: (String, String, Boolean, Boolean) -> Unit,
    onDeleteTimerList: (String) -> Unit,
    onAddTimer: (String, String, Long, Boolean, Boolean, Int,Boolean) -> Unit,
    onUpdateTimer: (String, String, Long, Boolean, Boolean, Int,Boolean) -> Unit,
    onDeleteTimer: (String) -> Unit,
    onReorderTimers: (String, List<Timer>) -> Unit,
    onNavigateToDetail: (TimerList) -> Unit,
    onStartTimer: (TimerList) -> Unit,
    onPauseTimer: () -> Unit,
    onResumeTimer: () -> Unit,
    onStopTimer: () -> Unit
) {
    var showAddTimerListDialog by remember { mutableStateOf(false) }
    var showEditTimerListDialog by remember { mutableStateOf<TimerList?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Timer Lists",
                style = MaterialTheme.typography.h5
            )
            Button(
                onClick = { showAddTimerListDialog = true },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.primary
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Timer List")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Timer List")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Timer Lists Grid
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(timerLists) { timerList ->
                TimerListCard(
                    timerList = timerList,
                    timerPlaybackState = timerPlaybackState,
                    notifications = notifications.filter { it.listId == timerList.id },
                    onPlay = { onStartTimer(timerList) },
                    onPause = onPauseTimer,
                    onResume = onResumeTimer,
                    onStop = onStopTimer,
                    onEdit = { showEditTimerListDialog = timerList },
                    onDelete = { onDeleteTimerList(timerList.id) },
                    onViewDetail = { onNavigateToDetail(timerList) }
                )
            }
        }
    }

    // Add Timer List Dialog
    if (showAddTimerListDialog) {
        AddTimerListDialog(
            onDismiss = { showAddTimerListDialog = false },
            onAdd = { name, loopTimers, pomodoroGrouped ->
                onAddTimerList(name, loopTimers, pomodoroGrouped)
                showAddTimerListDialog = false
            }
        )
    }

    // Edit Timer List Dialog
    showEditTimerListDialog?.let { timerList ->
        EditTimerListDialog(
            timerList = timerList,
            onDismiss = { showEditTimerListDialog = null },
            onUpdate = { name, loopTimers, pomodoroGrouped ->
                onUpdateTimerList(timerList.id, name, loopTimers, pomodoroGrouped)
                showEditTimerListDialog = null
            }
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun TimerListCard(
    timerList: TimerList,
    timerPlaybackState: TimerPlaybackState,
    notifications: List<TimerNotification>,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onViewDetail: () -> Unit
) {
    val timerCount = timerList.timers?.size ?: 0
    val enabledTimerCount = timerList.timers?.count { it.enabled } ?: 0
    
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = timerList.name,
                        style = MaterialTheme.typography.h6
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$enabledTimerCount of $timerCount timers enabled",
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (timerList.loopTimers) {
                            Chip(
                                onClick = { },
                                colors = ChipDefaults.chipColors(
                                    backgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.1f)
                                )
                            ) {
                                Icon(
                                    Icons.Default.Repeat,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Loop", style = MaterialTheme.typography.caption)
                            }
                        }
                        if (timerList.pomodoroGrouped) {
                            Chip(
                                onClick = { },
                                colors = ChipDefaults.chipColors(
                                    backgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.1f)
                                )
                            ) {
                                Icon(
                                    Icons.Default.Timer,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Pomodoro", style = MaterialTheme.typography.caption)
                            }
                        }
                    }
                    if (notifications.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        val latestNotification = notifications.last()
                        Text(
                            text = "${latestNotification.type}: ${latestNotification.status}",
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.primary
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Timer Controls
                    TimerControls(
                        timerList = timerList,
                        timerPlaybackState = timerPlaybackState,
                        onStart = onPlay,
                        onPause = onPause,
                        onResume = onResume,
                        onStop = onStop,
                        isActiveForThisList = timerPlaybackState.timerList?.id == timerList.id
                    )
                    
                    // Action Buttons
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.background(
                            color = MaterialTheme.colors.primary.copy(alpha = 0.1f),
                            shape = MaterialTheme.shapes.small
                        ),
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.background(
                            color = MaterialTheme.colors.error.copy(alpha = 0.1f),
                            shape = MaterialTheme.shapes.small
                        ),
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colors.error)
                    }
                    IconButton(
                        onClick = onViewDetail,
                        modifier = Modifier.background(
                            color = MaterialTheme.colors.primary.copy(alpha = 0.1f),
                            shape = MaterialTheme.shapes.small
                        ),
                    ) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "View Details")
                    }
                }
            }
        }
    }
}

@Composable
private fun AddTimerListDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Boolean, Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var loopTimers by remember { mutableStateOf(false) }
    var pomodoroGrouped by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Timer List") },
        text = {
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Loop Timers",
                            style = MaterialTheme.typography.subtitle1
                        )
                        Text(
                            text = "Repeat the entire timer list when finished",
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Switch(
                        checked = loopTimers,
                        onCheckedChange = { loopTimers = it }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Pomodoro Grouped",
                            style = MaterialTheme.typography.subtitle1
                        )
                        Text(
                            text = "Group pomodoro timers together",
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Switch(
                        checked = pomodoroGrouped,
                        onCheckedChange = { pomodoroGrouped = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(name, loopTimers, pomodoroGrouped) },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.primary
                )
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun EditTimerListDialog(
    timerList: TimerList,
    onDismiss: () -> Unit,
    onUpdate: (String, Boolean, Boolean) -> Unit
) {
    var name by remember { mutableStateOf(timerList.name) }
    var loopTimers by remember { mutableStateOf(timerList.loopTimers) }
    var pomodoroGrouped by remember { mutableStateOf(timerList.pomodoroGrouped) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Timer List") },
        text = {
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Loop Timers",
                            style = MaterialTheme.typography.subtitle1
                        )
                        Text(
                            text = "Repeat the entire timer list when finished",
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Switch(
                        checked = loopTimers,
                        onCheckedChange = { loopTimers = it }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Pomodoro Grouped",
                            style = MaterialTheme.typography.subtitle1
                        )
                        Text(
                            text = "Group pomodoro timers together",
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Switch(
                        checked = pomodoroGrouped,
                        onCheckedChange = { pomodoroGrouped = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onUpdate(name, loopTimers, pomodoroGrouped) },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.primary
                )
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 