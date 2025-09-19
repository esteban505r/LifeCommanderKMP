package com.esteban.ruano.lifecommander.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.esteban.ruano.lifecommander.models.TaskFilters
import com.esteban.ruano.lifecommander.ui.components.*
import com.esteban.ruano.ui.components.HabitList
import com.esteban.ruano.ui.components.TaskList
import com.esteban.ruano.utils.DateUIUtils.toLocalDateTime
import com.esteban.ruano.utils.DateUIUtils.toLocalTime
import com.lifecommander.models.Habit
import com.lifecommander.models.Task
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import services.NightBlockService
import ui.components.*
import ui.composables.*
import ui.viewmodels.*
import com.esteban.ruano.lifecommander.ui.viewmodels.TimersViewModel
import com.esteban.ruano.lifecommander.utils.TaskUtils.dueAtMillis
import com.esteban.ruano.lifecommander.utils.TaskUtils.isOverdue
import com.esteban.ruano.utils.DateUIUtils.getCurrentDateTime
import com.esteban.ruano.utils.HabitsUtils.isOverdue
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toLocalDateTime as toLocalDateTimeKt
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import ui.composables.focus.FocusMixedList

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun DashboardScreen(
    habitsViewModel: HabitsViewModel = koinViewModel(),
    tasksViewModel: TasksViewModel = koinViewModel(),
    dashboardViewModel: DashboardViewModel = koinViewModel(),
    nightBlockService: NightBlockService = koinInject(),
    dailyJournalViewModel: DailyJournalViewModel = koinViewModel(),
    onTaskClick: (Task) -> Unit,
    onHabitClick: (Habit) -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToHabits: () -> Unit,
    onNavigateToStatistics: () -> Unit = {}
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

    val timersViewModel: TimersViewModel = koinViewModel()
    val pomodoros by timersViewModel.pomodoros.collectAsState()

    // Helper: Meals per day this week as dateMap
    val mealsPerDayDateMap = remember(dashboardViewModel.mealsLoggedPerDayThisWeek.collectAsState().value) {
        val now =
            getCurrentDateTime(TimeZone.currentSystemDefault()).date
        val startOfWeek = now.minus(DatePeriod(days = now.dayOfWeek.ordinal))
        (0..6).associate { offset ->
            val date = startOfWeek.plus(DatePeriod(days = offset))
            date to (dashboardViewModel.mealsLoggedPerDayThisWeek.value.getOrNull(offset) ?: 0)
        }
    }

    LaunchedEffect(
        Unit
    ) {
        val now =
            getCurrentDateTime(TimeZone.currentSystemDefault()).date
        val startOfWeek = now.minus(DatePeriod(days = now.dayOfWeek.ordinal))
        timersViewModel.loadPomodorosByDateRange(
            startOfWeek.toJavaLocalDate(),
            startOfWeek.plus(DatePeriod(days = 6)).toJavaLocalDate()
        )
    }

    val pomodorosPerDayThisWeek = remember(pomodoros) {
        val now =
            getCurrentDateTime(TimeZone.currentSystemDefault()).date
        val startOfWeek = now.minus(DatePeriod(days = now.dayOfWeek.ordinal))
        val counts = MutableList(7) {
            startOfWeek.plus(DatePeriod(days = it))
        }
        counts.map { date ->
            val count = pomodoros.count {
                println("Comparing  ${it.startDateTime.toLocalDateTime().date} with $date")
                it.startDateTime.toLocalDateTime().date == date
            }
            println(count)
            count
        }
    }

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

    // Compute weekly tasks completed (placeholder: use tasksViewModel.tasks if available)
    val tasksCompletedPerDayThisWeek by dashboardViewModel.tasksCompletedPerDayThisWeek.collectAsState()

    val overdueHabits by dashboardViewModel.overdueHabitsList.collectAsState()
    val overdueTasks by dashboardViewModel.overdueTasksList.collectAsState()

    var focusModeExpanded by remember { mutableStateOf(false) }
    // Check for Night Block activation every minute
    /*LaunchedEffect(Unit) {
        while (true) {
            nightBlockService.checkAndActivateNightBlock()
            delay(60000) // 1 minute
        }
    }*/

    if (error) {
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
        dashboardViewModel.refreshDashboard()
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
            tasksViewModel.addTask(name, dueDate, scheduledDate, note, priority ?: 0)
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
        onAddHabit = { name, note, dateTime, frequency, reminders ->
            habitsViewModel.addHabit(name, note, frequency.value, dateTime, reminders)
        },
        onUpdateHabit = { id, habit ->
            habitsViewModel.updateHabit(id, habit)
        },
        onError = {
            errorMessage = it
            error = true
        }
    )

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        if (isNightBlockActive) {
            NightBlockQuestionsComposable(dailyJournalViewModel)
        } else {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Main content area (left side)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
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
                                text = "Dashboard",
                                style = MaterialTheme.typography.h4,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Welcome back! Here's your overview for today.",
                                style = MaterialTheme.typography.subtitle1,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    // Main content sections with grid layout
                    if(!focusModeExpanded){
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            // Statistics navigation button
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                    backgroundColor = MaterialTheme.colors.primary,
                                    elevation = 4.dp,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                "Detailed Statistics",
                                                style = MaterialTheme.typography.h6,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colors.onPrimary
                                            )
                                            Text(
                                                "View comprehensive charts and analytics",
                                                style = MaterialTheme.typography.body2,
                                                color = MaterialTheme.colors.onPrimary.copy(alpha = 0.8f)
                                            )
                                        }
                                        Button(
                                            onClick = onNavigateToStatistics,
                                            colors = ButtonDefaults.buttonColors(
                                                backgroundColor = MaterialTheme.colors.onPrimary,
                                                contentColor = MaterialTheme.colors.primary
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Analytics,
                                                contentDescription = "Statistics",
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("View Statistics")
                                        }
                                    }
                                }
                            }

                            // Weekly Overview and Pomodoro charts side by side
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                    backgroundColor = MaterialTheme.colors.surface,
                                    elevation = 4.dp,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Text(
                                            "Quick Overview",
                                            style = MaterialTheme.typography.h6,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colors.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                            QuickStatItem(
                                                label = "Tasks Completed",
                                                value = "${taskStats?.completed ?: 0}/${taskStats?.total ?: 0}",
                                                icon = Icons.Default.CheckCircle,
                                                color = Color(0xFF2196F3)
                                            )
                                            QuickStatItem(
                                                label = "Habits Completed",
                                                value = "${habitStats?.completed ?: 0}/${habitStats?.total ?: 0}",
                                                icon = Icons.Default.TrendingUp,
                                                color = Color(0xFF9C27B0)
                                            )
                                            QuickStatItem(
                                                label = "Pomodoros Today",
                                                value = "${pomodorosPerDayThisWeek.lastOrNull() ?: 0}",
                                                icon = Icons.Default.Timer,
                                                color = Color(0xFFE53935)
                                            )
                                        }
                                    }
                                }
                            }

                            // Meals and Workout charts below, side by side
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                    backgroundColor = MaterialTheme.colors.surface,
                                    elevation = 4.dp,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Text(
                                            "Today's Activity",
                                            style = MaterialTheme.typography.h6,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colors.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                            QuickStatItem(
                                                label = "Meals Logged",
                                                value = "${dashboardViewModel.mealsLogged.collectAsState().value}",
                                                icon = Icons.Default.Restaurant,
                                                color = Color(0xFFFFA726)
                                            )
                                            QuickStatItem(
                                                label = "Workouts",
                                                value = "${dashboardViewModel.workoutsCompletedPerDayThisWeek.collectAsState().value.lastOrNull() ?: 0}",
                                                icon = Icons.Default.FitnessCenter,
                                                color = Color(0xFF4CAF50)
                                            )
                                            QuickStatItem(
                                                label = "Calories Burned",
                                                value = "${dashboardViewModel.caloriesBurned.collectAsState().value}",
                                                icon = Icons.Default.LocalFireDepartment,
                                                color = Color(0xFFFF5722)
                                            )
                                        }
                                    }
                                }
                            }

                            // Summary sections in grid
                            items(sections) { section ->
                                AnimatedVisibility(
                                    visible = expandedSections[section] == true,
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
                                    HomeSectionCard(
                                        section = section,
                                        isExpanded = true,
                                        onToggleExpand = { expandedSections[section] = false },
                                        content = {
                                            when (section) {
                                                HomeSection.Tasks -> TasksSummary(
                                                    nextTask = nextTask,
                                                    taskStats = taskStats,
                                                    overdueTasks = overdueTasks,
                                                    onMarkTaskDone = { task ->
                                                        coroutineScope.launch {
                                                            tasksViewModel.markTaskDone(task)
                                                            dashboardViewModel.refreshDashboard()
                                                        }
                                                    },
                                                    onViewAllClick = onNavigateToTasks,
                                                    isExpanded = true,
                                                    currentTime = currentTime
                                                )

                                                HomeSection.Habits -> HabitsSummary(
                                                    nextHabit = nextHabit,
                                                    habitStats = habitStats,
                                                    overdueHabits = overdueHabits,
                                                    onMarkHabitDone = { habit ->
                                                        coroutineScope.launch {
                                                            habitsViewModel.markHabitDone(habit)
                                                            dashboardViewModel.refreshDashboard()
                                                        }
                                                    },
                                                    onViewAllClick = onNavigateToHabits,
                                                    isExpanded = true,
                                                    currentTime = currentTime
                                                )

                                                HomeSection.Meals -> MealsSummary(
                                                    todayCalories = dashboardViewModel.todayCalories.collectAsState().value,
                                                    mealsLogged = dashboardViewModel.mealsLogged.collectAsState().value,
                                                    nextMeal = dashboardViewModel.nextMeal.collectAsState().value,
                                                    weeklyMealLogging = dashboardViewModel.weeklyMealLogging.collectAsState().value,
                                                    plannedMeals = dashboardViewModel.plannedMealsPerDayThisWeek.collectAsState().value.sum(),
                                                    unexpectedMeals = dashboardViewModel.unexpectedMealsPerDayThisWeek.collectAsState().value.sum()
                                                )

                                                HomeSection.Workout -> WorkoutSummary(
                                                    todayWorkout = dashboardViewModel.todayWorkout.collectAsState().value,
                                                    caloriesBurned = dashboardViewModel.caloriesBurned.collectAsState().value,
                                                    workoutStreak = dashboardViewModel.workoutStreak.collectAsState().value,
                                                    weeklyWorkoutCompletion = dashboardViewModel.weeklyWorkoutCompletion.collectAsState().value
                                                )

                                                HomeSection.Finances -> FinanceSummary(
                                                    recentTransactions = dashboardViewModel.recentTransactions.collectAsState().value,
                                                    accountBalance = dashboardViewModel.accountBalance.collectAsState().value
                                                )

                                                HomeSection.Journal -> JournalSummary(
                                                    journalCompleted = dashboardViewModel.journalCompleted.collectAsState().value,
                                                    journalStreak = dashboardViewModel.journalStreak.collectAsState().value,
                                                    recentJournalEntries = dashboardViewModel.recentJournalEntries.collectAsState().value
                                                )
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                    else{
                        FocusMixedList(
                            tasks = tasks,
                            habits = habits,

                            isRefreshing = dashboardLoading,
                            onPullRefresh = { dashboardViewModel.refreshDashboard() },

                            // TASK actions
                            onTaskClick = onTaskClick,
                            onTaskCheckedChange = { task, checked ->
                                tasksViewModel.changeCheckTask(task.id, checked)
                                dashboardViewModel.refreshDashboard()
                            },
                            onTaskEdit = { tasksViewModel.updateTask(it.id,it) },
                            onTaskDelete = { tasksViewModel.deleteTask(it.id) },
                            onTaskReschedule = { },

                            // HABIT actions
                            onHabitClick = onHabitClick,
                            onHabitCheckedChange = { habit, checked, onComplete ->
                                habitsViewModel.changeCheckHabit(habit.id, checked)
                            },
                            onHabitComplete = { habit, done ->
                                habitsViewModel.changeCheckHabit(habit.id, done)
                            },
                            onHabitEdit = { habitsViewModel.updateHabit(it.id,it) },
                            onHabitDelete = { habitsViewModel.deleteHabit(it.id) },
                            taskItemWrapper = { content, _ -> content() },
                            habitItemWrapper = { content, _ -> content() },
                            habitIsDone = { it.done?:false },
                            habitIsOverdue = { it.isOverdue() },
                            taskIsDone = {it.done},
                            taskIsOverdue = { it.isOverdue() },
                            taskDueMillis = { it.dueAtMillis() },
                        )
                    }

                }

                // Right sidebar with toggle buttons
                Card(
                    modifier = Modifier
                        .width(280.dp)
                        .fillMaxHeight()
                        .padding(16.dp),
                    elevation = 4.dp,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Sidebar header
                        Text(
                            text = "Quick Actions",
                            style = MaterialTheme.typography.h6,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Section toggles
                        Text(
                            text = "Show/Hide Sections",
                            style = MaterialTheme.typography.subtitle2,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 24.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (focusModeExpanded)
                                            Color(0xFFB45253)
                                        else
                                            Color(0xFFFCB53B).copy(alpha = 0.1f)
                                    )
                                    .clickable {
                                        focusModeExpanded = !focusModeExpanded
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lightbulb,
                                        contentDescription = null,
                                        tint = if (focusModeExpanded)
                                            Color.White
                                        else
                                            MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Focus mode",
                                        style = MaterialTheme.typography.body2,
                                        fontWeight = if (focusModeExpanded) FontWeight.Normal else FontWeight.SemiBold,
                                        color = if (focusModeExpanded)
                                            Color.White
                                        else

                                            MaterialTheme.colors.primary
                                    )
                                }

                                Icon(
                                    imageVector = if (focusModeExpanded)
                                        Icons.Default.Visibility
                                    else
                                        Icons.Default.VisibilityOff,
                                    contentDescription = if (focusModeExpanded) "Hide" else "Show",
                                    tint = if (focusModeExpanded)
                                        Color.White
                                    else
                                        MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            if (!focusModeExpanded) {
                                sections.forEach { section ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (expandedSections[section] == true)
                                                    MaterialTheme.colors.primary.copy(alpha = 0.1f)
                                                else
                                                    MaterialTheme.colors.surface
                                            )
                                            .clickable {
                                                expandedSections[section] = !(expandedSections[section] ?: true)
                                            }
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Icon(
                                                imageVector = section.icon,
                                                contentDescription = null,
                                                tint = if (expandedSections[section] == true)
                                                    MaterialTheme.colors.primary
                                                else
                                                    MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                text = section.title,
                                                style = MaterialTheme.typography.body2,
                                                fontWeight = if (expandedSections[section] == true) FontWeight.SemiBold else FontWeight.Normal,
                                                color = if (expandedSections[section] == true)
                                                    MaterialTheme.colors.primary
                                                else
                                                    MaterialTheme.colors.onSurface
                                            )
                                        }

                                        Icon(
                                            imageVector = if (expandedSections[section] == true)
                                                Icons.Default.Visibility
                                            else
                                                Icons.Default.VisibilityOff,
                                            contentDescription = if (expandedSections[section] == true) "Hide" else "Show",
                                            tint = if (expandedSections[section] == true)
                                                MaterialTheme.colors.primary
                                            else
                                                MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Quick action buttons
                        Text(
                            text = "Quick Actions",
                            style = MaterialTheme.typography.subtitle2,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showNewTaskDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = MaterialTheme.colors.primary
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("New Task")
                            }

                            Button(
                                onClick = { showNewHabitDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = MaterialTheme.colors.secondary
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("New Habit")
                            }

                            OutlinedButton(
                                onClick = { /* TODO: Implement refresh */ },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Refresh")
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Stats summary
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = MaterialTheme.colors.surface,
                            elevation = 2.dp,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Today's Progress",
                                    style = MaterialTheme.typography.subtitle2,
                                    fontWeight = FontWeight.Bold
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "${taskStats?.completed ?: 0}",
                                            style = MaterialTheme.typography.h6,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colors.primary
                                        )
                                        Text(
                                            text = "Tasks Done",
                                            style = MaterialTheme.typography.caption,
                                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = "${habitStats?.completed ?: 0}",
                                            style = MaterialTheme.typography.h6,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colors.secondary
                                        )
                                        Text(
                                            text = "Habits Done",
                                            style = MaterialTheme.typography.caption,
                                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }

                                // Overdue section
                                if (dashboardViewModel.overdueTasks.collectAsState().value > 0 ||
                                    dashboardViewModel.overdueHabits.collectAsState().value > 0
                                ) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Divider(color = MaterialTheme.colors.onSurface.copy(alpha = 0.1f))
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "Overdue Items",
                                        style = MaterialTheme.typography.subtitle2,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colors.error
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = "${dashboardViewModel.overdueTasks.collectAsState().value}",
                                                style = MaterialTheme.typography.h6,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colors.error
                                            )
                                            Text(
                                                text = "Overdue Tasks",
                                                style = MaterialTheme.typography.caption,
                                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                            )
                                        }

                                        Column {
                                            Text(
                                                text = "${dashboardViewModel.overdueHabits.collectAsState().value}",
                                                style = MaterialTheme.typography.h6,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colors.error
                                            )
                                            Text(
                                                text = "Overdue Habits",
                                                style = MaterialTheme.typography.caption,
                                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNightBlockDialog) {
        AlertDialog(
            onDismissRequest = { showNightBlockDialog = false },
            title = { Text("Night Block") },
            text = {
                NightBlockQuestionsComposable(
                    viewModel = dailyJournalViewModel,
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(cardElevation, RoundedCornerShape(16.dp))
            .animateContentSize(),
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
                IconButton(
                    onClick = onToggleExpand,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close section",
                        tint = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            // Content
            content()
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
            text = when (day) {
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
                            title = when (index) {
                                0 -> "Grocery Shopping"
                                1 -> "Netflix Subscription"
                                else -> "Salary"
                            },
                            amount = when (index) {
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
                            date = when (index) {
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

@Composable
fun OverdueItemsSlider(
    overdueHabits: List<Habit>,
    overdueTasks: List<Task>,
    onMarkHabitDone: (Habit) -> Unit,
    onMarkTaskDone: (Task) -> Unit
) {
    if (overdueHabits.isNotEmpty()) {
        Text("Overdue Habits", style = MaterialTheme.typography.h6)
        LazyRow {
            items(overdueHabits.size) { habit ->
                val habit = overdueHabits[habit]
                OverdueHabitCard(habit, onMarkHabitDone)
            }
        }
    }
    if (overdueTasks.isNotEmpty()) {
        Text("Overdue Tasks", style = MaterialTheme.typography.h6)
        LazyRow {
            items(overdueTasks.size) { task ->
                val task = overdueTasks[task]
                OverdueTaskCard(task, onMarkTaskDone)
            }
        }
    }
}

@Composable
fun OverdueHabitCard(habit: Habit, onMarkDone: (Habit) -> Unit) {
    Card(
        modifier = Modifier.padding(8.dp).width(220.dp).height(120.dp),
        backgroundColor = MaterialTheme.colors.error.copy(alpha = 0.1f)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(habit.name, style = MaterialTheme.typography.subtitle1, fontWeight = FontWeight.Bold)
            Text("Time: " + (habit.dateTime?.toLocalDateTime()?.toLocalTime()?.toString() ?: "-"))
            Text("Frequency: ${habit.frequency}")
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { onMarkDone(habit) },
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
            ) {
                Text("Mark as done")
            }
        }
    }
}

@Composable
fun OverdueTaskCard(task: Task, onMarkDone: (Task) -> Unit) {
    Card(
        modifier = Modifier.padding(8.dp).width(220.dp).height(120.dp),
        backgroundColor = MaterialTheme.colors.error.copy(alpha = 0.1f)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(task.name, style = MaterialTheme.typography.subtitle1, fontWeight = FontWeight.Bold)
            Text("Due: " + (task.dueDateTime?.toLocalDateTime()?.toLocalTime()?.toString() ?: "-"))
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { onMarkDone(task) },
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
            ) {
                Text("Mark as done")
            }
        }
    }
}

@Composable
private fun QuickStatItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Column(
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
            color = MaterialTheme.colors.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
        )
    }
}




