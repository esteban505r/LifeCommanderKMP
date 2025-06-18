package com.esteban.ruano.lifecommander.ui.components

import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import com.esteban.ruano.utils.DateUIUtils.toLocalDateTime
import com.esteban.ruano.utils.DateUIUtils.toLocalTime
import com.lifecommander.models.Task
import com.lifecommander.models.dashboard.TaskStats
import ui.components.NextActionCard
import ui.components.StatItem
import ui.components.StatsCard
import utils.DateUtils.calculateTimeRemaining
import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter

@Composable
fun TasksSummary(
    nextTask: Task?,
    taskStats: TaskStats?,
    overdueTasks: List<Task>,
    onMarkTaskDone: (Task) -> Unit,
    onViewAllClick: () -> Unit,
    isExpanded: Boolean,
    currentTime: Long
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Next Task Card
        NextActionCard(
            title = "Next Task",
            name = nextTask?.name ?: "No tasks pending",
            note = nextTask?.note,
            timeRemaining = nextTask?.let { (it.dueDateTime?:it.scheduledDateTime)?.toLocalDateTime()?.calculateTimeRemaining(currentTime) },
            badge = {
                if (nextTask != null) {
                    PriorityBadge(priority = nextTask.priority)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Minimalist overdue slider above stats
        MinimalOverdueSliderTasks(overdueTasks, onMarkTaskDone)

        // Stats Card
        StatsCard(
            title = "Tasks",
            stats = listOf(
                StatItem(
                    "Total",
                    taskStats?.total ?: 0,
                    Color(0xFF2196F3),
                    Icons.Outlined.List
                ),
                StatItem(
                    "Completed",
                    taskStats?.completed ?: 0,
                    Color(0xFF4CAF50),
                    Icons.Filled.CheckCircle
                ),
                StatItem(
                    "High Priority",
                    taskStats?.highPriority ?: 0,
                    Color(0xFFF44336),
                    Icons.Filled.PriorityHigh
                )
            ),
            onClick = onViewAllClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun RowScope.TaskStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Card(
        modifier = Modifier
            .weight(1f)
            .padding(horizontal = 4.dp),
        backgroundColor = color.copy(alpha = 0.1f),
        elevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.caption,
                color = color
            )
        }
    }
}

@Composable
private fun TaskSummaryItem(task: Task) {
    var isHovered by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is androidx.compose.foundation.interaction.HoverInteraction.Enter -> isHovered = true
                is androidx.compose.foundation.interaction.HoverInteraction.Exit -> isHovered = false
            }
        }
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .hoverable(
                interactionSource = interactionSource,
                enabled = true,
            ),
        backgroundColor = if (isHovered) 
            MaterialTheme.colors.surface.copy(alpha = 0.05f) 
        else 
            MaterialTheme.colors.surface,
        elevation = if (isHovered) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = task.done == true,
                    onCheckedChange = null,
                    enabled = false,
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colors.primary,
                        uncheckedColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                )
                Column {
                    Text(
                        text = task.name,
                        style = MaterialTheme.typography.body2,
                        maxLines = 1,
                        color = if (task.done == true) 
                            MaterialTheme.colors.onSurface.copy(alpha = 0.6f) 
                        else 
                            MaterialTheme.colors.onSurface
                    )
                    if (task.note?.isNotBlank() == true) {
                        Text(
                            text = task.note?:"",
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                            maxLines = 1
                        )
                    }
                }
            }
            if (task.dueDateTime != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = task.dueDateTime?.toLocalDateTime()?.formatDefault() ?: "No due date",
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PriorityBadge(priority: Int) {
    val (color, text) = when (priority) {
        0 -> Color(0xFF9E9E9E) to "Low"
        1 -> Color(0xFF2196F3) to "Medium"
        2 -> Color(0xFFFFA726) to "High"
        else -> Color(0xFFF44336) to "Urgent"
    }
    
    Surface(
        modifier = Modifier.clip(RoundedCornerShape(4.dp)),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.caption,
            color = color
        )
    }
}

@Composable
fun MinimalOverdueSliderTasks(
    overdueTasks: List<Task>,
    onMarkTaskDone: (Task) -> Unit
) {
    if (overdueTasks.isNotEmpty()) {
        val state = rememberLazyListState()
        Text(
            "Overdue Tasks",
            style = MaterialTheme.typography.subtitle2,
            color = MaterialTheme.colors.error,
            modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colors.surface.copy(alpha = 0.7f), shape = RoundedCornerShape(8.dp))
                .padding(vertical = 4.dp)
        ) {
            LazyRow(
                state = state,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                items(overdueTasks.size) { idx ->
                    val task = overdueTasks[idx]
                    MinimalOverdueTaskCard(task, onMarkTaskDone)
                }
            }
            HorizontalScrollbar(
                adapter = rememberScrollbarAdapter(state),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
            )
        }
    }
}

@Composable
fun MinimalOverdueTaskCard(task: Task, onMarkDone: (Task) -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Surface(
        modifier = Modifier
            .padding(end = 8.dp)
            .width(180.dp)
            .height(70.dp)
            .hoverable(interactionSource),
        shape = RoundedCornerShape(10.dp),
        elevation = 2.dp,
        color =  MaterialTheme.colors.surface
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.ErrorOutline,
                        contentDescription = "Overdue",
                        tint = MaterialTheme.colors.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        " Overdue",
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.error,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(task.name, style = MaterialTheme.typography.body2, maxLines = 1)
                Text(
                    task.dueDateTime?.toLocalDateTime()?.toLocalTime()?.toString() ?: "-",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }
            Icon(
                Icons.Default.Check,
                contentDescription = "Mark as done",
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onMarkDone(task) },
                tint = MaterialTheme.colors.primary
            )
        }
    }
} 