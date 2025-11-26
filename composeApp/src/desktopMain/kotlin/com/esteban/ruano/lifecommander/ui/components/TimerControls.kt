package com.esteban.ruano.lifecommander.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.esteban.ruano.lifecommander.models.TimerList
import com.esteban.ruano.lifecommander.timer.TimerPlaybackState
import com.esteban.ruano.lifecommander.timer.TimerPlaybackStatus
import com.esteban.ruano.utils.DateUtils.formatDefault
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun TimerControls(
    timerList: TimerList?,
    timerPlaybackState: TimerPlaybackState,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
    isActiveForThisList: Boolean = false
) {
    val isRunning = timerPlaybackState.status == TimerPlaybackStatus.Running
    val isPaused = timerPlaybackState.status == TimerPlaybackStatus.Paused
    val hasActiveTimer = timerPlaybackState.currentTimer != null
    val isActive = isActiveForThisList && hasActiveTimer

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Timer Status Indicator
        if (hasActiveTimer && isActive) {
            Surface(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                color = when {
                    isRunning -> MaterialTheme.colors.primary.copy(alpha = 0.2f)
                    isPaused -> MaterialTheme.colors.secondary.copy(alpha = 0.2f)
                    else -> MaterialTheme.colors.surface
                },
                shape = CircleShape
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (isRunning) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Running",
                            tint = MaterialTheme.colors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    } else if (isPaused) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = "Paused",
                            tint = MaterialTheme.colors.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Timer Info Card (when active)
        if (hasActiveTimer && isActive) {
            Surface(
                modifier = Modifier
                    .height(40.dp)
                    .clip(RoundedCornerShape(20.dp)),
                color = when {
                    isRunning -> MaterialTheme.colors.primary.copy(alpha = 0.1f)
                    isPaused -> MaterialTheme.colors.secondary.copy(alpha = 0.1f)
                    else -> MaterialTheme.colors.surface
                },
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = when {
                        isRunning -> MaterialTheme.colors.primary.copy(alpha = 0.3f)
                        isPaused -> MaterialTheme.colors.secondary.copy(alpha = 0.3f)
                        else -> MaterialTheme.colors.onSurface.copy(alpha = 0.1f)
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .fillMaxHeight(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = timerPlaybackState.currentTimer?.name ?: "",
                        style = MaterialTheme.typography.body2,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colors.onSurface
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = timerPlaybackState.remainingMillis.milliseconds.formatDefault(),
                        style = MaterialTheme.typography.body2,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isRunning -> MaterialTheme.colors.primary
                            isPaused -> MaterialTheme.colors.secondary
                            else -> MaterialTheme.colors.onSurface
                        }
                    )
                }
            }
        }

        // Control Buttons
        when {
            isRunning && isActive -> {
                // Pause Button
                Button(
                    onClick = onPause,
                    modifier = Modifier.height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.secondary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pause",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Pause", style = MaterialTheme.typography.button)
                }
                
                // Stop Button
                OutlinedButton(
                    onClick = onStop,
                    modifier = Modifier.height(40.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colors.error
                    ),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colors.error.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Stop", style = MaterialTheme.typography.button)
                }
            }
            isPaused && isActive -> {
                // Resume Button
                Button(
                    onClick = onResume,
                    modifier = Modifier.height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.primary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Resume",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Resume", style = MaterialTheme.typography.button)
                }
                
                // Stop Button
                OutlinedButton(
                    onClick = onStop,
                    modifier = Modifier.height(40.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colors.error
                    ),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colors.error.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Stop", style = MaterialTheme.typography.button)
                }
            }
            else -> {
                // Start Button
                Button(
                    onClick = onStart,
                    modifier = Modifier.height(40.dp),
                    enabled = timerList != null,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.primary,
                        contentColor = Color.White,
                        disabledBackgroundColor = MaterialTheme.colors.onSurface.copy(alpha = 0.12f),
                        disabledContentColor = MaterialTheme.colors.onSurface.copy(alpha = 0.38f)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Start",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Start", style = MaterialTheme.typography.button)
                }
                
                // Show stop button only if there's an active timer (even if not for this list)
                if (hasActiveTimer && !isActive) {
                    OutlinedButton(
                        onClick = onStop,
                        modifier = Modifier.height(40.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colors.error
                        ),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colors.error.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Stop", style = MaterialTheme.typography.button)
                    }
                }
            }
        }
    }
}

