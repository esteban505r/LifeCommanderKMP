package com.esteban.ruano.tasks_presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.esteban.ruano.core_ui.composables.AppBar
import com.esteban.ruano.core_ui.composables.button.BaseButton
import com.esteban.ruano.core_ui.composables.button.ButtonType
import com.esteban.ruano.core_ui.R
import com.esteban.ruano.core_ui.theme.SoftYellow
import com.esteban.ruano.core_ui.utils.DateUIUtils.toLocalDateTime
import com.esteban.ruano.core_ui.utils.DateUIUtils.toResourceStringBasedOnNow
import com.esteban.ruano.tasks_presentation.intent.TaskIntent
import com.esteban.ruano.tasks_presentation.ui.composables.TaskReminderItem
import com.esteban.ruano.tasks_presentation.ui.viewmodel.TaskDetailViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TaskDetailScreen(
    taskId: String,
    onNavigateUp: () -> Unit,
    onEditClick: (String) -> Unit,
    viewModel: TaskDetailViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val pullRefreshState = rememberPullRefreshState(false, onRefresh = {
        coroutineScope.launch {
            viewModel.performAction(TaskIntent.FetchTask(taskId))
        }
    })
    val state by viewModel.viewState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.performAction(TaskIntent.FetchTask(taskId))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        when {
            state.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            
            state.errorMessage != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.errorMessage!!)
                }
            }

            else -> {
                var done by remember { mutableStateOf(state.task?.done == true) }
                val task = state.task

                Column(
                    modifier = Modifier
                        .padding(top = 24.dp, start = 16.dp, end = 16.dp)
                        .fillMaxSize()
                ) {
                    AppBar(
                        task?.name ?: "",
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        actions = {
                            IconButton(onClick = {
                                onEditClick(task?.id!!)
                            }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Edit")
                            }
                        }, 
                        onClose = onNavigateUp
                    )
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Priority and Status Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Priority: ${getPriorityText(task?.priority ?: 0)}",
                                style = MaterialTheme.typography.subtitle1
                            )
                            Text(
                                task?.dueDateTime?.toLocalDateTime()?.toResourceStringBasedOnNow(context)?.first ?: "",
                                style = MaterialTheme.typography.subtitle1,
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Status Display
                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (done) stringResource(id = R.string.done) else stringResource(id = R.string.pending),
                                style = MaterialTheme.typography.body1.copy(
                                    color = if (done) MaterialTheme.colors.primary else SoftYellow,
                                    fontSize = 64.sp
                                ),
                                modifier = Modifier.padding(vertical = 24.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Statistics Cards
                        Row {
                            Card(
                                modifier = Modifier.weight(1f),
                                backgroundColor = Color.White,
                                elevation = 0.dp,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(24.dp)) {
                                    Text(
                                        "Days Remaining",
                                        style = MaterialTheme.typography.h4,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                    Text(
                                        getDaysRemaining(task),
                                        style = MaterialTheme.typography.h2,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Card(
                                modifier = Modifier.weight(1f),
                                backgroundColor = Color.White,
                                elevation = 0.dp,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(24.dp)) {
                                    Text(
                                        "Reminders",
                                        style = MaterialTheme.typography.h4,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                    Text(
                                        "${task?.reminders?.size ?: 0}",
                                        style = MaterialTheme.typography.h2,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }
                        
                        // Due Date Card
                        if (task?.dueDateTime != null) {
                            Card(
                                modifier = Modifier
                                    .padding(top = 16.dp)
                                    .fillMaxWidth(),
                                backgroundColor = Color.White,
                                elevation = 0.dp,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(24.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Today,
                                            contentDescription = "Due Date",
                                            tint = MaterialTheme.colors.primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "Due Date",
                                            style = MaterialTheme.typography.h4,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )
                                    }
                                    Text(
                                        task.dueDateTime?.toLocalDateTime()?.toResourceStringBasedOnNow(context)?.first ?: "",
                                        style = MaterialTheme.typography.h2,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }
                        
                        // Scheduled Date Card
                        if (task?.scheduledDateTime != null) {
                            Card(
                                modifier = Modifier
                                    .padding(top = 16.dp)
                                    .fillMaxWidth(),
                                backgroundColor = Color.White,
                                elevation = 0.dp,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(24.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Schedule,
                                            contentDescription = "Scheduled Date",
                                            tint = MaterialTheme.colors.primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "Scheduled Date",
                                            style = MaterialTheme.typography.h4,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )
                                    }
                                    Text(
                                        task.scheduledDateTime?.toLocalDateTime()?.toResourceStringBasedOnNow(context)?.first ?: "",
                                        style = MaterialTheme.typography.h2,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Reminders Section
                        if (!task?.reminders.isNullOrEmpty()) {
                            Text(
                                text = "Reminders", 
                                style = MaterialTheme.typography.h3,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                backgroundColor = Color.White,
                                elevation = 0.dp,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    task.reminders?.forEach { reminder ->
                                        TaskReminderItem(reminder = reminder)
                                        if (reminder != task.reminders!!.last()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Note Section
                        if (!task?.note.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Note", 
                                style = MaterialTheme.typography.h3,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                backgroundColor = Color.White,
                                elevation = 0.dp,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = task?.note ?: "",
                                    style = MaterialTheme.typography.body1,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Action Buttons
                        BaseButton(
                            onClick = {
                                coroutineScope.launch {
                                    if (done) {
                                        viewModel.performAction(TaskIntent.UnCompleteTask(task?.id!!) {
                                            done = it
                                        })
                                    } else {
                                        viewModel.performAction(TaskIntent.CompleteTask(task?.id!!) {
                                            done = it
                                        })
                                    }
                                }
                            },
                            text = if (done) stringResource(id = R.string.mark_as_undone) else stringResource(
                                id = R.string.mark_as_done
                            ),
                        )
                        
                        Row {
                            Button(
                                onClick = {
                                    onEditClick(task?.id!!)
                                },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text("Edit task", modifier = Modifier.padding(vertical = 8.dp))
                            }
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        viewModel.performAction(TaskIntent.DeleteTask(task?.id!!))
                                        onNavigateUp()
                                    }
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = MaterialTheme.colors.error
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text("Delete task", modifier = Modifier.padding(vertical = 8.dp))
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun getPriorityText(priority: Int): String {
    return when (priority) {
        0 -> "Low"
        1 -> "Medium"
        2 -> "High"
        3 -> "Urgent"
        else -> "Low"
    }
}

@Composable
private fun getDaysRemaining(task: com.lifecommander.models.Task?): String {
    if (task?.dueDateTime == null) return "No due date"
    
    val dueDate = task.dueDateTime?.toLocalDateTime()?.toLocalDate()
    val today = LocalDateTime.now().toLocalDate()
    
    if (dueDate == null) return "No due date"
    
    val daysDiff = java.time.temporal.ChronoUnit.DAYS.between(today, dueDate)
    
    return when {
        daysDiff < 0 -> "${Math.abs(daysDiff)} days overdue"
        daysDiff == 0L -> "Due today"
        daysDiff == 1L -> "Due tomorrow"
        else -> "$daysDiff days"
    }
}