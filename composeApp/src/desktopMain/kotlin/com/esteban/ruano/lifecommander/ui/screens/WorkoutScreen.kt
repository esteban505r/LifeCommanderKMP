package com.esteban.ruano.lifecommander.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.esteban.ruano.lifecommander.models.Exercise
import com.esteban.ruano.lifecommander.ui.state.WorkoutState

@Composable
fun WorkoutScreen(
    state: WorkoutState,
    onAdd: (Exercise) -> Unit,
    onUpdate: (Exercise) -> Unit,
    onDelete: (String) -> Unit,
    onDaySelected: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Exercises", style = MaterialTheme.typography.h3)
        Spacer(Modifier.height(16.dp))
        DayChipBar(selectedDay = state.daySelected, onDaySelected = onDaySelected)
        Spacer(Modifier.height(16.dp))
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
                            onUpdate = onUpdate,
                            onDelete = onDelete
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }
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