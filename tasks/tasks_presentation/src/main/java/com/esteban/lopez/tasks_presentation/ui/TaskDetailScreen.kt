package com.esteban.ruano.tasks_presentation.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.esteban.ruano.core_ui.composables.AppBar
import com.esteban.ruano.core_ui.composables.button.BaseButton
import com.esteban.ruano.core_ui.composables.button.ButtonType
import com.esteban.ruano.core_ui.R
import com.esteban.ruano.tasks_presentation.intent.TaskIntent
import com.esteban.ruano.tasks_presentation.ui.composables.TaskReminderItem
import com.esteban.ruano.tasks_presentation.ui.viewmodel.TaskDetailViewModel
import kotlinx.coroutines.launch


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
            
            state.errorMessage!=null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.errorMessage!!)
                }
            }

             else -> {
                var done by remember { mutableStateOf(state.task?.done == true) }

                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize()
                ) {
                    AppBar(stringResource(id = R.string.task_detail_title), actions = {
                        IconButton(onClick = {
                            onEditClick(state.task?.id!!)
                        }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit")
                        }
                    }, onClose = onNavigateUp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Task Name", style = MaterialTheme.typography.h3)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = state.task?.name ?: "", style = MaterialTheme.typography.body1)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Frequency", style = MaterialTheme.typography.h3)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Due Date", style = MaterialTheme.typography.h3)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.task?.dueDateTime ?: stringResource(id = R.string.no_due_date),
                        style = MaterialTheme.typography.body1
                    )
                    Text(text = "Scheduled Date", style = MaterialTheme.typography.h3)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.task?.scheduledDateTime ?: stringResource(id = R.string.no_scheduled_date),
                        style = MaterialTheme.typography.body1
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Reminders", style = MaterialTheme.typography.h3)
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth().border(
                            width = 2.dp,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
                        ).padding(8.dp)
                    ) {
                        state.task?.reminders?.forEach {
                            TaskReminderItem(
                                reminder = it,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Note", style = MaterialTheme.typography.h3)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (state.task?.note.isNullOrEmpty()) stringResource(id = R.string.no_note) else state.task?.note!!,
                        style = MaterialTheme.typography.body1
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Status", style = MaterialTheme.typography.h3)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (done) "Completed" else "Pending",
                        style = MaterialTheme.typography.body1
                    )

                }
                Column(Modifier.align(Alignment.BottomCenter)) {
                    BaseButton(
                        onClick = {
                            coroutineScope.launch {
                                if (done) {
                                    viewModel.performAction(TaskIntent.UnCompleteTask(state.task?.id!!){
                                        done = it
                                    })
                                } else {
                                    viewModel.performAction(TaskIntent.CompleteTask(state.task?.id!!){
                                        done = it
                                    })
                                }
                            }
                        },
                        text = if (done) stringResource(id = R.string.mark_as_undone) else stringResource(
                            id = R.string.mark_as_done
                        ),
                    )
                    BaseButton(
                        onClick = {
                            coroutineScope.launch {
                                viewModel.performAction(TaskIntent.DeleteTask(state.task?.id!!))
                                onNavigateUp()
                            }
                        },
                        text = stringResource(id = R.string.delete),
                        buttonType = ButtonType.DANGER,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

            }
            
        }
    }

}