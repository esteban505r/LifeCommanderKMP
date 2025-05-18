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
import com.esteban.ruano.lifecommander.websocket.TimerWebSocketClient
import kotlinx.coroutines.launch

@Composable
fun TimersScreen(
    timerLists: List<TimerList>,
    timerPlaybackState: TimerPlaybackState,
    connectionState: TimerWebSocketClient.ConnectionState,
    notifications: List<TimerNotification>,
    onAddTimerList: (String, Boolean, Boolean) -> Unit,
    onUpdateTimerList: (String, String, Boolean, Boolean) -> Unit,
    onDeleteTimerList: (String) -> Unit,
    onAddTimer: (String, String, Int, Boolean, Boolean, Int) -> Unit,
    onUpdateTimer: (String, String, Int, Boolean, Boolean, Int) -> Unit,
    onDeleteTimer: (String) -> Unit,
    onReorderTimers: (String, List<Timer>) -> Unit,
    onNavigateToDetail: (TimerList) -> Unit,
    onReconnectToSocket: () -> Unit,
    onStartTimer: (TimerList) -> Unit,
    onPauseTimer: () -> Unit,
    onResumeTimer: () -> Unit,
    onStopTimer: () -> Unit
) {
    var showAddTimerListDialog by remember { mutableStateOf(false) }
    var showAddTimerDialog by remember { mutableStateOf(false) }
    var selectedTimerList by remember { mutableStateOf<TimerList?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        // Connection Status
        when (connectionState) {
            is TimerWebSocketClient.ConnectionState.Connected -> {
                Text(
                    text = "Connected to server",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.primary
                )
            }
            is TimerWebSocketClient.ConnectionState.Disconnected -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Disconnected from server",
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.error
                    )
                    Button(
                        onClick = { coroutineScope.launch { onReconnectToSocket() } },
                        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
                    ) {
                        Text("Reconnect")
                    }
                }
            }
            is TimerWebSocketClient.ConnectionState.Error -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Connection error",
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.error
                    )
                    Button(
                        onClick = { coroutineScope.launch { onReconnectToSocket() } },
                        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
                    ) {
                        Text("Reconnect")
                    }

                }
            }

            TimerWebSocketClient.ConnectionState.Reconnecting -> {
                Text(
                    text = "Reconnecting...",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.primary
                )
            }
            else -> {
                // Handle other states if necessary
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

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
                onClick = { showAddTimerListDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Timer List")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Timer List")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                    onEdit = { selectedTimerList = timerList },
                    onDelete = { onDeleteTimerList(timerList.id) },
                    onViewDetail = { onNavigateToDetail(timerList) }
                )
            }
        }
    }

    if (showAddTimerListDialog) {
        AddTimerListDialog(
            onDismiss = { showAddTimerListDialog = false },
            onAdd = { name, loopTimers, pomodoroGrouped ->
                onAddTimerList(name, loopTimers, pomodoroGrouped)
                showAddTimerListDialog = false
            }
        )
    }

    if (showAddTimerDialog) {
        // TODO: Implement add timer dialog
    }
}

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
                    if (notifications.isNotEmpty()) {
                        val latestNotification = notifications.last()
                        Text(
                            text = "${latestNotification.type}: ${latestNotification.status}",
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.primary
                        )
                    }
                }
                Row {
                    when (timerPlaybackState.status) {
                        TimerPlaybackStatus.Running ->  {
                            IconButton(onClick = onPause) {
                                Icon(Icons.Default.Pause, contentDescription = "Pause")
                            }
                        }
                        TimerPlaybackStatus.Paused -> {
                            IconButton(onClick = onResume) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Resume")
                            }
                        }
                        else -> {
                            IconButton(onClick = onPlay) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Play")
                            }
                        }
                    }
                    IconButton(onClick = onStop) {
                        Icon(Icons.Default.Stop, contentDescription = "Stop")
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                    IconButton(onClick = onViewDetail) {
                        Icon(Icons.Default.Info, contentDescription = "View Details")
                    }
                }
            }
        }
    }
}

@Composable
private fun TimerItem(
    timer: Timer,
    onUpdate: (Timer) -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = timer.name,
                style = MaterialTheme.typography.subtitle1
            )
            Text(
                text = "${timer.duration} seconds",
                style = MaterialTheme.typography.body2
            )
        }
        Row {
            Switch(
                checked = timer.enabled,
                onCheckedChange = { onUpdate(timer.copy(enabled = it)) }
            )
            Switch(
                checked = timer.countsAsPomodoro,
                onCheckedChange = { onUpdate(timer.copy(countsAsPomodoro = it)) }
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Timer")
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
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = loopTimers,
                        onCheckedChange = { loopTimers = it }
                    )
                    Text("Loop Timers")
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = pomodoroGrouped,
                        onCheckedChange = { pomodoroGrouped = it }
                    )
                    Text("Pomodoro Grouped")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(name, loopTimers, pomodoroGrouped) },
                enabled = name.isNotBlank()
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