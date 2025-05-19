package ui.composables

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.esteban.ruano.lifecommander.models.Timer
import kotlinx.coroutines.launch
import models.TimeTypes
import utils.DateUtils.getTimeSeparated
import utils.swap

@Composable
fun TimersDialog(
    show: Boolean,
    timersList : List<Timer>,
    onCreate: (timerId:String, name: String, duration: Long, enabled: Boolean, countsAsPomodoro: Boolean, order: Int) -> Unit,
    onUpdate: (
        timerId: String,
        name: String,
        duration: Long,
        enabled: Boolean,
        countsAsPomodoro: Boolean,
        order: Int
    ) -> Unit,
    onDelete: (timerId: String) -> Unit,
    onDismiss: () -> Unit
) {
    if (show) {
        val timerSelected = remember { mutableStateOf<String?>(null) }
        val state = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()
        var timers by remember(timersList) { mutableStateOf(timersList) }

        Dialog(
            onDismissRequest = onDismiss
        ) {
            Surface(
                color = MaterialTheme.colors.background
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                ) {
                    Row(modifier = Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            text = "Timers",
                            style = MaterialTheme.typography.h5,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
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
                            val selectedTimer = timers.first { it.id == timerSelected.value!! }
                            val timeSeparated = selectedTimer.duration.toLong().getTimeSeparated()
                            val hours = timeSeparated[TimeTypes.HOUR] ?: 0L
                            val minutes = timeSeparated[TimeTypes.MINUTE] ?: 0L
                            val seconds = timeSeparated[TimeTypes.SECOND] ?: 0L

                            Column(modifier = Modifier.align(Alignment.CenterVertically).weight(1f)) {
                                OutlinedTextField(
                                    modifier = Modifier.fillMaxWidth(),
                                    value = selectedTimer.name,
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
                                                    timer.copy(duration = value)
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
                                            val value = (hours * 3600 + temp * 60 + seconds) * 1000
                                            timers = timers.map { timer ->
                                                if (timer.id == timerSelected.value!!) {
                                                    timer.copy(duration = value)
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
                                            val value = (hours * 3600 + minutes * 60 + temp) * 1000
                                            timers = timers.map { timer ->
                                                if (timer.id == timerSelected.value!!) {
                                                    timer.copy(duration = value)
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
                                        checked = selectedTimer.enabled,
                                        onCheckedChange = { value ->
                                            coroutineScope.launch {
                                                timers = timers.map {
                                                    if (it.id == timerSelected.value!!) {
                                                        it.copy(enabled = value)
                                                    } else {
                                                        it
                                                    }
                                                }
                                            }
                                        }
                                    )
                                    Text("Enabled")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                                    horizontalArrangement = Arrangement.Start,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = selectedTimer.countsAsPomodoro,
                                        onCheckedChange = { value ->
                                            coroutineScope.launch {
                                                timers = timers.map {
                                                    if (it.id == timerSelected.value!!) {
                                                        it.copy(countsAsPomodoro = value)
                                                    } else {
                                                        it
                                                    }
                                                }
                                            }
                                        },
                                        enabled = !timers.any { it.countsAsPomodoro }
                                    )
                                    Text("Counts as Pomodoro")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Row {
                                    Button(
                                        onClick = {
                                            coroutineScope.launch {
                                                onDelete(selectedTimer.id)
                                                timers = timers.filter { it.id != timerSelected.value!! }
                                                timerSelected.value = null
                                            }
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
                                    Button(onClick = {
                                        coroutineScope.launch {
                                            timers.forEachIndexed { index, timer ->
                                                onUpdate(
                                                    timer.id,
                                                    timer.name,
                                                    timer.duration,
                                                    timer.enabled,
                                                    timer.countsAsPomodoro,
                                                    index
                                                )
                                            }
                                            onDismiss()
                                        }
                                    }) {
                                        Text("Apply")
                                    }

                                }
                            }
                        }
                    }
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            if (timerSelected.value == null) {
                                Column(
                                    modifier = Modifier.align(Alignment.CenterVertically)
                                ) {
                                    Button(
                                        onClick = {
                                            coroutineScope.launch {
                                                onCreate(
                                                    "",
                                                    "New Timer",
                                                    0,
                                                    true,
                                                    false,
                                                    timers.size
                                                )
                                            }
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



