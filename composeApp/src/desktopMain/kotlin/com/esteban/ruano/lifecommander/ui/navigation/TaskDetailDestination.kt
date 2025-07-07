package com.esteban.ruano.lifecommander.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.esteban.ruano.lifecommander.ui.components.ErrorScreen
import com.esteban.ruano.lifecommander.ui.components.LoadingScreen
import com.esteban.ruano.lifecommander.ui.screens.TaskDetailScreen
import org.koin.compose.viewmodel.koinViewModel
import ui.viewmodels.TasksViewModel

@Composable
fun TaskDetailDestination(
    modifier: Modifier = Modifier,
    taskId: String,
    tasksViewModel: TasksViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
    onEditTask: (String) -> Unit
) {
    val task by tasksViewModel.selectedTask.collectAsState()
    val taskLoading by tasksViewModel.taskDetailLoading.collectAsState()
    val taskError by tasksViewModel.taskDetailError.collectAsState()

    LaunchedEffect(taskId) {
        tasksViewModel.getTaskById(taskId)
    }

    when {
        taskLoading -> {
            LoadingScreen(
                message = "Loading task...",
                modifier = modifier
            )
        }
        taskError != null -> {
            ErrorScreen(
                message = taskError ?: "Failed to load task",
                onRetry = { tasksViewModel.getTaskById(taskId) },
                modifier = modifier
            )
        }
        else -> {
            task?.let { task ->
                TaskDetailScreen(
                    task = task,
                    onNavigateBack = onNavigateBack,
                    onEditTask = onEditTask,
                    onCompleteTask = { tasksViewModel.changeCheckTask(task.id, true) },
                    onUncompleteTask = { tasksViewModel.changeCheckTask(task.id, false) },
                    onDeleteTask = { tasksViewModel.deleteTask(task.id) },
                    modifier = modifier
                )
            }
        }
    }
} 