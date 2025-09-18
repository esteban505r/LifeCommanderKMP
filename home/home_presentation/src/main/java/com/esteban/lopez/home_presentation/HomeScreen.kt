package com.esteban.ruano.home_presentation

import android.content.pm.PackageManager
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CenterFocusWeak
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource as androidPainterResource
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.esteban.ruano.core.utils.devices.DeviceUtilities
import com.esteban.ruano.core_ui.R
import com.esteban.ruano.core_ui.utils.LocalMainIntent
import com.esteban.ruano.core_ui.view_model.intent.MainIntent
import com.esteban.ruano.habits_presentation.ui.intent.HabitIntent
import com.esteban.ruano.habits_presentation.ui.screens.viewmodel.HabitViewModel
import com.esteban.ruano.home_presentation.viewmodel.HomeViewModel
import com.esteban.ruano.lifecommander.models.Exercise
import com.esteban.ruano.lifecommander.ui.components.SharedAppBar
import com.esteban.ruano.lifecommander.ui.components.SharedSectionCard
import com.esteban.ruano.lifecommander.ui.components.SharedTaskCard
import com.esteban.ruano.resources.Res
import com.esteban.ruano.resources.ic_tasks_unselected
import com.esteban.ruano.resources.otter_focused
import com.esteban.ruano.resources.otter_sleepy
import com.esteban.ruano.resources.otter_happy
import com.esteban.ruano.resources.otter_proud
import com.esteban.ruano.resources.otter_idle
import com.esteban.ruano.tasks_presentation.intent.TaskIntent
import com.esteban.ruano.tasks_presentation.ui.viewmodel.TaskViewModel
import com.esteban.ruano.test_core.base.TestTags
import com.esteban.ruano.ui.*
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import com.esteban.ruano.utils.DateUIUtils.toLocalDateTime
import com.esteban.ruano.utils.HabitsUtils.findCurrentHabit
import com.esteban.ruano.workout_presentation.intent.WorkoutIntent
import com.esteban.ruano.workout_presentation.ui.composable.WorkoutCard
import com.esteban.ruano.workout_presentation.ui.viewmodel.WorkoutDetailViewModel
import com.lifecommander.models.Habit
import com.lifecommander.models.Task
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    habitViewModel: HabitViewModel = hiltViewModel(),
    taskViewModel: TaskViewModel = hiltViewModel(),
    workoutViewModel: WorkoutDetailViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel(),
    onGoToTasks: () -> Unit,
    onGoToWorkout: () -> Unit,
    onCurrentHabitClick: (Habit?) -> Unit,
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val sendMainIntent = LocalMainIntent.current

    val habitState = habitViewModel.viewState.collectAsState()
    val taskState = taskViewModel.viewState.collectAsState()
    val workoutState = workoutViewModel.viewState.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    var isFocusMode by rememberSaveable { mutableStateOf(false) }
    var focusOnlyCurrent by rememberSaveable { mutableStateOf(false) }
    var showOverflow by remember { mutableStateOf(false) }

    var isRefreshing by remember { mutableStateOf(false) }

    val version = remember {
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "0.0.0"
        } catch (_: PackageManager.NameNotFoundException) {
            "0.0.0"
        }
    }

    // --- Brand-new detection ---
    val allHabits = habitState.value.habits
    val tasks = taskState.value.tasks
    val hasWorkout = workoutState.value.workout?.exercises?.isNotEmpty() == true
    val isBrandNew = allHabits.isEmpty() && tasks.isEmpty() && !hasWorkout

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            coroutineScope.launch {
                isRefreshing = true
                habitViewModel.performAction(HabitIntent.FetchHabits())
                taskViewModel.performAction(TaskIntent.FetchTasks())
                workoutViewModel.performAction(
                    WorkoutIntent.FetchWorkoutByDay(
                        id = LocalDate.now().dayOfWeek.value.toString()
                    )
                )
                isRefreshing = false
            }
        }
    )

    // Initial fetch
    LaunchedEffect(Unit) {
        habitViewModel.performAction(HabitIntent.FetchHabits())
        taskViewModel.performAction(TaskIntent.FetchTasks())
        workoutViewModel.performAction(
            WorkoutIntent.FetchWorkoutByDay(
                id = LocalDate.now().dayOfWeek.value.toString()
            )
        )
    }

    // One-time gentle pull-to-refresh hint for brand-new users (visual education)
    LaunchedEffect(isBrandNew) {
        if (isBrandNew) {
            isRefreshing = true
            delay(700)
            isRefreshing = false
        }
    }

    Box(
        modifier = Modifier
            .testTag(TestTags.HOME_SCREEN_TEST_TAG)
            .fillMaxSize()
            .background(
                LCDS.createGradientBrush(
                    LCDS.gradients.BackgroundGradient,
                    LifeCommanderDesignSystem.GradientDirection.Vertical
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // App bar with overflow + focus toggle
            SharedAppBar(
                titleIcon = androidPainterResource(R.drawable.logo),
                title = stringResource(R.string.app_name),
                titleSize = MaterialTheme.typography.h3.fontSize,
                onSettingsClick = { DeviceUtilities.prepareAutoStartInXiaomi(context) },
                onLogoutClick = {
                    sendMainIntent(MainIntent.Logout)
                    onLogout()
                },
                actions = {
                    IconButton(onClick = {
                        isFocusMode = !isFocusMode
                        showOverflow = false
                    }) {
                        Icon(
                            if (isFocusMode) Icons.Filled.CenterFocusStrong else Icons.Outlined.CenterFocusWeak,
                            contentDescription = null
                        )
                    }
                    IconButton(
                        onClick = { showOverflow = true },
                        modifier = Modifier
                            .size(LifeCommanderDesignSystem.dimensions.TouchRecommended)
                            .clip(CircleShape)
                            .background(LifeCommanderDesignSystem.colors.Surface)
                            .border(
                                LifeCommanderDesignSystem.dimensions.BorderMedium,
                                LifeCommanderDesignSystem.colors.Border,
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            modifier = Modifier.size(LifeCommanderDesignSystem.dimensions.IconLarge),
                            tint = LifeCommanderDesignSystem.colors.OnSurfaceVariant
                        )
                    }
                    DropdownMenu(
                        expanded = showOverflow,
                        onDismissRequest = { showOverflow = false }
                    ) {
                        DropdownMenuItem(
                            onClick = {
                                showOverflow = false
                                DeviceUtilities.prepareAutoStartInXiaomi(context)
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = LifeCommanderDesignSystem.colors.OnSurfaceVariant
                                )
                                Text(
                                    text = "Settings",
                                    style = MaterialTheme.typography.body1,
                                    color = LifeCommanderDesignSystem.colors.OnSurface
                                )
                            }
                        }
                        DropdownMenuItem(
                            onClick = {
                                showOverflow = false
                                sendMainIntent(MainIntent.Logout)
                                onLogout()
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = null,
                                    tint = LifeCommanderDesignSystem.colors.Error
                                )
                                Text(
                                    text = "Logout",
                                    style = MaterialTheme.typography.body1,
                                    color = LifeCommanderDesignSystem.colors.Error
                                )
                            }
                        }
                    }
                }
            )

            // Content
            Box(
                modifier = Modifier
                    .pullRefresh(pullRefreshState)
                    .fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        horizontal = LifeCommanderDesignSystem.Layout.contentPadding,
                        vertical = LCDS.dimensions.SpacingSmall
                    ),
                    verticalArrangement = Arrangement.spacedBy(LifeCommanderDesignSystem.Layout.sectionSpacing)
                ) {

                    item {
                        if (isBrandNew) {
                            FirstRunHero(
                                onCreateHabit = { onCurrentHabitClick(null) },
                                onCreateTask = onGoToTasks,
                                onGoToWorkout = onGoToWorkout
                            )
                        } else {
                            // EXISTING: Your current welcome card
                            val pendingHabits = allHabits.count { it.done != true }
                            val pendingTasks = tasks.count { it.done != true }
                            val currentHabit = allHabits.findCurrentHabit()

                            OtterWelcomeCard(
                                pendingHabits = pendingHabits,
                                pendingTasks = pendingTasks,
                                currentHabitName = currentHabit?.name,
                                onHabitClick = { onCurrentHabitClick(currentHabit) },
                                onTasksClick = onGoToTasks
                            )
                        }
                    }

                    if (isFocusMode && !isBrandNew) {
                        // ---------- Focus Mode ----------
                        item {
                            FocusModePanel(
                                habits = allHabits,
                                currentHabit = allHabits.findCurrentHabit(),
                                tasks = tasks,
                                onlyCurrent = focusOnlyCurrent,
                                onToggleOnlyCurrent = { focusOnlyCurrent = it },
                                onOpenHabit = { onCurrentHabitClick(it) },
                                onOpenTasks = onGoToTasks,
                                onExitFocus = { isFocusMode = false }
                            )
                        }
                        item { Spacer(Modifier.height(100.dp)) }
                    } else if (!isBrandNew) {
                        // ---------- Normal Home ----------
                        item {
                            HabitsSectionOrEmpty(
                                habits = allHabits,
                                onCreateHabit = { onCurrentHabitClick(null) }
                            )
                        }

                        item {
                            TasksSectionOrEmpty(
                                tasks = tasks,
                                onGoToTasks = onGoToTasks,
                                onCreateTask = onGoToTasks // reuse tasks screen to add
                            )
                        }

                        item {
                            WorkoutSectionOrEmpty(
                                hasWorkout = hasWorkout,
                                onGoToWorkout = onGoToWorkout
                            )
                        }

                        item { Spacer(Modifier.height(100.dp)) }
                    }
                }

                // Pull refresh indicator
                PullRefreshIndicator(
                    refreshing = isRefreshing,
                    modifier = Modifier.align(Alignment.TopCenter),
                    backgroundColor = Color.White,
                    contentColor = LCDS.colors.Primary,
                    state = pullRefreshState,
                )
            }
        }

        // Floating version info
        Text(
            text = "v$version",
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .clip(LCDS.shapes.VersionBadge)
                .padding(
                    horizontal = LCDS.dimensions.PaddingMedium,
                    vertical = LCDS.dimensions.SpacingSmall
                ),
            style = MaterialTheme.typography.caption,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}


