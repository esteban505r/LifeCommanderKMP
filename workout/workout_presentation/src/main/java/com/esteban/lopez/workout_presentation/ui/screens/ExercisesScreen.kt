package com.esteban.ruano.workout_presentation.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.FabPosition
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.esteban.ruano.core_ui.R
import com.esteban.ruano.core_ui.composables.AppBar
import com.esteban.ruano.lifecommander.models.Exercise
import com.esteban.ruano.workout_domain.model.MuscleGroup
import com.esteban.ruano.workout_presentation.intent.ExercisesIntent
import com.esteban.ruano.workout_presentation.ui.composable.ExerciseCard
import com.esteban.ruano.workout_presentation.ui.viewmodel.state.ExercisesState

@Composable
fun ExerciseScreen(
    workoutDayId: Int? = null, // reserved for future flows
    onClose: () -> Unit,
    state: ExercisesState,
    onNewExerciseClick: (() -> Unit)? = null,
    onExerciseClick: (String?) -> Unit,
    userIntent: (ExercisesIntent) -> Unit,
) {
    when {
        state.loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        state.errorMessage != null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = state.errorMessage)
            }
        }
        else -> {
            Scaffold(
                floatingActionButtonPosition = FabPosition.Center,
                floatingActionButton = {
                    Button(
                        onClick = { onNewExerciseClick?.invoke() },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            stringResource(id = R.string.new_exercise),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            ) { paddingValues ->
                Exercises(
                    modifier = Modifier.padding(paddingValues),
                    exercises = state.exercises ?: emptyList(),
                    onExerciseClick = onExerciseClick
                )
            }
        }
    }
}

@Composable
private fun Exercises(
    modifier: Modifier = Modifier,
    exercises: List<Exercise>,
    onExerciseClick: (String?) -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            item { AppBar(title = stringResource(R.string.exercises)) }

            items(exercises.size) { index ->
                val exercise = exercises[index]
                ExerciseCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clickable { onExerciseClick(exercise.id) },

                    // required params for the rich ExerciseCard
                    exercise = exercise,
                    sets = emptyList(),                                      // no sets in this screen
                    workoutDayId = "",                                        // not used here
                    onAddSet = { _, _, _, onResult -> onResult(null) },      // no-op
                    onUpdateSetReps = { _, _ -> },                            // no-op
                    onRemoveSet = { _ -> },                                   // no-op

                    // keep the simple-card behavior
                    onUpdate = {},
                    onDelete = {},
                    onCompleteExercise = null,
                    isCompleted = false,
                    showActionButtons = false
                )
            }

            item { androidx.compose.foundation.layout.Spacer(Modifier.padding(bottom = 64.dp)) }
        }

        if (exercises.isEmpty()) {
            Text(
                text = stringResource(R.string.add_some_exercises),
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "UnusedMaterialScaffoldPaddingParameter")
@Preview
@Composable
fun PreviewExerciseScreen() {
    Scaffold {
        Exercises(
            exercises = listOf(
                Exercise(
                    id = "",
                    name = "Bench Press",
                    description = "Bench Press",
                    restSecs = 60,
                    baseSets = 3,
                    baseReps = 10,
                    muscleGroup = MuscleGroup.CORE.toString(),
                ),
                Exercise(
                    id = "",
                    name = "Squats",
                    description = "Squats",
                    restSecs = 60,
                    baseSets = 3,
                    baseReps = 10,
                    muscleGroup = MuscleGroup.LEGS.toString(),
                ),
                Exercise(
                    id = "",
                    name = "Deadlift",
                    description = "Deadlift",
                    restSecs = 60,
                    baseSets = 3,
                    baseReps = 10,
                    muscleGroup = MuscleGroup.UPPER_BODY.toString(),
                ),
            ),
            onExerciseClick = {}
        )
    }
}
