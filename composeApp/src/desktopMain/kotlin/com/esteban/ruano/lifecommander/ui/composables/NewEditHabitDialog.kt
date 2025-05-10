package ui.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import services.habits.models.Frequency
import services.habits.models.HabitResponse
import utils.DateUIUtils
import utils.DateUIUtils.parseDate
import utils.DateUIUtils.toLocalDateTime
import utils.DateUtils.getTime
import utils.DateUtils.parseDateTime
import utils.DateUtils.toLocalDate
import utils.DateUtils.toLocalDateTime
import utils.DateUtils.toLocalTime

@Composable
fun NewEditHabitDialog(
    habitToEdit: HabitResponse?,
    show: Boolean,
    onDismiss: () -> Unit,
    onAddHabit: (String, String?,Frequency, String) -> Unit,
    onError: (String) -> Unit,
    onUpdateHabit: (String, HabitResponse) -> Unit
) {
    var name by remember { mutableStateOf(habitToEdit?.name ?: "") }
    var notes by remember { mutableStateOf(habitToEdit?.note ?: "") }
    var dateTime by remember { mutableStateOf(habitToEdit?.dateTime?.toLocalDateTime()) }
    var frequency by remember { mutableStateOf(if(habitToEdit!=null) Frequency.fromString(habitToEdit.frequency?:Frequency.DAILY.value) else Frequency.DAILY) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    var editingDueDate by remember { mutableStateOf(false) }
    var showFrequencyDropDownMenu by remember { mutableStateOf(false) }

    LaunchedEffect(habitToEdit){
        println("Habit to edit: $habitToEdit")

        name = habitToEdit?.name ?: ""
        notes = habitToEdit?.note ?: ""
        dateTime = habitToEdit?.dateTime?.toLocalDateTime()
    }

    DatePickerDialog(
        date = dateTime?.toLocalDate()?.parseDate(),
        show = showDatePicker,
        onDismiss = { showDatePicker = false },
        onDateSelected = { selectedDate ->
            try{
                dateTime = selectedDate.toLocalDate().toLocalDateTime()
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
        timep = dateTime?.toLocalTime()?.getTime(),
        show = showTimePicker,
        onDismiss = { showTimePicker = false },
        onTimeSelected = { selectedTime ->
            try {
                selectedTime.toLocalTime()
                dateTime = DateUIUtils.joinDateAndTime(dateTime!!.toLocalDate(), selectedTime)
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
            Column(
                modifier = Modifier.padding(24.dp).wrapContentSize().verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(if (habitToEdit != null) "Edit Habit" else "New Habit")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Habit Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                if(dateTime != null) {
                    Row(modifier = Modifier.fillMaxWidth().clickable {
                        editingDueDate = true
                        showDatePicker = true
                    }) {
                        Text("Due Date: ")
                        Text(dateTime?.toLocalDate()?.toString() ?: "No start date time (Mandatory)")
                    }
                }
                else{
                    Button(onClick = {
                        editingDueDate = true
                        showDatePicker = true
                    }) {
                        Text("Add Start Date Time")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                DropdownMenu(
                    expanded = showFrequencyDropDownMenu,
                    onDismissRequest = { showFrequencyDropDownMenu = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Frequency.entries.forEach { item ->
                        DropdownMenuItem(onClick = {
                            frequency = item
                            showFrequencyDropDownMenu = false
                        }) {
                            Text(item.name)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Button(onClick = {
                        if(name.isBlank()) {
                            onError("Task name cannot be empty")
                            return@Button
                        }

                        if (habitToEdit != null) {
                            onUpdateHabit(
                                habitToEdit.id!!,
                                habitToEdit.copy(
                                    name = name,
                                    note = notes,
                                    frequency = frequency.value,
                                    dateTime = dateTime?.parseDateTime()

                                )
                            )
                        } else {
                            val baseDateTime = dateTime
                            if (baseDateTime == null) {
                                onError("Start date time is mandatory")
                                return@Button
                            }
                            onAddHabit(
                                name,
                                notes,
                                frequency,
                                baseDateTime.parseDateTime()
                            )
                        }
                        onDismiss()
                    }) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
