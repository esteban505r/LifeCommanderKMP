package ui.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import com.lifecommander.models.Frequency
import com.lifecommander.models.Habit
import com.esteban.ruano.ui.datePickerDimensionHeight
import com.esteban.ruano.ui.datePickerDimensionWith
import com.esteban.ruano.ui.timePickerDimensionHeight
import com.esteban.ruano.ui.timePickerDimensionWith
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import com.esteban.ruano.utils.DateUIUtils.getCurrentDateTime
import com.esteban.ruano.utils.DateUIUtils.toLocalDateTime
import com.esteban.ruano.utils.DateUtils.parseDate
import com.esteban.ruano.utils.DateUtils.parseDateTime
import com.lifecommander.ui.components.CustomDatePicker
import com.lifecommander.ui.components.CustomTimePicker
import kotlinx.datetime.*
import java.awt.Dimension

@Composable
fun NewEditHabitDialog(
    habitToEdit: Habit?,
    show: Boolean,
    onDismiss: () -> Unit,
    onAddHabit: (String, String, String, Frequency) -> Unit,
    onError: (String) -> Unit,
    onUpdateHabit: (String, Habit) -> Unit
) {
    var name by remember { mutableStateOf(habitToEdit?.name ?: "") }
    var notes by remember { mutableStateOf(habitToEdit?.note ?: "") }
    var dateTime by remember { mutableStateOf(habitToEdit?.dateTime?.toLocalDateTime()) }
    var frequencySelected by remember { mutableStateOf(habitToEdit?.frequency?.let { Frequency.fromString(it) } ?: Frequency.DAILY) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    LaunchedEffect(habitToEdit) {
        name = habitToEdit?.name ?: ""
        notes = habitToEdit?.note ?: ""
        dateTime = habitToEdit?.dateTime?.toLocalDateTime() ?: getCurrentDateTime()
        frequencySelected = habitToEdit?.frequency?.let { Frequency.fromString(it) } ?: Frequency.DAILY
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
                    .background(Color.Transparent),
                color = Color.Transparent
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
                        // Improved Header (no rounded corners)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colors.primary
                                )
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = if (habitToEdit != null) "Edit Habit" else "Create New Habit",
                                        style = MaterialTheme.typography.h6,
                                        color = MaterialTheme.colors.onPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (habitToEdit != null) "Update your habit details" else "Set up a new habit to track",
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
                            // Habit Name Section
                            Column {
                                Text(
                                    text = "Habit Name",
                                    style = MaterialTheme.typography.subtitle1,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colors.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = name,
                                    onValueChange = { name = it },
                                    placeholder = { Text("e.g., Morning Exercise, Read 30 minutes") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        focusedBorderColor = MaterialTheme.colors.primary,
                                        unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.2f)
                                    ),
                                    singleLine = true
                                )
                            }

                            // Notes Section
                            Column {
                                Text(
                                    text = "Notes (Optional)",
                                    style = MaterialTheme.typography.subtitle1,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colors.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = notes,
                                    onValueChange = { notes = it },
                                    placeholder = { Text("Add any additional details about your habit") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        focusedBorderColor = MaterialTheme.colors.primary,
                                        unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.2f)
                                    ),
                                    maxLines = 3
                                )
                            }

                            // Date and Time Section
                            Column {
                                Text(
                                    text = "Schedule",
                                    style = MaterialTheme.typography.subtitle1,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colors.onSurface
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Date Button
                                    OutlinedButton(
                                        onClick = { showDatePicker = true },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colors.primary
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Today,
                                            contentDescription = "Date",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = dateTime?.date?.parseDate() ?: "Select Date",
                                            style = MaterialTheme.typography.body2
                                        )
                                    }

                                    // Time Button
                                    OutlinedButton(
                                        onClick = { showTimePicker = true },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colors.primary
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Schedule,
                                            contentDescription = "Time",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = dateTime?.time?.formatDefault() ?: "Select Time",
                                            style = MaterialTheme.typography.body2
                                        )
                                    }
                                }
                            }

                            // Frequency Section
                            Column {
                                Text(
                                    text = "Frequency",
                                    style = MaterialTheme.typography.subtitle1,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colors.onSurface
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Frequency.entries.forEach { frequency ->
                                        val isSelected = frequencySelected == frequency
                                        Card(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable {
                                                    frequencySelected = frequency
                                                },
                                            backgroundColor = if (isSelected) 
                                                MaterialTheme.colors.primary.copy(alpha = 0.1f) 
                                            else 
                                                MaterialTheme.colors.surface,
                                            elevation = if (isSelected) 2.dp else 0.dp,
                                            border = if (isSelected) 
                                                BorderStroke(1.dp, MaterialTheme.colors.primary) 
                                            else null,
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Icon(
                                                    imageVector = when (frequency) {
                                                        Frequency.DAILY -> Icons.Default.Today
                                                        Frequency.WEEKLY -> Icons.Default.DateRange
                                                        Frequency.MONTHLY -> Icons.Default.CalendarMonth
                                                        Frequency.YEARLY -> Icons.Default.CalendarViewDay
                                                        else -> Icons.Default.Today
                                                    },
                                                    contentDescription = frequency.name,
                                                    tint = if (isSelected) 
                                                        MaterialTheme.colors.primary 
                                                    else 
                                                        MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = frequency.name.lowercase().capitalize(),
                                                    style = MaterialTheme.typography.caption,
                                                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                                    color = if (isSelected) 
                                                        MaterialTheme.colors.primary 
                                                    else 
                                                        MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
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
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Cancel")
                            }
                            
                            Button(
                                onClick = {
                                    if (name.isBlank()) {
                                        onError("Habit name cannot be empty")
                                        return@Button
                                    }

                                    if(dateTime == null) {
                                        onError("Date and time cannot be empty")
                                        return@Button
                                    }

                                    if (habitToEdit != null) {
                                        onUpdateHabit(
                                            habitToEdit.id!!,
                                            habitToEdit.copy(
                                                name = name,
                                                note = notes,
                                                dateTime = dateTime?.parseDateTime(),
                                                frequency = frequencySelected.value
                                            )
                                        )
                                    } else {
                                        onAddHabit(
                                            name,
                                            notes,
                                            dateTime!!.parseDateTime(),
                                            frequencySelected
                                        )
                                    }
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = MaterialTheme.colors.primary
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    if (habitToEdit != null) Icons.Default.Save else Icons.Default.Add,
                                    contentDescription = "Save",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (habitToEdit != null) "Update Habit" else "Create Habit")
                            }
                        }
                    }
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
                    selectedDate = dateTime?.date ?: getCurrentDateTime().date,
                    onDateSelected = { dateTime = it.atTime(dateTime?.time ?: LocalTime(0, 0)) },
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
                    selectedTime = dateTime?.time ?: LocalTime(0, 0),
                    onTimeSelected = { dateTime = dateTime?.date?.atTime(it) },
                    modifier = Modifier.fillMaxWidth(),
                    onDismiss = { showTimePicker = false }
                )
            }
        }
    }
}
