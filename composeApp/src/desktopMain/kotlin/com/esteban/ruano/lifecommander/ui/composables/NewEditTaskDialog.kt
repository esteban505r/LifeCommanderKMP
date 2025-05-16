package ui.composables

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import com.esteban.ruano.ui.datePickerDimensionHeight
import com.esteban.ruano.ui.datePickerDimensionWith
import com.esteban.ruano.ui.timePickerDimensionHeight
import com.esteban.ruano.ui.timePickerDimensionWith
import com.lifecommander.ui.components.CustomDatePicker
import getIconByPriority
import services.tasks.models.Priority
import services.tasks.models.Priority.Companion.toPriority
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import com.esteban.ruano.utils.DateUIUtils.getCurrentDateTime
import com.esteban.ruano.utils.DateUIUtils.toLocalDateTime
import com.esteban.ruano.utils.DateUtils.parseDate
import com.lifecommander.models.Reminder
import com.lifecommander.models.Task
import com.lifecommander.ui.components.CustomTimePicker
import kotlinx.datetime.*
import utils.DateUtils.parseDate
import utils.DateUtils.toLocalDateTime
import java.awt.Dimension

@Composable
fun NewEditTaskDialog(
    taskToEdit: Task?,
    show: Boolean,
    onDismiss: () -> Unit,
    onAddTask: (String, String, List<Reminder>, String?, String?, Int?) -> Unit,
    onError: (String) -> Unit,
    onUpdateTask: (String, Task) -> Unit
) {
    var name by remember { mutableStateOf(taskToEdit?.name ?: "") }
    var notes by remember { mutableStateOf(taskToEdit?.note ?: "") }
    var dueDate by remember { mutableStateOf(taskToEdit?.dueDateTime?.toLocalDateTime()) }
    var scheduledDate by remember { mutableStateOf(taskToEdit?.scheduledDateTime?.toLocalDateTime()) }
    var reminders by remember { mutableStateOf(taskToEdit?.reminders ?: emptyList()) }
    var prioritySelected by remember { mutableStateOf(taskToEdit?.priority?.toPriority() ?: Priority.MEDIUM) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    LaunchedEffect(taskToEdit) {
        name = taskToEdit?.name ?: ""
        notes = taskToEdit?.note ?: ""
        dueDate = taskToEdit?.dueDateTime?.toLocalDateTime()
        scheduledDate = taskToEdit?.scheduledDateTime?.toLocalDateTime()
        reminders = taskToEdit?.reminders ?: emptyList()
        prioritySelected = taskToEdit?.priority?.toPriority() ?: Priority.MEDIUM
    }

    if (show) {
        DialogWindow(
            onCloseRequest = onDismiss,
            state = rememberDialogState(position = WindowPosition(Alignment.Center))
        ) {
            LaunchedEffect(Unit) {
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
                        text = if (taskToEdit != null) "Edit Task" else "New Task",
                        style = MaterialTheme.typography.h5,
                        color = MaterialTheme.colors.onBackground
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Task Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Due Date", style = MaterialTheme.typography.subtitle1)
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        enabled = true,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = dueDate?.date?.parseDate()?:"Select a date",
                            style = MaterialTheme.typography.body1
                        )
                    }
                    if(showDatePicker) {
                       DialogWindow(
                            onCloseRequest = { showDatePicker = false },
                            state = rememberDialogState(position = WindowPosition(Alignment.Center))
                        ) {
                           LaunchedEffect(Unit) {
                               window.size = Dimension(datePickerDimensionWith, datePickerDimensionHeight)
                           }
                            CustomDatePicker(
                                selectedDate = dueDate?.date ?: getCurrentDateTime().date,
                                onDateSelected = { dueDate = it.atTime(dueDate?.time ?: LocalTime(0, 0)) },
                                modifier = Modifier.fillMaxWidth(),
                                onDismiss = { showDatePicker = false }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { showTimePicker = true },
                        enabled = true,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = dueDate?.time?.formatDefault() ?: "Select a time",
                            style = MaterialTheme.typography.body1
                        )
                    }
                    if(showTimePicker) {
                        DialogWindow(
                            onCloseRequest = { showTimePicker = false },
                            state = rememberDialogState(position = WindowPosition(Alignment.Center))
                        )
                        {
                            LaunchedEffect(Unit) {
                                window.size = Dimension(timePickerDimensionWith, timePickerDimensionHeight)
                            }
                            CustomTimePicker(
                                selectedTime = dueDate?.time ?: LocalTime(0, 0),
                                onTimeSelected = { dueDate = dueDate?.date?.atTime(it) },
                                modifier = Modifier.fillMaxWidth(),
                                onDismiss = { showTimePicker = false }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Priority", style = MaterialTheme.typography.subtitle1)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Priority.entries.forEach { priority ->
                            IconButton(
                                onClick = { prioritySelected = priority },
                            ) {
                                Icon(
                                    imageVector = getIconByPriority(priority.value),
                                    contentDescription = priority.name,
                                    tint = if (prioritySelected == priority) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onSurface
                                )
                            }
                        }
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
                                if (name.isBlank()) {
                                    onError("Task name cannot be empty")
                                    return@Button
                                }

                                if (taskToEdit != null) {
                                    onUpdateTask(
                                        taskToEdit.id!!,
                                        taskToEdit.copy(
                                            name = name,
                                            note = notes,
                                            dueDateTime = dueDate?.toString(),
                                            scheduledDateTime = scheduledDate?.toString(),
                                            reminders = reminders,
                                            priority = prioritySelected.value
                                        )
                                    )
                                } else {
                                    onAddTask(
                                        name,
                                        notes,
                                        reminders,
                                        dueDate?.toString(),
                                        scheduledDate?.toString(),
                                        prioritySelected.value
                                    )
                                }
                                onDismiss()
                            }
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}
