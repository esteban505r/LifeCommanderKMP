package com.esteban.ruano.lifecommander.ui.screens

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
import com.esteban.ruano.utils.DateUtils.formatDefault
import kotlinx.coroutines.launch
import ui.composables.TimersDialog
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun TimerListDetailScreen(
    timerList: TimerList,
    timerPlaybackState: TimerPlaybackState,
    listNotifications: List<TimerNotification>,
    onBack: () -> Unit,
    onAddTimer: (String, String, Long, Boolean, Boolean, Int) -> Unit,
    onUpdateTimer: (String, String, Long, Boolean, Boolean, Int) -> Unit,
    onDeleteTimer: (String) -> Unit,
    onReorderTimers: (List<Timer>) -> Unit,
    onGetTimerNotifications: (String) -> List<TimerNotification>,
    onStartTimer: (TimerList) -> Unit,
    onPauseTimer: () -> Unit,
    onResumeTimer: () -> Unit,
    onStopTimer: () -> Unit,
    onUpdateListSettings: (TimerList) -> Unit
) {
    var showAddTimerDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = timerList.name,
                    style = MaterialTheme.typography.h5
                )
                if (listNotifications.isNotEmpty()) {
                    val latestNotification = listNotifications.last()
                    Text(
                        text = "${latestNotification.type}: ${latestNotification.status}",
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.primary
                    )
                }
            }
            Row {
                when (timerPlaybackState.status) {
                    TimerPlaybackStatus.Running -> {
                        IconButton(onClick = onPauseTimer) {
                            Icon(Icons.Default.Pause, contentDescription = "Pause")
                        }
                    }
                    TimerPlaybackStatus.Paused -> {
                        IconButton(onClick = onResumeTimer) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Resume")
                        }
                    }
                    else -> {
                        IconButton(onClick = { onStartTimer(timerList) }) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play")
                        }
                    }
                }
                IconButton(onClick = onStopTimer) {
                    Icon(Icons.Default.Stop, contentDescription = "Stop")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Main Content
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Settings Section
            Card(
                modifier = Modifier
                    .width(300.dp)
                    .fillMaxHeight(),
                elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Loop Timers Setting
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(2f),
                        ) {
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
                            modifier = Modifier.weight(1f),
                            checked = timerList.loopTimers,
                            onCheckedChange = { 
                                onUpdateListSettings(timerList.copy(loopTimers = it))
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Pomodoro Grouped Setting
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(2f),
                        )
                        {
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
                            modifier = Modifier.weight(1f),
                            checked = timerList.pomodoroGrouped,
                            onCheckedChange = { 
                                onUpdateListSettings(timerList.copy(pomodoroGrouped = it))
                            }
                        )
                    }
                }
            }

            // Timers List Section
            Card(
                modifier = Modifier.weight(1f),
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
                            text = "Timers",
                            style = MaterialTheme.typography.h6
                        )
                        Button(
                            onClick = { showAddTimerDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = MaterialTheme.colors.primary
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Timer")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Timer")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(timerList.timers?.sortedBy { it.order } ?: emptyList()) { timer ->
                            TimerItem(
                                timer = timer,
                                timerListCountAsPomodoro = timerList.pomodoroGrouped,
                                notifications = onGetTimerNotifications(timer.id),
                                onUpdate = { updatedTimer ->
                                    onUpdateTimer(
                                        updatedTimer.id,
                                        updatedTimer.name,
                                        updatedTimer.duration,
                                        updatedTimer.enabled,
                                        updatedTimer.countsAsPomodoro,
                                        updatedTimer.order
                                    )
                                },
                                onDelete = { onDeleteTimer(timer.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddTimerDialog) {
        TimersDialog(
            show = true,
            onDismiss = { showAddTimerDialog = false },
            timersList = timerList.timers?.sortedBy { it.order } ?: emptyList(),
            onCreate = { timerId, name, duration, enabled, countsAsPomodoro, order ->
                onAddTimer(
                    timerList.id,
                    name,
                    duration,
                    enabled,
                    countsAsPomodoro,
                    order
                )
            },
            onUpdate = { timerId, name, duration, enabled, countsAsPomodoro, order ->
                onUpdateTimer(
                    timerId,
                    name,
                    duration,
                    enabled,
                    countsAsPomodoro,
                    order
                )
            },
            onDelete = { timerId ->
                onDeleteTimer(timerId)
            },
        )
    }
}

@Composable
private fun TimerItem(
    timer: Timer,
    timerListCountAsPomodoro: Boolean,
    notifications: List<TimerNotification>,
    onUpdate: (Timer) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp
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
                        text = timer.name,
                        style = MaterialTheme.typography.subtitle1
                    )
                    Text(
                        text = timer.duration.milliseconds.formatDefault(),
                        style = MaterialTheme.typography.body2
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Enabled Switch with Tooltip
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Switch(
                            checked = timer.enabled,
                            onCheckedChange = { onUpdate(timer.copy(enabled = it)) }
                        )
                        Text(
                            text = "Enabled",
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    // Pomodoro Switch with Tooltip
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Switch(
                            checked = timer.countsAsPomodoro,
                            enabled = !timerListCountAsPomodoro,
                            onCheckedChange = { onUpdate(timer.copy(countsAsPomodoro = it)) }
                        )
                        Text(
                            text = "Pomodoro",
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Timer")
                    }
                }
            }
        }
    }
} 