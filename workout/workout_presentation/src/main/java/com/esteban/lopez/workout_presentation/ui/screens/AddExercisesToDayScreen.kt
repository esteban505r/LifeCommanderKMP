package com.esteban.ruano.workout_presentation.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.DismissDirection.EndToStart
import androidx.compose.material.DismissValue.DismissedToStart
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.esteban.ruano.core_ui.R
import com.esteban.ruano.core_ui.composables.AppBar
import com.esteban.ruano.workout_domain.model.Exercise
import com.esteban.ruano.workout_domain.model.MuscleGroup
import com.esteban.ruano.workout_presentation.intent.ExercisesIntent
import com.esteban.ruano.workout_presentation.intent.WorkoutIntent
import com.esteban.ruano.workout_presentation.ui.composable.ExerciseCard
import com.esteban.ruano.workout_presentation.ui.viewmodel.state.ExercisesState
import com.esteban.ruano.workout_presentation.ui.viewmodel.state.WorkoutDayDetailState
import kotlinx.coroutines.launch


@Composable
fun AddExerciseToDayScreen(
    workoutDayId: String,
    onClose: () -> Unit,
    state: ExercisesState,
    workoutDayState: WorkoutDayDetailState,
    userIntent: (ExercisesIntent) -> Unit,
    detailUserIntent: (WorkoutIntent) -> Unit
) {

    var workoutDayExercises by remember {
        mutableStateOf(
            emptyList<Exercise>()
        )
    }

    val coroutineScope = rememberCoroutineScope()


    LaunchedEffect(workoutDayState) {
        workoutDayExercises =
            workoutDayState.workoutDay?.exercises
                ?: emptyList()
    }

    when{
        state.exercises!=null -> {
            AddExercisesComponent(
                state.exercises!!,
                workoutDayExercises,
                onWorkoutDayExercisesChange = {
                    workoutDayExercises = it
                },
                onSaveExercises = {
                    coroutineScope.launch {
                        detailUserIntent(
                            WorkoutIntent.UpdateWorkoutDayExercises(
                                workoutDayId,
                                workoutDayExercises
                            )
                        )
                    }

                }
            )
        }

        state.loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        state.errorMessage!=null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = state.errorMessage)
            }
        }

    }
}

@Composable
private fun AddExercisesComponent(
    allExercises: List<Exercise>,
    workoutExercises: List<Exercise>,
    onWorkoutDayExercisesChange: (List<Exercise>) -> Unit,
    onSaveExercises: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        AppBar(title = stringResource(R.string.add_exercises), actions = {
            TextButton(onClick = {
                onSaveExercises()
            }) {
                Text(stringResource(R.string.save))
            }
        })
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        ) {
            AllExercises(
                exercises = allExercises,
                workoutExercises = workoutExercises,
                onSelectedExercise = { exercise, checked ->
                    if (checked) {
                        onWorkoutDayExercisesChange(workoutExercises + exercise)
                    } else {
                        onWorkoutDayExercisesChange(workoutExercises - exercise)
                    }
                }
            )
            Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp))
            WorkoutExercises(
                workoutExercises = workoutExercises,
                onDeleted = {
                    onWorkoutDayExercisesChange(workoutExercises - it)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ColumnScope.WorkoutExercises(
    workoutExercises: List<Exercise>,
    onDeleted: (Exercise) -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                Text(
                    text = stringResource(R.string.exercises_on_this_day),
                    style = MaterialTheme.typography.h4,
                    modifier = Modifier.padding(16.dp)
                )
            }
            items(workoutExercises.size) { index ->

                val dismissState = rememberDismissState(
                    confirmStateChange = {
                        if (it == DismissedToStart) {
                            onDeleted(workoutExercises[index])
                            true
                        } else {
                            false
                        }
                    }
                )
                SwipeToDismiss(
                    directions = setOf(EndToStart),
                    background = {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Color.Red
                                ),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colors.onError,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    state = dismissState,
                ) {
                    ExerciseCard(
                        exercise = workoutExercises[index], modifier = Modifier.background(
                            MaterialTheme.colors.background
                        )
                    )
                }
            }
        }
        if (workoutExercises.isEmpty()) {
            Text(
                text = stringResource(R.string.add_some_exercises),
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}


@Composable
private fun ColumnScope.AllExercises(
    exercises: List<Exercise>,
    workoutExercises: List<Exercise> = emptyList(),
    onSelectedExercise: (Exercise, Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Text(
                    text = stringResource(R.string.all_exercises),
                    style = MaterialTheme.typography.h4,
                    modifier = Modifier.padding(16.dp)
                )
            }
            items(exercises.size) { index ->
                ExerciseCard(exercise = exercises[index],
                    checked = exercises[index] in workoutExercises,
                    onSelectExercise = { exercise, checked ->
                        onSelectedExercise(exercise, checked)
                    })
            }
        }
        if (exercises.isEmpty()) {
            Text(
                text = stringResource(R.string.add_some_exercises),
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Preview
@Composable
fun PreviewAddExercisesToDayScreen() {
    Scaffold {
        AddExercisesComponent(
            listOf(
                Exercise(
                    name = "Bench Press",
                    description = "Bench Press",
                    restSecs = 60,
                    baseSets = 3,
                    baseReps = 10,
                    muscleGroup = MuscleGroup.CORE,
                    equipment = emptyList(),
                    resource = null
                ),
                Exercise(
                    name = "Squats",
                    description = "Squats",
                    restSecs = 60,
                    baseSets = 3,
                    baseReps = 10,
                    muscleGroup = MuscleGroup.LEGS,
                    equipment = emptyList(),
                    resource = null
                ),
                Exercise(
                    name = "Deadlift",
                    description = "Deadlift",
                    restSecs = 60,
                    baseSets = 3,
                    baseReps = 10,
                    muscleGroup = MuscleGroup.UPPER_BODY,
                    equipment = emptyList(),
                    resource = null
                ),
            ),
            listOf(
                Exercise(
                    name = "Bench Press",
                    description = "Bench Press",
                    restSecs = 60,
                    baseSets = 3,
                    baseReps = 10,
                    muscleGroup = MuscleGroup.CORE,
                    equipment = emptyList(),
                    resource = null
                ),
            ),
            onWorkoutDayExercisesChange = {}
        )
    }
}
