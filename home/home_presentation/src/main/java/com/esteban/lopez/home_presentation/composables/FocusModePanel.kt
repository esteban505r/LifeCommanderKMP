package com.esteban.lopez.home_presentation.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.outlined.CenterFocusWeak
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.esteban.ruano.ui.LCDS
import com.esteban.ruano.ui.LifeCommanderDesignSystem
import com.lifecommander.models.Habit
import com.lifecommander.models.Task

@Composable
fun FocusModePanel(
    habits: List<Habit>,
    currentHabit: Habit?,
    tasks: List<Task>,
    onlyCurrent: Boolean,
    onToggleOnlyCurrent: (Boolean) -> Unit,
    onOpenHabit: (Habit?) -> Unit,
    onOpenTasks: () -> Unit,
    onExitFocus: () -> Unit
) {
    // Choose which habits to show
    val visibleHabits = remember(habits, currentHabit, onlyCurrent) {
        val pending = habits.filter { it.done != true } // adjust if your field differs
        if (onlyCurrent && currentHabit != null) listOf(currentHabit) else pending
    }

    // Pick a shortlist of tasks (pending first, soonest due first)
    val visibleTasks = remember(tasks) {
        tasks.filter { it.done != true }
            .sortedBy { it.priority}
            .take(6)
    }

    Surface(
        shape = LifeCommanderDesignSystem.ComponentPresets.TaskCardShape,
        color = MaterialTheme.colors.surface,
        elevation = LifeCommanderDesignSystem.dimensions.ElevationSmall,
        border = BorderStroke(1.dp, MaterialTheme.colors.onSurface.copy(.08f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {

            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CenterFocusStrong, contentDescription = null, tint = LCDS.colors.Primary)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Focus mode",
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                TextButton(onClick = onExitFocus) { Text("Exit") }
            }

            Spacer(Modifier.height(12.dp))

            // Toggle: only current
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = { onToggleOnlyCurrent(!onlyCurrent) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        if (onlyCurrent) Icons.Filled.CenterFocusStrong else Icons.Outlined.CenterFocusWeak,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(if (onlyCurrent) "Showing current" else "Show only current")
                }
                Spacer(Modifier.weight(1f))
            }

            // Habits list
            Spacer(Modifier.height(12.dp))
            Text(
                "Habits",
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.onSurface.copy(.7f)
            )
            Spacer(Modifier.height(6.dp))

            if (visibleHabits.isEmpty()) {
                Text(
                    "No pending habits",
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface.copy(.6f)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    visibleHabits.forEach { h ->
                        FocusHabitRow(
                            habit = h,
                            isCurrent = h.id == currentHabit?.id, // adjust id accessor if different
                            onClick = { onOpenHabit(h) }
                        )
                    }
                }
            }

            // Tasks list
            Spacer(Modifier.height(16.dp))
            Text(
                "Tasks",
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.onSurface.copy(.7f)
            )
            Spacer(Modifier.height(6.dp))

            if (visibleTasks.isEmpty()) {
                Text(
                    "No pending tasks",
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface.copy(.6f)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    visibleTasks.forEach { t ->
                        FocusTaskRow(task = t)
                    }
                }
                if (tasks.count { it.done != true } > visibleTasks.size) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "✨ +${tasks.count { !it.done } - visibleTasks.size} more tasks",
                        style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Medium, color = LCDS.colors.Secondary)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onOpenTasks, shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Default.Task,"Task")
                    Spacer(Modifier.width(8.dp))
                    Text("Open tasks")
                }
            }
        }
    }
}

@Composable
private fun FocusHabitRow(
    habit: Habit,
    isCurrent: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isCurrent) MaterialTheme.colors.primary.copy(.08f) else MaterialTheme.colors.onSurface.copy(.03f),
        border = if (isCurrent) BorderStroke(1.dp, MaterialTheme.colors.primary.copy(.35f)) else null,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        (if (isCurrent) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface)
                            .copy(.12f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.TaskAlt,"Habits",
                    tint = if (isCurrent) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface.copy(.7f)
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(habit.name ?: "Habit", maxLines = 1)
                if (!habit.note.isNullOrBlank()) {
                    Text(
                        habit.note!!,
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(.6f),
                        maxLines = 1
                    )
                }
            }
            if (isCurrent) {
                Text(
                    "Now",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.primary
                )
            }
        }
    }
}

@Composable
private fun FocusTaskRow(task: Task) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colors.onSurface.copy(.03f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (task.done) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (task.done) Color(0xFF2ECC71) else MaterialTheme.colors.onSurface.copy(.7f)
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(task.name, maxLines = 1)
                if (!task.note.isNullOrBlank()) {
                    Text(
                        task.note!!,
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(.6f),
                        maxLines = 1
                    )
                }
            }
        }
    }
}
