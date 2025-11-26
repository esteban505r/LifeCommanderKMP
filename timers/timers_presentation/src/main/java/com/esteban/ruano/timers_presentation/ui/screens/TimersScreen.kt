package com.esteban.ruano.timers_presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.esteban.ruano.lifecommander.models.TimerList
import com.esteban.ruano.lifecommander.timer.TimerConnectionState
import com.esteban.ruano.lifecommander.timer.TimerNotification
import com.esteban.ruano.lifecommander.timer.TimerPlaybackState
import com.esteban.ruano.lifecommander.timer.TimerPlaybackStatus
import com.esteban.ruano.timers_presentation.ui.composables.TimerControls
import com.esteban.ruano.timers_presentation.ui.intent.TimerIntent

@Composable
fun TimersScreen(
    state: com.esteban.ruano.timers_presentation.ui.screens.viewmodel.state.TimerState,
    userIntent: (TimerIntent) -> Unit,
    onNavigateUp: () -> Unit,
    onTimerListClick: (String) -> Unit
) {
    var showAddTimerListDialog by remember { mutableStateOf(false) }
    var showEditTimerListDialog by remember { mutableStateOf<TimerList?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Timers",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = MaterialTheme.colors.onPrimary,
                elevation = 0.dp,
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colors.onPrimary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTimerListDialog = true },
                backgroundColor = MaterialTheme.colors.primary
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Timer List",
                    tint = MaterialTheme.colors.onPrimary
                )
            }
        },
        backgroundColor = MaterialTheme.colors.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Connection Status - Compact version
            ConnectionStatusBar(
                connectionState = state.connectionState,
                onReconnect = { userIntent(TimerIntent.ReconnectWebSocket) }
            )

            // Timer Lists
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colors.primary
                        )
                        Text(
                            "Loading timers...",
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            } else if (state.timerLists.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Timer,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colors.onSurface.copy(alpha = 0.3f)
                        )
                        Text(
                            "No timer lists",
                            style = MaterialTheme.typography.h6,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Add your first timer list to get started!",
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.timerLists) { timerList ->
                        TimerListCard(
                            timerList = timerList,
                            timerPlaybackState = state.timerPlaybackState,
                            notifications = state.notifications.filter { it.listId == timerList.id },
                            onPlay = { userIntent(TimerIntent.StartTimer(timerList)) },
                            onPause = { userIntent(TimerIntent.PauseTimer) },
                            onResume = { userIntent(TimerIntent.ResumeTimer) },
                            onStop = { userIntent(TimerIntent.StopTimer) },
                            onEdit = { showEditTimerListDialog = timerList },
                            onDelete = { userIntent(TimerIntent.DeleteTimerList(timerList.id)) },
                            onViewDetail = { onTimerListClick(timerList.id) }
                        )
                    }
                }
            }
        }
    }

    // Add Timer List Dialog
    if (showAddTimerListDialog) {
        AddTimerListDialog(
            onDismiss = { showAddTimerListDialog = false },
            onAdd = { name, loopTimers, pomodoroGrouped ->
                userIntent(TimerIntent.CreateTimerList(name, loopTimers, pomodoroGrouped))
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
                userIntent(TimerIntent.UpdateTimerList(timerList.id, name, loopTimers, pomodoroGrouped))
                showEditTimerListDialog = null
            }
        )
    }
}