@Composable
fun HabitsSection(
    habits: List<Habit>,
    onHabitClick: (Habit) -> Unit = {},
    onToggleDone: (Habit) -> Unit = {},
    onSeeAll: () -> Unit = {}
) {
    if (habits.isEmpty()) return

    val completed = habits.count { it.done == true }
    val progress = if (habits.isNotEmpty()) completed.toFloat() / habits.size else 0f
    val preview = habits.take(3)

    Card(
        shape = RoundedCornerShape(12.dp),
        backgroundColor = LifeCommanderDesignSystem.colors.Surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Habits",
                    style = MaterialTheme.typography.h5,
                    fontWeight = FontWeight.Bold,
                    color = LifeCommanderDesignSystem.colors.OnSurface
                )
                TextButton(onClick = onSeeAll) {
                    Text("See all")
                }
            }

            // Progress
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    backgroundColor = LifeCommanderDesignSystem.colors.Border,
                )
                Text(
                    text = "$completed of ${habits.size} done today",
                    style = MaterialTheme.typography.caption,
                    color = LifeCommanderDesignSystem.colors.OnSurfaceVariant
                )
            }

            // Preview list
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                preview.forEach { habit ->
                    HabitRowItem(
                        habit = habit,
                        isCurrent = preview.findCurrentHabit() == habit,
                        onClick = { onHabitClick(habit) },
                        onToggleDone = { onToggleDone(habit) }
                    )
                }
            }

            // “+N more” footer if truncated
            val remaining = habits.size - preview.size
            if (remaining > 0) {
                TextButton(
                    onClick = onSeeAll,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("+$remaining more")
                }
            }
        }
    }
}

