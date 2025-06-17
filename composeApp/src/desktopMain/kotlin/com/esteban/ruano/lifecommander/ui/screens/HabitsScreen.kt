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
        // Professional Header with Add Button
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

            // Action Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { /* TODO: Implement filter */ },
                    modifier = Modifier.height(40.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colors.primary
                    )
                ) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Filter")
                }

                OutlinedButton(
                    onClick = { /* TODO: Implement sort */ },
                    modifier = Modifier.height(40.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colors.primary
                    )
                ) {
                    Icon(Icons.Default.Sort, contentDescription = "Sort")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sort")
                }

                Button(
                    onClick = { showNewHabitDialog = true },
                    modifier = Modifier.height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.primary
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Habit")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Habit")
                }
            }
        }

        // Habit Categories with improved styling
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HabitCategoryCard(
                title = "Today",
                count = habits.count { it.dateTime?.toLocalDateTime()?.date == currentDate },
                modifier = Modifier.weight(1f),
                backgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.1f)
            )
            HabitCategoryCard(
                title = "Active",
                count = habits.count { it.done == false},
                modifier = Modifier.weight(1f),
                backgroundColor = MaterialTheme.colors.secondary.copy(alpha = 0.1f)
            )
            HabitCategoryCard(
                title = "Completed",
                count = habits.count { it.done == true},
                modifier = Modifier.weight(1f),
                backgroundColor = MaterialTheme.colors.surface
            )
        }

        if (habitsLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colors.primary
                )
            }
        } else {
            if (habits.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Card(
                            modifier = Modifier.size(80.dp),
                            backgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.1f),
                            elevation = 0.dp
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "No habits",
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colors.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No habits yet",
                            style = MaterialTheme.typography.h6,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colors.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Start building healthy habits by adding your first one",
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { showNewHabitDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = MaterialTheme.colors.primary
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Habit")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Your First Habit")
                        }
                    }
                }
            } else {
                // Stats summary
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    backgroundColor = MaterialTheme.colors.surface,
                    elevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Habits",
                            style = MaterialTheme.typography.subtitle2,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "${habits.size}",
                            style = MaterialTheme.typography.h6,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.primary
                        )
                    }
                }

                HabitList(
                    habitList = habits,
                    isRefreshing = false,
                    onPullRefresh = onReload,
                    onHabitClick = onHabitClick,
                    onCheckedChange = onCheckedHabit,
                    modifier = Modifier.fillMaxSize(),
                    onDelete = onDelete,
                    itemWrapper = { content, habit ->
                        content()
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
        onDismiss = { 
            showNewHabitDialog = false
            habitToEdit = null
        },
        onAddHabit = { name, note, dateTime, frequency ->
            onAddHabit(name, note, frequency.value, dateTime)
            showNewHabitDialog = false
        },
        onUpdateHabit = { id, habit ->
            onUpdateHabit(id, habit)
            showNewHabitDialog = false
            habitToEdit = null
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
    modifier: Modifier = Modifier,
    backgroundColor: androidx.compose.ui.graphics.Color = MaterialTheme.colors.surface
) {
    Card(
        modifier = modifier,
        elevation = 2.dp,
        backgroundColor = backgroundColor
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.subtitle2,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.h4,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.onSurface
            )
        }
    }
} 