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
import com.esteban.ruano.lifecommander.timer.TimerPlaybackManager
import com.esteban.ruano.lifecommander.timer.TimerPlaybackState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import ui.composables.TimersDialog

@Composable
fun TimerListDetailScreen(
    timerList: TimerList,
    timerPlaybackManager: TimerPlaybackManager,
    onBack: () -> Unit,
    onAddTimer: (String, String, Int, Boolean, Boolean, Int) -> Unit,
    onUpdateTimer: (String, String, Int, Boolean, Boolean, Int) -> Unit,
    onDeleteTimer: (String) -> Unit,
    onReorderTimers: (List<Timer>) -> Unit
) {
    var showAddTimerDialog by remember { mutableStateOf(false) }
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
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = timerList.name,
                style = MaterialTheme.typography.h5
            )
            Row {
                when (timerPlaybackState) {
                    is TimerPlaybackState.Running -> {
                        IconButton(onClick = {
                            coroutineScope.launch {
                                timerPlaybackManager.pauseTimer()
                            }
                        }) {
                            Icon(Icons.Default.Pause, contentDescription = "Pause")
                        }
                    }
                    is TimerPlaybackState.Paused -> {
                        IconButton(onClick = {
                            coroutineScope.launch {
                                timerPlaybackManager.resumeTimer()
                            }
                        }) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Resume")
                        }
                    }
                    else -> {
                        IconButton(onClick = {
                            coroutineScope.launch {
                                timerPlaybackManager.startTimerList(timerList)
                            }
                        }) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play")
                        }
                    }
                }
                IconButton(onClick = {
                    coroutineScope.launch {
                        timerPlaybackManager.stopTimer()
                    }
                }) {
                    Icon(Icons.Default.Stop, contentDescription = "Stop")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Timer List Properties
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Loop Timers",
                        style = MaterialTheme.typography.subtitle1
                    )
                    Switch(
                        checked = timerList.loopTimers,
                        onCheckedChange = { /* TODO: Implement loop timers toggle */ }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Pomodoro Grouped",
                        style = MaterialTheme.typography.subtitle1
                    )
                    Switch(
                        checked = timerList.pomodoroGrouped,
                        onCheckedChange = { /* TODO: Implement pomodoro grouped toggle */ }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Timers List
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Timers",
                style = MaterialTheme.typography.h6
            )
            Button(onClick = { showAddTimerDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Timer")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Timer")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(timerList.timers.sortedBy { it.order }) { timer ->
                TimerItem(
                    timer = timer,
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

    if (showAddTimerDialog) {
        TimersDialog(
            show = true,
            onDismiss = { showAddTimerDialog = false },
            appViewModel = koinViewModel()
        )
    }
}

@Composable
private fun TimerItem(
    timer: Timer,
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
    }
} 