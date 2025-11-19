package com.esteban.ruano.lifecommander.ui.screens

import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.esteban.ruano.lifecommander.models.TaskFilters
import com.esteban.ruano.lifecommander.ui.components.TagsSidebar
import com.esteban.ruano.lifecommander.ui.components.ToggleButtons
import com.esteban.ruano.lifecommander.ui.components.ToggleChipsButtons
import com.esteban.ruano.ui.components.TaskList
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import com.esteban.ruano.utils.DateUIUtils.getCurrentDateTime
import com.esteban.ruano.utils.DateUIUtils.toLocalDateTime
import com.lifecommander.models.Reminder
import com.lifecommander.models.Task
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime as toLocalDateTimeKt
import org.koin.compose.viewmodel.koinViewModel
import ui.composables.NewEditTaskDialog
import ui.viewmodels.TasksViewModel

@Composable
fun TasksScreen(
    tasks: List<Task>,
    selectedFilter: TaskFilters,
    tasksLoading: Boolean,
    tags: List<com.lifecommander.models.Tag>,
    selectedTagSlug: String?,
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
    onFilterChange: (TaskFilters) -> Unit,
    onTagClick: (String?) -> Unit,
    onCreateTag: () -> Unit,
    onTagLongClick: (com.lifecommander.models.Tag) -> Unit = {},
    onUpdateTaskTags: ((String, List<String>) -> Unit)? = null
) {
    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    var showNewTaskDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var taskToDelete by remember { mutableStateOf<Task?>(null) }
    val currentDate = getCurrentDateTime(TimeZone.currentSystemDefault()).date

    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        // Tags Sidebar
        TagsSidebar(
            tags = tags,
            selectedTagSlug = selectedTagSlug,
            onTagClick = onTagClick,
            onCreateTag = onCreateTag,
            onTagLongClick = onTagLongClick,
            modifier = Modifier.fillMaxHeight()
        )

        // Main Content
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

            // Filter, Sort, and Add Task Controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { /* TODO: Implement filter */ },
                    modifier = Modifier.height(40.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colors.primary
                    )
                ) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Filter")
                }

                OutlinedButton(
                    onClick = { /* TODO: Implement sort */ },
                    modifier = Modifier.height(40.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colors.primary
                    )
                ) {
                    Icon(Icons.Default.Sort, contentDescription = "Sort")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sort")
                }

                Button(
                    onClick = { showNewTaskDialog = true },
                    modifier = Modifier.height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.primary
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Task")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Task")
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
                                ContextMenuItem("Edit") { 
                                    taskToEdit = task
                                    showNewTaskDialog = true
                                },
                                ContextMenuItem("Delete") { 
                                    taskToDelete = task
                                    showDeleteDialog = true
                                }
                            )
                        }
                    ) {
                        content()
                    }
                }
            )
        }
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
            tags = tags,
            onUpdateTaskTags = onUpdateTaskTags,
            onError = { /* Handle error */ }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog && taskToDelete != null) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteDialog = false
                taskToDelete = null
            },
            title = {
                Text(
                    "Delete Task",
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Are you sure you want to delete \"${taskToDelete!!.name}\"? This action cannot be undone.",
                    style = MaterialTheme.typography.body1
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(taskToDelete!!)
                        showDeleteDialog = false
                        taskToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.error
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { 
                        showDeleteDialog = false
                        taskToDelete = null
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel")
                }
            }
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

