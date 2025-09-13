package com.esteban.ruano.workout_presentation.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.esteban.ruano.core_ui.composables.AppBar
import com.esteban.ruano.workout_presentation.intent.ExerciseDetailIntent
import com.esteban.ruano.workout_presentation.ui.viewmodel.state.ExerciseDetailState
import com.esteban.ruano.workout_presentation.utils.toResourceString
import kotlinx.coroutines.launch

@Composable
fun ExerciseDetailScreen(
    state: ExerciseDetailState,
    userIntent: (ExerciseDetailIntent) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val exercise = state.exercise

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            AppBar(
                title = "Exercise Detail",
                onClose = {
                    coroutineScope.launch {
                        userIntent(ExerciseDetailIntent.NavigateUp)
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            //userIntent(ExercisesIntent.EditExercise(exercise))
                        }
                    ) {
                        Text("Edit")
                    }

                    TextButton(
                        onClick = {
                            //userIntent(ExercisesIntent.DeleteExercise(exercise))
                        }
                    ) {
                        Text("Delete")
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            DetailRow(label = "Name", value = exercise?.name ?: "")
            DetailRow(label = "Description", value = exercise?.description ?: "")
            DetailRow(label = "Rest Seconds", value = exercise?.restSecs.toString() ?: "")
            DetailRow(label = "Base Sets", value = exercise?.baseSets.toString() ?: "")
            DetailRow(label = "Base Reps", value = exercise?.baseReps.toString() ?: "")
            DetailRow(
                label = "Muscle Group",
                value = exercise?.muscleGroup ?: ""
            )

            // You could also add more info like equipment if you have it.
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = label, style = MaterialTheme.typography.subtitle1, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.body1)
    }
}
