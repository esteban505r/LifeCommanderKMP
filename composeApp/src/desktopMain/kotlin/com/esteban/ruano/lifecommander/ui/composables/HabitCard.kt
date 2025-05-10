package ui.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ContextMenuDataProvider
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import getColorByDelay
import getDelayByTime
import services.HabitResponses.HabitUtils.getTimeText
import services.HabitResponses.HabitUtils.time
import services.habits.models.HabitResponse
import ui.theme.SoftGreen
import utils.DateUtils.toLocalDateTime
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun HabitCard(
    habit: HabitResponse,
    currentTime: LocalDateTime,
    onCheckedChange: (String,Boolean) -> Unit,
    onEdit: (HabitResponse) -> Unit = {},
    onDelete: (HabitResponse) -> Unit = {},
    isEnabled: Boolean = true
) {
    val habitStatusColor = getColorByDelay(getDelayByTime(habit.time() ?: ""))

    ContextMenuDataProvider(
        items = {
            listOf(
                ContextMenuItem("Edit") { onEdit(habit) },
                ContextMenuItem("Delete") { onDelete(habit) }
            )
        }
    ) {
        SelectionContainer {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                elevation = 4.dp,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, habitStatusColor.copy(alpha = 0.6f)),
                backgroundColor = if (!isEnabled) MaterialTheme.colors.surface.copy(alpha = 0.5f) else MaterialTheme.colors.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
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
                                checked = habit.done ?: false,
                                onCheckedChange = {
                                    onCheckedChange(habit.id, it)
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colors.primary,
                                    uncheckedColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                )
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = habit.name ?: "",
                            style = MaterialTheme.typography.body1.copy(
                                textDecoration = if (habit.done==true) TextDecoration.LineThrough else TextDecoration.None
                            ),
                            color = if (!isEnabled) {
                                MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                            } else if (habit.done == true) {
                                MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                            } else {
                                MaterialTheme.colors.onSurface
                            }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Text(
                        text = habit.getTimeText(),
                        style = MaterialTheme.typography.body2,
                        fontSize = 24.sp,
                        color = if (!isEnabled) {
                            MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                        } else if (habit.done == true) {
                            SoftGreen
                        } else {
                            habitStatusColor
                        }
                    )
                }
            }
        }
    }
}