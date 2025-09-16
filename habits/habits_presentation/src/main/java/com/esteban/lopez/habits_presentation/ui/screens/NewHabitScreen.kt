package com.esteban.ruano.habits_presentation.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.esteban.ruano.core.utils.DateUtils.parseDateTime
import com.esteban.ruano.lifecommander.ui.components.AppBar
import com.esteban.ruano.lifecommander.ui.components.GeneralOutlinedTextField
import com.esteban.ruano.lifecommander.ui.components.ToggleButtons
import com.esteban.ruano.lifecommander.ui.components.text.TitleH3
import com.esteban.ruano.core_ui.utils.DateUIUtils.formatTime
import com.esteban.ruano.core_ui.utils.DateUIUtils.parseTime
import com.esteban.ruano.core_ui.utils.DateUIUtils.toLocalDate
import com.esteban.ruano.core_ui.utils.DateUIUtils.toMillis
import com.esteban.ruano.core_ui.utils.toResourceString
import com.esteban.ruano.core_ui.R
import com.esteban.ruano.lifecommander.ui.components.RemindersDialog
import com.esteban.ruano.core_ui.utils.DateUIUtils
import com.esteban.ruano.core_ui.utils.DateUIUtils.getTime
import com.esteban.ruano.core_ui.utils.DateUIUtils.toLocalDateTime
import com.esteban.ruano.core_ui.utils.ReminderType
import com.lifecommander.models.Frequency
import com.esteban.ruano.lifecommander.models.HabitReminder
import com.esteban.ruano.habits_presentation.ui.composables.HabitReminderItem
import com.esteban.ruano.habits_presentation.ui.intent.HabitIntent
import com.esteban.ruano.habits_presentation.ui.screens.viewmodel.state.HabitDetailState
import com.esteban.ruano.habits_presentation.ui.utils.FrequencyUtils
import com.esteban.ruano.ui.Gray
import com.esteban.ruano.ui.Gray2
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewHabitScreen(
    onClose: () -> Unit,
    state: HabitDetailState,
    userIntent: (HabitIntent) -> Unit
){

    val coroutineScope = rememberCoroutineScope()
    val habitToEdit = state.habit

    var name by remember {
        mutableStateOf(habitToEdit?.name ?: "")}

    var frequency by remember {
        mutableStateOf(habitToEdit?.frequency ?: Frequency.DAILY.value)
    }

    var startDate by remember {
        mutableStateOf(habitToEdit?.dateTime?.toLocalDateTime()?.toLocalDate()?: LocalDate.now())
    }

    var startTime by remember {
        mutableStateOf(
            habitToEdit?.dateTime?.toLocalDateTime()?.getTime() ?: LocalDateTime.now().parseTime()
        )
    }

    var reminders by remember {
        mutableStateOf(habitToEdit?.reminders ?: emptyList())
    }

    var showDatePicker by remember {
        mutableStateOf(false)
    }

    var showTimePicker by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        if(habitToEdit == null){
            reminders = reminders + HabitReminder(
                time = ReminderType.FifteenMinutes.time
            )
        }
    }

    val startDateState = rememberDatePickerState(
        initialSelectedDateMillis = startDate.toMillis(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis >= System.currentTimeMillis().toLocalDate().atStartOfDay()
                    .toLocalDate().toMillis()
            }
        }
    )

    val startTimeState = rememberTimePickerState(
        initialHour = startTime.split(":")[0].toInt(),
        initialMinute = startTime.split(":")[1].toInt()
    )

    var showRemindersDialog by remember {
        mutableStateOf(false)
    }

    if (showDatePicker) {
        DatePickerDialog(onDismissRequest = { showDatePicker = false }, confirmButton = {
            TextButton(onClick = {
                startDate = startDateState.selectedDateMillis?.toLocalDate()
                showDatePicker = false
            }) {
                Text("Ok")
            }
        }) {
            DatePicker(state = startDateState)
        }
   }

    if(showRemindersDialog){
        RemindersDialog(
            onDismiss = {
                showRemindersDialog = false
            },
            onConfirm = {
                reminders = reminders + HabitReminder(
                    time = it
                )
                showRemindersDialog = false
            }
        )
    }

    if (showTimePicker) {
        Dialog(onDismissRequest = { showTimePicker = false }) {
            Surface(shape = RoundedCornerShape(16.dp)) {
                Column(horizontalAlignment = Alignment.End,modifier = Modifier.padding(24.dp), ) {
                    TimePicker(state = startTimeState)
                    TextButton(onClick = {
                        startTime = formatTime(startTimeState.hour, startTimeState.minute)
                        showTimePicker = false
                    }) {
                        Text("Ok")
                    }
                }
            }
        }
    }


    val notes = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .padding(top = 24.dp, start = 16.dp, end = 16.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        AppBar(stringResource(id = R.string.new_habit_title), onClose = onClose) {
            TextButton(onClick = {
                coroutineScope.launch {
                    if (habitToEdit != null) {
                        userIntent(
                            HabitIntent.UpdateHabit(
                                habitToEdit.id!!,
                                habitToEdit.copy(
                                    name = name,
                                    note = notes.value,
                                    frequency = frequency,
                                    dateTime =  DateUIUtils.joinDateAndTime(startDate, startTime).parseDateTime(),
                                    reminders = reminders
                                )
                                )
                        )
                    } else {
                        userIntent(
                            HabitIntent.AddHabit(
                                name = name,
                                note = notes.value,
                                dateTime = DateUIUtils.joinDateAndTime(startDate, startTime).parseDateTime(),
                                frequency = frequency,
                                reminders = reminders
                            )
                        )
                    }
                    onClose()
                }
            }) {
                Text(stringResource(id = R.string.save))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        GeneralOutlinedTextField(
            value = name,
            placeHolder = stringResource(id = R.string.habit_name)
        ) {
            name = it
        }
        Spacer(modifier = Modifier.height(24.dp))
        TitleH3(R.string.frequency)
        Spacer(modifier = Modifier.height(16.dp))
        ToggleButtons(
            selectedIndex = Frequency.entries.filter {
                it != Frequency.ONE_TIME
            }.indexOfFirst { it.value == frequency },
            buttons = Frequency.entries.filter {
                it != Frequency.ONE_TIME
            }, onCheckedChange = {
                frequency = it.value
            }, toString = { FrequencyUtils.getResourceByFrequency(frequency = it) })
        Spacer(modifier = Modifier.height(16.dp))
        TitleH3(R.string.reminders)
        Column {
            reminders.forEach { it ->
                HabitReminderItem(
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
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier
            .fillMaxWidth()
            .clickable {
                showDatePicker = true
            }
            .padding(vertical = 16.dp)) {
            Text(stringResource(id = R.string.start_date))
            Text(startDate.toResourceString())
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier
            .fillMaxWidth()
            .clickable {
                showTimePicker = true
            }
            .padding(vertical = 16.dp)) {
            Text(stringResource(id = R.string.start_time))
            Text(startTime)
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
        Spacer(modifier = Modifier.height(32.dp))
    }
}

