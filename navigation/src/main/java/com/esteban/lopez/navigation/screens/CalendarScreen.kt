package com.esteban.lopez.navigation.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material.Text
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment

@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier,
    onTaskClick: (String) -> Unit,
    onHabitClick: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendar") }
            )
        }
    ) { padding ->
        val contentModifier = modifier
            .fillMaxSize()
            .padding(padding)

        // NOTE:
        // The full-featured calendar (with tasks, habits, and transactions)
        // is currently implemented for desktop. This mobile screen is a
        // placeholder shell wired into navigation so the app compiles and
        // navigation works. We can later hook it to a shared calendar
        // ViewModel and UI once those are made multiplatform.
        Box(
            modifier = contentModifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Calendar (mobile) coming soon",
                style = MaterialTheme.typography.body1
            )
        }
    }
}

