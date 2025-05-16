package com.lifecommander.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTimePicker(
    selectedTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
) {
    val timePickerState = rememberTimePickerState(
        initialHour = selectedTime.hour,
        initialMinute = selectedTime.minute
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        TimePicker(state = timePickerState)
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = {
                    val hour = timePickerState.hour
                    val minute = timePickerState.minute
                    onTimeSelected(LocalTime(hour, minute))
                    onDismiss()
                }
            ) {
                Text("OK")
            }
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        }
    }
} 