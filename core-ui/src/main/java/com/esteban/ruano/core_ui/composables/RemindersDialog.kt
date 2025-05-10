package com.esteban.ruano.core_ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.esteban.ruano.core_ui.utils.ReminderType
import com.esteban.ruano.core_ui.utils.ReminderType.Companion.toResource
import com.esteban.ruano.core_ui.utils.ReminderType.EightHours
import com.esteban.ruano.core_ui.utils.ReminderType.FifteenMinutes
import com.esteban.ruano.core_ui.utils.ReminderType.OneHour
import com.esteban.ruano.core_ui.R

@Composable
fun RemindersDialog(
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit,
){
    var selectedReminderType: ReminderType by remember{
        mutableStateOf(ReminderType.FifteenMinutes)
    }
    val reminderTypes = listOf(
        FifteenMinutes, OneHour, EightHours)

    Dialog(
        onDismissRequest = {
            onDismiss()
        }
    ) {
        Surface(
        ){
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.height(150.dp)
                ) {
//                    val list =  reminderTypes.filter {
//                        it !is ReminderType.Custom
//                    }
                    val list = reminderTypes
                    items(
                       list.size
                    ) {
                        ReminderItem(
                            selected = selectedReminderType == list[it],
                            reminderType = list[it],
                            onSelected = { selected ->
                                selectedReminderType = selected
                            }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            onConfirm(selectedReminderType.time)
                        }
                    ) {
                        Text(text = "Confirm")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onDismiss()
                        }
                    ) {
                        Text(text = "Cancel")
                    }
                }
            }
        }
    }
}

@Composable
fun ReminderItem(
    selected: Boolean,
    reminderType: ReminderType,
    onSelected: (ReminderType) -> Unit
){
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .background(
                if (selected)
                    MaterialTheme.colors.primary.copy(alpha = 0.1f)
                else
                    MaterialTheme.colors.surface
            )
            .fillMaxWidth()
            .padding(16.dp)
            .clickable {
                onSelected(reminderType)
            }
    ) {
        Text(text = "${stringResource(id = reminderType.toResource())} ${stringResource(id = R.string.before)}")
    }
}