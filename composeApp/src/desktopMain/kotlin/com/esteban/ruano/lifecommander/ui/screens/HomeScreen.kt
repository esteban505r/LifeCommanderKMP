package com.esteban.ruano.lifecommander.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.esteban.ruano.lifecommander.models.TaskFilters
import com.esteban.ruano.lifecommander.ui.components.CurrentHabitComposable
import com.lifecommander.models.Habit
import com.esteban.ruano.ui.components.HabitList
import com.esteban.ruano.ui.components.TaskList
import com.lifecommander.models.Task
import com.esteban.ruano.lifecommander.ui.components.ToggleChipsButtons
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import services.NightBlockService
import ui.components.*
import ui.composables.*
import ui.viewmodels.*
import com.esteban.ruano.lifecommander.ui.components.TasksSummary
import com.esteban.ruano.lifecommander.ui.components.HabitsSummary

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    habitsViewModel: HabitsViewModel = koinViewModel(),
    tasksViewModel: TasksViewModel = koinViewModel(),
    dashboardViewModel: DashboardViewModel = koinViewModel(),
    nightBlockService: NightBlockService = koinInject(),
    dailyJournalViewModel: DailyJournalViewModel = koinViewModel(),
    onTaskClick: (Task) -> Unit,
    onHabitClick: (Habit) -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToHabits: () -> Unit
) {
    var showTokenDialog by remember { mutableStateOf(false) }
    var showNewTaskDialog by remember { mutableStateOf(false) }
    var showNewHabitDialog by remember { mutableStateOf(false) }
    var showNightBlockDialog by remember { mutableStateOf(false) }
    var showOverrideDialog by remember { mutableStateOf(false) }

    val tasks by tasksViewModel.tasks.collectAsState()
    val isNightBlockActive by nightBlockService.isNightBlockActive.collectAsState()

    val selectedFilter by tasksViewModel.selectedFilter.collectAsState()
    val tasksLoading by tasksViewModel.loading.collectAsState()
    val habitsLoading by habitsViewModel.loading.collectAsState()
    val dashboardLoading by dashboardViewModel.loading.collectAsState()

    val habits by habitsViewModel.habits.collectAsState()
    val nextTask by dashboardViewModel.nextTask.collectAsState()
    val nextHabit by dashboardViewModel.nextHabit.collectAsState()
    val taskStats by dashboardViewModel.taskStats.collectAsState()
    val habitStats by dashboardViewModel.habitStats.collectAsState()
    val currentTime by dashboardViewModel.currentTime.collectAsState()

    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    var habitToEdit by remember { mutableStateOf<Habit?>(null) }

    var error by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    // Define sections
    val sections = remember {
        listOf(
            HomeSection.Tasks,
            HomeSection.Habits,
            HomeSection.Meals,
            HomeSection.Workout,
            HomeSection.Finances,
            HomeSection.Journal
        )
    }

    // Track expanded state for each section, all expanded by default
    val expandedSections = remember { 
        mutableStateMapOf<HomeSection, Boolean>().apply {
            sections.forEach { section ->
                put(section, true)
            }
        }
    }

    // Check for Night Block activation every minute
    LaunchedEffect(Unit) {
        while (true) {
            nightBlockService.checkAndActivateNightBlock()
            delay(60000) // 1 minute
        }
    }

    if(error){
        AlertDialog(
            onDismissRequest = { error = false },
            title = { Text("Error") },
            text = { Text("An error occurred: $errorMessage") },
            confirmButton = {
                Button(
                    onClick = { error = false }
                ) {
                    Text("OK")
                }
            }
        )
    }

    LaunchedEffect(Unit) {
        habitsViewModel.getHabits()
        tasksViewModel.getTasksByFilter()
    }

    TokenDialog(
        show = showTokenDialog,
        onDismiss = { showTokenDialog = false }
    )

    NewEditTaskDialog(
        taskToEdit = taskToEdit,
        show = showNewTaskDialog,
        onDismiss = { showNewTaskDialog = false },
        onAddTask = { name, note, reminders, dueDate, scheduledDate, priority ->
            tasksViewModel.addTask(name, dueDate, scheduledDate,note,priority?:0)
        },
        onUpdateTask = { id, task ->
            tasksViewModel.updateTask(id, task)
        },
        onError = {
            errorMessage = it
            error = true
        }
    )

    NewEditHabitDialog(
        habitToEdit = habitToEdit,
        show = showNewHabitDialog,
        onDismiss = {
            showNewHabitDialog = false
            habitToEdit = null
        },
        onAddHabit = { name, note, dateTime,frequency ->
            habitsViewModel.addHabit(name, note, frequency.value, dateTime)
        },
        onUpdateHabit = { id, habit ->
            habitsViewModel.updateHabit(id, habit)
        },
        onError = {
            errorMessage = it
            error = true
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (isNightBlockActive) {
            NightBlockQuestionsComposable(dailyJournalViewModel)
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(sections) { section ->
                    HomeSectionCard(
                        section = section,
                        isExpanded = expandedSections[section] ?: true,
                        onToggleExpand = { expandedSections[section] = !(expandedSections[section] ?: true) },
                        content = {
                            when (section) {
                                HomeSection.Tasks -> TasksSummary(
                                    nextTask = nextTask,
                                    taskStats = taskStats,
                                    onViewAllClick = onNavigateToTasks,
                                    isExpanded = expandedSections[section] ?: true,
                                    currentTime = currentTime
                                )
                                HomeSection.Habits -> HabitsSummary(
                                    nextHabit = nextHabit,
                                    habitStats = habitStats,
                                    onViewAllClick = onNavigateToHabits,
                                    isExpanded = expandedSections[section] ?: true,
                                    currentTime = currentTime
                                )
                                HomeSection.Meals -> MealsSection()
                                HomeSection.Workout -> WorkoutSection()
                                HomeSection.Finances -> FinancesSection()
                                HomeSection.Journal -> JournalSection(dailyJournalViewModel)
                            }
                        }
                    )
                }
            }
        }
    }

    if (showNightBlockDialog) {
        AlertDialog(
            onDismissRequest = { showNightBlockDialog = false },
            title = { Text("Night Block") },
            text = {
                NightBlockComposable(
                    dailyJournalViewModel = dailyJournalViewModel,
                    nightBlockService = nightBlockService,
                    habits = habits,
                    onOverride = { reason ->
                        coroutineScope.launch{
                            nightBlockService.overrideNightBlock(reason)
                        }
                        showNightBlockDialog = false
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = { showNightBlockDialog = false }
                ) {
                    Text("Close")
                }
            }
        )
    }

    if (showOverrideDialog) {
        AlertDialog(
            onDismissRequest = { showOverrideDialog = false },
            title = { Text("Override Night Block") },
            text = {
                Column {
                    Text("Please provide a reason for overriding the Night Block:")
                    OutlinedTextField(
                        value = "",
                        onValueChange = { /* Handle input */ },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Handle override with reason
                        showOverrideDialog = false
                    }
                ) {
                    Text("Override")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showOverrideDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.h6,
        color = MaterialTheme.colors.primary,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(MaterialTheme.colors.secondary.copy(alpha = 0.1f))
            .padding(8.dp)
    )
}

@Composable
private fun HomeSectionCard(
    section: HomeSection,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    content: @Composable () -> Unit
) {
    val transitionState = remember { MutableTransitionState(false).apply { targetState = true } }
    val transition = updateTransition(transitionState, label = "cardTransition")
    
    val cardElevation by transition.animateDp(
        transitionSpec = { spring(stiffness = Spring.StiffnessLow) },
        label = "elevation"
    ) { if (isExpanded) 8.dp else 2.dp }

    val rotationAngle by transition.animateFloat(
        transitionSpec = { spring(stiffness = Spring.StiffnessLow) },
        label = "rotation"
    ) { if (isExpanded) 180f else 0f }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(cardElevation, RoundedCornerShape(16.dp))
            .animateContentSize()
            .clickable(onClick = onToggleExpand),
        shape = RoundedCornerShape(16.dp),
        elevation = 0.dp
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = section.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colors.primary
                    )
                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.graphicsLayer {
                        rotationZ = rotationAngle
                    }
                )
            }

            // Content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 300)
                ),
                exit = shrinkVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeOut(
                    animationSpec = tween(durationMillis = 300)
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun TasksSection(
    tasks: List<Task>,
    tasksLoading: Boolean,
    selectedFilter: TaskFilters,
    onTaskClick: (Task) -> Unit,
    onTaskCheckedChange: (Task, Boolean) -> Unit,
    onFilterChange: (TaskFilters) -> Unit,
    onTaskEdit: (Task) -> Unit,
    onTaskDelete: (Task) -> Unit
) {
    Column {
        ToggleChipsButtons(
            modifier = Modifier.padding(bottom = 16.dp),
            selectedIndex = TaskFilters.entries.indexOf(selectedFilter),
            buttons = TaskFilters.entries,
            onGetStrings = { filter -> filter.toString() },
            onCheckedChange = { onFilterChange }
        )

        if (tasksLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            TaskList(
                taskList = tasks,
                isRefreshing = false,
                onPullRefresh = { },
                onTaskClick = onTaskClick,
                onCheckedChange = onTaskCheckedChange,
                modifier = Modifier.heightIn(max = 400.dp),
                onEdit = onTaskEdit,
                onDelete = onTaskDelete,
                onReschedule = { },
                itemWrapper = { content, task ->
                    ContextMenuArea(
                        items = {
                            listOf(
                                ContextMenuItem("Edit") { onTaskEdit(task) },
                                ContextMenuItem("Delete") { onTaskDelete(task) }
                            )
                        }
                    ) {
                        content.invoke()
                    }
                }
            )
        }
    }
}

@Composable
private fun HabitsSection(
    habits: List<Habit>,
    habitsLoading: Boolean,
    onHabitClick: (Habit) -> Unit,
    onHabitCheckedChange: (Habit, Boolean, (Boolean) -> Unit) -> Unit,
    onHabitEdit: (Habit) -> Unit,
    onHabitDelete: (Habit) -> Unit
) {
    if (habitsLoading) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        HabitList(
            habitList = habits,
            isRefreshing = false,
            onPullRefresh = { },
            onHabitClick = onHabitClick,
            onCheckedChange = onHabitCheckedChange,
            modifier = Modifier.heightIn(max = 400.dp),
            onEdit = onHabitEdit,
            onDelete = onHabitDelete,
            itemWrapper = { content, habit ->
                ContextMenuArea(
                    items = {
                        listOf(
                            ContextMenuItem("Edit") { onHabitEdit(habit) },
                            ContextMenuItem("Delete") { onHabitDelete(habit) }
                        )
                    }
                ) {
                    content()
                }
            }
        )
    }
}

@Composable
private fun MealsSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Today's Meals Overview
        Card(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colors.surface,
            elevation = 0.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Today's Meals",
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Bold
                )
                
                // Meal Schedule
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MealTimeCard(
                        time = "Breakfast",
                        icon = Icons.Default.WbSunny,
                        color = Color(0xFFFFB74D)
                    )
                    MealTimeCard(
                        time = "Lunch",
                        icon = Icons.Default.WbSunny,
                        color = Color(0xFFFFA726)
                    )
                    MealTimeCard(
                        time = "Dinner",
                        icon = Icons.Default.Nightlight,
                        color = Color(0xFF5C6BC0)
                    )
                }
            }
        }

        // Meal Plan Preview
        Card(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colors.surface,
            elevation = 0.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "This Week's Plan",
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { /* TODO */ }) {
                        Text("View All")
                    }
                }

                // Weekly Preview
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    repeat(7) { day ->
                        DayMealPreview(day = day)
                    }
                }
            }
        }
    }
}

