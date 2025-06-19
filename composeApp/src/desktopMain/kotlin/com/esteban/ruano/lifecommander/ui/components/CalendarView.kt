package ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.lifecommander.models.Habit
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import com.kizitonwose.calendar.compose.*
import com.kizitonwose.calendar.core.*
import kotlinx.coroutines.launch
import kotlinx.datetime.toJavaLocalDate
import com.esteban.ruano.utils.DateUIUtils.toLocalDateTime
import com.esteban.ruano.lifecommander.ui.viewmodels.CalendarViewModel
import com.esteban.ruano.utils.DateUIUtils.getCurrentDateTime
import com.esteban.ruano.utils.DateUIUtils.toLocalDate
import com.lifecommander.finance.model.Transaction
import com.lifecommander.models.Task
import kotlinx.datetime.TimeZone
import org.koin.compose.viewmodel.koinViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.*
import kotlinx.datetime.toKotlinLocalDate

@Composable
fun CalendarComposable(
    tasks: List<Task>,
    habits: List<Habit>,
    transactions: List<Transaction>,
    isLoading: Boolean,
    onRefresh: (startDate: LocalDate, endDate: LocalDate) -> Unit,
    error: String?,
    onTaskClick: (String) -> Unit,
    onHabitClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showTasks by remember { mutableStateOf(true) }
    var showHabits by remember { mutableStateOf(true) }
    var showTransactions by remember { mutableStateOf(true) }
    var showFutureTransactions by remember { mutableStateOf(false) }

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

    LaunchedEffect(state.firstVisibleMonth.yearMonth) {
        onRefresh(
            LocalDate.of(state.firstVisibleMonth.yearMonth.year, state.firstVisibleMonth.yearMonth.monthNumber, 1),
            LocalDate.of(state.firstVisibleMonth.yearMonth.year,
                state.firstVisibleMonth.yearMonth.monthNumber, state.firstVisibleMonth.yearMonth.lengthOfMonth())
        )
    }

    Column(modifier = modifier) {
        // Filter options
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            elevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = "Show Items",
                    style = MaterialTheme.typography.subtitle1,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FilterChip(
                        selected = showTasks,
                        onClick = { showTasks = !showTasks },
                        label = "Tasks",
                        icon = Icons.Default.CheckCircle
                    )
                    FilterChip(
                        selected = showHabits,
                        onClick = { showHabits = !showHabits },
                        label = "Habits",
                        icon = Icons.Default.Star
                    )
                    FilterChip(
                        selected = showTransactions,
                        onClick = { showTransactions = !showTransactions },
                        label = "Transactions",
                        icon = Icons.Default.Paid
                    )
                    FilterChip(
                        selected = showFutureTransactions,
                        onClick = { showFutureTransactions = !showFutureTransactions },
                        label = "Future Transactions",
                        icon = Icons.Default.Schedule
                    )
                }
            }
        }

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

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (error != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Error: $error",
                    color = MaterialTheme.colors.error
                )
            }
        } else {
            // Calendar content
            HorizontalCalendar(
                state = state,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
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
                    Day(
                        day,
                        if (showTasks) tasks else emptyList(),
                        if (showHabits) habits else emptyList(),
                        if (showTransactions) {
                            if (showFutureTransactions) transactions
                            else transactions.filter { it.date.toLocalDateTime().date <= getCurrentDateTime(
                                TimeZone.currentSystemDefault()
                            ).date }
                        } else emptyList(),
                        selectedDate == day.date.toJavaLocalDate()
                    ) { selectedDate = it }
                }
            )

            // Selected date details
            val selectedDateTasks = if (showTasks) {
                tasks.filter { task ->
                    val taskDate = task.dueDateTime?.toLocalDateTime()?.date
                        ?: task.scheduledDateTime?.toLocalDateTime()?.date
                    taskDate == selectedDate?.toKotlinLocalDate()
                }
            } else emptyList()

            val selectedDateHabits = if (showHabits) {
                habits.filter { habit ->
                    val habitDate = habit.dateTime?.toLocalDateTime()?.date
                    habitDate == selectedDate?.toKotlinLocalDate()
                }
            } else emptyList()

            val selectedDateTransactions = if (showTransactions) {
                transactions.filter { transaction ->
                    val transactionDate = transaction.date.toLocalDateTime().date
                    if (showFutureTransactions) {
                        transactionDate == selectedDate?.toKotlinLocalDate()
                    } else {
                        transactionDate == selectedDate?.toKotlinLocalDate() && 
                        transactionDate <= getCurrentDateTime(
                            TimeZone.currentSystemDefault()
                        ).date
                    }
                }
            } else emptyList()

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
                    items(selectedDateTasks.size) { index ->
                        TaskItem(selectedDateTasks[index], onTaskClick)
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
                    items(selectedDateHabits.size) { index ->
                        HabitItem(selectedDateHabits[index], onHabitClick)
                    }
                }

                if (selectedDateTransactions.isNotEmpty()) {
                    item {
                        Text(
                            text = "Transactions",
                            style = MaterialTheme.typography.subtitle1,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(selectedDateTransactions.size) { index ->
                        TransactionItem(selectedDateTransactions[index])
                    }
                }

                if (selectedDate != null && selectedDateTasks.isEmpty() && selectedDateHabits.isEmpty() && selectedDateTransactions.isEmpty()) {
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
}

@Composable
private fun FilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Surface(
        modifier = Modifier
            .padding(4.dp)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.small,
        color = if (selected) MaterialTheme.colors.primary.copy(alpha = 0.1f) else MaterialTheme.colors.surface,
        border = BorderStroke(1.dp, if (selected) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (selected) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.body2,
                color = if (selected) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface
            )
        }
    }
}

