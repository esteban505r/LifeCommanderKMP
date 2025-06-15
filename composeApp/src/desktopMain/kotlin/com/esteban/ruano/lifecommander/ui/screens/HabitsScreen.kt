package com.esteban.ruano.lifecommander.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.esteban.ruano.ui.components.HabitList
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import com.esteban.ruano.utils.DateUIUtils.toLocalDateTime
import com.lifecommander.models.Habit
import com.lifecommander.models.Reminder
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime as toLocalDateTimeKt
import ui.composables.NewEditHabitDialog
import ui.viewmodels.HabitsViewModel

@Composable
fun HabitsScreen(
    habits: List<Habit>,
    habitsLoading: Boolean,
    onHabitClick: (Habit) -> Unit,
    onReload: () -> Unit,
    onAddHabit: (
        name: String,
        note: String?,
        frequency: String,
        dateTime: String
    ) -> Unit,
    onUpdateHabit: (String, Habit) -> Unit,
    onDelete: (Habit) -> Unit,
    onCheckedHabit: (Habit, Boolean, onComplete: (Boolean) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    var habitToEdit by remember { mutableStateOf<Habit?>(null) }
    var showNewHabitDialog by remember { mutableStateOf(false) }
    val currentDate = Clock.System.now().toLocalDateTimeKt(
        timeZone = TimeZone.currentSystemDefault()
    ).date

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Habits",
                    style = MaterialTheme.typography.h4,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = currentDate.formatDefault(),
                    style = MaterialTheme.typography.subtitle1,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }

            // Filter and Sort Controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { /* TODO: Implement filter */ },
                    modifier = Modifier.height(40.dp)
                ) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Filter")
                }

                OutlinedButton(
                    onClick = { /* TODO: Implement sort */ },
                    modifier = Modifier.height(40.dp)
                ) {
                    Icon(Icons.Default.Sort, contentDescription = "Sort")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sort")
                }
            }
        }

        // Habit Categories
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HabitCategoryCard(
                title = "Today",
                count = habits.count { it.dateTime?.toLocalDateTime()?.date == currentDate },
                modifier = Modifier.weight(1f)
            )
            HabitCategoryCard(
                title = "Active",
                count = habits.count { it.done == false},
                modifier = Modifier.weight(1f)
            )
            HabitCategoryCard(
                title = "Completed",
                count = habits.count { it.done == true},
                modifier = Modifier.weight(1f)
            )
        }

        if (habitsLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Debug information
            Text(
                text = "Total habits: ${habits.size}",
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (habits.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "No habits",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No habits yet",
                            style = MaterialTheme.typography.h6,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                HabitList(
                    habitList = habits,
                    isRefreshing = false,
                    onPullRefresh = onReload,
                    onHabitClick = onHabitClick,
                    onCheckedChange = onCheckedHabit,
                    modifier = Modifier.fillMaxSize(),
                    onDelete = onDelete,
                    itemWrapper = { content, habit ->
                        content
                    },
                    onEdit = { habit ->
                        habitToEdit = habit
                        showNewHabitDialog = true
                    }
                )
            }
        }
    }

    NewEditHabitDialog(
        onDismiss = { showNewHabitDialog = false },
        onAddHabit = { name, note, dateTime, frequency ->
            onAddHabit(name, note, frequency.value, dateTime)
            showNewHabitDialog = false
        },
        onUpdateHabit = { id, habit ->
            onUpdateHabit(id, habit)
            showNewHabitDialog = false
        },
        habitToEdit = habitToEdit,
        onError = {
            // Handle error
        },
        show = showNewHabitDialog
    )
}

@Composable
private fun HabitCategoryCard(
    title: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.subtitle1,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.h5,
                fontWeight = FontWeight.Bold
            )
        }
    }
} 