package com.esteban.ruano.timers_presentation.ui.composables

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.esteban.ruano.lifecommander.models.TimerList
import com.esteban.ruano.lifecommander.timer.TimerPlaybackState
import com.esteban.ruano.lifecommander.timer.TimerPlaybackStatus

@Composable
fun TimerControls(
    timerList: TimerList?,
    timerPlaybackState: TimerPlaybackState?,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
    isActiveForThisList: Boolean = false
) {
    val isRunning = timerPlaybackState?.status == TimerPlaybackStatus.Running
    val isPaused = timerPlaybackState?.status == TimerPlaybackStatus.Paused
    val hasActiveTimer = timerPlaybackState?.currentTimer != null
    val isActive = isActiveForThisList && hasActiveTimer

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when {
            isRunning && isActive -> {
                IconButton(onClick = onPause) {
                    Icon(Icons.Default.Pause, contentDescription = "Pause")
                }
                IconButton(onClick = onStop) {
                    Icon(Icons.Default.Stop, contentDescription = "Stop", tint = MaterialTheme.colors.error)
                }
            }
            isPaused && isActive -> {
                IconButton(onClick = onResume) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Resume")
                }
                IconButton(onClick = onStop) {
                    Icon(Icons.Default.Stop, contentDescription = "Stop", tint = MaterialTheme.colors.error)
                }
            }
            else -> {
                IconButton(onClick = onStart) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Start")
                }
            }
        }
    }
}


