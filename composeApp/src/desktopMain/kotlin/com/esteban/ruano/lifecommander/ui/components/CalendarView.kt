package ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.compose.*
import com.kizitonwose.calendar.core.*
import com.kizitonwose.calendar.core.atStartOfMonth
import kotlinx.coroutines.launch
import kotlinx.datetime.toJavaLocalDate
import services.habits.models.HabitResponse
import services.tasks.models.TaskResponse
import utils.DateUIUtils.toLocalDateTime
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.*

@Composable
fun CalendarComposable(
    tasks: List<TaskResponse>,
    habits: List<HabitResponse>,
    onTaskClick: (String) -> Unit,
    onHabitClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(12) }
    val endMonth = remember { currentMonth.plusMonths(12) }
    val firstDayOfWeek = remember { WeekFields.of(Locale.getDefault()).firstDayOfWeek }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = firstDayOfWeek
    )

    Column(modifier = modifier) {
        // Month navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { 
                coroutineScope.launch {
                    state.scrollToMonth(state.firstVisibleMonth.yearMonth.minusMonths(1))
                }
            }) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month")
            }
            Text(
                text = state.firstVisibleMonth.yearMonth.toString(),
                style = MaterialTheme.typography.h6
            )
            IconButton(onClick = { 
                coroutineScope.launch {
                    state.scrollToMonth(state.firstVisibleMonth.yearMonth.plusMonths(1))
                }
            }) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
            }
        }

        // Remove the separate days of week header since we'll use monthHeader
        HorizontalCalendar(
            state = state,
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp),
            monthHeader = { month ->
                val daysOfWeek = month.weekDays.first().map { it.date.dayOfWeek }
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (dayOfWeek in daysOfWeek) {
                        Text(
                            text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            },
            dayContent = { day ->
                Day(day, tasks, habits, selectedDate == day.date.toJavaLocalDate()) { selectedDate = it }
            }
        )

        // Selected date details
        val selectedDateTasks = tasks.filter { task ->
            val taskDate = task.dueDateTime?.toLocalDateTime()?.toLocalDate() 
                ?: task.scheduledDateTime?.toLocalDateTime()?.toLocalDate()
            taskDate == selectedDate
        }
        val selectedDateHabits = habits.filter { habit ->
            habit.dateTime?.toLocalDateTime()?.toLocalDate() == selectedDate
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp)
        ) {
            item {
                Text(
                    text = selectedDate?.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"))
                        ?: "Select a date to view details",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            if (selectedDateTasks.isNotEmpty()) {
                item {
                    Text(
                        text = "Tasks",
                        style = MaterialTheme.typography.subtitle1,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(selectedDateTasks) { task ->
                    TaskItem(task, onTaskClick)
                }
            }

            if (selectedDateHabits.isNotEmpty()) {
                item {
                    Text(
                        text = "Habits",
                        style = MaterialTheme.typography.subtitle1,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(selectedDateHabits) { habit ->
                    HabitItem(habit, onHabitClick)
                }
            }

            if (selectedDate != null && selectedDateTasks.isEmpty() && selectedDateHabits.isEmpty()) {
                item {
                    Text(
                        text = "No items scheduled for this day",
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }
        }
    }
}


@Composable
private fun Day(
    day: CalendarDay,
    tasks: List<TaskResponse>,
    habits: List<HabitResponse>,
    isSelected: Boolean,
    onDateSelected: (LocalDate) -> Unit
) {
    val dayTasks = tasks.filter { task ->
        val taskDate = task.dueDateTime?.toLocalDateTime()?.toLocalDate() 
            ?: task.scheduledDateTime?.toLocalDateTime()?.toLocalDate()
        taskDate == day.date.toJavaLocalDate()
    }
    val dayHabits = habits.filter { habit ->
        habit.dateTime?.toLocalDateTime()?.toLocalDate() == day.date.toJavaLocalDate()
    }

    Box(
        modifier = Modifier
            .aspectRatio(4f)
            .clip(MaterialTheme.shapes.small)
            .background(
                when {
                    isSelected -> MaterialTheme.colors.primary.copy(alpha = 0.2f)
                    day.position == DayPosition.MonthDate -> {
                        when {
                            LocalDate.now() == day.date.toJavaLocalDate() -> MaterialTheme.colors.secondary.copy(alpha = 0.1f)
                            dayTasks.isNotEmpty() && dayHabits.isNotEmpty() -> MaterialTheme.colors.primary.copy(alpha = 0.1f)
                            dayTasks.isNotEmpty() -> MaterialTheme.colors.primary.copy(alpha = 0.05f)
                            dayHabits.isNotEmpty() -> MaterialTheme.colors.secondary.copy(alpha = 0.05f)
                            else -> Color.Transparent
                        }
                    }
                    else -> Color.Transparent
                }
            )
            .clickable(
                enabled = day.position == DayPosition.MonthDate,
                onClick = { onDateSelected(day.date.toJavaLocalDate()) }
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(2.dp)
        ) {
            Text(
                text = day.date.dayOfMonth.toString(),
                color = when {
                    day.position == DayPosition.MonthDate -> MaterialTheme.colors.onSurface
                    else -> MaterialTheme.colors.onSurface.copy(alpha = 0.3f)
                },
                style = MaterialTheme.typography.caption
            )
            
            if (day.position == DayPosition.MonthDate) {
                Spacer(modifier = Modifier.height(1.dp))
                // Show first task name if available
                dayTasks.firstOrNull()?.let { task ->
                    Text(
                        text = task.name ?: "",
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 1.dp)
                    )
                }
                // Show indicators for additional items
                if (dayTasks.size > 1 || dayHabits.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(1.dp),
                        modifier = Modifier.padding(top = 1.dp)
                    ) {
                        if (dayTasks.size > 1) {
                            Text(
                                text = "+${dayTasks.size - 1}",
                                style = MaterialTheme.typography.caption,
                                color = MaterialTheme.colors.primary
                            )
                        }
                        if (dayHabits.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Habits",
                                modifier = Modifier.size(8.dp),
                                tint = MaterialTheme.colors.secondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskItem(task: TaskResponse, onTaskClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onTaskClick(task.id) },
        elevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.done ?: false,
                onCheckedChange = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.name?:"",
                    style = MaterialTheme.typography.body1
                )
                val dateTime = task.dueDateTime?.toLocalDateTime() 
                    ?: task.scheduledDateTime?.toLocalDateTime()
                dateTime?.let {
                    Text(
                        text = it.format(DateTimeFormatter.ofPattern("h:mm a")),
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun HabitItem(habit: HabitResponse, onHabitClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onHabitClick(habit.id) },
        elevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = habit.done ?: false,
                onCheckedChange = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.name?:"",
                    style = MaterialTheme.typography.body1
                )
                habit.dateTime?.toLocalDateTime()?.let { dateTime ->
                    Text(
                        text = dateTime.format(DateTimeFormatter.ofPattern("h:mm a")),
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
} 