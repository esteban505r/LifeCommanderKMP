package com.esteban.ruano.lifecommander.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.esteban.ruano.lifecommander.models.Exercise
import com.esteban.ruano.lifecommander.models.WorkoutTrack
import com.esteban.ruano.lifecommander.ui.state.WorkoutState
import ui.composables.NewEditExerciseDialog

@Composable
fun WorkoutScreen(
    state: WorkoutState,
    onAdd: (Exercise) -> Unit,
    onUpdate: (Exercise) -> Unit,
    onDelete: (String) -> Unit,
    onDaySelected: (Int) -> Unit,
    onCompleteWorkout: (String) -> Unit = {}
) {
    var showExerciseDialog by remember { mutableStateOf(false) }
    var exerciseToEdit by remember { mutableStateOf<Exercise?>(null) }
    
    // Find if today's workout is completed
    val todayWorkoutTrack = state.workoutTracks.find { track ->
        // This is a simplified check - in a real implementation, you'd check the actual workout day ID
        track.workoutDayId.contains(state.daySelected.toString())
    }
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Header with Add Exercise button
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Exercises", style = MaterialTheme.typography.h3)
            Button(
                onClick = {
                    exerciseToEdit = null
                    showExerciseDialog = true
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary),
                modifier = Modifier.height(40.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Exercise")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Exercise")
            }
        }
        DayChipBar(selectedDay = state.daySelected, onDaySelected = onDaySelected)
        Spacer(Modifier.height(16.dp))
        
        // Complete Workout Button for the selected day
        if (state.exercises.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                elevation = 2.dp,
                backgroundColor = if (todayWorkoutTrack != null) 
                    MaterialTheme.colors.surface 
                else 
                    MaterialTheme.colors.surface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.FitnessCenter,
                                contentDescription = null,
                                tint = MaterialTheme.colors.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    "Day ${state.daySelected} Workout",
                                    style = MaterialTheme.typography.h6,
                                    color = MaterialTheme.colors.primary
                                )
                                if (todayWorkoutTrack != null) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = "Completed",
                                            tint = MaterialTheme.colors.secondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            "Completed",
                                            style = MaterialTheme.typography.caption,
                                            color = MaterialTheme.colors.secondary
                                        )
                                    }
                                }
                            }
                        }
                        Button(
                            onClick = { 
                                // For now, we'll use a placeholder workout day ID
                                // In a real implementation, you'd get the actual workout day ID
                                onCompleteWorkout("workout-day-${state.daySelected}")
                            },
                            enabled = todayWorkoutTrack == null,
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = if (todayWorkoutTrack != null) 
                                    MaterialTheme.colors.onSurface.copy(alpha = 0.3f)
                                else 
                                    MaterialTheme.colors.secondary
                            )
                        ) {
                            Icon(
                                Icons.Default.CheckCircle, 
                                contentDescription = if (todayWorkoutTrack != null) "Already Completed" else "Complete"
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (todayWorkoutTrack != null) "Completed" else "Complete Workout")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "${state.exercises.size} exercises planned",
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                    if (todayWorkoutTrack != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Completed: ${todayWorkoutTrack.doneDateTime}",
                                style = MaterialTheme.typography.caption,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
        
        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.isError -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: ${state.errorMessage}", color = MaterialTheme.colors.error)
                }
            }
            state.exercises.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No exercises for this day.")
                }
            }
            else -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(state.exercises.size) { idx ->
                        val exercise = state.exercises[idx]
                        ExerciseCard(
                            exercise = exercise,
                            onUpdate = {
                                exerciseToEdit = it
                                showExerciseDialog = true
                            },
                            onDelete = onDelete
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }
        // TODO: Add ExerciseDialog here
        NewEditExerciseDialog(
            exerciseToEdit = exerciseToEdit,
            show = showExerciseDialog,
            onDismiss = {
                showExerciseDialog = false
                exerciseToEdit = null
            },
            onSave = { exercise ->
                if (exerciseToEdit == null) {
                    onAdd(exercise)
                } else {
                    onUpdate(exercise)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DayChipBar(selectedDay: Int, onDaySelected: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        (1..7).forEach { day ->
            FilterChip(
                selected = (selectedDay) == (day),
                onClick = { onDaySelected(day) },
                content = { Text(dayOfWeekLabel(day)) }
            )
        }
    }
}

fun dayOfWeekLabel(day: Int): String =
    listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat","Sun").getOrElse(day-1) { "?" }

@Composable
fun ExerciseCard(
    exercise: Exercise,
    onUpdate: (Exercise) -> Unit,
    onDelete: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(exercise.name, style = MaterialTheme.typography.h3)
            Spacer(Modifier.height(4.dp))
            Text("Rest: ${exercise.restSecs} seconds", style = MaterialTheme.typography.body1)
            Text("Sets: ${exercise.baseSets}, Reps: ${exercise.baseReps}", style = MaterialTheme.typography.body1)
            Text("Muscle Group: ${exercise.muscleGroup}", style = MaterialTheme.typography.body1)
            if (!exercise.equipment.isNullOrEmpty()) {
                Text("Equipment: ${exercise.equipment!!.joinToString(", ")}", style = MaterialTheme.typography.body1)
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { onUpdate(exercise) }) { Text("Edit") }
                TextButton(onClick = { onDelete(exercise.id) }) { Text("Delete") }
            }
        }
    }
} 