@Composable
private fun HabitRowItem(
    habit: Habit,
    onClick: () -> Unit,
    onToggleDone: () -> Unit,
    isCurrent: Boolean
) {
    val isDone = habit.done == true

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .background(
                if (isCurrent) LifeCommanderDesignSystem.colors.Surface.copy(alpha = 0.6f)
                else Color.Transparent
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isDone,
            onCheckedChange = { onToggleDone() }
        )
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = habit.name,
                style = MaterialTheme.typography.body1.copy(
                    fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal
                ),
                color = LifeCommanderDesignSystem.colors.OnSurface
            )
            if (isCurrent) {
                Text(
                    text = "Current",
                    style = MaterialTheme.typography.caption,
                    color = LifeCommanderDesignSystem.colors.OnSurfaceVariant
                )
            }
        }
        // Optional right icon/chevron if you use one
        // Icon(Icons.AutoMirrored.Filled.ChevronRight, contentDescription = null, tint = LifeCommanderDesignSystem.colors.OnSurfaceVariant)
    }
}


/* -------------------------- Focus Mode -------------------------- */

private fun List<Task>.currentPending(): Task? =
    this.filter { it.done != true }.minByOrNull { it.priority }

@Composable
private fun HabitsSectionOrEmpty(
    habits: List<Habit>,
    onCreateHabit: () -> Unit
) {
    if (habits.isNotEmpty()) {
        HabitsSection(habits)
    } else {
        InlineEmptyRow(
            title = "Habits",
            description = "Build your first streak with a simple habit.",
            action = "Create a Habit",
            onClick = onCreateHabit
        )
    }
}

