package com.esteban.ruano.lifecommander.ui.composables

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lifecommander.models.Frequency
import com.lifecommander.models.Reminder
import ui.composables.NewEditHabitDialog
import ui.composables.NewEditTaskDialog

@Composable
fun GeneralFloatingActionButtons(
    onAddTask: (String, String, List<Reminder>, String?, String?, Int?) -> Unit = { _, _, _, _, _, _ -> },
    onAddHabit: (String, String, String, Frequency, List<com.esteban.ruano.lifecommander.models.HabitReminder>) -> Unit = { _, _, _, _, _ -> },
    onError: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showNewTaskDialog by remember { mutableStateOf(false) }
    var showNewHabitDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (showMenu) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Add Habit",
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onSurface
                    )
                    FloatingActionButton(
                        onClick = { showNewHabitDialog = true },
                        backgroundColor = MaterialTheme.colors.secondary,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.Repeat,
                            contentDescription = "Add Habit",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Add Task",
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onSurface
                    )
                    FloatingActionButton(
                        onClick = { showNewTaskDialog = true },
                        backgroundColor = MaterialTheme.colors.secondary,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Add Task",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            FloatingActionButton(
                onClick = { showMenu = !showMenu },
                backgroundColor = MaterialTheme.colors.primary,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    if (showMenu) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = if (showMenu) "Close Menu" else "Open Menu",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }

    if (showNewTaskDialog) {
        NewEditTaskDialog(
            taskToEdit = null,
            show = true,
            onDismiss = { showNewTaskDialog = false },
            onAddTask = onAddTask,
            onError = onError,
            onUpdateTask = { _, _ -> },
        )
    }

    if (showNewHabitDialog) {
        NewEditHabitDialog(
            habitToEdit = null,
            show = true,
            onDismiss = { showNewHabitDialog = false },
            onAddHabit = onAddHabit,
            onError = onError,
            onUpdateHabit = { _, _ -> },
        )
    }
} 