@Composable
private fun MealTimeCard(
    time: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .height(80.dp),
        backgroundColor = color.copy(alpha = 0.1f),
        elevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = time,
                style = MaterialTheme.typography.caption,
                color = color
            )
        }
    }
}

@Composable
private fun DayMealPreview(day: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = when(day) {
                0 -> "M"
                1 -> "T"
                2 -> "W"
                3 -> "T"
                4 -> "F"
                5 -> "S"
                else -> "S"
            },
            style = MaterialTheme.typography.caption
        )
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = MaterialTheme.colors.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                )
        )
    }
}

@Composable
private fun WorkoutSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Today's Workout
        Card(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colors.surface,
            elevation = 0.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Workout",
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = { /* TODO */ },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = MaterialTheme.colors.primary
                        )
                    ) {
                        Text("Start")
                    }
                }

                // Workout Preview
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    WorkoutStatCard(
                        title = "Duration",
                        value = "45 min",
                        icon = Icons.Default.Timer
                    )
                    WorkoutStatCard(
                        title = "Exercises",
                        value = "8",
                        icon = Icons.Default.FitnessCenter
                    )
                    WorkoutStatCard(
                        title = "Calories",
                        value = "320",
                        icon = Icons.Default.LocalFireDepartment
                    )
                }
            }
        }

        // Weekly Progress
        Card(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colors.surface,
            elevation = 0.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Weekly Progress",
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Bold
                )
                
                // Progress Bars
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WorkoutProgressBar(
                        label = "Strength",
                        progress = 0.7f,
                        color = Color(0xFF4CAF50)
                    )
                    WorkoutProgressBar(
                        label = "Cardio",
                        progress = 0.4f,
                        color = Color(0xFF2196F3)
                    )
                    WorkoutProgressBar(
                        label = "Flexibility",
                        progress = 0.9f,
                        color = Color(0xFF9C27B0)
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.WorkoutStatCard(
    title: String,
    value: String,
    icon: ImageVector
) {
    Card(
        modifier = Modifier
            .weight(1f)
            .height(80.dp),
        backgroundColor = MaterialTheme.colors.surface,
        elevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colors.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.caption
            )
        }
    }
}

