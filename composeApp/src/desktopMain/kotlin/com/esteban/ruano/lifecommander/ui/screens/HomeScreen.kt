package com.esteban.ruano.lifecommander.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.esteban.ruano.lifecommander.ui.components.CurrentHabitComposable
import com.esteban.ruano.lifecommander.ui.viewmodels.CalendarViewModel
import com.lifecommander.models.Frequency
import com.lifecommander.models.Habit
import com.esteban.ruano.ui.components.HabitList
import com.esteban.ruano.ui.components.TaskList
import com.lifecommander.models.Reminder
import com.lifecommander.models.Task
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import services.AppPreferencesService
import services.NightBlockService
import ui.components.*
import ui.composables.*
import ui.viewmodels.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    habitsViewModel: HabitsViewModel = koinViewModel(),
    tasksViewModel: TasksViewModel = koinViewModel(),
    calendarViewModel: CalendarViewModel = koinViewModel(),
    nightBlockService: NightBlockService = koinInject(),
    dataStore: DataStore<Preferences> = koinInject(),
    appPreferences: AppPreferencesService = koinInject(),
    appViewModel: AppViewModel = koinViewModel(),
    dailyJournalViewModel: DailyJournalViewModel = koinViewModel(),
    onTaskClick: (String) -> Unit,
    onLogoutClick: () -> Unit,
    onHabitClick: (String) -> Unit,
    onAddTask: (String, String, List<Reminder>, String?, String?, Int?) -> Unit,
    onAddHabit: (String, String, String, Frequency) -> Unit,
    onError: (String) -> Unit,
    onUpdateTask: (String, Task) -> Unit,
    onUpdateHabit: (String, Habit) -> Unit
) {
    var showTokenDialog by remember { mutableStateOf(false) }
    var showNewTaskDialog by remember { mutableStateOf(false) }
    var showNewHabitDialog by remember { mutableStateOf(false) }
    var showNightBlockDialog by remember { mutableStateOf(false) }
    var showOverrideDialog by remember { mutableStateOf(false) }
    var showTimerDialog by remember { mutableStateOf(false) }
    var timerDialogTitle by remember { mutableStateOf("") }
    var timerDialogMessage by remember { mutableStateOf("") }
    val showTimersDialog = appViewModel.appState.collectAsState().value.showTimersDialog
    var expanded by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    var habitToEdit by remember { mutableStateOf<Habit?>(null) }

    val habits by habitsViewModel.habits.collectAsState()
    val tasks by tasksViewModel.tasks.collectAsState()
    val isNightBlockActive by nightBlockService.isNightBlockActive.collectAsState()

    val selectedFilter by tasksViewModel.selectedFilter.collectAsState()
    val tasksLoading by tasksViewModel.loading.collectAsState()
    val habitsLoading by habitsViewModel.loading.collectAsState()
    val timer by appViewModel.timer.collectAsState()
    val timers by appViewModel.timers.collectAsState()
    val paused by appViewModel.paused.collectAsState()

    var error by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val pomodoroCount = dailyJournalViewModel.state.collectAsState().value.pomodoros.size

    // Handle timer events
    LaunchedEffect(Unit) {
        appViewModel.timerEvents.collect { event ->
            when (event) {
                is TimerEvent.TimerStarted -> {
                    timerDialogTitle = event.timer.name
                    timerDialogMessage = "Timer started!"
                    showTimerDialog = true
                }
                is TimerEvent.TimerFinished -> {
                    timerDialogTitle = event.timer.name
                    timerDialogMessage = "Time up!"
                    showTimerDialog = true
                }
            }
        }
    }

    LaunchedEffect(habits, tasks){
        calendarViewModel.refresh()
    }

    LaunchedEffect(Unit){
        coroutineScope.launch {
            appViewModel.timerEndingListenerChannel.consumeAsFlow().collect{
                dailyJournalViewModel.addSamplePomodoro()
                appViewModel.triggerTimerEndActions()
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
        appViewModel.loadTimers()
        dailyJournalViewModel.loadPomodoros()
    }

    TimersDialog(
        show = showTimersDialog,
        onDismiss = { appViewModel.hideTimersDialog() },
        appViewModel = appViewModel,
    )

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
        modifier = Modifier.fillMaxSize()
    ) {
        if (!isNightBlockActive) {
            CurrentHabitComposable(
                habits = habits,
            )
        } else {
            NightBlockQuestionsComposable(
                dailyJournalViewModel
            )
        }

        Row {
            if(!isNightBlockActive){
                if (tasksLoading.not()) {
                    TaskList(
                        taskList = tasks,
                        isRefreshing = false,
                        onPullRefresh = {
                            tasksViewModel.getTasksByFilter()
                        },
                        onTaskClick = { task ->
                            onTaskClick(task.id)
                        },
                        onCheckedChange = { task, checked ->
                            tasksViewModel.changeCheckHabit(task.id, checked)
                        },
                        modifier = Modifier.weight(1f),
                        onEdit = { task ->
                            taskToEdit = task
                            showNewTaskDialog = true
                        },
                        onDelete = { task ->
                            tasksViewModel.deleteTask(task.id)
                        },
                        onReschedule = { task ->
                            tasksViewModel.rescheduleTask(task)
                        },
                        itemWrapper = { content,task ->
                            ContextMenuArea(
                                items = {
                                    listOf(
                                        ContextMenuItem("Edit") {
                                            taskToEdit = task
                                            showNewTaskDialog = true
                                        },
                                        ContextMenuItem("Delete") {
                                            tasksViewModel.deleteTask(task.id)
                                        }
                                    )
                                }
                            ) {
                                content.invoke()
                            }
                        }
                    )
                }
                else {
                    Box(modifier = Modifier.fillMaxHeight().weight(1f)) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
            if (habitsLoading.not()) {
                HabitList(
                    habitList = habits,
                    isRefreshing = false,
                    onPullRefresh = {
                        habitsViewModel.getHabits()
                    },
                    onHabitClick = { habit ->
                        onHabitClick(habit.id)
                    },
                    onCheckedChange = { habit, checked, onComplete ->
                        habitsViewModel.changeCheckHabit(habit.id, checked)
                        onComplete(checked)
                    },
                    modifier = Modifier.weight(1f),
                    onEdit = { habit ->
                        habitToEdit = habit
                        showNewHabitDialog = true
                    },
                    onDelete = { habit ->
                        habitsViewModel.deleteHabit(habit.id)
                    },
                    itemWrapper = { content, habit ->
                        ContextMenuArea(
                            items = {
                                listOf(
                                    ContextMenuItem("Edit") {
                                        habitToEdit = habit
                                        showNewHabitDialog = true
                                    },
                                    ContextMenuItem("Delete") {
                                        habitsViewModel.deleteHabit(habit.id)
                                    }
                                )
                            }
                        ) {
                            content()
                        }
                    }
                )
            } else {
                Box(modifier = Modifier.fillMaxHeight().weight(1f)) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
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




