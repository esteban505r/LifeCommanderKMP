package com.esteban.ruano.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.esteban.ruano.models.Task
import com.esteban.ruano.utils.DateUIUtils.getColorByPriority
import com.esteban.ruano.utils.DateUIUtils.getCurrentDateTime
import com.esteban.ruano.utils.DateUIUtils.getIconByPriority
import com.esteban.ruano.utils.DateUIUtils.toLocalDateTime
import com.esteban.ruano.utils.DateUtils.toResourceStringBasedOnNow
import dev.icerock.moko.resources.compose.localized

@Composable
fun TaskItem(
    task: Task,
    interactionSource: MutableInteractionSource,
    isHovered: Boolean = false,
    onCheckedChange: (Task, Boolean) -> Unit,
    onClick: (Task) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onReschedule: (() -> Unit)? = null,
    textDecoration: TextDecoration = TextDecoration.None,
    modifier: Modifier = Modifier,
    itemWrapper: @Composable (content: @Composable () -> Unit) -> Unit = { content -> content() }
) {
    val resources = task.dueDateTime?.toLocalDateTime()
        ?.toResourceStringBasedOnNow()
        ?: task.scheduledDateTime?.toLocalDateTime()?.toResourceStringBasedOnNow()

    val now = getCurrentDateTime()
    val overdue = task.done != true && (
            task.dueDateTime?.toLocalDateTime()?.let { it < now } == true ||
                    task.scheduledDateTime?.toLocalDateTime()?.let { it < now } == true
            )

    val showContextMenu = remember { mutableStateOf(false) }
    val priorityColor = getColorByPriority(task.priority)

    CommonItem(
        modifier = modifier,
        title = task.name,
        isDone = task.done == true,
        isHovered = isHovered,
        interactionSource = interactionSource,
        borderColor = priorityColor,
        textDecoration = textDecoration,
        onCheckedChange = { checked -> onCheckedChange(task, checked) },
        onClick = { onClick(task) },
        onLongClick = { showContextMenu.value = true },
        showContextMenu = showContextMenu.value,
        onDismissContextMenu = { showContextMenu.value = false },
        rightContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (overdue && onReschedule != null) {
                    IconButton(
                        onClick = {
                            showContextMenu.value = false
                            onReschedule()
                        },
                    ){
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Reschedule",
                            tint = MaterialTheme.colors.error,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Icon(
                    imageVector = getIconByPriority(task.priority),
                    contentDescription = null,
                    tint = priorityColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        bottomContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Left: Due date
                if (task.dueDateTime != null) {
                    Text(
                        text = "Due: ${task.dueDateTime.toLocalDateTime().toResourceStringBasedOnNow().first.localized()}",
                        color = task.dueDateTime.toLocalDateTime().toResourceStringBasedOnNow().second,
                        style = MaterialTheme.typography.body2
                    )
                }

                // Right: Scheduled date
                if (task.scheduledDateTime != null) {
                    Text(
                        text = "Scheduled: ${task.scheduledDateTime.toLocalDateTime().toResourceStringBasedOnNow().first.localized()}",
                        color = task.scheduledDateTime.toLocalDateTime().toResourceStringBasedOnNow().second,
                        style = MaterialTheme.typography.body2
                    )
                }
            }
        },
        contextMenuContent = {
            DropdownMenuItem(onClick = {
                showContextMenu.value = false
                onEdit()
            }) {
                Text("Edit")
            }
            DropdownMenuItem(onClick = {
                showContextMenu.value = false
                onDelete()
            }) {
                Text("Delete")
            }
            if (overdue && onReschedule != null) {
                DropdownMenuItem(onClick = {
                    showContextMenu.value = false
                    onReschedule()
                }) {
                    Text("Reschedule")
                }
            }
        },
        itemWrapper = itemWrapper
    )
}