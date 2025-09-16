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
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CenterFocusWeak
import androidx.compose.material.icons.outlined.MoreVert
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.painterResource as androidPainterResource // only for your other icons
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
import com.esteban.ruano.lifecommander.ui.components.SharedGradientButton
import com.esteban.ruano.lifecommander.ui.components.SharedSectionCard
import com.esteban.ruano.lifecommander.ui.components.SharedTaskCard
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
import kotlinx.coroutines.launch
import lifecommander.shared.generated.resources.Res
import java.time.LocalDate

// --- Compose Multiplatform Resources (Option A) ---
// ⬇️ Replace `your.package` with your module's generated package
/*import your.package.generated.resources.Res
import your.package.generated.resources.images_otter_focus
import your.package.generated.resources.images_otter_happy
import your.package.generated.resources.images_otter_idle
import your.package.generated.resources.images_otter_proud
import your.package.generated.resources.images_otter_sleepy*/

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

    // Random animal (kept from your version for other UI parts if needed)
    val animalImage by remember {
        mutableIntStateOf(
            listOf(
                R.drawable.animal_sat_1,
                R.drawable.animal_sat_2,
                R.drawable.animal_sat_3,
                R.drawable.animal_sat_4,
                R.drawable.animal_sat_5,
                R.drawable.animal_sat_6,
                R.drawable.animal_sat_7,
                R.drawable.animal_sat_8
            ).random()
        )
    }

    var isRefreshing by remember { mutableStateOf(false) }

    val version = try {
        val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        pInfo.versionName
    } catch (_: PackageManager.NameNotFoundException) {
        "0.0.0"
    }

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
                title = "Life Commander",
                onSettingsClick = { DeviceUtilities.prepareAutoStartInXiaomi(context) },
                onLogoutClick = {
                    sendMainIntent(MainIntent.Logout)
                    onLogout()
                },
                actions = {
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
                        DropdownMenuItem(onClick = {
                            isFocusMode = !isFocusMode
                            showOverflow = false
                        }) {
                            Icon(
                                if (isFocusMode) Icons.Filled.CenterFocusStrong else Icons.Outlined.CenterFocusWeak,
                                contentDescription = null
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(if (isFocusMode) "Disable Focus mode" else "Enable Focus mode")
                        }
                        DropdownMenuItem(
                            onClick = {
                                focusOnlyCurrent = !focusOnlyCurrent
                                showOverflow = false
                            }
                        ) {
                            Icon(
                                imageVector = if (focusOnlyCurrent) Icons.Filled.CenterFocusStrong else Icons.Outlined.CenterFocusWeak,
                                contentDescription = null
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(if (focusOnlyCurrent) "Show lists (all)" else "Show only current")
                        }
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

                    // --- Otter welcome (replaces gradient greeting) ---
                    item {
                        val allHabits = habitState.value.habits
                        val pendingHabits = allHabits.count { it.done != true }
                        val pendingTasks = taskState.value.tasks.count { it.done != true }
                        val currentHabit = allHabits.findCurrentHabit()

                        OtterWelcomeCard(
                            pendingHabits = pendingHabits,
                            pendingTasks = pendingTasks,
                            currentHabitName = currentHabit?.name,
                            onHabitClick = { onCurrentHabitClick(currentHabit) },
                            onTasksClick = onGoToTasks
                        )
                    }

                    if (isFocusMode) {
                        // ---------- Focus Mode ----------
                        item {
                            FocusModePanel(
                                habits = habitState.value.habits,
                                currentHabit = habitState.value.habits.findCurrentHabit(),
                                tasks = taskState.value.tasks,
                                onlyCurrent = focusOnlyCurrent,
                                onToggleOnlyCurrent = { focusOnlyCurrent = it },
                                onOpenHabit = { onCurrentHabitClick(it) },
                                onOpenTasks = onGoToTasks,
                                onExitFocus = { isFocusMode = false }
                            )
                        }
                        item { Spacer(Modifier.height(100.dp)) }
                    } else {
                        // ---------- Normal Home (your original sections) ----------
                        val tasks = taskState.value.tasks
                        if (tasks.isNotEmpty()) {
                            item { TasksSection(tasks = tasks, onGoToTasks = onGoToTasks) }
                        }

                        if (workoutState.value.workout?.exercises?.isNotEmpty() == true) {
                            item {
                                WorkoutSection(
                                    exercises = workoutState.value.workout?.exercises ?: emptyList(),
                                    onGoToWorkout = onGoToWorkout
                                )
                            }
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

/* -------------------------- Focus Mode -------------------------- */

private fun List<Task>.currentPending(): Task? =
    this.filter { it.done != true }.minByOrNull { it.priority }

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
                    Icon(Icons.Filled.CenterFocusStrong, contentDescription = null, tint = LCDS.colors.Primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Focus mode", style = MaterialTheme.typography.subtitle1, fontWeight = FontWeight.SemiBold)
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
                OutlinedButton(onClick = { onToggleOnlyCurrent(!onlyCurrent) }, shape = RoundedCornerShape(12.dp)) {
                    Icon(if (onlyCurrent) Icons.Filled.CenterFocusStrong else Icons.Outlined.CenterFocusWeak, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (onlyCurrent) "Showing current" else "Show only current")
                }
                Spacer(Modifier.weight(1f))
            }

            // Habits
            Spacer(Modifier.height(12.dp))
            Text(if (onlyCurrent) "Habit (current)" else "Habits", style = MaterialTheme.typography.caption, color = MaterialTheme.colors.onSurface.copy(.7f))
            Spacer(Modifier.height(6.dp))
            if (visibleHabits.isEmpty()) {
                Text("No pending habits", style = MaterialTheme.typography.body2, color = MaterialTheme.colors.onSurface.copy(.6f))
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
            Text(if (onlyCurrent) "Task (current)" else "Tasks", style = MaterialTheme.typography.caption, color = MaterialTheme.colors.onSurface.copy(.7f))
            Spacer(Modifier.height(6.dp))
            if (visibleTasks.isEmpty()) {
                Text("No pending tasks", style = MaterialTheme.typography.body2, color = MaterialTheme.colors.onSurface.copy(.6f))
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
                            style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Medium, color = LCDS.colors.Secondary)
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
        color = if (isCurrent) MaterialTheme.colors.primary.copy(.08f) else MaterialTheme.colors.onSurface.copy(.03f),
        border = if (isCurrent) BorderStroke(1.dp, MaterialTheme.colors.primary.copy(.35f)) else null,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background((if (isCurrent) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface).copy(.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(androidPainterResource(id = R.drawable.ic_habits), contentDescription = null, tint = if (isCurrent) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface.copy(.7f))
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(habit.name ?: "Habit", maxLines = 1)
                if (!habit.note.isNullOrBlank()) {
                    Text(habit.note!!, style = MaterialTheme.typography.caption, color = MaterialTheme.colors.onSurface.copy(.6f), maxLines = 1)
                }
            }
            if (isCurrent) {
                Text("Now", style = MaterialTheme.typography.caption, color = MaterialTheme.colors.primary)
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
                    Text(task.note!!, style = MaterialTheme.typography.caption, color = MaterialTheme.colors.onSurface.copy(.6f), maxLines = 1)
                }
            }
        }
    }
}

/* -------------------------- Otter Welcome -------------------------- */

private enum class OtterMood { Idle, Happy, Proud, Focused, Sleepy }

@Composable
private fun rememberOtterPainter(mood: OtterMood) = when (mood) {
//    OtterMood.Focused -> painterResource(Res.drawable.images_otter_focused)
//    OtterMood.Proud   -> painterResource(Res.drawable.images_otter_focused)
//    OtterMood.Happy   -> painterResource(Res.drawable.images_otter_focused)
//    OtterMood.Sleepy  -> painterResource(Res.drawable.images_otter_focused)
//    OtterMood.Idle    -> painterResource(Res.drawable.images_otter_focused)
    else -> painterResource(R.drawable.ic_tasks)
}

private fun computeOtterMood(
    pendingHabits: Int,
    pendingTasks: Int,
    currentHabitName: String?
): OtterMood = when {
    pendingHabits == 0 && pendingTasks == 0 -> OtterMood.Proud
    pendingHabits <= 1 && pendingTasks <= 2 -> OtterMood.Happy
    currentHabitName != null                -> OtterMood.Focused
    pendingTasks >= 8 || pendingHabits >= 3 -> OtterMood.Sleepy
    else                                    -> OtterMood.Idle
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
                    .size(68.dp)
                    .graphicsLayer(scaleX = scale, scaleY = scale)
                    .clip(CircleShape)
                    .background(MaterialTheme.colors.onSurface.copy(.04f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painter,
                    contentDescription = "Otter",
                    modifier = Modifier.size(56.dp)
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

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onHabitClick, shape = RoundedCornerShape(12.dp)) {
                        Icon(androidPainterResource(id = R.drawable.ic_habits), null)
                        Spacer(Modifier.width(8.dp))
                        Text("Habits ($pendingHabits)")
                    }
                    Button(onClick = onTasksClick, shape = RoundedCornerShape(12.dp)) {
                        Icon(androidPainterResource(id = R.drawable.ic_tasks), null)
                        Spacer(Modifier.width(8.dp))
                        Text("Tasks ($pendingTasks)")
                    }
                }
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
            Icon(
                painter = androidPainterResource(id = R.drawable.ic_tasks),
                contentDescription = null,
                modifier = Modifier.size(LCDS.dimensions.IconMedium),
                tint = Color.White
            )
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
                modifier = Modifier.padding(top = LCDS.dimensions.SpacingMedium, start = LCDS.dimensions.SpacingExtraSmall)
            )
        }
        Spacer(modifier = Modifier.height(LCDS.dimensions.SpacingLarge))
        SharedGradientButton(
            text = stringResource(id = R.string.go_to_tasks),
            gradientColors = LCDS.gradients.SecondaryGradient,
            onClick = onGoToTasks
        )
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
        iconColor = LCDS.colors.AccentOrange,
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
        Spacer(modifier = Modifier.height(LCDS.dimensions.SpacingLarge))
        SharedGradientButton(
            text = stringResource(id = R.string.go_to_workout),
            gradientColors = listOf(LCDS.colors.AccentOrange, Color(0xFFFF7043)),
            onClick = onGoToWorkout
        )
    }
}
