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
import com.esteban.ruano.lifecommander.timer.TimerPlaybackManager
import com.esteban.ruano.lifecommander.timer.TimerPlaybackState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import ui.composables.TimersDialog

@Composable
fun TimersScreen(
    timerLists: List<TimerList>,
    timerPlaybackManager: TimerPlaybackManager,
    onAddTimerList: (String, Boolean, Boolean) -> Unit,
    onUpdateTimerList: (String, String, Boolean, Boolean) -> Unit,
    onDeleteTimerList: (String) -> Unit,
    onAddTimer: (String, String, Int, Boolean, Boolean, Int) -> Unit,
    onUpdateTimer: (String, String, Int, Boolean, Boolean, Int) -> Unit,
    onDeleteTimer: (String) -> Unit,
    onReorderTimers: (String, List<Timer>) -> Unit
) {
    var showAddTimerListDialog by remember { mutableStateOf(false) }
    var showAddTimerDialog by remember { mutableStateOf(false) }
    var selectedTimerList by remember { mutableStateOf<TimerList?>(null) }
    var timerPlaybackState by remember { mutableStateOf<TimerPlaybackState>(TimerPlaybackState.Stopped) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        timerPlaybackManager.getTimerFlow().collectLatest { state ->
            timerPlaybackState = state
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
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
                    onPlay = {
                        coroutineScope.launch {
                            timerPlaybackManager.startTimerList(timerList)
                        }
                    },
                    onPause = {
                        coroutineScope.launch {
                            timerPlaybackManager.pauseTimer()
                        }
                    },
                    onResume = {
                        coroutineScope.launch {
                            timerPlaybackManager.resumeTimer()
                        }
                    },
                    onStop = {
                        coroutineScope.launch {
                            timerPlaybackManager.stopTimer()
                        }
                    },
                    onEdit = { selectedTimerList = timerList },
                    onDelete = { onDeleteTimerList(timerList.id) },
                    onAddTimer = { showAddTimerDialog = true },
                    onUpdateTimer = { timer ->
                        onUpdateTimer(
                            timer.id,
                            timer.name,
                            timer.duration,
                            timer.enabled,
                            timer.countsAsPomodoro,
                            timer.order
                        )
                    },
                    onDeleteTimer = { onDeleteTimer(it) },
                    onReorderTimers = { timers ->
                        onReorderTimers(timerList.id, timers)
                    }
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
        TimersDialog(
            show = true,
            onDismiss = { showAddTimerDialog = false },
            appViewModel = koinViewModel (  )
        )
    }
}

@Composable
private fun TimerListCard(
    timerList: TimerList,
    timerPlaybackState: TimerPlaybackState,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddTimer: () -> Unit,
    onUpdateTimer: (Timer) -> Unit,
    onDeleteTimer: (String) -> Unit,
    onReorderTimers: (List<Timer>) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

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
                    text = timerList.name,
                    style = MaterialTheme.typography.h6
                )
                Row {
                    when (timerPlaybackState) {
                        is TimerPlaybackState.Running -> {
                            IconButton(onClick = onPause) {
                                Icon(Icons.Default.Pause, contentDescription = "Pause")
                            }
                        }
                        is TimerPlaybackState.Paused -> {
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
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (expanded) "Collapse" else "Expand"
                        )
                    }
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Loop Timers: ${timerList.loopTimers}",
                        style = MaterialTheme.typography.body2
                    )
                    Text(
                        text = "Pomodoro Grouped: ${timerList.pomodoroGrouped}",
                        style = MaterialTheme.typography.body2
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(timerList.timers.sortedBy { it.order }) { timer ->
                        TimerItem(
                            timer = timer,
                            onUpdate = onUpdateTimer,
                            onDelete = { onDeleteTimer(timer.id) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onAddTimer,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Timer")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Timer")
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