@Composable
private fun WorkoutProgressBar(
    label: String,
    progress: Float,
    color: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.caption
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.caption
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            backgroundColor = color.copy(alpha = 0.1f),
            color = color
        )
    }
}

@Composable
private fun FinancesSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Balance Overview
        Card(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colors.surface,
            elevation = 0.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Balance Overview",
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    FinanceStatCard(
                        title = "Income",
                        value = "+$2,500",
                        color = Color(0xFF4CAF50)
                    )
                    FinanceStatCard(
                        title = "Expenses",
                        value = "-$1,200",
                        color = Color(0xFFF44336)
                    )
                    FinanceStatCard(
                        title = "Balance",
                        value = "$1,300",
                        color = MaterialTheme.colors.primary
                    )
                }
            }
        }

        // Recent Transactions
        Card(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colors.surface,
            elevation = 0.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions",
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { /* TODO */ }) {
                        Text("View All")
                    }
                }

                // Transaction List
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(3) { index ->
                        TransactionItem(
                            title = when(index) {
                                0 -> "Grocery Shopping"
                                1 -> "Netflix Subscription"
                                else -> "Salary"
                            },
                            amount = when(index) {
                                0 -> "-$85.50"
                                1 -> "-$15.99"
                                else -> "+$2,500.00"
                            },
                            date = "Today",
                            isIncome = index == 2
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.FinanceStatCard(
    title: String,
    value: String,
    color: Color
) {
    Card(
        modifier = Modifier
            .weight(1f)
            .height(80.dp),
        backgroundColor = color.copy(alpha = 0.1f),
        elevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.caption,
                color = color
            )
            Text(
                text = value,
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun TransactionItem(
    title: String,
    amount: String,
    date: String,
    isIncome: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isIncome) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                contentDescription = null,
                tint = if (isIncome) Color(0xFF4CAF50) else Color(0xFFF44336),
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.body2
                )
                Text(
                    text = date,
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }
        }
        Text(
            text = amount,
            style = MaterialTheme.typography.body2,
            color = if (isIncome) Color(0xFF4CAF50) else Color(0xFFF44336)
        )
    }
}

