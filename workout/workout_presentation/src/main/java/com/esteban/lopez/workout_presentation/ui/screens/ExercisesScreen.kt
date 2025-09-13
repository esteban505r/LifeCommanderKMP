package com.esteban.ruano.workout_presentation.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import androidx.compose.material3.MaterialTheme

@Composable
fun ExerciseScreen(
    workoutDayId: Int? = null,
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

        state.errorMessage!=null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = state.errorMessage)
            }
        }
        else -> {
            Scaffold(
                floatingActionButtonPosition = FabPosition.Center,
                floatingActionButton = {
                    Button(
                        onClick = {
                            onNewExerciseClick?.invoke()
                        },
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
            ) {
                Exercises(
                    modifier = Modifier.padding(it),
                    state.exercises?: emptyList(),
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
            .background(MaterialTheme.colorScheme.background) // Ensure background is set
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background), // Ensure background is se
            // t
        ) {
            item {
                AppBar(title = stringResource(R.string.exercises))
            }
            items(exercises.size) { index ->
                ExerciseCard(exercise = exercises[index])
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

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "UnusedMaterialScaffoldPaddingParameter")
@Preview
@Composable
fun PreviewExerciseScreen() {
    Scaffold {
        Exercises(
            exercises = listOf(
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

        )
    }
}
