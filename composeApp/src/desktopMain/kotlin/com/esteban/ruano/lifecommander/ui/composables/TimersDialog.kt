package ui.composables

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import models.TimeTypes
import models.TimerModel
import ui.viewmodels.AppViewModel
import utils.DateUtils.getTimeSeparated
import utils.swap

@Composable
fun TimersDialog(
    show: Boolean,
    appViewModel: AppViewModel,
    onDismiss: () -> Unit
) {
    if (show) {
        val timerSelected = remember { mutableStateOf<String?>(null) }
        val isTimersLoopEnabled by appViewModel.isTimersLoopEnabled.collectAsState()
        val state = rememberLazyListState()
        var timers by remember {
            mutableStateOf(
                appViewModel.timers.value
            )
        }
        val coroutineScope = rememberCoroutineScope()

        Dialog(
            onDismissRequest = onDismiss
        ) {
            Surface(
                color = MaterialTheme.colors.background
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                ) {
                    Row {
                        if (timers.isNotEmpty()) {
                            Box(
                                modifier = Modifier.weight(1f).height(200.dp).padding(end = 16.dp).clickable {
                                    timerSelected.value = null
                                }
                            ) {
                                LazyColumn(
                                    modifier = Modifier.border(
                                        width = 1.dp,
                                        color = androidx.compose.ui.graphics.Color.Black
                                    ).padding(8.dp).fillMaxHeight(),
                                    state = state,
                                ) {
                                    items(timers.size) { index ->
                                        val timer = timers[index]
                                        Text(
                                            text = timer.name,
                                            color = if (timerSelected.value == timer.id) MaterialTheme.colors.primary else MaterialTheme.colors.onBackground,
                                            modifier = Modifier.fillMaxWidth().padding(8.dp).clickable {
                                                timerSelected.value = timer.id
                                            }.background(
                                                if (timerSelected.value == timer.id) MaterialTheme.colors.primary.copy(
                                                    0.5f
                                                ) else androidx.compose.ui.graphics.Color.Transparent
                                            )
                                        )
                                    }
                                }
                                VerticalScrollbar(
                                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                                    adapter = rememberScrollbarAdapter(
                                        scrollState = state
                                    )
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier.weight(1f).padding(vertical = 32.dp)
                            ) {
                                Text("No Timers", modifier = Modifier.align(Alignment.Center))
                            }
                        }
                        if (timerSelected.value != null) {
                            val timeSeparated =
                                timers.first { it.id == timerSelected.value!! }.startValue.getTimeSeparated()
                            val hours = timeSeparated[TimeTypes.HOUR] ?: 0L
                            val minutes = timeSeparated[TimeTypes.MINUTE] ?: 0L
                            val seconds = timeSeparated[TimeTypes.SECOND] ?: 0L

                            Column(modifier = Modifier.align(Alignment.CenterVertically).weight(1f)) {
                                OutlinedTextField(
                                    modifier = Modifier.fillMaxWidth(),
                                    value = timers.first { it.id == timerSelected.value!! }.name,
                                    onValueChange = { newValue ->
                                        timers = timers.map {
                                            if (it.id == timerSelected.value!!) {
                                                it.copy(name = newValue)
                                            } else {
                                                it
                                            }
                                        }
                                    },
                                    label = { Text("Name") }
                                )
                                Row {
                                    OutlinedTextField(
                                        modifier = Modifier.weight(1f),
                                        value = hours.toString(),
                                        onValueChange = {
                                            val temp = it.toLongOrNull() ?: hours
                                            val value = temp * 3600 + minutes * 60 + seconds
                                            timers = timers.map { timer ->
                                                if (timer.id == timerSelected.value!!) {
                                                    timer.copy(
                                                        startValue = value,
                                                        timeRemaining = value
                                                    )
                                                } else {
                                                    timer
                                                }
                                            }
                                        },
                                        label = { Text("H") }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    OutlinedTextField(
                                        modifier = Modifier.weight(1f),
                                        value = minutes.toString(),
                                        onValueChange = {
                                            val temp = it.toLongOrNull() ?: minutes
                                            val value = hours * 3600 + temp * 60 + seconds
                                            timers = timers.map { timer ->
                                                if (timer.id == timerSelected.value!!) {
                                                    timer.copy(
                                                        startValue = value,
                                                        timeRemaining = value
                                                    )
                                                } else {
                                                    timer
                                                }
                                            }
                                        },
                                        label = { Text("M") }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    OutlinedTextField(
                                        modifier = Modifier.weight(1f),
                                        value = seconds.toString(),
                                        onValueChange = {
                                            val temp = it.toLongOrNull() ?: seconds
                                            val value = hours * 3600 + minutes * 60 + temp
                                            timers = timers.map { timer ->
                                                if (timer.id == timerSelected.value!!) {
                                                    timer.copy(
                                                        startValue = value,
                                                        timeRemaining = value
                                                    )
                                                } else {
                                                    timer
                                                }
                                            }
                                        },
                                        label = { Text("S") }
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                                    horizontalArrangement = Arrangement.Start,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = timers.firstOrNull { it.id == timerSelected.value }?.showDialog
                                            ?: false,
                                        onCheckedChange = { value ->
                                            coroutineScope.launch {
                                                timers = timers.map {
                                                    if (it.id == timerSelected.value!!) {
                                                        it.copy(showDialog = value)
                                                    } else {
                                                        it
                                                    }
                                                }
                                            }
                                        }
                                    )
                                    Text("Show dialog")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                                    horizontalArrangement = Arrangement.Start,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = timers.firstOrNull { it.id == timerSelected.value }?.isPomodoro
                                            ?: false,
                                        onCheckedChange = { value ->
                                            coroutineScope.launch {
                                                timers = timers.map {
                                                    if (it.id == timerSelected.value!!) {
                                                        it.copy(isPomodoro = value)
                                                    } else {
                                                        it
                                                    }
                                                }
                                            }
                                        },
                                        enabled = !timers.any { it.isPomodoro }
                                    )
                                    Text("Pomodoro Timer")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Row {
                                    Button(
                                        onClick = {
                                            timers = timers.filter { it.id != timerSelected.value!! }
                                            timerSelected.value = null
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            backgroundColor = MaterialTheme.colors.error
                                        )
                                    ) {
                                        Text("Delete", color = androidx.compose.ui.graphics.Color.White)
                                    }
                                    IconButton(
                                        onClick = {
                                            val index = timers.indexOfFirst { it.id == timerSelected.value }
                                            if (index > 0) {
                                                timers = timers.swap(index, index - 1)
                                            }
                                        },
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowUp,
                                            contentDescription = "Move Up"
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            val index = timers.indexOfFirst { it.id == timerSelected.value }
                                            if (index < timers.size - 1) {
                                                timers = timers.swap(index, index + 1)
                                            }
                                        },
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Move Down",
                                        )
                                }
                            }
                            }
                        }
                        }
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isTimersLoopEnabled,
                                onCheckedChange = {
                                    appViewModel.setTimersLoopEnabled(it)
                                }
                            )
                            Text("Loop Timers")
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = timers.any { it.isPomodoro },
                                onCheckedChange = { value ->
                                    coroutineScope.launch {
                                        if (value) {
                                            // Set the last timer as a Pomodoro timer
                                            timers = timers.mapIndexed { index, timer ->
                                                if (index == timers.size - 1) {
                                                    timer.copy(isPomodoro = true)
                                                } else {
                                                    timer.copy(isPomodoro = false)
                                                }
                                            }
                                        } else {
                                            // Clear all Pomodoro flags
                                            timers = timers.map { it.copy(isPomodoro = false) }
                                        }
                                    }
                                }
                            )
                            Text("Last Timer is Pomodoro")
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            Button(onClick = {
                                coroutineScope.launch {
                                    appViewModel.replaceTimers(timers)
                                    onDismiss()
                                }
                            }) {
                                Text("Save")
                            }
                            if (timerSelected.value == null) {
                                Column(
                                    modifier = Modifier.align(Alignment.CenterVertically)
                                ) {
                                    Button(
                                        onClick = {
                                            timers += (
                                                    TimerModel(
                                                        name = "New Timer",
                                                        timeRemaining = 0L,
                                                        showDialog = true,
                                                        startValue = 0L,
                                                        endValue = 0L,
                                                        step = 1000L
                                                    )
                                                    )
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            backgroundColor = MaterialTheme.colors.secondary
                                        ),
                                        modifier = Modifier.padding(end = 24.dp)
                                    ) {
                                        Text("Add Timer", color = androidx.compose.ui.graphics.Color.White)
                                    }
                                }
                            }
                            Button(onClick = onDismiss) {
                                Text("Cancel")
                            }
                        }
                    }

                }
                }
            }
        }
    }