@Composable
private fun JournalSection(dailyJournalViewModel: DailyJournalViewModel) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Today's Entry
        Card(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colors.surface,
            elevation = 0.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Journal",
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = { /* TODO */ },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = MaterialTheme.colors.primary
                        )
                    ) {
                        Text("New Entry")
                    }
                }

                // Mood Tracker
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colors.surface,
                    elevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "How are you feeling?",
                            style = MaterialTheme.typography.subtitle2
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            MoodEmoji(emoji = "😊", label = "Happy")
                            MoodEmoji(emoji = "😐", label = "Neutral")
                            MoodEmoji(emoji = "😔", label = "Sad")
                            MoodEmoji(emoji = "😡", label = "Angry")
                            MoodEmoji(emoji = "😴", label = "Tired")
                        }
                    }
                }

                // Quick Notes
                OutlinedTextField(
                    value = "",
                    onValueChange = { /* TODO */ },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Write your thoughts for today...") },
                    maxLines = 3
                )
            }
        }

        // Recent Entries
        Card(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colors.surface,
            elevation = 0.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Recent Entries",
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Bold
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(3) { index ->
                        JournalEntryPreview(
                            date = when(index) {
                                0 -> "Yesterday"
                                1 -> "2 days ago"
                                else -> "3 days ago"
                            },
                            preview = "Today was a productive day. I completed all my tasks and..."
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MoodEmoji(emoji: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.h5
        )
        Text(
            text = label,
            style = MaterialTheme.typography.caption
        )
    }
}

@Composable
private fun JournalEntryPreview(
    date: String,
    preview: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colors.surface,
        elevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = date,
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = preview,
                style = MaterialTheme.typography.body2,
                maxLines = 2
            )
        }
    }
}

enum class HomeSection(
    val title: String,
    val icon: ImageVector
) {
    Tasks("Tasks", Icons.Default.CheckCircle),
    Habits("Habits", Icons.Default.Repeat),
    Meals("Meals", Icons.Default.Restaurant),
    Workout("Workout", Icons.Default.FitnessCenter),
    Finances("Finances", Icons.Default.AccountBalance),
    Journal("Journal", Icons.Default.Book)
}




