package com.esteban.ruano.lifecommander.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import services.NightBlockService
import ui.components.NightBlockComposable
import com.esteban.ruano.lifecommander.ui.composables.FloatingActionButtons
import services.tasks.models.Priority
import ui.navigation.Screen
import ui.viewmodels.AppViewModel
import ui.viewmodels.DailyJournalViewModel
import ui.viewmodels.HabitsViewModel
import ui.viewmodels.TasksViewModel
import utils.DateUtils.parseTime
import java.time.LocalDateTime

@Composable
fun AppLayout(
    appViewModel: AppViewModel,
    taskViewModel: TasksViewModel,
    habitViewModel: HabitsViewModel,
    dailyJournalViewModel: DailyJournalViewModel,
    nightBlockService: NightBlockService,
    navController: NavController,
    onLogoutClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val timer by appViewModel.timer.collectAsState()
    val timers by appViewModel.timers.collectAsState()
    val paused by appViewModel.paused.collectAsState()
    val isNightBlockActive by nightBlockService.isNightBlockActive.collectAsState()
    val pomodoroCount = dailyJournalViewModel.state.collectAsState().value.pomodoros.size
    var showNightBlockDialog by remember { mutableStateOf(false) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        floatingActionButton = {
            if (currentRoute != Screen.Finance.route) {
                FloatingActionButtons(
                    onAddTask = { name, notes, reminders, dueDate, scheduledDate, priority ->
                        taskViewModel.addTask(name, dueDate, scheduledDate, notes, priority ?: Priority.NONE.value)
                    },
                    onAddHabit = { name, notes, dateTime, frequency ->
                        habitViewModel.addHabit(name, notes, frequency.value, dateTime)
                    },
                    onError = { /* Handle error */ }
                )
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Life Commander")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🍅 $pomodoroCount",
                            style = MaterialTheme.typography.subtitle1,
                            color = MaterialTheme.colors.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(LocalDateTime.now().dayOfWeek.name, style = MaterialTheme.typography.subtitle1)
                    }
                },
                actions = {
                    if (timer != null) {
                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .border(
                                    width = 1.dp,
                                    color = Color.White,
                                    shape = MaterialTheme.shapes.small
                                )
                        ) {
                            Text(
                                text = "${timer!!.name} - ${timer!!.timeRemaining.parseTime()}",
                                style = MaterialTheme.typography.h6,
                                color = Color.White,
                                modifier = Modifier.padding(vertical = 4.dp, horizontal = 6.dp)
                            )
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
                    if ((timer == null && timers.isNotEmpty()) || paused) {
                        IconButton(onClick = { appViewModel.playTimer() }) {
                            Icon(Icons.Default.PlayCircle, contentDescription = "Timer")
                        }
                    }
                    if (timer != null && !paused) {
                        IconButton(onClick = { appViewModel.pauseTimer() }) {
                            Icon(Icons.Default.Pause, contentDescription = "Timer")
                        }
                    }
                    if (timer != null) {
                        IconButton(onClick = { appViewModel.previousTimer() }) {
                            Icon(Icons.Default.SkipPrevious, contentDescription = "Previous Timer")
                        }
                        IconButton(onClick = { appViewModel.stopTimer() }) {
                            Icon(Icons.Default.Stop, contentDescription = "Stop Timer")
                        }
                        IconButton(onClick = { appViewModel.nextTimer() }) {
                            Icon(Icons.Default.SkipNext, contentDescription = "Next Timer")
                        }
                    }
                    IconButton(onClick = { appViewModel.showTimersDialog() }) {
                        Icon(Icons.Default.Timer, contentDescription = "Timers")
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
                    .width(72.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colors.surface)
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { navController.navigate(Screen.Dashboard.route) },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = if (navController.currentDestination?.route == Screen.Dashboard.route)
                                MaterialTheme.colors.primary.copy(alpha = 0.1f)
                            else Color.Transparent,
                            shape = MaterialTheme.shapes.medium
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Dashboard",
                        tint = if (navController.currentDestination?.route == Screen.Dashboard.route)
                            MaterialTheme.colors.primary
                        else MaterialTheme.colors.onSurface
                    )
                }

                IconButton(
                    onClick = { navController.navigate(Screen.Calendar.route) },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = if (navController.currentDestination?.route == Screen.Calendar.route)
                                MaterialTheme.colors.primary.copy(alpha = 0.1f)
                            else Color.Transparent,
                            shape = MaterialTheme.shapes.medium
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Calendar",
                        tint = if (navController.currentDestination?.route == Screen.Calendar.route)
                            MaterialTheme.colors.primary
                        else MaterialTheme.colors.onSurface
                    )
                }

                IconButton(
                    onClick = { navController.navigate(Screen.Finance.route) },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = if (navController.currentDestination?.route == Screen.Finance.route)
                                MaterialTheme.colors.primary.copy(alpha = 0.1f)
                            else Color.Transparent,
                            shape = MaterialTheme.shapes.medium
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = "Financial",
                        tint = if (navController.currentDestination?.route == Screen.Finance.route)
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
} 