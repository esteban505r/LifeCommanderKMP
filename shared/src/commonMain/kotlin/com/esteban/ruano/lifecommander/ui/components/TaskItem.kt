package com.esteban.ruano.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esteban.ruano.utils.DateUIUtils.getColorByPriority
import com.esteban.ruano.utils.DateUIUtils.getCurrentDateTime
import com.esteban.ruano.utils.DateUIUtils.getIconByPriority
import com.esteban.ruano.utils.DateUIUtils.toLocalDateTime
import com.esteban.ruano.utils.DateUtils.toResourceStringBasedOnNow
import com.lifecommander.models.Task
import dev.icerock.moko.resources.compose.localized
import kotlinx.datetime.TimeZone

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

    val now = getCurrentDateTime(
        TimeZone.currentSystemDefault()
    )
    val overdue = task.done != true && (
            task.dueDateTime?.toLocalDateTime()?.let { it < now } == true ||
                    task.scheduledDateTime?.toLocalDateTime()?.let { it < now } == true
            )

    val showContextMenu = remember { mutableStateOf(false) }
    val priorityColor = when (task.priority) {
        3, 4, 5 -> Color(0xFFD32F2F) // High - Red
        2 -> Color(0xFFFFA000) // Medium - Orange
        1 -> Color(0xFF388E3C) // Low - Green
        else -> MaterialTheme.colors.primary
    }

    CommonItem(
        modifier = modifier,
        title = task.name,
        isDone = task.done,
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
                // Color-coded priority indicator
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(priorityColor.copy(alpha = 0.15f))
                        .border(
                            width = 2.dp,
                            color = priorityColor,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (task.priority) {
                            3, 4, 5 -> "H"
                            2 -> "M"
                            1 -> "L"
                            else -> "?"
                        },
                        style = MaterialTheme.typography.caption.copy(
                            fontWeight = FontWeight.Bold,
                            color = priorityColor,
                            fontSize = 12.sp
                        )
                    )
                }
                
                // Reschedule button for overdue tasks
                if (overdue && onReschedule != null) {
                    IconButton(
                        onClick = {
                            showContextMenu.value = false
                            onReschedule()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Reschedule",
                            tint = MaterialTheme.colors.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        },
        bottomContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Date information with improved styling
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (task.dueDateTime != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Due:",
                                style = MaterialTheme.typography.caption.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                )
                            )
                            Text(
                                text = task.dueDateTime.toLocalDateTime().toResourceStringBasedOnNow().first.localized(),
                                color = task.dueDateTime.toLocalDateTime().toResourceStringBasedOnNow().second,
                                style = MaterialTheme.typography.caption.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }

                    if (task.scheduledDateTime != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Scheduled:",
                                style = MaterialTheme.typography.caption.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                )
                            )
                            Text(
                                text = task.scheduledDateTime.toLocalDateTime().toResourceStringBasedOnNow().first.localized(),
                                color = task.scheduledDateTime.toLocalDateTime().toResourceStringBasedOnNow().second,
                                style = MaterialTheme.typography.caption.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }
                
                // Priority level text with color coding
                if (task.priority != null && task.priority > 0) {
                    Text(
                        text = when (task.priority) {
                            3, 4, 5 -> "High"
                            2 -> "Medium"
                            1 -> "Low"
                            else -> ""
                        },
                        style = MaterialTheme.typography.caption.copy(
                            fontWeight = FontWeight.Bold,
                            color = priorityColor
                        )
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