@Composable
private fun ConnectionStatusBar(
    connectionState: TimerConnectionState,
    onReconnect: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(8.dp)),
        elevation = 2.dp,
        color = when (connectionState) {
            is TimerConnectionState.Connected -> MaterialTheme.colors.primary.copy(alpha = 0.1f)
            is TimerConnectionState.Disconnected,
            is TimerConnectionState.Error -> MaterialTheme.colors.error.copy(alpha = 0.1f)
            TimerConnectionState.Reconnecting -> MaterialTheme.colors.primary.copy(alpha = 0.1f)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = when (connectionState) {
                        is TimerConnectionState.Connected -> Icons.Default.CloudDone
                        is TimerConnectionState.Disconnected -> Icons.Default.CloudOff
                        is TimerConnectionState.Error -> Icons.Default.Error
                        TimerConnectionState.Reconnecting -> Icons.Default.CloudSync
                    },
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = when (connectionState) {
                        is TimerConnectionState.Connected -> MaterialTheme.colors.primary
                        is TimerConnectionState.Disconnected,
                        is TimerConnectionState.Error -> MaterialTheme.colors.error
                        TimerConnectionState.Reconnecting -> MaterialTheme.colors.primary
                    }
                )
                Text(
                    text = when (connectionState) {
                        is TimerConnectionState.Connected -> "Connected"
                        is TimerConnectionState.Disconnected -> "Disconnected"
                        is TimerConnectionState.Error -> "Connection error"
                        TimerConnectionState.Reconnecting -> "Reconnecting..."
                    },
                    style = MaterialTheme.typography.caption,
                    fontWeight = FontWeight.Medium
                )
            }
            if (connectionState !is TimerConnectionState.Connected) {
                TextButton(
                    onClick = onReconnect,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        "Reconnect",
                        style = MaterialTheme.typography.caption
                    )
                }
            }
        }
    }
}

@Composable
private fun TimerListCard(
    timerList: TimerList,
    timerPlaybackState: TimerPlaybackState?,
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
    val isActive = timerPlaybackState?.timerList?.id == timerList.id
    val isRunning = timerPlaybackState?.status == TimerPlaybackStatus.Running
    val isPaused = timerPlaybackState?.status == TimerPlaybackStatus.Paused

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = if (isActive) 8.dp else 4.dp,
        backgroundColor = if (isActive) {
            when {
                isRunning -> MaterialTheme.colors.primary.copy(alpha = 0.05f)
                isPaused -> MaterialTheme.colors.secondary.copy(alpha = 0.05f)
                else -> MaterialTheme.colors.surface
            }
        } else {
            MaterialTheme.colors.surface
        }
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = timerList.name,
                            style = MaterialTheme.typography.h6,
                            fontWeight = if (isActive) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                        )
                        if (isActive) {
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = when {
                                    isRunning -> MaterialTheme.colors.primary.copy(alpha = 0.2f)
                                    isPaused -> MaterialTheme.colors.secondary.copy(alpha = 0.2f)
                                    else -> MaterialTheme.colors.surface
                                }
                            ) {
                                Text(
                                    text = if (isRunning) "● Running" else "⏸ Paused",
                                    style = MaterialTheme.typography.caption,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = when {
                                        isRunning -> MaterialTheme.colors.primary
                                        isPaused -> MaterialTheme.colors.secondary
                                        else -> MaterialTheme.colors.onSurface
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
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
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colors.primary.copy(alpha = 0.1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Repeat,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colors.primary
                                    )
                                    Text("Loop", style = MaterialTheme.typography.caption, color = MaterialTheme.colors.primary)
                                }
                            }
                        }
                        if (timerList.pomodoroGrouped) {
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colors.primary.copy(alpha = 0.1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Timer,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colors.primary
                                    )
                                    Text("Pomodoro", style = MaterialTheme.typography.caption, color = MaterialTheme.colors.primary)
                                }
                            }
                        }
                    }
                    if (notifications.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        val latestNotification = notifications.last()
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colors.primary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "${latestNotification.type}: ${latestNotification.status}",
                                style = MaterialTheme.typography.caption,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = MaterialTheme.colors.primary
                            )
                        }
                    }
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TimerControls(
                        timerList = timerList,
                        timerPlaybackState = timerPlaybackState,
                        onStart = onPlay,
                        onPause = onPause,
                        onResume = onResume,
                        onStop = onStop,
                        isActiveForThisList = isActive
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colors.primary
                            )
                        }
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colors.error
                            )
                        }
                        IconButton(
                            onClick = onViewDetail,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.ArrowForward,
                                contentDescription = "View Details",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colors.primary
                            )
                        }
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
                    Text("Loop Timers")
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
                    Text("Pomodoro Grouped")
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
                    Text("Loop Timers")
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
                    Text("Pomodoro Grouped")
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
                enabled = name.isNotBlank()
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