@Composable
private fun TasksSectionOrEmpty(
    tasks: List<Task>,
    onGoToTasks: () -> Unit,
    onCreateTask: () -> Unit
) {
    if (tasks.isNotEmpty()) {
        TasksSection(tasks = tasks, onGoToTasks = onGoToTasks)
    } else {
        InlineEmptyRow(
            title = "Tasks",
            description = "Keep a lightweight list for today.",
            action = "Add a Task",
            onClick = onCreateTask
        )
    }
}

@Composable
private fun WorkoutSectionOrEmpty(
    hasWorkout: Boolean,
    onGoToWorkout: () -> Unit
) {
    if (hasWorkout) {
        WorkoutSection(
            exercises = emptyList(), // you use your real list in your existing section
            onGoToWorkout = onGoToWorkout
        )
    } else {
        InlineEmptyRow(
            title = "Workout",
            description = "Pick a light routine for today.",
            action = "Choose Workout",
            onClick = onGoToWorkout
        )
    }
}

@Composable
fun InlineEmptyRow(title: String, description: String, action: String, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        backgroundColor = LifeCommanderDesignSystem.colors.Surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.h5, fontWeight = FontWeight.SemiBold)
                Text(description, style = MaterialTheme.typography.body2, color = LifeCommanderDesignSystem.colors.OnSurfaceVariant)
            }
            Spacer(Modifier.width(12.dp))
            OutlinedButton(onClick = onClick, shape = RoundedCornerShape(10.dp)) { Text(action) }
        }
    }
}


@Composable
fun FocusModePanel(
    habits: List<Habit>,
    currentHabit: Habit?,
    tasks: List<Task>,
    onlyCurrent: Boolean,
    onToggleOnlyCurrent: (Boolean) -> Unit,
    onOpenHabit: (Habit?) -> Unit,
    onOpenTasks: () -> Unit,
    onExitFocus: () -> Unit
) {
    val visibleHabits = remember(habits, currentHabit, onlyCurrent) {
        val pending = habits.filter { it.done != true }
        if (onlyCurrent && currentHabit != null) listOf(currentHabit) else pending
    }
    val visibleTasks = remember(tasks, onlyCurrent) {
        val pending = tasks.filter { it.done != true }
        if (onlyCurrent) listOfNotNull(pending.currentPending())
        else pending.sortedBy { it.priority }.take(6)
    }

    Surface(
        shape = LifeCommanderDesignSystem.ComponentPresets.TaskCardShape,
        color = MaterialTheme.colors.surface,
        elevation = LifeCommanderDesignSystem.dimensions.ElevationSmall,
        border = BorderStroke(1.dp, MaterialTheme.colors.onSurface.copy(.08f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {

            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.CenterFocusStrong,
                        contentDescription = null,
                        tint = LCDS.colors.Primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Focus mode",
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                TextButton(onClick = onExitFocus) { Text("Exit") }
            }

            Spacer(Modifier.height(12.dp))

            // Toggle: only current
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = { onToggleOnlyCurrent(!onlyCurrent) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        if (onlyCurrent) Icons.Filled.CenterFocusStrong else Icons.Outlined.CenterFocusWeak,
                        null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(if (onlyCurrent) "Showing current" else "Show only current")
                }
                Spacer(Modifier.weight(1f))
            }

            // Habits
            Spacer(Modifier.height(12.dp))
            Text(
                if (onlyCurrent) "Habit (current)" else "Habits",
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.onSurface.copy(.7f)
            )
            Spacer(Modifier.height(6.dp))
            if (visibleHabits.isEmpty()) {
                Text(
                    "No pending habits",
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface.copy(.6f)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    visibleHabits.forEach { h ->
                        FocusHabitRow(
                            habit = h,
                            isCurrent = h.id == currentHabit?.id,
                            onClick = { onOpenHabit(h) }
                        )
                    }
                }
            }

            // Tasks
            Spacer(Modifier.height(16.dp))
            Text(
                if (onlyCurrent) "Task (current)" else "Tasks",
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.onSurface.copy(.7f)
            )
            Spacer(Modifier.height(6.dp))
            if (visibleTasks.isEmpty()) {
                Text(
                    "No pending tasks",
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface.copy(.6f)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    visibleTasks.forEach { t -> FocusTaskRow(task = t) }
                }
                if (!onlyCurrent) {
                    val more = tasks.count { it.done != true } - visibleTasks.size
                    if (more > 0) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "✨ +$more more tasks",
                            style = MaterialTheme.typography.body2.copy(
                                fontWeight = FontWeight.Medium,
                                color = LCDS.colors.Secondary
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onOpenTasks, shape = RoundedCornerShape(12.dp)) {
                    Icon(androidPainterResource(id = R.drawable.ic_tasks), null)
                    Spacer(Modifier.width(8.dp))
                    Text("Open tasks")
                }
            }
        }
    }
}

@Composable
private fun FocusHabitRow(
    habit: Habit,
    isCurrent: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isCurrent) MaterialTheme.colors.primary.copy(.08f) else MaterialTheme.colors.onSurface.copy(
            .03f
        ),
        border = if (isCurrent) BorderStroke(
            1.dp,
            MaterialTheme.colors.primary.copy(.35f)
        ) else null,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        (if (isCurrent) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface).copy(
                            .12f
                        ), CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    androidPainterResource(id = R.drawable.ic_habits),
                    contentDescription = null,
                    tint = if (isCurrent) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface.copy(
                        .7f
                    )
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(habit.name ?: "Habit", maxLines = 1)
                if (!habit.note.isNullOrBlank()) {
                    Text(
                        habit.note!!,
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(.6f),
                        maxLines = 1
                    )
                }
            }
            if (isCurrent) {
                Text(
                    "Now",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.primary
                )
            }
        }
    }
}

