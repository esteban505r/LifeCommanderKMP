package ui.composables

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color

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
                window.size = Dimension(600, 650)
            }
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .background(androidx.compose.ui.graphics.Color.Transparent),
                color = androidx.compose.ui.graphics.Color.Transparent
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    elevation = 8.dp,
                    backgroundColor = MaterialTheme.colors.surface
                ) {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(0.dp)
                    ) {
                        // Professional Header
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colors.primary)
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = if (taskToEdit != null) "Edit Task" else "Create New Task",
                                        style = MaterialTheme.typography.h6,
                                        color = MaterialTheme.colors.onPrimary,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                    )
                                    Text(
                                        text = if (taskToEdit != null) "Update your task details" else "Set up a new task to track",
                                        style = MaterialTheme.typography.body2,
                                        color = MaterialTheme.colors.onPrimary.copy(alpha = 0.8f)
                                    )
                                }
                                IconButton(
                                    onClick = onDismiss,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = MaterialTheme.colors.onPrimary
                                    )
                                }
                            }
                        }
                        // Content
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            // Task Name
                            Column {
                                Text(
                                    text = "Task Name",
                                    style = MaterialTheme.typography.subtitle1,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                                    color = MaterialTheme.colors.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = name,
                                    onValueChange = { name = it },
                                    placeholder = { Text("e.g., Buy groceries, Call John") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        focusedBorderColor = MaterialTheme.colors.primary,
                                        unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.2f)
                                    ),
                                    singleLine = true
                                )
                            }
                            // Notes
                            Column {
                                Text(
                                    text = "Notes (Optional)",
                                    style = MaterialTheme.typography.subtitle1,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                                    color = MaterialTheme.colors.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = notes,
                                    onValueChange = { notes = it },
                                    placeholder = { Text("Add any additional details about your task") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        focusedBorderColor = MaterialTheme.colors.primary,
                                        unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.2f)
                                    ),
                                    maxLines = 3
                                )
                            }
                            // Due Date & Time
                            Column {
                                Text(
                                    text = "Due Date & Time",
                                    style = MaterialTheme.typography.subtitle1,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                                    color = MaterialTheme.colors.onSurface
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { showDatePicker = true },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colors.primary
                                        )
                                    ) {
                                        Icon(Icons.Default.Today, contentDescription = "Date", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = dueDate?.date?.parseDate() ?: "Select Date", style = MaterialTheme.typography.body2)
                                    }
                                    OutlinedButton(
                                        onClick = { showTimePicker = true },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colors.primary
                                        )
                                    ) {
                                        Icon(Icons.Default.Schedule, contentDescription = "Time", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = dueDate?.time?.formatDefault() ?: "Select Time", style = MaterialTheme.typography.body2)
                                    }
                                }
                            }
                            // Priority
                            Column {
                                Text(
                                    text = "Priority",
                                    style = MaterialTheme.typography.subtitle1,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                                    color = MaterialTheme.colors.onSurface
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Priority.entries.forEach { priority ->
                                        val isSelected = prioritySelected == priority
                                        val (bgColor, iconColor, textColor, borderColor) = when (priority) {
                                            Priority.HIGH ->
                                                if (isSelected) listOf(Color(0xFFD32F2F), Color.White, Color.White, Color(0xFFD32F2F))
                                                else listOf(Color(0x1AD32F2F), Color(0xFFD32F2F), Color(0xFFD32F2F), Color.Transparent)
                                            Priority.MEDIUM ->
                                                if (isSelected) listOf(Color(0xFFFFA000), Color.White, Color.White, Color(0xFFFFA000))
                                                else listOf(Color(0x1AFFA000), Color(0xFFFFA000), Color(0xFFFFA000), Color.Transparent)
                                            Priority.LOW ->
                                                if (isSelected) listOf(Color(0xFF388E3C), Color.White, Color.White, Color(0xFF388E3C))
                                                else listOf(Color(0x1A388E3C), Color(0xFF388E3C), Color(0xFF388E3C), Color.Transparent)
                                            else ->
                                                if (isSelected) listOf(MaterialTheme.colors.primary, Color.White, Color.White, MaterialTheme.colors.primary)
                                                else listOf(MaterialTheme.colors.surface, MaterialTheme.colors.primary, MaterialTheme.colors.primary, Color.Transparent)
                                        }
                                        Card(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { prioritySelected = priority },
                                            backgroundColor = bgColor,
                                            elevation = if (isSelected) 2.dp else 0.dp,
                                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, borderColor) else null
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Icon(
                                                    imageVector = getIconByPriority(priority.value),
                                                    contentDescription = priority.name,
                                                    tint = iconColor,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = priority.name.lowercase().replaceFirstChar { it.uppercase() },
                                                    style = MaterialTheme.typography.caption,
                                                    fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Medium else androidx.compose.ui.text.font.FontWeight.Normal,
                                                    color = textColor
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        // Action Buttons
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
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
                                            taskToEdit.id,
                                            taskToEdit.copy(
                                                name = name,
                                                note = notes,
                                                dueDateTime = dueDate?.formatDefault(),
                                                scheduledDateTime = scheduledDate?.formatDefault(),
                                                reminders = reminders,
                                                priority = prioritySelected.value
                                            )
                                        )
                                    } else {
                                        onAddTask(
                                            name,
                                            notes,
                                            reminders,
                                            dueDate?.formatDefault(),
                                            scheduledDate?.formatDefault(),
                                            prioritySelected.value
                                        )
                                    }
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = MaterialTheme.colors.primary
                                )
                            ) {
                                Icon(Icons.Default.Save, contentDescription = "Save", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Save Task")
                            }
                        }
                    }
                    // Date Picker Dialog
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
                    // Time Picker Dialog
                    if(showTimePicker) {
                        DialogWindow(
                            onCloseRequest = { showTimePicker = false },
                            state = rememberDialogState(position = WindowPosition(Alignment.Center))
                        ) {
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
                }
            }
        }
    }
}
