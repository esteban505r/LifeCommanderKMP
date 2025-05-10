package com.esteban.ruano.home_presentation

import android.content.pm.PackageManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ButtonColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.esteban.ruano.core.utils.devices.DeviceUtilities
import com.esteban.ruano.core_ui.R
import com.esteban.ruano.core_ui.composables.AppBar
import com.esteban.ruano.habits_domain.model.Habit
import com.esteban.ruano.habits_presentation.ui.composables.HabitCard
import com.esteban.ruano.tasks_presentation.ui.composables.TaskCard
import com.esteban.ruano.habits_presentation.ui.intent.HabitIntent
import com.esteban.ruano.habits_presentation.ui.screens.viewmodel.HabitViewModel
import com.esteban.ruano.habits_presentation.utilities.HabitsUtils.findCurrentHabit
import com.esteban.ruano.tasks_presentation.intent.TaskIntent
import com.esteban.ruano.tasks_presentation.ui.composables.TaskTile
import com.esteban.ruano.tasks_presentation.ui.viewmodel.TaskViewModel
import com.esteban.ruano.test_core.base.TestTags
import com.esteban.ruano.workout_presentation.intent.WorkoutIntent
import com.esteban.ruano.workout_presentation.ui.composable.WorkoutCard
import com.esteban.ruano.workout_presentation.ui.viewmodel.WorkoutDetailViewModel
import com.esteban.ruano.workout_presentation.ui.viewmodel.state.WorkoutDayDetailState
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

    val isRefreshing by remember {
        mutableStateOf(false)
    }

    val version = try {
        val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        pInfo.versionName
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        "0.0.0"
    }

    val pullRefreshState =
        rememberPullRefreshState(refreshing = isRefreshing, onRefresh = {
            coroutineScope.launch {
                habitViewModel.performAction(
                    HabitIntent.FetchHabits()
                )
                taskViewModel.performAction(
                    TaskIntent.FetchTasks()
                )
                workoutViewModel.performAction(
                    WorkoutIntent.FetchWorkoutDayById(
                        id = LocalDate.now().dayOfWeek.value.toString()
                    )
                )

            }
        })

    LaunchedEffect(null) {
        habitViewModel.performAction(
            HabitIntent.FetchHabits(
            )
        )
        taskViewModel.performAction(
            TaskIntent.FetchTasks()
        )
        workoutViewModel.performAction(
            WorkoutIntent.FetchWorkoutDayById(
                id = LocalDate.now().dayOfWeek.value.toString()
            )
        )
    }

    Column(modifier = Modifier.testTag(TestTags.HOME_SCREEN_TEST_TAG)) {
        AppBar(title = "Home", actions = {
            IconButton(onClick = {
                DeviceUtilities.prepareAutoStartInXiaomi(context)
            }) {
                Image(
                    painter = painterResource(R.drawable.ic_settings),
                    contentDescription = "Settings",
                    modifier = Modifier.size(24.dp)
                )
            }
        })
        Box(
            modifier = Modifier
                .pullRefresh(pullRefreshState)
                .fillMaxWidth()
                .weight(1f)
        ) {
            LazyColumn(modifier = Modifier.padding(horizontal = 32.dp)) {
                val habit = habitState.value.habits.findCurrentHabit()
                item {
                    Box {
                        Column {
                            Spacer(modifier = Modifier.height(42.dp))
                            HabitCard(habit = habit, onHabitClick = onCurrentHabitClick)
                        }
                        Image(
                            painter = painterResource(
                                id = animalImage
                            ),
                            contentDescription = "Decoration",
                            modifier = Modifier
                                .size(80.dp)
                                .padding(start = 20.dp)
                                .align(Alignment.TopStart)
                        )
                    }
                }
                val tasks = taskState.value.tasks
                if (tasks.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    item {
                        Divider()
                    }
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onGoToTasks()
                                },
                        ) {
                            Text(
                                text = stringResource(id = R.string.tasks_title),
                                style = MaterialTheme.typography.h5
                            )
                            Image(
                                painter = painterResource(R.drawable.ic_tasks),
                                contentDescription = "Tasks",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    /*item{
                        TaskCard(
                            task = tasks[0],
                            onTaskClick = {
                                onGoToTasks()
                            }
                        )
                    }*/

                    items(tasks.size) {
                        TaskTile(task = tasks[it])
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    item{
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                        ) {
                            OutlinedButton(
                                border = BorderStroke(2.dp, MaterialTheme.colors.primary),
                                colors = ButtonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = MaterialTheme.colors.primary,
                                    disabledContainerColor = Color.Transparent,
                                    disabledContentColor = Color.Gray
                                ),
                                shape = RoundedCornerShape(50),
                                onClick = {
                                    onGoToTasks()
                                },
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .height(40.dp)
                            ) {
                                Text(
                                    stringResource(id = R.string.go_to_tasks),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                }
                if (workoutState.value.workoutDay?.exercises?.isNotEmpty() == true) {
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    item {
                        Divider()
                    }
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onGoToWorkout()
                                }
                        ) {
                            Text(
                                text = stringResource(id = R.string.todays_workout),
                                style = MaterialTheme.typography.h5
                            )
                            Image(
                                painter = painterResource(R.drawable.ic_workout),
                                contentDescription = "Workout",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        WorkoutCard(
                            exercises = workoutState.value.workoutDay?.exercises ?: emptyList()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                border = BorderStroke(2.dp, MaterialTheme.colors.primary),
                                colors = ButtonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = MaterialTheme.colors.primary,
                                    disabledContainerColor = Color.Transparent,
                                    disabledContentColor = Color.Gray
                                ),
                                shape = RoundedCornerShape(50),
                                onClick = {
                                    onGoToWorkout()
                                },
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .height(40.dp)
                            ) {
                                Text(
                                    stringResource(id = R.string.go_to_workout),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            PullRefreshIndicator(
                isRefreshing,
                pullRefreshState,
                Modifier.align(Alignment.TopCenter)
            )
        }
        Text(
            text = "Version $version",
            modifier = Modifier.padding(16.dp)
        )
    }
}

