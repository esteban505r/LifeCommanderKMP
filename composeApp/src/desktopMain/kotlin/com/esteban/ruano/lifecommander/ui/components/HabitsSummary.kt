package com.esteban.ruano.lifecommander.ui.components

import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
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
import com.esteban.ruano.utils.DateUIUtils.toLocalDateTime
import com.esteban.ruano.utils.DateUIUtils.toLocalTime
import com.lifecommander.models.Habit
import com.lifecommander.models.dashboard.HabitStats
import ui.components.NextActionCard
import ui.components.StatItem
import ui.components.StatsCard
import utils.DateUtils.calculateTimeRemaining

@Composable
fun HabitsSummary(
    nextHabit: Habit?,
    habitStats: HabitStats?,
    overdueHabits: List<Habit>,
    onMarkHabitDone: (Habit) -> Unit,
    onViewAllClick: () -> Unit,
    isExpanded: Boolean,
    currentTime: Long
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Next Habit Card
        NextActionCard(
            title = "Next Habit",
            name = nextHabit?.name ?: "No habits pending",
            note = nextHabit?.note,
            timeRemaining = nextHabit?.let { it.dateTime?.toLocalDateTime()?.calculateTimeRemaining(currentTime) },
            badge = {
                if (nextHabit != null) {
                    StreakBadge(streak = nextHabit.streak)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Minimalist overdue slider above stats
        MinimalOverdueSliderHabits(overdueHabits, onMarkHabitDone)

        // Stats Card
        StatsCard(
            title = "Habits",
            stats = listOf(
                StatItem(
                    "Total",
                    habitStats?.total ?: 0,
                    Color(0xFF9C27B0),
                    Icons.Outlined.Repeat
                ),
                StatItem(
                    "Completed",
                    habitStats?.completed ?: 0,
                    Color(0xFF4CAF50),
                    Icons.Filled.CheckCircle
                ),
                StatItem(
                    "Current Streak",
                    habitStats?.currentStreak ?: 0,
                    Color(0xFFFFA726),
                    Icons.Filled.LocalFireDepartment
                )
            ),
            onClick = onViewAllClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun RowScope.HabitStatCard(
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
private fun HabitSummaryItem(habit: Habit) {
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
                    checked = habit.done == true,
                    onCheckedChange = null,
                    enabled = false,
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colors.primary,
                        uncheckedColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                )
                Column {
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.body2,
                        maxLines = 1,
                        color = if (habit.done == true) 
                            MaterialTheme.colors.onSurface.copy(alpha = 0.6f) 
                        else 
                            MaterialTheme.colors.onSurface
                    )
                    if (habit.note?.isNotBlank() == true) {
                        Text(
                            text = habit.note ?: "",
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                            maxLines = 1
                        )
                    }
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Repeat,
                    contentDescription = null,
                    tint = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = habit.frequency.toString(),
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun StreakBadge(streak: Int) {
    Surface(
        modifier = Modifier.clip(RoundedCornerShape(4.dp)),
        color = Color(0xFFFFA726).copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocalFireDepartment,
                contentDescription = null,
                tint = Color(0xFFFFA726),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "$streak days",
                style = MaterialTheme.typography.caption,
                color = Color(0xFFFFA726)
            )
        }
    }
}

@Composable
fun MinimalOverdueSliderHabits(
    overdueHabits: List<Habit>,
    onMarkHabitDone: (Habit) -> Unit
) {
    if (overdueHabits.isNotEmpty()) {

        val state = rememberLazyListState()
        Text("Overdue Habits", style = MaterialTheme.typography.subtitle2, color = MaterialTheme.colors.error,
        modifier = Modifier.padding(top=8.dp , start = 8.dp, end = 8.dp))
        Column (
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            LazyRow(
                state = state,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                items(overdueHabits.size) { idx ->
                    val habit = overdueHabits[idx]
                    MinimalOverdueHabitCard(habit, onMarkHabitDone)
                }
            }

            HorizontalScrollbar(
                adapter = rememberScrollbarAdapter(state),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
fun MinimalOverdueHabitCard(habit: Habit, onMarkDone: (Habit) -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    Surface(
        modifier = Modifier
            .padding(end = 8.dp)
            .width(180.dp)
            .height(70.dp)
            .hoverable(interactionSource),
        shape = RoundedCornerShape(10.dp),
        elevation = 2.dp,
        color = MaterialTheme.colors.surface
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
                Text(habit.name, style = MaterialTheme.typography.body2, maxLines = 1)
                Text(
                    habit.dateTime?.toLocalDateTime()?.toLocalTime()?.toString() ?: "-",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }
            Icon(
                Icons.Default.Check,
                contentDescription = "Mark as done",
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onMarkDone(habit) },
                tint = MaterialTheme.colors.primary
            )
        }
    }
} 