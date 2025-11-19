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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.esteban.ruano.core.utils.DateUtils.parseDateTime
import com.esteban.ruano.core_ui.R
import com.esteban.ruano.core_ui.utils.DateUIUtils
import com.esteban.ruano.core_ui.utils.DateUIUtils.formatTime
import com.esteban.ruano.core_ui.utils.DateUIUtils.getTime
import com.esteban.ruano.core_ui.utils.DateUIUtils.parseTime
import com.esteban.ruano.core_ui.utils.DateUIUtils.toLocalDateTime
import com.esteban.ruano.core_ui.utils.DateUIUtils.toMillis
import com.esteban.ruano.core_ui.utils.toResourceString
import com.esteban.ruano.lifecommander.ui.components.AppBar
import com.esteban.ruano.lifecommander.ui.components.RemindersDialog
import com.esteban.ruano.lifecommander.ui.components.TagChip
import com.esteban.ruano.lifecommander.ui.components.text.TitleH3
import com.esteban.ruano.lifecommander.utils.UiUtils.getColorByPriority
import com.esteban.ruano.lifecommander.utils.UiUtils.getIconByPriority
import com.esteban.ruano.tasks_presentation.intent.TagIntent
import com.esteban.ruano.tasks_presentation.intent.TaskIntent
import com.esteban.ruano.tasks_presentation.ui.composables.TaskReminderItem
import com.esteban.ruano.tasks_presentation.ui.viewmodel.TagsViewModel
import com.esteban.ruano.tasks_presentation.ui.viewmodel.TaskDetailViewModel
import com.esteban.ruano.ui.Gray
import com.esteban.ruano.ui.Gray2
import com.esteban.ruano.ui.*
import com.lifecommander.models.Reminder
import com.lifecommander.models.Task
import kotlinx.coroutines.launch
import services.tasks.models.Priority
import services.tasks.models.Priority.Companion.toPriority
import java.time.LocalDateTime


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTaskScreenComposable(
    taskToEdit: Task?,
    onClose: () -> Unit,
    viewModel: TaskDetailViewModel = hiltViewModel(),
    tagsViewModel: TagsViewModel = hiltViewModel(),
    onManageTags: () -> Unit = {}
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
    
    // Priority state
    var prioritySelected by remember {
        mutableStateOf(taskToEdit?.priority?.toPriority() ?: Priority.MEDIUM)
    }
    
    // Tags state
    val tagsState = tagsViewModel.viewState.collectAsState()
    var selectedTags by remember {
        mutableStateOf<Set<String>>(taskToEdit?.tags?.map { it.id }?.toSet() ?: emptySet())
    }
    
    // Track which fields were cleared by the user
    var clearedFields by remember {
        mutableStateOf(setOf<String>())
    }
    
    LaunchedEffect(Unit) {
        tagsViewModel.performAction(TagIntent.LoadTags)
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
                        // Update task first
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
                                    reminders = reminders,
                                    priority = prioritySelected.value,
                                    clearFields = if (clearedFields.isNotEmpty()) clearedFields.toList() else null
                                )
                            )
                        )
                        // Update task tags after task update
                        // Note: This is fire-and-forget, but tags update is independent of task update
                        if (selectedTags.isNotEmpty() || taskToEdit.tags?.isNotEmpty() == true) {
                            tagsViewModel.performAction(
                                TagIntent.UpdateTaskTags(
                                    taskId = taskToEdit.id!!,
                                    tagIds = selectedTags.toList()
                                )
                            )
                        }
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
                                priority = prioritySelected.value,
                                onComplete = { success, taskId ->
                                    if (success && taskId != null) {
                                        // Update task tags after successful creation
                                        if (selectedTags.isNotEmpty()) {
                                            tagsViewModel.performAction(
                                                TagIntent.UpdateTaskTags(
                                                    taskId = taskId,
                                                    tagIds = selectedTags.toList()
                                                )
                                            )
                                        }
                                        onClose()
                                    } else if (!success) {
                                        // Handle error - don't close if creation failed
                                    } else {
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
        TitleH3(stringResource(R.string.reminders))
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
        TitleH3(stringResource(R.string.notes))
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
        Spacer(modifier = Modifier.height(16.dp))
        
        // Priority Section
        TitleH3("Priority")
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Priority.entries.forEach { priority ->
                val isSelected = prioritySelected == priority
                val priorityColor = getColorByPriority(priority.value)
                val icon = getIconByPriority(priority.value)
                
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { prioritySelected = priority },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) priorityColor.copy(alpha = 0.15f) else Color.White,
                    border = if (isSelected) {
                        androidx.compose.foundation.BorderStroke(1.5.dp, priorityColor)
                    } else {
                        androidx.compose.foundation.BorderStroke(1.dp, Gray2.copy(alpha = 0.3f))
                    },
                    elevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = priority.name,
                            tint = if (isSelected) priorityColor else Gray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = priority.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = androidx.compose.material.MaterialTheme.typography.caption.copy(
                                fontSize = 11.sp
                            ),
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) priorityColor else Gray,
                            maxLines = 1,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Tags Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TitleH3("Tags")
            TextButton(
                onClick = onManageTags
            ) {
                Text(
                    text = "Manage Tags",
                    style = androidx.compose.material.MaterialTheme.typography.body2,
                    color = LifeCommanderDesignSystem.colors.Primary
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (tagsState.value.tags.isEmpty() && !tagsState.value.isLoading) {
            Column {
                Text(
                    text = "No tags available. Create tags to organize your tasks.",
                    style = androidx.compose.material.MaterialTheme.typography.body2,
                    color = Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onManageTags,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Create Your First Tag")
                }
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(tagsState.value.tags) { tag ->
                    val isSelected = selectedTags.contains(tag.id)
                    TagChip(
                        tag = tag,
                        selected = isSelected,
                        onClick = {
                            selectedTags = if (isSelected) {
                                selectedTags - tag.id
                            } else {
                                selectedTags + tag.id
                            }
                        },
                        modifier = Modifier
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Due Date Section
        TitleH3(stringResource(id = R.string.due_date))
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    showDueDatePicker = true
                },
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, Gray2.copy(alpha = 0.3f))
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.due_date),
                    style = androidx.compose.material.MaterialTheme.typography.body1,
                    color = LifeCommanderDesignSystem.colors.OnSurface
                )
                Text(
                    text = dueDate?.toLocalDate()?.toResourceString()
                        ?: stringResource(id = R.string.no_due_date),
                    style = androidx.compose.material.MaterialTheme.typography.body1.copy(
                        fontWeight = if (dueDate != null) FontWeight.Medium else FontWeight.Normal
                    ),
                    textAlign = TextAlign.End,
                    color = if (dueDate != null) LifeCommanderDesignSystem.colors.OnSurface else Gray
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    showDueDateTimePicker = true
                },
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, Gray2.copy(alpha = 0.3f))
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.due_date_start_time),
                    style = androidx.compose.material.MaterialTheme.typography.body1,
                    color = LifeCommanderDesignSystem.colors.OnSurface
                )
                Text(
                    text = if (dueDate == null) stringResource(id = R.string.no_due_date_start_time) else dueDateStartTime,
                    style = androidx.compose.material.MaterialTheme.typography.body1.copy(
                        fontWeight = if (dueDate != null) FontWeight.Medium else FontWeight.Normal
                    ),
                    textAlign = TextAlign.End,
                    color = if (dueDate != null) LifeCommanderDesignSystem.colors.OnSurface else Gray
                )
            }
        }
        
        // Clear due date button
        if (dueDate != null) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    dueDate = null
                    clearedFields = clearedFields + "dueDateTime"
                },
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material.ButtonDefaults.outlinedButtonColors(
                    contentColor = androidx.compose.material.MaterialTheme.colors.error
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Clear, contentDescription = "Clear due date", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Clear Due Date", style = androidx.compose.material.MaterialTheme.typography.body2)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Scheduled Date Section
        TitleH3(stringResource(id = R.string.scheduled_date))
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    showScheduledDatePicker = true
                },
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, Gray2.copy(alpha = 0.3f))
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.scheduled_date),
                    style = androidx.compose.material.MaterialTheme.typography.body1,
                    color = LifeCommanderDesignSystem.colors.OnSurface
                )
                Text(
                    text = scheduledDate?.toLocalDate()?.toResourceString()
                        ?: stringResource(id = R.string.no_scheduled_date),
                    style = androidx.compose.material.MaterialTheme.typography.body1.copy(
                        fontWeight = if (scheduledDate != null) FontWeight.Medium else FontWeight.Normal
                    ),
                    textAlign = TextAlign.End,
                    color = if (scheduledDate != null) LifeCommanderDesignSystem.colors.OnSurface else Gray
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    showScheduledDateTimePicker = true
                },
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, Gray2.copy(alpha = 0.3f))
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.scheduled_date_start_time),
                    style = androidx.compose.material.MaterialTheme.typography.body1,
                    color = LifeCommanderDesignSystem.colors.OnSurface
                )
                Text(
                    text = if (scheduledDate == null) stringResource(id = R.string.no_scheduled_date_start_time) else scheduledDateStartTime,
                    style = androidx.compose.material.MaterialTheme.typography.body1.copy(
                        fontWeight = if (scheduledDate != null) FontWeight.Medium else FontWeight.Normal
                    ),
                    textAlign = TextAlign.End,
                    color = if (scheduledDate != null) LifeCommanderDesignSystem.colors.OnSurface else Gray
                )
            }
        }
        
        // Clear scheduled date button
        if (scheduledDate != null) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    scheduledDate = null
                    clearedFields = clearedFields + "scheduledDateTime"
                },
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material.ButtonDefaults.outlinedButtonColors(
                    contentColor = androidx.compose.material.MaterialTheme.colors.error
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Clear, contentDescription = "Clear scheduled date", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Clear Scheduled Date", style = androidx.compose.material.MaterialTheme.typography.body2)
            }
        }
    }
}

@Composable
fun NewTaskScreen(
    taskToEditId: String? = null,
    onClose: () -> Unit,
    viewModel: TaskDetailViewModel = hiltViewModel(),
    onManageTags: () -> Unit = {}
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
            NewTaskScreenComposable(
                taskToEdit = state.value.task,
                onClose = onClose,
                tagsViewModel = hiltViewModel(),
                onManageTags = onManageTags
            )
        }

    }
}
