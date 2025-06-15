package com.esteban.ruano.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esteban.ruano.utils.DateUIUtils.getColorByDelay
import com.esteban.ruano.utils.HabitsUtils.getDelay
import com.esteban.ruano.utils.HabitsUtils.getTimeText
import com.lifecommander.models.Habit
import dev.icerock.moko.resources.compose.localized

@Composable
fun HabitItem(
    habit: Habit,
    interactionSource: MutableInteractionSource,
    textDecoration: TextDecoration = TextDecoration.None,
    onCheckedChange: (Habit, Boolean, onComplete: (Boolean) -> Unit) -> Unit,
    onComplete: (Habit, Boolean) -> Unit,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    isEnabled: Boolean = true,
    itemWrapper: @Composable (content: @Composable () -> Unit) -> Unit = { content -> content() }
) {
    val isHovered by interactionSource.collectIsHoveredAsState()
    val statusColor = if (habit.done == true) Color.Green else getColorByDelay(habit.getDelay())
    val showContextMenu = remember { mutableStateOf(false) }

    CommonItem(
        title = habit.name,
        isDone = habit.done == true,
        isEnabled = isEnabled,
        isHovered = isHovered,
        interactionSource = interactionSource,
        borderColor = statusColor,
        textDecoration = textDecoration,
        leftIcon = if (!isEnabled) {
            {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
            }
        } else null,
        onCheckedChange = if (isEnabled) {
            { checked ->
                onCheckedChange(habit, checked) { final ->
                    onComplete(habit, final)
                }
            }
        } else null,
        onClick = onClick,
        onLongClick = { showContextMenu.value = true },
        showContextMenu = showContextMenu.value,
        onDismissContextMenu = { showContextMenu.value = false },
        rightContent = {
            Box(
                modifier = Modifier.fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = habit.getTimeText().localized(),
                    style = MaterialTheme.typography.body2.copy(fontSize = 18.sp),
                    color = if (!isEnabled) {
                        MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                    } else {
                        statusColor
                    }
                )
            }
        },
        bottomContent = null,
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
        },
        itemWrapper = itemWrapper
    )
}

 