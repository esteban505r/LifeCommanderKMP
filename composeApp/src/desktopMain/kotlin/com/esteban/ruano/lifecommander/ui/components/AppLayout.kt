package com.esteban.ruano.lifecommander.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.esteban.ruano.lifecommander.timer.TimerPlaybackStatus
import com.esteban.ruano.lifecommander.ui.composables.GeneralFloatingActionButtons
import com.esteban.ruano.lifecommander.ui.viewmodels.TimersViewModel
import com.esteban.ruano.lifecommander.utils.APP_NAME
import com.esteban.ruano.utils.DateUtils.formatDefault
import services.NightBlockService
import services.tasks.models.Priority
import ui.components.NightBlockComposable
import ui.navigation.Screen
import ui.viewmodels.AppViewModel
import ui.viewmodels.DailyJournalViewModel
import ui.viewmodels.HabitsViewModel
import ui.viewmodels.TasksViewModel
import java.time.LocalDateTime
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun AppLayout(
    appViewModel: AppViewModel,
    taskViewModel: TasksViewModel,
    habitViewModel: HabitsViewModel,
    dailyJournalViewModel: DailyJournalViewModel,
    nightBlockService: NightBlockService,
    timersViewModel: TimersViewModel,
    navController: NavController,
    onLogoutClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val isNightBlockActive by nightBlockService.isNightBlockActive.collectAsState()
    val pomodoroCount = timersViewModel.pomodoros.collectAsState().value.size
    var showNightBlockDialog by remember { mutableStateOf(false) }
    var showTimerDialog by remember { mutableStateOf(false) }
    var timerDialogTitle by remember { mutableStateOf("") }
    var timerDialogMessage by remember { mutableStateOf("") }

    val timerPlaybackState by timersViewModel.timerPlaybackState.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        floatingActionButton = {
           /* if (currentRoute != Screen.Finance.route && currentRoute != Screen.FinanceImporter.route) {
                GeneralFloatingActionButtons(
                    onAddTask = { name, notes, reminders, dueDate, scheduledDate, priority ->
                        taskViewModel.addTask(name, dueDate, scheduledDate, notes, priority ?: Priority.NONE.value)
                    },
                    onAddHabit = { name, notes, dateTime, frequency ->
                        habitViewModel.addHabit(name, notes, frequency.value, dateTime)
                    },
                    onError = { *//* Handle error *//* }
                )
            }*/
        },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(APP_NAME)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🍅 $pomodoroCount",
                            style = MaterialTheme.typography.subtitle1,
                            color = MaterialTheme.colors.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(LocalDateTime.now().dayOfWeek.name, style = MaterialTheme.typography.subtitle1)
                        
                        if (timerPlaybackState.status != TimerPlaybackStatus.Stopped) {
                            Spacer(modifier = Modifier.width(16.dp))
                            Box(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colors.onSurface,
                                        shape = MaterialTheme.shapes.small
                                    )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${timerPlaybackState.currentTimer?.name} ${timerPlaybackState.remainingMillis.milliseconds.formatDefault()}",
                                        style = MaterialTheme.typography.subtitle1,
                                        color = MaterialTheme.colors.onSurface
                                    )
                                }
                            }
                        }
                    }
                },
                actions = {
                    if (timerPlaybackState.currentTimer != null) {
                        if (timerPlaybackState.status == TimerPlaybackStatus.Running) {
                            IconButton(onClick = { timersViewModel.pauseTimer() }) {
                                Icon(Icons.Default.Pause, contentDescription = "Pause Timer")
                            }
                        } else {
                            IconButton(onClick = { timersViewModel.resumeTimer() }) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Resume Timer")
                            }
                        }
                        IconButton(onClick = { timersViewModel.stopTimer() }) {
                            Icon(Icons.Default.Stop, contentDescription = "Stop Timer")
                        }
                    }
                    
                    IconButton(onClick = { showNightBlockDialog = true }) {
                        Icon(
                            imageVector = if (isNightBlockActive) Icons.Default.Nightlight
                            else Icons.Default.LightMode,
                            contentDescription = "Night Block",
                            tint = if (isNightBlockActive) MaterialTheme.colors.error
                            else MaterialTheme.colors.primary
                        )
                    }
                    IconButton(onClick = { /* TODO: Refresh */ }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reload")
                    }
                    IconButton(onClick = onLogoutClick) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { padding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(if (isNightBlockActive) Color.Black.copy(alpha = 0.1f) else Color.Transparent)
        ) {
            // Sidebar
            Column(
                modifier = Modifier
                    .width(200.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colors.surface)
                    .padding(vertical = 8.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .clickable { navController.navigate(Screen.Dashboard.route) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = if (navController.currentDestination?.route == Screen.Dashboard.route)
                                    MaterialTheme.colors.primary.copy(alpha = 0.1f)
                                else Color.Transparent,
                                shape = MaterialTheme.shapes.medium
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Dashboard",
                            tint = if (navController.currentDestination?.route == Screen.Dashboard.route)
                                MaterialTheme.colors.primary
                            else MaterialTheme.colors.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Dashboard",
                        style = MaterialTheme.typography.subtitle1,
                        color = if (navController.currentDestination?.route == Screen.Dashboard.route)
                            MaterialTheme.colors.primary
                        else MaterialTheme.colors.onSurface
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .clickable { navController.navigate(Screen.Tasks.route) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = if (navController.currentDestination?.route == Screen.Tasks.route)
                                    MaterialTheme.colors.primary.copy(alpha = 0.1f)
                                else Color.Transparent,
                                shape = MaterialTheme.shapes.medium
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Tasks",
                            tint = if (navController.currentDestination?.route == Screen.Tasks.route)
                                MaterialTheme.colors.primary
                            else MaterialTheme.colors.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tasks",
                        style = MaterialTheme.typography.subtitle1,
                        color = if (navController.currentDestination?.route == Screen.Tasks.route)
                            MaterialTheme.colors.primary
                        else MaterialTheme.colors.onSurface
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .clickable { navController.navigate(Screen.Habits.route) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = if (navController.currentDestination?.route == Screen.Habits.route)
                                    MaterialTheme.colors.primary.copy(alpha = 0.1f)
                                else Color.Transparent,
                                shape = MaterialTheme.shapes.medium
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = "Habits",
                            tint = if (navController.currentDestination?.route == Screen.Habits.route)
                                MaterialTheme.colors.primary
                            else MaterialTheme.colors.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Habits",
                        style = MaterialTheme.typography.subtitle1,
                        color = if (navController.currentDestination?.route == Screen.Habits.route)
                            MaterialTheme.colors.primary
                        else MaterialTheme.colors.onSurface
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .clickable { navController.navigate(Screen.Calendar.route) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = if (navController.currentDestination?.route == Screen.Calendar.route)
                                    MaterialTheme.colors.primary.copy(alpha = 0.1f)
                                else Color.Transparent,
                                shape = MaterialTheme.shapes.medium
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Calendar",
                            tint = if (navController.currentDestination?.route == Screen.Calendar.route)
                                MaterialTheme.colors.primary
                            else MaterialTheme.colors.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Calendar",
                        style = MaterialTheme.typography.subtitle1,
                        color = if (navController.currentDestination?.route == Screen.Calendar.route)
                            MaterialTheme.colors.primary
                        else MaterialTheme.colors.onSurface
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .clickable { navController.navigate(Screen.Finance.route) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = if (navController.currentDestination?.route == Screen.Finance.route)
                                    MaterialTheme.colors.primary.copy(alpha = 0.1f)
                                else Color.Transparent,
                                shape = MaterialTheme.shapes.medium
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = "Financial",
                            tint = if (navController.currentDestination?.route == Screen.Finance.route)
                                MaterialTheme.colors.primary
                            else MaterialTheme.colors.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Financial",
                        style = MaterialTheme.typography.subtitle1,
                        color = if (navController.currentDestination?.route == Screen.Finance.route)
                            MaterialTheme.colors.primary
                        else MaterialTheme.colors.onSurface
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .clickable { navController.navigate(Screen.Timers.route) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = if (navController.currentDestination?.route == Screen.Timers.route)
                                    MaterialTheme.colors.primary.copy(alpha = 0.1f)
                                else Color.Transparent,
                                shape = MaterialTheme.shapes.medium
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Timers",
                            tint = if (navController.currentDestination?.route == Screen.Timers.route)
                                MaterialTheme.colors.primary
                            else MaterialTheme.colors.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Timers",
                        style = MaterialTheme.typography.subtitle1,
                        color = if (navController.currentDestination?.route == Screen.Timers.route)
                            MaterialTheme.colors.primary
                        else MaterialTheme.colors.onSurface
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .clickable { navController.navigate(Screen.Settings.route) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = if (navController.currentDestination?.route == Screen.Settings.route)
                                    MaterialTheme.colors.primary.copy(alpha = 0.1f)
                                else Color.Transparent,
                                shape = MaterialTheme.shapes.medium
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = if (navController.currentDestination?.route == Screen.Settings.route)
                                MaterialTheme.colors.primary
                            else MaterialTheme.colors.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.subtitle1,
                        color = if (navController.currentDestination?.route == Screen.Settings.route)
                            MaterialTheme.colors.primary
                        else MaterialTheme.colors.onSurface
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .clickable { navController.navigate(Screen.Pomodoros.route) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = if (navController.currentDestination?.route == Screen.Pomodoros.route)
                                    MaterialTheme.colors.primary.copy(alpha = 0.1f)
                                else Color.Transparent,
                                shape = MaterialTheme.shapes.medium
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LockClock,
                            contentDescription = "Pomodoros",
                            tint = if (navController.currentDestination?.route == Screen.Pomodoros.route)
                                MaterialTheme.colors.primary
                            else MaterialTheme.colors.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pomodoros",
                        style = MaterialTheme.typography.subtitle1,
                        color = if (navController.currentDestination?.route == Screen.Pomodoros.route)
                            MaterialTheme.colors.primary
                        else MaterialTheme.colors.onSurface
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .clickable { navController.navigate(Screen.Meals.route) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = if (navController.currentDestination?.route == Screen.Meals.route)
                                    MaterialTheme.colors.primary.copy(alpha = 0.1f)
                                else Color.Transparent,
                                shape = MaterialTheme.shapes.medium
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fastfood,
                            contentDescription = "Meals",
                            tint = if (navController.currentDestination?.route == Screen.Meals.route)
                                MaterialTheme.colors.primary
                            else MaterialTheme.colors.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Meals",
                        style = MaterialTheme.typography.subtitle1,
                        color = if (navController.currentDestination?.route == Screen.Meals.route)
                            MaterialTheme.colors.primary
                        else MaterialTheme.colors.onSurface
                    )
                }

                //Workout
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .clickable { navController.navigate(Screen.Workout.route) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = if (navController.currentDestination?.route == Screen.Workout.route)
                                    MaterialTheme.colors.primary.copy(alpha = 0.1f)
                                else Color.Transparent,
                                shape = MaterialTheme.shapes.medium
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = "Workout",
                            tint = if (navController.currentDestination?.route == Screen.Workout.route)
                                MaterialTheme.colors.primary
                            else MaterialTheme.colors.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Workouts",
                        style = MaterialTheme.typography.subtitle1,
                        color = if (navController.currentDestination?.route == Screen.Workout.route)
                            MaterialTheme.colors.primary
                        else MaterialTheme.colors.onSurface
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .clickable { navController.navigate(Screen.Journal.route) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = if (navController.currentDestination?.route == Screen.Journal.route)
                                    MaterialTheme.colors.primary.copy(alpha = 0.1f)
                                else Color.Transparent,
                                shape = MaterialTheme.shapes.medium
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = "Journal",
                            tint = if (navController.currentDestination?.route == Screen.Journal.route)
                                MaterialTheme.colors.primary
                            else MaterialTheme.colors.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Journal",
                        style = MaterialTheme.typography.subtitle1,
                        color = if (navController.currentDestination?.route == Screen.Journal.route)
                            MaterialTheme.colors.primary
                        else MaterialTheme.colors.onSurface
                    )
                }
            }

            // Main content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                content()
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
                    habits = emptyList(), // TODO: Pass habits from parent
                    onOverride = { reason ->
                        // TODO: Handle override
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

    if (showTimerDialog) {
        AlertDialog(
            onDismissRequest = { showTimerDialog = false },
            title = { Text(timerDialogTitle) },
            text = { Text(timerDialogMessage) },
            confirmButton = {
                Button(
                    onClick = { showTimerDialog = false }
                ) {
                    Text("OK")
                }
            }
        )
    }
} 