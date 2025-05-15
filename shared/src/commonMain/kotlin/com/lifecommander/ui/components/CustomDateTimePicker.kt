package com.lifecommander.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.*

@Composable
fun CustomDateTimePicker(
    selectedDateTime: LocalDateTime,
    onDateTimeSelected: (LocalDateTime) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    enabled: Boolean = true
) {
    Column(modifier = modifier) {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
        
        CustomDatePicker(
            selectedDate = selectedDateTime.date,
            onDateSelected = { newDate ->
                onDateTimeSelected(
                    LocalDateTime(
                        newDate.year,
                        newDate.month,
                        newDate.dayOfMonth,
                        selectedDateTime.hour,
                        selectedDateTime.minute
                    )
                )
            },
            enabled = enabled
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        CustomTimePicker(
            selectedTime = selectedDateTime.time,
            onTimeSelected = { newTime ->
                onDateTimeSelected(
                    LocalDateTime(
                        selectedDateTime.year,
                        selectedDateTime.month,
                        selectedDateTime.dayOfMonth,
                        newTime.hour,
                        newTime.minute
                    )
                )
            },
            enabled = enabled
        )
    }
} 