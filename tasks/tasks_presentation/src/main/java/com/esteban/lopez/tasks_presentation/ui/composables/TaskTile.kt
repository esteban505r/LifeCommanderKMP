package com.esteban.ruano.tasks_presentation.ui.composables

import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Done
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esteban.ruano.core_ui.R
import com.esteban.ruano.core_ui.theme.DarkGray2
import com.esteban.ruano.core_ui.theme.Gray3
import com.esteban.ruano.core_ui.utils.DateUIUtils.toLocalDateTime
import com.esteban.ruano.core_ui.utils.DateUIUtils.toResourceStringBasedOnNow
import com.lifecommander.models.Task

@Composable
fun TaskTile(task: Task) {
    val context = LocalContext.current
    Row(
        horizontalArrangement = SpaceBetween,
        verticalAlignment = CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = task.name ?: stringResource(id = R.string.no_name),
                style = MaterialTheme.typography.subtitle1.copy(color = Gray3),
            )
            if (task.note?.isNotEmpty() == true) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = task.note ?: stringResource(id = R.string.no_note),
                    style = MaterialTheme.typography.subtitle2.copy(color = Color.Gray)
                )
            }
        }
        if (task.done == false) {
            val resources = task.scheduledDateTime?.toLocalDateTime()
                ?.toResourceStringBasedOnNow(
                    LocalContext.current
                )
                ?: task.dueDateTime?.toLocalDateTime()
                    ?.toResourceStringBasedOnNow(
                        LocalContext.current
                    )
            Text(
                text = when {
                    task.scheduledDateTime != null -> "\u23F3 ${resources?.first ?: ""}"
                    task.dueDateTime != null -> "\uD83D\uDD50 ${resources?.first ?: ""}"
                    else -> resources?.first ?: ""
                },
                style = MaterialTheme.typography.subtitle2.copy(
                    color = resources?.second?: DarkGray2, fontSize = 18.sp
                ),
                modifier = Modifier.weight(1f)
            )
        }
        if (task.done == true) {
            Icon(
                imageVector = Icons.Rounded.Done,
                contentDescription = "Done",
                tint = Color.Green,
                modifier = Modifier.weight(1f)
            )
        }
    }
}