package com.esteban.ruano.lifecommander.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import com.esteban.ruano.lifecommander.utils.UiUtils.getColorByPriority
import com.esteban.ruano.lifecommander.utils.UiUtils.getIconByPriority
import com.esteban.ruano.lifecommander.utils.UiUtils.parseHexColor
import androidx.compose.runtime.*
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esteban.ruano.ui.components.CommonItem
import com.esteban.ruano.utils.DateUIUtils.getCurrentDateTime
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
    val overdue = !task.done && (task.dueDateTime?.toLocalDateTime()?.let { it < now } == true ||
            task.scheduledDateTime?.toLocalDateTime()?.let { it < now } == true)

    val showContextMenu = remember { mutableStateOf(false) }
    val priorityColor = getColorByPriority(task.priority ?: 0)
    val priorityIcon = getIconByPriority(task.priority ?: 0)

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
                // Color-coded priority indicator with icon
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
                    Icon(
                        imageVector = priorityIcon,
                        contentDescription = "Priority",
                        tint = priorityColor,
                        modifier = Modifier.size(18.dp)
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
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                text = task.dueDateTime.toLocalDateTime()
                                    .toResourceStringBasedOnNow().first.localized(),
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
                                text = task.scheduledDateTime.toLocalDateTime()
                                    .toResourceStringBasedOnNow().first.localized(),
                                color = task.scheduledDateTime.toLocalDateTime().toResourceStringBasedOnNow().second,
                                style = MaterialTheme.typography.caption.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }
                
                // Tags display
                if (!task.tags.isNullOrEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(task.tags ?: emptyList()) { tag ->
                            TagChip(
                                tag = tag,
                                onClick = { /* Tags are read-only in list view */ },
                                selected = false,
                                modifier = Modifier.height(24.dp)
                            )
                        }
                    }
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