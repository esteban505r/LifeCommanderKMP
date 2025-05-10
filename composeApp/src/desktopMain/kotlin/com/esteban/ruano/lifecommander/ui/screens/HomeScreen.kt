package ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import services.AppPreferencesService
import services.NightBlockService
import services.habits.models.HabitResponse
import services.tasks.models.TaskResponse
import ui.components.*
import ui.composables.*
import ui.viewmodels.*
import utils.DateUtils.parseTime
import java.time.LocalDateTime

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    habitsViewModel: HabitsViewModel = koinViewModel(),
    tasksViewModel: TasksViewModel = koinViewModel(),
    nightBlockService: NightBlockService = koinInject(),
    dataStore: DataStore<Preferences> = koinInject(),
    appPreferences: AppPreferencesService = koinInject(),
    appViewModel: AppViewModel = koinViewModel(),
    dailyJournalViewModel: DailyJournalViewModel = koinViewModel(),
    onTaskClick: (String) -> Unit,
    onLogoutClick: () -> Unit,
    onHabitClick: (String) -> Unit
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
    var showCalendarView by remember { mutableStateOf(false) }

    var taskToEdit by remember { mutableStateOf<TaskResponse?>(null) }
    var habitToEdit by remember { mutableStateOf<HabitResponse?>(null) }

    val habits by habitsViewModel.habits.collectAsState()
    val tasks by tasksViewModel.tasks.collectAsState()
    val isNightBlockActive by nightBlockService.isNightBlockActive.collectAsState()

    val selectedFilter by tasksViewModel.selectedFilter.collectAsState()
    val tasksLoading by tasksViewModel.loading.collectAsState()
    val habitsLoading by habitsViewModel.loading.collectAsState()
    val timer by appViewModel.timer.collectAsState()
    val timers by appViewModel.timers.collectAsState()
    val paused by appViewModel.paused.collectAsState()
    val coroutineScope = rememberCoroutineScope()

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
            kotlinx.coroutines.delay(60000) // 1 minute
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
        onAddHabit = { name, note, frequency, dateTime ->
            habitsViewModel.addHabit(name, note, frequency, dateTime)
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
        floatingActionButton = {
            FloatingHabitsComposable(
                onHabitClick = {

                }
            )
        },
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically,modifier = Modifier.onClick(
                        matcher = PointerMatcher.mouse(PointerButton.Secondary),
                        onClick = {
                            dailyJournalViewModel.removeLastPomodoro()
                        }
                    ).onClick(
                        matcher = PointerMatcher.mouse(PointerButton.Primary),
                        onClick = {
                            dailyJournalViewModel.addSamplePomodoro()
                        }
                    )) {
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
                        IconButton(
                            onClick = {
                                appViewModel.playTimer()
                            },
                        ) {
                            Icon(
                                Icons.Default.PlayCircle,
                                contentDescription = "Timer"
                            )
                        }
                    }
                    if(timer!=null && !paused){
                        IconButton(
                            onClick = {
                                appViewModel.pauseTimer()
                            },
                        ) {
                            Icon(
                                Icons.Default.Pause,
                                contentDescription = "Timer"
                            )
                        }
                    }
                    if(timer != null ){
                        IconButton(
                            onClick = {
                                appViewModel.previousTimer()
                            },
                        ) {
                            Icon(
                                Icons.Default.SkipPrevious,
                                contentDescription = "Previous Timer"
                            )
                        }
                        IconButton(
                            onClick = {
                                appViewModel.stopTimer()
                            },
                        ) {
                            Icon(
                                Icons.Default.Stop,
                                contentDescription = "Stop Timer"
                            )
                        }
                        IconButton(
                            onClick = {
                                appViewModel.nextTimer()
                            },
                        ) {
                            Icon(
                                Icons.Default.SkipNext,
                                contentDescription = "Next Timer"
                            )
                        }
                    }
                    IconButton(onClick = {
                        appViewModel.showTimersDialog()
                    }) {
                        Icon(Icons.Default.Timer, contentDescription = "Timers")
                    }
                    IconButton(onClick = {
                        habitsViewModel.getHabits()
                        tasksViewModel.getTasksByFilter()
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reload")
                    }
                    IconButton(onClick = { onLogoutClick() }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                    IconButton(onClick = { showCalendarView = !showCalendarView }) {
                        Icon(
                            imageVector = if (showCalendarView) Icons.Default.ViewList else Icons.Default.CalendarMonth,
                            contentDescription = if (showCalendarView) "Show List View" else "Show Calendar View"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(if (isNightBlockActive) Color.Black.copy(alpha = 0.1f) else Color.Transparent)
        ) {
            if(!showCalendarView){
                if (!isNightBlockActive) {
                    CurrentHabitComposable(
                        habits = habits,
                    )
                } else {
                    NightBlockQuestionsComposable(
                        dailyJournalViewModel
                    )
                }
            }
            if (showCalendarView) {
                CalendarComposable(
                    tasks = tasks,
                    habits = habits,
                    onTaskClick = onTaskClick,
                    onHabitClick = onHabitClick,
                    modifier = Modifier.weight(1f)
                )
            } else {
                Row {
                    if(!isNightBlockActive){
                        if (tasksLoading.not()) {
                            TasksList(
                                tasks = tasks,
                                modifier = Modifier
                                    .weight(1f),
                                onTaskClick = onTaskClick,
                                selectedFilter = selectedFilter,
                                onFilterChange = {
                                    tasksViewModel.changeFilter(it)
                                },
                                onCheckedChange = { id, checked ->
                                    tasksViewModel.changeCheckHabit(id, checked)
                                },
                                onNewTaskClick = {
                                    taskToEdit = null
                                    showNewTaskDialog = true
                                },
                                onEditTaskClick = {
                                    taskToEdit = it
                                    showNewTaskDialog = true
                                },
                                onDeleteTaskClick = {
                                    tasksViewModel.deleteTask(it)
                                },
                                onRescheduleTask = {
                                    tasksViewModel.rescheduleTask(it)
                                },
                            )
                        }
                        else {
                            Box(modifier = Modifier.fillMaxHeight().weight(1f)) {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                            }
                        }
                    }
                    if (habitsLoading.not()) {
                        HabitsList(
                            modifier = Modifier
                                .weight(1f),
                            onCheckedChange = { id, checked ->
                                habitsViewModel.changeCheckHabit(id, checked)
                            },
                            habits = habits,
                            onNewHabitClick = {
                                habitToEdit = null
                                showNewHabitDialog = true
                            },
                            onEditHabitClick = {
                                habitToEdit = it
                                showNewHabitDialog = true
                            },
                            onDeleteHabitClick = {
                                habitsViewModel.deleteHabit(it)
                            },
                            nightBlockService = nightBlockService
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxHeight().weight(1f)) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            /* SectionTitle(title = "Habits")
             LazyRow(
                 modifier = Modifier.fillMaxWidth(),
                 contentPadding = PaddingValues(horizontal = 16.dp),
                 horizontalArrangement = Arrangement.spacedBy(8.dp)
             ) {
                 items(habits.size) { habit ->
                     HabitChip(habit = habit.toString(), onClick = onHabitClick)
                 }
             }*/
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




