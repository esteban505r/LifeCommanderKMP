package com.esteban.ruano.workout_presentation.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.outlined.LinkOff
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.esteban.ruano.core_ui.R
import com.esteban.ruano.lifecommander.ui.components.AppBar
import com.esteban.ruano.core_ui.utils.DateUIUtils.toDayOfTheWeekString
import com.esteban.ruano.ui.SoftRed
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import com.esteban.ruano.utils.DateUIUtils.getCurrentDateTime
import com.esteban.ruano.workout_presentation.intent.WorkoutIntent
import com.esteban.ruano.workout_presentation.ui.composable.ExerciseCard
import com.esteban.ruano.workout_presentation.ui.composable.WorkoutCompletionDialog
import com.esteban.ruano.workout_presentation.ui.viewmodel.state.WorkoutDayDetailState
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun WorkoutDayDetailScreen(
    dayId: String,
    onClose: () -> Unit,
    onStartWorkout: () -> Unit,
    onAddExercisesClick: () -> Unit,
    onDeleteExercise: (String) -> Unit,
    onEditExercise: (String) -> Unit,
    onUnlinkFromDay: (String) -> Unit,
    state: WorkoutDayDetailState,
    userIntent: (WorkoutIntent) -> Unit,
) {

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showCompletionDialog by remember { mutableStateOf(false) }
    var moreOptionsExerciseId by remember { mutableStateOf<String?>(null)}

    val isRefreshing = state.isLoading
    val pullRefreshState = rememberPullRefreshState(refreshing = isRefreshing, onRefresh = {
        coroutineScope.launch {
            userIntent(WorkoutIntent.FetchWorkoutByDay(dayId))
        }
    })

    // Fetch completed exercises when workout day is loaded
    LaunchedEffect(state.workout?.id) {
        state.workout?.id?.let { dayId ->
            userIntent(WorkoutIntent.GetCompletedExercisesForDay(dayId))
        }
    }

        ExerciseMoreActionsDialog(
            moreOptionsExerciseId!=null,
            onDismiss = {
                moreOptionsExerciseId = null
            },
            onUnlinkFromDay = {
                moreOptionsExerciseId?.let{onUnlinkFromDay(it)}
                moreOptionsExerciseId = null
            },
            onDelete = {
                moreOptionsExerciseId?.let{onDeleteExercise(it)}
                moreOptionsExerciseId = null
            }
        )


    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {

        val exercises = state.workout?.exercises ?: emptyList()
        if (exercises.isEmpty()) {
            NotFoundScreen(
                context = context,
                workoutId = dayId,
            )
            Button(
                onClick = {
                    onAddExercisesClick()
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 24.dp)
            ) {
                Text(
                    stringResource(id = R.string.add_exercises),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        } else {
            Column(modifier = Modifier.padding(16.dp)) {
                Spacer(modifier = Modifier.height(16.dp))
                AppBar(title = dayId.toIntOrNull()?.toDayOfTheWeekString(LocalContext.current)?:"Workout Detail")
                Spacer(modifier = Modifier.height(24.dp))

                // Workout completion status
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    backgroundColor = if (state.isWorkoutCompleted)
                        MaterialTheme.colors.primary.copy(alpha = 0.1f)
                    else
                        MaterialTheme.colors.surface
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    if (state.isWorkoutCompleted) Icons.Default.CheckCircle else Icons.Default.CheckCircleOutline,
                                    contentDescription = if (state.isWorkoutCompleted) "Completed" else "Not Completed",
                                    tint = if (state.isWorkoutCompleted) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface.copy(
                                        alpha = 0.6f
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    if (state.isWorkoutCompleted) "Workout Completed" else "Workout In Progress",
                                    style = MaterialTheme.typography.h6.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (state.isWorkoutCompleted) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface
                                    )
                                )
                            }

                            if (!state.isWorkoutCompleted) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "${state.completedExercises.size} of ${exercises.size} exercises completed",
                                    style = MaterialTheme.typography.body2.copy(
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                    )
                                )
                            }
                        }
                        if (!state.isWorkoutCompleted) {
                            Button(
                                onClick = {
                                    if (state.completedExercises.size >= exercises.size) {
                                        state.workout?.day?.let { day ->
                                            userIntent(WorkoutIntent.CompleteWorkout(day))
                                        }
                                        return@Button
                                    }
                                    showCompletionDialog = true
                                }
                            ) {
                                Text("Complete")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    stringResource(id = R.string.exercises),
                    style = MaterialTheme.typography.h4
                )
                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(exercises) { exercise ->
                        ExerciseCard(
                            exercise = exercise,
                            onEdit = {
                                onEditExercise(
                                    it.id?:""
                                )
                            },
                            onDelete = { exerciseId ->
                                onDeleteExercise.invoke(exerciseId)
                            },
                            onCompleteExercise = {
                                state.workout?.id?.let { dayId ->
                                    userIntent(
                                        WorkoutIntent.CompleteExerciseById(
                                            exercise.id ?: "", dayId, getCurrentDateTime(
                                                TimeZone.currentSystemDefault()
                                            ).formatDefault()
                                        ) {
                                            userIntent(WorkoutIntent.FetchWorkoutByDay(dayId))
                                        }
                                    )
                                }
                            },
                            onShowMoreOptions = {
                                moreOptionsExerciseId = it
                            },
                            isCompleted = state.completedExercises.contains(exercise.id),
                            showActionButtons = true,
                            sets = state.workoutStatus.firstOrNull { it.exerciseId == exercise.id }?.setsDone
                                ?: emptyList(),
                            workoutId = state.workout?.id,
                            onAddSet = { reps, exerciseId, workoutDayId, _ ->
                                userIntent(WorkoutIntent.AddSet(exerciseId, reps, workoutDayId))
                            },
                            onUpdateSetReps = { _, _ ->

                            },
                            onRemoveSet = {
                                userIntent(WorkoutIntent.RemoveSet(it, {
                                    userIntent(WorkoutIntent.FetchWorkoutByDay(dayId))
                                }))
                            },
                            onUndoExercise = {
                                userIntent(WorkoutIntent.UndoExercise(it, {
                                    userIntent(WorkoutIntent.FetchWorkoutByDay(dayId))
                                }))
                            },
                            isAddingSet = state.isLoadingSet,
                            inProgress = getCurrentDateTime(
                                TimeZone.currentSystemDefault()
                            ).date.dayOfWeek.value == state.workout?.day
                        )
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.5f)
                        ) {
                            IconButton(
                                onClick = { onAddExercisesClick() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "Add Exercise",
                                        tint = MaterialTheme.colors.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Add Exercise",
                                        style = MaterialTheme.typography.body1.copy(
                                            color = MaterialTheme.colors.primary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom action buttons
            /*Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                if (!state.isWorkoutCompleted) {
                    Button(
                        onClick = { showCompletionDialog = true },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Text(
                            "Complete Workout",
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
                
                Button(
                    onClick = { onStartWorkout() },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(id = R.string.start_workout),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }*/

            PullRefreshIndicator(
                isRefreshing,
                pullRefreshState,
                Modifier.align(Alignment.TopCenter)
            )
        }

        // Workout completion dialog
        WorkoutCompletionDialog(
            show = showCompletionDialog,
            onDismiss = { showCompletionDialog = false },
            onConfirm = {
                state.workout?.day?.let { day ->
                    userIntent(WorkoutIntent.CompleteWorkout(day))
                }
                showCompletionDialog = false
            },
            workoutName = state.workout?.name ?: "Workout",
            totalExercises = exercises.size,
            completedExercises = state.completedExercises.size
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ExerciseMoreActionsDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onUnlinkFromDay: () -> Unit,
    onDelete: () -> Unit,
    exerciseName: String? = null
) {
    if (!visible) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = exerciseName?.let { "Options for \"$it\"" } ?: "Exercise options",
                style = MaterialTheme.typography.subtitle1,
                modifier = Modifier.padding(
                    bottom = 12.dp
                )
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OptionRow(
                    icon = Icons.Outlined.LinkOff,               // if missing, switch to Icons.Filled.LinkOff
                    title = "Unlink from this day",
                    subtitle = "Keep the exercise; remove today’s association",
                    tint = MaterialTheme.colors.primary,
                    onClick = {
                        onUnlinkFromDay()
                    }
                )
                OptionRow(
                    icon = Icons.Outlined.Menu,
                    title = "Delete exercise",
                    subtitle = "Permanently remove this exercise",
                    tint = SoftRed,
                    onClick = {
                        onDelete()
                    }
                )
            }
        },
        buttons = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun OptionRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colors.onSurface,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = MaterialTheme.colors.onSurface.copy(alpha = 0.04f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(tint.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tint)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.body1, fontWeight = FontWeight.SemiBold)
                if (!subtitle.isNullOrBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}


@Composable
fun NotFoundScreen(
    context: Context,
    workoutId: String,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        AppBar(title = workoutId.toIntOrNull()?.toDayOfTheWeekString(context) ?: "")
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.empty_workout_day),
            )
        }
    }
}