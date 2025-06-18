package com.esteban.ruano.lifecommander.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.esteban.ruano.lifecommander.ui.components.PlaceholderScreen

@Composable
fun TimersPlaceholderScreen(
    modifier: Modifier = Modifier
) {
    PlaceholderScreen(
        title = "Timer Management",
        description = "Create and manage custom timers, pomodoros, and productivity sessions. Track your time and stay focused on your tasks.",
        icon = Icons.Default.Timer,
        modifier = modifier
    )
}

@Composable
fun TimerListDetailPlaceholderScreen(
    timerListId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Timer Details",
                    style = MaterialTheme.typography.h4,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        PlaceholderScreen(
            title = "Timer List Details",
            description = "View and manage individual timers in this list. Start, pause, and track your productivity sessions.",
            icon = Icons.Default.PlayArrow,
            modifier = Modifier.fillMaxSize()
        )
    }
} 