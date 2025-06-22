package com.esteban.ruano.home_presentation

import android.content.pm.PackageManager
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.esteban.ruano.core.utils.devices.DeviceUtilities
import com.esteban.ruano.core_ui.R
import com.esteban.ruano.ui.*
import com.esteban.ruano.lifecommander.ui.components.*
import com.esteban.ruano.habits_presentation.ui.intent.HabitIntent
import com.esteban.ruano.habits_presentation.ui.screens.viewmodel.HabitViewModel
import com.esteban.ruano.tasks_presentation.intent.TaskIntent
import com.esteban.ruano.tasks_presentation.ui.viewmodel.TaskViewModel
import com.esteban.ruano.test_core.base.TestTags
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import com.esteban.ruano.utils.DateUIUtils.toLocalDateTime
import com.esteban.ruano.utils.HabitsUtils.findCurrentHabit
import com.esteban.ruano.workout_presentation.intent.WorkoutIntent
import com.esteban.ruano.workout_presentation.ui.composable.WorkoutCard
import com.esteban.ruano.workout_presentation.ui.viewmodel.WorkoutDetailViewModel
import com.lifecommander.models.Habit
import com.lifecommander.models.Task
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    habitViewModel: HabitViewModel = hiltViewModel(),
    taskViewModel: TaskViewModel = hiltViewModel(),
    workoutViewModel: WorkoutDetailViewModel = hiltViewModel(),
    onGoToTasks: () -> Unit,
    onGoToWorkout: () -> Unit,
    onCurrentHabitClick: (Habit?) -> Unit
) {
    val context = LocalContext.current

    val habitState = habitViewModel.viewState.collectAsState()
    val taskState = taskViewModel.viewState.collectAsState()
    val workoutState = workoutViewModel.viewState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

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
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
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
                    WorkoutIntent.FetchWorkoutDayById(
                        id = LocalDate.now().dayOfWeek.value.toString()
                    )
                )
                isRefreshing = false
            }
        }
    )

    LaunchedEffect(null) {
        habitViewModel.performAction(HabitIntent.FetchHabits())
        taskViewModel.performAction(TaskIntent.FetchTasks())
        workoutViewModel.performAction(
            WorkoutIntent.FetchWorkoutDayById(
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
            SharedAppBar(
                title = "Life Commander",
                onSettingsClick = { DeviceUtilities.prepareAutoStartInXiaomi(context) }
            )
            Box(
                modifier = Modifier.pullRefresh(pullRefreshState)
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
                    // Welcome section
                    item {
                        val habit = habitState.value.habits.findCurrentHabit()
                        SharedWelcomeCard(
                            greeting = LCDS.getGreeting(),
                            subtitle = "Ready to make today amazing?",
                            habitName = habit?.name,
                            habitSubtitle = if (habit != null) "Keep going! You're doing great" else "Create your first habit",
                            onHabitClick = { onCurrentHabitClick(habit) },
                            mascotContent = {
                                // Android-specific mascot content
                                Image(
                                    painter = painterResource(id = animalImage),
                                    contentDescription = "Your companion",
                                    modifier = Modifier
                                        .size(60.dp)
                                        .align(Alignment.Center)
                                )
                            }
                        )
                    }

                    // Tasks section
                    val tasks = taskState.value.tasks
                    if (tasks.isNotEmpty()) {
                        item {
                            TasksSection(
                                tasks = tasks,
                                onGoToTasks = onGoToTasks
                            )
                        }
                    }
                    
                    // Workout section
                    if (workoutState.value.workoutDay?.exercises?.isNotEmpty() == true) {
                        item {
                            WorkoutSection(
                                exercises = workoutState.value.workoutDay?.exercises ?: emptyList(),
                                onGoToWorkout = onGoToWorkout
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(100.dp))
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
            // Android-specific icon content
            Icon(
                painter = painterResource(id = R.drawable.ic_tasks),
                contentDescription = null,
                modifier = Modifier.size(LCDS.dimensions.IconMedium),
                tint = Color.White
            )
        }
    ) {
        // Task items
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
    exercises: List<com.esteban.ruano.workout_domain.model.Exercise>,
    onGoToWorkout: () -> Unit
) {
    SharedSectionCard(
        title = stringResource(id = R.string.todays_workout),
        subtitle = "${exercises.size} exercises ready",
        iconColor = LCDS.colors.AccentOrange,
        onHeaderClick = onGoToWorkout,
        iconContent = {
            // Android-specific icon content
            Icon(
                painter = painterResource(id = R.drawable.ic_workout),
                contentDescription = null,
                modifier = Modifier.size(LCDS.dimensions.IconMedium),
                tint = Color.White
            )
        }
    ) {
        // Workout preview
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

