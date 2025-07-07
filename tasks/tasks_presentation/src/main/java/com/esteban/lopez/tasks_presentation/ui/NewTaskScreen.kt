package com.esteban.ruano.tasks_presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.esteban.ruano.core.utils.DateUtils.parseDateTime
import com.esteban.ruano.core_ui.composables.AppBar
import com.esteban.ruano.core_ui.composables.text.TitleH3
import com.esteban.ruano.core_ui.theme.Gray
import com.esteban.ruano.core_ui.theme.Gray2
import com.esteban.ruano.core_ui.utils.DateUIUtils.toMillis
import com.esteban.ruano.core_ui.R
import com.esteban.ruano.core_ui.composables.RemindersDialog
import com.esteban.ruano.core_ui.utils.DateUIUtils
import com.esteban.ruano.core_ui.utils.DateUIUtils.formatTime
import com.esteban.ruano.core_ui.utils.DateUIUtils.getTime
import com.esteban.ruano.core_ui.utils.DateUIUtils.parseTime
import com.esteban.ruano.core_ui.utils.DateUIUtils.toLocalDateTime
import com.esteban.ruano.core_ui.utils.toResourceString
import com.lifecommander.models.Task
import com.esteban.ruano.tasks_presentation.intent.TaskIntent
import com.esteban.ruano.tasks_presentation.ui.composables.TaskReminderItem
import com.esteban.ruano.tasks_presentation.ui.viewmodel.TaskDetailViewModel
import com.lifecommander.models.Reminder
import kotlinx.coroutines.launch
import java.time.LocalDateTime


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTaskScreenComposable(
    taskToEdit: Task?,
    onClose: () -> Unit,
    viewModel: TaskDetailViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    var name by remember {
        mutableStateOf(taskToEdit?.name ?: "")
    }
    var showDueDatePicker by remember {
        mutableStateOf(false)
    }
    var showScheduledDatePicker by remember {
        mutableStateOf(false)
    }

    val notes = remember { mutableStateOf("") }
    var dueDate by remember {
        mutableStateOf(if (taskToEdit != null) taskToEdit.dueDateTime?.toLocalDateTime() else null)
    }
    var scheduledDate by remember {
        mutableStateOf(taskToEdit?.scheduledDateTime?.toLocalDateTime())
    }

    var dueDateStartTime by remember {
        mutableStateOf(
            taskToEdit?.dueDateTime?.toLocalDateTime()?.getTime() ?: LocalDateTime.now().parseTime()
        )
    }

    var scheduledDateStartTime by remember {
        mutableStateOf(
            taskToEdit?.scheduledDateTime?.toLocalDateTime()?.getTime() ?: LocalDateTime.now()
                .parseTime()
        )
    }

    var reminders by remember {
        mutableStateOf(taskToEdit?.reminders ?: emptyList())
    }

    var showRemindersDialog by remember {
        mutableStateOf(false)
    }

    var showDueDateTimePicker by remember {
        mutableStateOf(false)
    }

    var showScheduledDateTimePicker by remember {
        mutableStateOf(false)
    }

    val dueDateState = rememberDatePickerState(
        initialSelectedDateMillis = dueDate?.toLocalDate()?.toMillis(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis >= System.currentTimeMillis().toLocalDateTime().minusYears(1)
                    .toMillis()
            }
        }
    )

    val scheduledDateState = rememberDatePickerState(
        initialSelectedDateMillis = scheduledDate?.toLocalDate()?.toMillis(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis >= System.currentTimeMillis().toLocalDateTime().minusYears(1)
                    .toMillis()
            }
        }
    )

    val dueDateStartTimeState = rememberTimePickerState(
        initialHour = dueDateStartTime.split(":")[0].toInt(),
        initialMinute = dueDateStartTime.split(":")[1].toInt()
    )

    val scheduledDateStartTimeState = rememberTimePickerState(
        initialHour = scheduledDateStartTime.split(":")[0].toInt(),
        initialMinute = scheduledDateStartTime.split(":")[1].toInt()
    )

    if (showDueDateTimePicker || showScheduledDateTimePicker) {
        Dialog(onDismissRequest = {
            showDueDateTimePicker = false
            showScheduledDateTimePicker = false
        }) {
            Surface(shape = RoundedCornerShape(16.dp)) {
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(24.dp)) {
                    TimePicker(state = if (showDueDateTimePicker) dueDateStartTimeState else scheduledDateStartTimeState)
                    TextButton(onClick = {
                        if (showDueDateTimePicker) dueDateStartTime =
                            formatTime(dueDateStartTimeState.hour, dueDateStartTimeState.minute)
                        else scheduledDateStartTime = formatTime(
                            scheduledDateStartTimeState.hour,
                            scheduledDateStartTimeState.minute
                        )
                        showDueDateTimePicker = false
                        showScheduledDateTimePicker = false
                    }) {
                        Text("Ok")
                    }
                }
            }
        }
    }

    if (showRemindersDialog) {
        RemindersDialog(
            onDismiss = {
                showRemindersDialog = false
            },
            onConfirm = {
                reminders = reminders + Reminder(
                    time = it
                )
                showRemindersDialog = false
            }
        )
    }

    if (showDueDatePicker || showScheduledDatePicker) {
        DatePickerDialog(onDismissRequest = {
            showDueDatePicker = false
            showScheduledDatePicker = false
        }, confirmButton = {
            TextButton(onClick = {
                if ( showDueDatePicker) {
                    dueDate = dueDateState.selectedDateMillis?.toLocalDateTime()
                }
                else {
                    scheduledDate = scheduledDateState.selectedDateMillis?.toLocalDateTime()
                }
                showDueDatePicker = false
                showScheduledDatePicker = false
            }) {
                Text("Ok")
            }
        }) {
            DatePicker(state = if(showDueDatePicker) dueDateState else scheduledDateState)
        }
    }


    Column(
        modifier = Modifier
            .padding(top = 24.dp, start = 16.dp, end = 16.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        AppBar(
            if (taskToEdit != null) stringResource(id = R.string.edit_task_title) else stringResource(
                id = R.string.new_task_title
            ),
            onClose = onClose
        ) {
            TextButton(onClick = {
                coroutineScope.launch {
                    if (taskToEdit != null) {
                        viewModel.performAction(
                            TaskIntent.UpdateTask(
                                taskToEdit.id!!,
                                taskToEdit.copy(
                                    name = name,
                                    note = notes.value,
                                    dueDateTime = dueDate?.let {
                                        DateUIUtils.joinDateAndTime(
                                            dueDate!!.toLocalDate(),
                                            dueDateStartTime
                                        ).parseDateTime()
                                    },
                                    scheduledDateTime = scheduledDate?.let {
                                        DateUIUtils.joinDateAndTime(
                                            scheduledDate!!.toLocalDate(),
                                            scheduledDateStartTime
                                        ).parseDateTime()
                                    },
                                    reminders = reminders
                                )
                            )
                        )
                        onClose()
                    } else {
                        viewModel.performAction(
                            TaskIntent.AddTask(
                                name = name,
                                note = notes.value,
                                reminders = reminders,
                                dueDate = dueDate?.let {
                                    DateUIUtils.joinDateAndTime(
                                        dueDate!!.toLocalDate(),
                                        dueDateStartTime
                                    ).parseDateTime()
                                },
                                scheduledDate = scheduledDate?.let {
                                    DateUIUtils.joinDateAndTime(
                                        scheduledDate!!.toLocalDate(),
                                        scheduledDateStartTime
                                    ).parseDateTime()
                                },
                                onComplete = { success ->
                                    if (success) {
                                        onClose()
                                    }
                                })
                        )
                    }
                }
            }) {
                Text(stringResource(id = R.string.save))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                backgroundColor = Color.White,
                focusedBorderColor = Gray2,
                unfocusedBorderColor = Gray2,
                placeholderColor = Gray
            ),
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(id = R.string.tasks_title)) }
        )
        Spacer(modifier = Modifier.height(24.dp))
        /*    ToggleButtons(buttons = listOf("Daily", "Weekly", "Monthly"), onCheckedChange = {
                frequency = it
            })*/
        Spacer(modifier = Modifier.height(16.dp))
        TitleH3(R.string.reminders)
        Column {
            reminders.forEach { it ->
                TaskReminderItem(
                    reminder = it,
                    withActions = true,
                    onDeleteReminder = { reminderToD ->
                        reminders = reminders - reminderToD
                    }
                )
            }
        }
        Button(
            onClick = {
                showRemindersDialog = true
            },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                stringResource(id = R.string.add_reminder),
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        TitleH3(R.string.notes)
        OutlinedTextField(
            value = notes.value,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                backgroundColor = Color.White,
                focusedBorderColor = Gray2,
                unfocusedBorderColor = Gray2,
                placeholderColor = Gray
            ),
            shape = RoundedCornerShape(12.dp),
            label = { Text(stringResource(id = R.string.add_notes)) },
            onValueChange = { notes.value = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier
            .fillMaxWidth()
            .clickable {
                showDueDatePicker = true
            }
            .padding(vertical = 16.dp)) {
            Text(stringResource(id = R.string.due_date))
            Text(
                dueDate?.toLocalDate()?.toResourceString()
                    ?: stringResource(id = R.string.no_due_date),
                    textAlign = TextAlign.End,
            )
        }
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier
            .fillMaxWidth()
            .clickable {
                showDueDateTimePicker = true
            }
            .padding(vertical = 16.dp)) {
            Text(stringResource(id = R.string.due_date_start_time))
            Text(if (dueDate == null) stringResource(id = R.string.no_due_date_start_time) else dueDateStartTime,textAlign = TextAlign.End,)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier
            .fillMaxWidth()
            .clickable {
                showScheduledDatePicker = true
            }
            .padding(vertical = 16.dp)) {
            Text(stringResource(id = R.string.scheduled_date))
            Text(
                scheduledDate?.toLocalDate()?.toResourceString()
                    ?: stringResource(id = R.string.no_scheduled_date),
                textAlign = TextAlign.End,
            )
        }
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier
            .fillMaxWidth()
            .clickable {
                showScheduledDateTimePicker = true
            }
            .padding(vertical = 16.dp)) {
            Text(stringResource(id = R.string.scheduled_date_start_time))
            Text(
                text = if (scheduledDate == null) stringResource(id = R.string.no_scheduled_date_start_time) else scheduledDateStartTime,
                textAlign = TextAlign.End,
            )
        }
    }
}

@Composable
fun NewTaskScreen(
    taskToEditId: String? = null,
    onClose: () -> Unit,
    viewModel: TaskDetailViewModel = hiltViewModel()
) {
    val editing = taskToEditId != null
    val state = viewModel.viewState.collectAsState()

    LaunchedEffect(Unit) {
        if (editing) {
            viewModel.performAction(TaskIntent.FetchTask(taskToEditId!!))
        }
    }
    when{
        state.value.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        state.value.errorMessage != null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = state.value.errorMessage!!)
            }
        }
        else -> {
            NewTaskScreenComposable(taskToEdit = state.value.task, onClose = onClose)
        }

    }
}
