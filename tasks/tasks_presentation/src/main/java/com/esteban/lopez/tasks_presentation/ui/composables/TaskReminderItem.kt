package com.esteban.ruano.tasks_presentation.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.esteban.ruano.core_ui.R
import com.esteban.ruano.core_ui.utils.ReminderType.Companion.toReminderType
import com.esteban.ruano.core_ui.utils.ReminderType.Companion.toResource
import com.esteban.ruano.ui.SoftRed
import com.lifecommander.models.Reminder

@Composable
fun TaskReminderItem(
    withActions: Boolean = false,
    reminder: Reminder,
    onDeleteReminder: ((Reminder) -> Unit)? = null
){
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ){
        Row{
            Icon(
                Icons.Default.WatchLater,
                contentDescription = "Reminder",
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                "${
                    stringResource(
                        id = reminder.time.toReminderType().toResource()
                    )
                } ${stringResource(id = R.string.before)}"
            )
        }
        Row{
            Spacer(modifier = Modifier.width(16.dp))
            if(withActions){
                IconButton(
                    onClick = {
                        onDeleteReminder?.invoke(reminder)
                    },
                ){
                    Icon(
                        Icons.Default.Delete,
                        "Delete",
                        tint = SoftRed
                    )
                }
            }
        }
    }
}