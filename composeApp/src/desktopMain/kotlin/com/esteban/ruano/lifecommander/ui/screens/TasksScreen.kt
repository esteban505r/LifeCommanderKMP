package com.esteban.ruano.lifecommander.ui.screens

import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.esteban.ruano.lifecommander.models.TaskFilters
import com.esteban.ruano.lifecommander.ui.components.ToggleButtons
import com.esteban.ruano.lifecommander.ui.components.ToggleChipsButtons
import com.esteban.ruano.ui.components.TaskList
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import com.esteban.ruano.utils.DateUIUtils.toLocalDateTime
import com.lifecommander.models.Reminder
import com.lifecommander.models.Task
import kotlinx.datetime.Clock
import kotlinx.datetime.toLocalDateTime as toLocalDateTimeKt
import org.koin.compose.viewmodel.koinViewModel
import ui.composables.NewEditTaskDialog
import ui.viewmodels.TasksViewModel

@Composable
fun TasksScreen(
    tasks: List<Task>,
    selectedFilter: TaskFilters,
    tasksLoading: Boolean,
    onTaskClick: (Task) -> Unit,
    onReload: () -> Unit,
    onDelete: (Task) -> Unit,
    onReschedule: (Task) -> Unit,
    onCheckedTask: (Task, Boolean) -> Unit,
    onAddTask: (
        name: String,
        note: String?,
        reminders: List<Reminder>,
        dueDate: String?,
        scheduledDate: String?,
        priority: Int
    ) -> Unit,
    onUpdateTask: (String, Task) -> Unit,
    onFilterChange: (TaskFilters) -> Unit
) {
    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    var showNewTaskDialog by remember { mutableStateOf(false) }
    val currentDate = Clock.System.now().toLocalDateTimeKt(
        timeZone = kotlinx.datetime.TimeZone.currentSystemDefault()
    ).date

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Tasks",
                    style = MaterialTheme.typography.h4,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = currentDate.formatDefault(),
                    style = MaterialTheme.typography.subtitle1,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }

            // Filter and Sort Controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { /* TODO: Implement filter */ },
                    modifier = Modifier.height(40.dp)
                ) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Filter")
                }

                OutlinedButton(
                    onClick = { /* TODO: Implement sort */ },
                    modifier = Modifier.height(40.dp)
                ) {
                    Icon(Icons.Default.Sort, contentDescription = "Sort")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sort")
                }
            }
        }

        // Task Categories
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TaskCategoryCard(
                title = "Today",
                count = tasks.count { it.dueDateTime?.let { it.toLocalDateTime().date == currentDate } ?:
                 it.scheduledDateTime?.let { it.toLocalDateTime().date == currentDate } ?: false },
                modifier = Modifier.weight(1f)
            )
            TaskCategoryCard(
                title = "Upcoming",
                count = tasks.count { it.dueDateTime?.let { it.toLocalDateTime().date > currentDate } ?:
                 it.scheduledDateTime?.let { it.toLocalDateTime().date > currentDate } ?: false },
                modifier = Modifier.weight(1f)
            )
            TaskCategoryCard(
                title = "Overdue",
                count = tasks.count { it.dueDateTime?.let{ it.toLocalDateTime().date < currentDate } ?:
                 it.scheduledDateTime?.let { it.toLocalDateTime().date < currentDate } ?: false } ,
                modifier = Modifier.weight(1f)
            )
        }

        if (tasksLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {

            ToggleChipsButtons(
                modifier = Modifier.padding(bottom = 16.dp),
                selectedIndex = TaskFilters.entries.indexOf(selectedFilter),
                buttons = TaskFilters.entries,
                onGetStrings = { filter -> filter.toString() },
                onCheckedChange = onFilterChange
            )

            TaskList(
                taskList = tasks,
                isRefreshing = false,
                onPullRefresh = {
                    onReload()
                },
                onTaskClick = onTaskClick,
                onCheckedChange = { task, checked ->
                  onCheckedTask(task, checked)
                },
                modifier = Modifier.fillMaxSize(),
                onEdit = { task ->
                    taskToEdit = task
                    showNewTaskDialog = true
                },
                onDelete = { task ->
                    onDelete(task)
                },
                onReschedule = { task ->
                  onReschedule(task)
                },
                itemWrapper = { content, task ->
                    ContextMenuArea(
                        items = {
                            listOf(
                                ContextMenuItem("Edit") { },
                                ContextMenuItem("Delete") { }
                            )
                        }
                    ) {
                        content()
                    }
                }
            )
        }

    }

    if (showNewTaskDialog) {
        NewEditTaskDialog(
            taskToEdit = taskToEdit,
            show = showNewTaskDialog,
            onDismiss = { 
                showNewTaskDialog = false
                taskToEdit = null
            },
            onAddTask = { name, note, reminders, dueDate, scheduledDate, priority ->
                onAddTask(
                    name,
                    note,
                    reminders,
                    dueDate,
                    scheduledDate,
                    priority ?: 0
                )
            },
            onUpdateTask = { id, task ->
                onUpdateTask(id, task)
            },
            onError = { /* Handle error */ }
        )
    }
}

@Composable
 fun TaskCategoryCard(
    title: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.subtitle1,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.h5,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

