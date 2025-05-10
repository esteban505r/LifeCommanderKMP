package ui.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ContextMenuDataProvider
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import getColorByPriority
import getIconByPriority
import services.tasks.models.TaskResponse
import toResourceStringBasedOnNow
import utils.DateUIUtils.toLocalDateTime
import utils.DateUtils.toLocalDateTime
import java.time.LocalDateTime

@Composable
fun TaskCard(
    task: TaskResponse,
    onCompleteChange: (String,Boolean) -> Unit,
    currentTime: LocalDateTime,
    onEdit: (TaskResponse) -> Unit,
    onDelete: (String) -> Unit,
    onReschedule: (TaskResponse) -> Unit,
    isEnabled: Boolean = true,
) {
    val resources = task.dueDateTime?.toLocalDateTime()
        ?.toResourceStringBasedOnNow() ?: task.scheduledDateTime?.toLocalDateTime()?.toResourceStringBasedOnNow()

    val isOverdue = task.dueDateTime?.toLocalDateTime()?.isBefore(currentTime) == true && !task.done!!

    ContextMenuDataProvider(
        items = {
            listOf(
                ContextMenuItem("Edit") { onEdit(task) },
                ContextMenuItem("Delete") { onDelete(task.id) },
                if (isOverdue) ContextMenuItem("Reschedule") { onReschedule(task) } else null
            ).filterNotNull()
        }
    ) {
        SelectionContainer {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                elevation = 4.dp,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, resources?.second?.copy(alpha = 0.6f)?:Color.Transparent),
                backgroundColor = if (!isEnabled) MaterialTheme.colors.surface.copy(alpha = 0.5f) else MaterialTheme.colors.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Completion Checkbox and Task Text
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (!isEnabled) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                            Checkbox(
                                checked = task.done == true,
                                onCheckedChange = {
                                    onCompleteChange(task.id, it)
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colors.primary,
                                    uncheckedColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                )
                            )
                            }
                            Text(
                                text = task.name ?: "",
                                modifier = Modifier.padding(start = 8.dp).weight(1f),
                                style = MaterialTheme.typography.body1.copy(
                                    textDecoration = if (task.done == true) TextDecoration.LineThrough else TextDecoration.None
                                ),
                                color = if (!isEnabled) {
                                    MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                                } else if (task.done == true) {
                                    MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                } else {
                                    MaterialTheme.colors.onSurface
                                }
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = resources?.first ?: "",
                                style = MaterialTheme.typography.body2,
                                color = if (!isEnabled) {
                                    MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                                } else {
                                    resources?.second ?: Color.Transparent
                                },
                            )
                            if (isOverdue) {
                                IconButton(
                                    onClick = { onReschedule(task) },
                                    modifier = Modifier.size(36.dp),
                                    enabled = isEnabled && !task.done!!
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = "Reschedule",
                                        tint = if (!isEnabled) {
                                            MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                                        } else {
                                            MaterialTheme.colors.error
                                        }
                                    )
                                }
                            }
                            Icon(
                                imageVector = getIconByPriority(task.priority?:0),
                                contentDescription = "Priority",
                                tint = if (!isEnabled) {
                                    MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                                } else {
                                    getColorByPriority(task.priority?:0)
                                },
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
