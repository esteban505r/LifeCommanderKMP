package ui.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import getColorByPriority
import getIconByPriority
import services.tasks.models.Priority
import services.tasks.models.Priority.Companion.toPriority
import services.tasks.models.Reminder
import services.tasks.models.TaskResponse
import utils.DateUIUtils
import utils.DateUIUtils.getTime
import utils.DateUIUtils.parseDate
import utils.DateUIUtils.toLocalDateTime
import utils.DateUtils.parseDateTime
import utils.DateUtils.toLocalDate
import utils.DateUtils.toLocalDateTime
import utils.DateUtils.toLocalTime

@Composable
fun NewEditTaskDialog(
    taskToEdit: TaskResponse?,
    show: Boolean,
    onDismiss: () -> Unit,
    onAddTask: (String, String, List<Reminder>, String?, String?,Int?) -> Unit,
    onError: (String) -> Unit,
    onUpdateTask: (String, TaskResponse) -> Unit
) {
    var name by remember { mutableStateOf(taskToEdit?.name ?: "") }
    var notes by remember { mutableStateOf(taskToEdit?.note ?: "") }
    var dueDate by remember { mutableStateOf(taskToEdit?.dueDateTime?.toLocalDateTime()) }
    var scheduledDate by remember { mutableStateOf(taskToEdit?.scheduledDateTime?.toLocalDateTime()) }
    var reminders by remember { mutableStateOf(taskToEdit?.reminders ?: emptyList()) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    var editingDueDate by remember { mutableStateOf(false) }
    var showPriorityDropdown by remember { mutableStateOf(false) }

    var prioritySelected by remember { mutableStateOf(taskToEdit?.priority?.toPriority()?:Priority.NONE) }

    LaunchedEffect(taskToEdit){
        println("Task to edit: $taskToEdit")

        name = taskToEdit?.name ?: ""
        notes = taskToEdit?.note ?: ""
        dueDate = taskToEdit?.dueDateTime?.toLocalDateTime()
        scheduledDate = taskToEdit?.scheduledDateTime?.toLocalDateTime()
        reminders = taskToEdit?.reminders ?: emptyList()
    }

    DatePickerDialog(
        date = if(editingDueDate) dueDate?.toLocalDate()?.parseDate() else scheduledDate?.toLocalDate()?.parseDate(),
        show = showDatePicker,
        onDismiss = { showDatePicker = false },
        onDateSelected = { selectedDate ->
            try{
                if(editingDueDate) {
                    dueDate = selectedDate.toLocalDate().toLocalDateTime()
                }
                else {
                    scheduledDate = selectedDate.toLocalDate().toLocalDateTime()
                }
                showDatePicker = false
                showTimePicker = true
            }
            catch (e: Exception) {
                e.printStackTrace()
                onError("Invalid date")
                onDismiss()
            }
        }
    )

    TimePickerDialog(
        timep = if(editingDueDate) dueDate?.getTime() else scheduledDate?.getTime(),
        show = showTimePicker,
        onDismiss = { showTimePicker = false },
        onTimeSelected = { selectedTime ->
            try {
                selectedTime.toLocalTime()
                if(editingDueDate) {
                    dueDate = DateUIUtils.joinDateAndTime(dueDate!!.toLocalDate(), selectedTime)
                }
                else {
                    scheduledDate = DateUIUtils.joinDateAndTime(scheduledDate!!.toLocalDate(), selectedTime)
                }
                showTimePicker = false
            }
            catch (e: Exception) {
                e.printStackTrace()
                onError("Invalid time")
                onDismiss()
            }
        }
    )

    if (show) {
        DialogWindow(
            onCloseRequest = onDismiss,
            state = rememberDialogState(position = WindowPosition(Alignment.Center))
        ) {
            LaunchedEffect(Unit){
                window.pack()
            }
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colors.background
            ) {
            Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .wrapContentSize()
                        .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                    Text(
                        text = if(taskToEdit != null) "Edit Task" else "New Task",
                        style = MaterialTheme.typography.h5,
                        color = MaterialTheme.colors.onBackground
                    )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                        label = { Text("Task Name", color = MaterialTheme.colors.onBackground) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            textColor = MaterialTheme.colors.onBackground,
                            cursorColor = MaterialTheme.colors.primary,
                            focusedBorderColor = MaterialTheme.colors.primary,
                            unfocusedBorderColor = MaterialTheme.colors.onBackground.copy(alpha = 0.5f),
                            focusedLabelColor = MaterialTheme.colors.primary,
                            unfocusedLabelColor = MaterialTheme.colors.onBackground.copy(alpha = 0.5f)
                        )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                        label = { Text("Notes", color = MaterialTheme.colors.onBackground) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            textColor = MaterialTheme.colors.onBackground,
                            cursorColor = MaterialTheme.colors.primary,
                            focusedBorderColor = MaterialTheme.colors.primary,
                            unfocusedBorderColor = MaterialTheme.colors.onBackground.copy(alpha = 0.5f),
                            focusedLabelColor = MaterialTheme.colors.primary,
                            unfocusedLabelColor = MaterialTheme.colors.onBackground.copy(alpha = 0.5f)
                        )
                )
                Spacer(modifier = Modifier.height(16.dp))
                if(dueDate != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                        editingDueDate = true
                        showDatePicker = true
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Due Date: ",
                                color = MaterialTheme.colors.onBackground
                            )
                            Text(
                                dueDate?.toLocalDate()?.toString() ?: "No due date",
                                color = MaterialTheme.colors.primary
                            )
                    }
                }
                else if(scheduledDate == null){
                        Button(
                            onClick = {
                        editingDueDate = true
                        showDatePicker = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = MaterialTheme.colors.primary,
                                contentColor = MaterialTheme.colors.onPrimary
                            )
                        ) {
                        Text("Add Due Date")
                    }
                }
                if(scheduledDate != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                        editingDueDate = false
                        showDatePicker = true
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Scheduled Date: ",
                                color = MaterialTheme.colors.onBackground
                            )
                            Text(
                                scheduledDate?.toLocalDate()?.toString() ?: "No scheduled date",
                                color = MaterialTheme.colors.primary
                            )
                    }
                }
                else if(dueDate == null){
                        Button(
                            onClick = {
                        editingDueDate = false
                        showDatePicker = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = MaterialTheme.colors.primary,
                                contentColor = MaterialTheme.colors.onPrimary
                            )
                        ) {
                        Text("Add Scheduled Date")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Priority",
                        color = MaterialTheme.colors.onBackground
                    )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                        Text(
                            prioritySelected.name,
                            color = MaterialTheme.colors.onBackground
                        )
                        IconButton(
                            onClick = { showPriorityDropdown = true },
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                        Icon(
                            imageVector = getIconByPriority(prioritySelected.value),
                            contentDescription = prioritySelected.name,
                            tint = getColorByPriority(prioritySelected.value)
                        )
                    }
                    DropdownMenu(
                        expanded = showPriorityDropdown,
                        onDismissRequest = { showPriorityDropdown = false },
                    ) {
                        Priority.entries.forEach { priority ->
                                DropdownMenuItem(
                                    onClick = {
                                showPriorityDropdown = false
                                prioritySelected = priority
                                    }
                                ) {
                                Row {
                                        Text(
                                            priority.name,
                                            color = MaterialTheme.colors.onBackground
                                        )
                                    Icon(
                                        imageVector = getIconByPriority(priority.value),
                                        contentDescription = priority.name,
                                        modifier = Modifier.padding(start = 8.dp),
                                        tint = getColorByPriority(priority.value)
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Reminders",
                        color = MaterialTheme.colors.onBackground
                    )
                reminders.forEach { reminder ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                reminder.time.toString(),
                                color = MaterialTheme.colors.onBackground
                            )
                        Spacer(modifier = Modifier.weight(1f))
                            IconButton(
                                onClick = { reminders = reminders - reminder },
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete reminder",
                                    tint = MaterialTheme.colors.error
                                )
                        }
                    }
                }
                    Button(
                        onClick = { /* Show reminder picker */ },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = MaterialTheme.colors.primary,
                            contentColor = MaterialTheme.colors.onPrimary
                        )
                    ) {
                    Text("Add Reminder")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = MaterialTheme.colors.surface,
                                contentColor = MaterialTheme.colors.onSurface
                            )
                        ) {
                        Text("Cancel")
                    }
                        Button(
                            onClick = {
                        if(name.isBlank()) {
                            onError("Task name cannot be empty")
                            return@Button
                        }

                        if (taskToEdit != null) {
                            onUpdateTask(
                                taskToEdit.id!!,
                                taskToEdit.copy(
                                    name = name,
                                    note = notes,
                                    dueDateTime = dueDate?.parseDateTime() ,
                                    scheduledDateTime = scheduledDate?.parseDateTime(),
                                    reminders = reminders,
                                    priority = prioritySelected.value
                                )
                            )
                        } else {
                            onAddTask(
                                name,
                                notes,
                                reminders,
                                dueDate?.parseDateTime(),
                                scheduledDate?.parseDateTime(),
                                prioritySelected.value
                            )
                        }
                        onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = MaterialTheme.colors.primary,
                                contentColor = MaterialTheme.colors.onPrimary
                            )
                        ) {
                        Text("Save")
                        }
                    }
                }
            }
        }
    }
}
