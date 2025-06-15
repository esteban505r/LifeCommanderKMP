package com.esteban.ruano.tasks_presentation.ui

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.esteban.ruano.core_ui.R
import com.esteban.ruano.core_ui.composables.AppBar
import com.esteban.ruano.core_ui.composables.GeneralOutlinedTextField
import com.esteban.ruano.core_ui.utils.LocalMainIntent
import com.esteban.ruano.core_ui.utils.LocalMainState
import com.esteban.ruano.core_ui.view_model.intent.MainIntent
import com.esteban.ruano.lifecommander.models.TaskFilters
import com.esteban.ruano.lifecommander.ui.components.ToggleChipsButtons
import com.esteban.ruano.tasks_presentation.intent.TaskIntent
import com.esteban.ruano.tasks_presentation.ui.composables.TasksCalendarView
import com.esteban.ruano.tasks_presentation.ui.viewmodel.state.TaskState
import com.esteban.ruano.tasks_presentation.utils.toResource
import com.esteban.ruano.test_core.base.TestTags
import com.esteban.ruano.ui.components.TaskList
import com.lifecommander.models.Task
import kotlinx.coroutines.launch

@Composable
fun TasksScreen(
    onNavigateUp: () -> Unit,
    onTaskClick: (Task) -> Unit,
    onNewTaskClick: () -> Unit,
    state: TaskState,
    userIntent: (TaskIntent) -> Unit,
) {
    Log.d("TasksScreen", "TasksScreen is being recomposed")


    val coroutineScope = rememberCoroutineScope()
    val filters = TaskFilters.entries
    //val search = viewModel.search.collectAsState()
    //val dateRange = viewModel.dateRange.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    val scaffoldState = rememberScaffoldState()

    val mainState = LocalMainState.current
    val sendMainIntent = LocalMainIntent.current

    Scaffold(
        scaffoldState = scaffoldState,
        floatingActionButtonPosition = FabPosition.Center,
        modifier = Modifier.testTag(TestTags.TASKS_SCREEN),
        floatingActionButton = {
            Button(
                onClick = {
                    onNewTaskClick()
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    stringResource(id = R.string.new_task_title),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column {
                AppBar(stringResource(id = R.string.tasks_title), actions = {

                    /*Switch(
                        checked = state.offlineModeEnabled,
                        onCheckedChange = {
                            coroutineScope.launch {
                                userIntent(
                                    TaskIntent.ToggleOfflineMode(state.offlineModeEnabled.not())
                                )
                            }
                        }
                    )*/

                    /*when {

                        mainState.isLoading -> {
                            IconButton(onClick = {}) {
                                CircularProgressIndicator(
                                    color = Color.Black,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        mainState.isSynced -> {
                            IconButton(onClick = {}) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Sync"
                                )
                            }
                        }

                        else -> {
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    sendMainIntent(
                                        MainIntent.Sync
                                    )
                                }
                            }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Sync"
                                )
                            }
                        }
                    }*/

                    IconButton(onClick = {
                        coroutineScope.launch {
                            userIntent(
                                TaskIntent.ToggleCalendarView
                            )
                        }
                    }) {
                        Icon(
                            if (state.calendarViewEnabled) Icons.AutoMirrored.Default.List else Icons.Default.DateRange,
                            contentDescription = "Calendar View"
                        )
                    }
                })
                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp)
                        .fillMaxWidth()
                ) {
                    if (state.calendarViewEnabled) {
                        TasksCalendarView()
                    } else {
                        GeneralOutlinedTextField(
                            value = state.filter,
                            placeHolder = R.string.search_tasks
                        ) {
                            userIntent(
                                TaskIntent.SetFilter(it)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        ToggleChipsButtons(
                            state.dateRangeSelectedIndex,
                            buttons = filters,
                            onGetStrings = {
                                stringResource(id = it.toResource())
                            },
                            onCheckedChange = {
                                userIntent(
                                    TaskIntent.SetDateRangeSelectedIndex(filters.indexOf(it))
                                )
                                coroutineScope.launch {
                                    when (it) {
                                        TaskFilters.ALL -> {
                                            userIntent(TaskIntent.ClearDateRange)
                                        }

                                        TaskFilters.NO_DUE_DATE -> {
                                            val range = TaskFilters.NO_DUE_DATE.getDateRangeByFilter()
                                            userIntent(
                                                TaskIntent.SetDateRange(
                                                    range.first,
                                                    range.second
                                                )
                                            )
                                        }

                                        else -> {
                                            it.getDateRangeByFilter()
                                                .let { (startDate, endDate) ->
                                                    userIntent(
                                                        TaskIntent.SetDateRange(
                                                            startDate,
                                                            endDate
                                                        )
                                                    )
                                                }
                                        }
                                    }
                                }
                            })
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth().fillMaxHeight()
                        ){
                            when{
                                state.isLoading -> {
                                    CircularProgressIndicator(
                                        color = Color.Black,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                                state.tasks.isEmpty() -> {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .align(Alignment.TopCenter),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        Spacer(modifier = Modifier.height(100.dp))
                                        Image(
                                            painter = painterResource(R.drawable.empty_tasks),
                                            contentDescription = "Empty tasks",
                                            modifier = Modifier.size(150.dp)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = stringResource(id = R.string.empty_tasks),
                                            style = MaterialTheme.typography.subtitle1,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.width(400.dp)
                                        )
                                    }
                                }
                                else -> {
                                    TaskList(state.tasks, state.isRefreshing, onPullRefresh = {
                                        coroutineScope.launch {
                                            userIntent(
                                                TaskIntent.Refresh
                                            )
                                        }
                                    }, onTaskClick = onTaskClick,
                                        onCheckedChange = { task, it ->
                                            if (task.id != null) {
                                                coroutineScope.launch {
                                                    userIntent(
                                                        if (it) TaskIntent.CompleteTask(
                                                            task.id!!
                                                        ) {
                                                            coroutineScope.launch {
                                                                sendMainIntent(
                                                                    MainIntent.Sync
                                                                )
                                                            }
                                                        } else TaskIntent.UnCompleteTask(
                                                            task.id!!
                                                        ) {
                                                            coroutineScope.launch {
                                                                sendMainIntent(
                                                                    MainIntent.Sync
                                                                )
                                                            }
                                                        }
                                                    )
                                                }
                                            }
                                        },
                                        onEdit = {
                                            onTaskClick(it)
                                        },
                                        onDelete = {
                                            coroutineScope.launch {
                                                userIntent(
                                                    TaskIntent.DeleteTask(it.id)
                                                )
                                            }
                                        },
                                        onReschedule = {
                                            coroutineScope.launch {
                                                userIntent(
                                                    TaskIntent.RescheduleTask(it.id!!, it)
                                                )
                                            }
                                        },
                                        itemWrapper = { content,task ->
                                            Box {
                                                content.invoke()
                                            }
                                        }
                                    )
                                }
                            }
                        }

                    }

                }
            }


        }
    }

}

