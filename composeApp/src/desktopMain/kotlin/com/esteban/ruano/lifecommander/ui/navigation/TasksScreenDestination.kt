package com.esteban.ruano.lifecommander.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.esteban.ruano.lifecommander.ui.components.ErrorScreen
import com.esteban.ruano.lifecommander.ui.components.LoadingScreen
import com.esteban.ruano.lifecommander.ui.screens.TasksScreen
import com.esteban.ruano.utils.DateUIUtils.toLocalDateTime
import com.lifecommander.models.Task
import org.koin.compose.viewmodel.koinViewModel
import ui.viewmodels.TasksViewModel

@Composable
fun TasksScreenDestination(
    modifier: Modifier = Modifier,
    tasksViewModel: TasksViewModel = koinViewModel(),
    onTaskClick: (Task) -> Unit
) {
    val tasks by tasksViewModel.tasks.collectAsState()
    val selectedFilter by tasksViewModel.selectedFilter.collectAsState()
    val tasksLoading by tasksViewModel.loading.collectAsState()
    val tasksError by tasksViewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        tasksViewModel.getTasksByFilter()
    }

    when {
        tasksLoading -> {
            LoadingScreen(
                message = "Loading tasks...",
                modifier = modifier
            )
        }
        tasksError != null -> {
            ErrorScreen(
                message = tasksError ?: "Failed to load tasks",
                onRetry = { tasksViewModel.getTasksByFilter() },
                modifier = modifier
            )
        }
        else -> {
            TasksScreen(
                selectedFilter = selectedFilter,
                tasks = tasks,
                tasksLoading = tasksLoading,
                onReschedule = { task ->
                    tasksViewModel.rescheduleTask(task)
                },
                onCheckedTask = { task, checked ->
                    tasksViewModel.changeCheckTask(task.id, checked)
                },
                onFilterChange = {
                    tasksViewModel.changeFilter(it)
                },
                onTaskClick = onTaskClick,
                onReload = {
                    tasksViewModel.getTasksByFilter()
                },
                onAddTask = { name, note, reminders, dueDate, scheduledDate, priority ->
                    tasksViewModel.addTask(name, dueDate, scheduledDate, note, priority)
                },
                onUpdateTask = {  id, task ->
                    tasksViewModel.updateTask(id, task)
                },
                onDelete = { task ->
                    tasksViewModel.deleteTask(task.id)
                },
            )
        }
    }
} 