@Composable
private fun FocusTaskRow(task: Task) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colors.onSurface.copy(.03f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (task.done) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (task.done) Color(0xFF2ECC71) else MaterialTheme.colors.onSurface.copy(.7f)
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(task.name, maxLines = 1)
                if (!task.note.isNullOrBlank()) {
                    Text(
                        task.note!!,
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(.6f),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/* -------------------------- Otter Welcome -------------------------- */

private enum class OtterMood { Idle, Happy, Proud, Focused, Sleepy }

@Composable
private fun rememberOtterPainter(mood: OtterMood) = when (mood) {
    OtterMood.Focused -> painterResource(Res.drawable.otter_focused)
    OtterMood.Proud -> painterResource(Res.drawable.otter_proud)
    OtterMood.Happy -> painterResource(Res.drawable.otter_happy)
    OtterMood.Sleepy -> painterResource(Res.drawable.otter_sleepy)
    OtterMood.Idle -> painterResource(Res.drawable.otter_idle)
}

private fun computeOtterMood(
    pendingHabits: Int,
    pendingTasks: Int,
    currentHabitName: String?
): OtterMood = when {
    pendingHabits == 0 && pendingTasks == 0 -> OtterMood.Proud
    pendingHabits <= 1 && pendingTasks <= 2 -> OtterMood.Happy
    currentHabitName != null -> OtterMood.Focused
    pendingTasks >= 8 || pendingHabits >= 3 -> OtterMood.Sleepy
    else -> OtterMood.Idle
}

private fun moodMessage(
    mood: OtterMood,
    currentHabit: String?,
    pendingTasks: Int
): Pair<String, String> = when (mood) {
    OtterMood.Proud -> "All clear! 🎉" to "Everything is done. Enjoy some rest."
    OtterMood.Happy -> "Nice pace 🦦" to "A few things left—let’s wrap them up."
    OtterMood.Focused ->
        "Locked in" to (currentHabit?.let { "Let’s continue “$it”. I’m with you." }
            ?: "Let’s keep the streak going.")

    OtterMood.Sleepy -> "We’ve got this" to "Take a breath. Start with just one task."
    OtterMood.Idle -> "Welcome back" to "You have $pendingTasks task(s) waiting."
}



@Composable
private fun FirstRunHero(
    onCreateHabit: () -> Unit,
    onCreateTask: () -> Unit,
    onGoToWorkout: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        backgroundColor = LifeCommanderDesignSystem.colors.Surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // (Optional) Add your otter/illustration here
            Text(
                "Welcome to Life Commander 👋",
                style = MaterialTheme.typography.h4,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Let’s set up the basics so your Home isn’t empty.",
                style = MaterialTheme.typography.body1,
                color = LifeCommanderDesignSystem.colors.OnSurfaceVariant
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onCreateHabit,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Create a Habit") }

                OutlinedButton(
                    onClick = onCreateTask,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Add a Task") }
            }

            TextButton(
                onClick = onGoToWorkout,
                modifier = Modifier.align(Alignment.End)
            ) { Text("Pick a workout for today") }
        }
    }
}


@Composable
fun OtterWelcomeCard(
    pendingHabits: Int,
    pendingTasks: Int,
    currentHabitName: String?,
    onHabitClick: () -> Unit,
    onTasksClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mood = remember(pendingHabits, pendingTasks, currentHabitName) {
        computeOtterMood(pendingHabits, pendingTasks, currentHabitName)
    }
    val (title, subtitle) = remember(mood) { moodMessage(mood, currentHabitName, pendingTasks) }
    val painter = rememberOtterPainter(mood)

    Surface(
        shape = LifeCommanderDesignSystem.ComponentPresets.TaskCardShape,
        color = MaterialTheme.colors.surface,
        elevation = LifeCommanderDesignSystem.dimensions.ElevationSmall,
        border = BorderStroke(1.dp, MaterialTheme.colors.onSurface.copy(.08f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // subtle breathing animation
            val infinite = rememberInfiniteTransition(label = "otter-breathe")
            val scale by infinite.animateFloat(
                initialValue = 0.98f,
                targetValue = 1.02f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1800, easing = LinearOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )

            Box(
                modifier = Modifier
                    .size(88.dp)
                    .graphicsLayer(scaleX = scale, scaleY = scale)
                    .clip(CircleShape)
                    .background(MaterialTheme.colors.onSurface.copy(.04f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painter,
                    contentDescription = "Otter",
                    modifier = Modifier.size(76.dp)
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.body2.copy(
                        color = MaterialTheme.colors.onSurface.copy(.70f)
                    ),
                    maxLines = 2
                )

                Spacer(Modifier.height(12.dp))

            }
        }
    }
}

/* -------------------------- Sections (unchanged) -------------------------- */

@Composable
private fun TasksSection(
    tasks: List<Task>,
    onGoToTasks: () -> Unit
) {
    SharedSectionCard(
        title = stringResource(id = R.string.tasks_title),
        subtitle = "${tasks.size} tasks pending",
        iconColor = LCDS.colors.Secondary,
        onHeaderClick = onGoToTasks,
        iconContent = {
            Image(
                painter = painterResource(Res.drawable.ic_tasks_unselected),
                contentDescription = null,)
        }
    ) {
        tasks.take(3).forEach { task ->
            SharedTaskCard(
                taskName = task.name,
                taskNote = task.note,
                dueDate = task.dueDateTime?.toLocalDateTime()?.formatDefault(),
                isCompleted = task.done
            )
        }
        if (tasks.size > 3) {
            Text(
                text = "✨ +${tasks.size - 3} more tasks waiting",
                style = MaterialTheme.typography.body2.copy(
                    fontWeight = FontWeight.Medium,
                    color = LCDS.colors.Secondary
                ),
                modifier = Modifier.padding(
                    top = LCDS.dimensions.SpacingMedium,
                    start = LCDS.dimensions.SpacingExtraSmall
                )
            )
        }
    }
}

@Composable
private fun WorkoutSection(
    exercises: List<Exercise>,
    onGoToWorkout: () -> Unit
) {
    SharedSectionCard(
        title = stringResource(id = R.string.todays_workout),
        subtitle = "${exercises.size} exercises ready",
        iconColor = LCDS.colors.Secondary,
        onHeaderClick = onGoToWorkout,
        iconContent = {
            Icon(
                painter = androidPainterResource(id = R.drawable.ic_workout),
                contentDescription = null,
                modifier = Modifier.size(LCDS.dimensions.IconMedium),
                tint = Color.White
            )
        }
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = LifeCommanderDesignSystem.ComponentPresets.TaskCardShape,
            backgroundColor = LCDS.colors.SurfaceVariant,
            elevation = LCDS.dimensions.ElevationSmall
        ) {
            WorkoutCard(exercises = exercises)
        }
    }
}
