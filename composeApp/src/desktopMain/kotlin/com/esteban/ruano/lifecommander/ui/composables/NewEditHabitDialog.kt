package ui.composables

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
                        text = if (habitToEdit != null) "Edit Habit" else "New Habit",
                        style = MaterialTheme.typography.h5,
                        color = MaterialTheme.colors.onBackground
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Habit Name") },
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
                    Text("Date and Time", style = MaterialTheme.typography.subtitle1)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        enabled = true,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = dateTime?.date?.parseDate()?:"Select a date",
                            style = MaterialTheme.typography.body1
                        )
                    }
                    if(showDatePicker) {
                        DialogWindow(
                            onCloseRequest = { showDatePicker = false },
                            state = rememberDialogState(position = WindowPosition(Alignment.BottomCenter))
                        )
                        {
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
                    OutlinedButton(
                        onClick = { showTimePicker = true },
                        enabled = true,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = dateTime?.time?.formatDefault() ?: "Select a time",
                            style = MaterialTheme.typography.body1
                        )
                    }
                    if(showTimePicker) {
                        DialogWindow(
                            onCloseRequest = { showTimePicker = false },
                            state = rememberDialogState(position = WindowPosition(Alignment.BottomCenter))
                        )
                        {
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
                    Text("Frequency", style = MaterialTheme.typography.subtitle1)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Frequency.entries.forEach { frequency ->
                            IconButton(
                                onClick = { frequencySelected = frequency },
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
                                    tint = if (frequencySelected == frequency) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onSurface
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