@Composable
private fun Day(
    day: CalendarDay,
    tasks: List<Task>,
    habits: List<Habit>,
    transactions: List<Transaction>,
    isSelected: Boolean,
    onDateSelected: (LocalDate) -> Unit
) {
    val kotlinDate = day.date
    println("Processing day: $kotlinDate")
    
    val dayTasks = tasks.filter { task ->
        val taskDate = task.dueDateTime?.toLocalDateTime()?.date 
            ?: task.scheduledDateTime?.toLocalDateTime()?.date
        println("Task ${task.name}: comparing $taskDate with $kotlinDate")
        taskDate == kotlinDate
    }
    
    val dayHabits = habits.filter { habit ->
        val habitDate = habit.dateTime?.toLocalDateTime()?.date
        println("Habit ${habit.name}: comparing $habitDate with $kotlinDate")
        habitDate == kotlinDate
    }

    val dayTransactions = transactions.filter { transaction ->
        transaction.date.toLocalDateTime().date == kotlinDate
    }

    println("Day $kotlinDate: ${dayTasks.size} tasks, ${dayHabits.size} habits, ${dayTransactions.size} transactions")

    Box(
        modifier = Modifier
            .aspectRatio(4f)
            .clip(MaterialTheme.shapes.small)
            .background(
                when {
                    isSelected -> MaterialTheme.colors.primary.copy(alpha = 0.2f)
                    day.position == DayPosition.MonthDate -> {
                        when {
                            getCurrentDateTime(
                                TimeZone.currentSystemDefault()
                            ).date == kotlinDate ->
                                MaterialTheme.colors.secondary.copy(alpha = 0.1f)
                            dayTasks.isNotEmpty() && dayHabits.isNotEmpty() -> MaterialTheme.colors.primary.copy(alpha = 0.1f)
                            dayTasks.isNotEmpty() -> MaterialTheme.colors.primary.copy(alpha = 0.05f)
                            dayHabits.isNotEmpty() -> MaterialTheme.colors.secondary.copy(alpha = 0.05f)
                            dayTransactions.isNotEmpty() -> MaterialTheme.colors.secondaryVariant.copy(alpha = 0.05f)
                            else -> Color.Transparent
                        }
                    }
                    else -> Color.Transparent
                }
            )
            .clickable(
                enabled = day.position == DayPosition.MonthDate,
                onClick = { onDateSelected(kotlinDate.toJavaLocalDate()) }
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
                if (dayTasks.size > 1 || dayHabits.isNotEmpty() || dayTransactions.isNotEmpty()) {
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
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colors.secondary
                            )
                        }
                        if (dayTransactions.isNotEmpty()) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(1.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 2.dp)
                            ){
                                Icon(
                                    imageVector = Icons.Default.Paid,
                                    contentDescription = "Transactions",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colors.secondaryVariant
                                )
                                Text(
                                    text =
                                        if (dayTransactions.size < 4) {
                                            dayTransactions.joinToString("\n") {
                                                it.description
                                            }
                                        } else {
                                            "${dayTransactions.size} transactions"
                                        }
                                    ,
                                    style = MaterialTheme.typography.caption,
                                    color = MaterialTheme.colors.secondaryVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskItem(task: Task, onTaskClick: (String) -> Unit) {
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
                        text = it.formatDefault(),
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun HabitItem(habit: Habit, onHabitClick: (String) -> Unit) {
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
                        text = dateTime.formatDefault(),
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TransactionItem(transaction: Transaction) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.description ?: "",
                    style = MaterialTheme.typography.body1
                )
                Text(
                    text = transaction.date.toLocalDateTime().formatDefault(),
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }
            Text(
                text = "$${transaction.amount}",
                style = MaterialTheme.typography.body1,
                color = if (transaction.amount >= 0) 
                    MaterialTheme.colors.primary 
                else 
                    MaterialTheme.colors.error
            )
        }
    }
} 